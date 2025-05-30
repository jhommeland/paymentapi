package no.jhommeland.paymentapi.service;

import no.jhommeland.paymentapi.dao.EventRepository;
import no.jhommeland.paymentapi.dao.MerchantRepository;
import no.jhommeland.paymentapi.dao.TransactionRepository;
import no.jhommeland.paymentapi.model.*;
import no.jhommeland.paymentapi.util.LogUtil;
import no.jhommeland.paymentapi.util.ReconciliationUtil;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final String REPORT_AVAILABLE_EVENT = "REPORT_AVAILABLE";

    private final EventRepository eventRepository;

    private final MerchantRepository merchantRepository;

    private final TransactionRepository transactionRepository;

    public ReportService(EventRepository eventRepository, MerchantRepository merchantRepository, TransactionRepository transactionRepository) {
        this.eventRepository = eventRepository;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
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

        StringBuilder reconcileLog = new StringBuilder();
        LogUtil.appendLogLine(reconcileLog, String.format("Processing %s", eventModel.getPspReference()));

        ReportType reportType = ReportType.getReportTypeFromFilename(eventModel.getPspReference());
        if (reportType != null) {
            List<CSVRecord> records = ReconciliationUtil.downloadAndParseCsv(eventModel.getReason(), merchantModel.getAdyenReportApiKey());
            reconcileTransactionStatus(records, reportType, reconcileLog);
        } else {
            LogUtil.appendLogLine(reconcileLog, "Unsupported report type: " + requestModel.getReportType());
        }

        return reconcileLog.toString();
    }

    private void reconcileTransactionStatus(List<CSVRecord> records, ReportType reportType, StringBuilder reconcileLog) {
        LogUtil.appendLogLine(reconcileLog, String.format("%d records found", records.size()));
        for (CSVRecord record : records) {
            ReconciliationModel reconciliationModel = convertToReconciliationModel(record);
            transactionRepository.findByMerchantReference(reconciliationModel.getMerchantReference()).ifPresentOrElse(
                    transaction -> {
                        LogUtil.appendLogLine(reconcileLog, String.format("(%s) Reconciling adyen status (%s -> %s)", reconciliationModel.getMerchantReference(), transaction.getAdyenStatus(), reconciliationModel.getRecordType()));
                        transaction.setAdyenStatus(reconciliationModel.getRecordType());
                        transaction.setPaymentMethod(reconciliationModel.getPaymentMethod());
                        if (ReportType.SETTLEMENT_DETAIL_REPORT == reportType) {
                            transaction.setOriginalPspReference(reconciliationModel.getPspReference());
                            transaction.setPspReference(reconciliationModel.getModificationReference());
                        } else if (ReportType.PAYMENTS_ACCOUNTING_REPORT == reportType) {
                            setPspReference(transaction, reconciliationModel);
                        }
                        transaction.setLastModifiedAt(OffsetDateTime.now());
                        TransactionStatus transactionStatus = TransactionStatus.getStatusFromAdyenStatus(reconciliationModel.getRecordType());
                        if (transactionStatus != null) {
                            LogUtil.appendLogLine(reconcileLog, String.format("(%s) Updating transaction status (%s -> %s)", reconciliationModel.getMerchantReference(), transaction.getStatus(), transactionStatus.getStatus()));
                            transaction.setAdyenStatus(reconciliationModel.getRecordType());
                            transaction.setStatus(transactionStatus.getStatus());
                        }
                        transactionRepository.save(transaction);
                    },
                    () -> {
                        LogUtil.appendLogLine(reconcileLog, "Could not find transaction " + reconciliationModel.getMerchantReference());
                    }
            );
        }
    }

    private void setPspReference(TransactionModel transactionModel, ReconciliationModel reconciliationModel) {
        if (transactionModel.getOriginalPspReference() == null) {
            transactionModel.setOriginalPspReference(reconciliationModel.getPspReference());
        } else if (!transactionModel.getOriginalPspReference().equals(reconciliationModel.getPspReference())) {
            transactionModel.setPspReference(reconciliationModel.getPspReference());
        }
    }

    private ReconciliationModel convertToReconciliationModel(CSVRecord record) {
        ReconciliationModel reconciliationModel = new ReconciliationModel();
        reconciliationModel.setMerchantReference(getRecord(record,"Merchant Reference"));
        reconciliationModel.setRecordType(getRecord(record, "Type"));
        if (reconciliationModel.getRecordType() == null) {
            reconciliationModel.setRecordType(getRecord(record, "Record Type"));
        }
        reconciliationModel.setPaymentMethod(getRecord(record, "Payment Method"));
        reconciliationModel.setPspReference(getRecord(record, "Psp Reference"));
        reconciliationModel.setModificationReference(getRecord(record, "Modification Reference"));
        return reconciliationModel;
    }

    private String getRecord(CSVRecord record, String key) {
        String value = null;
        try {
            value = record.get(key);
        } catch (IllegalArgumentException ignored) {
        }
        return value;
    }

}
