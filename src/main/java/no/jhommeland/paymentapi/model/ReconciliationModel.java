package no.jhommeland.paymentapi.model;

public class ReconciliationModel {

    private String reportType = "";

    private String merchantReference;

    private String recordType;

    private String paymentMethod;

    private String pspReference;

    private String modificationReference;

    public String getMerchantReference() {
        return merchantReference;
    }

    public void setMerchantReference(String merchantReference) {
        this.merchantReference = merchantReference;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPspReference() {
        return pspReference;
    }

    public void setPspReference(String pspReference) {
        this.pspReference = pspReference;
    }

    public String getModificationReference() {
        return modificationReference;
    }

    public void setModificationReference(String modificationReference) {
        this.modificationReference = modificationReference;
    }
}
