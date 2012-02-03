package hudson.plugins.selenium.configuration.browser;

import hudson.model.Descriptor;

import java.util.ArrayList;
import java.util.List;

public abstract class BrowserDescriptor extends
		Descriptor<Browser> {

	// define additional constructor parameters if you want
	protected BrowserDescriptor(
			Class<? extends Browser> clazz) {
		super(clazz);
	}

	protected BrowserDescriptor() {
	}
	
}
