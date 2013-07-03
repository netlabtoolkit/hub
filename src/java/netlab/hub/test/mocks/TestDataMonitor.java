package netlab.hub.test.mocks;

import netlab.hub.core.IDataActivityMonitor;

public class TestDataMonitor implements IDataActivityMonitor {
	
	boolean sent, received;
	
	public void reset() {
		sent = false;
		received = false;
	}

	public void dataReceived() {
		received = true;
	}

	public void dataSent() {
		sent = true;
	}
	
	public boolean received() {
		return received;
	}
	
	public boolean sent() {
		return sent;
	}

}
