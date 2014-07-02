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

package netlab.hub.plugins.httpclient;

import java.io.IOException;

import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.Logger;

public class HttpClientService extends Service {

	HttpRequestDispatcher dispatcher;


	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#init()
	 */
	public void init() {
		dispatcher = new HttpRequestDispatcher();
		dispatcher.start();
	}


	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#process(netlab.hub.core.ServiceMessage, netlab.hub.core.ServiceResponse)
	 */
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		String command = request.getPath().getFirst();
		
		if ("get".equals(command)) {
			try {
				// All Hub services are invoked by a single thread so we need to use 
				// a dispatcher with its own worker thread to ensure asynchronous 
				// handling of HTTP requests. We don't want a slow server to block
				// execution of all other services running on the Hub.
				dispatcher.add(request, response); 
			} catch (DispatcherException e) {
				throw new ServiceException("Error handling get", e);
			} catch (RequestQueueOverflowException e) {
				//System.out.println("HTTP request queue capacity reached. Ignoring request ["+request+"]");
				Logger.debug("HTTP request queue capacity reached. Ignoring request ["+request+"]");
			}
		} 
		else 
		if ("queuesize".equals(command)) {
			if (request.hasArgument(0)) {
				dispatcher.setMaximumRequestQueueSize(request.argInt(0, 0));
				response.write("OK");
			} else {
				throw new ServiceException("Missing parameter for getrate command");
			}
		} else {
			throw new ServiceException("Unsupported command ["+command+"]");
		}
	}

	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#dispose()
	 */
	public void dispose() throws ServiceException {
		if (dispatcher != null)
			try {
				dispatcher.stop();
			} catch (IOException e) {
				throw new ServiceException(e);
			}
	}
}