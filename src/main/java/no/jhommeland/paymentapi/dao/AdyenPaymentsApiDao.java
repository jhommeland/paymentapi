package no.jhommeland.paymentapi.dao;

import com.adyen.Client;
import com.adyen.enums.Environment;
import com.adyen.model.RequestOptions;
import com.adyen.model.checkout.*;
import com.adyen.service.checkout.ModificationsApi;
import com.adyen.service.checkout.PaymentsApi;
import com.adyen.service.exception.ApiException;
import jakarta.annotation.PostConstruct;
import no.jhommeland.paymentapi.exception.InternalServerErrorException;
import no.jhommeland.paymentapi.util.AdyenApiCall;
import no.jhommeland.paymentapi.util.PaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AdyenPaymentsApiDao {

    public static final Logger logger = LoggerFactory.getLogger(AdyenPaymentsApiDao.class);

    @Value("${adyen.api.key}")
    private String adyenApiKey;

    private PaymentsApi paymentsService;

    private ModificationsApi modificationsService;

    @PostConstruct
    void initApiClient() {
        paymentsService = new PaymentsApi(new Client(adyenApiKey, Environment.TEST));
        modificationsService = new ModificationsApi(new Client(adyenApiKey, Environment.TEST));
    }

    public static <T, R> T executeApiCall(AdyenApiCall<T> function, R request) {
        try {
            logger.info("Adyen Request: {}", PaymentUtil.convertToJsonString(request));
            T result = function.apply();
            logger.info("Adyen Response: {}", PaymentUtil.convertToJsonString(result));
            return result;
        } catch (ApiException e) {
            logger.info("Adyen Error Response: {}", e.getResponseBody());
            throw new ResponseStatusException(e.getStatusCode(), e.getMessage(), e);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public PaymentMethodsResponse callPaymentMethodsApi(PaymentMethodsRequest paymentMethodsRequest) {
        return AdyenPaymentsApiDao.executeApiCall(() ->
                paymentsService.paymentMethods(paymentMethodsRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
        , paymentMethodsRequest);
    }

    public CreateCheckoutSessionResponse callCreateSessionApi(CreateCheckoutSessionRequest createCheckoutSessionRequest) {
        return AdyenPaymentsApiDao.executeApiCall(() ->
                        paymentsService.sessions(createCheckoutSessionRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , createCheckoutSessionRequest);
    }

    public PaymentResponse callPaymentApi(PaymentRequest paymentRequest) {
        return AdyenPaymentsApiDao.executeApiCall(() ->
                paymentsService.payments(paymentRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
        , paymentRequest);
    }

    public PaymentCaptureResponse callPaymentCaptureApi(String originalPspReference, PaymentCaptureRequest captureRequest) {
        return AdyenPaymentsApiDao.executeApiCall(() ->
                        modificationsService.captureAuthorisedPayment(originalPspReference, captureRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , captureRequest);
    }

    public PaymentReversalResponse callPaymentReversalApi(String originalPspReference, PaymentReversalRequest reversalRequest) {
        return AdyenPaymentsApiDao.executeApiCall(() ->
                        modificationsService.refundOrCancelPayment(originalPspReference, reversalRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , reversalRequest);
    }

    public PaymentDetailsResponse callPaymentDetailsApi(PaymentDetailsRequest paymentDetailsRequest) {
        return AdyenPaymentsApiDao.executeApiCall(() ->
                paymentsService.paymentsDetails(paymentDetailsRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
        , paymentDetailsRequest);
    }

}
