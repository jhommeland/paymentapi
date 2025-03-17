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
    private String id;

    private String merchantSettings;

    private String adyenMerchantAccount;

    @JsonIgnore
    private String returnUrl;

    @JsonIgnore
    private String adyenApiKey;

    @JsonIgnore
    private String adyenClientKey;

    @JsonIgnore
    private String hmacKey;

    @JsonIgnore
    private String securityKeyIdentifier;

    @JsonIgnore
    private String securityKeyPassphrase;

    @JsonIgnore
    private String securityKeyVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
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

    public String getHmacKey() {
        return hmacKey;
    }

    public void setHmacKey(String hmacKey) {
        this.hmacKey = hmacKey;
    }

    public String getSecurityKeyIdentifier() {
        return securityKeyIdentifier;
    }

    public void setSecurityKeyIdentifier(String securityKeyIdentifier) {
        this.securityKeyIdentifier = securityKeyIdentifier;
    }

    public String getSecurityKeyPassphrase() {
        return securityKeyPassphrase;
    }

    public void setSecurityKeyPassphrase(String securityKeyPassphrase) {
        this.securityKeyPassphrase = securityKeyPassphrase;
    }

    public String getSecurityKeyVersion() {
        return securityKeyVersion;
    }

    public void setSecurityKeyVersion(String securityKeyVersion) {
        this.securityKeyVersion = securityKeyVersion;
    }
}
