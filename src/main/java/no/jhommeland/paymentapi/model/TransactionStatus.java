package no.jhommeland.paymentapi.model;

public enum TransactionStatus {
    REGISTERED("REGISTERED"),
    AWAITING_AUTHORISATION("AWAITING_AUTHORISATION"),
    AUTHORISATION("AUTHORISATION"),
    AWAITING_PAYMENT("AWAITING_PAYMENT"),
    AWAITING_CAPTURE("AWAITING_CAPTURE"),
    CAPTURE("CAPTURE"),
    AWAITING_REVERSAL("AWAITING_REVERSAL"),
    CANCEL_OR_REFUND("CANCEL_OR_REFUND"),
    CANCELLATION("CANCELLATION"),
    REFUND("REFUND");

    private final String status;

    TransactionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}