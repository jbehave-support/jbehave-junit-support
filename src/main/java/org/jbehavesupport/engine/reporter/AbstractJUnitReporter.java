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

import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildStoryText;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.removeClass;

import org.jbehave.core.model.Story;
import org.junit.runner.Description;

public class AbstractJUnitReporter extends LoggingReporter {
    protected static final String BEFORE_STORIES = "BeforeStories";
    protected static final String AFTER_STORIES = "AfterStories";

    protected int givenStories = 0;

    protected boolean isEligibleAs(Story story, Description description, String storyName) {
        return story.getName().equals(storyName) && description.getDisplayName().startsWith(storyName);
    }

    protected boolean isEligibleAs(Description description, String storyName) {
        return removeClass(description.getDisplayName()).equals(buildStoryText(storyName));
    }

    protected boolean isAGivenStory() {
        return this.givenStories > 0;
    }

    protected boolean notAGivenStory() {
        return this.givenStories == 0;
    }
}
