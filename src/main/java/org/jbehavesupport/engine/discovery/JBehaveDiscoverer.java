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

import org.jbehavesupport.engine.JUnit5Stories;
import org.jbehavesupport.engine.descriptor.JBehaveEngineDescriptor;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

public class JBehaveDiscoverer {

	public EngineDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		EngineDescriptor engineDescriptor = new JBehaveEngineDescriptor(uniqueId);
        getResolver(discoveryRequest, engineDescriptor.getUniqueId()).resolve(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	private EngineDiscoveryRequestResolver<TestDescriptor> getResolver(EngineDiscoveryRequest discoveryRequest, UniqueId engineId) {
        return EngineDiscoveryRequestResolver.builder()
            .addClassContainerSelectorResolver(JUnit5Stories.class::isAssignableFrom)
            .addSelectorResolver(new JBehaveSelectorResolver(discoveryRequest, engineId))
            .build();
    }

}
