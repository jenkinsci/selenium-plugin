package hudson.plugins.seleniumaes;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * @author onozaty
 */
public class PluginImpl extends Plugin {

    /**
     * @see hudson.Plugin#start()
     */
    @Override
    public void start() throws Exception {
        BuildStep.BUILDERS.add(SeleniumAutoExecBuilder.DESCRIPTOR);
    }
}
