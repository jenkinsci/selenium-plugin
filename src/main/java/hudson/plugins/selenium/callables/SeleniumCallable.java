package hudson.plugins.selenium.callables;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.RemoteControlLauncher;
import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

public class SeleniumCallable implements FileCallable<Object> {

	private static final Logger LOGGER = Logger.getLogger(SeleniumCallable.class.getName());

	
	private FilePath seleniumJar;
	private long jarTimestamp;
	private String masterName;
	private int masterPort;
	private String hostName;
	private String nodeName;
	private SeleniumRunOptions options;
	private TaskListener listener;
	
	public SeleniumCallable(FilePath jar, String hostName, String masterName, int masterPort, String nodeName, TaskListener listener, SeleniumRunOptions options) throws InterruptedException, IOException {
		seleniumJar = jar;
		jarTimestamp = jar.lastModified();
		this.masterName = masterName;
		this.masterPort = masterPort;
		this.hostName = hostName;
		this.nodeName = nodeName;
		this.options = options;
		this.listener = listener;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2047557797415325512L;
	
	public final static String ALREADY_STARTED = SeleniumCallable.class.getName() + ".seleniumRcAlreadyStarted";

	public Object invoke(File f, VirtualChannel channel) throws IOException {
        String alreadyStartedPropertyName = ALREADY_STARTED;
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
        	PropertyUtils.setProperty(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STARTING);
            
        	// this is potentially unsafe way to figure out a free port number, but it's far easier
            // than patching Selenium
            ServerSocket ss = new ServerSocket(0);
            int port = ss.getLocalPort();
            ss.close();

            String[] defaultArgs = new String[] {
                    "-role","node",
                    //"-host",hostName,
                    "-port",String.valueOf(port),
//              "-env",labelList.toString(),
                    "-hub","http://"+masterName+":"+masterPort+"/grid/register" };
            
            // TODO change this
            Channel jvm = PluginImpl.createSeleniumRCVM(localJar,listener, options.getJVMArguments(), options.getEnvironmentVariables());
            PropertyUtils.setProperty(SeleniumConstants.PROPERTY_JVM, jvm);
            jvm.callAsync(
                    new RemoteControlLauncher( nodeName,
                            (String[]) ArrayUtils.addAll(defaultArgs, options.getSeleniumArguments().toArray(new String[0]))));
            PropertyUtils.setProperty(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STARTED);
        } catch (Exception t) {
        	PropertyUtils.setProperty(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.ERROR);
            LOGGER.log(Level.WARNING,"Selenium launch failed",t);
            throw new IOException2("Selenium launch interrupted",t);
        }

        System.setProperty(alreadyStartedPropertyName, Boolean.TRUE.toString());
        return null;
    }
}