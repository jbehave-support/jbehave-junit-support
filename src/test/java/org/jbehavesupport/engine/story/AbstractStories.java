package org.jbehavesupport.engine.story;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehavesupport.engine.EmbedderConfiguration;
import org.jbehavesupport.engine.JUnit5Stories;

import java.util.List;

public abstract class AbstractStories extends JUnit5Stories {

    public AbstractStories() {
        EmbedderConfiguration.recommendedConfiguration(configuredEmbedder());
    }

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration();
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), getStepClasses());
    }

    protected abstract List<?> getStepClasses();
}
