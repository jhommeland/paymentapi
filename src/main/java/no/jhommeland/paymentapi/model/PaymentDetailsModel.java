package no.jhommeland.paymentapi.model;

import com.adyen.model.checkout.PaymentDetailsRequest;

public class PaymentDetailsModel {

    private String merchantId;

    private PaymentDetailsRequest paymentDetails;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public PaymentDetailsRequest getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(PaymentDetailsRequest paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
