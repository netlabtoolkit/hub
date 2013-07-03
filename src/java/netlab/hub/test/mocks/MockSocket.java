package netlab.hub.test.mocks;

import java.io.OutputStream;
import java.net.Socket;

public class MockSocket extends Socket {
	
	OutputStream outputStream;
	
	public MockSocket() {
		this(System.out);
	}
	
	public MockSocket(OutputStream out) {
		super();
		this.outputStream = out;
	}
	
	public OutputStream getOutputStream() {
		return this.outputStream;
	}

}
