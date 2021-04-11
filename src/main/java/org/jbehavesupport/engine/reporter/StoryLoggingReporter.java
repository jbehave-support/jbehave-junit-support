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
package org.jbehavesupport.engine.reporter;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Story;
import org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

import static java.util.Objects.nonNull;

public class StoryLoggingReporter extends AbstractLoggingReporter {

    private final EngineExecutionListener engineExecutionListener;
    private final JBehaveTestDescriptor rootDescriptor;
    private final Configuration configuration;

    private TestDescriptor currentStoryDescriptor;

    public StoryLoggingReporter(EngineExecutionListener engineExecutionListener, JBehaveTestDescriptor rootDescriptor, Configuration configuration) {
        this.engineExecutionListener = engineExecutionListener;
        this.rootDescriptor = rootDescriptor;
        this.configuration = configuration;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            this.givenStories++;
        } else {
            for (TestDescriptor descriptor : rootDescriptor.getChildren()) {
                if (descriptor.isTest()
                    && isEligibleAs(descriptor, story.getName())) {
                    currentStoryDescriptor = descriptor;
                    engineExecutionListener.executionStarted(currentStoryDescriptor);
                }
            }
        }
        super.beforeStory(story, givenStory);
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        super.afterStory(givenOrRestartingStory);
        if (isAGivenStory()) {
            this.givenStories--;
        } else if (nonNull(currentStoryDescriptor)) {
            engineExecutionListener.executionFinished(currentStoryDescriptor, TestExecutionResult.successful());
            currentStoryDescriptor = null;
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        if (cause instanceof UUIDExceptionWrapper) {
            cause = cause.getCause();
        }
        super.failed(step, cause);
        engineExecutionListener.executionFinished(currentStoryDescriptor, TestExecutionResult.failed(cause));
    }

    @Override
    public void pending(String step) {
        super.pending(step);
        engineExecutionListener.executionFinished(currentStoryDescriptor, TestExecutionResult.failed(new PendingStepFound(step)));
    }
}
