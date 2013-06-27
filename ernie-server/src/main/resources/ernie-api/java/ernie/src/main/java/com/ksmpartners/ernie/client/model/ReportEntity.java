package com.ksmpartners.ernie.client.model;

import java.util.Date;
import java.util.*;
public class ReportEntity {
  private Date createdDate = null;
  private Date startDate = null;
  private Date finishDate = null;
  private Date retentionDate = null;
  private String rptId = null;
  private String sourceDefId = null;
  private String createdUser = null;
  /* Report parameters */
  private List<String> params = new ArrayList<String>();
  private String reportType = null;
  public Date getCreatedDate() {
    return createdDate;
  }
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getStartDate() {
    return startDate;
  }
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getFinishDate() {
    return finishDate;
  }
  public void setFinishDate(Date finishDate) {
    this.finishDate = finishDate;
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

  public List<String> getParams() {
    return params;
  }
  public void setParams(List<String> params) {
    this.params = params;
  }

  public String getReportType() {
    return reportType;
  }
  public void setReportType(String reportType) {
    this.reportType = reportType;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportEntity {\n");
    sb.append("  createdDate: ").append(createdDate).append("\n");
    sb.append("  startDate: ").append(startDate).append("\n");
    sb.append("  finishDate: ").append(finishDate).append("\n");
    sb.append("  retentionDate: ").append(retentionDate).append("\n");
    sb.append("  rptId: ").append(rptId).append("\n");
    sb.append("  sourceDefId: ").append(sourceDefId).append("\n");
    sb.append("  createdUser: ").append(createdUser).append("\n");
    sb.append("  params: ").append(params).append("\n");
    sb.append("  reportType: ").append(reportType).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

