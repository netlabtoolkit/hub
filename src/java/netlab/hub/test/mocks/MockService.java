package netlab.hub.test.mocks;

import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;

public class MockService extends Service {
	
	ServiceMessage lastMessageFromClient;

	@Override
	public void process(ServiceMessage msg, ServiceResponse response) throws ServiceException {
		this.lastMessageFromClient = msg;
		response.write("Hello");
	}
	
	public ServiceMessage getLastMessageFromClient() {
		return lastMessageFromClient;
	}

}
