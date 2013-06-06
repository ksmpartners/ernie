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
    private JobStatus jobStatus;
    private DateTime submitDate;
    private String rptId;
    private ReportEntity rptEntity;

    public JobEntity() {}

    public JobEntity(Long jobId, JobStatus jobStatus, DateTime submitDate, String rptId, ReportEntity rptEntity) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
        this.submitDate = submitDate;
        this.rptId = rptId;
        this.rptEntity = rptEntity;
    }

    public String getRptId() {
        return rptId;
    }

    public ReportEntity getRptEntity() {
        return rptEntity;
    }

    public void setRptId(String rptId) {
        this.rptId = rptId;
    }

    public void setRptEntity(ReportEntity rptEntity) {
        this.rptEntity = rptEntity;
    }

    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getSubmitDate() {
        return submitDate;
    }

    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setSubmitDate(DateTime submitDate) {
        this.submitDate = submitDate;
    }


    public Long getJobId() {
        return jobId;
    }



    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

}
