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
package org.jbehavesupport.engine;

import org.jbehave.core.Embeddable;
import org.jbehave.core.io.StoryPathResolver;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * <p>
 * JUnit 5-runnable entry-point to run a single story specified by a {@link Embeddable} class.
 * </p>
 */

@Test
public abstract class JUnit5Story extends JUnit5Stories {

    @Override
    public List<String> storyPaths() {
        StoryPathResolver pathResolver = configuredEmbedder().configuration().storyPathResolver();
        String storyPath = pathResolver.resolve(this.getClass());
        return singletonList(storyPath);
    }

}
