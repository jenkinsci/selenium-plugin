package hudson.plugins.selenium;

import jenkins.security.MasterToSlaveCallable;

class HubParamsCallable extends MasterToSlaveCallable<HubParams, Throwable> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4136171068376050199L;

	public HubParams call() throws Throwable {
		if (HubHolder.getHub() == null) {
			return new HubParams();
		}
		return new HubParams(HubHolder.getHub().getUrl(), true);
	}
}
