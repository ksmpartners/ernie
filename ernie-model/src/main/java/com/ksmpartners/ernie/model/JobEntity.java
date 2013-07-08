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
 * A JSONable class used to serialize definition meta-data to disk
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

    public String getRptId() {
        return rptId;
    }

    public ReportEntity getRptEntity() {
        return rptEntity;
    }

    public void setRptId(String rptId) {
        this.rptId = rptId;
    }

    public void setRptEntity(ReportEntity rptEntity) {
        this.rptEntity = rptEntity;
    }

    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getSubmitDate() {
        return submitDate;
    }

    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setSubmitDate(DateTime submitDate) {
        this.submitDate = submitDate;
    }


    public Long getJobId() {
        return jobId;
    }



    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

}
