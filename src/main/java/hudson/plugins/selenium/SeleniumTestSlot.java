package hudson.plugins.selenium;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Selenium Remote Control instance.
 * 
 * <p>
 * This class is used to expose RC data to the remoting API, as well as using this from index.jelly rendering of {@link PluginImpl}.
 * 
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class SeleniumTestSlot implements Comparable<SeleniumTestSlot>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Map<String, String> ENV_MAPPING = new HashMap<String, String>();
    /**
     * is anything running?
     */
    private final boolean isReserved;
    private final URL host;
    private final Map<String, String> capabilities;

    public SeleniumTestSlot(TestSlot testSlot) {
        RemoteProxy proxy = testSlot.getProxy();
        host = proxy.getRemoteHost();
        capabilities = toCapabilities(testSlot);
        isReserved = testSlot.getSession() != null;
    }

    private Map<String, String> toCapabilities(TestSlot testSlot) {
        Map<String, String> r = new HashMap<String, String>();
        for (Entry<String, Object> e : testSlot.getCapabilities().entrySet()) {
            r.put(e.getKey(), e.getValue().toString());
        }
        return r;
    }

    @Exported
    public String getHost() {
        return host.getHost();
    }

    @Exported
    public int getPort() {
        return host.getPort();
    }

    @Exported
    public Map<String, String> getCapabilities() {
        return capabilities;
    }

    @Exported
    public boolean isReserved() {
        return isReserved;
    }

    public String getStatus() {
        if (isReserved)
            return "In use";
        else
            return "Idle";
    }

    static {
        ENV_MAPPING.put("*iexplore", "internet explorer");
        ENV_MAPPING.put("*firefox", "firefox");
        ENV_MAPPING.put("*googlechrome", "chrome");
        ENV_MAPPING.put("*opera", "opera");
        ENV_MAPPING.put("*MicrosoftEdge", "Microsoft Edge");
    }

    public String getBrowserName() {
        return getCapabilities().get("browserName");
    }

    public String getUnifiedBrowserName() {
        String browser = getCapabilities().get("browserName");
        String unified = ENV_MAPPING.get(browser);
        return unified != null ? unified : browser;
    }

    public int compareTo(SeleniumTestSlot that) {
        int r = this.getHost().compareTo(that.getHost());
        if (r != 0)
            return r;
        return this.getPort() - that.getPort();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SeleniumTestSlot that = (SeleniumTestSlot) o;

        return new EqualsBuilder()
                .append(isReserved, that.isReserved)
                .append(host, that.host)
                .append(capabilities, that.capabilities)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(isReserved)
                .append(host)
                .append(capabilities)
                .toHashCode();
    }
}
