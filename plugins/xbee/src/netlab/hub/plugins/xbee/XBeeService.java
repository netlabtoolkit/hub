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

package netlab.hub.plugins.xbee;

import java.util.HashMap;
import java.util.Iterator;

import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.serial.SerialPort;
import netlab.hub.util.Logger;

/**
 * @author Ewan Branda
 *
 */
public class XBeeService extends Service {
	
	/**
	 * The XBee networks to which this service is currently connected.
	 */
	HashMap<String, XBeeNetwork> xbees = new HashMap<String, XBeeNetwork>();
	
	int series;
	
	/**
	 * 
	 */
	public XBeeService() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#init()
	 */
	public void init() {
		this.series = "1".equals(getConfig().getParameter("xbee-series")) ? 1 : 2;
	}

	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#dispose()
	 */
	public synchronized void dispose() {
		if (xbees != null)
			for (Iterator<XBeeNetwork> it=xbees.values().iterator(); it.hasNext();) {
				it.next().dispose();
			}
	}
	
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#process(netlab.hub.core.ServiceMessage, netlab.hub.core.ServiceResponse)
	 */
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		String command = request.getPath().getLast();
		if ("connect".equals(command)) {
			commandConnect(request, response); 
		} else {
			String portNamePattern = new ReadWriteRequest(request).getPortNamePattern();
			XBeeNetwork network = xbees.get(portNamePattern);
			if (network == null) {
				throw new ServiceException("No network found connected to port ["+
						portNamePattern+"]. Did you send the /connect command first?");
			}
			if ("digitalout".equals(command)) {
				commandDigitalOut(request, response, network); 
			} 
			else
			if ("analogin".equals(command)) {
				commandRead(request, response, network, false);
			} 
			else
			if ("digitalin".equals(command)) {
				commandRead(request, response, network, true);
			}
		}
	}
	
	/**
	 * @param request
	 * @param response
	 */
	public void commandConnect(ServiceMessage request, ServiceResponse response) {
		try {
			String portNamePattern = request.getArgument(0);
			if (portNamePattern == null) {
				throw new ServiceException("Missing serial port name paramater for /connect command");
			}
			XBeeNetwork network = xbees.get(portNamePattern);
			if (network == null) {
				network = new XBeeNetwork(this.series);
				String[] availablePorts = SerialPort.list(portNamePattern);
				if (availablePorts.length == 0) {
					throw new ServiceException("Could not find a USB device matching the name pattern ["
												+portNamePattern+"]. Is your XBee connected?");
				}
				String portName = availablePorts[0]; // Take the first matching name by default
				if (!request.hasArgument(1)) {
					Logger.debug("No baud rate argument found... using default of "+XBeeNetwork.DEFAULT_BAUD_RATE);
				}
				int baudRate = request.argInt(1, XBeeNetwork.DEFAULT_BAUD_RATE);
				network.connect(portName, baudRate);
				if (xbees.isEmpty()) {
					xbees.put("*", network);
				}
				xbees.put(portName, network);
				if (!portName.equals(portNamePattern)) {
					xbees.put(portNamePattern, network);
				}
			}
			response.write(new String[]{"OK", network.getPortName()});
		} catch (Exception e) {
			response.write(new String[]{"FAIL", e.toString()});
			Logger.debug(e);
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @param network
	 * @throws ServiceException
	 */
	public void commandDigitalOut(ServiceMessage request, ServiceResponse response, XBeeNetwork network) throws ServiceException {
		try {
			ReadWriteRequest req = new ReadWriteRequest(request);
			boolean value = request.argInt(0, 0) > 0;
			network.digitalWrite(req.getRemoteId(), req.getPin(), value);
		} catch (Exception e) {
			Logger.debug("Error writing to XBee network", e);
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @param network
	 * @param reader
	 * @throws ServiceException
	 */
	public void commandRead(ServiceMessage request, ServiceResponse response, XBeeNetwork network, boolean digital) throws ServiceException {
		try {
			ReadWriteRequest req = new ReadWriteRequest(request);
			if (req.getRemoteId().equals("*")) {
				for (String id : network.getCurrentRemoteIds()) {
					ServiceMessage returnAddress = req.getReturnAddress(id);
					int value = digital ? network.digitalRead(id, req.getPin()) : network.analogRead(id, req.getPin());
					response.write(returnAddress, value);
				}
			} else {
				String id = req.getRemoteId();
				int value = digital ? network.digitalRead(id, req.getPin()) : network.analogRead(id, req.getPin());
				response.write(value);
			}
		} catch (Exception e) {
			Logger.debug("Error reading from XBee network", e);
		}
	}
}


/**
 * Helper class for parsing values from a ServiceMessage.
 *
 */
class ReadWriteRequest {
	
	ServiceMessage request;
	String portNamePattern;
	String remoteId;
	int pin;
	
	public ReadWriteRequest(ServiceMessage request) throws ServiceException {
		if (request.getPath().size() != 4) {
			throw new ServiceException("Incorrect path in command ["+request+"]");
		}
		this.request = request;
		portNamePattern = request.getPathElement(0);
		remoteId = request.getPathElement(1);
		try {
			pin = Integer.parseInt(request.getPathElement(3));
		} catch (Exception e) {
			throw new ServiceException("Incorrect pin number ["+request.getPathElement(3)+"] Should be an integer.");
		}
	}

	public String getPortNamePattern() {
		return portNamePattern;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public int getPin() {
		return pin;
	}
	
	/**
	 * Gets the return address for the request. Rewrites the
	 * remoteId in the path so that a wildcard is replaced
	 * with the actual id of a board.
	 * @param remoteId
	 * @return
	 */
	public ServiceMessage getReturnAddress(String remoteId) {
		if (!this.remoteId.equals("*")) {
			return request;
		}
		StringBuffer address = new StringBuffer();
		address.append(request.getServiceAddressString());
		String elem;
		for (int i=0; i<request.getPath().size(); i++) {
			elem = i == 1 ? remoteId : request.getPath().get(i);
			address.append("/").append(elem);
		}
		return new ServiceMessage(address.toString());
	}
}
