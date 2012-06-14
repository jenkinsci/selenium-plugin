package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;

public class SetSysPropertyCallable implements Callable<Void,Exception> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3519905249359789575L;
	
	
	private String prop;
	private String val;

	public SetSysPropertyCallable(String property, String value) {
		prop = property;
		val = value;
	}

	public Void call() throws Exception {
		System.setProperty(prop, val);
		return null;
	}

	
	
}
