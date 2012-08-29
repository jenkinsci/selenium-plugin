package hudson.plugins.selenium;

import hudson.model.Hudson;
import hudson.model.Node.Mode;
import hudson.plugins.selenium.configuration.CustomConfiguration;
import hudson.plugins.selenium.configuration.browser.Browser;
import hudson.plugins.selenium.configuration.browser.HTMLUnitBrowser;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.plugins.selenium.configuration.global.matcher.MatchAllMatcher;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.Mailer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jvnet.hudson.test.HudsonTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        getPlugin().getGlobalConfigurations().add(new SeleniumGlobalConfiguration("test", new MatchAllMatcher(), cc));
        //HtmlPage newSlave = submit(new WebClient().goTo("configure").getFormByName("config"));
        DumbSlave slave = new DumbSlave("foo", "dummy", createTmpDir().getPath(), "1", Mode.NORMAL, "foo", createComputerLauncher(null), RetentionStrategy.NOOP);
        hudson.addNode(slave);

        waitForRC();

        DesiredCapabilities dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.label","foo");
        WebDriver wd = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);

        try {
            wd.get("http://www.google.com/");
            new WebDriverWait(wd, 5).until(ExpectedConditions.presenceOfElementLocated(By.id("pocs")));
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
        getPlugin().waitForHubLaunch();
        
        // system config to set the root URL
        
        List<Browser> browsers = new ArrayList<Browser>();
        browsers.add(new HTMLUnitBrowser(1));

        CustomConfiguration cc = new CustomConfiguration(5000, false, false, false, false, -1, "", browsers, null);        
        
        getPlugin().getGlobalConfigurations().add(new SeleniumGlobalConfiguration("test", new MatchAllMatcher(), cc));
        Mailer.descriptor().setHudsonUrl(getURL().toExternalForm());
        
        //HtmlPage newSlave = submit(new WebClient().goTo("configure").getFormByName("config"));
        DumbSlave slave = new DumbSlave("foo", "dummy", createTmpDir().getPath(), "1", Mode.NORMAL, "foo", createComputerLauncher(null), RetentionStrategy.NOOP);

        hudson.addNode(slave);

        waitForRC();

        DesiredCapabilities dc = DesiredCapabilities.htmlUnit();
        dc.setCapability("jenkins.label","bar");
        try {
            WebDriver dr = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
            fail(); // should have failed
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        dc.setCapability("jenkins.label","foo");
        try {
            WebDriver dr = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),dc);
        } catch (Exception e) {
        	e.printStackTrace();
        	fail(); // should have failed
        }

    }
    
    protected WebDriver configureSelenium() {
    	WebDriver wd = new HtmlUnitDriver();
    	wd.get("http://localhost:8080/configure");
    	
    	WebElement main = wd.findElement(By.id("main-table"));
    	new WebDriverWait(wd, 5000).until(ExpectedConditions.elementToBeClickable(By.name("Submit")));
    	wd.findElement(By.name("Submit")).click();
        new WebDriverWait(wd, 5000).until(ExpectedConditions.stalenessOf(main));
        return wd;
    }
    
}
