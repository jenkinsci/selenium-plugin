package hudson.plugins.selenium.configuration.browser;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Hudson;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class Browser implements Describable<Browser>, ExtensionPoint {
	
	private int maxInstances = 0;
	private String browserName;
	private String version;

	protected Browser(int instances, String version, String browser) {
		maxInstances = instances;
		this.version = version;
		browserName = browser;
		
	}
	
	@Exported
	public int getMaxInstances() {
		return maxInstances;
	}
	
	public String getBrowserName() {
		return browserName;
	}
	
	public BrowserDescriptor getDescriptor() {
        return (BrowserDescriptor)Hudson.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<Browser,BrowserDescriptor> all() {
        return Hudson.getInstance().<Browser,BrowserDescriptor>getDescriptorList(Browser.class);
    }
    
}
