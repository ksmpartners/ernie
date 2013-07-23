/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

import java.util.Map;

/**
 * A JSONable class used to serialize a map of report generation job identifiers.
 */
public class JobsMapResponse extends ModelObject {

    private Map<String, String> jobsMap;

    public JobsMapResponse() {}

    public JobsMapResponse(Map<String, String> jobsMap) {
        this.jobsMap = jobsMap;
    }

    /**
     * Return the map of report generation job identifiers
     */
    public Map<String, String> getJobStatusMap() {
        return jobsMap;
    }

    /**
     * Set the map of report generation job identifiers
     */
    public void setJobStatusMap(Map<String, String> jobsMap) {
        this.jobsMap = jobsMap;
    }

}
