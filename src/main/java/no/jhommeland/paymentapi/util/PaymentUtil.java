package no.jhommeland.paymentapi.util;

import com.adyen.model.checkout.*;
import com.adyen.service.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.jhommeland.paymentapi.exception.InternalServerErrorException;
import no.jhommeland.paymentapi.model.PaymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaymentUtil {

    public static final String PAYMENT_TYPE_SCHEME = "scheme";

    private static final Logger logger = LoggerFactory.getLogger(PaymentUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T, R> T executeApiCall(AdyenApiCall<T> function, R request) {
        try {
            if (request != null) {
                logger.info("Adyen Request: {}", PaymentUtil.convertToJsonString(request, false));
            }
            T result = function.apply();
            logger.info("Adyen Response: {}", PaymentUtil.convertToJsonString(result, false));
            return result;
        } catch (ApiException e) {
            logger.info("Adyen Error Response: {}", e.getResponseBody());
            throw new ResponseStatusException(e.getStatusCode(), e.getMessage(), e);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public static void addRequiredData(PaymentRequest paymentRequest, JsonNode clientStateData) {
        String paymentType = getPaymentType(paymentRequest.getPaymentMethod());
        if (PAYMENT_TYPE_SCHEME.equals(paymentType) && !clientStateData.path("installments").isMissingNode()) {
            paymentRequest.setInstallments(new Installments());
            if (!clientStateData.path("installments").path("plan").asText().isEmpty()) {
                paymentRequest.getInstallments().setPlan(Installments.PlanEnum.fromValue(clientStateData.path("installments").path("plan").asText()));
            }
            paymentRequest.getInstallments().setValue(clientStateData.path("installments").path("value").asInt());
        }
        if (extractTypeList(EcontextVoucherDetails.TypeEnum.class).contains(paymentType)) {
            paymentRequest.getPaymentMethod().getEcontextVoucherDetails().setFirstName(clientStateData.path("shopperName").path("firstName").asText());
            paymentRequest.getPaymentMethod().getEcontextVoucherDetails().setLastName(clientStateData.path("shopperName").path("lastName").asText());
            paymentRequest.getPaymentMethod().getEcontextVoucherDetails().setShopperEmail(clientStateData.path("shopperEmail").asText());
            paymentRequest.getPaymentMethod().getEcontextVoucherDetails().setTelephoneNumber(clientStateData.path("telephoneNumber").asText());
        }
    }

    public static List<String> extractTypeList(Class<? extends Enum<?>> enumClass) {
        return Stream.of(enumClass.getEnumConstants())
                .map(e -> objectMapper.convertValue(e, String.class))
                .collect(Collectors.toList());
    }

    public static String getPaymentType(Object paymentMethod) {
        return objectMapper.convertValue(paymentMethod, PaymentType.class).getType();
    }

    public static Boolean isUsingStoredCard(CheckoutPaymentMethod paymentMethod) {
        try {
            return paymentMethod.getCardDetails() != null && paymentMethod.getCardDetails().getStoredPaymentMethodId() != null;
        } catch (ClassCastException e ) {
            return false;
        }
    }

    public static String convertToJsonString(Object object, boolean prettyPrint) {
        try {
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            String jsonString;
            if (prettyPrint) {
                jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                jsonString = objectMapper.writeValueAsString(object);
            }

            return jsonString;

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON");
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public static String generateServiceId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0,10);
    }

}
