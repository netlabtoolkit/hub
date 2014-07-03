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

package netlab.hub.plugins.tools.pipe;

import java.util.Iterator;

import netlab.hub.core.ClientSession;
import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.Logger;

/**
 * @author ewan
 *
 */
public class PipeService extends Service {
	
	PipeRegistry pipes = new PipeRegistry();
	
	/**
	 * To support unit testing
	 * @return
	 */
	public PipeRegistry getPipes() {
		return pipes;
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	@Override
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		
		String command = request.getPathElement(0);
		String id = request.getPath().size() > 1 ? request.getPathElement(1) : "default";
		Pipe pipe;
		
		if ("connect".equals(command)) {
			if (!pipes.exists(id)) {
				pipes.create(id);
			}
			response.write("OK");
		} else {
			try {
				if ("send".equals(command)) {
					pipe = pipes.get(id);
					if (!request.hasArgument(0)) {
						Logger.warn("Missing argument for send command");
						return;
					}
					pipe.send(request.getArgumentsAsStringArray());
				} else if ("receive".equals(command)) {
					pipe = pipes.get(id);
					final ServiceResponse resp = response;
					pipe.listeners.add(new IPipeListener() {
						public void accept(String[] value) {
							resp.write(value);
						}
						public ServiceResponse getResponse() {
							return resp;
						}
					});
				} else if ("latestvalue".equals(command)) {
					pipe = pipes.get(id);
					response.write(pipe.latestValue);
				} else {
					Logger.warn("Unsupported command: ["+command+"]");
				}
			} catch (NoSuchPipeException e) {
				throw new ServiceException("No pipe found for command ["+request+"]. Did you send the [/connect "+id+"] command first?");
			}
		}
	}
	
	/* (non-Javadoc)
	 * We don't have to worry about synchronization because the Hub core
	 * ensures that this method is called in a thread-safe way.
	 * @see netlab.hub.core.Service#sessionEnded(netlab.hub.core.ClientSession)
	 */
	public void sessionEnded(ClientSession client) {
		// For each pipe, remove any listeners associated with the client
		// Do not clean up the pipe itself since senders may use it again
		// without sending the "connect" command.
		for (Iterator<String> pipeIds = pipes.pipes.keySet().iterator(); pipeIds.hasNext();) {
			String pipeId = pipeIds.next();
			Pipe pipe = pipes.pipes.get(pipeId);
			for (Iterator<IPipeListener> listeners = pipe.listeners.iterator(); listeners.hasNext();) {
				if (listeners.next().getResponse().isForClient(client)) {
					listeners.remove();
					Logger.debug("Removing client listener");
				}
			}
		}
	}

}
