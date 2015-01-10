package hudson.plugins.selenium;

import java.io.Serializable;

public class HubParams implements Serializable {
	private static final long serialVersionUID = 3813168118997469056L;

	private String host ="";
	private Integer port = -1;
	private boolean isActive = false;

	public HubParams() {
		this("", -1, false);
	}

	public HubParams(String host, Integer port, boolean isActive) {
		this.host = host;
		this.port = port;
		this.isActive = isActive;
	}
	
	public boolean isNotActiveOn(String masterHostName, int masterPort) {
		return isActive && !(port == masterPort && host.equalsIgnoreCase(masterHostName));
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

}
