package hudson.plugins.selenium;

import java.io.Serializable;

public class HubParams implements Serializable {
	private static final long serialVersionUID = 3813168118997469056L;


	public String getHost() {
		return host;
	}

	public String host ="";
	public Integer port = -1;
	public Boolean isActive = false;
}
