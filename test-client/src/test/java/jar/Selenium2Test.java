package jar;

import junit.framework.TestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

/**
 * Very simple test that uses Selenium Grid.
 *
 * @author Kohsuke Kawaguchi
 */
public class Selenium2Test extends TestCase {
    public void testFoo() throws Exception {
        WebDriver wd = new RemoteWebDriver(new URL("http://127.0.0.1:4444/wd/hub"), DesiredCapabilities.firefox());
        try {
            wd.get("http://www.yahoo.com/");
            wd.findElement(By.name("p")).sendKeys("hello world");
            wd.findElement(By.id("search-submit")).click();
//        wd.waitForPageToLoad("10000");

            assertTrue(wd.getTitle().contains("hello world"));
            assertTrue(wd.getTitle().contains("Yahoo"));
        } finally {
            wd.close();
        }
    }
}
