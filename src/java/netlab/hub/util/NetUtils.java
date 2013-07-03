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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class NetUtils {

	public static void tryBind(int port) throws Exception {
		ServerSocket tempSocket = null;
		tempSocket = new ServerSocket(port);
		if (tempSocket != null)
			tempSocket.close();
	}

	public static void verifyRouteToHost(String sendAddress) throws Exception {
		InetAddress host = InetAddress.getByName(sendAddress);
		if (!host.isReachable(5000)) {
			throw new IOException("Host is not reachable");
		}
	}

}
