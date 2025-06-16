package no.jhommeland.paymentapi.dao;

import com.adyen.Client;
import com.adyen.enums.Environment;
import com.adyen.model.RequestOptions;
import com.adyen.model.checkout.*;
import com.adyen.service.checkout.ModificationsApi;
import com.adyen.service.checkout.PaymentsApi;
import no.jhommeland.paymentapi.model.MerchantModel;
import no.jhommeland.paymentapi.util.PaymentUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdyenPaymentsApiDao {

    public static final Logger logger = LoggerFactory.getLogger(AdyenPaymentsApiDao.class);

    private Client initializeClient(MerchantModel merchantModel) {
        if (StringUtils.isEmpty(merchantModel.getLivePrefix())) { //Treat as test environment
            return new Client(merchantModel.getAdyenApiKey(), Environment.TEST);
        } else {
            return new Client(merchantModel.getAdyenApiKey(), Environment.LIVE, merchantModel.getLivePrefix());
        }
    }

    private PaymentsApi initializePaymentsApi(MerchantModel merchantModel) {
        return new PaymentsApi(initializeClient(merchantModel));
    }

    private ModificationsApi initializeModificationsApi(MerchantModel merchantModel) {
        return new ModificationsApi(initializeClient(merchantModel));
    }

    public PaymentMethodsResponse callPaymentMethodsApi(PaymentMethodsRequest paymentMethodsRequest, MerchantModel merchantModel) {
        PaymentsApi paymentsApi = initializePaymentsApi(merchantModel);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.paymentMethods(paymentMethodsRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
        , paymentMethodsRequest);
    }

    public SessionResultResponse callSessionResultApi(String sessionId, String sessionResult, MerchantModel merchantModel) {
        PaymentsApi paymentsApi = initializePaymentsApi(merchantModel);
        return PaymentUtil.executeApiCall(() -> paymentsApi.getResultOfPaymentSession(sessionId, sessionResult), null);
    }

    public CreateCheckoutSessionResponse callCreateSessionApi(CreateCheckoutSessionRequest createCheckoutSessionRequest, MerchantModel merchantModel) {
        PaymentsApi paymentsApi = initializePaymentsApi(merchantModel);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.sessions(createCheckoutSessionRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , createCheckoutSessionRequest);
    }

    public CardDetailsResponse callCardDetailsApi(CardDetailsRequest cardDetailsRequest, MerchantModel merchantModel) {
        PaymentsApi paymentsApi = initializePaymentsApi(merchantModel);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.cardDetails(cardDetailsRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , cardDetailsRequest);
    }

    public PaymentResponse callPaymentApi(PaymentRequest paymentRequest, MerchantModel merchantModel) {
        PaymentsApi paymentsApi = initializePaymentsApi(merchantModel);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.payments(paymentRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
        , paymentRequest);
    }

    public PaymentDetailsResponse callPaymentDetailsApi(PaymentDetailsRequest paymentDetailsRequest, MerchantModel merchantModel) {
        PaymentsApi paymentsApi = initializePaymentsApi(merchantModel);
        return PaymentUtil.executeApiCall(() ->
                        paymentsApi.paymentsDetails(paymentDetailsRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , paymentDetailsRequest);
    }

    public PaymentCaptureResponse callPaymentCaptureApi(String originalPspReference, PaymentCaptureRequest captureRequest, MerchantModel merchantModel) {
        ModificationsApi modificationsApi = initializeModificationsApi(merchantModel);
        return PaymentUtil.executeApiCall(() ->
                        modificationsApi.captureAuthorisedPayment(originalPspReference, captureRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , captureRequest);
    }

    public PaymentReversalResponse callPaymentReversalApi(String originalPspReference, PaymentReversalRequest reversalRequest, MerchantModel merchantModel) {
        ModificationsApi modificationsApi = initializeModificationsApi(merchantModel);
        return PaymentUtil.executeApiCall(() ->
                        modificationsApi.refundOrCancelPayment(originalPspReference, reversalRequest, new RequestOptions().idempotencyKey(java.util.UUID.randomUUID().toString()))
                , reversalRequest);
    }

}
