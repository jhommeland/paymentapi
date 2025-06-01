package no.jhommeland.paymentapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import no.jhommeland.paymentapi.util.ReferenceUtil;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@Entity
@Table(name = "transactions")
public class TransactionModel {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid2")
    private String id;

    private String merchantReference = ReferenceUtil.generateReference();

    private String merchantAccountName;

    private String shopperInteraction;

    private String originalPspReference;

    private String pspReference;

    private String paymentMethod;

    private String status;

    private String amount;

    private String currency;

    private String adyenStatus;

    private String errorReason;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime createdAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime lastModifiedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMerchantReference() {
        return merchantReference;
    }

    public void setMerchantReference(String merchantReference) {
        this.merchantReference = merchantReference;
    }

    public String getMerchantAccountName() {
        return merchantAccountName;
    }

    public void setMerchantAccountName(String merchantAccountName) {
        this.merchantAccountName = merchantAccountName;
    }

    public String getShopperInteraction() {
        return shopperInteraction;
    }

    public void setShopperInteraction(String shopperInteraction) {
        this.shopperInteraction = shopperInteraction;
    }

    public String getOriginalPspReference() {
        return originalPspReference;
    }

    public void setOriginalPspReference(String originalPspReference) {
        this.originalPspReference = originalPspReference;
    }

    public String getPspReference() {
        return pspReference;
    }

    public void setPspReference(String pspReference) {
        this.pspReference = pspReference;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAdyenStatus() {
        return adyenStatus;
    }

    public void setAdyenStatus(String adyenStatus) {
        this.adyenStatus = adyenStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(OffsetDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

}
