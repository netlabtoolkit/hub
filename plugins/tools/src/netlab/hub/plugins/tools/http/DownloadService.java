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

package netlab.hub.plugins.tools.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.Logger;

public class DownloadService extends Service {
	
	File directory;
	int timeoutMillis = -1;
	
	public DownloadService() {
		super();
	}
	
	/**
	 * Handler for auto-dispatching of command /localpath
	 * 
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandLocalpath(ServiceMessage request, ServiceResponse response, String dir) throws ServiceException {
		if (dir == null) {
			throw new ServiceException("Missing directory path argument");
		}
		File directory = new File(dir);
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				throw new ServiceException("Could not create directory "+dir);
			}
		}
		this.directory = directory;
		Logger.info("Directory set to ["+this.directory.getAbsolutePath()+"]");
	}
	
	/**
	 * Handler for auto-dispatching of command /timeout
	 * 
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandTimeout(ServiceMessage request, ServiceResponse response, Integer timeoutValue) throws ServiceException {
		if (timeoutValue == null) {
			throw new ServiceException("Missing timeout value argument");
		}
		this.timeoutMillis = timeoutValue * 1000;
		Logger.info("Timeout value set to ["+(timeoutMillis/1000)+"]");
	}
	
	/**
	 * Handler for auto-dispatching of command /get [url]
	 * 
	 * @param request
	 * @param response
	 * @param url
	 * @throws ServiceException
	 */
	public void commandGet(ServiceMessage request, ServiceResponse response, String url) throws ServiceException {
		if (url == null) {
			throw new ServiceException("Missing url argument [0]");
		}
		// If local directory has been specified then download 
		// the resource to a local file.
		if (this.directory != null) {
			String filename = url.substring(url.lastIndexOf("/"));
			downloadFile(response, url, this.directory, filename);
		} else {
			downloadSource(response, url);
		}
	}
	
	/**
	 * Handler for auto-dispatching of command /get [url] [filename]
	 * 
	 * @param request
	 * @param response
	 * @param url
	 * @param filename
	 * @throws ServiceException
	 */
	public void commandGet(ServiceMessage request, ServiceResponse response, String url, String filename) throws ServiceException {
		if (this.directory == null) {
			throw new ServiceException("Must call /localpath command first");
		}
		if (url == null) {
			throw new ServiceException("Missing url argument [0]");
		}
		downloadFile(response, url, this.directory, filename);
	}
	
	/**
	 * @param response
	 * @param url
	 */
	public void downloadSource(ServiceResponse response, String url) {
		BufferedReader in = null;
		try {
			try {
				in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			} catch(FileNotFoundException e) {
				Logger.error("404 - Resource not found ["+url+"]");
			}
			if (in != null) {
				StringWriter output = new StringWriter();
				PrintWriter printer = new PrintWriter(output);
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					printer.println(inputLine);
				}
				response.write(output.toString());
			}
		} catch(Exception e) {
			Logger.error("Error reading URL contents", e);
		} finally {
			if (in != null) try { in.close(); } catch(Exception e) {}
		}
	}
	
	/**
	 * @param response
	 * @param url
	 * @param dir
	 * @param filename
	 */
	public void downloadFile(ServiceResponse response, String url, File dir, String filename) {
		final ServiceResponse out = response;
		final String theUrl = url;
		final String fn = filename;
		final File directory = this.directory;
		final int timeout = this.timeoutMillis;
		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedInputStream in = new BufferedInputStream(new URL(theUrl).openStream());
					File outputFile = new File(directory, fn);
					FileOutputStream fos = new FileOutputStream(outputFile);
					BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
					byte[] data = new byte[1024];
					int x=0;
					long start = System.currentTimeMillis();
					boolean timedOut = false;
					while((x=in.read(data,0,1024))>=0) {
						if (timeout > 0 && (System.currentTimeMillis() - start) > timeout) {
							timedOut = true;
							break;
						}
						bout.write(data,0,x);
					}
					bout.close();
					in.close();
					if (timedOut) {
						out.write(new String[]{theUrl, "TIMEOUT"});
					} else {
						Logger.debug("Wrote file ["+outputFile.getAbsolutePath()+"]");
						out.write(new String[]{theUrl, "OK"});
					}
				} catch (MalformedURLException e) {
					Logger.error("Illegal resource URL: "+theUrl);
					out.write(new String[]{theUrl, "ERROR"});
				} catch (IOException e) {
					Logger.error("Error fetching resource", e);
					out.write(new String[]{theUrl, "ERROR"});
				}
			}
						
		}).start();
	}

}
