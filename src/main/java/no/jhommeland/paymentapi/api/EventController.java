package no.jhommeland.paymentapi.api;

import no.jhommeland.paymentapi.model.AdyenWebhookModel;
import no.jhommeland.paymentapi.model.ReportModel;
import no.jhommeland.paymentapi.service.EventService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ReportModel>> getReports() {
        HttpHeaders headers = new HttpHeaders();
        List<ReportModel> reports = eventService.getReports();
        return new ResponseEntity<>(reports, headers, HttpStatus.OK);
    }

    @PostMapping("/payments/notification")
    public ResponseEntity<String> paymentNotification(@RequestBody AdyenWebhookModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        eventService.processPaymentNotification(requestModel);
        return new ResponseEntity<>("", headers, HttpStatus.NO_CONTENT);
    }

}
