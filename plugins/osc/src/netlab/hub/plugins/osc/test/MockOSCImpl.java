package netlab.hub.plugins.osc.test;

import java.util.ArrayList;
import java.util.List;

import netlab.hub.core.ServiceException;
import netlab.hub.plugins.osc.OSC;

public class MockOSCImpl implements OSC {
	
	public List<Integer> listenPorts = new ArrayList<Integer>();
	public String lastSendIp;
	public int lastSendPortNum;
	public Object[] lastSendArgs;
	public String lastSendAddress;

	public void dispose() {
	}

	public void listen(int portNum) throws ServiceException {
		listenPorts.add(portNum);
	}

	public void send(String address, Object[] args, String ip, int portNum) {
		lastSendAddress = address;
		lastSendArgs = args;
		lastSendIp = ip;
		lastSendPortNum = portNum;
	}

	public void stopListen(int portNum) {
		listenPorts.remove(portNum);
	}

}
