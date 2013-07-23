/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

import scala.Predef;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

/**
 * A JSONable class used to serialize a report request.
 */
public class ReportRequest extends ModelObject {

    private String defId;
    private ReportType rptType;
    private int retentionDays;
    private Map<String,String> reportParameters;


    public ReportRequest() {}

    public ReportRequest(String defId, ReportType rptType, int retentionDays, Map<String,String> reportParameters) {
        this.defId = defId;
        this.rptType = rptType;
        this.retentionDays = retentionDays;
        this.reportParameters = reportParameters;
    }

    /**
     * Set the internal identifier for the report definition to be used during report generation.
     */
    public void setDefId(String defId) {
        this.defId = defId;
    }

    /**
     * Return the internal identifier for the report definition to be used during report generation.
     */
    public String getDefId() {
        return defId;
    }

    /**
     * Set the output format.
     */
    public void setRptType(ReportType rptType) {
        this.rptType = rptType;
    }

    /**
     * Return the output format.
     */
    public ReportType getRptType() {
        return rptType;
    }

    /**
     * Set the number of days from report generation until the output expires.
     */
    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    /**
     * Return the number of days from report generation until the output expires.
     */
    public int getRetentionDays() {
        return retentionDays;
    }

    /**
     * Set the map of report parameter names to values.
     */
    public void setReportParameters(Map<String,String> reportParameters) {
        this.reportParameters = reportParameters;
    }

    /**
     * Return the map of report parameter names to values.
     */
    public Map<String,String> getReportParameters() {
        return reportParameters;
    }
}
