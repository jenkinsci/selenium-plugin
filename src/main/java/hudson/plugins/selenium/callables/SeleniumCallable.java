package hudson.plugins.selenium.callables;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.selenium.RemoteControlLauncher;
import hudson.plugins.selenium.RemoteRunningStatus;
import hudson.plugins.selenium.process.ProcessArgument;
import hudson.plugins.selenium.process.SeleniumProcessUtils;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeleniumCallable extends MasterToSlaveFileCallable<String> {

    /**
     *
     */
    private static final long serialVersionUID = 2047557797415325512L;

    private static final String ROLE_PARAM = "-role";

    private static final String ROLE_NODE_VALUE = "node";

    private static final String HUB_PARAM = "-hub";

    private static final Logger LOGGER = Logger.getLogger(SeleniumCallable.class.getName());

    private FilePath seleniumJar;
    private FilePath htmlUnitDriverJar;
    private long seleniumJarTimestamp;
    private long htmlUnitDriverJarTimestamp;
    private String nodeName;
    private SeleniumRunOptions options;
    private String config;

    private TaskListener listener;

    private String[] defaultArgs;

    public SeleniumCallable(FilePath seleniumJar, FilePath htmlUnitDriverJar, String nodehost, String masterName, int masterPort, String nodeName, TaskListener listener,
            String confName, SeleniumRunOptions options) throws InterruptedException, IOException {
        this.seleniumJar = seleniumJar;
        seleniumJarTimestamp = seleniumJar.lastModified();
        this.htmlUnitDriverJar = htmlUnitDriverJar;
        htmlUnitDriverJarTimestamp = htmlUnitDriverJar.lastModified();
        this.nodeName = nodeName;
        this.options = options;
        this.listener = listener;
        config = confName;
        defaultArgs = new String[] {
                ROLE_PARAM, ROLE_NODE_VALUE,
                HUB_PARAM, "http://" + masterName + ":" + masterPort + "/wd/hub" };
    }

    public String invoke(File f, VirtualChannel channel) throws IOException {
        RemoteRunningStatus status = PropertyUtils.getMapProperty(SeleniumConstants.PROPERTY_STATUS, config);

        if (status != null && status.isRunning()) {
            return null;
        }

        File localSeleniumJar = new File(f, seleniumJar.getName());
        File localHtmlUnitDriverJar = new File(f, htmlUnitDriverJar.getName());
        if (localSeleniumJar.lastModified() != seleniumJarTimestamp) {
            try {
                seleniumJar.copyTo(new FilePath(localSeleniumJar));
                localSeleniumJar.setLastModified(seleniumJarTimestamp);
            } catch (InterruptedException e) {
                throw new IOException("Failed to copy grid jar", e);
            }
        }
        if (localHtmlUnitDriverJar.lastModified() != htmlUnitDriverJarTimestamp) {
            try {
                htmlUnitDriverJar.copyTo(new FilePath(localHtmlUnitDriverJar));
                localHtmlUnitDriverJar.setLastModified(htmlUnitDriverJarTimestamp);
            } catch (InterruptedException e) {
                throw new IOException("Failed to copy htmlunit driver jar", e);
            }
        }

        try {

            listener.getLogger().println("Creating selenium node VM");
            Channel jvm = SeleniumProcessUtils.createSeleniumRCVM(localSeleniumJar, localHtmlUnitDriverJar, listener, options.getJVMArguments(), options.getEnvironmentVariables());
            status = new RemoteRunningStatus(jvm, options);
            status.setStatus(SeleniumConstants.STARTING);

            List<String> arguments = new ArrayList<String>(options.getSeleniumArguments().size());
            for (ProcessArgument arg : options.getSeleniumArguments()) {
                arguments.addAll(arg.toArgumentsList());
            }

            Object[] allArgs = ArrayUtils.addAll(defaultArgs, arguments.toArray(new String[arguments.size()]));

            listener.getLogger().println("Starting the selenium node process. Args: " + Arrays.toString(allArgs));
            jvm.callAsync(new RemoteControlLauncher(nodeName, (String[])allArgs));
            status.setStatus(SeleniumConstants.STARTED);
            status.setRunning(true);
        } catch (Exception t) {
            if(status == null){
                status = new RemoteRunningStatus(null, options);
            }
            status.setRunning(false);
            status.setStatus(SeleniumConstants.ERROR);
            LOGGER.log(Level.WARNING, "Selenium node launch failed", t);
            listener.getLogger().println( "Selenium node launch failed" + t.getMessage());

            throw new IOException("Selenium node launch interrupted", t);
        }
        PropertyUtils.setMapProperty(SeleniumConstants.PROPERTY_STATUS, config, status);

        return config;
    }
}
