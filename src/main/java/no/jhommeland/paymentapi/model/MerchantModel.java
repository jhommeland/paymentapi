package no.jhommeland.paymentapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "merchants")
public class MerchantModel {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid2")
    private String merchantId;

    private String merchantSettings;

    private String adyenMerchantAccount;

    @JsonIgnore
    private String adyenApiKey;

    @JsonIgnore
    private String adyenClientKey;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantSettings() {
        return merchantSettings;
    }

    public void setMerchantSettings(String merchantSettings) {
        this.merchantSettings = merchantSettings;
    }

    public String getAdyenMerchantAccount() {
        return adyenMerchantAccount;
    }

    public void setAdyenMerchantAccount(String adyenMerchantAccount) {
        this.adyenMerchantAccount = adyenMerchantAccount;
    }

    public String getAdyenApiKey() {
        return adyenApiKey;
    }

    public void setAdyenApiKey(String adyenApiKey) {
        this.adyenApiKey = adyenApiKey;
    }

    public String getAdyenClientKey() {
        return adyenClientKey;
    }

    public void setAdyenClientKey(String adyenClientKey) {
        this.adyenClientKey = adyenClientKey;
    }
}
