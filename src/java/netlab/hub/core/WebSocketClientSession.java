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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.webbitserver.WebSocketConnection;

import netlab.hub.util.Logger;

public class WebSocketClientSession {
	
	WebSocketConnection webSock;
	Socket hubSock;
	PrintWriter hubWriter;
	
	public WebSocketClientSession(WebSocketConnection webSock, Socket hubSock) throws IOException {
		this.webSock = webSock;
		this.hubSock = hubSock;
		this.hubWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.hubSock.getOutputStream())));
		// Set up socket listener for returning messages back to client websocket
		final BufferedReader hubReader = new BufferedReader(new InputStreamReader(this.hubSock.getInputStream()));
		final WebSocketConnection clientWs = this.webSock;
		new Thread(new Runnable() {
			public void run() {
				String fromHub;
				try {
					while ((fromHub = hubReader.readLine()) != null) {
						clientWs.send(fromHub);
					}
				} catch (Exception e) {
				}
			}
		}).start();
	}
	
	public boolean isFor(WebSocketConnection webSock) {
		return webSock == this.webSock;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof WebSocketClientSession)) return false;
		return ((WebSocketClientSession)other).isFor(this.webSock);
	}
	
	public void sendToHub(String message) {
        if (message.length() > 0) {
        	hubWriter.println(message);
			hubWriter.flush();
        }
	}
	
	public void close() {
		webSock.close();
		try {
			this.hubSock.close();
		} catch (IOException e) {
			// TODO ?
		}
		Logger.debug("Closed WebSocket connection");
	}

}
