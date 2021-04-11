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

import org.jbehavesupport.engine.discovery.JBehaveDiscoverer
import org.jbehavesupport.engine.story.MultipleStories
import org.junit.platform.engine.UniqueId
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.runner.notification.RunNotifier
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import static org.junit.platform.testkit.engine.EventConditions.container
import static org.junit.platform.testkit.engine.EventConditions.engine
import static org.junit.platform.testkit.engine.EventConditions.event
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully
import static org.junit.platform.testkit.engine.EventConditions.started

import static org.junit.platform.testkit.engine.EventConditions.test

class MultipleStoryTest extends Specification {

    def notifier = Mock(RunNotifier)

    def "Test correct notifications"() {
        given:
        EngineTestKit.Builder builder = EngineTestKit.engine("jbehave")
            .enableImplicitConfigurationParameters(true)
            .selectors(selectClass(MultipleStories))

        when:
        def executionResults = builder.execute()

        then:
        executionResults.allEvents()
            .assertEventsMatchExactly(
                event(engine(), started()),
                event(container(MultipleStories), started()),
                event(test("BeforeStories"), started()),
                event(test("BeforeStories"), finishedSuccessfully()),

                event(container("Story: Scenario01-1"), started()),
                event(container("Scenario: login to system 1"), started()),
                event(test("Given login with data"), started()),
                event(test("Given login with data"), finishedSuccessfully()),
                event(test("When I submit login data on http://test01"), started()),
                event(test("When I submit login data on http://test01"), finishedSuccessfully()),
                event(test("Then user should be logged in successful"), started()),
                event(test("Then user should be logged in successful"), finishedSuccessfully()),
                event(container("Scenario: login to system 1"), finishedSuccessfully()),
                event(container("Story: Scenario01-1"), finishedSuccessfully()),

                event(container("Story: Scenario01"), started()),
                event(container("Scenario: login to system 2"), started()),
                event(test("Given login with data"), started()),
                event(test("Given login with data"), finishedSuccessfully()),
                event(test("When I submit login data on http://test02"), started()),
                event(test("When I submit login data on http://test02"), finishedSuccessfully()),
                event(test("Then user should be logged in successful"), started()),
                event(test("Then user should be logged in successful"), finishedSuccessfully()),
                event(container("Scenario: login to system 2"), finishedSuccessfully()),
                event(container("Story: Scenario01"), finishedSuccessfully()),

                event(container("Story: Scenario03"), started()),
                event(container("Scenario: login to system 3"), started()),
                event(test("Given login with data"), started()),
                event(test("Given login with data"), finishedSuccessfully()),
                event(test("When I submit login data on http://test03"), started()),
                event(test("When I submit login data on http://test03"), finishedSuccessfully()),
                event(test("Then user should be logged in successful"), started()),
                event(test("Then user should be logged in successful"), finishedSuccessfully()),
                event(container("Scenario: login to system 3"), finishedSuccessfully()),
                event(container("Story: Scenario03"), finishedSuccessfully()),

                event(test("AfterStories"), started()),
                event(test("AfterStories"), finishedSuccessfully()),
                event(container(MultipleStories), finishedSuccessfully()),
                event(engine(), finishedSuccessfully())
            )
    }

    def "Test descriptions"() {
        given:
        def request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(MultipleStories))
            .build()

        def discoverer = new JBehaveDiscoverer()

        when:
        def engineDescriptor = discoverer.discover(request, UniqueId.forEngine("jbehave"))
        def desc = engineDescriptor.children[0]
        def children = desc.children

        then:
        desc.displayName == "MultipleStories"
        children.size() == 5
        // children are in the order they are defined in storyPaths, the actual run order depends on StoryExecutionComparator
        children[0].displayName =~ /BeforeStories.*/
        children[1].displayName == "Story: Scenario01"
        children[1].children[0].displayName == "Scenario: login to system 2"
        children[1].children[0].children[0].displayName =~ /.*Given login with data/
        children[1].children[0].children[1].displayName =~ /.*When I submit login data on http:\/\/test02/
        children[1].children[0].children[2].displayName =~ /.*Then user should be logged in successful/
        children[2].displayName == "Story: Scenario01-1"
        children[2].children[0].displayName == "Scenario: login to system 1"
        children[2].children[0].children[0].displayName =~ /.*Given login with data/
        children[2].children[0].children[1].displayName =~ /.*When I submit login data on http:\/\/test01/
        children[2].children[0].children[2].displayName =~ /.*Then user should be logged in successful/
        children[3].displayName == "Story: Scenario03"
        children[3].children[0].displayName == "Scenario: login to system 3"
        children[3].children[0].children[0].displayName =~ /.*Given login with data/
        children[3].children[0].children[1].displayName =~ /.*When I submit login data on http:\/\/test03/
        children[3].children[0].children[2].displayName =~ /.*Then user should be logged in successful/
        children[4].displayName =~ /AfterStories.*/
    }

    @RestoreSystemProperties
    def "Test correct notifications for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        EngineTestKit.Builder builder = EngineTestKit.engine("jbehave")
            .enableImplicitConfigurationParameters(true)
            .selectors(selectClass(MultipleStories))

        when:
        def executionResults = builder.execute()

        then:
        executionResults.allEvents()
            .assertEventsMatchExactly(
                event(engine(), started()),
                event(container(MultipleStories), started()),
                event(test("Scenario01-1"), started()),
                event(test("Scenario01-1"), finishedSuccessfully()),
                event(test("Scenario01"), started()),
                event(test("Scenario01"), finishedSuccessfully()),
                event(test("Scenario03"), started()),
                event(test("Scenario03"), finishedSuccessfully()),
                event(container(MultipleStories), finishedSuccessfully()),
                event(engine(), finishedSuccessfully())
            )
    }

    @RestoreSystemProperties
    def "Test descriptions for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        def request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(MultipleStories))
            .build()

        def discoverer = new JBehaveDiscoverer()

        when:
        def engineDescriptor = discoverer.discover(request, UniqueId.forEngine("jbehave"))
        def desc = engineDescriptor.children[0]
        def children = desc.children

        then:
        desc.displayName == "MultipleStories"
        children.size() == 3
        children[0].displayName =~ "Story: Scenario01"
        children[0].children.size() == 0
        children[1].displayName =~ "Story: Scenario01-1"
        children[1].children.size() == 0
        children[2].displayName =~ "Story: Scenario03"
        children[2].children.size() == 0
    }

}
