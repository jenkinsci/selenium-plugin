package hudson.plugins.selenium.configuration.browser;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.QueryParameter;

import hudson.model.Descriptor;
import hudson.util.FormValidation;

public abstract class BrowserDescriptor extends
		Descriptor<Browser> {

	// define additional constructor parameters if you want
	protected BrowserDescriptor(
			Class<? extends Browser> clazz) {
		super(clazz);
	}

	protected BrowserDescriptor() {
	}
	
    public FormValidation doCheckMaxInstances(@QueryParameter String value)
    throws IOException, ServletException {
    	return FormValidation.validatePositiveInteger(value);
}
	
}
