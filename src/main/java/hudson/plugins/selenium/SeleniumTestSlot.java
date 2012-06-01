package hudson.plugins.selenium;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;

/**
 * Selenium Remote Control instance.
 *
 * <p>
 * This class is used to expose RC data to the remoting API, as well as
 * using this from index.jelly rendering of {@link PluginImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class SeleniumTestSlot implements Comparable<SeleniumTestSlot>, Serializable {
    /**
     * is anything running?
     */
    private final boolean isReserved;
    private final URL host;
    private final Map<String,String> capabilities;

    public SeleniumTestSlot(TestSlot testSlot) {
        RemoteProxy proxy = testSlot.getProxy();
        host = proxy.getRemoteHost();
        capabilities = toCapabilities(testSlot);
        isReserved = testSlot.getSession() != null;
    }

    private Map<String,String> toCapabilities(TestSlot testSlot) {
        Map<String,String> r = new HashMap<String, String>();
        for (Entry<String, Object> e : testSlot.getCapabilities().entrySet()) {
            r.put(e.getKey(),e.getValue().toString());
        }
        return r;
    }

    public String getHostAndPort() {
        return host.toExternalForm();
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
        if(isReserved)  return "In use";
        else            return "Idle";
    }

    public int compareTo(SeleniumTestSlot that) {
        int r = this.getHost().compareTo(that.getHost());
        if(r!=0)    return r;
        return this.getPort()-that.getPort();
    }

    private static final long serialVersionUID = 1L;
}
