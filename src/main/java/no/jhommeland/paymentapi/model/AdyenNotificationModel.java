package no.jhommeland.paymentapi.model;

import com.adyen.model.notification.NotificationRequestItem;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdyenNotificationModel {

    @JsonProperty("NotificationRequestItem")
    private NotificationRequestItem notificationRequestItem;

    public NotificationRequestItem getNotificationRequestItem() {
        return notificationRequestItem;
    }

    public void setNotificationRequestItem(NotificationRequestItem notificationRequestItem) {
        this.notificationRequestItem = notificationRequestItem;
    }
}