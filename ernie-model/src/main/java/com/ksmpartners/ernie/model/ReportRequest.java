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
 * A JSONable class used to send a report request via HTTP
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

    public void setDefId(String defId) {
        this.defId = defId;
    }

    public String getDefId() {
        return defId;
    }

    public void setRptType(ReportType rptType) {
        this.rptType = rptType;
    }

    public ReportType getRptType() {
        return rptType;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setReportParameters(Map<String,String> reportParameters) {
        this.reportParameters = reportParameters;
    }

    public Map<String,String> getReportParameters() {
        return reportParameters;
    }
}
