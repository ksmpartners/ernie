/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

/**
 * Interface used to designate that a class is serializable to JSON using Jackson
 */
public abstract class ModelObject {

    public static final String TYPE_PREFIX = "application";
    public static final String TYPE_POSTFIX = "vnd.ksmpartners.ernie+json";

    /**
     * Method that returns the custom MIME type of the serialized object
     */
    public String cType() {
        return TYPE_PREFIX + "/" + TYPE_POSTFIX;
    }

}
