package jar;

import com.thoughtworks.selenium.*;
import junit.framework.*;

public class GoogleTest extends TestCase {
    private Selenium browser;
    public void setUp() {
        browser = new DefaultSelenium("localhost",
            4444, "foo:*firefox", "http://www.google.com");
        browser.start();
    }

    public void testGoogle() {
        browser.open("http://www.yahoo.com/");
        browser.type("p", "hello world");
        browser.click("search-submit");
        browser.waitForPageToLoad("10000");
        assertTrue(browser.getTitle().contains("hello world"));
        assertTrue(browser.getTitle().contains("Yahoo"));
    }

    public void tearDown() {
        browser.stop();
    }
}
