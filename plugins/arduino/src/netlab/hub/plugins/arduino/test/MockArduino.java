package netlab.hub.plugins.arduino.test;

import netlab.hub.plugins.arduino.Arduino;
import netlab.hub.serial.SerialException;

public class MockArduino extends Arduino {
	
	public int[] digitalPins;
	public int[] analogPins;

	public MockArduino(String portName) throws SerialException {
		super(portName);
		setDefaultPinCapabilities();
		capabilitiesResponseCallback();
	}
	
	public MockArduino(String portName, int rate) throws SerialException {
		super(portName, rate);
		setDefaultPinCapabilities();
		capabilitiesResponseCallback();
	}
	
	public void digitalWrite(int pin, int value) {
		super.digitalWrite(pin, value);
		if (digitalPins != null) digitalPins[pin] = value;
	}
	
	public void analogWrite(int pin, int value) {
		super.analogWrite(pin, value);
		if (analogPins != null) analogPins[pin] = value;
	}

}
