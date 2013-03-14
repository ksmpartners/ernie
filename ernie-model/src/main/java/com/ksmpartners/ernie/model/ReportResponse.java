package com.ksmpartners.ernie.model;

public class ReportResponse {

    private long jobId;

    public ReportResponse() {}

    public ReportResponse(long jobId) {
        this.jobId = jobId;
    }

    public long getJobId() {
        return jobId;
    }

    public void setjobId(long jobId) {
        this.jobId = jobId;
    }
}
