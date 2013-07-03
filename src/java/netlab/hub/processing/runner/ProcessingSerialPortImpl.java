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

package netlab.hub.processing.runner;

import netlab.hub.serial.SerialEventHandler;
import netlab.hub.serial.SerialException;
import netlab.hub.serial.SerialPort;
import netlab.hub.util.Logger;
import netlab.hub.util.ThreadUtil;
import processing.core.PApplet;
import processing.serial.Serial;
/**
 * Implementation of the netlab.hub.serial.SerialPort class.
 * Provides access to Processing's serial library tools without 
 * exposing the Processing libraries themselves. To use this 
 * class, the <code>netlab.hub.serialportfactory</code> system
 * property should be set before the Hub <code>start()</code>
 * method is called. Beyond that, plug-ins and other code only 
 * need know about the <code>netlab.hub.serial.SerialPort</code> class.
 */
public class ProcessingSerialPortImpl extends SerialPort {
	
	Serial serial;
	ProcessingSerialPortImpl self; // Needed so that SerialProxy local class can refer to the Serial instance
	
	public ProcessingSerialPortImpl() {
		super();
		self = this;
	}
	
	@SuppressWarnings("serial")
	public class SerialProxy extends PApplet {
		SerialEventHandler eventHandler;
		public SerialProxy(SerialEventHandler handler) {
			this.eventHandler = handler;
		}
		public void serialEvent(Serial which) {
			if (!isReady()) return;
			if (which != self.serial) return; // Why is this needed?
			try {
				this.eventHandler.serialEvent(self);
			} catch (Exception e) {
				Logger.debug("Error invoking serialEvent method in service", e);
				return;
			}
		}
	};

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPort#connect(netlab.hub.serial.SerialEventHandler, java.lang.String, int)
	 */
	@Override
	public synchronized void connect(SerialEventHandler inputHandler, String name, int baud) throws SerialException {
		Logger.info("Connecting to serial port ["+name+"]...");
		if (MacSerialFixer.isNeeded()) {
			new MacSerialFixer(HubRunnerGUI.parent).run(); // Run in this thread so dialog box will block execution
			String msg = "Serial port configuration fix is needed by the Hub - "+
							"Please check the Hub application to run the fix and try connecting again.";
			Logger.info(msg);
			throw new SerialException(msg);
		}
		this.serial = new Serial(new SerialProxy(inputHandler), name, baud);
		ThreadUtil.pause(3000);
		Logger.info("Connection to serial port ["+name+"] established");
	}

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPort#closeConnection()
	 */
	@Override
	public synchronized void closeConnection() {
		Logger.info("Closing connection to serial port ["+name+"]");
		if (serial != null) {
			serial.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPort#read()
	 */
	@Override
	public int read() {
		if (serial == null || !isReady())
			return 0;
		return serial.read();
	}

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPort#bufferUntil(int)
	 */
	@Override
	public void bufferUntil(int what) {
		if (serial != null)
			serial.buffer(what);
	}
	
	public String readString() {
		if (serial == null || !isReady())
			return null;
		return serial.readString();
	}
	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPort#readStringUntil(int)
	 */
	@Override
	public String readStringUntil(int interesting) {
		if (serial == null || !isReady()) {
			return null;
		}
		return serial.readStringUntil(interesting);
	}

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPort#write(int)
	 */
	@Override
	public void write(int value) {
		if (serial != null && isReady())
			serial.write(value);
	}

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPort#write(java.lang.String)
	 */
	@Override
	public void write(String value) {
		if (serial != null && isReady())
			serial.write(value);
	}

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPort#write(byte[])
	 */
	@Override
	public void write(byte[] value) {
		if (serial != null && isReady())
			serial.write(value);
	}
	
	
	public int available() {
		if (serial != null && isReady())
			return serial.available();
		return 0;
	}
	
}