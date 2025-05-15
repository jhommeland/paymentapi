package no.jhommeland.paymentapi.service;

import no.jhommeland.paymentapi.dao.EventRepository;
import no.jhommeland.paymentapi.model.EventModel;
import no.jhommeland.paymentapi.model.ReportModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final String REPORT_AVAILABLE_EVENT = "REPORT_AVAILABLE";

    private final EventRepository eventRepository;

    public ReportService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<ReportModel> getReports() {

        List<EventModel> reportAvailableEvents = eventRepository.findAllByEventCodeOrderByEventDateDesc(REPORT_AVAILABLE_EVENT);
        List<ReportModel> reportModels = new ArrayList<>();
        reportAvailableEvents.forEach((eventModel) -> {
            ReportModel reportModel = new ReportModel();
            reportModel.setMerchantAccountName(eventModel.getMerchantAccountCode());
            reportModel.setReportType(eventModel.getPspReference().split("_\\d+")[0]);
            reportModel.setCreationDate(eventModel.getEventDate());
            reportModel.setFilename(eventModel.getPspReference());
            reportModel.setReportLink(eventModel.getReason());
            reportModels.add(reportModel);
        });

        return reportModels;
    }

    public String reconcile() {
        return "";
    }

}
