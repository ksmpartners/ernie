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
import scala.Some;
import scala.concurrent.duration.FiniteDuration;

/**
 * Provides a DSL for building a new ErnieConfiguration. See ErnieController for an example.
 */
public class ErnieConfig {

    private final ReportManager reportManager;
    private final FiniteDuration timeout;
    private final int defaultRetentionDays;
    private final int maxRetentionDays;
    private final int workerCount;

    public static class Builder {

        private  ReportManager reportManager;
        private  FiniteDuration timeout;
        private  int defaultRetentionDays;
        private  int maxRetentionDays;
        private  int workerCount;

        public Builder(ReportManager reportManager) { this.reportManager = reportManager; }
        public Builder withDefaultRetentionDays(int defaultRetentionDays){this.defaultRetentionDays = defaultRetentionDays; return this; }
        public Builder withMaxRetentionDays(int maxRetentionDays){this.maxRetentionDays = maxRetentionDays; return this; }
        public Builder withWorkers(int workers){this.workerCount = workers; return this; }
        public Builder timeoutAfter(FiniteDuration timeout){this.timeout = timeout; return this; }

        public ErnieConfiguration build() {
            return new ErnieConfiguration(reportManager, new Some<FiniteDuration>(timeout), new Some<Object>(new Integer(defaultRetentionDays)), new Some<Object>(new Integer(maxRetentionDays)),
                    new Some<Object>(new Integer(workerCount)));
        }
    }

    private ErnieConfig(Builder builder) {
        this.reportManager = builder.reportManager;
        this.timeout = builder.timeout;
        this.defaultRetentionDays = builder.defaultRetentionDays;
        this.maxRetentionDays = builder.maxRetentionDays;
        this.workerCount = builder.workerCount;
    }
}

/**
 * Exception thrown when attempt is made to start Ernie or use the API when it has not yet been configured.
 */
class ErnieNotConfiguredException extends Exception {
    public ErnieNotConfiguredException(String message) {
        super(message);
    }
    public ErnieNotConfiguredException() {
        super();
    }
}

/**
 * Exception thrown when API call is made without having called ErnieController.start()
 */
class ErnieEngineNotStartedException extends Exception {
    public ErnieEngineNotStartedException(String message) {
        super(message);
    }
    public ErnieEngineNotStartedException() {
        super();
    }
}