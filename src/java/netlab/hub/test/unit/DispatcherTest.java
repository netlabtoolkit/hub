package netlab.hub.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import netlab.hub.core.ClientSession;
import netlab.hub.core.Dispatcher;
import netlab.hub.core.DispatcherPoller;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceRegistry;
import netlab.hub.test.mocks.MockService;
import netlab.hub.test.mocks.MockSocket;
import netlab.hub.util.ThreadUtil;

import org.junit.Test;

public class DispatcherTest {
	
	@Test
	public void testRegister() throws Exception {
		
		ClientSession session1 = new ClientSession(new MockSocket());
		ClientSession session2 = new ClientSession(new MockSocket());
		DispatcherPoller poller = new DispatcherPoller(new ServiceMessage("/service/test/test/poll /analogin/0"), session1);
		
		Dispatcher d = new Dispatcher();
		d.start();
		
		d.register(session1);
		assertEquals(1, d.getClients().size());
		
		d.register(session2);
		assertEquals(2, d.getClients().size());
		
		d.deregister(session2);
		ThreadUtil.pause(200);
		assertEquals(1, d.getClients().size());
		assertTrue(d.getClients().get(0) == session1);
		
		d.getPollers().add(poller);
		d.deregister(session1);
		ThreadUtil.pause(200);
		assertEquals(0, d.getClients().size());
		assertEquals(0, d.getPollers().size());
	}
	
	@Test
	public void testProcessMessage() throws Exception {
		ServiceRegistry.register("/service/arduino/reader-writer", new MockService());
		Dispatcher d = new Dispatcher();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ClientSession client = new ClientSession(new MockSocket(out));
		client.start();
		
		ServiceMessage msg = new ServiceMessage("/service/arduino/reader-writer/analogin/0");
		d.processMessage(msg, client);
		assertEquals("/service/arduino/reader-writer/analogin/0 Hello", out.toString().trim());
		
		msg = new ServiceMessage("/service/arduino/reader-writer/poll /analogin/0");
		d.processMessage(msg, client);
		assertEquals(1, d.getPollers().size());
		d.getPollers().clear();
		msg = new ServiceMessage("/service/arduino/reader-writer/stoppoll /analogin/0");
		d.processMessage(msg, client);
		d.processMessage(msg, client);
		assertEquals(0, d.getPollers().size());
		
		d.getClients().clear();
		msg = new ServiceMessage("/service/arduino/reader-writer/poll /analogin/0");
		d.processMessage(msg, client);
		DispatcherPoller listener = d.getPollers().get(0);
		assertEquals(33, listener.getDelay());
		msg = new ServiceMessage("/service/arduino/reader-writer/config/pollsamplerate /analogin/0 20");
		d.processMessage(msg, client);
		assertEquals(50, listener.getDelay());
		/*
		msg = new ServiceMessage("/service/arduino/reader-writer/config/changedvaluesonly /analogin/0 1");
		d.processMessage(msg, client);
		assertFalse(client.isVerboseResponse("/service/arduino/reader-writer/analogin/0"));
		msg = new ServiceMessage("/service/arduino/reader-writer/config/changedvaluesonly /analogin/0 0");
		d.processMessage(msg, client);
		assertTrue(client.isVerboseResponse("/service/arduino/reader-writer/analogin/0"));
		*/
	}

}
