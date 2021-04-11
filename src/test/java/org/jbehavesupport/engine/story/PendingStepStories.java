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
package org.jbehavesupport.engine.story;

import org.jbehavesupport.runner.story.steps.TestSteps;

import java.util.Collections;
import java.util.List;

public class PendingStepStories extends AbstractStories {

    @Override
    protected List<String> storyPaths() {
        return Collections.singletonList(
            "org/jbehavesupport/runner/story/PendingStep.story"
        );
    }

    @Override
    protected List<?> getStepClasses() {
        return Collections.singletonList(new TestSteps());
    }
}
