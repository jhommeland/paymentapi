package no.jhommeland.paymentapi.model;

import java.util.List;

public class AdyenWebhookModel {

    public Boolean live;

    public List<AdyenNotificationModel> notificationItems;

    public Boolean getLive() {
        return live;
    }

    public void setLive(Boolean live) {
        this.live = live;
    }

    public List<AdyenNotificationModel> getNotificationItems() {
        return notificationItems;
    }

    public void setNotificationItems(List<AdyenNotificationModel> notificationItems) {
        this.notificationItems = notificationItems;
    }
}
