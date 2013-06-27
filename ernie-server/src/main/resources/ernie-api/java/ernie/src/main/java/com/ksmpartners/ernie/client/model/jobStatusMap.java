package com.ksmpartners.ernie.client.model;

import java.util.*;
public class JobStatusMap {
  /* Jobs map */
  private List<String> jobStatusMap = new ArrayList<String>();
  public List<String> getJobStatusMap() {
    return jobStatusMap;
  }
  public void setJobStatusMap(List<String> jobStatusMap) {
    this.jobStatusMap = jobStatusMap;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class JobStatusMap {\n");
    sb.append("  jobStatusMap: ").append(jobStatusMap).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

