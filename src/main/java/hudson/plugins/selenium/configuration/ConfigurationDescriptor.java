package hudson.plugins.selenium.configuration;

import hudson.model.Descriptor;

public abstract class ConfigurationDescriptor extends Descriptor<SeleniumNodeConfiguration> {

	// define additional constructor parameters if you want
	protected ConfigurationDescriptor(Class<? extends SeleniumNodeConfiguration> clazz) {
		super(clazz);
	}

	protected ConfigurationDescriptor() {
	}

}