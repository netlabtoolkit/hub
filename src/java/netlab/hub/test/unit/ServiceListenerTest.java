package netlab.hub.test.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import netlab.hub.core.ClientSession;
import netlab.hub.core.DispatcherPoller;
import netlab.hub.core.ServiceMessage;
import netlab.hub.test.mocks.MockSocket;

import org.junit.Test;

public class ServiceListenerTest {
/*
	@Test
	public void testServiceListener() throws Exception {
		ServiceListener l;
		
		l = new ServiceListener(new ServiceMessage("/service/test/test/config/listen /analogin/0"), null);
		assertEquals("/service/test/test/analogin/0", l.getPattern().toString());
		assertEquals(33, l.getDelay());
		assertTrue(l.isChangedValuesOnly());
		
		l = new ServiceListener(new ServiceMessage("/service/test/test/config/listen /analogin/0 20"), null);
		assertEquals("/service/test/test/analogin/0", l.getPattern().toString());
		assertEquals(50, l.getDelay());
		assertTrue(l.isChangedValuesOnly());
		
		l = new ServiceListener(new ServiceMessage("/service/test/test/config/listen /analogin/0 20 1"), null);
		assertEquals("/service/test/test/analogin/0", l.getPattern().toString());
		assertTrue(l.isChangedValuesOnly());
		
		l = new ServiceListener(new ServiceMessage("/service/test/test/config/listen /analogin/0 20 0"), null);
		assertEquals("/service/test/test/analogin/0", l.getPattern().toString());
		assertFalse(l.isChangedValuesOnly());
	}
	*/

	@Test
	public void testEqualsObject() throws Exception {
		ServiceMessage message1 = new ServiceMessage("/service/test/test/analogin/0 50");
		ServiceMessage message2 = new ServiceMessage("/service/test/test/digitalin/0 50");
		ClientSession session1 = new ClientSession(new MockSocket());
		ClientSession session2 = new ClientSession(new MockSocket());		
		
		assertTrue(new DispatcherPoller(message1, session1).equals(new DispatcherPoller(message1, session1)));
		assertFalse(new DispatcherPoller(message1, session1).equals(new DispatcherPoller(message1, session2)));
		assertFalse(new DispatcherPoller(message1, session1).equals(new DispatcherPoller(message2, session1)));
		assertFalse(new DispatcherPoller(message1, session1).equals(new DispatcherPoller(message2, session2)));
	}

}
