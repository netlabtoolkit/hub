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

package netlab.hub.plugins.tools.fileio;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.Logger;


/**
 * @author ebranda
 */
public class FileIOService extends Service {
	
	public static final int ERROR_BAD_FILENAME = 501;
	public static final int ERROR_MISSING_FILENAME = 502;
	public static final int ERROR_MISSING_DIRECTORY = 503;
	public static final int ERROR_DIRECTORY_NOT_FOUND = 504;
	public static final int ERROR_FILE_NOT_FOUND = 505;
	public static final int ERROR_READ_ERROR = 506;
	public static final int ERROR_WRITE_ERROR = 507;
	public static final int ERROR_UNSUPPORTED_COMMAND = 600;

	public static final String CMD_BASE = "base";
	public static final String CMD_FILENAME = "filename";
	public static final String CMD_GET = "get";
	public static final String CMD_PUT = "put";
	public static final String CMD_APPEND = "append";
	
	FileSystem fileStore;

	/**
	 * 
	 */
	public FileIOService() {
		super();
		this.fileStore = new FileSystem();
	}
	
	@Override
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if (request.getPath().isEmpty()) {
			throw new ServiceException("Illegal (empty) request");
		}
		String cmd = request.getPath().getFirst();
		if (CMD_FILENAME.equalsIgnoreCase(cmd)) {
			validateHasArgument(request);
			doFilename(request.getArgument(0), response);
		}
		else
		if (CMD_BASE.equalsIgnoreCase(cmd)) {
			validateHasArgument(request);
			doBase(request.getArgument(0), response);
		} else 
		if (checkPreconditions(response)) {
			if (CMD_GET.equalsIgnoreCase(cmd)) {
				doGet(response);
			}
			else
			if (CMD_PUT.equalsIgnoreCase(cmd)) {
				validateHasArgument(request);
				doPut(request, response);
			}
			else
			if (CMD_APPEND.equalsIgnoreCase(cmd)) {
				validateHasArgument(request);
				doAppend(request.getArgument(0), response);
			}
			else {
				sendError(ERROR_UNSUPPORTED_COMMAND, "Unsuppored command ["+request+"]", response);
			}
		}
	}
	
	private void validateHasArgument(ServiceMessage request) throws ServiceException {
		if (request.getArguments().isEmpty()) {
			throw new ServiceException("Argument missing");
		}
	}
	
	protected void doFilename(String name, ServiceResponse response) {
		try {
			Logger.debug("Setting filename to ["+name+"]");
			fileStore.setFilename(name);
		} catch (IOException e) {
			sendError(ERROR_BAD_FILENAME, "Illegal value for filename: "+name, response);
			return;
		}
	}
	
	protected boolean doBase(String path, ServiceResponse response) {
		try {
			File f = new File(path);
			Logger.debug("Accepted base path ["+path+"]");
			Logger.debug("Setting base directory to ["+f.getAbsolutePath()+"]");
			fileStore.setBaseDirectory(f);
			Logger.debug("Set base directory to ["+f.getAbsolutePath()+"]");
		} catch (FileNotFoundException e) {
			sendError(ERROR_DIRECTORY_NOT_FOUND, 
				"No such base directory: "+new File(path).getAbsolutePath(), response);
			return false;
		}
		return true;
	}
	
	protected boolean checkPreconditions(ServiceResponse response) {
		if (fileStore.getBaseDirectory() == null) {
			sendError(ERROR_MISSING_DIRECTORY, "No base directory specified", response);
			return false;
		}
		if (fileStore.getFilename() == null) {
			sendError(ERROR_MISSING_FILENAME, "No filename specified", response);
			return false;
		}
		return true;
	}
	
	protected void doGet(ServiceResponse response) {
		StringWriter results = new StringWriter();
		try {
			fileStore.get(new PrintWriter(results));
		} catch (FileNotFoundException e) {
			Logger.debug("No such file: "+e);
			response.write(new FileIOError(Integer.toString(ERROR_FILE_NOT_FOUND), "No such file: "+e).toString());
			return;
		} catch(IOException e) {
			sendError(ERROR_READ_ERROR, "Error reading file: "+e, response);
			return;
		}
		response.write(results.toString().trim());
	}
	
	protected void doPut(ServiceMessage request, ServiceResponse response) {
		try {
			String contents = request.getArgumentsAsString();
			Logger.debug("Writing ["+contents+"]");
			fileStore.put(contents.trim());
		} catch (IOException e) {
			sendError(ERROR_WRITE_ERROR, "Error writing file: "+e, response);
		}
	}
	
	protected void doAppend(String contents, ServiceResponse response) {
		try {
			Logger.debug("Appending ["+contents+"]");
			fileStore.append(contents);
		} catch (IOException e) {
			sendError(ERROR_WRITE_ERROR, "Error writing file: "+e, response);
		}
	}

	
	/**
	 * For unit testing
	 * @param store
	 */
	public void setFileStore(FileSystem store) {
		this.fileStore = store;
	}
	
	private void sendError(int code, String descr, ServiceResponse response) {
		Logger.error(descr);
		response.write(new FileIOError(Integer.toString(code), descr).toString());
	}

}
