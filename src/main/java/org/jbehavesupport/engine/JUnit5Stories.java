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

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.embedder.Embedder;

import java.util.List;

/**
 * <p>
 * JUnit 5-runnable entry-point to run multiple stories specified by {@link JUnit5Stories#storyPaths()}.
 * </p>
 */
@Test
public abstract class JUnit5Stories extends ConfigurableEmbedder {

    @Override
    public void run() {
        Embedder embedder = configuredEmbedder();
        try {
            embedder.runStoriesAsPaths(storyPaths());
        } finally {
            embedder.generateCrossReference();
            embedder.generateSurefireReport();
        }
    }

    protected abstract List<String> storyPaths();

}
