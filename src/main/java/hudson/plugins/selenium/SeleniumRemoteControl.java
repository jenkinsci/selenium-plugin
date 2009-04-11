package hudson.plugins.selenium;

import com.thoughtworks.selenium.grid.hub.remotecontrol.RemoteControlProxy;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Exported;

import java.io.Serializable;

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
public class SeleniumRemoteControl implements Comparable<SeleniumRemoteControl>, Serializable {
    private final boolean isReserved;
    private final String host;
    private final int port;
    private final String environment;

    public SeleniumRemoteControl(RemoteControlProxy proxy, boolean reserved) {
        host = proxy.host();
        port = proxy.port();
        environment = proxy.environment();
        isReserved = reserved;
    }

    public String getHostAndPort() {
        return host+':'+port;
    }

    @Exported
    public String getHost() {
        return host;
    }

    @Exported
    public int getPort() {
        return port;
    }

    @Exported
    public String getEnvironment() {
        return environment;
    }

    @Exported
    public boolean isReserved() {
        return isReserved;
    }

    public String getStatus() {
        if(isReserved)  return "In use";
        else            return "Idle";
    }

    public int compareTo(SeleniumRemoteControl that) {
        int r = this.getHost().compareTo(that.getHost());
        if(r!=0)    return r;
        return this.getPort()-that.getPort();
    }

    private static final long serialVersionUID = 1L;
}
