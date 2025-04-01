package no.jhommeland.paymentapi.model;

import com.adyen.model.checkout.BrowserInfo;
import com.adyen.model.checkout.CheckoutPaymentMethod;
import com.adyen.model.checkout.PaymentRequest;

public class PaymentModel {

    private String merchantId;

    private String shopperId;

    private String amount;

    private String currency;

    private String countryCode;

    private String locale;

    private String tdsMode;

    private CheckoutPaymentMethod paymentMethod;

    private BrowserInfo browserInfo;

    private String origin;

    private String savePaymentMethod;

    private String shopperInteraction = PaymentRequest.ShopperInteractionEnum.ECOMMERCE.toString();

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getShopperId() {
        return shopperId;
    }

    public void setShopperId(String shopperId) {
        this.shopperId = shopperId;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTdsMode() {
        return tdsMode;
    }

    public void setTdsMode(String tdsMode) {
        this.tdsMode = tdsMode;
    }

    public CheckoutPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(CheckoutPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BrowserInfo getBrowserInfo() {
        return browserInfo;
    }

    public void setBrowserInfo(BrowserInfo browserInfo) {
        this.browserInfo = browserInfo;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getSavePaymentMethod() {
        return savePaymentMethod;
    }

    public void setSavePaymentMethod(String savePaymentMethod) {
        this.savePaymentMethod = savePaymentMethod;
    }

    public String getShopperInteraction() {
        return shopperInteraction;
    }

    public void setShopperInteraction(String shopperInteraction) {
        this.shopperInteraction = shopperInteraction;
    }
}
