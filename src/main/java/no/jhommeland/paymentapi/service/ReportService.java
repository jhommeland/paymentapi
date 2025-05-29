package no.jhommeland.paymentapi.service;

import no.jhommeland.paymentapi.dao.EventRepository;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.model.EventModel;
import no.jhommeland.paymentapi.model.MerchantModel;
import no.jhommeland.paymentapi.model.ReportModel;
import no.jhommeland.paymentapi.util.ReconciliationUtil;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final String SETTLEMENT_DETAIL_REPORT_TYPE = "settlement_detail_report";

    private final String PAYMENTS_ACCOUNT_REPORT_TYPE = "payments_account_report";

    private final String REPORT_AVAILABLE_EVENT = "REPORT_AVAILABLE";

    private final EventRepository eventRepository;

    private final MerchantRepository merchantRepository;

    public ReportService(EventRepository eventRepository, MerchantRepository merchantRepository) {
        this.eventRepository = eventRepository;
        this.merchantRepository = merchantRepository;
    }

    public List<ReportModel> getReports() {

        List<EventModel> reportAvailableEvents = eventRepository.findAllByEventCodeOrderByEventDateDesc(REPORT_AVAILABLE_EVENT);
        List<ReportModel> reportModels = new ArrayList<>();
        reportAvailableEvents.forEach((eventModel) -> {
            ReportModel reportModel = new ReportModel();
            reportModel.setEventId(eventModel.getId());
            reportModel.setMerchantAccountName(eventModel.getMerchantAccountCode());
            reportModel.setReportType(eventModel.getPspReference().split("_\\d+")[0]);
            reportModel.setCreationDate(eventModel.getEventDate());
            reportModel.setFilename(eventModel.getPspReference());
            reportModel.setReportLink(eventModel.getReason());
            reportModels.add(reportModel);
        });

        return reportModels;
    }

    public String reconcile(ReportModel requestModel) {

        EventModel eventModel = eventRepository.findById(requestModel.getEventId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event not found"));

        MerchantModel merchantModel = merchantRepository.findByAdyenMerchantAccount(eventModel.getMerchantAccountCode()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant not found"));

        List<CSVRecord> records = ReconciliationUtil.downloadAndParseCsv(eventModel.getReason(), merchantModel.getAdyenReportApiKey());
        if (eventModel.getReason().contains(SETTLEMENT_DETAIL_REPORT_TYPE)) {
            return reconcileSdr(records);
        } else if (eventModel.getReason().contains(PAYMENTS_ACCOUNT_REPORT_TYPE)) {
            return reconcilePar(records);
        }

        return "Unsupported report type: " + requestModel.getReportType();
    }

    private String reconcileSdr(List<CSVRecord> records) {

        StringBuilder reconcileLog = new StringBuilder(String.format("=== Processing %d records ===\n", records.size()));
        for (CSVRecord record : records) {
            reconcileLog.append(String.format("Creation Date=%s Type=%s, PspReference=%s\n", record.get("Creation Date"), record.get("Type"), record.get("Psp Reference")));
        }

        return reconcileLog.toString();
    }

    private String reconcilePar(List<CSVRecord> records) {

        StringBuilder reconcileLog = new StringBuilder(String.format("=== Processing %d records ===\n", records.size()));
        for (CSVRecord record : records) {
            reconcileLog.append(String.format("Booking Date=%s Record Type=%s, PspReference=%s\n", record.get("Booking Date"), record.get("Record Type"), record.get("Psp Reference")));
        }

        return reconcileLog.toString();
    }

}
