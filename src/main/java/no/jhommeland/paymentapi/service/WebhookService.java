package no.jhommeland.paymentapi.service;

import com.adyen.model.notification.NotificationRequestItem;
import jakarta.persistence.EntityNotFoundException;
import no.jhommeland.paymentapi.dao.TransactionRepository;
import no.jhommeland.paymentapi.model.AdyenWebhookModel;
import no.jhommeland.paymentapi.model.TransactionModel;
import no.jhommeland.paymentapi.util.PaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class WebhookService {

    private final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    TransactionRepository transactionRepository;

    public WebhookService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void processPaymentNotification(AdyenWebhookModel requestModel) {
        requestModel.getNotificationItems().forEach(notificationItem -> {
            NotificationRequestItem item = notificationItem.getNotificationRequestItem();
            logger.info("Event Received: {}", PaymentUtil.convertToJsonString(item));
            try {
                TransactionModel transactionModel = transactionRepository.findById(item.getMerchantReference()).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
                transactionModel.setStatus(item.getEventCode());
                if (!item.isSuccess()) {
                    transactionModel.setErrorReason(item.getReason());
                }
                transactionModel.setPspReference(item.getPspReference());
                transactionModel.setLastModifiedAt(OffsetDateTime.now());
                transactionRepository.save(transactionModel);
            } catch (Exception e) {
                logger.error("Error processing notification: eventCode={}, reference={}, message={}", item.getEventCode(), item.getMerchantReference(), e.getMessage());
            }
        });
    }

}
