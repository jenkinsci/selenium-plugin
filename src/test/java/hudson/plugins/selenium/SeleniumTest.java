package hudson.plugins.selenium;

import hudson.plugins.selenium.configuration.CustomWDConfiguration;
import hudson.plugins.selenium.configuration.DirectJsonInputConfiguration;
import hudson.plugins.selenium.configuration.SeleniumNodeConfiguration;
import hudson.plugins.selenium.configuration.browser.webdriver.*;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.NodeLabelMatcher;
import hudson.plugins.selenium.configuration.global.matcher.SeleniumConfigurationMatcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Kohsuke Kawaguchi
 * @author Richard Lavoie
 */
public class SeleniumTest {

	private static final String WEB_SITE_URL = "http://jenkins.io/";

	@Rule
	public JenkinsRule j = new JenkinsRule();

    private int timeout = j.timeout;

    @Test
    public void testWDConfiguration() throws Exception {
        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(1));
        browsers.add(new IEBrowser(1, "", ""));
        browsers.add(new FirefoxBrowser(1, "", ""));
        browsers.add(new OperaBrowser(1, "", ""));

        CustomWDConfiguration cc = new CustomWDConfiguration(5000, -1, browsers, null, 5);
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
	public void testJSONConfig() throws Exception {
		final String jsonNodeConfig = "{\n" +
				"  \"capabilities\":\n" +
				"  [\n" +
				"    {\n" +
				"      \"browserName\": \"firefox\",\n" +
				"      \"maxInstances\": 20,\n" +
				"      \"seleniumProtocol\": \"WebDriver\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"browserName\": \"chrome\",\n" +
				"      \"maxInstances\": 10,\n" +
				"      \"seleniumProtocol\": \"WebDriver\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"proxy\": \"org.openqa.grid.selenium.proxy.DefaultRemoteProxy\",\n" +
				"  \"maxSession\": 15,\n" +
				"  \"port\": 5555,\n" +
				"  \"register\": true,\n" +
				"  \"registerCycle\": 15000,\n" +
				"  \"hub\": \"http://0.0.0.0:4444\",\n" +
				"  \"nodeStatusCheckTimeout\": 5000,\n" +
				"  \"nodePolling\": 5000,\n" +
				"  \"role\": \"node\",\n" +
				"  \"unregisterIfStillDownAfter\": 60000,\n" +
				"  \"downPollingLimit\": 2,\n" +
				"  \"debug\": false,\n" +
				"  \"servlets\" : [],\n" +
				"  \"withoutServlets\": [],\n" +
				"  \"custom\": {}\n" +
				"}\n";
		DirectJsonInputConfiguration jsonConfig = new DirectJsonInputConfiguration("JSON", jsonNodeConfig, "", "");
		addConfiguration("jsonWD", new NodeLabelMatcher("json-slave"), jsonConfig);
		
		j.createSlave("json-slave", "json-slave", null);
		waitForRC();
		
		//Get all the capability maps as a list
		List<Map<String, String>> capabilitySets = getPlugin().getRemoteControls()
				.stream()
				.findFirst()
				.orElseThrow(() -> new AssertionError("No SeleniumTestSlotGroups returned!"))
				.getSlots()
				.stream()
				.map(SeleniumTestSlot::getCapabilities)
				.collect(Collectors.toList());
		
		Assert.assertTrue("Looking for 10 Chrome browsers",
				capabilitySets.stream().anyMatch(c -> hasCapability("browserName", "chrome", c) && hasCapability("maxInstances", "10", c)));
		Assert.assertTrue("Looking for 20 Firefox browsers",
				capabilitySets.stream().anyMatch(c -> hasCapability("browserName", "firefox", c) && hasCapability("maxInstances", "20", c)));
		Assert.assertFalse("Should not contain Internet Explorer",
				capabilitySets.stream().anyMatch(c -> hasCapability("browserName", "internet explorer", c)));
		Assert.assertTrue("MaxSession should be set to 15",
				capabilitySets.stream().anyMatch(c -> hasCapability("maxSession", "15", c)));
		Assert.assertTrue("MaxSession should be set to 15",
				capabilitySets.stream().anyMatch(c -> hasCapability("registerSession", "15000", c)));
	}
	private static boolean hasCapability(String key, String value, Map<String, String> capabilities) {
		return capabilities.containsKey(key) && capabilities.get(key).equalsIgnoreCase(value);
	}

    @Test
    public void testHtmlUnitDriver() throws Exception {

        //Set jenkins rule timeout to never
        j.timeout = 0;

        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(10));

        CustomWDConfiguration cc = new CustomWDConfiguration(5001, -1, browsers, null, 5);
        addConfiguration("test", new NodeLabelMatcher("foolabel"), cc);
        j.createSlave("foo", "foolabel", null);

        waitForRC();

        DesiredCapabilities dc = DesiredCapabilities.htmlUnit();

        // No label requested should find the node
        WebDriver wd = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), dc);
        try {
            wd.get(WEB_SITE_URL);
            new WebDriverWait(wd, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("ji-home-carousel")));
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
        //Try for a maximum less than default test timeout of 180 seconds
        for (long i = System.currentTimeMillis() + (timeout * 900); System.currentTimeMillis() < i;) {
            Collection<SeleniumTestSlotGroup> slots = getPlugin().getRemoteControls();
            if (!slots.isEmpty()) {
            	//Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[] { Which.classFileUrl(Hub.class) }, ClassLoader.getSystemClassLoader()));
                return;
            }
            Thread.sleep(2000);
        }
        throw new AssertionError("No RC had checked in after " + (timeout * 0.9) + " seconds");
    }

    private PluginImpl getPlugin() {
        return j.jenkins.getPlugin(PluginImpl.class);
    }

    @Test
    public void testLabelMatch() throws Exception {

        // system config to set the root URL

        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(1));

        CustomWDConfiguration cc = new CustomWDConfiguration(5002, -1, browsers, null, 5);

        getPlugin().getGlobalConfigurations().add(new SeleniumGlobalConfiguration("test", new NodeLabelMatcher("foolabel"), cc));
        j.createSlave("foo", "foolabel", null);

        waitForRC();

    }

}
