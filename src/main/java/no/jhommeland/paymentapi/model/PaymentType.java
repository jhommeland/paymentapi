package no.jhommeland.paymentapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentType {

    private String type;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

}
