package hudson.plugins.selenium.configuration.browser;

import java.util.List;

import hudson.model.Descriptor;

public abstract class BrowserDescriptor extends
		Descriptor<Browser> {

	private int maxInstances = 0;
	private String browserName;
	
	protected BrowserDescriptor(int instances, String name) {
		maxInstances = instances;
		browserName = name;
	}
	
	// define additional constructor parameters if you want
	protected BrowserDescriptor(
			Class<? extends Browser> clazz) {
		super(clazz);
	}

	protected BrowserDescriptor() {
	}
	
	public abstract List<String> getArgs();
}
