package hudson.plugins.selenium;

import hudson.model.Hudson;
import hudson.model.Node.Mode;
import hudson.plugins.selenium.configuration.CustomWDConfiguration;
import hudson.plugins.selenium.configuration.browser.webdriver.FirefoxBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.HTMLUnitBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.IEBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.OperaBrowser;
import hudson.plugins.selenium.configuration.browser.webdriver.WebDriverBrowser;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.MatchAllMatcher;
import hudson.plugins.selenium.configuration.global.matcher.NodeLabelMatcher;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.Mailer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jvnet.hudson.test.HudsonTestCase;
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
public class SeleniumTest extends HudsonTestCase {
    @Override
    protected Hudson newHudson() throws Exception {
        Hudson h = super.newHudson();
        Mailer.descriptor().setHudsonUrl(getURL().toExternalForm());
        return h;
    }

    public void testWDConfiguration() throws Exception {
        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(1));
        browsers.add(new IEBrowser(1, "", ""));
        browsers.add(new FirefoxBrowser(1, "", ""));
        browsers.add(new OperaBrowser(1, "", ""));

    	CustomWDConfiguration cc = new CustomWDConfiguration(5000, -1, browsers, null);
    	SeleniumRunOptions opt = cc.initOptions(null);
    	System.out.println(opt.getEnvironmentVariables());
    	System.out.println(opt.getJVMArguments());
    	System.out.println(opt.getSeleniumArguments());
    }
    
    public void testSelenium1() throws Exception {
        getPlugin().waitForHubLaunch();
        
        // system config to set the root URL
        
        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(10));

        CustomWDConfiguration cc = new CustomWDConfiguration(5001, -1, browsers, null);
        getPlugin().getGlobalConfigurations().add(new SeleniumGlobalConfiguration("test", new MatchAllMatcher(), cc));
        //HtmlPage newSlave = submit(new WebClient().goTo("configure").getFormByName("config"));
        DumbSlave slave = new DumbSlave("foo", "dummy", createTmpDir().getPath(), "1", Mode.NORMAL, "foolabel", createComputerLauncher(null), RetentionStrategy.NOOP);
        hudson.addNode(slave);

        waitForRC();

        DesiredCapabilities dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.label","foo");
        WebDriver wd = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);

        try {
            wd.get("http://www.google.com/");
            new WebDriverWait(wd, 5).until(ExpectedConditions.presenceOfElementLocated(By.id("pocs")));
        } finally {
            wd.quit();
        }

        dc = DesiredCapabilities.htmlUnit();
        System.out.println("jenkins.label=foolabel");
        dc.setCapability("jenkins.label","foolabel");
        try {
            WebDriver dr = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
            dr.quit();
        } catch (Exception e) {
            fail(e.getMessage()); // should have passed
        }

        System.out.println("jenkins.nodeName=foo");
        dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.nodeName","foo");
        try {
            WebDriver dr = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
            dr.quit();
        } catch (Exception e) {
            fail(e.getMessage()); // should have passed
        }

        dc.setCapability("jenkins.label","foolabel");
        System.out.println("jenkins.label=foolabel & jenkins.nodeName=foo");
        try {
            WebDriver dr = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
            dr.quit();
        } catch (Exception e) {
            fail(e.getMessage()); // should have passed
        }

        dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.label","bar");
        System.out.println("jenkins.label=bar");
        try {
            WebDriver dr = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
            fail("jenkins.label=bar should not return a valid session"); // should have failed
        } catch (Exception e) {

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
        getPlugin().waitForHubLaunch();
        
        // system config to set the root URL
        
        List<WebDriverBrowser> browsers = new ArrayList<WebDriverBrowser>();
        browsers.add(new HTMLUnitBrowser(1));

        CustomWDConfiguration cc = new CustomWDConfiguration(5002, -1, browsers, null);
        
        getPlugin().getGlobalConfigurations().add(new SeleniumGlobalConfiguration("test", new NodeLabelMatcher("foolabel"), cc));
        Mailer.descriptor().setHudsonUrl(getURL().toExternalForm());

        //HtmlPage newSlave = submit(new WebClient().goTo("configure").getFormByName("config"));
        DumbSlave slave = new DumbSlave("foo", "dummy", createTmpDir().getPath(), "1", Mode.NORMAL, "foolabel", createComputerLauncher(null), RetentionStrategy.NOOP);

        hudson.addNode(slave);

        waitForRC();

    }

}
