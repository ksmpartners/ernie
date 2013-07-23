/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.model;

import java.util.List;

/**
 * A JSONable class used to serialize a catalog of report generation job metadata.
 */
public class JobsCatalogResponse extends ModelObject {

    private List<JobEntity> jobsCatalog;

    public JobsCatalogResponse() {}

    public JobsCatalogResponse(List<JobEntity> jobsCatalog) {
        this.jobsCatalog = jobsCatalog;
    }

    /**
     * Return the JobEntity list.
     */
    public List<JobEntity> getJobsCatalog() {
        return jobsCatalog;
    }

    /**
     * Set the JobEntity list.
     */
    public void setJobStatusMap(List<JobEntity> jobsCatalog) {
        this.jobsCatalog = jobsCatalog;
    }

}
