/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.  
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved. 
 */

package com.ksmpartners.ernie.model;

import java.util.Date;
import java.util.List;

/**
 * A JSONable class used to serialize definition meta-data to disk
 */
public class DefinitionEntity {

    private Date createdDate;
    private String defId;
    private String createdUser;
    private List<String> paramNames;
    private String defDescription;

    public DefinitionEntity() {}

    public DefinitionEntity(Date createdDate, String defId, String createdUser, List<String> paramNames, String defDescription) {
        this.createdDate = createdDate;
        this.defId = defId;
        this.createdUser = createdUser;
        this.paramNames = paramNames;
        this.defDescription = defDescription;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getDefId() {
        return defId;
    }

    public void setDefId(String defId) {
        this.defId = defId;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public void setParamNames(List<String> paramNames) {
        this.paramNames = paramNames;
    }

    public String getDefDescription() {
        return defDescription;
    }

    public void setDefDescription(String defDescription) {
        this.defDescription = defDescription;
    }
}
