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

/**
 * A JSONable class used to serialize job status information.
 */
public class StatusResponse extends ModelObject {

    private JobStatus jobStatus;

    public StatusResponse() {}

    public StatusResponse(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    /**
     * Return the status of report generation.
     */
    public JobStatus getJobStatus() {
        return jobStatus;
    }

    /**
     * Set the status of report generation.
     */
    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

}
