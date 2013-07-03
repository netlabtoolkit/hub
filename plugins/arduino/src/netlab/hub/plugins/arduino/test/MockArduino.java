package netlab.hub.plugins.arduino.test;

import netlab.hub.plugins.arduino.Arduino;
import netlab.hub.serial.SerialException;

public class MockArduino extends Arduino {
	
	int[] writeBuffer;
	int bytesWritten = 0;

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
	
	public int[] getBytesWritten() {
		int[] bytes = new int[bytesWritten];
		for (int i=0; i<bytesWritten; i++) {
			bytes[i] = writeBuffer[i];
		}
		return bytes;
	}
	
	public void clearWriteBuffer() {
		writeBuffer = new int[1024];
		bytesWritten = 0;
	}

	@Override
	public int serialRead() {
		return 0;
	}

	@Override
	public void serialWrite(int value) {
		if (writeBuffer == null)
			clearWriteBuffer();
		writeBuffer[bytesWritten++] = value;
	}

}
