package no.jhommeland.paymentapi.model;

public enum ReportType {
    SETTLEMENT_DETAIL_REPORT("settlement_detail_report"),
    PAYMENTS_ACCOUNTING_REPORT("payments_accounting_report");

    private String filenamePrefix;

    public String getFilenamePrefix() {
        return filenamePrefix;
    }

    ReportType(String filenamePrefix) {
        this.filenamePrefix = filenamePrefix;
    }

    public static ReportType getReportTypeFromFilename(String filename) {
        for (ReportType reportType : ReportType.values()) {
            if (filename.contains(reportType.getFilenamePrefix())) {
                return reportType;
            }
        }
        return null;
    }
}
