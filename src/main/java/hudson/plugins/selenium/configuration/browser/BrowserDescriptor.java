package hudson.plugins.selenium.configuration.browser;

import java.util.List;

import hudson.model.Descriptor;

public abstract class BrowserDescriptor extends
		Descriptor<Browser> {

	public int maxInstances = 0;
	
	// define additional constructor parameters if you want
	protected BrowserDescriptor(
			Class<? extends Browser> clazz) {
		super(clazz);
	}

	protected BrowserDescriptor() {
	}
	
	public abstract List<String> getArgs();
}
