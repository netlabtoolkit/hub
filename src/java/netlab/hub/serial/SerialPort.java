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

package netlab.hub.serial;

import java.util.ArrayList;
import java.util.List;

import netlab.hub.util.ArrayUtils;
import netlab.hub.util.ThreadUtil;
import netlab.hub.util.WildcardPatternMatch;
import processing.core.PApplet;
import processing.serial.Serial;

/**
 * This class is a decorator for the Processing serial implementation.
 * It is not technically necessary, but it protects plugins that need
 * a serial port from the fact that we are using Processing's serial
 * implementation, which will make changes easier down the road.
 * 
 */
public class SerialPort {
	
	Serial serial;
	SerialEventHandler eventHandler;
	SerialPort self;
	
	@SuppressWarnings("serial")
	public class SerialProxy extends PApplet {
	    public void serialEvent(Serial which) {
	      eventHandler.serialEvent(self);
	    }
	  }
	
	public SerialPort(SerialEventHandler eventHandler, String name, int rate) throws SerialException {
		this.self = this;
		MacSerialFixer.check();
		try {
			this.serial = new Serial(new SerialProxy(), name, rate);
		} catch (RuntimeException e) {
			throw new SerialException("Error opening serial port. The port may be in use by another application.");
		}
		ThreadUtil.pause(3000);
	}
	
	public String getName() {
		return serial.port.getName();
	}
	
	public void bufferUntil(int what) {
		serial.bufferUntil(what);
	}
	
	public void write(int value) {
		serial.write(value);
	}
	
	public void write(byte[] value) {
		serial.write(value);
	}
	
	public void write(String value) {
		serial.write(value);
	}
	
	public String readStringUntil(int interesting) {
		return serial.readStringUntil(interesting);
	}
	
	public void dispose() {
		serial.dispose();
	}
	
	public static String[] list() {
		return Serial.list();
	}
	
	public static String[] list(String pattern) {
		if (pattern == null) return list();
		String[] allPorts = list();
		List<String> matched = new ArrayList<String>();
		for (int i=0; i<allPorts.length; i++) {
			String candidate = allPorts[i];
			if (WildcardPatternMatch.matches(pattern, candidate)) {
				matched.add(candidate);
			}
		} 
		return ArrayUtils.toStringArray(matched);
	}

}

