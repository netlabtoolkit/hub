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

package netlab.hub.processing.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import processing.core.PApplet;
import processing.net.Client;

/**
 * Convenience class that helps a Processing sketch to act as a Hub client. 
 * Provides high level methods for creating a socket connection and for
 * reading from and writing to the Hub. Use the write() method for sending
 * OSC strings to the Hub. Define a handleHubInputMethod() method to
 * processing incoming messages received from the Hub.
 * 
 */
public class HubClient extends Client {
	
	public static final int DEFAULT_PORT = 51000;
	public static final String LOCALHOST = "127.0.0.1";
	
	boolean warnedOnFailure = false;
			
	public HubClient(PApplet applet) {
		this(applet, DEFAULT_PORT); 
	}
		
	public HubClient(PApplet applet, int port) {
		this(applet, LOCALHOST, port); 
	}
	
	public HubClient(PApplet applet, String ip, int port) {
		super(applet, ip, port); 
		warnedOnFailure = false;
		try {
			final Method handleHubInputMethod = applet.getClass().getMethod("hubDataReceived", new Class[] { String.class, String[].class });
			final PApplet pApplet = applet;
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (available() > 0) {
							ServiceMessage msg = new ServiceMessage(readString());
							try {
								handleHubInputMethod.invoke(pApplet, new Object[] {msg.getAbsoluteAddressString(), msg.getArgumentsAsStringArray()});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						try {Thread.sleep(10);} catch (Exception e) {}
					}
				}
			}).start();
		} catch (Exception e) {}
	}

		
	public void write(String value) {
		if (value != null) {
			if (super.active())
				super.write(value);
			if (super.active())
				super.write("\n");
			if (!super.active() && !warnedOnFailure) {
				PApplet.println("The connection to the Hub was broken for some reason.");
				PApplet.println("You should shut down your Processing sketch, restart the Hub, and then restart your sketch.");
				warnedOnFailure = true;
			}
		}
	}
	
	public static void print(String command, String[] values) {
		PApplet.print(command+" "); 
	    for (int i=0; i<values.length; i++) {
	    	PApplet.print(values[i]);
	      if (i < values.length-1) PApplet.print(" ");
	    }
	    PApplet.println("");
	}
	
}


class ServiceMessage {
	
	String original;
	
	LinkedList<String> absoluteAddress;
	String absoluteAddressString;
	
	LinkedList<String> serviceAddress;
	String serviceAddressString;
	
	String pathString;
	String pathStringWithArgs;
	LinkedList<String> path;
	
	LinkedList<String> arguments;
	
	public ServiceMessage(String str) {
		parse(str);
	}
	
	/**
	 * Helper method to parse a message string received from client.
	 */
	public void parse(String str) {
		if (str == null) {
			return;
		}
		original = str.trim();
		absoluteAddress = new LinkedList<String>();
		serviceAddress = new LinkedList<String>();
		pathString = null;
		pathStringWithArgs = null;
		path = new LinkedList<String>();
		arguments = new LinkedList<String>();
		
		if (original.length() == 0) {
			return;
		}
		
		String absoluteAddressString;
		String argsString = null;
		if (original.indexOf(" ") > -1) {
			int separatorIdx = original.indexOf(" ");
			absoluteAddressString = original.substring(0, separatorIdx).trim();
			argsString = original.substring(separatorIdx).trim();
		} else {
			absoluteAddressString = original;
		}
				
		// Parse the path
		if (absoluteAddressString.endsWith("/")) {
			absoluteAddressString = absoluteAddressString.substring(0, absoluteAddressString.length() - 1);
		}
		this.absoluteAddressString = absoluteAddressString;
		if (absoluteAddressString.startsWith("/")) {
			absoluteAddressString = absoluteAddressString.substring(1);
		}
		String[] absAddrSegments = absoluteAddressString.split("/");
		for (int i=0; i<absAddrSegments.length; i++) {
			absoluteAddress.add(absAddrSegments[i]);
		}
		StringBuffer serviceAddressSb = new StringBuffer();
		for (int i=0; i<Math.min(3, absAddrSegments.length); i++) {
			serviceAddress.add(absAddrSegments[i]);
			serviceAddressSb.append("/").append(absAddrSegments[i]);
		}
		serviceAddressString = serviceAddressSb.toString();
		StringBuffer pathSb = new StringBuffer();
		for (int i=3; i<absAddrSegments.length; i++) {
			path.add(absAddrSegments[i]);
			pathSb.append("/").append(absAddrSegments[i]);
		}
		this.pathString = pathSb.toString();
		this.pathStringWithArgs = argsString == null ? this.pathString : pathSb.append(" ").append(argsString).toString();
		if (argsString != null) {
			parseArguments(argsString);
		}
	}
	
	protected void parseArguments(String args) {
		if (args != null) {
			arguments.clear();
			char[] chars = args.toCharArray();
			char c;
			boolean insideString = false;
			boolean cdata = false;
			boolean processArg = false;
			StringBuffer arg = new StringBuffer();
			for(int i=0; i<chars.length; i++) {
				c = chars[i];
				if (c == '{') {
					insideString = true;
					cdata = true;
				} else if (c == '"' && !insideString) {
					insideString = true;
					cdata = false;
				} else if (c == '}') {
					insideString = false;
					cdata = false;
					processArg = true;
				} else if (c == '"' && insideString && !cdata) {
					insideString = false;
					processArg = true;
				} else if (c == ' ') {
					if (insideString) {
						arg.append(c);
					} else if (arg.length() > 0) {
						processArg = true;
					}
				} else {
					arg.append(c);
					if (i == chars.length-1) {
						processArg = true;
					}
				}
				if (processArg) {
					arguments.add(arg.toString());
					processArg = false;
					arg = new StringBuffer();
				}
			}
		}
	}
	
	public boolean isValid() {
		return serviceAddress.size() == 3;
	}
	
	public String toString() {
		return original;
	}
	
	public String debug() {
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		out.print("ORIGINAL: "); out.println(this);
		out.print("PATH STRING: "); out.println(pathString);
		out.println("PATH SEGMENTS: ");
		for (Iterator<String> it = path.iterator(); it.hasNext();) {
			out.print("  "); out.println(it.next());
		}
		out.println("ARGUMENTS: ");
		for (Iterator<String> it = arguments.iterator(); it.hasNext();) {
			out.print("  "); out.println(it.next());
		}
		return buffer.toString();
	}

	/**
	 * @return
	 */
	public boolean isConfig() {
		try {
			return "config".equals(path.getFirst()) ||
					"nlhubconfig".equals(path.getFirst()); // nlhubconfig supported for legacy
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	public boolean isHubConfig() {
		try {
			return isConfig() && getServiceAddress().get(1).equals("hub");
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	/**
	 * Returns the segments corresponding to the address
	 * portion of the client message (the first three segments). 
	 * 
	 * @return the address segments
	 * @see getServiceAddressString
	 */
	public LinkedList<String> getServiceAddress() {
		return this.serviceAddress;
	}
	
	/**
	 * Returns the address portion of the client message
	 * as a String. For the client message
	 * "/service/core/hello/say World", the address portion
	 * would be "/service/core/hello".
	 * 
	 * @return the address
	 */
	public String getServiceAddressString() {
		return this.serviceAddressString;
	}
	
	
	public LinkedList<String> getAbsoluteAddress() {
		return this.absoluteAddress;
	}
	
	/**
	 * Returns the full address portion of the client message
	 * as a String (without args). For the client message
	 * "/service/core/hello/say World", the address portion
	 * would be "/service/core/hello/say".
	 * 
	 * @return the address
	 */
	public String getAbsoluteAddressString() {
		return this.absoluteAddressString;
	}

	/**
	 * Returns the segments in the client message
	 * after the service address.
	 * 
	 * @return the segments
	 * @see getPathString
	 */
	public LinkedList<String> getPath() {
		return this.path;
	}
	
	/**
	 * Returns the path element at the specified position.
	 * For the client message "/service/core/hello/say/multiple"
	 * the path would be "/say/multiple" and the path element 0
	 * would be "say".
	 * 
	 * @param index
	 * @return the element at the specified position
	 */
	public String getPathElement(int index) {
		try {
			return this.path.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Returns the path portion of the client message.
	 * For the client message "/service/core/hello/say/multiple"
	 * the path would be "/say/multiple".
	 * 
	 * @param includeArgs - specifies whether the message arguments should be included
	 * @return
	 */
	public String getPathString(boolean includeArgs) {
		return includeArgs ? this.pathStringWithArgs : this.pathString;
	}
	
	public String getPathString() {
		return getPathString(false);
	}
	
	public String getPathString(boolean includeArgs, int offset) {
		if (offset > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i=offset; i<path.size(); i++) {
				sb.append("/").append(path.get(i));
			}
			return sb.toString();
		} else {
			return getPathString(includeArgs);
		}
	}

	/**
	 * Returns the arguments sent with the original message.
	 * 
	 * @return the argument Strings
	 */
	public LinkedList<String> getArguments() {
		return this.arguments;
	}
	
	/**
	 * Returns the argument at the specified position.
	 * 
	 * @return the argument String, or null of there is no argument at the specified position.
	 */
	public String getArgument(int index) {
		try {
			return this.arguments.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public String arg(int index) {
		return getArgument(index);
	}
	
	public int argInt(int index) throws Exception {
		return (Integer)getArgument(index, Integer.class);
	}
	
	public float argFloat(int index) throws Exception {
		return (Float)getArgument(index, Float.class);
	}
	
	public double argDouble(int index) throws Exception {
		return (Double)getArgument(index, Double.class);
	}
	
	@SuppressWarnings("unchecked")
	public Object getArgument(int index, Class cls) throws Exception {
		String arg = getArgument(index);
		if (arg == null) throw new Exception("No argument ["+index+"]");
		try {
			if (cls == Integer.class) {
				return Integer.parseInt(arg);
			} else if (cls == Float.class) {
				return Float.parseFloat(arg);
			} else if (cls == Double.class) {
				return Double.parseDouble(arg);
			} else {
				return arg;
			}
		} catch (Throwable t) {
			throw new Exception(t);
		}
	}
	
	public boolean hasArgument(int index) {
		return getArgument(index) != null;
	}
	
	/**
	 * @return
	 */
	public String getArgumentsAsString() {
		StringBuffer sb = new StringBuffer();
		getArguments(sb);
		return sb.toString();
	}
	
	/**
	 * @param sb
	 */
	public void getArguments(StringBuffer sb) {
		for (Iterator<String> it=arguments.iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(" ");
			}
		}
	}
	
	/**
	 * @return
	 */
	public String[] getArgumentsAsStringArray() {
		String[] args = new String[arguments.size()];
		for (int i=0; i<arguments.size(); i++) {
			args[i] = getArgument(i);
		}
		return args;
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public boolean equals(Object other) {
		if (other instanceof ServiceMessage) {
			return ((ServiceMessage)other).toString().equals(toString());
		}
		return false;
	}
	
}