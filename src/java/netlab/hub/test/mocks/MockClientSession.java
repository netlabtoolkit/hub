package netlab.hub.test.mocks;

import java.io.IOException;

import netlab.hub.core.ClientSession;

public class MockClientSession extends ClientSession {
	
	public MockClientSession() throws IOException {
		super(null, new TestDataMonitor());
	}

}
