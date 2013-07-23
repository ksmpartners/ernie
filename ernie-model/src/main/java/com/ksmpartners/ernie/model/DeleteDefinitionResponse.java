/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved. 
 */

package com.ksmpartners.ernie.model;

/**
 * A JSONable class used to send report response information via HTTP
 */
public class DeleteDefinitionResponse extends ModelObject {

    private DeleteStatus deleteStatus;

    public DeleteDefinitionResponse() {}

    public DeleteDefinitionResponse(DeleteStatus deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    /**
     *
     * @return
     */
    public DeleteStatus getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(DeleteStatus deleteStatus) {
        this.deleteStatus = deleteStatus;
    }
}
