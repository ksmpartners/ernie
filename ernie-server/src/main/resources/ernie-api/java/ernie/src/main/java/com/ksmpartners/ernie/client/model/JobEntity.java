package com.ksmpartners.ernie.client.model;

import java.util.Date;
import com.ksmpartners.ernie.client.model.ReportEntity;
public class JobEntity {
  private Long jobId = null;
  private String jobStatus = null;
  private Date submitDate = null;
  private String rptId = null;
  private ReportEntity rptEntity = null;
  public Long getJobId() {
    return jobId;
  }
  public void setJobId(Long jobId) {
    this.jobId = jobId;
  }

  public String getJobStatus() {
    return jobStatus;
  }
  public void setJobStatus(String jobStatus) {
    this.jobStatus = jobStatus;
  }

  public Date getSubmitDate() {
    return submitDate;
  }
  public void setSubmitDate(Date submitDate) {
    this.submitDate = submitDate;
  }

  public String getRptId() {
    return rptId;
  }
  public void setRptId(String rptId) {
    this.rptId = rptId;
  }

  public ReportEntity getRptEntity() {
    return rptEntity;
  }
  public void setRptEntity(ReportEntity rptEntity) {
    this.rptEntity = rptEntity;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class JobEntity {\n");
    sb.append("  jobId: ").append(jobId).append("\n");
    sb.append("  jobStatus: ").append(jobStatus).append("\n");
    sb.append("  submitDate: ").append(submitDate).append("\n");
    sb.append("  rptId: ").append(rptId).append("\n");
    sb.append("  rptEntity: ").append(rptEntity).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

