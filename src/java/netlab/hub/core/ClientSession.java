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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import netlab.hub.util.Logger;
import netlab.hub.util.WildcardPatternMatch;

/**
 * @author ebranda
 */
public class ClientSession {
	
	Socket sock;
	IDataActivityMonitor dataMonitor;
	PrintWriter out;
	
	HashMap<String,Boolean> verboseResponse = new HashMap<String, Boolean>();
	ResponseMessage lastMessage;
	
	List<String> responseAddressFilterPatterns = new ArrayList<String>();
	
	List<ServiceMessage> outgoing = new ArrayList<ServiceMessage>();
	int outgoingBufferLimit = 10000;
	
	/**
	 * 
	 */
	public ClientSession(Socket sock) {
		this(sock, new IDataActivityMonitor() {
			public void dataReceived() {}
			public void dataSent() {}
		});
	}

	/**
	 * 
	 */
	public ClientSession(Socket sock, IDataActivityMonitor dm) {
		super();
		this.sock = sock;
		this.dataMonitor = dm;
	}
	
	public InputStream getInputStream() throws IOException {
		return sock.getInputStream();
	}
	
	public void dispose() {
		try {
			this.sock.close();
		} catch (IOException e) {
			Logger.debug("Error closing client socket", e);
		}
	}
	
	/**
	 * @param clientOut
	 */
	public void start() throws IOException {
		out = new PrintWriter(
				new OutputStreamWriter(
						new MonitoredOutputStream(sock.getOutputStream(), dataMonitor)));
	}
	
	public String toString() {
		return sock.getRemoteSocketAddress().toString();
		//return socket.getInetAddress()+":"+socket.getPort()
	}
	
	/**
	 * @param addressPattern
	 * @param v
	 */
	public void setVerboseResponse(String addressPattern, boolean v) {
		this.verboseResponse.put(addressPattern, v);
		//System.out.println(addressPattern+" "+v);
	}
	
	/**
	 * @param addressPattern
	 * @return
	 */
	public boolean isVerboseResponse(String address) {
		if (address == null) return false;
		if (verboseResponse.containsKey(address) && 
				verboseResponse.get(address)) {
			return true;
		}
		String key;
		for (Iterator<String> it=verboseResponse.keySet().iterator(); it.hasNext();) {
			key = it.next();
			if (WildcardPatternMatch.matches(key, address)) {
				return verboseResponse.get(key);
			}
		}
		return false;	
	}

	
	/**
	 * @param msg
	 * @throws ServiceException
	 */
	public void processClientMessage(String msg) throws ServiceException {
		dataMonitor.dataReceived();
		MessageBundle bundle = new MessageBundle(msg);
		while(bundle.hasMoreMessages()) {
			String message = bundle.nextMessage();
			ServiceMessage request = new ServiceMessage(message);
			if (!request.isValid()) {
				throw new ServiceException("Illegal message format ["+msg+"]");
			}
			addOutgoingMessage(message);
		}
	}
	
	/**
	 * @param msg
	 * @throws ServiceException
	 */
	private synchronized void addOutgoingMessage(String msg) throws ServiceException {
		// This should never happen because the Dispatcher is reading as fast as possible.
		if (outgoing.size() > outgoingBufferLimit) {
			throw new ServiceException("Outgoing message buffer limit exceeded." +
					"Please lower the rate at which your client is writing to the server.");
		}
		outgoing.add(new ServiceMessage(msg));
	}
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.IDispatchClient#getMessages()
	 */
	public synchronized List<ServiceMessage> getMessages() {
		List<ServiceMessage> messages = new ArrayList<ServiceMessage>();
		messages.addAll(this.outgoing);
		this.outgoing.clear();
		return messages;
	}
	
	/**
	 * @param response
	 */
	public void sendResponse(ServiceResponse response) {
		if (!response.isEmpty()) {
			if (isVerboseResponse(response.getMessage().getAddress()) || !response.getMessage().equals(lastMessage)) {
				boolean send = responseAddressFilterPatterns.isEmpty() ? true : false;
				for (Iterator<String> it=responseAddressFilterPatterns.iterator(); it.hasNext();) {
					if (WildcardPatternMatch.matches(it.next(), response.getMessage().getAddress())) {
						send = true;
						break;
					}
				}
				if (send) {
					boolean sent = response.send(this.out);
					if (sent) {
						dataMonitor.dataSent();
					}
					lastMessage = response.getMessage();
				}
			}
		}
	}

	/**
	 * @param pattern
	 */
	public synchronized void addResponseAddressFilterPattern(String pattern) {
		if (!responseAddressFilterPatterns.contains(pattern)) {
			responseAddressFilterPatterns.add(pattern);
		}
	}
	
	/**
	 * @return
	 */
	public List<String> getResponseAddressFilterPatterns() {
		return this.responseAddressFilterPatterns;
	}

}
