package com.ksmpartners.ernie.model;

/**
 * A JSONable class used to send a report request via HTTP
 */
public class ReportRequest {

    private String reportDefId;

    public ReportRequest() {}

    public ReportRequest(String reportDefId) {
        this.reportDefId = reportDefId;
    }

    public void setReportDefId(String reportDefId) {
        this.reportDefId = reportDefId;
    }

    public String getReportDefId() {
        return reportDefId;
    }

}
