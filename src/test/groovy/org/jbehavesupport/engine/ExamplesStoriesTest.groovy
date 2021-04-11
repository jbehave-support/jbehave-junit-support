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
import org.jbehavesupport.engine.story.ExamplesStories
import org.junit.platform.engine.UniqueId
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.testkit.engine.EngineTestKit
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import static org.junit.platform.testkit.engine.EventConditions.container
import static org.junit.platform.testkit.engine.EventConditions.engine
import static org.junit.platform.testkit.engine.EventConditions.event
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully
import static org.junit.platform.testkit.engine.EventConditions.started
import static org.junit.platform.testkit.engine.EventConditions.test

class ExamplesStoriesTest extends Specification {

    def "Test correct notifications"() {
        given:
        EngineTestKit.Builder builder = EngineTestKit.engine("jbehave")
            .enableImplicitConfigurationParameters(true)
            .selectors(selectClass(ExamplesStories))

        when:
        def executionResults = builder.execute()

        then:
        executionResults.allEvents()
            .assertEventsMatchExactly(
                event(engine(), started()),
                event(container(ExamplesStories), started()),
                event(test("BeforeStories"), started()),
                event(test("BeforeStories"), finishedSuccessfully()),
                event(container("Story: Examples"), started()),
                event(container("Scenario: login to system"), started()),
                event(container("Example: {url=http://examplescom/login, status=OK}"), started()),
                event(test("Given login with data"), started()),
                event(test("Given login with data"), finishedSuccessfully()),
                event(test("When I submit login data"), started()),
                event(test("When I submit login data"), finishedSuccessfully()),
                event(test("Then user should be logged in"), started()),
                event(test("Then user should be logged in"), finishedSuccessfully()),
                event(container("Example: {url=http://examplescom/login, status=OK}"), finishedSuccessfully()),
                event(container("Example: {url=http://examplescom/logout, status=NOK}"), started()),
                event(test("Given login with data"), started()),
                event(test("Given login with data"), finishedSuccessfully()),
                event(test("When I submit login data"), started()),
                event(test("When I submit login data"), finishedSuccessfully()),
                event(test("Then user should be logged in"), started()),
                event(test("Then user should be logged in"), finishedSuccessfully()),
                event(container("Example: {url=http://examplescom/logout, status=NOK}"), finishedSuccessfully()),
                event(container("Scenario: login to system"), finishedSuccessfully()),
                event(container("Story: Examples"), finishedSuccessfully()),
                event(test("AfterStories"), started()),
                event(test("AfterStories"), finishedSuccessfully()),
                event(container(ExamplesStories), finishedSuccessfully()),
                event(engine(), finishedSuccessfully())
            )
    }

    def "Test descriptions"() {
        given:
        def request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(ExamplesStories))
            .build()

        def discoverer = new JBehaveDiscoverer()

        when:
        def engineDescriptor = discoverer.discover(request, UniqueId.forEngine("jbehave"))
        def desc = engineDescriptor.children[0]
        def children = desc.children

        then:
        desc.displayName == "ExamplesStories"
        children.size() == 3
        children[0].displayName =~ /BeforeStories.*/
        children[1].displayName == "Story: Examples"
        children[1].children[0].displayName == "Scenario: login to system"
        children[1].children[0].children[0].displayName =~ /Example.*/
        children[1].children[0].children[0].children[0].displayName =~ /Given login with data.*/
        children[1].children[0].children[0].children[1].displayName =~ /When I submit login data on.*/
        children[1].children[0].children[0].children[2].displayName =~ /Then user should be logged in.*/
        children[1].children[0].children[1].displayName =~ /Example.*/
        children[1].children[0].children[1].children[0].displayName =~ /Given login with data.*/
        children[1].children[0].children[1].children[1].displayName =~ /When I submit login data on.*/
        children[1].children[0].children[1].children[2].displayName =~ /Then user should be logged in.*/
        children[2].displayName =~ /AfterStories.*/
    }

    @RestoreSystemProperties
    def "Test correct notifications for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        EngineTestKit.Builder builder = EngineTestKit.engine("jbehave")
            .enableImplicitConfigurationParameters(true)
            .selectors(selectClass(ExamplesStories))

        when:
        def executionResults = builder.execute()

        then:
        executionResults.allEvents()
            .assertEventsMatchExactly(
                event(engine(), started()),
                event(container(ExamplesStories), started()),
                event(test("Examples"), started()),
                event(test("Examples"), finishedSuccessfully()),
                event(container(ExamplesStories), finishedSuccessfully()),
                event(engine(), finishedSuccessfully())
            )
    }

    @RestoreSystemProperties
    def "Test descriptions for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        def request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(ExamplesStories))
            .build()

        def discoverer = new JBehaveDiscoverer()

        when:
        def engineDescriptor = discoverer.discover(request, UniqueId.forEngine("jbehave"))
        def desc = engineDescriptor.children[0]
        def children = desc.children

        then:
        desc.displayName == "ExamplesStories"
        children.size() == 1
        children[0].displayName =~ /Story: Examples.*/
        children[0].children.size() == 0
    }

}
