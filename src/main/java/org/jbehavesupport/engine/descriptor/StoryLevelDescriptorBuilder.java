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
import org.junit.platform.engine.UniqueId;

import java.util.List;
import java.util.stream.Collectors;

import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildStoryText;
import static org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor.SEGMENT_TYPE_STORY;

class StoryLevelDescriptorBuilder extends AbstractDescriptorBuilder {

    private UniqueDescriptionGenerator descriptions = new UniqueDescriptionGenerator();

    public StoryLevelDescriptorBuilder(final PerformableTree story) {
        super(story);
    }

    @Override
    public StoryResult buildDescriptor(UniqueId parentId) {
        List<JBehaveTestDescriptor> descriptors = getStory().getRoot()
            .getStories()
            .stream()
            .map(story -> createStoryDescriptor(parentId, story))
            .collect(Collectors.toList());
        return new StoryResult(descriptors);
    }

    @Override
    protected JBehaveTestDescriptor createStoryDescriptor(UniqueId parentId, PerformableTree.PerformableStory story) {
        String uniqueStoryDescription = descriptions.getUnique(story.getStory().getName());
        UniqueId uniqueId = parentId.append(SEGMENT_TYPE_STORY, uniqueStoryDescription);
        return new JBehaveTestDescriptor(uniqueId, buildStoryText(uniqueStoryDescription));
    }
}
