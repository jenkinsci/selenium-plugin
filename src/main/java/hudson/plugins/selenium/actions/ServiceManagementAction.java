package hudson.plugins.selenium.actions;

import hudson.model.Action;
import hudson.model.Computer;
import hudson.plugins.selenium.NodePropertyImpl;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.callables.ChannelPropertyGetterCallable;
import hudson.plugins.selenium.callables.ChannelPropertySetterCallable;
import hudson.plugins.selenium.callables.DeepLevelCallable;
import hudson.plugins.selenium.callables.RemoteStopSelenium;
import hudson.plugins.selenium.callables.SeleniumCallable;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.plugins.selenium.callables.SetSysPropertyCallable;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.util.StreamTaskListener;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

public class ServiceManagementAction implements Action {

	private Computer computer;
	
	public ServiceManagementAction(Computer c) {
		computer = c;
	}
	
    public String getIconFileName() {
    	if (NodePropertyImpl.getNodeProperty(computer) != null)
    		return "/plugin/selenium/24x24/selenium.png";
    	return null;
    }

    public String getDisplayName() {
        return "Selenium Grid Management";
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
		NodePropertyImpl np = NodePropertyImpl.getNodeProperty(computer);
		if (np != null) {
			try {
				VirtualChannel slaveChannel = np.getChannel(); 
				slaveChannel.call(new ChannelPropertySetterCallable<String>(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STOPPING));
				slaveChannel.call(new DeepLevelCallable<Void, Exception>(SeleniumConstants.PROPERTY_JVM, new RemoteStopSelenium()));
				slaveChannel.call(new ChannelPropertySetterCallable<String>(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.STOPPED));
				Channel c = slaveChannel.call(new ChannelPropertyGetterCallable<Channel, Exception>(SeleniumConstants.PROPERTY_JVM));
				slaveChannel.call(new SetSysPropertyCallable(SeleniumCallable.ALREADY_STARTED, Boolean.FALSE.toString()));
				c.close();
				//np.setChannel(null);
			} catch (Exception e) {
				try {
					np.getChannel().call(new ChannelPropertySetterCallable<String>(SeleniumConstants.PROPERTY_STATUS, SeleniumConstants.ERROR));
				} catch (Exception e1) {
				}
			}
		}
		return HttpResponses.forwardToPreviousPage();
	}
	
	public HttpResponse doStart() throws IOException, ServletException {
		NodePropertyImpl np = NodePropertyImpl.getNodeProperty(computer);
		if (np != null) {
			try {
				PluginImpl.startSeleniumNode(computer, new StreamTaskListener(new OutputStreamWriter(System.out)));
			} catch (Exception e) {
			}
		}
		return HttpResponses.forwardToPreviousPage();
	}
	
	public NodePropertyImpl getProperty() {
		return NodePropertyImpl.getNodeProperty(computer);
	}
	
	public Computer getComputer() {
		return computer;
	}
}
