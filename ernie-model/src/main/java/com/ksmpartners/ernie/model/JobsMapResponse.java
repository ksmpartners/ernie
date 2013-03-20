/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

import java.util.Map;

/**
 * A JSONable class used to send jobs information via HTTP
 */
public class JobsMapResponse {

    private Map<String, String> jobsMap;

    public JobsMapResponse() {}

    public JobsMapResponse(Map<String, String> jobsMap) {
        this.jobsMap = jobsMap;
    }

    public Map<String, String> getJobStatusMap() {
        return jobsMap;
    }

    public void setJobStatusMap(Map<String, String> jobsMap) {
        this.jobsMap = jobsMap;
    }

}
