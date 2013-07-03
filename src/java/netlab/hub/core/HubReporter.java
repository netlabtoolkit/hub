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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import netlab.hub.util.Logger;

public class HubReporter {
	
	Config config;
	
	public HubReporter(Config config) {
		this.config = config;
	}
	
	public String callHome() {
		//Logger.info("Calling home to URL "+callbackUrl);
		try {
			String version = URLEncoder.encode(Config.getAppVersion()+" ("+Config.getAppBuild()+")", "UTF-8");
			URL url = new URL(Config.getCallHomeUrl() + "?hub_version="+version);
			return readFromUrl(url);
		} catch (Exception e) {
			Logger.error("Illegal callback URL: "+Config.getCallHomeUrl());
			return null;
		}
	}
	
	public String readFromUrl(URL url) {
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
