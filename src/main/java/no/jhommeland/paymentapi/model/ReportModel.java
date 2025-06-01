package no.jhommeland.paymentapi.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

public class ReportModel {

    private String eventId;

    private String merchantAccountName;

    private String reportType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime creationDate;

    private String filename;

    private String reportLink;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getMerchantAccountName() {
        return merchantAccountName;
    }

    public void setMerchantAccountName(String merchantAccountName) {
        this.merchantAccountName = merchantAccountName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getReportLink() {
        return reportLink;
    }

    public void setReportLink(String reportLink) {
        this.reportLink = reportLink;
    }
}
