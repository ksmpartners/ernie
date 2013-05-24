/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.  
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved. 
 */

package com.ksmpartners.ernie.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ksmpartners.ernie.util.ISODateDeserializer;
import com.ksmpartners.ernie.util.ISODateSerializer;
import org.joda.time.DateTime;

import java.util.List;

/**
 * A JSONable class used to serialize definition meta-data to disk
 */
public class DefinitionEntity extends ModelObject {

    private DateTime createdDate;
    private String defId;
    private String createdUser;
    private List<String> paramNames;
    private String defDescription;
    private List<ReportType> unsupportedReportTypes;

    public DefinitionEntity() {}

    public DefinitionEntity(DateTime createdDate, String defId, String createdUser, List<String> paramNames, String defDescription, List<ReportType> unsupportedReportTypes) {
        this.createdDate = createdDate;
        this.defId = defId;
        this.createdUser = createdUser;
        this.paramNames = paramNames;
        this.defDescription = defDescription;
        this.unsupportedReportTypes = unsupportedReportTypes;
    }

    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getCreatedDate() {
        return createdDate;
    }

    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setCreatedDate(DateTime createdDate) {
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

    public List<ReportType> getUnsupportedReportTypes() {
        return unsupportedReportTypes;
    }

    public void setUnsupportedReportTypes(List<ReportType> unsupportedReportTypes) {
        this.unsupportedReportTypes = unsupportedReportTypes;
    }
}
