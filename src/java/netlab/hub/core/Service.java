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

import java.util.Iterator;

public abstract class Service {

	ServiceConfig config;
	FilterSet outputFilters;
	boolean initialized;
	
	public Service() {
		super();
		initialized = false;
	}
	
	public void setConfig(ServiceConfig config) {
		this.config = config;
	}
	
	public ServiceConfig getConfig() {
		return this.config;
	}
	
	public boolean addresses(ServiceMessage request) {
		return this.config.addressEquals(request);
	}
	
	public String getAddress() {
		return this.config.getAddress();
	}
	
	public String toString() {
		return new StringBuffer().append(config.getGroup()).append("/").append(config.getName()).toString();
	}
	
	public FilterSet getOutputFilters() throws ServiceException {
		if (outputFilters == null && config != null) {
			if (config.getOutputFilters() != null)
				outputFilters = new FilterSet(config.getOutputFilters());
		}
		return outputFilters;
	}

	/**
	 * Handle a request message sent by a client.
	 * 
	 * @param command
	 * @throws ServiceException
	 */
	public void accept(ServiceMessage msg, ServiceResponse response) throws ServiceException {
		if (!initialized) {
			init();
			initialized = true;
			// Send any predefined startup messages to the service
			if (config != null) {
				for (Iterator<String> it=config.getStartupMessages().iterator(); it.hasNext();) {
					accept(new ServiceMessage(it.next()), response);
				}
			}
		}
		if (!Autodispatcher.dispatch(this, msg, response)) {
			process(msg, response);
		}
	}
	
	public void init() throws ServiceException {
		// Override this method to perform any required initialization
	}
	
	public void process(ServiceMessage msg, ServiceResponse response) throws ServiceException {
		// Override this method to respond to a message
	}
	
	public void sessionEnded(ClientSession session) {
		// Overried this method to perform any required cleanup when
		// a client has finished with this service
	}
	
	public void dispose() throws ServiceException {
		// Override this method to perform any required cleanup on quit
	}

}
