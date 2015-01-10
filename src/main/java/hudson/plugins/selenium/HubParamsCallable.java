package hudson.plugins.selenium;

import hudson.remoting.Callable;

class HubParamsCallable implements Callable<HubParams, Throwable> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4136171068376050199L;

	public HubParams call() throws Throwable {
		if (HubHolder.hub == null) {
			return new HubParams();
		}
		return new HubParams(HubHolder.hub.getHost(), HubHolder.hub.getPort(), true);
	}
}
