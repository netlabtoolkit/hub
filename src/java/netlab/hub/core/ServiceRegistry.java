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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ServiceRegistry {
	
	private static HashMap<String, Service> services = new HashMap<String, Service>(); 
	
	public synchronized static Collection<Service> getAll() {
		return services.values();
	}
	
	public synchronized static void register(String address, Service service) {
		services.put(address, service);
	}
	
	public synchronized static Service get(String address) {
		return services.get(address);
	}
	
	public synchronized static void disposeAll() throws ServiceException {
		for (Iterator<Service> it=getAll().iterator(); it.hasNext();) {
			it.next().dispose();
		}
		services.clear();
	}

}
