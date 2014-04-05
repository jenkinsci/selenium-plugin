/**
 * 
 */
package hudson.plugins.selenium.process;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.plugins.selenium.HubHolder;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.callables.RunningRemoteSetterCallable;
import hudson.plugins.selenium.callables.SeleniumCallable;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.plugins.selenium.callables.StopSeleniumServer;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;

/**
 * @author Richard Lavoie
 * 
 */
public abstract class SeleniumJarRunner implements SeleniumProcess {

    public abstract SeleniumRunOptions initOptions(Computer c);

    public void start(Computer computer, TaskListener listener, String name) throws IOException, InterruptedException, ExecutionException {
        PluginImpl p = PluginImpl.getPlugin();

        final FilePath seleniumJar = new FilePath(SeleniumProcessUtils.findStandAloneServerJar());
        final String nodeName = computer.getName();
        final String masterName = PluginImpl.getMasterHostName();

        String nodehost = computer.getHostName();

        SeleniumRunOptions opts = initOptions(computer);
        opts.addOptionIfSet("-host", computer.getHostName());

        if (opts != null) {
            computer.getNode().getRootPath()
                    .act(new SeleniumCallable(seleniumJar, nodehost, masterName, p.getPort(), nodeName, listener, name, opts));
        }
    }

    /*
     * (non-Javadoc)
     * @see hudson.plugins.selenium.configuration.SeleniumRunner#stop(hudson.model.Computer)
     */
    public void stop(Computer computer, String name) {
        FilePath path = computer.getNode().getRootPath();
        if (path != null) {
            VirtualChannel slaveChannel = path.getChannel();
            try {
                final String url = slaveChannel.call(new StopSeleniumServer(name));
                PluginImpl.getPlugin().getHubChannel().call(new Callable<Void, Exception>() {

                    /**
                     * 
                     */
                    private static final long serialVersionUID = -5805313572457450300L;
                    private String remoteUrl = url;

                    public Void call() throws Exception {
                        Registry registry = HubHolder.hub.getRegistry();
                        if (registry != null) {
                            Iterator<RemoteProxy> it = registry.getAllProxies().iterator();
                            while (it.hasNext()) {
                                RemoteProxy proxy = it.next();
                                if (remoteUrl.equals(proxy.getRemoteHost().toString())) {
                                    registry.removeIfPresent(proxy);
                                }
                            }
                        }
                        return null;
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    slaveChannel.call(new RunningRemoteSetterCallable(name, SeleniumConstants.ERROR));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

}
