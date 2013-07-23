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

import java.util.Map;

/**
 * A JSONable class used to serialize report output metadata.
 */
public class ReportEntity extends ModelObject {

    private DateTime createdDate;
    private DateTime startDate;
    private DateTime finishDate;
    private DateTime retentionDate;
    private String rptId;
    private String sourceDefId;
    private String createdUser;
    private Map<String, String> params;
    private ReportType reportType;

    public ReportEntity() {}

    public ReportEntity(DateTime createdDate, DateTime retentionDate, String rptId, String sourceDefId, String createdUser, Map<String, String> params, ReportType reportType, DateTime startDate, DateTime finishDate) {
        this.createdDate = createdDate;
        this.retentionDate = retentionDate;
        this.rptId = rptId;
        this.sourceDefId = sourceDefId;
        this.createdUser = createdUser;
        this.params = params;
        this.reportType = reportType;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    /**
     * Return the date of the creation of this metadata.
     */
    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getCreatedDate() {
        return createdDate;
    }

    /**
     * Set the date of the creation of this metadata.
     */
    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Return the date on which this metadata and associated output will expire.
     */
    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getRetentionDate() {
        return retentionDate;
    }

    /**
     * Set the date on which this metadata and associated output will expire.
     */
    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setRetentionDate(DateTime retentionDate) {
        this.retentionDate = retentionDate;
    }

    /**
     * Return the date on which report output generation begun.
     */
    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getStartDate() {
        return startDate;
    }

    /**
     * Set the date on which report output generation begun.
     */
    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * Return the date on which report output generation completed.
     * @return
     */
    @JsonSerialize(using = ISODateSerializer.class)
    public DateTime getFinishDate() {
        return finishDate;
    }

    /**
     * Set the date on which report output generation completed.
     */
    @JsonDeserialize(using = ISODateDeserializer.class)
    public void setFinishDate(DateTime finishDate) {
        this.finishDate = finishDate;
    }

    /**
     * Return the internal identifier for this metadata and associated report output.
     */
    public String getRptId() {
        return rptId;
    }

    /**
     * Set the internal identifier for this metadata and associated report output.
     */
    public void setRptId(String rptId) {
        this.rptId = rptId;
    }

    /**
     * Return the internal identifier of the report design used to generate this report.
     */
    public String getSourceDefId() {
        return sourceDefId;
    }

    /**
     * Set the internal identifier of the report design used to generate this report.
     */
    public void setSourceDefId(String sourceDefId) {
        this.sourceDefId = sourceDefId;
    }

    /**
     * Return an identifier for the user that submitted this report generation job.
     */
    public String getCreatedUser() {
        return createdUser;
    }

    /**
     * Set the identifier for the user that submitted this report generation job.
     */
    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    /**
     * Return the mapping of report parameter names to values.
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * Set the mapping of report parameter names to values.
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * Return the output format for this report.
     */
    public ReportType getReportType() {
        return reportType;
    }

    /**
     * Set the output format for this report.
     */
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
}
