package hudson.plugins.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import hudson.plugins.selenium.configuration.SeleniumNodeConfiguration;
import hudson.plugins.selenium.configuration.CustomWDConfiguration;
import hudson.plugins.selenium.configuration.browser.webdriver.WebDriverBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.HTMLUnitBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.IEBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.OperaBrowser;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher;
import hudson.plugins.selenium.configuration.global.matcher.NodeLabelMatcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Kohsuke Kawaguchi
 * @author Richard Lavoie
 */
public class SeleniumTest {

	private static final String WEB_SITE_URL = "http://jenkins-ci.org/";
	
	@Rule
	public JenkinsRule j = new JenkinsRule();

    @Test
    public void testWDConfiguration() throws Exception {
        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(1));
        browsers.add(new IEBrowser(1, "", ""));
        browsers.add(new FirefoxBrowser(1, "", ""));
        browsers.add(new OperaBrowser(1, "", ""));

        CustomWDConfiguration cc = new CustomWDConfiguration(5000, -1, browsers, null);
        addConfiguration("customWD", new NodeLabelMatcher("label-node"), cc);
        j.createSlave("label-node", "label-node", null);
        
        waitForRC();
        
        Collection<SeleniumTestSlotGroup> slots = getPlugin().getRemoteControls();
        assertEquals(1, slots.size());
        List<SeleniumTestSlot> testSlots = slots.iterator().next().getSlots();
        assertEquals(4, testSlots.size());
        assertHasBrowser(true, testSlots, DesiredCapabilities.firefox().getBrowserName());
        assertHasBrowser(true, testSlots, DesiredCapabilities.htmlUnit().getBrowserName());
        assertHasBrowser(true, testSlots, DesiredCapabilities.internetExplorer().getBrowserName());
        assertHasBrowser(true, testSlots, DesiredCapabilities.opera().getBrowserName());
    }
    
    private static void assertHasBrowser(boolean validationValue, List<SeleniumTestSlot> slots, String browser) {
    	boolean contains = false;
    	if (slots != null) {
	    	for (SeleniumTestSlot slot : slots) {
	    		if (slot.getBrowserName().equals(browser)) {
	    			contains = true; 
	    			break;
	    		}
	    	}
    	}
    	assertEquals(validationValue, contains);
    }

    @Test
    public void testSelenium1() throws Exception {
        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(10));

        CustomWDConfiguration cc = new CustomWDConfiguration(5001, -1, browsers, null);
        addConfiguration("test", new NodeLabelMatcher("foolabel"), cc);
        j.createSlave("foo", "foolabel", null);

        waitForRC();
        
        DesiredCapabilities dc = DesiredCapabilities.htmlUnit();

        // No label requested should find the node
        WebDriver wd = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), dc);
        try {
            wd.get(WEB_SITE_URL);
            new WebDriverWait(wd, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("logo")));
        } finally {
            wd.quit();
        }

        dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.label", "foolabel");
        WebDriver dr = null;
        try {
            dr = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), dc);
        } catch (Exception e) {
            fail(e.getMessage()); // should have passed
        } finally {
        	if (dr != null) {
        		dr.quit();
        	}
        }

        dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.nodeName", "foo");
        try {
            dr = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), dc);
        } catch (Exception e) {
            fail(e.getMessage()); // should have passed
        } finally {
        	if (dr != null) {
        		dr.quit();
        	}
        }

        dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.label", "bar");
        try {
            new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), dc);
            fail("jenkins.label=bar should not return a valid session"); // should have failed
        } catch (Exception e) {

        }

    }

    private void addConfiguration(String name, SeleniumConfigurationMatcher matcher, SeleniumNodeConfiguration configuration) {
    	getPlugin().getGlobalConfigurations().add(new SeleniumGlobalConfiguration(name, matcher, configuration));
		
	}

	private void waitForRC() throws Exception {
        getPlugin().waitForHubLaunch();
        for (int i = 0; i < 50; i++) {
            Collection<SeleniumTestSlotGroup> slots = getPlugin().getRemoteControls();
            if (!slots.isEmpty()) {
            	//Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[] { Which.classFileUrl(Hub.class) }, ClassLoader.getSystemClassLoader()));
                return;
            }
            Thread.sleep(2000);
        }
        throw new AssertionError("No RC had checked in");
    }

    private PluginImpl getPlugin() {
        return j.jenkins.getPlugin(PluginImpl.class);
    }

    @Test 
    public void testLabelMatch() throws Exception {

        // system config to set the root URL

        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(1));

        CustomWDConfiguration cc = new CustomWDConfiguration(5002, -1, browsers, null);

        getPlugin().getGlobalConfigurations().add(new SeleniumGlobalConfiguration("test", new NodeLabelMatcher("foolabel"), cc));
        j.createSlave("foo", "foolabel", null);

        waitForRC();

    }

}
