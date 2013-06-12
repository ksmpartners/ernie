package com.wordnik.client.model

import java.util.Date
import com.wordnik.client.model.ReportEntity
case class JobEntity (
  jobId: Long,
  jobStatus: String,
  submitDate: Date,
  rptId: String,
  rptEntity: ReportEntity)

