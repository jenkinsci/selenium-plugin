package hudson.plugins.selenium.actions;

import hudson.model.Action;
import hudson.model.Computer;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.callables.ChannelPropertyGetterCallable;
import hudson.plugins.selenium.callables.ChannelPropertySetterCallable;
import hudson.plugins.selenium.callables.DeepLevelCallable;
import hudson.plugins.selenium.callables.RemoteGetStatus;
import hudson.plugins.selenium.callables.RemoteStopSelenium;
import hudson.plugins.selenium.callables.SeleniumCallable;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.plugins.selenium.callables.SetSysPropertyCallable;
import hudson.plugins.selenium.configuration.global.SeleniumGlobalConfiguration;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.util.StreamTaskListener;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

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
    
	public HttpResponse doRestart() throws IOException, ServletException {
		doStop();
		doStart();
        return HttpResponses.forwardToPreviousPage();
    }
	
	public HttpResponse doStop() throws IOException, ServletException {
		//NodePropertyImpl np = NodePropertyImpl.getNodeProperty(computer);
		VirtualChannel slaveChannel = PluginImpl.getChannel(computer);
		if (slaveChannel != null) {
			try {
				slaveChannel.call(new ChannelPropertySetterCallable<String>(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STOPPING));
				slaveChannel.call(new DeepLevelCallable<Void, Exception>(SeleniumConstants.PROPERTY_JVM, new RemoteStopSelenium()));
				slaveChannel.call(new ChannelPropertySetterCallable<String>(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STOPPED));
				Channel c = slaveChannel.call(new ChannelPropertyGetterCallable<Channel, Exception>(SeleniumConstants.PROPERTY_JVM));
				slaveChannel.call(new SetSysPropertyCallable(SeleniumCallable.ALREADY_STARTED, Boolean.FALSE.toString()));
				c.close();
				//np.setChannel(null);
			} catch (Exception e) {
				try {
					slaveChannel.call(new ChannelPropertySetterCallable<String>(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.ERROR));
				} catch (Exception e1) {
				}
			}
		}
		return HttpResponses.forwardToPreviousPage();
	}
	
	public HttpResponse doStart() throws IOException, ServletException {
		if (PluginImpl.getChannel(computer) != null) {
			try {
				PluginImpl.startSeleniumNode(computer, new StreamTaskListener(new OutputStreamWriter(System.out)));
			} catch (Exception e) {
			}
		}
		return HttpResponses.forwardToPreviousPage();
	}
	
	public Computer getComputer() {
		return computer;
	}
	
	public List<SeleniumGlobalConfiguration> getConfigurations() {
		List<SeleniumGlobalConfiguration> confs = new ArrayList<SeleniumGlobalConfiguration>();
		for (SeleniumGlobalConfiguration c : PluginImpl.getPlugin().getGlobalConfigurations()) {
			if (c.getOptions(computer) != null) {
				confs.add(c);
			}
		}
		return confs;
	}
	
	public String getStatus() {
		try {
			VirtualChannel channel = PluginImpl.getChannel(computer);
			if (channel == null) return SeleniumConstants.STOPPED;
			return channel.call(new RemoteGetStatus());
		} catch (Throwable e) {
			return "An error occured while retrieving the status of the selenium process " + e.getMessage();
		}
	}

}
