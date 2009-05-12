package hudson.plugins.selenium;

import hudson.Extension;
import hudson.FilePath.FileCallable;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * When a new slave is connected, launch a selenium RC.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ComputerListenerImpl extends ComputerListener implements Serializable {
    /**
     * Starts a selenium RC remotely.
     */
    public void onOnline(Computer c, final TaskListener listener) throws IOException, InterruptedException {
        LOGGER.fine("Examining if we need to start Selenium RC");

        final String masterName = PluginImpl.getMasterHostName();
        if(masterName==null) {
            listener.getLogger().println("Unable to determine the host name of the master. Skipping Selenium execution.");
            return;
        }
        final String hostName = c.getHostName();
        if(hostName==null) {
            listener.getLogger().println("Unable to determine the host name. Skipping Selenium execution.");
            return;
        }
        final int masterPort = Hudson.getInstance().getPlugin(PluginImpl.class).getPort();
        final int nrc = c.getNumExecutors();
        final StringBuilder labelList = new StringBuilder();
        for(Label l : c.getNode().getAssignedLabels()) {
            labelList.append('/');
            labelList.append(l);
        }
        labelList.append('/');

        LOGGER.fine("Going to start "+nrc+" RCs on "+c.getName());
        c.getNode().getRootPath().actAsync(new FileCallable<Object>() {
            public Object invoke(File f, VirtualChannel channel) throws IOException {
                try {
                    for (int i=0; i<nrc; i++) {
                        // this is potentially unsafe way to figure out a free port number, but it's far easier
                        // than patching Selenium
                        ServerSocket ss = new ServerSocket(0);
                        int port = ss.getLocalPort();
                        ss.close();
                        PluginImpl.createSeleniumRCVM(f,listener).callAsync(new RemoteControlLauncher(
                                "-host",hostName,"-port",String.valueOf(port),"-env",labelList.toString(),"-hubURL","http://"+masterName+":"+masterPort+"/"));
                    }
                } catch (Exception t) {
                    throw new IOException2("Selenium RC launch interrupted",t);
                }
                return null;
            }
        });
    }

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ComputerListenerImpl.class.getName());
}
