package hudson.plugins.selenium.actions;

import hudson.model.Action;
import hudson.model.Computer;
import hudson.plugins.selenium.NodePropertyImpl;
import hudson.plugins.selenium.PluginImpl;
import hudson.plugins.selenium.callables.DeepLevelCallable;
import hudson.plugins.selenium.callables.PropertyUtils;
import hudson.plugins.selenium.callables.RemoteStopSelenium;
import hudson.plugins.selenium.callables.SeleniumCallable;
import hudson.plugins.selenium.callables.SeleniumConstants;
import hudson.remoting.Channel;
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
				np.getChannel().call(new DeepLevelCallable<Void, Exception>(SeleniumConstants.PROPERTY_JVM, new RemoteStopSelenium()));
				System.setProperty(SeleniumCallable.ALREADY_STARTED, Boolean.FALSE.toString());
				Channel c = PropertyUtils.getProperty(SeleniumConstants.PROPERTY_JVM);
				c.close();
				//np.setChannel(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return HttpResponses.forwardToPreviousPage();
	}
	
	public HttpResponse doStart() throws IOException, ServletException {
		try {
			//np.getChannel().call(new DeepLevelCallable<Void, Exception>(SeleniumConstants.PROPERTY_JVM, new RemoteStartSelenium()));
			PluginImpl.startSeleniumNode(computer, new StreamTaskListener(new OutputStreamWriter(System.out)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		NodePropertyImpl np = NodePropertyImpl.getNodeProperty(computer);
//		if (np != null) {
//		}
		return HttpResponses.forwardToPreviousPage();
	}
	
	public NodePropertyImpl getProperty() {
		return NodePropertyImpl.getNodeProperty(computer);
	}
	
	public Computer getComputer() {
		return computer;
	}
}
