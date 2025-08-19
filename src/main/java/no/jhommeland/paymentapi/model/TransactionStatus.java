package no.jhommeland.paymentapi.model;

import java.util.List;

public enum TransactionStatus {
    REGISTERED("REGISTERED", List.of()),
    AWAITING_AUTHORISATION("AWAITING_AUTHORISATION", List.of()),
    AUTHORISATION("AUTHORISATION", List.of("Authorised")),
    AWAITING_PAYMENT("AWAITING_PAYMENT", List.of()),
    AWAITING_CAPTURE("AWAITING_CAPTURE", List.of()),
    CAPTURE("CAPTURE", List.of("Settled", "SentForSettle")),
    AWAITING_REVERSAL("AWAITING_REVERSAL", List.of()),
    CANCEL_OR_REFUND("CANCEL_OR_REFUND", List.of()),
    CANCELLATION("CANCELLATION", List.of("Cancelled")),
    REFUND("REFUND", List.of("Refunded", "SentForRefund")),
    REFUSED("REFUSED", List.of("Refused"));

    private final String status;

    private final List<String> adyenStatusList;

    TransactionStatus(String status, List<String> adyenStatusList) {
        this.status = status;
        this.adyenStatusList = adyenStatusList;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getAdyenStatusList() {
        return adyenStatusList;
    }

    public static TransactionStatus getStatusFromAdyenStatus(String adyenStatus) {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {
            if (transactionStatus.getAdyenStatusList().contains(adyenStatus)) {
                return transactionStatus;
            }
        }
        return null;
    }

}