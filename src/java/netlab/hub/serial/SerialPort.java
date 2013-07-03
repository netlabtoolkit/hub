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

import java.util.HashMap;
import java.util.Iterator;

import netlab.hub.util.Logger;


/**
 * A platform-neutral serial port. Provides additional control over open ports
 * that prevents more than one client from getting a reference to a port instance. 
 * <p />
 * Usage:
 * <p />
 * <code>
 *		SerialEventHandler inputHandler = new SerialEventHandler() {
 *			public void serialEvent(SerialPort port) {
 *				System.out.println(port.readStringUntil('\n'));
 *			}
 *		};
 * 		String[] ports = SerialPort.list();
 *		// String[] ports = SerialPort.list("/usbmodem*");
 *		SerialPort port = SerialPort.open(inputHandler, ports[0], 9600);
 *		// Now make use of the port. 
 *		port.write("ok");
 *		// Close it when completely finished with it. Nobody else will be able to use it in the meantime.
 *		port.close();
 *	</code>
 *
 */
public abstract class SerialPort {
	
	/**
	 * Convenience constant for the implementation system property name.
	 */
	public static final String SERIAL_PORT_FACTORY_IMPL_CLASS = "netlab.hub.serialportfactory";
	
	/**
	 * Registry of all open <code>SerialPort</code> 
	 * instances currently in use by clients.
	 */
	private static HashMap<String, SerialPort> openPorts = new HashMap<String, SerialPort>();
	
	/**
	 * @return
	 * @throws SerialException
	 */
	public static synchronized String[] list() throws SerialException {
		String[] ports = SerialPortFactory.list();
		if (Logger.isDebug()) {
			//Logger.debug("Listing serial ports...");
			//System.out.println(Logger.getStackTrace(new java.lang.Exception()));
			//Logger.debug(ArrayUtils.toString(ports));
		}
		return ports;
	}
	
	/**
	 * @param namePattern
	 * @return
	 * @throws SerialException
	 */
	public static synchronized String[] list(String namePattern) throws SerialException {
		String[] ports = SerialPortFactory.list(namePattern);
		if (Logger.isDebug()) {
			//Logger.debug("Listing serial ports for pattern...");
			//System.out.println(Logger.getStackTrace(new java.lang.Exception()));
			//Logger.debug(ArrayUtils.toString(ports));
		}
		return ports;
	}
	
	/**
	 * @param inputHandler
	 * @param name
	 * @param baud
	 * @return
	 * @throws SerialException
	 */
	public static synchronized SerialPort open(SerialEventHandler inputHandler, String name, int baud) throws SerialException {
		Logger.debug("Connecting to port ["+name+"] at ["+baud+"]");
		if (openPorts.containsKey(name)) {
			throw new SerialException("The port ["+name+"] is already in use by another service.");
		}
		final SerialPort port = SerialPortFactory.getNewPortInstance();
		port.name = name;
		port.baud = baud;
		port.inputHandler = inputHandler;
		port.connect(inputHandler, name, baud);
		openPorts.put(name, port);
		Logger.debug("Established connection to port ["+name+"] at ["+baud+"]");
		port.ready = true;
		return port;
	}
	
	/**
	 * 
	 */
	public static synchronized void disposeAll() {
		SerialPort[] ports = new SerialPort[openPorts.size()];
		int i=0;
		// Copy ports into an array so we don't get concurrency errors if someone calls close 
		for (Iterator<SerialPort> it=openPorts.values().iterator(); it.hasNext();) {
			ports[i++] = it.next();
		}
		for (int j=0; j<ports.length; j++) {
			ports[j].close();
		}
	}
	
	protected String name;
	protected int baud;
	protected SerialEventHandler inputHandler;
	boolean ready = false;
	
	//private SerialPortMonitor monitor;
	
	public SerialPort() {
		super();
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	
	/*
	class SerialPortMonitor implements Runnable {
		boolean running = false;
		SerialPort port;
		SerialEventHandler inputHandler;
		public SerialPortMonitor(SerialPort port, SerialEventHandler inputHandler) {
			this.port = port;
			this.inputHandler = inputHandler;
		}
		public void start() {
			running = true;
			new Thread(this, "Serial-port-monitor-"+name).start();
		}
		public void stop() {
			running = false;
		}
		public void run() {
			while (running) {
				if (!port.connected()) {
					synchronized(port) {
						inputHandler.serialDisconnect(port);
						port.close();
					}
				}
				ThreadUtil.pause(100);
			}
		}
	}
	*/
	
	/**
	 * @param port
	 * @throws SerialException
	 */
	public synchronized void close() {
		if (!openPorts.containsValue(this)) return;
		ready = false;
		Logger.debug("Closing port ["+name+"]");
		//monitor.stop();
		openPorts.remove(name);
		closeConnection();
		Logger.debug("Closed port ["+name+"]");
	}
	
	/**
	 * @throws SerialException
	 */
	public synchronized void reconnect() throws SerialException {
		ready = false;
		closeConnection();
		connect(inputHandler, name, baud);
	}
	
	public boolean isReady() {
		return ready;
	}
	
	/**
	 * @param inputHandler
	 * @param name
	 * @param baud
	 * @throws SerialException
	 */
	public abstract void connect(SerialEventHandler inputHandler, String name, int baud) throws SerialException;
	
	/**
	 * @param value
	 */
	public abstract void write(int value);
	
	/**
	 * @param value
	 */
	public abstract void write(String value);
	
	/**
	 * @param value
	 */
	public abstract void write(byte[] value);
	
	/**
	 * @param what
	 */
	public abstract void bufferUntil(int what);
	
	/**
	 * @return
	 */
	public abstract String readString();
	
	/**
	 * @param interesting
	 * @return
	 */
	public abstract String readStringUntil(int interesting);
	
	/**
	 * @return
	 */
	public abstract int read();
	
	/**
	 * 
	 */
	public abstract void closeConnection();
	
	/**
	 * @return
	 */
	public abstract int available();
	
}
