package netlab.hub.core;

@SuppressWarnings("serial")
public class ConfigurationException extends Exception {
	
	public ConfigurationException(String msg) {
		super(msg);
	}
	
	public ConfigurationException(Throwable cause) {
		super(cause);
	}
	
	public ConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
