package hudson.plugins.selenium;

import hudson.model.Hudson;
import hudson.model.Node.Mode;
import hudson.plugins.selenium.configuration.CustomConfiguration;
import hudson.plugins.selenium.configuration.browser.Browser;
import hudson.plugins.selenium.configuration.browser.HTMLUnitBrowser;
import hudson.slaves.NodeProperty;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.Mailer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jvnet.hudson.test.HudsonTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * @author Kohsuke Kawaguchi
 * @author Richard Lavoie
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
        
        List<Browser> browsers = new ArrayList<Browser>();
        browsers.add(new HTMLUnitBrowser(1));

        CustomConfiguration cc = new CustomConfiguration(5000, false, false, false, false, -1, "", browsers, null);        
        
        HtmlPage newSlave = submit(new WebClient().goTo("configure").getFormByName("config"));
        DumbSlave slave = new DumbSlave("foo", "dummy", createTmpDir().getPath(), "1", Mode.NORMAL, "foo", createComputerLauncher(null), RetentionStrategy.NOOP, null);
        hudson.addNode(slave);

        waitForRC();
        Thread.sleep(5000);

        Selenium browser = new DefaultSelenium("localhost", 5000, "*htmlunit", "http://www.google.com");
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
            wd.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            wd.findElement(By.id("main"));
            assertTrue(wd.getTitle().contains("hello world"));
            assertTrue(wd.getTitle().contains("Yahoo"));
        } finally {
            wd.close();
        }
    }
        
    private void waitForRC() throws Exception {
        for(int i=0; i<10; i++) {
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
        getPlugin().waitForHubLaunch();
        
        // system config to set the root URL
        
        List<Browser> browsers = new ArrayList<Browser>();
        browsers.add(new HTMLUnitBrowser(1));

        CustomConfiguration cc = new CustomConfiguration(5000, false, false, false, false, -1, "", browsers, null);        
        
        HtmlPage newSlave = submit(new WebClient().goTo("configure").getFormByName("config"));
        DumbSlave slave = new DumbSlave("foo", "dummy", createTmpDir().getPath(), "1", Mode.NORMAL, "foo", createComputerLauncher(null), RetentionStrategy.NOOP, null);

        hudson.addNode(slave);

        waitForRC();

        DesiredCapabilities dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.label","bar");
        try {
            WebDriver dr = new RemoteWebDriver(new URL("http://localhost:5000/wd/hub"),dc);
            fail(); // should have failed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
