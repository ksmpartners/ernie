/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

/**
 * Enumeration used to indicate the status of a given job.
 */
public enum JobStatus {

    /** Job has been requested, but not started */
    PENDING,
    /** Job has been started */
    IN_PROGRESS,
    /** Job has completed successfully */
    COMPLETE,
    /** Job has failed processing */
    FAILED,
    /** Job retention date is in the past */
    FAILED_RETENTION_DATE_PAST,
    /** Job retention date after the maximum */
    FAILED_RETENTION_DATE_EXCEEDS_MAXIMUM,
    /** Job was deleted */
    DELETED,
    /** Job does not exist */
    NO_SUCH_JOB;

}
