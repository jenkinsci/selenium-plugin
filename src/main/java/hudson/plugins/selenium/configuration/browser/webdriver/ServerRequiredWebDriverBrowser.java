/**
 * 
 */
package hudson.plugins.selenium.configuration.browser.webdriver;

import org.kohsuke.stapler.export.Exported;

/**
 * This is an abstract object that holds the serer binary for selenium browsers that uses an intermediate server to communicate with the browser : IE
 * and Chrome.
 * 
 * @author Richard Lavoie
 * 
 */
public abstract class ServerRequiredWebDriverBrowser extends WebDriverBrowser {

    /**
     * 
     */
    private static final long serialVersionUID = -4250465507404287777L;

    /**
     * Path to the server binary used to communicate with the browser.
     */
    private String serverBinary;

    /**
     * 
     * @param instances
     *            Number of instances to run of this browser type session.
     * @param version
     *            Version of the browser to use.
     * @param name
     *            Name of the browser
     * @param server_binary
     *            Path to the server binary that communicate with the real browser
     */
    protected ServerRequiredWebDriverBrowser(int instances, String version, String name, String server_binary) {
        super(instances, version, name);
        this.serverBinary = server_binary;
    }

    @Exported
    public String getServerBinary() {
        return serverBinary;
    }
}
