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

package netlab.hub.serial;

import java.util.HashMap;
import java.util.Iterator;

import netlab.hub.util.Logger;

/**
 * This class manages a global registry of serial port names
 * and the client services to which they currently belong.
 * It allows clients to register themselves as users of a
 * particular serial port and exposes a principle administrative
 * method for releasing all serial ports currently in use. Ideally,
 * all services that use serial ports would obtain a port reference
 * from a central registry, but given the various libraries we are
 * using there is no way to standardize the way in which ports
 * are obtained.
 */
public class SerialPortClientRegistry {
	
	static HashMap<String, SerialPortClient> registry = new HashMap<String, SerialPortClient>();
	
	public static synchronized void register(String portName, SerialPortClient client) {
		if (registry.get(portName) != client) {
			registry.put(portName, client);
		}
	}
	
	public static synchronized void releasePorts() {
		Logger.info("Releasing all serial ports...");
		for (Iterator<SerialPortClient> clients = registry.values().iterator(); clients.hasNext();) {
			SerialPortClient client = clients.next();
			Logger.info("Releaseing ports from service ["+client.getClass()+"]");
			client.releasePorts();
		}
		Logger.info("Serial ports released.");
	}
	
	
}
