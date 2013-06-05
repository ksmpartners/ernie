/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.  
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved. 
 */

package com.ksmpartners.ernie.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ksmpartners.ernie.util.ISODateDeserializer;
import com.ksmpartners.ernie.util.ISODateSerializer;
import org.joda.time.DateTime;

import java.util.List;

/**
 * A JSONable class used to serialize definition meta-data to disk
 */
public class JobEntity extends ModelObject {

    private Long jobId;
    private String defId;
    private JobStatus jobStatus;
    private ReportType rptType;

    public JobEntity() {}

    public JobEntity(Long jobId, String defId, JobStatus jobStatus, ReportType rptType) {
        this.jobId = jobId;
        this.defId = defId;
        this.jobStatus = jobStatus;
        this.rptType = rptType;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getDefId() {
        return defId;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public ReportType getRptType() {
        return rptType;
    }
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public void setDefId(String defId) {
        this.defId = defId;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void setRptType(ReportType rptType) {
        this.rptType = rptType;
    }

}
