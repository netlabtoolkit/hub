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

package netlab.hub.plugins.osc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.core.ClientSession;
import netlab.hub.util.WildcardPatternMatch;

/**
 * @author ewan
 *
 */
public class ResponseDispatcher {
	
	class Listener {
		ServiceResponse response;
		public Listener(ServiceResponse response) {
			this.response = response;
		}
		public boolean equals(Object other) {
			return other instanceof Listener && 
				response.clientEquals(((Listener)other).response);
		}
	}
	
	/**
	 * 
	 */
	HashMap<String,List<Listener>> listeners = new HashMap<String, List<Listener>>();
	
	/**
	 * @param pattern
	 * @param sourceIp
	 * @param response
	 */
	public void addListener(String pattern, ServiceResponse response) {
		List<Listener> listeners = this.listeners.get(pattern);
		if (listeners == null) {
			listeners = new ArrayList<Listener>();
			this.listeners.put(pattern, listeners);
		}
		Listener listener = new Listener(response);
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * @param pattern
	 * @param sourceIp
	 * @param response
	 */
	public void removeListener(String pattern, ServiceResponse response) {
		List<Listener> listeners = this.listeners.get(pattern);
		if (listeners != null) {
			listeners.remove(new Listener(response));
		}
	}
	
	/**
	 * @param session
	 */
	public void removeListeners(ClientSession session) {
		List<String> expiredPatterns = new ArrayList<String>();
		for (Iterator<String> it=this.listeners.keySet().iterator(); it.hasNext();) {
			String pattern = it.next();
			List<Listener> listeners = this.listeners.get(pattern);
			for (Iterator<Listener>its=listeners.iterator(); its.hasNext();) {
				if (its.next().response.isForClient(session)) {
					its.remove();
				}
			}
			if (listeners.isEmpty()) {
				expiredPatterns.add(pattern);
			}
		}
		for (Iterator<String> it=expiredPatterns.iterator(); it.hasNext();) {
			listeners.remove(it.next());
		}
	}
	
	/**
	 * @param pattern
	 * @return
	 */
	public boolean hasListener(String pattern) {	
		return listeners.containsKey(pattern);
	}

	/**
	 * @param serviceAddress
	 * @param senderNetAddress
	 * @param oscPattern
	 * @param args
	 * @return true if the message was dispatched to one or more listeners
	 */
	public boolean tryDispatch(String serviceAddress, String senderNetAddress, String oscPattern, Object[] args) {
		boolean dispatched = false;
		List<Listener> listeners = getListenersFor(new StringBuffer("/").append(senderNetAddress).append(oscPattern).toString());
		if (listeners != null) {
			ServiceMessage returnAddress = buildReturnAddress(serviceAddress, senderNetAddress, oscPattern);
			for (Iterator<Listener> it=listeners.iterator(); it.hasNext();) {
				it.next().response.write(returnAddress, args);
			}
			dispatched = true;
		}
		return dispatched;
	}
	
	/**
	 * Helper method.
	 * @param oscPattern
	 * @return
	 */
	public List<Listener> getListenersFor(String oscPattern) {
		String matchedKey = null;
		for (Iterator<String> it=listeners.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			if (WildcardPatternMatch.matches(key, oscPattern)) {
				matchedKey = key;
				break;
			}
		}
		return matchedKey != null ? listeners.get(matchedKey) : null;
	}
	
	/**
	 * Helper method.
	 * @param serviceAddress
	 * @param senderNetAddress
	 * @param oscPattern
	 * @return
	 */
	protected ServiceMessage buildReturnAddress(String serviceAddress, String senderNetAddress, String oscPattern) {
		StringBuffer returnAddress = new StringBuffer(serviceAddress);
		if (senderNetAddress != null && senderNetAddress.length() > 0) {
			returnAddress.append("/").append(senderNetAddress);
		}
		returnAddress.append(oscPattern);
		return new ServiceMessage(returnAddress.toString());
	}

}
