package hudson.plugins.selenium.configuration.browser;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Computer;
import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.plugins.selenium.SeleniumRunOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class Browser implements Describable<Browser>, ExtensionPoint {
	
	protected final String PARAM_BROWSER_NAME = "browserName";

	protected final String PARAM_MAX_INSTANCES = "maxInstances";
	
	protected final String PARAM_VERSION = "version";
	
	private int maxInstances = 0;
	private String version;

	protected Browser(int instances, String version) {
		maxInstances = instances;
		this.version = version;
	}
	
	@Exported
	public int getMaxInstances() {
		return maxInstances;
	}
	
	@Exported
	public String getVersion() {
		return version;
	}
	
	public BrowserDescriptor getDescriptor() {
        return (BrowserDescriptor)Hudson.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<Browser,BrowserDescriptor> all() {
        return Hudson.getInstance().<Browser,BrowserDescriptor>getDescriptorList(Browser.class);
    }

    /**
     * Retrieve the browser name
     * @return Browser name, must be one of 
     */
    public abstract String getBrowserName();
    
    /**
     * Combine the key and value on the key=value form and add that form in the options list
     * @param options List on which to add the key=value pair
     * @param key Key option
     * @param value Value option
     */
    public static void combine(List<String> options, String key, Object value) {
    	if (value != null) {
    		//TODO validate the " in the strings, this is error prone ...
    		options.add(key + "=" + value.toString());
    	}
    }

	public void initOptions(Computer c, SeleniumRunOptions opt) {
		List<String> args = new ArrayList<String>();
		combine(args, PARAM_BROWSER_NAME, getBrowserName());
		combine(args, PARAM_MAX_INSTANCES, maxInstances);
		combine(args, PARAM_VERSION, version);
		args.addAll(getAdditionnalOptions());
		
		List<String> opts = opt.getSeleniumArguments();
		opts.add("-browser");
		
		//TODO fix this ... error prone, need more validation on the ", see comment in combine()
		opts.add("\"" + StringUtils.join(args, ",") + "\"");
	}
	
	public List<String> getAdditionnalOptions() {
		return Collections.emptyList();
	}

	
}
