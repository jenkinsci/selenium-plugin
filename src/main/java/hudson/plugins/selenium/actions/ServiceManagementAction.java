package hudson.plugins.selenium.actions;

import hudson.FilePath.FileCallable;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.plugins.selenium.NodePropertyImpl;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

public class ServiceManagementAction implements Action {

	private Computer computer;
	
	public ServiceManagementAction(Computer c) {
		computer = c;
	}
	
    public String getIconFileName() {
//    	if (NodePropertyImpl.getNodeProperty(computer) != null)
//    		return "/plugin/selenium/24x24/selenium.png";
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
        return HttpResponses.redirectViaContextPath("../..");
    }
	
	public HttpResponse doStop() throws IOException, ServletException {
		NodePropertyImpl np = NodePropertyImpl.getNodeProperty(computer);
		if (np != null) {
			try {
				Boolean response = np.getChannel().call(new Callable<Boolean,Exception>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 3692858218403086907L;

					public Boolean call() throws Exception {
						
						// TODO Auto-generated method stub
						return null;
					}
				});
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return HttpResponses.redirectViaContextPath("../..");
	}
	
	public HttpResponse doStart() throws IOException, ServletException {
		return HttpResponses.redirectViaContextPath("../..");
	}
}
