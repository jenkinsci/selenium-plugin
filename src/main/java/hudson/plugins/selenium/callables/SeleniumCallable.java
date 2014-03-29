package hudson.plugins.selenium.callables;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.plugins.selenium.RemoteControlLauncher;
import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.plugins.selenium.process.ProcessArgument;
import hudson.plugins.selenium.process.SeleniumProcessUtils;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

public class SeleniumCallable implements FileCallable<String> {

    private static final String ROLE_PARAM = "-role";

    private static final String ROLE_NODE_VALUE = "node";

    private static final String HOST_PARAM = "-host";

    private static final String HUB_PARAM = "-hub";

    private static final Logger LOGGER = Logger.getLogger(SeleniumCallable.class.getName());

    private FilePath seleniumJar;
    private long jarTimestamp;
    private String nodeName;
    private SeleniumRunOptions options;
    private String config;
    private TaskListener listener;

    private String[] defaultArgs;

    public SeleniumCallable(FilePath jar, String nodehost, String masterName, int masterPort, String nodeName, TaskListener listener,
            String confName, SeleniumRunOptions options) throws InterruptedException, IOException {
        seleniumJar = jar;
        jarTimestamp = jar.lastModified();
        this.nodeName = nodeName;
        this.options = options;
        this.listener = listener;
        config = confName;
        defaultArgs = new String[] { ROLE_PARAM, ROLE_NODE_VALUE, HOST_PARAM, nodehost, HUB_PARAM,
                "http://" + masterName + ":" + masterPort + "/wd/hub" };
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 2047557797415325512L;

    // public final static String ALREADY_STARTED = SeleniumCallable.class.getName() + ".seleniumRcAlreadyStarted";

    public String invoke(File f, VirtualChannel channel) throws IOException {
        RemoteRunningStatus status = (RemoteRunningStatus) PropertyUtils.getMapProperty(SeleniumConstants.PROPERTY_STATUS.displayName, config);

        if (status != null && status.isRunning()) {
            // listener.getLogger().println("Skipping Selenium RC execution because this slave has already started its RCs");
            return null;
        }

        // listener.getLogger().println("Copy grid jar");
        File localJar = new File(f, seleniumJar.getName());
        if (localJar.lastModified() != jarTimestamp) {
            try {
                seleniumJar.copyTo(new FilePath(localJar));
                localJar.setLastModified(jarTimestamp);
            } catch (InterruptedException e) {
                throw new IOException2("Failed to copy grid jar", e);
            }
        }

        try {

            // listener.getLogger().println("Creating selenium VM");
            Channel jvm = SeleniumProcessUtils.createSeleniumRCVM(localJar, listener, options.getJVMArguments(), options.getEnvironmentVariables());
            status = new RemoteRunningStatus(jvm, options);

            List<String> arguments = new ArrayList<String>(options.getSeleniumArguments().size());
            int i = 0;
            for (ProcessArgument arg : options.getSeleniumArguments()) {
                arguments.addAll(arg.toArgumentsList());
            }

            // listener.getLogger().println("Starting the selenium process");
            jvm.callAsync(new RemoteControlLauncher(nodeName, (String[]) ArrayUtils.addAll(defaultArgs, arguments.toArray(new String[0]))));
            status.setStatus(SeleniumConstants.STARTED);
            status.setRunning(true);
        } catch (Exception t) {
            status.setRunning(false);
            status.setStatus(SeleniumConstants.ERROR);
            LOGGER.log(Level.WARNING, "Selenium launch failed", t);
            // listener.getLogger().println( "Selenium launch failed" + t.getMessage());

            throw new IOException2("Selenium launch interrupted", t);
        }
        PropertyUtils.setMapProperty(SeleniumConstants.PROPERTY_STATUS.displayName, config, status);

        // System.setProperty(alreadyStartedPropertyName, Boolean.TRUE.toString());
        return null;
    }
}