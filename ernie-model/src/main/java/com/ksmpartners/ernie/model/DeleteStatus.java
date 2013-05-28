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
public enum DeleteStatus {
    /** Definition deletion successful */
    SUCCESS,
    /** Definition deletion failed */
    FAILED,
    /** Definition is in use by a running job and cannot be deleted */
    FAILED_IN_USE,
    /** Definition not exist */
    NOT_FOUND;

}
