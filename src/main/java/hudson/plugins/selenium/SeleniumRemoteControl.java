package hudson.plugins.selenium;

import com.thoughtworks.selenium.grid.hub.remotecontrol.RemoteControlProxy;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Exported;

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
public class SeleniumRemoteControl implements Comparable<SeleniumRemoteControl> {
    private final RemoteControlProxy proxy;
    private final boolean isReserved;

    public SeleniumRemoteControl(RemoteControlProxy proxy, boolean reserved) {
        this.proxy = proxy;
        isReserved = reserved;
    }

    public String getHostAndPort() {
        return proxy.host()+':'+proxy.port();
    }

    @Exported
    public String getHost() {
        return proxy.host();
    }

    @Exported
    public int getPort() {
        return proxy.port();
    }

    @Exported
    public String getEnvironment() {
        return proxy.environment();
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
}
