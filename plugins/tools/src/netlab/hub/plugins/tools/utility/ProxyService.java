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

package netlab.hub.plugins.tools.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.Logger;

public class ProxyService extends Service {
	
	Socket sock;
	PrintWriter hubWriter;

	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#accept(netlab.hub.core.ServiceRequest, netlab.hub.core.ServiceResponse)
	 */
	@Override
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if ("service".equals(request.getPath().getFirst())) {
			if (hubWriter != null) {
				// Prevent infinite loops
				if ("proxy".equals(request.getArgument(2))) {
					throw new ServiceException("Cannot act as proxy to itself");
				}
				// Go ahead and dispatch to the other service
				String message = request.getPathString(true);
				if (message != null) {
					dispatch(message);
				}
			} else {
				throw new ServiceException("Not connected to Hub");
			}
		}
	}
	
	/**
	 * Subclasses should override this method to modify
	 * the way the proxied service is called.
	 * 
	 * @param message
	 * @throws ServiceException
	 */
	protected void dispatch(String message) throws ServiceException {
		hubWriter.println(message);
		hubWriter.flush();
	}
	
	/**
	 * @param request
	 * @param response
	 * @param port
	 * @throws ServiceException
	 */
	public void commandConnect(ServiceMessage request, ServiceResponse response, Integer port) 
	throws ServiceException {
		try {
			commandConnect(request, response, InetAddress.getLocalHost().getHostName(), port);
		} catch (UnknownHostException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @param host
	 * @param port
	 * @throws ServiceException
	 */
	public void commandConnect(ServiceMessage request, ServiceResponse response, String host, Integer port) 
	throws ServiceException {
		try {
			dispose();
			sock = new Socket(host, port.intValue());
			hubWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));
			final BufferedReader hubReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			final ServiceResponse resp = response;
			new Thread(new Runnable() {
				public void run() {
					String fromHub;
					try {
						while ((fromHub = hubReader.readLine()) != null) {
							resp.write(fromHub);
						}
					} catch (IOException e) {
						Logger.debug("Reader socket closed: "+e);
					}
				}
			}).start();
		} catch (Exception e) {
			throw new ServiceException("Error connecting to service",e );
		}
	}

	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#sessionEnded()
	 */
	@Override
	public void dispose() throws ServiceException {
		if (sock != null && !sock.isClosed()) {
			try {
				sock.close();
			} catch (IOException e) {
				throw new ServiceException(e);
			}
		}
	}

}
