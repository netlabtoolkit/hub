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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import netlab.hub.core.Config;
import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceRegistry;
import netlab.hub.core.ServiceResponse;

/**
 * @author ebranda
 */
public class SendConfigService extends Service {
	
	/**
	 * 
	 */
	public SendConfigService() {
		super();
	}

	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#accept(netlab.hub.core.ClientMessage)
	 */
	@Override
	public void process(ServiceMessage request, ServiceResponse response)
	throws ServiceException {
		// Return the config, regardless of contents of request
		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		try {
			for (Iterator<Service> it=ServiceRegistry.getAll().iterator(); it.hasNext();) {
				Service service = it.next();
				String id = service.getConfig().getAddress();
				printer.print("Port="+Config.getPort()+"  ");
				printer.print("["+id+"]   ");
				printer.println(service.getConfig().getDescription());
				//printer.println(" ");
			}
		} catch(Exception e) {
			printer.print("Error formatting configuration: "+e);
		}
		String result = writer.toString();
		response.write(result);
	}

}
