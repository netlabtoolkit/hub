package netlab.hub.plugins.xbee.test;

import netlab.hub.core.ServiceException;
import netlab.hub.plugins.xbee.RemoteXBee;
import netlab.hub.plugins.xbee.XBeeNetwork;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;

public class MockXBeeNetwork extends XBeeNetwork {
	
	public int baudRate;
	
	public void connect(String port, int baudRate) throws ServiceException {
		this.portName = port;
		this.baudRate = baudRate;
	}
	
	public String lookupPort(String namePattern) throws ServiceException {
		return namePattern;
	}

	public void processResponse(XBeeResponse arg0) {}

	public void setAnalogSample(String remoteId, int pin, int value) {
		RemoteXBee xbee = new RemoteXBee(remoteId);
		xbee.setAnalog(pin, value);
		put(xbee);
	}
	
	public void setDigitalSample(String remoteId, int pin, int value) {
		RemoteXBee xbee = new RemoteXBee(remoteId);
		xbee.setDigital(pin, value);
		put(xbee);
	}
	
	public void clearSamples() {
		xbees.clear();
	}
	
	public void digitalWrite(String remoteId, int pin, boolean value) {
		setDigitalSample(remoteId, pin, value ? 1 : 0);
	}
	
	public void put(RemoteXBee xbee) {
		xbees.put(xbee.getId(), xbee);
	}
	
	public RemoteXBee get(String id) {
		return xbees.get(id);
	}
	
	public void transmit(RemoteXBee state) {
		XBeeResponse response = null;
		if (series == 1) {
			response = new RxResponseIoSample();
			response.setApiId(ApiId.RX_16_IO_RESPONSE);
			
		} else {
			response = new ZNetRxIoSampleResponse();
			response.setApiId(ApiId.ZNET_IO_SAMPLE_RESPONSE);
		}
		processResponse(response);
	}

}
