package com.ksmpartners.ernie.model;

/**
 * Created with IntelliJ IDEA.
 * User: aalbrecht
 * Date: 3/14/13
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class StatusResponse {

    private JobStatus jobStatus;

    public StatusResponse() {}

    public StatusResponse(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

}
