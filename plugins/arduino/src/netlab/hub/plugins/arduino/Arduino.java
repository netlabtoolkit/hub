/*
Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org

Copyright (c) 2006-2013 Ewan Branda

NETLab Hub is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

NETLab Hub is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
*/

package netlab.hub.plugins.arduino;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import netlab.hub.serial.SerialPort;
import processing.core.PApplet;
import processing.serial.Serial;

/**
 * This class communicates with an Arduino running the Firmata firmware.
 * It is based on the original Arduino Processing library class written by
 * David Mellis. See http://playground.arduino.cc/interfacing/processing.
 * It has been updated to support servos over StandardFirmata, sysex messages,
 * capability reporting, and pin validation.
 * <p />
 * This base class provided all of core Arduino functionality but leaves 
 * the actual serial port communications to subclasses.
 * 
 */
public class Arduino {
	
	// Constants for setting pin modes
	public static final int INPUT = 0;
	public static final int OUTPUT = 1;
	public static final int ANALOG = 2;
	public static final int PWM = 3;
	public static final int SERVO = 4;
	public static final int SHIFT = 5;
	public static final int I2C = 6;

	// Pin high/low (true/false) values for digitalWrite()
	public static final int LOW = 0;
	public static final int HIGH = 1;

	// Size of input data buffer
	public static final int MAX_DATA_BYTES = 1024;
	
	public static final int DIGITAL_MESSAGE        = 0x90; // send data for a digital port
	public static final int ANALOG_MESSAGE         = 0xE0; // send data for an analog pin (or PWM)
	public static final int REPORT_ANALOG          = 0xC0; // enable analog input by pin #
	public static final int REPORT_DIGITAL         = 0xD0; // enable digital input by port
	public static final int SET_PIN_MODE           = 0xF4; // set a pin to INPUT/OUTPUT/PWM/etc
	public static final int REPORT_VERSION         = 0xF9; // report firmware version
	//public static final int SYSTEM_RESET         = 0xFF; // reset from MIDI
	public static final int START_SYSEX            = 0xF0; // start a MIDI SysEx message
	public static final int END_SYSEX              = 0xF7; // end a MIDI SysEx message
	public static final int CAPABILITY_QUERY       = 0x6B; // request board capability report
	public static final int CAPABILITY_RESPONSE    = 0x6C; // response with capability report
	public static final int PIN_STATE_QUERY		   = 0x6D; // request pin mode and state
	public static final int PIN_STATE_RESPONSE	   = 0x6E; // response with pin mode and state
	public static final int	ANALOG_MAPPING_QUERY   = 0x69;
	public static final int	ANALOG_MAPPING_RESPONSE	= 0x6A;
	public static final int REPORT_FIRMWARE		   = 0x79;
	public static final int SERVO_CONFIG 		   = 0x70;

	int waitForData = 0;
	int executeMultiByteCommand = 0;
	int multiByteChannel = 0;
	int[] storedInputData = new int[MAX_DATA_BYTES];
	boolean parsingSysex;
	int sysexBytesRead;
	int majorVersion = 0;
	int minorVersion = 0;
	
	// The buffers of the latest pin data. 
	int[] digitalInputData;
	int[] analogInputData;
	int[] digitalOutputData;
		
	List<PinCapabilities> capabilities = new ArrayList<PinCapabilities>();
	int analogPinCount = 0;
	int digitalPinCount = 0;
	boolean firmwareReported = false;
	boolean initialized = false;
	
	Serial serial;
	String portName;
	
	@SuppressWarnings("serial")
	public class SerialEventHandler extends PApplet {
	    public void serialEvent(Serial which) {
	      // Notify the Arduino class that there's serial data for it to process.
	      while (available() > 0)
	        processInput();
	    }
	  }
	
	/**
	 * Create a proxy to an Arduino board running the Firmata 2 firmware.
	 *
	 * @param the serial port to which the Arduino is connected
	 */
	public Arduino() {
		super();
	}
	
	public Arduino(String portName) {
		this(portName, 57600);
	}
	
	public Arduino(String portName, int rate) {
		this.serial = new Serial(new SerialEventHandler(), portName, rate);
		this.portName = portName;
		begin();
	}
	
	// To ensure backward compatibility with uses of cc.arduino class
	public Arduino(PApplet parent, String portName, int rate) {
		this(portName, rate);
	}
	
	// To ensure backward compatibility with uses of cc.arduino class
	public Arduino(PApplet parent, String portName) {
		this(portName);
	}
	
	/**
	   * Get a list of the available Arduino boards; currently all serial devices
	   * (i.e. the same as Serial.list()).  In theory, this should figure out
	   * what's an Arduino board and what's not.
	   */
	  public static String[] list() {
	    return Serial.list();
	  }
	
	public int available() {
	    return serial.available();
	}
	
	public String getPortName() {
		return portName;
	}
	
	public boolean initialized() {
		return initialized;
	}
	
	public int serialRead() {
		return serial.read();
	}

	public void serialWrite(int value) {
		serial.write(value);
	}

	public void serialEvent(SerialPort port) {
		while (serial.available() > 0)
			processInput();
	}
	
	public void dispose() {
		serial.dispose();
	}
	
	/**
	 * 
	 */
	public void begin() {
		// Required to start the session. No firmware or capabilities reporting works until this is sent. Why????
		for (int pin=0; pin<1; pin++) {
			serialWrite(REPORT_ANALOG | pin);
			serialWrite(1);
		}
		// Wait for Firmata to report. If it doesn't, manually initialize
		// the board using default board capabilities. This will ensure 
		// that custom Arduino firmware that uses the Firmata library 
		// will still be able to use the board. If we were guaranteed
		// that all users were using StandardFirmata firmware we would
		// not need to do any of this.
		new Thread(new Runnable() {
			long start = System.currentTimeMillis();
			public void run() {
				while (!initialized) {
					delay(100);
					if ((System.currentTimeMillis() - start) > 5000) {
						setDefaultPinCapabilities();
						capabilitiesResponseCallback();
						break;
					}
				}
			}
		}).start();
	}
	
	public void reportFirmwareCallback() {
		if (!firmwareReported) {
			firmwareReported = true;
			queryCapabilities();
		}
	}
	
	/**
	 * Initialize the pins and local data buffers based on
	 * the currently known set of pin capabilities.
	 */
	public void capabilitiesResponseCallback() {
		for (int pin=0; pin<analogPinCount; pin++) {
			serialWrite(REPORT_ANALOG | pin);
			serialWrite(1);
		}
		int digitalPorts = (int)Math.ceil(digitalPinCount/8.0); // Find the number of 8-bit port registers used for the digital pins
		for (int port=0; port<digitalPorts; port++) {
			serialWrite(REPORT_DIGITAL | port);
			serialWrite(1);
		}
		digitalOutputData = new int[digitalPinCount];
		digitalInputData  = new int[digitalPinCount];
		analogInputData   = new int[analogPinCount];
		initialized = true;
	}
	
	/**
	 * Configures the pin for use as a servo. See 
	 * http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1274073450
	 * @param pin
	 * @param minPulse
	 * @param maxPulse
	 * @param angle
	 */
	public void servoConfig(int pin, int minPulse, int maxPulse, int angle) {
        serialWrite(START_SYSEX);
        serialWrite(SERVO_CONFIG);
        serialWrite(pin & 0x7F);
        serialWrite(minPulse & 0x7F);
        serialWrite(minPulse >> 7);
        serialWrite(maxPulse & 0x7F);
        serialWrite(maxPulse >> 7);
        serialWrite(angle & 0x7F);
        serialWrite(angle >> 7);
        serialWrite(END_SYSEX);
		pinMode(pin, SERVO);
	}
		
	/**
	 * Returns the last known value read from the digital pin: HIGH or LOW.
	 *
	 * @param pin the digital pin whose value should be returned
	 */
	public int digitalRead(int pinNumber) {
		if (!initialized) {
			return 0;
		}
		if (0 > pinNumber || pinNumber >= digitalInputData.length) {
			return 0;
		}
		return (digitalInputData[pinNumber >> 3] >> (pinNumber & 0x07)) & 0x01;
	}

	/**
	 * Returns the last known value read from the analog pin: 0 (0 volts) to
	 * 1023 (5 volts).
	 *
	 * @param pin the analog pin whose value should be returned
	 */
	public int analogRead(int pinNumber) {
		if (!initialized) {
			return 0;
		} 
		if (0 > pinNumber || pinNumber >= analogInputData.length) {
			return 0;
		}
		return analogInputData[pinNumber];
	}

	/**
	 * Set a digital pin to input or output mode.
	 *
	 * @param pin the pin whose mode to set
	 * @param mode
	 */
	public void pinMode(int pin, int mode) {
		serialWrite(SET_PIN_MODE);
		serialWrite(pin);
		serialWrite(mode);
	}

	/**
	 * Write to a digital pin (the pin must have been put into output mode with
	 * pinMode()).
	 *
	 * @param pin the pin to write to
	 * @param value the value to write: Arduino.LOW (0 volts) or Arduino.HIGH
	 * (5 volts)
	 */
	public void digitalWrite(int pin, int value) {
		if (!initialized) {
			return;
		}
		int portNumber = (pin >> 3) & 0x0F; // Need to understand this pin mapping
		if (value == 0)
			digitalOutputData[portNumber] &= ~(1 << (pin & 0x07));
		else
			digitalOutputData[portNumber] |= (1 << (pin & 0x07));

		serialWrite(DIGITAL_MESSAGE | portNumber);
		serialWrite(digitalOutputData[portNumber] & 0x7F);
		serialWrite(digitalOutputData[portNumber] >> 7);
	}

	/**
	 * Write an analog value (PWM-wave) to a digital pin.
	 *
	 * @param pin the pin to write to
	 * @param the value: 0 being the lowest (always off), and 255 the highest
	 * (always on)
	 */
	public void analogWrite(int pin, int value) {
		if (!initialized) {
			return; // Report error?
		}
		pinMode(pin, PWM);
		serialWrite(ANALOG_MESSAGE | (pin & 0x0F));
		serialWrite(value & 0x7F);
		serialWrite(value >> 7);
	}
	
	/**
	 * @param pin
	 * @param angle
	 */
	public void setServoAngle(int pin, int angle) {
		if (!initialized) return;
		int value = Math.min(180, Math.max(0, angle)); // Clamp value to 0-180
		// Don't use the Arduino class built-in analogWrite() function because it
		// calls pinMode(PWM) each time. Instead, we need to call pinMode(SERVO)
		pinMode(pin, SERVO);
		serialWrite(ANALOG_MESSAGE | (pin & 0x0F));
	    serialWrite(value & 0x7F);
	    serialWrite(value >> 7);
	}
	
	/**
	 * @param pin
	 * @param angle
	 */
	public void setServoAngleRelative(int pin, int angle) {
		if (!initialized) return;
		pinMode(pin, INPUT);
		int currentAngle = analogRead(pin);
		pinMode(pin, SERVO);
		setServoAngle(pin, currentAngle + angle);
	}

	/**
	 * Processes input received from the Arduino. Called whenever
	 * a serial input event is detected.
	 */
	public void processInput() {
		
		int inputData = serialRead(); // Read the next available byte from the serial port buffer
		//if (inputData > 0) System.out.println("inputData="+inputData);
		
		// Handle the case in which we are already inside a sysex message
		if (parsingSysex) {
			if (inputData == END_SYSEX) {
				// Sysex message is done so process it
				parsingSysex = false;
				processSysexMessage();
			} else {
				// Add to the sysex message buffer
				storedInputData[sysexBytesRead] = inputData;
				sysexBytesRead++;
			}
		} 
		
		// The current byte marks the start of a sysex message
		else 
		if (inputData == START_SYSEX) {
			parsingSysex = true;
			sysexBytesRead = 0;
		} 
		
		// Handle the case in which we are expecting a data input message
		else
		if (waitForData > 0 && inputData < 128) {
			waitForData--;
			storedInputData[waitForData] = inputData;
			if (executeMultiByteCommand != 0 && waitForData == 0) {
				//we got everything
				switch(executeMultiByteCommand) {
					case DIGITAL_MESSAGE:
						if (!initialized) break;
						digitalInputData[multiByteChannel] = (storedInputData[0] << 7) + storedInputData[1];
						break;
					case ANALOG_MESSAGE:
						if (!initialized) break;
						analogInputData[multiByteChannel] = (storedInputData[0] << 7) + storedInputData[1];
						break;
					case REPORT_VERSION:
						this.majorVersion = storedInputData[1];
						this.minorVersion = storedInputData[0];
						break;
					}
			}
		} 
		
		// Handle the markers for the start of data input messages
		else {
			int command;
			if(inputData < 0xF0) {
				command = inputData & 0xF0;
				multiByteChannel = inputData & 0x0F;
			} else {
				command = inputData;
				// commands in the 0xF* range don't use channel data
			}
			switch (command) {
				case DIGITAL_MESSAGE:
				case ANALOG_MESSAGE:
				case REPORT_VERSION:
					waitForData = 2;
					executeMultiByteCommand = command;
					break;
			}
		}
	}
	
	/**
	 * Process the MIDI sysex message currently in the input buffer. 
	 */
	public void processSysexMessage() {
		//System.out.println("SYSEX "+storedInputData[0]);
		int[] msg = Arrays.copyOfRange(storedInputData, 0, sysexBytesRead);
		switch (storedInputData[0]) {
			case REPORT_FIRMWARE: // This is called whenever we connect to Firmata and when Firmata starts up (including on Arduino reset) NOT ON MEGA???
				reportFirmwareCallback();
				break;
			case CAPABILITY_RESPONSE:
				processCapabilitiesResponse(msg);
				capabilitiesResponseCallback();
				break;
			case PIN_STATE_RESPONSE:
				// TODO use this if needed
				break;
			case ANALOG_MAPPING_RESPONSE:
				processAnalogMappingResponse(msg);
				break;
		}
	}
	
	/**
	 * Read the pin capabilities and do pin configuration
	 * based on capabilities reported by Firmata. The design of
	 * this method is based on Jeff Hoefs's Breakout IOBoard class. 
	 * See http://breakoutjs.com/docs/symbols/src/Breakout_src_core_IOBoard.js.html.
	 * 
	 * @param message the capabilities response
	 */
	public synchronized void processCapabilitiesResponse(int[] message) {
		int pinCounter = 0;
		int byteCounter = 1; // Skip command byte
		PinCapabilities pinCapabilities = new PinCapabilities(pinCounter);
		while (byteCounter < message.length) {
			if (message[byteCounter] == 127) {
				capabilities.add(pinCapabilities);
				pinCounter++;
				byteCounter++;
				pinCapabilities = new PinCapabilities(pinCounter);
			} else {
				//int resolution = message[byteCounter+1];
				switch (message[byteCounter]) {
					case ANALOG:
						pinCapabilities.addAnalog();
						analogPinCount++;
						break;
					case INPUT:
					case OUTPUT:
						pinCapabilities.addDigital();
						digitalPinCount++;
						break;
					case PWM:
						pinCapabilities.addPwm();
						break;
				}
				byteCounter += 2; // Skip over the resolution byte for each mode/resolution value pair
			}
		}
		//queryAnalogMapping();
	}
	
	// No longer needed since capability query provides this info
	public void processAnalogMappingResponse(int[] message) {
		for (int i=1; i<message.length; i++) {
			//int pin = i-1;
			if (message[i] != 127) {
				// Analog pin
			}
		}
	}
	
	/**
	 * If Firmata doesn't provide the pin capabilities of the board
	 * then just assume Arudino Uno configuration.
	 */
	public synchronized void setDefaultPinCapabilities() {
		for (int i=0; i<=19; i++) {
			capabilities.add(new PinCapabilities(i));
		}
		for (int i=0; i<=13; i++) {
			capabilities.get(i).addDigital();
		}
		capabilities.get(3).addPwm();
		capabilities.get(5).addPwm();
		capabilities.get(7).addPwm();
		capabilities.get(9).addPwm();
		capabilities.get(10).addPwm();
		capabilities.get(11).addPwm();
		for (int i=14; i<=19; i++) {
			capabilities.get(i).addAnalog();
		}
		analogPinCount = 6;
		digitalPinCount = 13;
	}
	
	/**
	 * Sends a firmware query to Firmata.
	 */
	public void reportFirmware() {
		serialWrite(START_SYSEX);
		serialWrite(REPORT_FIRMWARE);
		serialWrite(END_SYSEX);
	}
	
	/**
	 * Sends a capability query to Firmata.
	 */
	public void queryCapabilities() {
		serialWrite(START_SYSEX);
		serialWrite(CAPABILITY_QUERY);
		serialWrite(END_SYSEX);
	}
	
	/**
	 * Sends analog pin mapping query to Firmata.
	 */
	public void queryAnalogMapping() {
		serialWrite(START_SYSEX);
		serialWrite(ANALOG_MAPPING_QUERY);
		serialWrite(END_SYSEX);
	}
	
	/**
	 * Sends a series of pin state queries to Firmata 
	 * from 0 to given max number of pins.
	 */
	public void queryPinStates(int numPins) {
		for (int i=0; i<numPins; i++) {
			serialWrite(START_SYSEX);
			serialWrite(PIN_STATE_QUERY);
			serialWrite(i);
			serialWrite(END_SYSEX);
		}
	}
	
	public void delay(int millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {}
	}
	
}

class PinCapabilities {
	
	int pin;
	boolean analog, digital, pwm;
	
	public PinCapabilities(int pin) {
		super();
		this.pin = pin;
		analog = false;
		digital = false;
		pwm = false;
	}
	
	public void addAnalog() {
		analog = true;
	}
	
	public void addDigital() {
		digital = true;
	}
	
	public void addPwm() {
		pwm = true;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Pin ").append(pin).append(":\t");
		if (analog) {
			sb.append("\tanalog");
		}
		if (digital) {
			sb.append("\tdigital");
		}
		if (pwm) {
			sb.append("\tpwm");
		}
		return sb.toString();
	}

}
