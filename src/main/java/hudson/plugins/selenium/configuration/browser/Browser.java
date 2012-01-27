package hudson.plugins.selenium.configuration.browser;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Hudson;

import java.util.List;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class Browser implements Describable<Browser>, ExtensionPoint {
	
	public BrowserDescriptor getDescriptor() {
        return (BrowserDescriptor)Hudson.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<Browser,BrowserDescriptor> all() {
        return Hudson.getInstance().<Browser,BrowserDescriptor>getDescriptorList(Browser.class);
    }

	public abstract List<String> getArgs();
}
