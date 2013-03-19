package com.ksmpartners.ernie.model;

import java.util.Map;

public class ReportDefinitionMap {

    private Map<String, String> reportDefMap;

    public ReportDefinitionMap() {}

    public ReportDefinitionMap(Map<String, String> reportDefMap) {
        this.reportDefMap = reportDefMap;
    }

    public Map<String, String> getReportDefMap() {
        return reportDefMap;
    }

    public void setReportDefMap(Map<String, String> reportDefMap) {
        this.reportDefMap = reportDefMap;
    }

}
