package hudson.plugins.selenium;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.tasks.Mailer;
import org.jvnet.hudson.test.HudsonTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class SeleniumTest extends HudsonTestCase {
    @Override
    protected Hudson newHudson() throws Exception {
        Hudson h = super.newHudson();
        Mailer.descriptor().setHudsonUrl(getURL().toExternalForm());
        return h;
    }

    public void testSelenium1() throws Exception {
        getPlugin().waitForHubLaunch();
        
        // system config to set the root URL
        submit(new WebClient().goTo("configure").getFormByName("config"));

        createSlave(Label.get("foo"));
        waitForRC();
        Thread.sleep(5000);

        Selenium browser = new DefaultSelenium("localhost",
            4444, "*firefox"/* /usr/lib/firefox-3.6.3/firefox-bin"*/, "http://www.google.com");
        browser.start();

        try {
            browser.open("http://www.yahoo.com/");
            browser.type("p", "hello world");
            browser.click("search-submit");
            browser.waitForPageToLoad("10000");
            assertTrue(browser.getTitle().contains("hello world"));
            assertTrue(browser.getTitle().contains("Yahoo"));
        } finally {
            browser.stop();
        }


        DesiredCapabilities dc = DesiredCapabilities.firefox();
        dc.setCapability("jenkins.label","foo");
        WebDriver wd = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);

        try {
            wd.get("http://www.yahoo.com/");
            wd.findElement(By.name("p")).sendKeys("hello world");
            wd.findElement(By.id("search-submit")).click();
            assertTrue(wd.getTitle().contains("hello world"));
            assertTrue(wd.getTitle().contains("Yahoo"));
        } finally {
            wd.close();
        }
    }
    
    private void waitForRC() throws Exception {
        for(int i=0; i<100; i++) {
            if(!getPlugin().getRemoteControls().isEmpty())
                return;
            Thread.sleep(500);
        }
        throw new AssertionError("No RC had checked in");
    }

    private PluginImpl getPlugin() {
        return hudson.getPlugin(PluginImpl.class);
    }

    public void testLabelMatch() throws Exception {
        createSlave(Label.get("foo"));

        DesiredCapabilities dc = DesiredCapabilities.firefox();
        dc.setCapability("jenkins.label","bar");
        try {
            new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
            fail(); // should have failed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
