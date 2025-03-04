package no.jhommeland.paymentapi.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.jhommeland.paymentapi.exception.InternalServerErrorException;
import no.jhommeland.paymentapi.model.PaymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentUtil {

    private static final Logger logger = LoggerFactory.getLogger(PaymentUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getPaymentType(Object paymentMethod) {
        return objectMapper.convertValue(paymentMethod, PaymentType.class).getType();
    }

    public static String convertToJsonString(Object object) {
        try {
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON");
            throw new InternalServerErrorException(e.getMessage());
        }
    }

}
