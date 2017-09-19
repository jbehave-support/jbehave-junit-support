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

package org.jbehavesupport.runner.description

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Michal Bocek
 * @since 19/09/2017
 */
class DescriptionGeneratorTest extends Specification {
    @Shared
    def generator = new DescriptionGenerator()

    @Unroll
    def "test getUnique for #description with expected length #length"() {
        when:
        def uniqueString = generator.getUnique(description)

        then:
        length == uniqueString.length()

        where:
        description||length
        "test"||4
        "test1"|| 5
        "test"||5
        "test"||6

    }
}