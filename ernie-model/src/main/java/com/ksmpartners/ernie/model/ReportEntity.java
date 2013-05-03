/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.  
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved. 
 */

package com.ksmpartners.ernie.model;

import java.util.Date;
import java.util.Map;

/**
 * A JSONable class used to serialize report meta-data to disk
 */
public class ReportEntity {

    private Date createdDate;
    private Date retentionDate;
    private String rptId;
    private String sourceDefId;
    private String createdUser;
    private Map<String, String> params;
    private ReportType reportType;

    public ReportEntity() {}

    public ReportEntity(Date createdDate, Date retentionDate, String rptId, String sourceDefId, String createdUser, Map<String, String> params, ReportType reportType) {
        this.createdDate = createdDate;
        this.retentionDate = retentionDate;
        this.rptId = rptId;
        this.sourceDefId = sourceDefId;
        this.createdUser = createdUser;
        this.params = params;
        this.reportType = reportType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getRetentionDate() {
        return retentionDate;
    }

    public void setRetentionDate(Date retentionDate) {
        this.retentionDate = retentionDate;
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
