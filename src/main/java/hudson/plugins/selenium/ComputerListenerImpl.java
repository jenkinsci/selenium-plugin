package hudson.plugins.selenium;

import hudson.Extension;
import hudson.FilePath.FileCallable;
import hudson.console.HyperlinkNote;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import hudson.util.IOException2;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * When a new slave is connected, launch a selenium RC.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ComputerListenerImpl extends ComputerListener implements Serializable {
    /**
     * Starts a selenium Grid node remotely.
     */
    public void onOnline(Computer c, final TaskListener listener) throws IOException, InterruptedException {
        LOGGER.fine("Examining if we need to start Selenium Grid Node");

        PluginImpl p = Hudson.getInstance().getPlugin(PluginImpl.class);
        
        final String exclusions = p.getExclusionPatterns();
        List<String> exclusionPatterns = new ArrayList<String>();
        if (StringUtils.hasText(exclusions)) {
            exclusionPatterns = Arrays.asList(exclusions.split(SEPARATOR));
        }
        if (exclusionPatterns.size() > 0){
            // loop over all the labels and check if we need to exclude a node based on the exlusionPatterns
            for(Label l : c.getNode().getAssignedLabels()) {
                for(String pattern : exclusionPatterns){
                    if (l.toString().matches(pattern)) {
                        LOGGER.fine("Node " + c.getNode().getDisplayName() + " is excluded from Selenium Grid because its label '"
                                + l + "' matches exclusion pattern '" + pattern + "'");
                        return;
                    }
                }
            }
        }

        final String masterName = PluginImpl.getMasterHostName();
        if(masterName==null) {
            listener.getLogger().println("Unable to determine the host name of the master. Skipping Selenium execution. "
                +"Please "+ HyperlinkNote.encodeTo("/configure", "configure the Jenkins URL")+" from the system configuration screen.");
            return;
        }
        final String hostName = c.getHostName();
        if(hostName==null) {
            listener.getLogger().println("Unable to determine the host name. Skipping Selenium execution.");
            return;
        }
        final int masterPort = p.getPort();
        final int nrc = c.getNumExecutors();
        final StringBuilder labelList = new StringBuilder();
        for(Label l : c.getNode().getAssignedLabels()) {
            labelList.append('/');
            labelList.append(l);
        }
        labelList.append('/');

        // user defined parameters for starting the RC
        final List<String> userArgs = new ArrayList<String>();
        if (hasText(p.getRcLog())){
            userArgs.add("-log");
            userArgs.add(p.getRcLog());
        }
        if (p.getRcBrowserSideLog()){
            userArgs.add("-browserSideLog");
        }
        if (p.getRcDebug()){
            userArgs.add("-debug");
        }
        if (p.getRcTrustAllSSLCerts()){
            userArgs.add("-trustAllSSLCertificates");
        }
        if (p.getRcBrowserSessionReuse()) {
        	userArgs.add("-browserSessionReuse");
        }
        if (hasText(p.getRcFirefoxProfileTemplate())){
            userArgs.add("-firefoxProfileTemplate");
            userArgs.add(p.getRcFirefoxProfileTemplate());
        }


        // make sure that Selenium Hub is started before we start RCs.
        try {
            p.waitForHubLaunch();
        } catch (ExecutionException e) {
            throw new IOException2("Failed to wait for the Hub launch to complete",e);
        }

        LOGGER.fine("Going to start "+nrc+" RCs on "+c.getName());
        c.getNode().getRootPath().actAsync(new FileCallable<Object>() {
            public Object invoke(File f, VirtualChannel channel) throws IOException {
                String alreadyStartedPropertyName = getClass().getName() + ".seleniumRcAlreadyStarted";
                if (Boolean.valueOf(System.getProperty(alreadyStartedPropertyName))) {
                    LOGGER.info("Skipping Selenium RC execution because this slave has already started its RCs");
                    return null;
                }

                try {
                    for (int i=0; i<nrc; i++) {
                        // this is potentially unsafe way to figure out a free port number, but it's far easier
                        // than patching Selenium
                        ServerSocket ss = new ServerSocket(0);
                        int port = ss.getLocalPort();
                        ss.close();

                        String[] defaultArgs = new String[] {"-host",hostName,"-port",String.valueOf(port),"-env",labelList.toString(),"-hubURL","http://"+masterName+":"+masterPort+"/" };
                        PluginImpl.createSeleniumRCVM(f,listener).callAsync(
                                new RemoteControlLauncher((String[]) ArrayUtils.addAll(defaultArgs, userArgs.toArray(new String[0]))));
                    }
                } catch (Exception t) {
                    LOGGER.log(Level.WARNING,"Selenium RC launch failed",t);
                    throw new IOException2("Selenium RC launch interrupted",t);
                }

                System.setProperty(alreadyStartedPropertyName, Boolean.TRUE.toString());
                return null;
            }
        });
    }

    private boolean hasText(String s){
        return s != null && s.trim().length() > 0;
    }

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ComputerListenerImpl.class.getName());

    private static final String SEPARATOR = ",";    
}
