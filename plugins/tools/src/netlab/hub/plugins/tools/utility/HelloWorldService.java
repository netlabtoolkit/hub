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


import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceResponse;

/**
 * @author ebranda
 *
 */
public class HelloWorldService extends Service {
	
	/**
	 * Demonstrates auto-dispatching for message [servicepath]/say
	 * 
	 * @param request
	 * @param response
	 */
	public void commandSay(ServiceResponse response) {
		response.write("Hello, World (from dispatcher)");
	}
	
	/**
	 * Demonstrates auto-dispatching for message [servicepath]/say [name]
	 * 
	 * @param request
	 * @param response
	 * @param name
	 */
	public void commandSay(ServiceResponse response, String name) {
		response.write("Hello, "+name+" (from dispatcher)");
	}
	
	/**
	 * Demonstrates auto-dispatching for message [servicepath]/say/loud
	 * 
	 * @param request
	 * @param response
	 */
	public void commandSayLoud(ServiceResponse response) {
		response.write("HELLO, WORLD! (from dispatcher)");
	}
	
	/**
	 * Demonstrates auto-dispatching for message [servicepath]/say/testargs
	 * 
	 * @param request
	 * @param response
	 */
	public void commandSayTestargs(ServiceResponse response) {
		String[] values = {"Frank", "Fred", "George W"};
		response.write(values);
	}
	
	@Override
	public void dispose() throws ServiceException {
		//Logger.debug("Hello World service is saying goodbye");
	}
	
}
