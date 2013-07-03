package netlab.hub.test.mocks;

import netlab.hub.core.ClientSession;
import netlab.hub.core.Dispatcher;
import netlab.hub.core.ServiceMessage;

public class MockDispatcher extends Dispatcher {
	
	ServiceMessage lastDispatch;

	public MockDispatcher() {
		super();
	}
	
	public void dispatch(ServiceMessage msg, ClientSession session) {
		lastDispatch = msg;
	}
	
	public ServiceMessage getLastDispatch() {
		return lastDispatch;
	}
}
