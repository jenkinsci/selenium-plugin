package hudson.plugins.selenium.configuration.browser;

import hudson.model.Descriptor;

import java.util.ArrayList;
import java.util.List;

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
	
	public int getMaxInstances() {
		return maxInstances;
	}

	public List<String> getJVMArgs() {
		List<String> args = new ArrayList<String>();
		return args;
	}
	
	public List<String> getArgs() {
		List<String> args = new ArrayList<String>();
		combine(args, "browserName", browserName);
		combine(args, "maxInstances", maxInstances);
		return args;
	}
	
    public static void combine(List<String> options, String key, Object value) {
    	if (value != null) {
    		options.add(key + "=" + value.toString());
    	}
    }
}
