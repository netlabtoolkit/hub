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

package netlab.hub.plugins.tools.serial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import netlab.hub.core.ClientSession;
import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.serial.SerialEventHandler;
import netlab.hub.serial.SerialPort;
import netlab.hub.serial.SerialPortClient;
import netlab.hub.serial.SerialPortClientRegistry;
import netlab.hub.util.Logger;
import netlab.hub.util.ThreadUtil;
import netlab.hub.util.WildcardPatternMatch;


/**
 * <p>Implementation of Service for raw serial access.</p>
 * 
 * <p>Supported commands are:</p>
 * 
 * <code>
 * [service-path]/connect [port-name-pattern] [baud (optional)]
 * 		return arguments: OK [device-id] [real-port-name]
 * 		throws a ServiceException if connect fails
 * 
 * [service-path]/[port-name-pattern]/terminator [int]
 * 		Sets the input terminator character value for 
 * 		data received from the serial port.
 * 
 * [service-path]/[port-name-pattern]/listen [pattern (optional)]
 * 		Registers a listener to automatically dispatch
 *      data received from serial port back to client.
 *      
 * [service-path]/[port-name-pattern]/stoplisten
 * 		Deregisters a listener.
 * 
 * [service-path]/[port-name-pattern]/write [value]
 * 		return arguments: none
 * 		Write a value to the serial port.
 * 
 * </code>
 *
 */
public class SerialService extends Service implements SerialEventHandler, SerialPortClient {
	
	HashMap<String, SerialPort> ports = new HashMap<String, SerialPort>();
	HashMap<String, List<Listener>> listeners = new HashMap<String, List<Listener>>();
	int terminator = '\n';
	
	public enum Command {connect, terminator, listen, stoplisten, write}; 
		
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#process(netlab.hub.core.ServiceMessage, netlab.hub.core.ServiceResponse)
	 */
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		Command cmd = Command.valueOf(request.getPath().getLast());
		switch (cmd) {
			case connect: 
				commandConnect(request, response);
				break;
			case terminator: 
				commandTerminator(request, response); 
				break;
			case listen: 
				commandListen(request, response); 
				break;
			case stoplisten: 
				commandStopListen(request, response); 
				break;
			case write: 
				commandWrite(request, response); 
				break;
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public synchronized void commandConnect(ServiceMessage request, ServiceResponse response) throws ServiceException {
		try {
			String portName = getPortName(request.getArgument(0));
			Logger.debug("Using serial port ["+portName+"]");
			SerialPort serial = ports.get(portName);
			if (serial == null) {
				int baud = request.argInt(1, 9600);
				serial = new SerialPort(this, portName, baud);
				SerialPortClientRegistry.register(portName, this);
				serial.bufferUntil(terminator);
				ports.put(portName, serial);
			}
			Logger.debug("Serial port name = ["+serial.getName()+"]");
			response.write(new String[]{"OK", serial.getName()});
		} catch (Exception e) {
			Logger.error("Error connecting to serial port", e);
			response.write(new String[] {"FAIL", e.toString()});
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandTerminator(ServiceMessage request, ServiceResponse response) throws ServiceException {
		String portName = getPortName(request.getPathElement(-1));
		this.terminator = request.argInt(0, this.terminator);
		SerialPort serial = ports.get(portName);
		if (serial != null) {
			serial.bufferUntil(terminator);
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandListen(ServiceMessage request, ServiceResponse response) throws ServiceException {
		String portName = getPortName(request.getPathElement(-1));
		synchronized (this) {
			List<Listener> portListeners = listeners.get(portName);
			if (portListeners == null) {
				portListeners = new ArrayList<Listener>();
				listeners.put(portName, portListeners);
			}
			String pattern = request.getArgument(0);
			if (pattern == null) {
				pattern = "*";
			}
			Listener listener = new Listener(response, pattern);
			if (!portListeners.contains(listener)) {
				portListeners.add(listener);
			}
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandStopListen(ServiceMessage request, ServiceResponse response) throws ServiceException {
		String portName = getPortName(request.getPathElement(-1));
		synchronized (this) {
			List<Listener> portListeners = listeners.get(portName);
			if (portListeners == null) {
				return;
			}
			String pattern = request.getArgument(0);
			if (pattern == null) 
				pattern = "*";
			Listener listener = new Listener(response, pattern);
			for (Iterator<Listener> it=portListeners.iterator(); it.hasNext();) {
				if (it.next().equals(listener)) {
					it.remove();
					break;
				}
			}
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandWrite(ServiceMessage request, ServiceResponse response) throws ServiceException {
		String portName = getPortName(request.getPathElement(-1));
		if (!request.hasArgument(0)) {
			Logger.warn("Missing argument for /write command");
			return;
		}
		if (Logger.isDebug()) // Avoid extra String processing if not in debug mode
			Logger.debug("Sending ["+request.getArgument(0)+"] to device "+portName);
		SerialPort serial = getPort(portName);
		serial.write(request.getArgument(0));
		serial.write(terminator);
	}
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#sessionEnded(netlab.hub.core.ClientSession)
	 */
	public void sessionEnded(ClientSession session) {
		// Remove any listeners registered by this client
		synchronized (this) {
			for (Iterator<String> it=this.listeners.keySet().iterator(); it.hasNext();) {
				for (Iterator<Listener> portListeners=this.listeners.get(it.next()).iterator(); portListeners.hasNext();) {
					Listener listener = portListeners.next();
					if (listener.response.isForClient(session)) {
						Logger.debug("Removing listener ["+listener+"]");
						portListeners.remove();
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialEventHandler#serialEvent(netlab.hub.serial.SerialPort)
	 */
	public void serialEvent(SerialPort port) {
		String input = port.readStringUntil(terminator);
		if (input == null) return;
		input = input.trim();
		if (input.length() == 0) return;
		// We need to synchronize this because in the case that
		// the serial port implementation runs in its own thread
		// we don't want to iterate through the listeners
		// while a hub command is modifying the listener list.
		synchronized(this) {
			String portName = port.getName();
			if (listeners.containsKey(portName)) {
				List<Listener> portListeners = listeners.get(portName);
				for (Iterator<Listener> it=portListeners.iterator(); it.hasNext();) {
					Listener listener = it.next();
					listener.processInput(input);
				}
			} else {
				System.out.println("No listener found for "+portName);
			}
		}
	}
	
	/**
	 * @param pattern
	 * @return
	 * @throws ServiceException
	 */
	public String getPortName(String pattern) throws ServiceException {
		String[] portNames = null;
		portNames = SerialPort.list(pattern);
		if (portNames.length == 0) {
			throw new ServiceException("No serial port found matching name pattern ["+pattern+"]");
		}
		return portNames[0];
	}
	
	/**
	 * @param portName
	 * @return
	 * @throws ServiceException
	 */
	public SerialPort getPort(String portName) throws ServiceException {
		SerialPort serial = ports.get(portName);
		if (serial == null) {
			throw new ServiceException("No serial port connection. Did you send the /connect command first?");
		}
		return serial;
	}
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#dispose()
	 */
	public void dispose() throws ServiceException {
		for (Iterator<SerialPort> it=ports.values().iterator(); it.hasNext();) {
			it.next().dispose();
		}
		ports.clear();
	}

	/* (non-Javadoc)
	 * @see netlab.hub.serial.SerialPortClient#releasePorts()
	 */
	public synchronized void releasePorts() {
		try {
			dispose();
			ThreadUtil.pause(1000);
		} catch (ServiceException e) {
			Logger.error("Error releasing serial ports", e);
		}
	}
	
	class Listener {
		final ServiceResponse response;
		final String pattern;
		final String str;
		public Listener(ServiceResponse response) {
			this(response, null);
		}
		public Listener(ServiceResponse response, String pattern) {
			this.response = response;
			this.pattern = pattern;
			str = response.getClient() + " " + pattern;
		}
		public boolean equals(Listener other) {
			boolean sameClient = this.response.clientEquals(other.response);
			boolean samePattern = false;
			if (this.pattern == null) {
				samePattern = other.pattern == null;
			} else {
				samePattern = this.pattern.equals(other.pattern);
			}
			return sameClient && samePattern;
		}
		public void processInput(String input) {
			if (pattern == null || WildcardPatternMatch.matches(pattern, input)) {
				response.write(input);
			}
		}
		public String toString() {
			return str;
		}
	}
}
