package hudson.plugins.selenium.configuration;

import hudson.Extension;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.springframework.util.StringUtils;

public class FileConfiguration extends Configuration {

    private boolean rcBrowserSideLog;
    private boolean rcDebug;
    private boolean rcTrustAllSSLCerts;
    private boolean rcBrowserSessionReuse;
    private String rcFirefoxProfileTemplate;
    private String rcLog;

    @Exported
    public String getRcLog(){
        return rcLog;
    }

    @Exported
    public boolean getRcBrowserSideLog(){
        return rcBrowserSideLog;
    }

    @Exported
    public boolean getRcDebug(){
        return rcDebug;
    }

    @Exported
    public boolean getRcTrustAllSSLCerts() {
        return rcTrustAllSSLCerts;
    }
    
    @Exported
    public boolean getRcBrowserSessionReuse() {
    	return rcBrowserSessionReuse;
    }

    @Exported
    public String getRcFirefoxProfileTemplate(){
        return rcFirefoxProfileTemplate;
    }

	@Override
	public List<String> getLaunchingArguments() {
        List<String> args = new ArrayList<String>();
        if (StringUtils.hasText(getRcLog())){
            args.add("-log");
            args.add(getRcLog());
        }
        if (getRcBrowserSideLog()){
        	args.add("-browserSideLog");
        }
        if (getRcDebug()){
        	args.add("-debug");
        }
        if (getRcTrustAllSSLCerts()){
        	args.add("-trustAllSSLCertificates");
        }
        if (getRcBrowserSessionReuse()) {
        	args.add("-browserSessionReuse");
        }
        if (StringUtils.hasText(getRcFirefoxProfileTemplate())){
        	args.add("-firefoxProfileTemplate");
        	args.add(getRcFirefoxProfileTemplate());
        }
        return args;
	}

	@Extension
	public static class DescriptorImpl extends ConfigurationDescriptor {

		@Override
		public String getDisplayName() {
			return "File configuration";
		}
	}
	
}
