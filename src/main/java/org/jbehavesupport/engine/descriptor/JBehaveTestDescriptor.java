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

import lombok.Getter;
import org.jbehave.core.embedder.Embedder;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.List;

@Getter
public class JBehaveTestDescriptor extends AbstractTestDescriptor {

    private List<String> storyPaths;
    private Embedder configuredEmbedder;

    public static final String SEGMENT_TYPE_STEP = "step";
    public static final String SEGMENT_TYPE_SCENARIO = "scenario";
    public static final String SEGMENT_TYPE_STORY = "story";
    public static final String SEGMENT_TYPE_CLASS = "class";

    public JBehaveTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    public JBehaveTestDescriptor(UniqueId uniqueId, String displayName, List<String> storyPaths, Embedder configuredEmbedder) {
        this(uniqueId, displayName);
        this.storyPaths = storyPaths;
        this.configuredEmbedder = configuredEmbedder;
    }

    @Override
	public Type getType() {
		return getChildren().isEmpty() ? Type.TEST : Type.CONTAINER;
	}

	public boolean isRunnable() {
        return storyPaths != null && configuredEmbedder != null;
    }

}
