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
package org.jbehavesupport.runner

import org.jbehavesupport.runner.story.ExamplesStories
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

/**
 * @author Michal Bocek
 * @since 13/03/17
 */
class ExamplesStoriesTest extends Specification {

    def notifier = Mock(RunNotifier)

    def "Test correct notifications"() {
        given:
        def runner = new JUnitRunner(ExamplesStories)

        when:
        runner.run(notifier)

        then:
        1 * notifier.fireTestStarted({it.displayName.startsWith("BeforeStories")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.startsWith("BeforeStories")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.equals("Story: Examples")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.equals("Scenario: login to system")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Example: {url=http://examplescom/login, status=OK}")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("When I submit login data")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("When I submit login data")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Then user should be logged in")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Then user should be logged in")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Example: {url=http://examplescom/login, status=OK}")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Example: {url=http://examplescom/logout, status=NOK}")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("When I submit login data")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("When I submit login data")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Then user should be logged in")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Then user should be logged in")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Example: {url=http://examplescom/logout, status=NOK}")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.equals("Scenario: login to system")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.equals("Story: Examples")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.startsWith("AfterStories")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.startsWith("AfterStories")} as Description)
    }

    def "Test descriptions"() {
        given:
        def runner = new JUnitRunner(ExamplesStories)

        when:
        def desc = runner.description
        def children = desc.children

        then:
        desc.testClass == ExamplesStories
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
        def runner = new JUnitRunner(ExamplesStories)

        when:
        runner.run(notifier)

        then:
        1 * notifier.fireTestStarted({it.displayName.startsWith("Story: Examples")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.startsWith("Story: Examples")} as Description)
    }

    @RestoreSystemProperties
    def "Test descriptions for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        def runner = new JUnitRunner(ExamplesStories)

        when:
        def desc = runner.description
        def children = desc.children

        then:
        desc.testClass == ExamplesStories
        children.size() == 1
        children[0].displayName =~ /Story: Examples.*/
        children[0].children.size() == 0
    }
}
