/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ksmpartners.ernie.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ksmpartners.ernie.util.ISODateDeserializer;
import com.ksmpartners.ernie.util.ISODateSerializer;
import org.joda.time.DateTime;

import java.util.List;

/**
 * A JSONable class used to serialize report generation job metadata.
 */
public class JobEntity extends ModelObject {

    private Long jobId;
    private JobStatus jobStatus;
    private DateTime submitDate;
    private String rptId;
    private ReportEntity rptEntity;

    public JobEntity() {}

    public JobEntity(Long jobId, JobStatus jobStatus, DateTime submitDate, String rptId, ReportEntity rptEntity) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
        this.submitDate = submitDate;
        this.rptId = rptId;
        this.rptEntity = rptEntity;
    }

    /**
     * Return the internal identifier for the generated report.
     */
    public String getRptId() {
        return rptId;
    }

    /**
     * Return the object containing the report output metadata. When the job completes, the ReportEntity will be removed from the JobEntity and persisted alongside the report output.
     */
    public ReportEntity getRptEntity() {
        return rptEntity;
    }

    /**
     * Set the internal identifier for the generated report
     */
    public void setRptId(String rptId) {
        this.rptId = rptId;
    }

    /**
     * Set the object containing the report output metadata. When the job completes, the ReportEntity will be removed from the JobEntity and persisted alongside the report output.
     */
    public void setRptEntity(ReportEntity rptEntity) {
        this.rptEntity = rptEntity;
    }

    /**
     * Return job submission date.
     */
    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getSubmitDate() {
        return submitDate;
    }

    /**
     * Set job submission date.
     */
    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setSubmitDate(DateTime submitDate) {
        this.submitDate = submitDate;
    }


    /**
     * Return the internal identifier for the report generation job.
     */
    public Long getJobId() {
        return jobId;
    }

    /**
     * Return the status of report generation.
     */
    public JobStatus getJobStatus() {
        return jobStatus;
    }

    /**
     * Set the internal identifier for the report generation job.
     */
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    /**
     * Set the status of report generation.
     */
    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

}
