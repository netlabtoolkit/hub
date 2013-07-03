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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netlab.hub.util.Logger;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;


public class WebSocketServerImpl {
	
	int port;
	int hubPort;
	WebServer webServer;
	List<WebSocketClientSession> sessions = new ArrayList<WebSocketClientSession>();

	public WebSocketServerImpl(int port, int hubPort) {
		this.port = port;
		this.hubPort = hubPort;
	}
	
	public void start() throws ServerException {
		try {
			new Thread(new Runnable() {
				public void run() {
						webServer = WebServers.createWebServer(port);
						webServer.add("/", new WebSocketHandlerImpl());
						webServer.start();
				}
			}, "Hub-WebSocket-server").start();
		} catch (Exception e) {
			Logger.error("Error starting WebSockets service on port "+port+
					". Make sure the port number is larger than 1023 and that it is not"+
					" already in use by another service or application.", e);
			throw new ServerException("Error starting websocket server", e);
		}
	}
	
	class WebSocketHandlerImpl extends BaseWebSocketHandler {
		
	    public void onOpen(WebSocketConnection connection) {
	    	try {
				Socket hubSock = new Socket(InetAddress.getLocalHost().getHostName(), hubPort);
				WebSocketClientSession session = new WebSocketClientSession(connection, hubSock);
				if (!sessions.contains(session)) {
					Logger.debug("WebSocket connection "+sessions.size()+" to "+connection.httpRequest().remoteAddress()+" established.");
					sessions.add(session);
				}
			} catch (IOException e) {
				Logger.error("WebSocket client session had error establishing Hub connection", e);
			}
	    }

	    public void onClose(WebSocketConnection connection) {
	        Logger.debug("WebSocket connection to "+connection.httpRequest().remoteAddress()+" closed");
	    }

	    public void onMessage(WebSocketConnection connection, String msg) {
	    	WebSocketClientSession session;
			String message = msg.trim();
			for (Iterator<WebSocketClientSession> it=sessions.iterator(); it.hasNext();) {
				session = it.next();
				if (session.isFor(connection)) {
					if (message.equals("exit")) {
						session.close();
					} else {
						session.sendToHub(message);
					}
				}
			}
	    }
	}
	
	public void stop() {
		if (this.webServer != null) {
			this.webServer.stop();
			this.webServer = null;
			for (Iterator<WebSocketClientSession> it = sessions.iterator(); it.hasNext();) {
				it.next().close();
			}
		}
		Logger.debug("Stopped WebSockets server on port ["+this.port+"]");
	}
}
