package org.jbehave.runner.story;

import org.jbehave.runner.story.steps.TestSteps;

import java.util.Collections;
import java.util.List;

/**
 * @author Michal Bocek
 * @since 23/03/2017
 */
public class GivenStories extends AbstractStories {

    @Override
    protected List<String> storyPaths() {
        return Collections.singletonList(
            "org/jbehave/runner/story/GivenStory.story"
        );
    }

    @Override
    protected List<?> getStepClasses() {
        return Collections.singletonList(new TestSteps());
    }
}
