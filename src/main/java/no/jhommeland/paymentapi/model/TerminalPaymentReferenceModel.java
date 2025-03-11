package no.jhommeland.paymentapi.model;

public class TerminalPaymentReferenceModel {

    private String merchantId;

    private String poiId;

    private String referenceServiceId;

    private String apiType;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getPoiId() {
        return poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public String getReferenceServiceId() {
        return referenceServiceId;
    }

    public void setReferenceServiceId(String referenceServiceId) {
        this.referenceServiceId = referenceServiceId;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }
}
