package netlab.hub.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import netlab.hub.core.ClientSession;
import netlab.hub.core.Dispatcher;
import netlab.hub.core.DispatcherAction;
import netlab.hub.core.DispatcherPoller;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceRegistry;
import netlab.hub.core.ServiceResponse;
import netlab.hub.test.mocks.MockService;
import netlab.hub.test.mocks.MockSocket;

import org.junit.Test;

public class DispatcherActionTest {
	
	@Test
	public void testBuildPollMessage() throws Exception {
		
		ServiceMessage msg;
		DispatcherAction action = new DispatcherAction() {
			public void perform(ServiceMessage message, ClientSession client,
					Dispatcher dispatcher) throws ServiceException {}
		};
		
		msg = new ServiceMessage("/service/group/test/poll /abc");
		assertEquals("/service/group/test/abc", action.argumentToMessage(msg, 0).toString());

		msg = new ServiceMessage("/service/group/test/poll {/abc xyz}");
		assertEquals("/service/group/test/abc xyz", action.argumentToMessage(msg, 0).toString());

		msg = new ServiceMessage("/service/group/test/poll /abc/{/def}");
		assertEquals("/service/group/test/abc/{/def}", action.argumentToMessage(msg, 0).toString());
		//   /service/group/test/abc//def

		msg = new ServiceMessage("/service/group/test/poll {/abc/{/def} xyz}");
		assertEquals("/service/group/test/abc/{/def} xyz", action.argumentToMessage(msg, 0).toString());
		//   /service/group/test/abc//def
	}
	
	@Test
	public void testFactory() throws Exception {
		
		class MockDispatcher extends Dispatcher {
			DispatcherPoller poller;
			public MockDispatcher() {
				super();
			}
			public void registerPoller(DispatcherPoller p) {
				poller = p;
			}
			public boolean deregisterPoller(DispatcherPoller p) {
				if (poller != null && poller.getPattern().equals(p.getPattern())) {
					poller = null;
					return true;
				}
				return false;
			}
			public DispatcherPoller getRegisteredPoller(ClientSession client, ServiceMessage pattern) {
				try {
					if (poller != null && poller.equals(new DispatcherPoller(pattern, client))) {
						return poller;
					}
				} catch (Exception e) {}
				return null;
			}
		}
		
		DispatcherAction action;
		ClientSession client = new ClientSession(new MockSocket(System.out));
		ServiceRegistry.register("/service/group/test", new MockService());
		MockDispatcher dispatcher = new MockDispatcher();
		
		ServiceMessage msg;
		
		msg = new ServiceMessage("/service/group/test/poll /abc");
		action = DispatcherAction.parse(msg);
		assertEquals(action.getClass().getName(), "netlab.hub.core.PollAction");
		action.perform(msg, client, dispatcher);
		assertNotNull(dispatcher.poller);
		assertEquals(30, dispatcher.poller.getSampleRate());
		assertEquals("/service/group/test/abc", dispatcher.poller.getPattern().toString());
		
		msg = new ServiceMessage("/service/group/test/pollsamplerate /def 10");
		action = DispatcherAction.parse(msg);
		assertEquals(action.getClass().getName(), "netlab.hub.core.PollSampleRateAction");
		action.perform(msg, client, dispatcher);
		assertEquals(30, dispatcher.poller.getSampleRate());
		msg = new ServiceMessage("/service/group/test/pollsamplerate /abc 10");
		action.perform(msg, client, dispatcher);
		assertEquals(10, dispatcher.poller.getSampleRate());
		
		msg = new ServiceMessage("/service/group/test/stoppoll /def");
		action = DispatcherAction.parse(msg);
		assertEquals(action.getClass().getName(), "netlab.hub.core.StopPollLoopAction");
		action.perform(msg, client, dispatcher);
		assertEquals("/service/group/test/abc", dispatcher.poller.getPattern().toString());
		msg = new ServiceMessage("/service/group/test/stoppoll /abc");
		action.perform(msg, client, dispatcher);
		assertNull(dispatcher.poller);
		
		msg = new ServiceMessage("/service/group/test/setverbose /abc 1");
		action = DispatcherAction.parse(msg);
		assertEquals(action.getClass().getName(), "netlab.hub.core.SetVerboseAction");
		action.perform(msg, client, dispatcher);
		assertTrue(client.isVerboseResponse("/service/group/test/abc"));
		msg = new ServiceMessage("/service/group/test/setverbose /def 0");
		action.perform(msg, client, dispatcher);
		assertTrue(client.isVerboseResponse("/service/group/test/abc"));
		msg = new ServiceMessage("/service/group/test/setverbose /abc 0");
		action.perform(msg, client, dispatcher);
		assertFalse(client.isVerboseResponse("/service/group/test/abc"));
		
		msg = new ServiceMessage("/service/group/test/filterresponse /analogin/1");
		action = DispatcherAction.parse(msg);
		assertEquals(action.getClass().getName(), "netlab.hub.core.SetResponseFilterAction");
		assertTrue(client.getResponseAddressFilterPatterns().isEmpty());
		//
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		client = new ClientSession(new MockSocket(out));
		client.start();
		ServiceResponse response = new ServiceResponse(new ServiceMessage("/service/group/test/analogin/1 arg1"), client);
		response.write("arg1");
		assertEquals("/service/group/test/analogin/1 arg1", out.toString().trim());
		//
		response.write("x"); // To clear the state of the client lastMessage field
		out.reset();
		action.perform(msg, client, dispatcher);
		assertEquals(1, client.getResponseAddressFilterPatterns().size());
		response.write("arg1");
		assertEquals("/service/group/test/analogin/1 arg1", out.toString().trim());
		//
		response.write("x"); // To clear the state of the client lastMessage field
		out.reset();
		action.perform(msg, client, dispatcher);
		response = new ServiceResponse(new ServiceMessage("/service/group/test/analogin/2 arg1"), client);
		response.write("arg1");
		assertEquals(0, out.toString().trim().length());
		//
		response.write("x"); // To clear the state of the client lastMessage field
		out.reset();
		response = new ServiceResponse(new ServiceMessage("/service/group/test/analogin/1 arg1"), client);
		response.write("arg1");
		assertEquals("/service/group/test/analogin/1 arg1", out.toString().trim());
		//
		response.write("x"); // To clear the state of the client lastMessage field
		out.reset();
		msg = new ServiceMessage("/service/group/test/filterresponse /analogin/*");
		action = DispatcherAction.parse(msg);
		action.perform(msg, client, dispatcher);
		response = new ServiceResponse(new ServiceMessage("/service/group/test/analogin/1"), client);
		response.write("arg1");
		assertEquals("/service/group/test/analogin/1 arg1", out.toString().trim());
		out.reset();
		response = new ServiceResponse(new ServiceMessage("/service/group/test/analogout/1"), client);
		response.write("arg1");
		assertEquals(0, out.toString().trim().length());
		out.reset();
		response = new ServiceResponse(new ServiceMessage("/service/group/test/analogin/99"), client);
		response.write("arg1");
		assertEquals("/service/group/test/analogin/99 arg1", out.toString().trim());
		assertEquals(2, client.getResponseAddressFilterPatterns().size());
		
		
		action = DispatcherAction.parse(new ServiceMessage("/service/group/test/unknown"));
		assertEquals(action.getClass().getName(), "netlab.hub.core.DefaultDispatchAction");
		
	}
}
