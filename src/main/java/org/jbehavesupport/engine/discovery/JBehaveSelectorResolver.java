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

package org.jbehavesupport.engine.discovery;

import lombok.SneakyThrows;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehavesupport.engine.JUnit5Stories;
import org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor;
import org.jbehavesupport.engine.descriptor.StoryParser;
import org.jbehavesupport.engine.descriptor.StoryResult;
import org.jbehavesupport.engine.reporter.ReportLevel;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor.SEGMENT_TYPE_CLASS;
import static org.jbehavesupport.engine.reporter.ReportLevel.REPORT_LEVEL_PROPERTY;
import static org.jbehavesupport.engine.reporter.ReportLevel.STEP;

public class JBehaveSelectorResolver implements SelectorResolver {

    private static final String STORY_PATHS = "storyPaths";

    private final UniqueId engineId;
    private final String reportLevel;

    public JBehaveSelectorResolver(EngineDiscoveryRequest discoveryRequest, UniqueId engineId) {
        this.engineId = engineId;
        this.reportLevel = discoveryRequest.getConfigurationParameters().get(REPORT_LEVEL_PROPERTY).orElse(STEP.name());
    }

    @Override
    public Resolution resolve(ClassSelector selector, Context context) {
        if (!JUnit5Stories.class.isAssignableFrom(selector.getJavaClass())) {
            return Resolution.unresolved();
        }
        return resolveTestClass((Class<? extends JUnit5Stories>)selector.getJavaClass(), context);
    }

    @SneakyThrows({IllegalAccessException.class, InstantiationException.class})
    private Resolution resolveTestClass(Class<? extends ConfigurableEmbedder> testClass, Context context) {
        List<String> storyPaths;
        Embedder configuredEmbedder;

        ConfigurableEmbedder configurableEmbedder = testClass.newInstance();
        configuredEmbedder = configurableEmbedder.configuredEmbedder();
        storyPaths = getStoryPaths(configurableEmbedder);

        UniqueId classDescriptorId = engineId.append(SEGMENT_TYPE_CLASS, testClass.getCanonicalName());
        JBehaveTestDescriptor classDescriptor = new JBehaveTestDescriptor(classDescriptorId, testClass.getSimpleName(), storyPaths, configuredEmbedder);

        List<JBehaveTestDescriptor> storiesDescriptors = getStoriesDescriptors(classDescriptorId, configuredEmbedder, storyPaths);
        storiesDescriptors.forEach(classDescriptor::addChild);

        context.addToParent(parent -> Optional.of(classDescriptor));
        return Resolution.match(Match.exact(classDescriptor));
    }

    private List<JBehaveTestDescriptor> getStoriesDescriptors(UniqueId parentId, Embedder configuredEmbedder, List<String> storyPaths) {
        StoryResult storyResult = StoryParser.parse(createPerformableTree(configuredEmbedder, storyPaths), ReportLevel.valueOf(reportLevel))
            .withCandidateSteps(getCandidateStepsWithNullStepMonitor(configuredEmbedder))
            .withKeywords(configuredEmbedder.configuration().keywords())
            .buildDescriptor(parentId);

        return storyResult.getStoryDescriptors();
    }

    private PerformableTree createPerformableTree(Embedder configuredEmbedder, List<String> storyPaths) {
        BatchFailures failures = new BatchFailures(configuredEmbedder.embedderControls().verboseFailures());
        PerformableTree performableTree = new PerformableTree();
        PerformableTree.RunContext context = performableTree.newRunContext(configuredEmbedder.configuration(),
            configuredEmbedder.stepsFactory().createCandidateSteps(),
            configuredEmbedder.embedderMonitor(),
            configuredEmbedder.metaFilter(), failures);

        List<Story> stories = new ArrayList<>();
        for (String storyPath : storyPaths) {
            stories.add(performableTree.storyOfPath(configuredEmbedder.configuration(), storyPath));
        }
        performableTree.addStories(context, stories);

        return performableTree;
    }

    private List<CandidateSteps> getCandidateStepsWithNullStepMonitor(Embedder embedder) {
        NullStepMonitor stepMonitor = new NullStepMonitor();
        List<CandidateSteps> candidateSteps = embedder.stepsFactory().createCandidateSteps();
        for (CandidateSteps candidateStep : candidateSteps) {
            candidateStep.configuration().useStepMonitor(stepMonitor);
        }
        return candidateSteps;
    }

    @SneakyThrows({NoSuchMethodException.class, InvocationTargetException.class, IllegalAccessException.class})
    @SuppressWarnings("unchecked")
    private List<String> getStoryPaths(ConfigurableEmbedder configurableEmbedder) {
        Method method = lookupStoryPathsMethod(configurableEmbedder.getClass());
        method.setAccessible(true);
        return ((List<String>) method.invoke(configurableEmbedder, (Object[]) null));
    }

    private Method lookupStoryPathsMethod(Class<? extends ConfigurableEmbedder> testClass) throws NoSuchMethodException {
        Method method;
        try {
            method = storyPathsLookup(testClass);
        } catch (NoSuchMethodException e) {
            method = testClass.getMethod(STORY_PATHS, (Class<?>[]) null);
        }
        return method;
    }

    private Method storyPathsLookup(Class<?> clazz) throws NoSuchMethodException {
        while (clazz != null) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                // Test any other things about it beyond the name...
                if (method.getName().equals(STORY_PATHS)) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchMethodException("Can not find method: " + STORY_PATHS);
    }

}
