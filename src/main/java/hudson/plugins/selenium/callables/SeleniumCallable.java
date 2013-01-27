package hudson.plugins.selenium.callables;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.RemoteControlLauncher;
import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

public class SeleniumCallable implements FileCallable<Object> {

	private static final Logger LOGGER = Logger.getLogger(SeleniumCallable.class.getName());

	
	private FilePath seleniumJar;
	private long jarTimestamp;
	private String masterName;
	private int masterPort;
	private String nodeName;
	private SeleniumRunOptions options;
	private String config;
	private String nodehost;
	private TaskListener listener;
	
	public SeleniumCallable(FilePath jar, String nodehost, String masterName, int masterPort, String nodeName, TaskListener listener, String confName, SeleniumRunOptions options) throws InterruptedException, IOException {
		seleniumJar = jar;
		jarTimestamp = jar.lastModified();
		this.masterName = masterName;
		this.masterPort = masterPort;
		this.nodehost = nodehost;
		this.nodeName = nodeName;
		this.options = options;
		this.listener = listener;
		config = confName;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2047557797415325512L;
	
	//public final static String ALREADY_STARTED = SeleniumCallable.class.getName() + ".seleniumRcAlreadyStarted";

	public Object invoke(File f, VirtualChannel channel) throws IOException {
        //String alreadyStartedPropertyName = ALREADY_STARTED;
		//@SuppressWarnings("unchecked")
		//Map<String, RemoteRunningStatus> rConfig = (Map<String, RemoteRunningStatus>) PropertyUtils.getProperty(SeleniumConstants.PROPERTY_STATUS);
        RemoteRunningStatus status = (RemoteRunningStatus) PropertyUtils.getMapProperty(SeleniumConstants.PROPERTY_STATUS.displayName, config);

//		if (rConfig == null) {
            //listener.getLogger().println("rConfig is null");
//			rConfig = new HashMap<String, RemoteRunningStatus>();
//            PropertyUtils.setProperty(SeleniumConstants.PROPERTY_STATUS, rConfig);
		//}

        listener.getLogger().println("is running");
		//RemoteRunningStatus status = rConfig.get(config);
        if (status != null && status.isRunning()) {
            listener.getLogger().println("Skipping Selenium RC execution because this slave has already started its RCs");
            return null;
        }

        listener.getLogger().println("Copy grid jar");
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
        	//PropertyUtils.setProperty(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STARTING);

            String[] defaultArgs = new String[] {
                    "-role","node",
                    "-host", nodehost,
                    "-hub","http://"+masterName+":"+masterPort+"/grid/register" };
            
            
            listener.getLogger().println("Creating selenium VM");
            Channel jvm = PluginImpl.createSeleniumRCVM(localJar,listener, options.getJVMArguments(), options.getEnvironmentVariables());
            status = new RemoteRunningStatus(jvm, options);

            listener.getLogger().println("Starting the selenium process");
            jvm.callAsync(
                    new RemoteControlLauncher( nodeName,
                            (String[]) ArrayUtils.addAll(defaultArgs, options.getSeleniumArguments().toArray(new String[0])), config));
            status.setStatus(SeleniumConstants.STARTED);
            status.setRunning(true);
        } catch (Exception t) {
        	status.setRunning(false);
        	status.setStatus(SeleniumConstants.ERROR);
        	LOGGER.log(Level.WARNING,"Selenium launch failed",t);
            listener.getLogger().println(       "Selenium launch failed" + t.getMessage());
            
            throw new IOException2("Selenium launch interrupted",t);
        }
		PropertyUtils.setMapProperty(SeleniumConstants.PROPERTY_STATUS.displayName, config, status);

        //System.setProperty(alreadyStartedPropertyName, Boolean.TRUE.toString());
        return null;
    }
}