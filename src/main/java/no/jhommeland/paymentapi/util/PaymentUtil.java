package no.jhommeland.paymentapi.util;

import com.adyen.model.checkout.CheckoutPaymentMethod;
import com.adyen.service.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.jhommeland.paymentapi.exception.InternalServerErrorException;
import no.jhommeland.paymentapi.model.PaymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

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

    public static String getPaymentType(Object paymentMethod) {
        return objectMapper.convertValue(paymentMethod, PaymentType.class).getType();
    }

    public static Boolean isUsingStoredCard(CheckoutPaymentMethod paymentMethod) {
        return paymentMethod.getCardDetails() != null && paymentMethod.getCardDetails().getStoredPaymentMethodId() != null;
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
