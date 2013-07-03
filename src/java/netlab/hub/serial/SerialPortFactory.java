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
import netlab.hub.util.Logger;
import netlab.hub.util.WildcardPatternMatch;

/**
 * Builds <code>SerialPort</code> instances. Uses the implementation specified
 * in the <code>netlab.hub.serialportfactory</code> system property. This
 * property must be set prior to calling the <code>Hub start()</code> method.
 * <p />
 * Clients may use this class directly, but simpler is to use the equivalent
 * static methods in the <code>SerialPort</code> class. That way, clients
 * only need know of one class to access all serial port functionality.
 */
public abstract class SerialPortFactory {
	
	private static SerialPortFactory instance; // Singleton
	
	/**
	 * Builds a new <code>SerialPortFactory</code> implementation instance. Use this
	 * method to acquire a reference to the specific factory instance for building
	 * <code>SerialPort</code> instances.
	 * 
	 * @return the implementation instance
	 * @throws SerialException
	 */
	private static synchronized SerialPortFactory getInstance() throws SerialException {
		if (instance == null) {
			String cls = System.getProperty(SerialPort.SERIAL_PORT_FACTORY_IMPL_CLASS);
			if (cls == null) return new NullSerialPortFactory(); // No serial support set
			try {
				instance = (SerialPortFactory)Class.forName(cls).newInstance();
			} catch (Exception e) {
				throw new SerialException("Unknown SerialPortFactory implementation class: "+e);
			}
		}
		return instance;
	}
	
	/**
	 * Returns a list names of all currently available serial ports.
	 * 
	 * @return the list of names
	 * @throws SerialException
	 */
	protected static synchronized String[] list() throws SerialException {
		try {
			return getInstance().listAllPorts();
		} catch (SerialException e) {
			Logger.error("Error listing serial ports", e);
			return new String[] {};
		}
	}

	/**
	 * Returns a list names of all currently available serial ports that match
	 * the name pattern string provided.
	 * 
	 * @param namePattern a string specifying the name pattern to match
	 * @return a list of matching names
	 * @throws SerialException
	 */
	protected synchronized static String[] list(String namePattern) throws SerialException {
		if (namePattern == null)
			return list();
		String[] allPorts = list();
		List<String> matched = new ArrayList<String>();
		for (int i=0; i<allPorts.length; i++) {
			String candidate = allPorts[i];
			if (WildcardPatternMatch.matches(namePattern, candidate)) {
				matched.add(candidate);
			}
		} 
		return ArrayUtils.toStringArray(matched);
	}
	
	/**
	 * The main static factory method for obtaining instances of 
	 * <code>SerialPort</code> implementations.
	 * 
	 * @return the implementation instance
	 * @throws SerialException
	 */
	protected synchronized static SerialPort getNewPortInstance() throws SerialException {
		return getInstance().newPortInstance();
	}
	
	/**
	 * @return a list of names of all available ports.
	 * 
	 * @throws SerialException
	 */
	protected abstract String[] listAllPorts() throws SerialException;
	
	/**
	 * The main factory method for creating <code>SerialPort</code> instances.
	 * 
	 * @return the new instance
	 * @throws SerialException
	 */
	protected abstract SerialPort newPortInstance() throws SerialException;

}


/**
 * Null factory class that is used by default when no
 * implementation is specified in the system property.
 */
class NullSerialPortFactory extends SerialPortFactory {
	public String[] listAllPorts() throws SerialException {
		return new String[] {};
	}
	public SerialPort newPortInstance() {
		return new NullSerialImpl();
	}
}

/**
 * Null <code>SerialPort</code> implementation class that is used 
 * by default when no implementation is specified in the system property.
 */
class NullSerialImpl extends SerialPort {
	
	public void connect(SerialEventHandler inputHandler, String name, int baud) throws SerialException {
		super.inputHandler = inputHandler;
		super.name = name;
		super.baud = baud;
	}
	public int available() { return 0; }
	public void closeConnection() {}
	public void bufferUntil(int what) {}
	public int read() { return 0; }
	public String readString() { return ""; }
	public String readStringUntil(int interesting) { return ""; }
	public void write(int value) {}
	public void write(String value) {}
	public void write(byte[] value) {}
}
