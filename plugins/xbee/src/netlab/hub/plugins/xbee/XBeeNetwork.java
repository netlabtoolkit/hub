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

package netlab.hub.plugins.xbee;

import java.util.HashMap;
import java.util.Iterator;

import netlab.hub.core.ServiceException;
import netlab.hub.serial.SerialPort;
import netlab.hub.util.Logger;
import netlab.hub.util.ThreadUtil;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.RemoteAtRequest;
import com.rapplogic.xbee.api.RemoteAtResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.IoSample;
import com.rapplogic.xbee.api.wpan.RxBaseResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import com.rapplogic.xbee.util.DoubleByte;


public class XBeeNetwork implements PacketListener {
	
	/**
	 * The default baud rate for the serial port connection.
	 */
	public static final int DEFAULT_BAUD_RATE = 9600;
	
	protected HashMap<String, RemoteXBee> xbees = new HashMap<String, RemoteXBee>();
	protected String portName;
	protected XBee baseStation;
	protected int series = 2;
	
	/**
	 * 
	 */
	public XBeeNetwork() {
		super();
	}
	
	/**
	 * @param series
	 */
	public XBeeNetwork(int series) {
		this();
		this.series = series;
	}
	
	/**
	 * @param port
	 * @param baudRate
	 * @throws ServiceException
	 */
	public void connect(String portNamePattern, int baudRate) throws ServiceException {
		portName = null;
		try {
			String[] portNames = SerialPort.list(portNamePattern);
			if (portNames.length == 0) {
				throw new ServiceException("Could not find available serial port matching ["+portNamePattern+"]");
			}
			portName = portNames[0]; // In case of multiple matching ports, take the first one in the list
			Logger.info("Opening serial port connection to XBee base station "+portName+" (rate="+baudRate+")...");
			try {		
				baseStation = new XBee();
				baseStation.open(portName, baudRate);
				Logger.info("Serial port connection to XBee base station at port "+portName+" established.");
				ThreadUtil.pause(2000);
				//baseStation.addPacketListener(this);
				// Start the thread for processing the incoming messages. The packet listener
				// built in to the XBee class is unreliable.
				new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								processResponse(baseStation.getResponse());
							} catch (XBeeException e) {
								Logger.debug("Error fetching XBee response", e);
							}
							ThreadUtil.pause(10);
						}
					}
				}).start();
			} catch (XBeeException e) {
				throw new ServiceException("Error connecting to XBee", e);
			}
		} catch (Exception e) {
			Logger.debug("Error connecting to XBee base station through serial port", e);
		}
	}
	
	/**
	 * @return
	 */
	public boolean isConnected() {
		return baseStation != null && baseStation.isConnected();
	}
	
	/**
	 * @return
	 */
	public String getPortName() {
		return this.portName;
	}
	
	/* A packet has been received from the network so update the sample buffer
	 * if the packet contains samples. The method of extracting samples from
	 * the packet differs based on the XBee series number. This method cannot
	 * be properly refactored because the XBee API fails to define a sample
	 * interface or common superclass and a common iteration pattern for 
	 * series 1 and series 2 xbees.
	 * See http://code.google.com/p/xbee-api/wiki/DevelopersGuide
	 * @see com.rapplogic.xbee.api.PacketListener#processResponse(com.rapplogic.xbee.api.XBeeResponse)
	 */
	public synchronized void processResponse(XBeeResponse response) {
		switch (series) {
		case 1:
			if (response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
				if (response instanceof RxResponseIoSample) {
					RxResponseIoSample ioSample = (RxResponseIoSample)response;
					int[] addr = ((RxBaseResponse)response).getSourceAddress().getAddress();
					RemoteXBee xbee = getRemote(new DoubleByte(addr[0], addr[1]).get16BitValue());
					xbee.setRssi(ioSample.getRssi());
					for (IoSample sample: ioSample.getSamples()) {
						if (ioSample.containsAnalog()) {
							for (int pin=0; pin<xbee.getAnalogPinCount(); pin++) {
								Integer value = sample.getAnalog(pin);
								if (value == null) continue;
								xbee.setAnalogSample(pin, value);
								//System.out.println("Setting xbee addr="+xbee.getId()+" pin="+pin+" to value="+value);
							}
						}
						if (ioSample.containsDigital()) {
							for (int pin=0; pin<xbee.getDigitalPinCount(); pin++) {
								Boolean value = sample.isDigitalOn(pin);
								if (value == null) continue;
								xbee.setDigitalSample(pin, value == null || value.booleanValue() == false ? 0 : 1);
							}
						}
					}
				}
			}
			break;
		case 2:
		default:
			if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
				ZNetRxIoSampleResponse sample = (ZNetRxIoSampleResponse) response;
				RemoteXBee xbee = getRemote(sample.getRemoteAddress16().get16BitValue());
				// getRssi() is not supported in XBee API for XBee series 2 so we need to send out
				// a DB command to get the signal strength (RSSI) of the last hop.
				// See ZNetReceiverExample.java in the xbee api package.
                try {
                	baseStation.sendAsynchronous(new RemoteAtRequest(sample.getRemoteAddress16(), "DB"));
				} catch (XBeeException e) {
					Logger.debug(e);
				}
				if (sample.containsAnalog()) {
					for (int pin=0; pin<xbee.getAnalogPinCount(); pin++) {
						Integer value = sample.getAnalog(pin);
						if (value == null) continue;
						xbee.setAnalogSample(pin, value);
					}
				}
				if (sample.containsDigital()) {
					for (int pin=0; pin<xbee.getDigitalPinCount(); pin++) {
						Boolean value = sample.isDigitalOn(pin);
						if (value == null) continue;
						xbee.setDigitalSample(pin, value == null || value.booleanValue() == false ? 0 : 1);
					}
				}
			} else 
			if (response.getApiId() == ApiId.REMOTE_AT_RESPONSE) {
				RemoteAtResponse atResponse = (RemoteAtResponse)response;
				RemoteXBee xbee = getRemote(atResponse.getRemoteAddress16().get16BitValue());
				if ("DB".equals(atResponse.getCommand())) {
					int rssi = -((AtCommandResponse)atResponse).getValue()[0]; // DB returns negative db integer value
					xbee.setRssi(rssi);
				}
			}
		}
	} 
	
	public RemoteXBee getRemote(int remoteId) {
		String remoteIdStr = Integer.toString(remoteId, 16);
		RemoteXBee xbee = xbees.get(remoteIdStr);
		if (xbee == null) {
			xbee = new RemoteXBee(remoteIdStr);
			xbees.put(remoteIdStr, xbee);
			Logger.debug("Received initial contact from remote XBee at address "+remoteIdStr);
		}
		return xbee;
	}
	
	/**
	 * @return
	 */
	public synchronized String[] getAllRemoteIds() {
		String[] remoteIds = new String[xbees.size()];
		int i=0;
		for (Iterator<String> ids=xbees.keySet().iterator(); ids.hasNext();) {
			remoteIds[i++] = ids.next();
		}
		return remoteIds;
	}
	
	public synchronized RemoteXBee getRemoteXBee(String id) {
		return xbees.get(id);
	}
	
	public void digitalSend(int pin, boolean value) {
		XBeeAddress64 addr64 = XBeeAddress64.BROADCAST;
		// 5 is Digital Output High, 0 is Low
		RemoteAtRequest request = new RemoteAtRequest(addr64, "D"+pin, new int[] {(value ? 5 : 0)});
		try {
			baseStation.sendAsynchronous(request);
		} catch (XBeeException e) {
			Logger.warn("Error writing digital value to XBee: "+e);
		}
	}
	
	/**
	 * See http://code.google.com/p/xbee-api/wiki/DevelopersGuide
	 * @param remoteId the id of the target device as a hex string, or as "*" for broadcast to all devices
	 * @param pin
	 * @param value
	 */
	public void digitalSend(int pin, boolean value, String remoteId) {
		XBeeAddress64 addr64 = new XBeeAddress64(new int[8]);
		// 5 is Digital Output High, 0 is Low
		RemoteAtRequest request = new RemoteAtRequest(addr64, "D"+pin, new int[] {(value ? 5 : 0)});
		DoubleByte addr = new DoubleByte(Integer.parseInt(remoteId, 16));
		request.setRemoteAddr16(new XBeeAddress16(addr.getMsb(), addr.getLsb()));
		try {
			baseStation.sendAsynchronous(request);
		} catch (XBeeException e) {
			Logger.warn("Error writing digital value to XBee: "+e);
		}
	}
	
	/**
	 * 
	 */
	public void dispose() {
		if (baseStation != null) {
			baseStation.close();
			baseStation = null;
			ThreadUtil.pause(1000);
		}
	}
}
