package com.wordnik.client.model;

import java.util.*;
public class JobsMapResponse {
  /* Jobs map */
  private List<String> jobsStatusMap = new ArrayList<String>();
  public List<String> getJobsStatusMap() {
    return jobsStatusMap;
  }
  public void setJobsStatusMap(List<String> jobsStatusMap) {
    this.jobsStatusMap = jobsStatusMap;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class JobsMapResponse {\n");
    sb.append("  jobsStatusMap: ").append(jobsStatusMap).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

