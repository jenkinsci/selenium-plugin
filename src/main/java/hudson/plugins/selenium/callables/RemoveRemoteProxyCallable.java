package hudson.plugins.selenium.callables;

import hudson.plugins.selenium.RegistryHolder;
import hudson.remoting.Callable;

import org.openqa.grid.internal.RemoteProxy;

public class RemoveRemoteProxyCallable implements Callable<String, Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1179436797545442003L;

	private String remoteURL;
	
	public RemoveRemoteProxyCallable(String url) {
		remoteURL = url;
	}
	
	public String call() throws Exception {
        String px = "";
        String pxs = "";

		for (RemoteProxy proxy : RegistryHolder.registry.getAllProxies()) {
			if (remoteURL.equals(proxy.getRemoteHost().toString())) {
				RegistryHolder.registry.removeIfPresent(proxy);
				px = px + proxy.toString() + " ";
                
			}
			pxs = pxs + proxy.toString() + " ";
		}
pxs = pxs + " ---------- ";
		for (RemoteProxy proxy : RegistryHolder.registry.getAllProxies()) {
			pxs = pxs + proxy.toString() + " ";
		}

		
        return px + " !!! " + pxs;
	}	
}
