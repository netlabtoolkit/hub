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

package netlab.hub.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtils {
	
	public static String getLocalMachineAddress() {
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			InetAddress addrs[] = InetAddress.getAllByName(hostName);
			for (InetAddress addr: addrs) {
				if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
					return addr.getHostAddress();
				}
			}
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {}
		return "Unknown";
	}
	
	public static String readFromUrl(URL url) {
		StringWriter output = new StringWriter();
		PrintWriter printer = new PrintWriter(output);
		try {
	        URLConnection conn = url.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                		conn.getInputStream()));
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	            printer.println(inputLine);
	        }
	        in.close();
		} catch (Exception e) {
			Logger.debug("Error calling home "+ e);
		}
		return output.toString();
	}

}
