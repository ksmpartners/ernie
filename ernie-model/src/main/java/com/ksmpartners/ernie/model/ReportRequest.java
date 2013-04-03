/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

/**
 * A JSONable class used to send a report request via HTTP
 */
public class ReportRequest {

    private String defId;
    private ReportType rptType;

    public ReportRequest() {}

    public ReportRequest(String defId, ReportType rptType) {
        this.defId = defId;
        this.rptType = rptType;
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

}
