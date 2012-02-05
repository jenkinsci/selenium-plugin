package hudson.plugins.selenium;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.console.HyperlinkNote;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.TaskListener;
import hudson.model.Hudson.MasterComputer;
import hudson.remoting.Future;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import hudson.util.IOException2;

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

import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.StringUtils;

/**
 * When a new slave is connected, launch a selenium RC.
 *
 * @author Kohsuke Kawaguchi
 * @author Richard Lavoie
 */
@Extension
public class ComputerListenerImpl extends ComputerListener implements Serializable {
	
    /**
     * Starts a selenium Grid node remotely.
     */
	@Override
    public void onOnline(Computer c, final TaskListener listener) throws IOException, InterruptedException {
        LOGGER.fine("Examining if we need to start Selenium Grid Node");

        NodePropertyImpl np = null;
        if (c instanceof MasterComputer) {
        	np = Hudson.getInstance().getGlobalNodeProperties().get(NodePropertyImpl.class);
        } else {
        	np = c.getNode().getNodeProperties().get(NodePropertyImpl.class);
        }
        
        if (np == null) {
        	//the node is configured to not start a grid node
        	LOGGER.fine("Node " + c.getNode().getDisplayName() + " is excluded from Selenium Grid because it is disabled");
        	return;
        }        
        
        final PluginImpl p = Hudson.getInstance().getPlugin(PluginImpl.class);
        
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
        final StringBuilder labelList = new StringBuilder();
        for(Label l : c.getNode().getAssignedLabels()) {
            labelList.append('/');
            labelList.append(l);
        }
        labelList.append('/');

        
        // make sure that Selenium Hub is started before we start RCs.
        try {
            p.waitForHubLaunch();
        } catch (ExecutionException e) {
            throw new IOException2("Failed to wait for the Hub launch to complete",e);
        }

        final SeleniumRunOptions options = np.initOptions(c);
        if (options == null) {
        	// if configuration returned no options, that means it doesn't want to start selenium
        	LOGGER.fine("The configuration returned no run options. Skipping selenium execution.");
        	return;        	
        }
        
        listener.getLogger().println("Starting Selenium Grid nodes on " + c.getName());

        final FilePath seleniumJar = new FilePath(PluginImpl.findStandAloneServerJar());
        final long jarTimestamp = seleniumJar.lastModified();
        final String nodeName = c.getName();

        try {
			Future<Object> future = c.getNode().getRootPath().actAsync(new FileCallable<Object>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 2047557797415325512L;

				public Object invoke(File f, VirtualChannel channel) throws IOException {
			        String alreadyStartedPropertyName = getClass().getName() + ".seleniumRcAlreadyStarted";
			        if (Boolean.valueOf(System.getProperty(alreadyStartedPropertyName))) {
			            LOGGER.info("Skipping Selenium RC execution because this slave has already started its RCs");
			            return null;
			        }
			        
			        File localJar = new File(f,seleniumJar.getName());
			        if (localJar.lastModified() != jarTimestamp) {
			            try {
			                seleniumJar.copyTo(new FilePath(localJar));
			                localJar.setLastModified(jarTimestamp);
			            } catch (InterruptedException e) {
			                throw new IOException2("Failed to copy grid jar",e);
			            }
			        }

			        try {
			            // this is potentially unsafe way to figure out a free port number, but it's far easier
			            // than patching Selenium
			            ServerSocket ss = new ServerSocket(0);
			            int port = ss.getLocalPort();
			            ss.close();

			            String[] defaultArgs = new String[] {
			                    "-role","node",
			                    "-host",hostName,
			                    "-port",String.valueOf(port),
//                          "-env",labelList.toString(),
			                    "-hub","http://"+masterName+":"+masterPort+"/grid/register" };
			            
			            // TODO change this
			            PluginImpl.createSeleniumRCVM(localJar,listener, options.getJVMArguments()).callAsync(
			                    new RemoteControlLauncher( nodeName,
			                            (String[]) ArrayUtils.addAll(defaultArgs, options.getSeleniumArguments().toArray(new String[0]))));
			        } catch (Exception t) {
			            LOGGER.log(Level.WARNING,"Selenium launch failed",t);
			            throw new IOException2("Selenium launch interrupted",t);
			        }

			        System.setProperty(alreadyStartedPropertyName, Boolean.TRUE.toString());
			        return null;
			    }
			});
			
			future.get();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ComputerListenerImpl.class.getName());

    private static final String SEPARATOR = ",";    
}
