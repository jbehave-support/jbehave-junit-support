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

package org.jbehavesupport.engine.descriptor;

import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepType;
import org.junit.platform.engine.UniqueId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildExampleText;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildScenarioText;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildStoryText;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.normalizeStep;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.normalizeStoryName;
import static org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor.SEGMENT_TYPE_SCENARIO;
import static org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor.SEGMENT_TYPE_STEP;
import static org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor.SEGMENT_TYPE_STORY;

class StepLevelDescriptorBuilder extends AbstractDescriptorBuilder {

    private UniqueDescriptionGenerator descriptions;
    private String previousNonAndStep;

    public StepLevelDescriptorBuilder(final PerformableTree story) {
        super(story);
        descriptions = new UniqueDescriptionGenerator();
    }

    @Override
    public StoryResult buildDescriptor(UniqueId parentId) {
        List<JBehaveTestDescriptor> descriptors = getStory().getRoot()
            .getStories()
            .stream()
            .map(story -> createStoryDescriptor(parentId, story))
            .collect(Collectors.toList());
        descriptors.add(0, createStorySegmentDescriptor(parentId, STORIES_BEFORE));
        descriptors.add(createStorySegmentDescriptor(parentId, STORIES_AFTER));
        return new StoryResult(descriptors);
    }

    private JBehaveTestDescriptor createStorySegmentDescriptor(UniqueId parentId, String name) {
        UniqueId uniqueId = parentId.append(SEGMENT_TYPE_STORY, name);
        return new JBehaveTestDescriptor(uniqueId, name);
    }

    private JBehaveTestDescriptor createScenarioSegmentDescriptor(UniqueId parentId, String name) {
        UniqueId uniqueId = parentId.append(SEGMENT_TYPE_SCENARIO, name);
        return new JBehaveTestDescriptor(uniqueId, name);
    }

    private JBehaveTestDescriptor createStepSegmentDescriptor(UniqueId parentId, String name) {
        UniqueId uniqueId = parentId.append(SEGMENT_TYPE_STEP, name);
        return new JBehaveTestDescriptor(uniqueId, name);
    }

    protected JBehaveTestDescriptor createStoryDescriptor(UniqueId parentId, PerformableTree.PerformableStory performableStory) {
        String storyString = buildStoryText(performableStory.getStory().getName());
        String uniqueStoryDescription = descriptions.getUnique(storyString);
        JBehaveTestDescriptor descriptor = createStorySegmentDescriptor(parentId, uniqueStoryDescription);
        if (hasGivenStories(performableStory)) {
            addGivenStories(descriptor, performableStory.getStory());
        }
        performableStory.getScenarios().forEach(
            performableScenario -> getScenarioDescriptor(descriptor.getUniqueId(), performableScenario).forEach(descriptor::addChild)
        );
        return descriptor;
    }

    private List<JBehaveTestDescriptor> getScenarioDescriptor(UniqueId parentId, PerformableTree.PerformableScenario performableScenario) {
        String scenarioText = buildScenarioText(getKeywords(), performableScenario.getScenario().getTitle());
        JBehaveTestDescriptor scenarioDescriptor = createScenarioSegmentDescriptor(parentId, scenarioText);
        if (performableScenario.hasExamples()) {
            performableScenario.getExamples()
                .stream()
                .map(examplePerformableScenario -> {
                    String exampleString = buildExampleText(getKeywords(), examplePerformableScenario.getParameters().toString());
                    JBehaveTestDescriptor exampleDescriptor = createScenarioSegmentDescriptor(scenarioDescriptor.getUniqueId(), descriptions.getUnique(exampleString));
                    performableScenario.getScenario()
                        .getSteps()
                        .forEach(step -> addIfNotAComment(exampleDescriptor, step));
                    return exampleDescriptor;
                })
                .forEach(scenarioDescriptor::addChild);
        } else {
            if (hasGivenStories(performableScenario)) {
                addGivenStories(scenarioDescriptor, performableScenario.getScenario());
            }
            performableScenario.getScenario()
                .getSteps()
                .forEach(step -> addIfNotAComment(scenarioDescriptor, step));
        }
        return Collections.singletonList(scenarioDescriptor);
    }

    private void addIfNotAComment(JBehaveTestDescriptor descriptor, String step) {
        if (isNotAComment(step)) {
            descriptor.addChild(getStepDescriptor(descriptor.getUniqueId(), step));
        }
    }

    private void addGivenStories(JBehaveTestDescriptor scenarioDescriptor, Scenario scenario) {
        scenario.getGivenStories()
            .getStories()
            .forEach(story -> {
                String storyString = normalizeStoryName(story.getPath());
                scenarioDescriptor.addChild(createStorySegmentDescriptor(scenarioDescriptor.getUniqueId(), descriptions.getUnique(storyString)));
            });
    }


    private void addGivenStories(JBehaveTestDescriptor storyDescriptor, Story story) {
        story.getGivenStories()
            .getStories()
            .forEach(givenStory -> {
                String storyString = normalizeStoryName(givenStory.getPath());
                storyDescriptor.addChild(createStorySegmentDescriptor(storyDescriptor.getUniqueId(), descriptions.getUnique(storyString)));
            });
    }

    private boolean hasGivenStories(PerformableTree.PerformableScenario performableScenario) {
        return !performableScenario.getScenario().getGivenStories().getPaths().isEmpty();
    }

    private boolean hasGivenStories(PerformableTree.PerformableStory performableStory) {
        return !performableStory.getStory().getGivenStories().getPaths().isEmpty();
    }

    private JBehaveTestDescriptor getStepDescriptor(UniqueId parentId, String step) {
        JBehaveTestDescriptor result;
        StepCandidate stepCandidate = findCandidateStep(step);
        if (stepCandidate != null) {
            result = getStepDescriptor(parentId, stepCandidate, step);
        } else {
            result = createStepSegmentDescriptor(parentId, step);
        }
        return result;
    }

    private JBehaveTestDescriptor getStepDescriptor(UniqueId parentId, StepCandidate stepCandidate, String step) {
        JBehaveTestDescriptor result;
        String uniqueStep = descriptions.getUnique(normalizeStep(step));
        if (stepCandidate.isComposite()) {
            result = createStepSegmentDescriptor(parentId, uniqueStep);
            Arrays.stream(stepCandidate.composedSteps())
                .forEach(childStep -> addIfNotAComment(result, childStep));
        } else {
            result = createStepSegmentDescriptor(parentId, uniqueStep);
        }
        return result;
    }

    private StepCandidate findCandidateStep(String step) {
        StepCandidate resultStepCandidate = getStepCandidates().stream()
            .filter(stepCandidate -> stepCandidate.matches(step, previousNonAndStep))
            .findFirst()
            .orElse(null);
        if (nonNull(resultStepCandidate) && resultStepCandidate.getStepType() != StepType.AND) {
            previousNonAndStep = resultStepCandidate.getStartingWord() + " ";
        }
        return resultStepCandidate;
    }

    private boolean isNotAComment(final String stringStepOneLine) {
        boolean result;
        if (getStepCandidates().isEmpty()) {
            result = true;
        } else {
            result = !getStepCandidates().get(0).comment(stringStepOneLine);
        }
        return result;
    }
}
