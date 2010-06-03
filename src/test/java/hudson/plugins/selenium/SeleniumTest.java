package hudson.plugins.selenium;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import hudson.model.Label;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class SeleniumTest extends HudsonTestCase {
    public void testRun() throws Exception {
        getPlugin().waitForHubLaunch();
        
        // system config to set the root URL
        submit(new WebClient().goTo("configure").getFormByName("config"));

        createSlave(new Label("foo"));
        waitForRC();

        Selenium browser;

        browser = new DefaultSelenium("localhost",
            4444, "foo:*firefox /Applications/Firefox.app/Contents/MacOS/firefox-bin", "http://www.google.com");
        browser.start();

        try {
            browser.open("http://www.google.com/webhp?hl=en");
            browser.type("q", "hello world");
            browser.click("btnG");
            browser.waitForPageToLoad("5000");
            assertEquals("hello world - Google Search", browser.getTitle());
        } finally {
            browser.stop();
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

//    public void testLabelMatch() throws Exception {
//        createSlave(new Label("foo"));
//
//        Selenium browser;
//
//        browser = new DefaultSelenium("localhost",
//            4444, "bar:*firefox /usr/lib/firefox-3.0.9/firefox", "http://www.google.com");
//        try {
//            browser.start();
//            fail(); // should have failed
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
