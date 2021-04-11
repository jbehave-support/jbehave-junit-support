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

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;

public class EmbedderConfiguration {

    private EmbedderConfiguration() {
        throw new UnsupportedOperationException();
    }

    public static EmbedderControls recommendedConfiguration(Embedder embedder) {
        return embedder.embedderControls()
            // don't throw an exception on generating reports for failing stories
            .doIgnoreFailureInView(true)
            // don't throw an exception when a story failed
            .doIgnoreFailureInStories(true)
            // show verbose failures
            .doVerboseFailures(true);
    }

}
