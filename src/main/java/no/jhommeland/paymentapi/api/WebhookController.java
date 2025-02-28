package no.jhommeland.paymentapi.api;

import no.jhommeland.paymentapi.model.AdyenWebhookModel;
import no.jhommeland.paymentapi.service.WebhookService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/payments/notification")
    public ResponseEntity<String> paymentNotification(@RequestBody AdyenWebhookModel requestModel) {
        HttpHeaders headers = new HttpHeaders();

        //TODO: Validation HMAC and Basic Authentication

        webhookService.processPaymentNotification(requestModel);
        return new ResponseEntity<>("", headers, HttpStatus.NO_CONTENT);
    }

}
