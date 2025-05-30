package no.jhommeland.paymentapi.service;

import com.adyen.model.notification.NotificationRequestItem;
import com.adyen.util.HMACValidator;
import jakarta.persistence.EntityNotFoundException;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.dao.TransactionRepository;
import no.jhommeland.paymentapi.dao.EventRepository;
import no.jhommeland.paymentapi.model.*;
import no.jhommeland.paymentapi.util.PaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class EventService {

    private final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final String EVENT_IS_SUCCESS = "true";

    private final String EVENT_IS_NOT_SUCCESS = "false";

    private final HMACValidator hmacValidator = new HMACValidator();

    private final EventRepository eventRepository;

    private final MerchantRepository merchantRepository;

    private final TransactionRepository transactionRepository;

    public EventService(MerchantRepository merchantRepository, TransactionRepository transactionRepository, EventRepository eventRepository) {
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
    }

    public void processPaymentNotification(AdyenWebhookModel requestModel) {
        requestModel.getNotificationItems().forEach(notificationItem -> {
            NotificationRequestItem item = notificationItem.getNotificationRequestItem();
            EventModel eventModel = convertToEventModel(item);
            eventRepository.save(eventModel);
            try {
                MerchantModel merchantModel = merchantRepository.findByAdyenMerchantAccount(eventModel.getMerchantAccountCode()).orElseThrow(() -> new EntityNotFoundException("Merchant not found"));
                if (!hmacValidator.validateHMAC(item, merchantModel.getHmacKey())) {
                    throw new EntityNotFoundException("Invalid HMAC Data");
                }
                TransactionModel transactionModel = transactionRepository.findByMerchantReference(eventModel.getMerchantReference()).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
                if (transactionModel.getStatus().equals(eventModel.getEventCode())) {
                    logger.info("Skipping duplicated event. eventCode={}, id={}", eventModel.getEventCode(), eventModel.getId());
                }
                transactionModel.setPaymentMethod(eventModel.getPaymentMethod());
                if (!eventModel.getSuccess().equals(EVENT_IS_SUCCESS)) {
                    transactionModel.setPspReference(null);
                    transactionModel.setErrorReason(eventModel.getEventCode() + ": " + eventModel.getReason());
                } else {
                    transactionModel.setStatus(eventModel.getEventCode());
                    setPspReference(transactionModel, eventModel);
                }
                transactionModel.setLastModifiedAt(OffsetDateTime.now());
                transactionRepository.save(transactionModel);
            } catch (Exception e) {
                logger.error("Error processing event: id={}, errorMessage={}", eventModel.getId(), e.getMessage());
            }
        });
    }

    private void setPspReference(TransactionModel transactionModel, EventModel eventModel) {
        if (transactionModel.getOriginalPspReference() == null) {
            transactionModel.setOriginalPspReference(eventModel.getPspReference());
        } else if (!transactionModel.getOriginalPspReference().equals(eventModel.getPspReference())) {
            transactionModel.setPspReference(eventModel.getPspReference());
        }
    }

    private EventModel convertToEventModel(NotificationRequestItem item) {
        EventModel eventModel = new EventModel();
        eventModel.setEventCode(item.getEventCode());
        eventModel.setEventDate(OffsetDateTime.ofInstant(item.getEventDate().toInstant(), ZoneOffset.ofHours(9)));
        eventModel.setReceivedDate(OffsetDateTime.now());
        eventModel.setMerchantAccountCode(item.getMerchantAccountCode());
        eventModel.setMerchantReference(item.getMerchantReference());
        eventModel.setPspReference(item.getPspReference());
        eventModel.setPaymentMethod(item.getPaymentMethod());
        eventModel.setCurrency(item.getAmount().getCurrency());
        eventModel.setAmount(String.valueOf(item.getAmount().getValue()));
        eventModel.setReason(item.getReason());
        eventModel.setSuccess(item.isSuccess() ? EVENT_IS_SUCCESS : EVENT_IS_NOT_SUCCESS);
        eventModel.setAdditionalData(PaymentUtil.convertToJsonString(item.getAdditionalData(), true));
        return eventModel;
    }

}
