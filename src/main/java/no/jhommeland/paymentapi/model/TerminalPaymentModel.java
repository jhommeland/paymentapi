package no.jhommeland.paymentapi.model;

public class TerminalPaymentModel {

    private String merchantId;

    private String serviceId;

    private String operator;

    private String amount;

    private String currency;

    private String printReceipt;

    private AdyenTerminalConfig terminalConfig;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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

    public String getPrintReceipt() {
        return printReceipt;
    }

    public void setPrintReceipt(String printReceipt) {
        this.printReceipt = printReceipt;
    }

    public AdyenTerminalConfig getTerminalConfig() {
        return terminalConfig;
    }

    public void setTerminalConfig(AdyenTerminalConfig terminalConfig) {
        this.terminalConfig = terminalConfig;
    }
}
