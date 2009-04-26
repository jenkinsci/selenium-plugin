package hudson.plugins.selenium;

import hudson.Extension;
import hudson.FilePath.FileCallable;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.remoting.Callable;
import hudson.slaves.ComputerListener;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Inet4Address;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

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
//        SeleniumEnvironmentProperty env = c.getNode().getNodeProperties().get(SeleniumEnvironmentProperty.class);
//        // launch RCs only for nodes that are configured accordingly
//        if(env==null) {
//            listener.getLogger().println("Selenium RC is disabled in configuration of this node. Skipping Selenium RC launch.");
//            return;
//        }

        final String masterName = PluginImpl.getMasterHostName();
        if(masterName==null) {
            listener.getLogger().println("Unable to determine the host name of the master. Skipping Selenium execution.");
            return;
        }
        final String hostName = getHostName(c);
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

// to be removed when bumping up to 1.302 and replaced by c.getHostName()
    private String getHostName(Computer c) throws IOException, InterruptedException {
        for( String address : c.getChannel().call(new ListPossibleNames())) {
            try {
                InetAddress ia = InetAddress.getByName(address);
                if(ia.isReachable(500))
                    return ia.getCanonicalHostName();
            } catch (IOException e) {
                // if a given name fails to parse on this host, we get this error
                LOGGER.log(Level.FINE, "Failed to parse "+address,e);
            }
        }
        return null;
    }

    private static class ListPossibleNames implements Callable<List<String>,IOException> {
        public List<String> call() throws IOException {
            List<String> names = new ArrayList<String>();

            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni =  nis.nextElement();
                Enumeration<InetAddress> e = ni.getInetAddresses();
                while (e.hasMoreElements()) {
                    InetAddress ia =  e.nextElement();
                    if(ia.isLoopbackAddress())  continue;
                    if(!(ia instanceof Inet4Address))   continue;
                    names.add(ia.getHostAddress());
                }
            }
            return names;
        }
        private static final long serialVersionUID = 1L;
    }
// until here

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ComputerListenerImpl.class.getName());
}
