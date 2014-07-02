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
import netlab.hub.serial.SerialPortClient;
import netlab.hub.serial.SerialPortClientRegistry;
import netlab.hub.util.Logger;
import netlab.hub.util.ThreadUtil;

/**
 * @author Ewan Branda
 *
 */
public class XBeeService extends Service implements SerialPortClient {
	
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
	 * @see netlab.hub.serial.SerialPortClient#releasePorts()
	 */
	public synchronized void releasePorts() {
		dispose();
		ThreadUtil.pause(1000);
	}
	
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#process(netlab.hub.core.ServiceMessage, netlab.hub.core.ServiceResponse)
	 */
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if ("connect".equals(request.getPath().getLast())) {
			commandConnect(request, response); 
		} else {
			String command = request.getPathElement(2);
			String portNamePattern = request.getPathElement(0);
			XBeeNetwork network = xbees.get(portNamePattern);
			if (network == null) {
				throw new ServiceException("No network found connected to port ["+
						portNamePattern+"]. Did you send the /connect command first?");
			}
			if ("digitalout".equals(command)) {
				commandDigitalWrite(request, response, network); 
			} 
			else
			if ("analogin".equals(command)) {
				commandRead(request, response, network, false);
			} 
			else
			if ("digitalin".equals(command)) {
				commandRead(request, response, network, true);
			}
			else
			if ("rssi".equals(command)) {
				commandRssi(request, response, network, true);
			}
			else {
				Logger.debug("Unsupported command ["+command+"]");
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
				throw new ServiceException("Missing serial port name parameter for /connect command");
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
				SerialPortClientRegistry.register(portName, this);
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
	public void commandDigitalWrite(ServiceMessage request, ServiceResponse response, XBeeNetwork network) throws ServiceException {
		try {
			ReadWriteRequest req = new ReadWriteRequest(request, network);
			boolean value = request.argInt(0, 0) > 0;
			String remoteId = req.getRemoteId();
			if ("*".equals(remoteId)) {
				network.digitalSend(req.getPin(), value);
			} else {
				network.digitalSend(req.getPin(), value, remoteId);
			}
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
			ReadWriteRequest req = new ReadWriteRequest(request, network);
			String[] remoteIds = req.getRemoteIds();
			for (String remoteId : remoteIds) {
				ServiceMessage returnAddress = req.getReturnAddress(remoteId);
				RemoteXBee xbee = network.getRemoteXBee(remoteId);
				int value = 0;
				if (xbee != null) {
					value = digital ? xbee.digitalRead(req.getPin()) : xbee.analogRead(req.getPin());
				}
				response.write(returnAddress, value);
			}
		} catch (Exception e) {
			Logger.debug("Error reading from XBee network", e);
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @param network
	 * @param reader
	 * @throws ServiceException
	 */
	public void commandRssi(ServiceMessage request, ServiceResponse response, XBeeNetwork network, boolean digital) throws ServiceException {
		try {
			XBeeRequest req = new XBeeRequest(request, network);
			String[] remoteIds = req.getRemoteIds();
			for (String remoteId : remoteIds) {
				ServiceMessage returnAddress = req.getReturnAddress(remoteId);
				RemoteXBee xbee = network.getRemoteXBee(remoteId);
				if (xbee != null) {
					response.write(returnAddress, xbee.getRssi());
				} else {
					Logger.debug("No xbee found for id "+remoteId);
				}
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
class XBeeRequest {
	
	ServiceMessage request;
	String portNamePattern;
	String remoteId;
	XBeeNetwork network;
	
	public XBeeRequest(ServiceMessage request, XBeeNetwork network) throws ServiceException {
		this.network = network;
		if (request.getPath().size() < 3) {
			throw new ServiceException("Incorrect path in command ["+request+"]");
		}
		this.request = request;
		portNamePattern = request.getPathElement(0);
		remoteId = request.getPathElement(1);
	}

	public String getPortNamePattern() {
		return portNamePattern;
	}
	
	public String getRemoteId() {
		return remoteId;
	}
	
	public String[] getRemoteIds() {
		if ("*".equals(remoteId)) {
			return network.getAllRemoteIds();
		} else {
			return new String[]{remoteId};
		}
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

class ReadWriteRequest extends XBeeRequest {
	
	int pin;
	
	public ReadWriteRequest(ServiceMessage request, XBeeNetwork network) throws ServiceException {
		super(request, network);
		if (request.getPath().size() != 4) {
			throw new ServiceException("Incorrect path in command ["+request+"]");
		}
		try {
			pin = Integer.parseInt(request.getPathElement(3));
		} catch (Exception e) {
			throw new ServiceException("Incorrect pin number ["+request.getPathElement(3)+"] Should be an integer.");
		}
	}

	public int getPin() {
		return pin;
	}
}
