package no.jhommeland.paymentapi.model;

public class TerminalPaymentReferenceModel {

    private String merchantId;

    private String referenceServiceId;

    private AdyenTerminalConfig terminalConfig;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getReferenceServiceId() {
        return referenceServiceId;
    }

    public void setReferenceServiceId(String referenceServiceId) {
        this.referenceServiceId = referenceServiceId;
    }

    public AdyenTerminalConfig getTerminalConfig() {
        return terminalConfig;
    }

    public void setTerminalConfig(AdyenTerminalConfig terminalConfig) {
        this.terminalConfig = terminalConfig;
    }
}
