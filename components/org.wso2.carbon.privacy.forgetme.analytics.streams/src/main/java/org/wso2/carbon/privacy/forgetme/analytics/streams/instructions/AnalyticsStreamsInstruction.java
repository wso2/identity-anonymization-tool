/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.privacy.forgetme.analytics.streams.instructions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.privacy.forgetme.analytics.streams.beans.Streams;
import org.wso2.carbon.privacy.forgetme.analytics.streams.exceptions.AnalyticsStreamsProcessorException;
import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.runtime.*;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implements instructions for clearing analytics streams.
 */
public class AnalyticsStreamsInstruction implements ForgetMeInstruction {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsStreamsInstruction.class);

    private static final String CARBON_HOME = "CARBON_HOME";
    private static final String CUSTOM_CONF_DIR_NAME = "wso2_custom_conf_dir";
    private static final String CUSTOM_CONF_DIR_PATH_SEGMENT = "/repository/conf";
    private static final int RECORD_BATCH_SIZE = -1;

    private List<Record> anonymizedRecords;
    private List<Streams.Stream> streams;
    private Environment environment;

    public AnalyticsStreamsInstruction(Environment environment, List<Streams.Stream> streams) {
        this.environment = environment;
        this.streams = streams;
        this.anonymizedRecords = new ArrayList<>();
    }

    /**
     * Get table name from the stream name.
     *
     * @param streamName Stream name
     * @return Table name
     */
    private static String getTableName(String streamName) {
        return streamName.replace(".", "_").toUpperCase();
    }

    /**
     * Hash value of a record.
     *
     * @param value Raw value
     * @return Hashed value
     */
    private static Object hashRecordValue(Object value) {
        return Objects.hash(value);
    }

    @Override
    public ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig,
                                  Environment environment, ReportAppender reportAppender)
            throws InstructionExecutionException {
        AnalyticsDataService analyticsDataService = getAnalyticsDataService();
        for (Streams.Stream stream : this.streams) {
            filterRecords(analyticsDataService, userIdentifier, stream);
        }

        try {
            updateQueuedRecords(analyticsDataService);
        } catch (AnalyticsException e) {
            throw new AnalyticsStreamsProcessorException("Error in updating data records.", e);
        }
        return new ForgetMeResult();
    }

    /**
     * Get analytics data access layer.
     *
     * @return
     */
    private AnalyticsDataService getAnalyticsDataService() {
        // This requires "wso2_custom_conf_dir" property to point to the target DAS pack's conf directory.
        String carbonHome = environment.getProperty(CARBON_HOME);
        System.setProperty(CUSTOM_CONF_DIR_NAME, Paths.get(carbonHome, CUSTOM_CONF_DIR_PATH_SEGMENT).toString());

        AnalyticsServiceHolder.setAnalyticsClusterManager(new AnalyticsClusterManagerImpl());
        return AnalyticsServiceHolder.getAnalyticsDataService();
    }

    /**
     * Filter records which are to be anonymized.
     *
     * @param analyticsDataService Analytics data service reference
     * @param userIdentifier       User identifier
     * @param stream               Stream
     */
    private void filterRecords(AnalyticsDataService analyticsDataService, UserIdentifier userIdentifier,
                               Streams.Stream stream) {
        try {
            AnalyticsDataResponse analyticsDataResponse = analyticsDataService.get(userIdentifier.getTenantId(),
                    getTableName(stream.getStreamName()), 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0,
                    RECORD_BATCH_SIZE);
            List<AnalyticsDataResponse.Entry> entries = analyticsDataResponse.getEntries();

            for (AnalyticsDataResponse.Entry entry : entries) {
                AnalyticsIterator<Record> recordAnalyticsIterator = analyticsDataService.readRecords(
                        entry.getRecordStoreName(), entry.getRecordGroup());

                while (recordAnalyticsIterator.hasNext()) {
                    Record record = recordAnalyticsIterator.next();
                    if (record.getValue(stream.getId()).equals(userIdentifier.getUsername())) {
                        this.anonymizedRecords.add(anonymizeRecord(record, stream, userIdentifier.getPseudonym()));
                    }
                }
            }
        } catch (AnalyticsException e) {
            log.error("Error occurred while filtering record set.", e);
        }
    }

    /**
     * Update queued records via the DAL.
     *
     * @param analyticsDataService Analytics data service reference
     * @throws AnalyticsException
     */
    private void updateQueuedRecords(AnalyticsDataService analyticsDataService) throws AnalyticsException {
        analyticsDataService.put(this.anonymizedRecords);
    }

    /**
     * Anonymize record using pseudonym and hashed values
     *
     * @param record    Analytics record
     * @param stream    Stream
     * @param pseudonym Pseudonym to replace username
     * @return
     */
    private Record anonymizeRecord(Record record, Streams.Stream stream, String pseudonym) {
        for (String attribute : stream.getAttributes()) {
            if (stream.isIdAttribute(attribute)) {
                record.getValues().put(attribute, pseudonym);
            } else {
                Object value = record.getValue(attribute);
                if (value != null) {
                    record.getValues().put(attribute, hashRecordValue(value));
                }
            }
        }
        return record;
    }
}
