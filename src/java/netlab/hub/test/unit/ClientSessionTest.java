package netlab.hub.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import netlab.hub.core.ClientSession;
import netlab.hub.core.IResponseFilter;
import netlab.hub.core.ResponseMessage;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceRegistry;
import netlab.hub.core.ServiceResponse;
import netlab.hub.test.mocks.MockDispatcher;
import netlab.hub.test.mocks.MockSocket;
import netlab.hub.test.mocks.TestDataMonitor;
import netlab.hub.test.mocks.TestSessionLifecycleMonitor;

import org.junit.Before;
import org.junit.Test;

public class ClientSessionTest {
	
	TestDataMonitor dm ;
	TestSessionLifecycleMonitor lm;
	ServiceRegistry services;
	MockDispatcher dispatcher;
	
	@Before
	public void setup() {
		dm = new TestDataMonitor();
		lm = new TestSessionLifecycleMonitor();
		dispatcher = new MockDispatcher();
	}

	@Test
	public void testProcessClientMessage() throws Exception {		
		String msg = "/service/arduino/reader-writer";
		ClientSession session = new ClientSession(new MockSocket(System.out), dm);
		session.start();
		session.processClientMessage(msg);
		assertTrue(session.getMessages().contains(new ServiceMessage(msg)));
	}

	@Test
	public void testSendResponse() throws Exception {
		String msg = "/service/arduino/reader-writer";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ClientSession session = new ClientSession(new MockSocket(out), dm);
		session.start();
		
		ServiceResponse response = new ServiceResponse(new ServiceMessage(msg), session);
		response.write(1);
		assertEquals(msg+" 1", out.toString().trim());
		
		out.reset();
		response = new ServiceResponse(new ServiceMessage(msg), session);
		response.write("abc");
		assertEquals(msg+" abc", out.toString().trim());
		
		out.reset();
		response = new ServiceResponse(new ServiceMessage(msg), session);
		String[] returns = {"abc", "def"};
		response.write(returns);
		assertEquals(msg+" abc def", out.toString().trim());
		
		out.reset();
		response = new ServiceResponse(new ServiceMessage(msg), session);
		response.write("abc def");
		assertEquals(msg+" {abc def}", out.toString().trim());
		
		// Now test filters
		
		out.reset();
		response = new ServiceResponse(new ServiceMessage(msg), session);
		response.addFilter(new IResponseFilter() {
			public void apply(ResponseMessage m) {
				m.suppressOutput();
			}
		});
		response.write("abc");
		assertEquals(0, out.toString().trim().length());
		
		out.reset();
		response = new ServiceResponse(new ServiceMessage(msg), session);
		response.addFilter(new IResponseFilter() {
			public void apply(ResponseMessage m) {
				m.getArguments().clear();
			}
		});
		response.write("abc");
		assertEquals(msg, out.toString().trim());
		
		out.reset();
		response = new ServiceResponse(new ServiceMessage(msg), session);
		response.addFilter(new IResponseFilter() {
			public void apply(ResponseMessage m) {
				m.getArguments().add("def");
			}
		});
		response.write("abc");
		assertEquals(msg+" abc def", out.toString().trim());
		
		out.reset();
		response = new ServiceResponse(new ServiceMessage(msg), session);
		response.addFilter(new IResponseFilter() {
			public void apply(ResponseMessage m) {
				m.getArguments().add("def");
			}
		});
		response.write("abc");
		assertEquals(msg+" abc def", out.toString().trim());
		
	}

}
