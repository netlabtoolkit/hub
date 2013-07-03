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

package netlab.hub.plugins.linkm;

import java.io.IOException;

import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.Logger;
import netlab.hub.util.MathUtils;
import thingm.linkm.LinkM;

public class LinkMService extends Service {
	
	private LinkM device;
	
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#process(netlab.hub.core.ServiceMessage, netlab.hub.core.ServiceResponse)
	 */
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if ("connect".equals(request.getPath().getLast())) {
			commandConnect(request, response);
		} else {
			if (device == null) {
				throw new ServiceException("Device not connected. Send connect command first.");
			}
			String command = request.getPath().getFirst();
			if ("fadetorgb".equals(command)) {
				commandFadeToRGB(request, response);
			} else if ("setfadespeed".equals(command)) {
				commandSetFadeSpeed(request, response);
			} else {
				Logger.warn("Unsupported LinkM command ["+command+"]");
			}
		}
	}
	
	public void commandConnect(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if (device == null) {
			device = new LinkM();
			try {
				device.open();
				Logger.info("Connection to LinkM device established");
			} catch (IOException e) {
				device = null;
				if (Logger.isDebug()) {
					e.printStackTrace();
				}
				throw new ServiceException("Error connecting to LinkM. Make sure the hardware is connected.");
			}
		}
		response.write("OK");
	}
	
	public void commandFadeToRGB(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if (request.getArguments().size() != 4) {
			Logger.debug("Incorrect arguments to fadetorgb (should be [address, r, g, b])");
			return;
		}
		int address = request.argInt(0, 0);
		int r = request.argInt(1, 0);
		int g = request.argInt(2, 0);
		int b = request.argInt(3, 0);
		try {
			r = MathUtils.clamp(0, 255, r);
			g = MathUtils.clamp(0, 255, g);
			b = MathUtils.clamp(0, 255, b);
			device.fadeToRGB(address, r, g, b);
		} catch (Exception e) {
			throw new ServiceException("Error handling command fadetorgb", e);
		}
	}
	
	public void commandSetFadeSpeed(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if (request.getArguments().size() != 2) {
			Logger.debug("Incorrect arguments to setfadespeed (should be [address, speed])");
			return;
		}
		int address = request.argInt(0, 0);
		int speed = request.argInt(1, 1);
		try {
			speed = MathUtils.clamp(1, 255, speed);
			device.setFadeSpeed(address, speed);
		} catch (Exception e) {
			throw new ServiceException("Error handling command setfadespeed", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#dispose()
	 */
	public void dispose() throws ServiceException {
		if (device != null) {
			device.close();
			device = null;
		}
	}

}
