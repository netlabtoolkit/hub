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

import netlab.hub.serial.SerialEventHandler;
import netlab.hub.serial.SerialException;
import netlab.hub.serial.SerialPort;
import netlab.hub.util.Logger;

public class Arduino extends BaseArduino implements SerialEventHandler {
	
	public static String[] list() {
		try {
			return SerialPort.list();
		} catch (SerialException e) {
			Logger.error("Error listing serial ports", e);
			return new String[] {};
		}
	}
	
	SerialPort serial;
	
	public Arduino() {
		super();
	}

	public Arduino(String portName) throws SerialException {
		this(portName, 57600);
	}
	
	public Arduino(String portName, int rate) throws SerialException {
		this.serial = SerialPort.open(this, portName, rate);
		begin();
	}
	
	public SerialPort getSerialPort() {
		return this.serial;
	}

	@Override
	public String getSerialPortName() {
		return serial.getName();
	}

	@Override
	public int serialRead() {
		return serial.read();
	}

	@Override
	public void serialWrite(int value) {
		serial.write(value);
	}

	@Override
	public void serialEvent(SerialPort port) {
		while (serial.available() > 0)
			processInput();
	}

	@Override
	public void dispose() {
		serial.close();
	}


}
