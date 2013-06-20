/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine.ErnieCoordinator
import com.ksmpartners.ernie.engine.report.ReportManager

/**
 * Trait that indicates a requirement on a Coordinator
 */
trait RequiresCoordinator {
  protected def coordinator: ErnieCoordinator
  protected def timeout: Long
  protected def jobsDir: String
}

/**
 * Trait that indicates a requirement on a ReportManager
 */
trait RequiresReportManager {
  protected def reportManager: ReportManager
  protected def outputDir: String
  protected def defDir: String
}