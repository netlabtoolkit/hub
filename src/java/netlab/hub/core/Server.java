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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netlab.hub.util.Logger;
import netlab.hub.util.ThreadUtil;

/**
 * @author ebranda
 */
public class Server implements Runnable {
	
	/* For autonumbering session threads. */
	private static int sessionThreadInitNumber = 0;
	private static synchronized int nextSessionThreadNum() {
		return sessionThreadInitNumber++;
	}
	
	int port;
	ServerSocket serverSock;
	IDataActivityMonitor dataActivityMonitor;
	ISessionLifecycleMonitor sessionLifecycleMonitor;
	Dispatcher dispatcher;
	
	List<ClientSession> activeSessions = new ArrayList<ClientSession>();
	boolean stopped = true;

	/**
	 * 
	 */
	public Server(int port, Dispatcher dispatcher, ISessionLifecycleMonitor lm, IDataActivityMonitor dm) {
		super();
		this.port = port;
		this.dispatcher = dispatcher;
		// Create null monitors if none were provided
		this.dataActivityMonitor = dm != null ? dm : new IDataActivityMonitor() {
			public void dataReceived() {}
			public void dataSent() {}
		};
		this.sessionLifecycleMonitor = lm != null ? lm : new ISessionLifecycleMonitor() {
			public void sessionStarted(String clientId) {}
			public void sessionEnded(String clientId) {}
		};
	}
	
	public void start() throws ServerException {
		Logger.debug("Starting server on port ["+this.port+"]");
		stopped = false;
		new Thread(this, "Hub-Server").start();
		ThreadUtil.pause(1000); // Give the server a chance to start
		if (stopped) {
			throw new ServerException("Unable to start server");
		}
	}
	
	public void run() {
		try {
			try {
				serverSock = new ServerSocket(this.port);
			} catch(Throwable e) {
				Logger.error("Error starting service on port "+this.port+
						". Make sure the port number is larger than 1023 and that it is not"+
						" already in use by another service or application.", e);
				throw new ServerException(e);
			}
			while (true) {
				Socket sock = serverSock.accept(); // Blocks until client makes connection to server
				Logger.debug("Accepted connection from client ["+sock+"]");
				// Start a new thread for the client session lifecycle
				final ClientSession session = new ClientSession(sock, dataActivityMonitor);
				addSession(session);
				final int sessionNumber = nextSessionThreadNum();
				new Thread(new Runnable() {
					public void run() {
						try {
							String line;
							session.start();
							sessionLifecycleMonitor.sessionStarted(session.toString());
							Logger.debug("Session "+sessionNumber+" started (client="+session+")");
							dispatcher.register(session);
							InputStream clientIn = session.getInputStream();
							BufferedReader reader = new MonitoredReader(new InputStreamReader(clientIn), session.dataMonitor);
							while ((line = reader.readLine()) != null) { // Blocks until data received
								line = line.trim();
								Logger.debug("### Received message from client ["+line+"]");
								try {
									// We have received a message from the client so process it
									session.processClientMessage(line);
								} catch (Exception e) {
									Logger.error("Error processing client input: "+(e.getMessage() == null ? e : e.getMessage()), e);
								}
							}
						} catch (IOException e) {
							Logger.debug("Exception running session: "+e);
						}
						dispatcher.deregister(session);
						removeSession(session);
						sessionLifecycleMonitor.sessionEnded(session.toString());
						Logger.debug("Session "+sessionNumber+" ended (client="+session+")");
					}
				}, "Hub-Session-"+sessionNumber).start();
			}
		} catch (Exception e) {}
		stop();
	}
	
	private synchronized void addSession(ClientSession session) {
		this.activeSessions.add(session);
	}
	
	private synchronized void removeSession(ClientSession session) {
		this.activeSessions.remove(session);
	}
	
	public synchronized void stop() {
		if (stopped) return;
		stopped = true;
		dispatcher.dispose();
		Logger.debug("Dispatcher stopped");
		Logger.debug("Closing sockets...");
		if (serverSock != null) {
			try {
				for (Iterator<ClientSession> it = activeSessions.iterator(); it.hasNext();) {
					it.next().dispose();
				}
				serverSock.close();
				serverSock = null;
			} catch (IOException e1) {}
		}
		Logger.debug("Stopped server on port ["+this.port+"]");
	}

	public synchronized String[] listActiveSessions() {
		String[] active = new String[activeSessions.size()];
		int i=0;
		for (Iterator<ClientSession> it=activeSessions.iterator(); it.hasNext();) {
			active[i++] = it.next().toString();
		}
		return active;
	}
}
