/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.jbehavesupport.engine.executor;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor;
import org.jbehavesupport.engine.reporter.ReportLevel;
import org.jbehavesupport.engine.reporter.StepLoggingReporter;
import org.jbehavesupport.engine.reporter.StoryLoggingReporter;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;

import java.util.List;

import static org.jbehavesupport.engine.reporter.ReportLevel.REPORT_LEVEL_PROPERTY;
import static org.jbehavesupport.engine.reporter.ReportLevel.STEP;

public class JBehaveExecutor {

    public JBehaveExecutor(ExecutionRequest request) {
        this.engineExecutionListener = request.getEngineExecutionListener();
        this.reportLevel = request.getConfigurationParameters().get(REPORT_LEVEL_PROPERTY).orElse(STEP.name());
    }

    private final EngineExecutionListener engineExecutionListener;
    private final String reportLevel;

    public void execute(JBehaveTestDescriptor testDescriptor) {
        List<String> storyPaths = testDescriptor.getStoryPaths();
        Embedder configuredEmbedder = testDescriptor.getConfiguredEmbedder();

        StoryReporter junitReporter = resolveReporter(reportLevel, configuredEmbedder, testDescriptor);

        configuredEmbedder.configuration()
            .storyReporterBuilder()
            .withReporters(junitReporter);

        engineExecutionListener.executionStarted(testDescriptor);
        try {
            configuredEmbedder.runStoriesAsPaths(storyPaths);
            engineExecutionListener.executionFinished(testDescriptor, TestExecutionResult.successful());
        } catch (Throwable e) {
            engineExecutionListener.executionFinished(testDescriptor, TestExecutionResult.failed(e));
            throw new RuntimeException(e);
        } finally {
            configuredEmbedder.generateCrossReference();
            configuredEmbedder.generateSurefireReport();
        }
    }

    private StoryReporter resolveReporter(String reportLevel, Embedder configuredEmbedder, JBehaveTestDescriptor testDescriptor) {
        switch (ReportLevel.valueOf(reportLevel)) {
            case STEP:
                return new StepLoggingReporter(engineExecutionListener, testDescriptor, configuredEmbedder.configuration());
            case STORY:
                return new StoryLoggingReporter(engineExecutionListener, testDescriptor, configuredEmbedder.configuration());
            default:
                throw new IllegalStateException("Report level does not exists: " + reportLevel);
        }
    }

}
