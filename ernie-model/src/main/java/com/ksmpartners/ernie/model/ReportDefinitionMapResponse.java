/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

import java.util.Map;

/**
 * A JSONable class used to send report definition information via HTTP
 */
public class ReportDefinitionMapResponse {

    private Map<String, String> reportDefMap;

    public ReportDefinitionMapResponse() {}

    public ReportDefinitionMapResponse(Map<String, String> reportDefMap) {
        this.reportDefMap = reportDefMap;
    }

    public Map<String, String> getReportDefMap() {
        return reportDefMap;
    }

    public void setReportDefMap(Map<String, String> reportDefMap) {
        this.reportDefMap = reportDefMap;
    }

}
