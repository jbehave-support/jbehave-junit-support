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
package org.jbehave.runner;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michal Bocek
 * @since 26/08/16
 */
public class JUnitRunner extends BlockJUnit4ClassRunner {

    private static final String AFTER_STORIES = "AfterStories";
    private static final String BEFORE_STORIES = "BeforeStories";

    private final List<String> storyPaths;
    private final List<CandidateSteps> candidateSteps;
    private final Description description;
    private final Embedder embedder;
    private int testCount;

    public JUnitRunner(Class<? extends ConfigurableEmbedder> testClass)
        throws InitializationError, IllegalAccessException, InstantiationException, InvocationTargetException,
        NoSuchMethodException {

        super(testClass);
        ConfigurableEmbedder configurableEmbedder = testClass.newInstance();
        embedder = configurableEmbedder.configuredEmbedder();
        storyPaths = getStoryPaths(configurableEmbedder);
        candidateSteps = getCandidateStepsWithNullStepMonitor(embedder);
        description = buildStoryDescription(testClass, embedder.configuration(), storyPaths, candidateSteps);
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public int testCount() {
        return testCount;
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                JUnitStepReporter junitReporter = new JUnitStepReporter(notifier, testCount, description,
                    embedder.configuration());

                embedder.configuration()
                    .storyReporterBuilder()
                    .withReporters(junitReporter);

                try {
                    embedder.runStoriesAsPaths(storyPaths);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                } finally {
                    embedder.generateCrossReference();
                }
            }
        };
    }

    private Description buildStoryDescription(Class<? extends ConfigurableEmbedder> testClass,
                                              Configuration configuration, List<String> storyPaths,
                                              List<CandidateSteps> candidateSteps) {
        Description description = Description.createSuiteDescription(testClass);
        List<Description> descriptions = new ArrayList<>();

        addSuite(descriptions, BEFORE_STORIES);
        addStories(descriptions, storyPaths, configuration);
        addSuite(descriptions, AFTER_STORIES);

        for (Description currentDescription : descriptions) {
            description.addChild(currentDescription);
        }
        return description;
    }

    private void addStories(List<Description> descriptions, List<String> storyPaths, Configuration configuration) {
        PerformableTree performableTree = new PerformableTree();
        for (String storyPath : storyPaths) {
            Story story = performableTree.storyOfPath(configuration, storyPath);
            StoryParser.StoryResult storyResult = StoryParser.parse(story)
                .withCandidateSteps(candidateSteps)
                .withKeywords(configuration.keywords())
                .buildDescription();
            descriptions.add(storyResult.getStoryDescription());
            testCount += storyResult.getTestCount();
        }
    }

    private void addSuite(List<Description> descriptions, String storyName) {
        descriptions.add(Description.createTestDescription(Story.class, storyName));
        testCount++;
    }

    private List<CandidateSteps> getCandidateStepsWithNullStepMonitor(Embedder embedder) {
//        NullStepMonitor stepMonitor = new NullStepMonitor();
        List<CandidateSteps> candidateSteps = embedder.stepsFactory().createCandidateSteps();
//        for (CandidateSteps candidateStep : candidateSteps) {
//            candidateStep.configuration().useStepMonitor(stepMonitor);
//        }
        return candidateSteps;
    }

    private List<String> getStoryPaths(ConfigurableEmbedder configurableEmbedder)
        throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        List<String> stories;
        if (configurableEmbedder instanceof JUnitStory) {
            Configuration configuration = configurableEmbedder.configuredEmbedder().configuration();
            String story = configuration.storyPathResolver().resolve(configurableEmbedder.getClass());
            stories = Arrays.asList(story);
        } else {
            Method method = lookupStoryPathsMethod(configurableEmbedder.getClass());
            method.setAccessible(true);
            stories = ((List<String>) method.invoke(configurableEmbedder, (Object[]) null));
        }

        return stories;
    }

    private Method lookupStoryPathsMethod(Class<? extends ConfigurableEmbedder> testClass)
        throws NoSuchMethodException {

        Method method;
        try {
            method = methodLookup(testClass, "storyPaths");
        } catch (NoSuchMethodException e) {
            method = testClass.getMethod("storyPaths", (Class[]) null);
        }
        return method;
    }

    private Method methodLookup(Class<?> clazz, String methodName) throws NoSuchMethodException {
        while (clazz != null) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                // Test any other things about it beyond the name...
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchMethodException("Can not find method: " + methodName);
    }
}
