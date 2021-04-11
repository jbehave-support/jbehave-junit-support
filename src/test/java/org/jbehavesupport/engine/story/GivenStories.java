package org.jbehavesupport.engine.story;

import org.jbehavesupport.runner.story.steps.TestSteps;

import java.util.Collections;
import java.util.List;

public class GivenStories extends AbstractStories {

    @Override
    protected List<String> storyPaths() {
        return Collections.singletonList(
            "org/jbehavesupport/runner/story/GivenStory.story"
        );
    }

    @Override
    protected List<?> getStepClasses() {
        return Collections.singletonList(new TestSteps());
    }
}
