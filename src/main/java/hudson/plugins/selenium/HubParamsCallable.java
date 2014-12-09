package hudson.plugins.selenium;

import hudson.remoting.Callable;

class HubParamsCallable implements Callable<HubParams, Throwable> {


	public HubParams call() throws Throwable {
		HubParams hubParams = new HubParams();
		if (HubHolder.hub == null) {
			return hubParams;
		}
		hubParams.isActive = true;
		hubParams.host = HubHolder.hub.getHost();
		hubParams.port = HubHolder.hub.getPort();
		return hubParams;
	}
}
