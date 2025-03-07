package no.jhommeland.paymentapi.dao;

import com.adyen.Client;
import com.adyen.enums.Environment;
import com.adyen.model.RequestOptions;
import com.adyen.model.checkout.*;
import com.adyen.service.checkout.ModificationsApi;
import com.adyen.service.checkout.PaymentsApi;
import no.jhommeland.paymentapi.util.PaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdyenPaymentsApiDao {

    public static final Logger logger = LoggerFactory.getLogger(AdyenPaymentsApiDao.class);

    private PaymentsApi initializePaymentsApi(String adyenApiKey) {
        return new PaymentsApi(new Client(adyenApiKey, Environment.TEST));
    }

    private ModificationsApi initializeModificationsApi(String adyenApiKey) {
        return new ModificationsApi(new Client(adyenApiKey, Environment.TEST));
    }

    public PaymentMethodsResponse callPaymentMethodsApi(PaymentMethodsRequest paymentMethodsRequest, String adyenApiKey) {
        PaymentsApi paymentsApi = initializePaymentsApi(adyenApiKey);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.paymentMethods(paymentMethodsRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
        , paymentMethodsRequest);
    }

    public CreateCheckoutSessionResponse callCreateSessionApi(CreateCheckoutSessionRequest createCheckoutSessionRequest, String adyenApiKey) {
        PaymentsApi paymentsApi = initializePaymentsApi(adyenApiKey);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.sessions(createCheckoutSessionRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , createCheckoutSessionRequest);
    }

    public PaymentResponse callPaymentApi(PaymentRequest paymentRequest, String adyenApiKey) {
        PaymentsApi paymentsApi = initializePaymentsApi(adyenApiKey);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.payments(paymentRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
        , paymentRequest);
    }

    public PaymentDetailsResponse callPaymentDetailsApi(PaymentDetailsRequest paymentDetailsRequest, String adyenApiKey) {
        PaymentsApi paymentsApi = initializePaymentsApi(adyenApiKey);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.paymentsDetails(paymentDetailsRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , paymentDetailsRequest);
    }

    public PaymentCaptureResponse callPaymentCaptureApi(String originalPspReference, PaymentCaptureRequest captureRequest, String adyenApiKey) {
        ModificationsApi modificationsApi = initializeModificationsApi(adyenApiKey);
        return PaymentUtil.executeApiCall(() ->
                        modificationsApi.captureAuthorisedPayment(originalPspReference, captureRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , captureRequest);
    }

    public PaymentReversalResponse callPaymentReversalApi(String originalPspReference, PaymentReversalRequest reversalRequest, String adyenApiKey) {
        ModificationsApi modificationsApi = initializeModificationsApi(adyenApiKey);
        return PaymentUtil.executeApiCall(() ->
                        modificationsApi.refundOrCancelPayment(originalPspReference, reversalRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , reversalRequest);
    }

}
