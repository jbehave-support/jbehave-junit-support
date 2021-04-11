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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.nonNull;

public class StepLoggingReporter extends AbstractLoggingReporter {

    private final EngineExecutionListener engineExecutionListener;
    private final JBehaveTestDescriptor rootDescriptor;
    private final Configuration configuration;

    private TestDescriptor currentStoryDescriptor;
    private Iterator<? extends TestDescriptor> scenariosDescriptors;
    private TestDescriptor currentScenarioDescriptor;
    private Iterator<TestDescriptor> examplesDescriptors;
    private TestDescriptor currentExampleDescriptor;
    private Iterator<TestDescriptor> stepsDescriptors;
    private Deque<TestDescriptor> currentStepDescriptor = new ArrayDeque<>();

    public StepLoggingReporter(EngineExecutionListener engineExecutionListener, JBehaveTestDescriptor rootDescriptor,
                               Configuration configuration) {
        this.engineExecutionListener = engineExecutionListener;
        this.rootDescriptor = rootDescriptor;
        this.configuration = configuration;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            if (notAGivenStory()) {
                currentStepDescriptor.push(hasNextStepsDescriptions() ? stepsDescriptors.next() : scenariosDescriptors.next());
                engineExecutionListener.executionStarted(currentStepDescriptor.peek());
            }
            this.givenStories++;
        } else {
            beforeStory(story);
        }
        super.beforeStory(story, givenStory);
    }

    private boolean hasNextStepsDescriptions() {
        return nonNull(stepsDescriptors) && stepsDescriptors.hasNext();
    }

    private void beforeStory(Story story) {
        for (TestDescriptor descriptor : rootDescriptor.getChildren()) {
            if (descriptor.isTest()
                && (isEligibleAs(story, descriptor, BEFORE_STORIES)
                || isEligibleAs(story, descriptor, AFTER_STORIES))) {
                currentStoryDescriptor = descriptor;
                engineExecutionListener.executionStarted(currentStoryDescriptor);

            }
            if (descriptor.isContainer()
                && isEligibleAs(descriptor, story.getName())) {
                currentStoryDescriptor = descriptor;
                engineExecutionListener.executionStarted(currentStoryDescriptor);
                scenariosDescriptors = currentStoryDescriptor.getChildren().iterator();
            }
        }
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        super.afterStory(givenOrRestartingStory);
        if (this.givenStories == 1) {
            engineExecutionListener.executionFinished(currentStepDescriptor.pop(), TestExecutionResult.successful());
            this.givenStories--;
        } else if (isAGivenStory()) {
            this.givenStories--;
        } else if (nonNull(currentStoryDescriptor)) {
            engineExecutionListener.executionFinished(currentStoryDescriptor, TestExecutionResult.successful());
        }

    }

    @Override
    public void beforeScenario(String scenarioTitle) {
        if (notAGivenStory()) {
            currentScenarioDescriptor = scenariosDescriptors.next();
            stepsDescriptors = getAllChildren(currentScenarioDescriptor.getChildren(), new ArrayList<>()).iterator();
            examplesDescriptors = getAllExamples(currentScenarioDescriptor.getChildren()).iterator();
            engineExecutionListener.executionStarted(currentScenarioDescriptor);
            super.beforeScenario(scenarioTitle);
        }
    }

    private List<TestDescriptor> getAllExamples(Set<? extends TestDescriptor> children) {
        List<TestDescriptor> result = new ArrayList<>();
        for (TestDescriptor child : children) {
            if (isExample(child)) {
                result.add(child);
            }
        }
        return result;
    }

    private boolean isExample(TestDescriptor descriptor) {
       return  descriptor.getDisplayName().startsWith(configuration.keywords().examplesTableRow() + " ");
    }

    private List<TestDescriptor> getAllChildren(Set<? extends TestDescriptor> children, List<TestDescriptor> result) {
        for (TestDescriptor descriptor : children) {
            if (descriptor.isContainer() && !isExample(descriptor)) {
                result.add(descriptor);
                getAllChildren(descriptor.getChildren(), result);
            } else if (descriptor.isContainer()) {
                getAllChildren(descriptor.getChildren(), result);
            } else {
                result.add(descriptor);
            }
        }
        return result;
    }

    @Override
    public void afterScenario() {
        super.afterScenario();
        if (notAGivenStory()) {
            engineExecutionListener.executionFinished(currentScenarioDescriptor, TestExecutionResult.successful());
        }
    }

    @Override
    public void beforeStep(String step) {
        if (notAGivenStory()) {
            currentStepDescriptor.push(stepsDescriptors.next());
            engineExecutionListener.executionStarted(currentStepDescriptor.peek());
        }
        super.beforeStep(step);
    }

    @Override
    public void successful(String step) {
        super.successful(step);
        if (notAGivenStory()) {
            engineExecutionListener.executionFinished(currentStepDescriptor.pop(), TestExecutionResult.successful());
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        if (cause instanceof UUIDExceptionWrapper) {
            cause = cause.getCause();
        }
        super.failed(step, cause);
        engineExecutionListener.executionFinished(currentStepDescriptor.peek(), TestExecutionResult.failed(cause));
        if (notAGivenStory()) {
            engineExecutionListener.executionFinished(currentStepDescriptor.peek(), TestExecutionResult.successful());
        }
    }

    @Override
    public void notPerformed(String step) {
        super.notPerformed(step);
        if (notAGivenStory()) {
            currentStepDescriptor.push(stepsDescriptors.next());
            engineExecutionListener.executionSkipped(currentStepDescriptor.peek(), "Not performed");
        }
    }

    @Override
    public void pending(String step) {
        super.pending(step);
        if (notAGivenStory()) {
            currentStepDescriptor.push(stepsDescriptors.next());
            engineExecutionListener.executionFinished(currentStepDescriptor.peek(), TestExecutionResult.failed(new PendingStepFound(step)));
        }
    }

    @Override
    public void example(Map<String, String> tableRow) {
        if (notAGivenStory()) {
            if (nonNull(currentExampleDescriptor)) {
                engineExecutionListener.executionFinished(currentExampleDescriptor, TestExecutionResult.successful());
            }
            currentExampleDescriptor = examplesDescriptors.next();
            engineExecutionListener.executionStarted(currentExampleDescriptor);
        }
        super.example(tableRow);
    }

    @Override
    public void afterExamples() {
        if (notAGivenStory()) {
            engineExecutionListener.executionFinished(currentExampleDescriptor, TestExecutionResult.successful());
        }
        super.afterExamples();
    }

    @Override
    public void ignorable(String step) {
        super.ignorable(step);
        if (notAGivenStory()) {
            currentStepDescriptor.push(stepsDescriptors.next());
            engineExecutionListener.executionSkipped(currentStepDescriptor.peek(), "Ignored");
        }
    }

}
