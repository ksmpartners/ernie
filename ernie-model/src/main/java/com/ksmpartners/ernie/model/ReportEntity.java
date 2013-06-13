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

import java.util.Map;

/**
 * A JSONable class used to serialize report meta-data to disk
 */
public class ReportEntity extends ModelObject {

    private DateTime createdDate;
    private DateTime startDate;
    private DateTime finishDate;
    private DateTime retentionDate;
    private String rptId;
    private String sourceDefId;
    private String createdUser;
    private Map<String, String> params;
    private ReportType reportType;

    public ReportEntity() {}

    public ReportEntity(DateTime createdDate, DateTime retentionDate, String rptId, String sourceDefId, String createdUser, Map<String, String> params, ReportType reportType, DateTime startDate, DateTime finishDate) {
        this.createdDate = createdDate;
        this.retentionDate = retentionDate;
        this.rptId = rptId;
        this.sourceDefId = sourceDefId;
        this.createdUser = createdUser;
        this.params = params;
        this.reportType = reportType;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getCreatedDate() {
        return createdDate;
    }

    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getRetentionDate() {
        return retentionDate;
    }

    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setRetentionDate(DateTime retentionDate) {
        this.retentionDate = retentionDate;
    }

    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getStartDate() {
        return startDate;
    }

    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getFinishDate() {
        return finishDate;
    }

    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setFinishDate(DateTime finishDate) {
        this.finishDate = finishDate;
    }


    public String getRptId() {
        return rptId;
    }

    public void setRptId(String rptId) {
        this.rptId = rptId;
    }

    public String getSourceDefId() {
        return sourceDefId;
    }

    public void setSourceDefId(String sourceDefId) {
        this.sourceDefId = sourceDefId;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
}
