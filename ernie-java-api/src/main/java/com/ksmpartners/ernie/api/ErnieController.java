/*
	Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.ksmpartners.ernie.api;
import akka.pattern.AskTimeoutException;
import com.ksmpartners.ernie.api.ErnieBuilder.*;
import com.ksmpartners.ernie.api.ErnieControl;
import com.ksmpartners.ernie.api.ErnieEngine;
import com.ksmpartners.ernie.model.*;
import scala.None$;
import scala.Option;
import scala.Some;
import scala.Tuple2;
import scala.collection.Seq;
import scala.collection.immutable.Map$;
import scala.concurrent.duration.FiniteDuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API for interacting with Ernie. For example:
 *   ErnieController api = new ErnieController();
 *   api.configure(
 *      new ErnieConfig.Builder(
 *          new com.ksmpartners.ernie.api.MemoryReportManager()).withWorkers(10).build());
 *   try {
 *      api.start();
 */
public class ErnieController {
    private ErnieConfiguration ec = null;
    private ErnieEngine ee = null;
    private ErnieControl api = null;

    public ErnieController() {

    }

    /**
     * Configure this ErnieController with an ErnieConfiguration constructed using ErnieConfig.Builder.
     */
    public void configure(ErnieConfiguration ec) {
        this.ec = ec;
        ee = ErnieEngine.apply(ec);
    }

    /**
     * Initialize the API.
     * @throws ErnieNotConfiguredException if configure() has not yet been called.
     */
    public void start() throws ErnieNotConfiguredException {
        if (ec == null) throw new ErnieNotConfiguredException();
        else if (ec.reportManager() == null) throw new ErnieNotConfiguredException("Report manager null");
        else if (ee == null) throw new ErnieNotConfiguredException();
        else {
            api = ee.start();
        }
    }

    /**
     * Create a report definition.
     * @param rptDesign BIRT report design XML as byte array input stream
     * @param description a plain text description or identifier for the definition
     * @param createdUser the username of the definition creator
     * @throws InvalidDefinitionException if rptDesign is null or contains malformed XML
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return the resultant definition metadata.
     */
    public DefinitionEntity createDefinition(InputStream rptDesign, String description, String createdUser) throws InvalidDefinitionException, AskTimeoutException, ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return api.createDefinition(new Some<InputStream>(rptDesign), description, createdUser);
    }

    /**
     * Create a report definition.
     * @param description a plain text description or identifier for the definition
     * @param createdUser the username of the definition creator
     * @throws InvalidDefinitionException if rptDesign is null or contains malformed XML
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return the resultant definition metadata.
     */
    public DefinitionEntity createDefinition(String description, String createdUser) throws InvalidDefinitionException, AskTimeoutException, ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        final scala.Option<InputStream> none = scala.Option.apply(null);
        return api.createDefinition(none, description, createdUser);
    }

    /**
     * Return DefinitionEntities for all definitions.
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     */
    public List<DefinitionEntity> getDefinitionsCatalog() throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return scala.collection.JavaConversions.asJavaList(api.getDefinitionsCatalog());
    }

    /**
     *  Update a definition.
     *  @param defId existing definition to update
     * @param rptDesign BIRT report design XML as byte array input stream
     * @param defEnt definition metadata
     * @throws NotFoundException if defId is not found
     * @throws MissingArgumentException if neither report design nor DefinitionEntity is provided
     * @throws InvalidDefinitionException if rptDesign is null or contains malformed XML
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return updated definition metadata.
     */
    public DefinitionEntity updateDefinition(String defId, DefinitionEntity defEnt, InputStream rptDesign) throws ErnieEngineNotStartedException, MissingArgumentException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return api.updateDefinition(defId, new Some<DefinitionEntity>(defEnt), new Some<InputStream>(rptDesign));
    }

    /**
     *  Update a definition.
     *  @param defId existing definition to update
     * @param rptDesign BIRT report design XML as byte array input stream
     * @throws NotFoundException if defId is not found
     * @throws MissingArgumentException if neither report design nor DefinitionEntity is provided
     * @throws InvalidDefinitionException if rptDesign is null or contains malformed XML
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return updated definition metadata.
     */
    public DefinitionEntity updateDefinition(String defId, InputStream rptDesign) throws ErnieEngineNotStartedException, MissingArgumentException {
        if (api == null) throw new ErnieEngineNotStartedException();
        final scala.Option<DefinitionEntity> none = scala.Option.apply(null);
        return api.updateDefinition(defId, none, new Some<InputStream>(rptDesign));
    }

    /**
     *  Update a definition.
     *  @param defId existing definition to update
     * @param defEnt definition metadata
     * @throws NotFoundException if defId is not found
     * @throws MissingArgumentException if neither report design nor DefinitionEntity is provided
     * @throws InvalidDefinitionException if rptDesign is null or contains malformed XML
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @throws AskTimeoutException if request times out
     * @return updated definition metadata.
     */
    public DefinitionEntity updateDefinition(String defId, DefinitionEntity defEnt) throws ErnieEngineNotStartedException, MissingArgumentException {
        if (api == null) throw new ErnieEngineNotStartedException();
        Option<InputStream> none = scala.Option.apply(null);
        return api.updateDefinition(defId, new Some<DefinitionEntity>(defEnt),  none);
    }

    /**
     * Get definition metadata.
     * @param defId definition to interrogate
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return DefinitionEntity if defId is found; otherwise, [[scala.None]].
     */
    public DefinitionEntity getDefinitionEntity(String defId) throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return api.getDefinitionEntity(defId);
    }

    /**
     * Get definition design as input stream. Caller is responsible for closing resultant InputStream.
     * @param defId definition to interrogate
     * @throws NotFoundException if defId is not found or invalid
     * @throws MissingArgumentException if defId is null
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return InputStream.
     */
    public InputStream getDefinitionDesign(String defId) throws ErnieEngineNotStartedException, MissingArgumentException, NotFoundException {
        if (api == null) throw new ErnieEngineNotStartedException();
        if (defId == null) throw new MissingArgumentException("Null definition ID");
        Option<InputStream> o = api.defsResource().getDefinitionDesign(defId);
        if (o.isEmpty()) throw new NotFoundException(defId + " design not found");
        else return o.get();
    }

    /**
     * Delete a definition. Completely remove the report design and DefinitionEntity from the report manager and filesystem (if applicable).
     * @param defId definition to delete
     * @throws MissingArgumentException if defId is null
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return a DeleteStatus indicating the result of deletion.
     */
    public DeleteStatus deleteDefinition(String defId) throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return api.deleteDefinition(defId);
    }

    /**
     * Create and start a report generation job.
     * @param defId an existing report definition/design
     * @param rptType the report output format
     * @param retentionPeriod optional override for default number of days to retain report output
     * @param reportParameters a set of BIRT Report Parameters corresponding to the parameters specified in the report definition.
     * @param userName username of the user creating the job
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return the generated job ID and a [[com.ksmpartners.ernie.model.JobStatus]]
     */
    public JobEntity createJob(String defId, ReportType rptType, int retentionPeriod, Map<String,String> reportParameters, String userName) throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        scala.collection.immutable.Map m = Map$.MODULE$.empty();
        if (reportParameters != null) m.$plus$plus(scala.collection.JavaConversions.mapAsScalaMap(reportParameters));
        Tuple2<Object, JobStatus> result = api.createJob(defId, rptType, new Some<Object>(retentionPeriod), m , userName);
        JobEntity res = new JobEntity();
        res.setJobId((Long) result._1());
        res.setJobStatus(result._2());
        return res;
    }

    /**
     * Create and start a report generation job.
     * @param defId an existing report definition/design
     * @param rptType the report output format
     * @param reportParameters a set of BIRT Report Parameters corresponding to the parameters specified in the report definition.
     * @param userName username of the user creating the job
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return the generated job ID and a [[com.ksmpartners.ernie.model.JobStatus]]
     */
    public JobEntity createJob(String defId, ReportType rptType, Map<String,String> reportParameters, String userName) throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        scala.collection.immutable.Map m = Map$.MODULE$.empty();
        if (reportParameters != null) m.$plus$plus(scala.collection.JavaConversions.mapAsScalaMap(reportParameters));
        Tuple2<Object, JobStatus> result = api.createJob(defId, rptType, Option.apply(null), m , userName);
        JobEntity res = new JobEntity();
        res.setJobId((Long) result._1());
        res.setJobStatus(result._2());
        return res;
    }

    /**
     * Get the status of a given job ID
     * @param jobId to interrogate
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return [[com.ksmpartners.ernie.model.JobStatus]] of given jobId.
     */
    public JobStatus getJobStatus(Long jobId) throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return api.getJobStatus(jobId);
    }

    /**
     * Get a catalog of jobs.
     * @param catalog optionally specify a subset of jobs to retrieve
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return a list of [[com.ksmpartners.ernie.model.JobEntity]] constituting the catalog.
     */
    public List<JobEntity> getJobCatalog(JobCatalog catalog)  throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return scala.collection.JavaConversions.asJavaList(api.getJobCatalog(new Some<JobCatalog>(catalog)));
    }

    /**
     * Get a catalog of jobs.
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return a list of [[com.ksmpartners.ernie.model.JobEntity]] constituting the catalog.
     */
    public List<JobEntity> getJobCatalog()  throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        scala.Option<JobCatalog> none = scala.Option.apply(null);
        return scala.collection.JavaConversions.asJavaList(api.getJobCatalog(none));
    }

    /**
     * Get a list of all job IDs as strings.
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     */
    public List<String> getJobList()  throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return scala.collection.JavaConversions.asJavaList(api.getJobList());
    }

    /**
     * Return all existing definition IDs.
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     */
    public List<String> getDefinitionList()  throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return scala.collection.JavaConversions.asJavaList(api.getDefinitionList());
    }

    /**
     * Retrieve job metadata.
     * @param jobId the ID of the job to interrogate
     * @throws MissingArgumentException if jobId is null or invalid
     * @throws AskTimeoutException if request times out
     * @throws NotFoundException if jobId is not found
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return a JobEntity
     */
    public JobEntity getJobEntity(Long jobId) throws ErnieEngineNotStartedException, NotFoundException {
        if (api == null) throw new ErnieEngineNotStartedException();
        scala.Option<JobEntity> res = api.getJobEntity(jobId);
        if (res.isEmpty()) throw new NotFoundException(jobId + " not found");
        else return res.get();
    }

    /**
     * Retrieve report output metadata.
     * @param jobId the job whose report output metadata is to be interrogated
     * @throws AskTimeoutException if request times out
     * @throws NotFoundException if jobId is not found
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return a [[com.ksmpartners.ernie.model.ReportEntity]] if the job ID is found.
     */
    public ReportEntity getReportEntity(Long jobId) throws ErnieEngineNotStartedException, NotFoundException {
        if (api == null) throw new ErnieEngineNotStartedException();
        scala.Option<ReportEntity> res = api.getReportEntity(jobId);
        if (res.isEmpty()) throw new NotFoundException(jobId + " not found");
        else return res.get();
    }

    /**
     * Retrieve report output metadata.
     * @param rptId the report output whose metadata is to be interrogated
     * @throws AskTimeoutException if request times out
     * @throws NotFoundException if jobId is not found
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return a [[com.ksmpartners.ernie.model.ReportEntity]] if the report ID is found.
     */
    public ReportEntity getReportEntity(String rptId) throws ErnieEngineNotStartedException, NotFoundException {
        if (api == null) throw new ErnieEngineNotStartedException();
        scala.Option<ReportEntity> res = api.getReportEntity(rptId);
        if (res.isEmpty()) throw new NotFoundException(rptId + " not found");
        else return res.get();
    }

    /**
     * Retrieve job output.
     * @param jobId the jobId whose output is to be retrieved
     * @throws AskTimeoutException if request times out
     * @throws NotFoundException if jobId is not found
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return a [[java.io.InputStream]] if the report output is available; otherwise, [[scala.None]]
     */
    public InputStream getReportOutput(Long jobId) throws ErnieEngineNotStartedException, NotFoundException {
        if (api == null) throw new ErnieEngineNotStartedException();
        Option res = api.getReportOutput(jobId);
        if (res.isEmpty()) throw new NotFoundException(jobId + " not found");
        else return (InputStream) res.get();
    }

    /**
     * Delete a job's output and any associated metadata
     * @param jobId the job whose output and metadata is to be deleted
     * @throws AskTimeoutException if request times out
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return the status of the deletion
     */
    public DeleteStatus deleteReportOutput(Long jobId) throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return api.deleteReportOutput(jobId);
    }

    /**
     * Purge jobs in expired catalog.
     * @throws ErnieEngineNotStartedException if ErnieController.start() was not called
     * @return the status of the batch deletion and a list of purged report IDs.
     */
    public PurgeResult purgeExpiredReports() throws ErnieEngineNotStartedException  {
        if (api == null) throw new ErnieEngineNotStartedException();
        Tuple2<DeleteStatus, scala.collection.immutable.List<String>> result = api.purgeExpiredReports();
        return new PurgeResult(result._1(), scala.collection.JavaConversions.asJavaList(result._2()));
    }

    /**
     * Shut down the instance of [[com.ksmpartners.ernie.engine]] in use by this object
     */
    public void shutDown() {
        if (api != null) api.shutDown();
    }

    /**
     * Represents the result of a request to purge all expired jobs
     */
    public class PurgeResult {
        public final DeleteStatus deleteStatus;
        public final List<String> deletedReports;
        public PurgeResult(DeleteStatus deleteStatus, List<String> deletedReports) {
            this.deletedReports = deletedReports;
            this.deleteStatus = deleteStatus;
        }
    }

    /**
     * Get the timeout for requests to [[com.ksmpartners.ernie.engine.Coordinator]] as specified by the initial [[com.ksmpartners.ernie.api.ErnieConfiguration]]
     */
    public FiniteDuration timeoutDuration() throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        return api.timeoutDuration();
    }


    /**
     * Spawn an additional Akka Actor to generate BIRT reports
     */
    public void spawnWorker() throws ErnieEngineNotStartedException {
        if (api == null) throw new ErnieEngineNotStartedException();
        api.spawnWorker();
    }
}
