package no.jhommeland.paymentapi.model;

public enum TransactionStatus {
    REGISTERED("REGISTERED"),
    AWAITING_AUTHORISATION("AWAITING_AUTHORISATION"),
    AWAITING_PAYMENT("AWAITING_PAYMENT"),
    AWAITING_CAPTURE("AWAITING_CAPTURE"),
    AWAITING_REVERSAL("AWAITING_REVERSAL");

    private final String status;

    TransactionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}