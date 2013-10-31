package netlab.hub.plugins.arduino;

public class ArduinoFactory {
	
	public ArduinoFactory() {
		super();
	}
	
	public Arduino newArduinoInstance(String port, int baud) {
		return new Arduino(port, baud);
	}

}
