package no.jhommeland.paymentapi.model;

import com.adyen.model.nexo.PaymentResult;

public class TerminalPaymentResponseModel {

    String result;

    PaymentResult details;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public PaymentResult getDetails() {
        return details;
    }

    public void setDetails(PaymentResult details) {
        this.details = details;
    }
}
