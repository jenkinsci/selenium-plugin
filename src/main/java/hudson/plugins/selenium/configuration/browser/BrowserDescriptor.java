package hudson.plugins.selenium.configuration.browser;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

import org.kohsuke.stapler.QueryParameter;

public abstract class BrowserDescriptor<T extends AbstractSeleniumBrowser<T> & Describable<T>> extends Descriptor<T> {

    // define additional constructor parameters if you want
    protected BrowserDescriptor(Class<T> clazz) {
        super(clazz);
    }

    protected BrowserDescriptor() {
    }

    public FormValidation doCheckMaxInstances(@QueryParameter String value) {
        return FormValidation.validatePositiveInteger(value);
    }

}
