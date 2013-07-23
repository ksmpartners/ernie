/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved. 
 */

package com.ksmpartners.ernie.model;

/**
 * A JSONable class used to serialize report response information.
 */
public class ReportResponse extends ModelObject {

    private long jobId;
    private JobStatus jobStatus;

    public ReportResponse() {}

    public ReportResponse(long jobId, JobStatus jobStatus) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
    }

    /**
     * Return the internal identifier for this report generation task.
     */
    public long getJobId() {
        return jobId;
    }

    /**
     * Set the internal identifier for this report generation task.
     */
    public void setjobId(long jobId) {
        this.jobId = jobId;
    }

    /**
     * Return the report generation status.
     */
    public JobStatus getJobStatus() {
        return jobStatus;
    }

    /**
     * Set the report generation status.
     */
    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }


}
