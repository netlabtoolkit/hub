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

import java.io.PrintWriter;

import netlab.hub.util.Logger;

public class ServiceResponse {
	
	//Object value;
	FilterSet filters;
	ServiceMessage returnAddress;
	ClientSession client;
	ResponseMessage message;
	
	public ServiceResponse(ServiceMessage request, ClientSession client) {
		super();
		this.returnAddress = request; // Use the request as the default return address
		this.client = client;
	}
	
	public void setFilters(FilterSet filters) {
		this.filters = filters;
	}
	
	public void addFilter(IResponseFilter filter) {
		if (filters == null) {
			filters = new FilterSet();
		}
		this.filters.add(filter);
	}
	
	public void write() {
		write(null);
	}
	
	public ClientSession getClient() {
		return this.client;
	}

	/**
	 * Write the response using the original request as the
	 * default return address.
	 * @param value
	 */
	public void write(Object value) {
		write(this.returnAddress, value);
	}
	
	/**
	 * Write the response using the given return address.
	 * @param returnAddress
	 * @param value
	 */
	public void write(ServiceMessage returnAddress, Object value) {
		this.message = new ResponseMessage(returnAddress, value);
		if (client != null)
			client.sendResponse(this);
	}
	
	public boolean isEmpty() {
		return message == null;
	}
	
	public ResponseMessage getMessage() {
		return this.message;
	}
	
	public boolean send(PrintWriter out) {
		if (out == null) {
			Logger.error("No PrintWriter has been sent to service response object");
			return false;
		}
		if (isEmpty()) return false;
		if (filters != null) {
			filters.apply(this.message);
		}
		String output = this.message.format();
		if (output != null && output.length() > 0) {
			out.print(output);
			out.print("\n");
			out.write(0x00); // EOF is indicated by a single NULL byte
			out.flush();
			if (Logger.isDebug())
				Logger.debug("### Sending message to client ["+this.message+"]");
			return true;
		}
		return false;
	}

	public boolean isForClient(ClientSession client) {
		return this.client == client; // TODO this needs to be more robust
	}
	
	public boolean clientEquals(ServiceResponse other) {
		return isForClient(other.client);
	}

}
