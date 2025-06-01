package no.jhommeland.paymentapi.api;

import no.jhommeland.paymentapi.model.ReportModel;
import no.jhommeland.paymentapi.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ReportModel>> getReports() {
        HttpHeaders headers = new HttpHeaders();
        List<ReportModel> reports = reportService.getReports();
        return new ResponseEntity<>(reports, headers, HttpStatus.OK);
    }

    @PostMapping("/reconcile")
    public ResponseEntity<String> reconcile(@RequestBody ReportModel requestModel) {
        HttpHeaders headers = new HttpHeaders();
        String result = reportService.reconcile(requestModel);
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }
}
