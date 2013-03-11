/** This source code file is the intellectual property of KSM Technology Partners LLC.  
  * The contents of this file may not be reproduced, published, or distributed in any
  * form, except as allowed in a license agreement between KSM Technology Partners LLC
  * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved. 
  */

package com.ksmpartners.ernie.model;

/**
 * A JSONable classed used to send job status information via HTTP
 */
public class Notification {
    private long jobId;
    private JobStatus jobStatus;

    public Notification() {}

    public Notification(long jobId, JobStatus jobStatus) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
    }

    public long getJobId() {
        return jobId;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }
}
