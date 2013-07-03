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

package netlab.hub.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netlab.hub.util.Logger;
import netlab.hub.util.ThreadUtil;

/**
 * Accepts messages from clients and dispatches them to the appropriate service.
 * Converts multithreaded socket client requests to a single thread thereby
 * guaranteeing services that they will only be called from one thread.
 * 
 * @author
 *
 */
public class Dispatcher {
	
	List<ClientSession> expiredClients = new ArrayList<ClientSession>();
	List<ClientSession> clients = new ArrayList<ClientSession>();
	List<DispatcherPoller> pollers = new ArrayList<DispatcherPoller>();
	boolean running = false;
	boolean processing = false;
	
	
	/**
	 * 
	 */
	public Dispatcher() {
	}
	
	/**
	 * @param session
	 * @throws ServiceException
	 */
	public synchronized void register(ClientSession session) {
		if (!clients.contains(session)) {
			clients.add(session);
		}
	}
	
	/**
	 * @param client
	 */
	public synchronized void deregister(ClientSession client) {
		if (!expiredClients.contains(client)) {
			expiredClients.add(client);
		}
	}
	
	/**
	 * 
	 */
	public void start() {
		new Thread(new Runnable() {
			public void run() {
				// Start the maintenance thread. We process the clients as fast as possible
				// to avoid socket input stream overflow in case the client is sending 
				// messages very quickly.
				running = true;
				processing = false;
				while (running) {
					processing = true;
					try {
						process();
					} catch (Throwable t) {
						Logger.error("Error dispatching message", t);
					}
					processing = false;
					ThreadUtil.pause(10); // Tiny delay, so we don't hog the CPU. Is this needed?
				}
				Logger.debug("Shutting down services...");
				try {
					ServiceRegistry.disposeAll();
				} catch (ServiceException e) {
					Logger.error("Error stopping all services", e);
				}
			}
		}, "Hub-Dispatcher").start();
	}
	
	/**
	 * This method is invoked by each iteration
	 * of the main dispatcher maintenance loop.
	 */
	protected synchronized void process() {
		ClientSession client;
		// Remove any expired client sessions and give all services a chance 
		// to take action when a client session has ended
		for (Iterator<ClientSession> it=expiredClients.iterator(); it.hasNext();) {
			client = it.next();
			for (Iterator<Service> its=ServiceRegistry.getAll().iterator(); its.hasNext();) {
				its.next().sessionEnded(client);
			}
			// Remove all pollers that have been registered by this client
			for (Iterator<DispatcherPoller> itp=pollers.iterator(); itp.hasNext();) {
				if (itp.next().client == client) {
					itp.remove();
				}
			}
			it.remove();
			clients.remove(client);
		}
		// Process any available client messages
		for (Iterator<ClientSession> it=clients.iterator(); it.hasNext();) {
			client = it.next();
			for (Iterator<ServiceMessage> it2=client.getMessages().iterator(); it2.hasNext();) {
				processMessage(it2.next(), client);
			}
		}
		// Process any currently registered pollers
		DispatcherPoller poller;
		boolean polled;
		for (Iterator<DispatcherPoller> it=pollers.iterator(); it.hasNext();) {
			poller = it.next();
			polled = poller.tryPoll(this);
			if (polled) {
				// If this poller has been configured to run for a finite number 
				// of times then remove it from the list of registered pollers
				// if it has completed its lifecycle
				if (poller.completed()) {
					Logger.debug("Removing registered poller for pattern ["+poller.getPattern()+"]");
					it.remove();
				}
			}
		}
	}
	
	/**
	 * @param client
	 */
	public void processMessage(ServiceMessage message, ClientSession client) {
		try {
			DispatcherAction action = DispatcherAction.parse(message);
			action.perform(message, client, this);
		} catch (Exception e) {
			Logger.error("Error processing client message", e);
		}
	}
	
	/**
	 * @param request
	 * @throws ServiceException
	 */
	protected void dispatch(ServiceMessage request, ClientSession client) {
		if (!request.isValid()) {
			Logger.debug("Invalid request pattern ["+request+"]... ignoring.");
			return;
		}
		Service service = getService(request);
		if (service == null) {
			Logger.debug("No service exists for request ["+request+"]... ignoring.");
			return;
		}
		ServiceResponse response = new ServiceResponse(request, client);
		try {
			response.setFilters(service.getOutputFilters());
			service.accept(request, response);
		} catch (Exception e) {
			Logger.debug("Error dispatching message ["+request+"]");
			Logger.debug(e);
			return;
		}
	}
	
	/**
	 * @param request
	 * @return
	 */
	public Service getService(ServiceMessage request) {
		return ServiceRegistry.get(request.getServiceAddressString());
	}
	
	/**
	 * @param poller
	 */
	public synchronized void registerPoller(DispatcherPoller poller) {
		if (!pollers.contains(poller)) {
			pollers.add(poller);
		}
	}
	
	/**
	 * @param poller
	 * @return
	 */
	public synchronized boolean deregisterPoller(DispatcherPoller poller) {
		return pollers.remove(poller);
	}
	
	/**
	 * @param client
	 * @param pattern
	 * @return
	 */
	public synchronized DispatcherPoller getRegisteredPoller(ClientSession client, ServiceMessage pattern) {
		for (Iterator<DispatcherPoller> it=pollers.iterator(); it.hasNext();) {
			DispatcherPoller poller = it.next();
			if (poller.getClient() == client && poller.getPattern().equals(pattern)) {
				return poller;
			}
		}
		return null;
	}
	
	/**
	 * 
	 */
	public synchronized void dispose() {
		running = false;
	}
	
	/**
	 * To support unit testing.
	 * @return
	 */
	public List<ClientSession> getClients() {
		return this.clients;
	}
	
	/**
	 * To support unit testing.
	 * @return
	 */
	public List<DispatcherPoller> getPollers() {
		return this.pollers;
	}
	
}
