package hudson.plugins.selenium.actions;

import hudson.model.Action;
import hudson.model.Computer;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.SeleniumRunOptions;
import hudson.plugins.selenium.callables.CloseSeleniumChannelCallable;
import hudson.plugins.selenium.callables.DeepLevelCallable;
import hudson.plugins.selenium.callables.GetConfigurations;
import hudson.plugins.selenium.callables.RemoteStopSelenium;
import hudson.plugins.selenium.callables.RunningRemoteSetterCallable;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.plugins.selenium.callables.SetRemoteRunningCallable;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.remoting.VirtualChannel;
import hudson.util.StreamTaskListener;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;

public class ServiceManagementAction implements Action {

	private Computer computer;
	
	public ServiceManagementAction(Computer c) {
		computer = c;
	}
	
    public String getIconFileName() {
    	return "/plugin/selenium/24x24/selenium.png";
    }

    public String getDisplayName() {
        return "Selenium node Management";
    }

    public String getUrlName() {
        return "selenium";
    }
    
	public HttpResponse doRestart(@QueryParameter String conf) throws IOException, ServletException {
		doStop(conf);
		doStart(conf);
        return HttpResponses.forwardToPreviousPage();
    }
	
	public HttpResponse doStop(@QueryParameter String conf) throws IOException, ServletException {
		VirtualChannel slaveChannel = PluginImpl.getChannel(computer);
		if (slaveChannel != null) {
			try {
				slaveChannel.call(new RunningRemoteSetterCallable(conf, SeleniumConstants.STOPPING));
				slaveChannel.call(new DeepLevelCallable<String>(conf, new RemoteStopSelenium(conf)));
				slaveChannel.call(new RunningRemoteSetterCallable(conf, SeleniumConstants.STOPPED));
				slaveChannel.call(new SetRemoteRunningCallable(conf, false));
                slaveChannel.call(new CloseSeleniumChannelCallable(conf));
			} catch (Exception e) {
				e.printStackTrace();
				try {
					slaveChannel.call(new RunningRemoteSetterCallable(conf, SeleniumConstants.ERROR));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		return HttpResponses.forwardToPreviousPage();
	}
	
	public HttpResponse doStart(@QueryParameter String conf) throws IOException, ServletException {
		try {
			PluginImpl.startSeleniumNode(computer, new StreamTaskListener(new OutputStreamWriter(System.out)), conf);
		} catch (Exception e) {
            e.printStackTrace();
		}
		return HttpResponses.forwardToPreviousPage();
	}
	
	public Computer getComputer() {
		return computer;
	}

    public Map<String, SeleniumRunOptions> getConfigurations() {
    	try {
			return computer.getNode().getRootPath().getChannel().call(new GetConfigurations());
		} catch (Exception e) {
			return Collections.emptyMap();
		}
    }

    public List<SeleniumGlobalConfiguration> getMatchingConfigurations() {
        return PluginImpl.getPlugin().getGlobalConfigurationForComputer(computer);
    }

}
