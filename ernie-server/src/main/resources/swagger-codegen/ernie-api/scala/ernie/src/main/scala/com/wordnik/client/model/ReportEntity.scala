package com.wordnik.client.model

import java.util.Date
case class ReportEntity (
  createdDate: Date,
  startDate: Date,
  finishDate: Date,
  retentionDate: Date,
  rptId: String,
  sourceDefId: String,
  createdUser: String,
  /* Report parameters */
  params: List[String],
  reportType: String)

