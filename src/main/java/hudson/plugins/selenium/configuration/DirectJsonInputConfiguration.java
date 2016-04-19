/**
 *
 */
package hudson.plugins.selenium.configuration;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.plugins.selenium.process.SeleniumRunOptions;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import hudson.util.IOException2;
import jenkins.MasterToSlaveFileCallable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Richard Lavoie
 *
 */
public class DirectJsonInputConfiguration extends SeleniumNodeConfiguration {

    /**
     *
     */
    private static final long serialVersionUID = 4521592447812296365L;

    /**
     * JSON formatted configuration
     */
    private String config;

    /**
     * JVM arguments.
     */
    private String jvmArgs;

    private String seleniumArgs;

    /**
     */
    @DataBoundConstructor
    public DirectJsonInputConfiguration(String display, String config, String jvmArgs, String seleniumArgs) {
        super(display);
        if (config.startsWith("\"")) {
            this.config = config.substring(1, config.length() - 1);
        } else {
            this.config = config;
        }
        this.jvmArgs = jvmArgs;
        this.seleniumArgs = seleniumArgs;
    }

    @Exported
    public String getConfig() {
        return config;
    }

    @Exported
    public String getJvmArgs() {
        return jvmArgs;
    }

    @Exported
    public String getSeleniumArgs() {
        return seleniumArgs;
    }

    @Extension
    public static class DescriptorImpl extends ConfigurationDescriptor {

        @Override
        public String getDisplayName() {
            return "JSON configuration";
        }

        public FormValidation doCheckConfig(@QueryParameter( "config" ) String config) {
            FormValidation notEmpty = FormValidation.validateRequired(config);
            if (!notEmpty.equals(FormValidation.ok())) {
                return notEmpty;
            }
            try {
                new JSONObject(config);
                // We don't want to validate the fields, even RegistrationRequest doesn't ... It just ignores the unknown fields.
                return FormValidation.ok();
            } catch (Exception e) {
                return FormValidation.error("Invalid JSON input.");
            }
        }
    }

    @Override
    public SeleniumRunOptions initOptions(Computer computer) {
        SeleniumRunOptions opt = super.initOptions(computer);
        try {

            final String filename = "selenium-temp-config-" + System.currentTimeMillis() + ".json";

            if (jvmArgs != null) {
                Properties p = new Properties();
                p.load(new StringReader(jvmArgs.replace("\\", "\\\\")));
                for (Entry<Object, Object> e : p.entrySet()) {
                    opt.getJVMArguments().put(e.getKey().toString(), e.getValue().toString());
                }
            }

            if (seleniumArgs != null) {
                for (Object l : IOUtils.readLines(new StringReader(seleniumArgs))) {
                    String line = (String) l;
                    line = line.trim();
                    if (line.contains(" ")) {
                        String[] keyValue = StringUtils.split(line, " ", 2);
                        opt.addOptionIfSet(keyValue[0], keyValue[1]);
                    } else {
                        opt.addOption(line);
                    }
                }
            }

            String fullPath = computer.getNode().getRootPath().act(new MasterToSlaveFileCallable<String>() {

                /**
                 *
                 */
                private static final long serialVersionUID = -288688398601004624L;

                public String invoke(File f, VirtualChannel channel) throws IOException {
                    File conf = new File(f, filename);

                    FilePath urlConf = new FilePath(conf);
                    try {
                        urlConf.copyFrom(new ByteArrayInputStream(config.getBytes()));
                    } catch (InterruptedException e) {
                        throw new IOException2("Failed to write configuration to " + filename, e);
                    }

                    return conf.getAbsolutePath();
                }
            });

            opt.addOptionIfSet("-nodeConfig", fullPath);

            return opt;
        } catch (Exception e) {
            LOGGER.fine("Cannot write the specified configuration on the node. " + e.getMessage());
            return null;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(FileConfiguration.class.getName());

    public String getIcon() {
        return "/images/24x24/document.png";
    }

}
