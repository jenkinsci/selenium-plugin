package hudson.plugins.selenium;

import hudson.remoting.Callable;

class HubParamsCallable implements Callable<HubParams, Throwable> {


	public HubParams call() throws Throwable {
		if (HubHolder.hub == null) {
			return new HubParams();
		}
		return new HubParams(HubHolder.hub.getHost(), HubHolder.hub.getPort(), true);
	}
}
