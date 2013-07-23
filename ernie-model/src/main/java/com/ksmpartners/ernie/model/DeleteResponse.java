/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved. 
 */

package com.ksmpartners.ernie.model;

/**
 * A JSONable class used to serialize metadata for report output deletion
 */
public class DeleteResponse extends ModelObject {

    private DeleteStatus deleteStatus;

    public DeleteResponse() {}

    public DeleteResponse(DeleteStatus deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    /**
     * Return deletion status
     */
    public DeleteStatus getDeleteStatus() {
        return deleteStatus;
    }

    /**
     * Set deletion status
     */
    public void setDeleteStatus(DeleteStatus deleteStatus) {
        this.deleteStatus = deleteStatus;
    }
}
