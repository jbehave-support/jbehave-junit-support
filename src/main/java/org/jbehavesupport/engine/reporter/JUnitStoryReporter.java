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
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import static java.util.Objects.nonNull;

public class JUnitStoryReporter extends AbstractJUnitReporter {

    private final RunNotifier notifier;
    private final Description rootDescription;
    private final Configuration configuration;

    private Description currentStoryDescription;

    public JUnitStoryReporter(RunNotifier notifier, Description rootDescription, Configuration configuration) {
        this.notifier = notifier;
        this.rootDescription = rootDescription;
        this.configuration = configuration;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            this.givenStories++;
        } else {
            for (Description description : rootDescription.getChildren()) {
                if (description.isTest()
                    && isEligibleAs(description, story.getName())) {
                    currentStoryDescription = description;
                    notifier.fireTestStarted(currentStoryDescription);
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
        } else if (nonNull(currentStoryDescription)) {
            notifier.fireTestFinished(currentStoryDescription);
            currentStoryDescription = null;
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        if (cause instanceof UUIDExceptionWrapper) {
            cause = cause.getCause();
        }
        super.failed(step, cause);
        notifier.fireTestFailure(new Failure(currentStoryDescription, cause));
    }

    @Override
    public void pending(String step) {
        super.pending(step);
        notifier.fireTestFailure(new Failure(currentStoryDescription, new PendingStepFound(step)));
    }
}
