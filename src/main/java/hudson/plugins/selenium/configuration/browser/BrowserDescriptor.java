package hudson.plugins.selenium.configuration.browser;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.QueryParameter;

public abstract class BrowserDescriptor<T extends AbstractSeleniumBrowser<T>&Describable<T>> extends
		Descriptor<T> {

	// define additional constructor parameters if you want
	protected BrowserDescriptor(
			Class<? extends T> clazz) {
		super(clazz);
	}

	protected BrowserDescriptor() {
	}
	
    public FormValidation doCheckMaxInstances(@QueryParameter String value) throws IOException, ServletException {
    	return FormValidation.validatePositiveInteger(value);
    }
	
    
}
