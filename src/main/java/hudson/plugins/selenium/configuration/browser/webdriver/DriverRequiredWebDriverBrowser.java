/**
 * 
 */
package hudson.plugins.selenium.configuration.browser.webdriver;

import org.kohsuke.stapler.export.Exported;

/**
 * This is an abstract object that holds the driver binary for selenium browsers that use a separate driver binary
 * to communicate with the browser : IE, Chrome, Edge, Opera, HTMLUnit (provided).
 *
 * HTMLUnit is provided in plugin as this is a jar, not a binary file, and thus not as easily added.
 * 
 * @author Richard Lavoie
 * 
 */
public abstract class DriverRequiredWebDriverBrowser extends WebDriverBrowser {

    /**
     * 
     */
    private static final long serialVersionUID = -4250465507404287777L;

    /**
     * Path to the server binary used to communicate with the browser.
     */
    private String driverBinaryPath;

    /**
     * 
     * @param instances
     *            Number of instances to run of this browser type session.
     * @param version
     *            Version of the browser to use.
     * @param name
     *            Name of the browser
     * @param driverBinaryPath
     *            Path to the driver binary that communicate with the browser
     */
    public DriverRequiredWebDriverBrowser(int instances, String version, String name, String driverBinaryPath) {
        super(instances, version, name);
        this.driverBinaryPath = driverBinaryPath;
    }

    @Exported
    public String getDriverBinaryPath() {

        return driverBinaryPath;
    }
}
