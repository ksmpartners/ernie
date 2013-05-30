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
    /** Report definition does not support requested output format */
    FAILED_UNSUPPORTED_FORMAT,
    /** A required parameter was not supplied */
    FAILED_PARAMETER_NULL,
    /** A supplied parameter value was the wrong data type */
    FAILED_INVALID_PARAMETER_VALUES,
    /** A parameter in the definition has an unsupported data type */
    FAILED_UNSUPPORTED_PARAMETER_TYPE,
    /** The specified definition ID does not exist */
    FAILED_NO_SUCH_DEFINITION,
    /** Job was deleted */
    DELETED,
    /** Job does not exist */
    NO_SUCH_JOB;

}
