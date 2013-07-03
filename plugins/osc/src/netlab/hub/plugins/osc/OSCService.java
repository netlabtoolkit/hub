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

package netlab.hub.plugins.osc;

import java.net.InetAddress;

import netlab.hub.core.ClientSession;
import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.Logger;
import netlab.hub.util.NetUtils;

public class OSCService extends Service {

	/**
	 * Helper for managing dispatching of osc messages received
	 * from the osc implementation to registered clients
	 */
	ResponseDispatcher responseDispatcher;
	
	/**
	 * The specific OSC implementation we will be using
	 */
	OSC oscImpl;
	
	/**
	 * 
	 */
	public OSCService() {
		this(new ResponseDispatcher());
	}
	
	public OSCService(ResponseDispatcher dispatcher) {
		super();
		responseDispatcher = dispatcher;
		oscImpl = new P5OSC(this);
	}
	
	/**
	 * @param oscImpl
	 */
	public void setOscImpl(OSC oscImpl) {
		this.oscImpl = oscImpl;
	}
	
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#process(netlab.hub.core.ServiceMessage, netlab.hub.core.ServiceResponse)
	 */
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if (request.getPath().isEmpty()) {
			Logger.debug("Incorrect message format ["+request+"]... ignoring.");
			return;
		}
		String command = request.getPath().getLast();
		if ("connect".equalsIgnoreCase(command)) {
			commandConnect(request, response);
		} else
		if ("listen".equalsIgnoreCase(command)) {
			commandListen(request, response);
		} else
		if ("stoplisten".equalsIgnoreCase(command)) {
			commandStopListen(request, response);
		} else {
			commandDispatch(request, response);
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandConnect(ServiceMessage request, ServiceResponse response) throws ServiceException {
		int receivePort = request.argInt(0, -1);
		if (receivePort == -1) {
			throw new ServiceException("Illegal port number in message ["+request+"]");
		}
		try {
			String[] args = request.getArgumentsAsStringArray();
			validateConnection(args);
			String[] responseArgs = new String[args.length+1];
			responseArgs[0] = "OK";
			for (int i=1; i<responseArgs.length; i++) {
				responseArgs[i] = args[i-1];
			}
			response.write(responseArgs);
		} catch (Exception e) {
			response.write(new String[] {"FAIL", e.toString()});
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandListen(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if (request.getArguments().size() < 2) {
			throw new ServiceException("Missing listen pattern in message ["+request+"]");
		}
		String pattern = request.getArgument(0);
		int portNum = request.argInt(1, -1);
		if (portNum == -1) {
			throw new ServiceException("Illegal port number in message ["+request+"]");
		}
		oscImpl.listen(portNum);
		responseDispatcher.addListener(pattern, response);
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandStopListen(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if (request.getArguments().size() < 1) {
			throw new ServiceException("Missing listen pattern in message ["+request+"]");
		}
		String pattern = request.getArgument(0);
		responseDispatcher.removeListener(pattern, response);
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandDispatch(ServiceMessage request, ServiceResponse response) throws ServiceException {
		// Send the message to the device and let listeners
		// handle sending any responses back to the client
		String[] dest = request.getPath().getFirst().split(":");
		if (dest.length != 2) {
			throw new ServiceException("Incorrect IP address format for target device. Should be [host-ip]:[port]");
		}
		String ip = dest[0];
		int portNum = Integer.parseInt(dest[1]);
		String address = request.getPathString(false, 1);
		Object[] args = request.getArgumentsAsObjectArray();
		oscImpl.send(address, args, ip, portNum);
	}
	
	/**
	 * @param args - can be {receivePort}, {sendAddress}, or {receivePort, sendAddress}
	 * @throws ServiceException
	 */
	protected void validateConnection(String[] args) throws Exception {
		if (args.length == 0) {
			throw new ServiceException("No connection settings specified");
		}
		int receivePort = 0;
		String sendAddress = null;
		if (args[0] != null) {
			try {
				receivePort = Integer.parseInt(args[0]);
				if (args.length > 1) {
					sendAddress = args[1];
				}
			} catch (NumberFormatException e) {
				sendAddress = args[0];
			}
		}
		if (receivePort > 0) {
			NetUtils.tryBind(receivePort);
		}
		if (sendAddress != null) {
			// Remove port if provided
			if (sendAddress.indexOf(":") > -1) {
				sendAddress = sendAddress.substring(0, sendAddress.indexOf(":"));
			}
			NetUtils.verifyRouteToHost(sendAddress);
		}
	}
	
	/**
	 * Called by the osc implementation when a message is received from a device.
	 * @param senderNetAddress
	 * @param oscPattern
	 * @param oscArgs
	 */
	public void messageReceived(InetAddress source, String oscPattern, Object[] oscArgs) {
		responseDispatcher.tryDispatch(this.getAddress(), source.getHostAddress(), oscPattern, oscArgs);
	}
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#sessionEnded(netlab.hub.core.ClientSession)
	 */
	public void sessionEnded(ClientSession session) {
		responseDispatcher.removeListeners(session);
	}
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#dispose()
	 */
	public void dispose() throws ServiceException {
		oscImpl.dispose();
	}
}
