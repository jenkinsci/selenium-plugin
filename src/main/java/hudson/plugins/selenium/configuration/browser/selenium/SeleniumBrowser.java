package hudson.plugins.selenium.configuration.browser.selenium;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.plugins.selenium.configuration.browser.AbstractSeleniumBrowser;
import hudson.plugins.selenium.configuration.browser.BrowserDescriptor;
import jenkins.model.Jenkins;

import org.openqa.grid.common.SeleniumProtocol;

public abstract class SeleniumBrowser extends AbstractSeleniumBrowser<SeleniumBrowser> implements Describable<SeleniumBrowser> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 412877504468641135L;

	protected SeleniumBrowser(int instances, String version, String name) {
		super(SeleniumProtocol.Selenium, instances, version, name);
	}

	@SuppressWarnings("unchecked")
	public Descriptor<SeleniumBrowser> getDescriptor() {
		return Jenkins.getInstance().getDescriptor(getClass());
	}
	
	public static DescriptorExtensionList<SeleniumBrowser,SeleniumBrowserDescriptor> all() {
        return Hudson.getInstance().<SeleniumBrowser,SeleniumBrowserDescriptor>getDescriptorList(SeleniumBrowser.class);
    }
    
	public static class SeleniumBrowserDescriptor extends BrowserDescriptor<SeleniumBrowser> {
		
		@Override
		public String getDisplayName() {
			return "Selenium Browser";
		}

	}
	
}
