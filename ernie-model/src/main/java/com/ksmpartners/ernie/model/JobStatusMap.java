package com.ksmpartners.ernie.model;

import java.util.Map;

public class JobStatusMap {

    private Map<Long, JobStatus> jobStatusMap;

    public JobStatusMap() {}

    public JobStatusMap(Map<Long, JobStatus> jobStatusMap) {
        this.jobStatusMap = jobStatusMap;
    }

    public Map<Long, JobStatus> getJobStatusMap() {
        return jobStatusMap;
    }

    public void setJobStatusMap(Map<Long, JobStatus> jobStatusMap) {
        this.jobStatusMap = jobStatusMap;
    }

}
