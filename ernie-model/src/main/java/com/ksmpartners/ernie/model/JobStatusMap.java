/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

import java.util.Map;

/**
 * A JSONable class used to send job status information via HTTP
 */
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
