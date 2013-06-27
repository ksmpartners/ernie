package com.wordnik.client.model;

import java.util.*;
import com.wordnik.client.model.JobEntity;
public class JobsCatalogResponse {
  private List<JobEntity> jobsCatalog = new ArrayList<JobEntity>();
  public List<JobEntity> getJobsCatalog() {
    return jobsCatalog;
  }
  public void setJobsCatalog(List<JobEntity> jobsCatalog) {
    this.jobsCatalog = jobsCatalog;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class JobsCatalogResponse {\n");
    sb.append("  jobsCatalog: ").append(jobsCatalog).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

