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

package org.jbehavesupport.engine

import org.jbehave.core.configuration.Keywords
import spock.lang.Specification
import spock.lang.Unroll

import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildExampleText
import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildScenarioText
import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildStoryText
import static org.jbehavesupport.runner.JUnitRunnerFormatter.normalizeStep
import static org.jbehavesupport.runner.JUnitRunnerFormatter.normalizeStoryName
import static org.jbehavesupport.runner.JUnitRunnerFormatter.removeClass

class FormatterTest extends Specification {
    @Unroll
    def "test build story text for #storyName"() {
        when:
        def storyNameString = buildStoryText(storyName)

        then:
        storyNameString == expected

        where:
        storyName   || expected
        "Name"      || "Story: Name"
        "Name.1"    || "Story: Name"
        "Name\r\n1" || "Story: Name, 1"
    }

    @Unroll
    def "test build scenario text for #scenarioName"() {
        when:
        def scenarioNameString = buildScenarioText(new Keywords(), scenarioName)

        then:
        scenarioNameString == expected

        where:
        scenarioName || expected
        "Name"       || "Scenario: Name"
        "Name.1"     || "Scenario: Name1"
        "Name\r\n1"  || "Scenario: Name, 1"
        "Name(1)"    || "Scenario: Name|1|"
    }

    @Unroll
    def "test build example text for #example"() {
        when:
        def exampleString = buildExampleText(new Keywords(), example)

        then:
        exampleString == expected

        where:
        example     || expected
        "Name"      || "Example: Name"
        "Name.1"    || "Example: Name1"
        "Name\r\n1" || "Example: Name, 1"
        "Name(1)"   || "Example: Name|1|"
    }

    @Unroll
    def "test normalize story name for #storyName"() {
        when:
        def storyNameString = normalizeStoryName(storyName)

        then:
        storyNameString == expected

        where:
        storyName   || expected
        "Name"      || "Name"
        "Name.1"    || "Name"
        "Name\r\n1" || "Name, 1"
        "Name(1)"   || "Name|1|"
    }

    @Unroll
    def "test normalize step text for #step"() {
        when:
        def stepString = normalizeStep(step)

        then:
        stepString == expected

        where:
        step     || expected
        "Name"      || "Name"
        "Name.1"    || "Name.1"
        "Name\r\n1" || "Name"
        "Name(1)"   || "Name|1|"
    }

    @Unroll
    def "test remove class text for #string"() {
        when:
        def cleanString = removeClass(string)

        then:
        cleanString == expected

        where:
        string               || expected
        "Test"               || "Test"
        "Test(xx.xxx.xxxxx)" || "Test"
        "Test(xxx"           || "Test(xxx"
    }
}
