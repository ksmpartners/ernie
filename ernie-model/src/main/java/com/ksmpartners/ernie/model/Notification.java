package com.ksmpartners.ernie.model;

public class Notification {
    private int jobId;
    private JobStatus jobStatus;

    public Notification() {}

    public Notification(int jobId, JobStatus jobStatus) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
    }

    public int getJobId() {
        return jobId;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }
}
