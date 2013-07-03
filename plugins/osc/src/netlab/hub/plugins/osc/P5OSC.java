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

import java.util.HashMap;
import java.util.Iterator;

import netP5.NetAddress;
import netlab.hub.core.ServiceException;

import oscP5.OscMessage;
import oscP5.OscP5;

public class P5OSC implements OSC {
	
	public static final int DEFAULT_RECEIVE_PORT = 10000;
	
	/**
	 * A map of OscP5 instances, each listening on a different receive port.
	 */
	HashMap<Integer,OscP5> oscClients = new HashMap<Integer, OscP5>();
	
	/**
	 * The OscP5 instance that we will use for sending all messages.
	 */
	OscP5 sender;
	
	OSCService service;
	
	/**
	 * 
	 */
	public P5OSC(OSCService service) {
		super();
		this.service = service;
	}
	
	
	/* (non-Javadoc)
	 * @see netlab.hub.plugins.osc.IOSC#listen(int)
	 */
	public void listen(int portNum) throws ServiceException {
		if (!oscClients.containsKey(portNum)) {
			oscClients.put(portNum, new OscP5(new OscListenerProxy(service), portNum));
		}
	}
	
	public void stopListen(int portNum) {
		if (oscClients.containsKey(portNum)) {
			oscClients.get(portNum).dispose();
		}
	}


	/* (non-Javadoc)
	 * @see netlab.hub.plugins.osc.IOSC#send(java.lang.String, java.lang.String[], java.lang.String, int)
	 */
	public void send(String address, Object[] args, String ip, int portNum) {
		OscMessage message;
		if (args == null || args.length == 0) {
			message = new OscMessage(address);
		} else {
			message = new OscMessage(address, args);
		}
		NetAddress destination = new NetAddress(ip, portNum);
		if (sender == null) {
			// Use the default client to send messages, since the port on which it is listening
			// doesn't make any difference to sending. OscP5 requires that a listener port
			// be registered for each client, even if we are only interested in sending.
			if (oscClients.isEmpty()) {
				oscClients.put(DEFAULT_RECEIVE_PORT, new OscP5(new OscListenerProxy(service), DEFAULT_RECEIVE_PORT));
			}
			sender = oscClients.values().iterator().next();
		}
		sender.send(message, destination);
	}


	/* (non-Javadoc)
	 * @see netlab.hub.plugins.osc.IOSC#dispose()
	 */
	public void dispose() {
		for (Iterator<OscP5> it=oscClients.values().iterator(); it.hasNext();) {
			it.next().dispose();
		}
	}

}

/**
 * Helper subclass of PApplet. The OscP5 library dispatches incoming
 * OSC messags to the oscEvent() method of PApplet implementations.
 * @author ewan
 *
 */
class OscListenerProxy {
	OSCService service;
	public OscListenerProxy(OSCService service) {
		this.service = service;
	}
	public void registerDispose(Object o) {
		// Do nothing. We need to override this because OscP5 calls it and 
		// it will throw a null pointer error when using a dummy applet.
	}
	public void oscEvent(OscMessage message) {
		service.messageReceived(message.netAddress().inetaddress(), message.addrPattern(), message.arguments());
	}
}
