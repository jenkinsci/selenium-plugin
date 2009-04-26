package jar;

import com.thoughtworks.selenium.*;
import junit.framework.*;

public class GoogleTest extends TestCase {
    private Selenium browser;
    public void setUp() {
        browser = new DefaultSelenium("localhost",
            4444, "foo:*firefox /usr/lib/firefox-3.0.8/firefox", "http://www.google.com");
        browser.start();
    }

    public void testGoogle() {
        browser.open("http://www.google.com/webhp?hl=en");
        browser.type("q", "hello world");
        browser.click("btnG");
        browser.waitForPageToLoad("5000");
        assertEquals("hello world - Google Search", browser.getTitle());
    }

    public void tearDown() {
        browser.stop();
    }
}
