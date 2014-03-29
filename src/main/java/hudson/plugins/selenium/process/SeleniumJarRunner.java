/**
 * 
 */
package hudson.plugins.selenium.process;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.callables.RunningRemoteSetterCallable;
import hudson.plugins.selenium.callables.SeleniumCallable;
import hudson.plugins.selenium.callables.StopSeleniumServer;
import hudson.remoting.VirtualChannel;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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
                    .actAsync(new SeleniumCallable(seleniumJar, nodehost, masterName, p.getPort(), nodeName, listener, name, opts));
        }
    }

    /*
     * (non-Javadoc)
     * @see hudson.plugins.selenium.configuration.SeleniumRunner#stop(hudson.model.Computer)
     */
    public void stop(Computer computer, String name) {
        // TODO Auto-generated method stub
        System.out.println(computer);
        System.out.println(name);

        VirtualChannel slaveChannel = computer.getNode().getChannel();
        if (slaveChannel != null) {
            try {
                slaveChannel.call(new StopSeleniumServer(name));
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    slaveChannel.call(new RunningRemoteSetterCallable(name, "Error"));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

}
