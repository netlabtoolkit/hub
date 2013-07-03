package netlab.hub.plugins.osc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

import netlab.hub.core.ClientSession;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.plugins.osc.OSCService;
import netlab.hub.plugins.osc.ResponseDispatcher;
import netlab.hub.test.mocks.MockSocket;
import netlab.hub.util.MockResponse;

import org.junit.Test;

public class OSCTest {
	
	@Test
	public void testListen() {
		MockOSCImpl osc = new MockOSCImpl();
		ResponseDispatcher dispatcher = new ResponseDispatcher();
		OSCService service = new OSCService(dispatcher);
		service.setOscImpl(osc);
		ServiceMessage request = new ServiceMessage("/service/osc/reader-writer/listen /analogin/7/value 20000");
		try {
			service.process(request, new MockResponse(request));
		} catch (ServiceException e) {
			assertTrue(e.toString(), false);
		}
		assertEquals(osc.listenPorts.get(0).intValue(), 20000);
		assertTrue(!dispatcher.getListenersFor("/analogin/7/value").isEmpty());
		request = new ServiceMessage("/service/osc/reader-writer/stoplisten /analogin/7/value");
		try {
			service.process(request, new MockResponse(request));
		} catch (ServiceException e) {
			assertTrue(e.toString(), false);
		}
		assertTrue(dispatcher.getListenersFor("/analogin/7/value").isEmpty());
	}
	
	@Test
	public void testDispatch() {
		MockOSCImpl osc = new MockOSCImpl();
		OSCService service = new OSCService();
		service.setOscImpl(osc);
		ServiceMessage request = new ServiceMessage("/service/osc/reader-writer/192.168.1.1:9600/myosctest 1");
		try {
			service.process(request, new MockResponse(request));
		} catch (ServiceException e) {
			assertTrue(e.toString(), false);
		}
		assertEquals("192.168.1.1", osc.lastSendIp);
		assertEquals(9600, osc.lastSendPortNum);
		assertEquals("/myosctest", osc.lastSendAddress);
		assertEquals(1, osc.lastSendArgs.length);
		assertTrue(osc.lastSendArgs[0] instanceof Integer);
		assertTrue((Integer)osc.lastSendArgs[0] == 1);
	}
	
	@Test
	public void testResponseDispatcher() throws Exception {
		
		ResponseDispatcher dispatcher = new ResponseDispatcher();
		ServiceResponse response = new ServiceResponse(new ServiceMessage("/service/test/mytest/analogin/7"), null);
		
		dispatcher.addListener("/192.168.5.200/analogin/7", response);
		boolean dispatched = dispatcher.tryDispatch("/service/test/mytest", "192.168.5.200", "/analogin/7", new String[]{});
		assertTrue(dispatched);
		
		dispatcher = new ResponseDispatcher();
		dispatcher.addListener("/*/analogin/7", response);
		dispatched = dispatcher.tryDispatch("/service/test/mytest", "192.168.5.200", "/analogin/7", new String[]{});
		assertTrue(dispatched);
	}
	
	@Test
	public void testCastArguments() throws Exception {
		
		ServiceMessage request;
		Object[] output;
		
		request = new ServiceMessage("/test");
		output = request.getArgumentsAsObjectArray();
		assertEquals(0, output.length);
		
		request = new ServiceMessage("/test 1 1.0 1f 1s abc {1} {1 2}");
		output = request.getArgumentsAsObjectArray();
		assertEquals(7, output.length);
		assertTrue(output[0] instanceof Integer);
		assertTrue(output[1] instanceof Float);
		assertTrue(output[2] instanceof Float);
		assertTrue(output[3] instanceof String);
		assertTrue(output[4] instanceof String);
		assertTrue(output[5] instanceof Integer);
		assertTrue(output[6] instanceof String);
	}

	//@Test
	public void testResponseDispatcherIntegration() throws Exception {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ClientSession client = new ClientSession(new MockSocket(out));
		client.start();
		ServiceMessage request;
		ServiceResponse response; 
		final String serviceAddr = "/service/test/mytest";
		String localNetAddr = "128.0.0.1";
		
		class Service extends OSCService {
			public Service(ResponseDispatcher d) {
				super(d);
			}
			public String getAddress() {
				return serviceAddr;
			}
		}
		ResponseDispatcher dispatcher = new ResponseDispatcher();
		OSCService service = new Service(dispatcher);
		
		String osc = "/analogin/7";
		request = new ServiceMessage(serviceAddr+osc);
		response = new ServiceResponse(request, client);
		dispatcher.addListener(osc, response);
		service.messageReceived(InetAddress.getByName(localNetAddr), "/noeffect", new String[0]);
		assertEquals(0, out.toString().length());
		
		out.reset();
		String[] args = {"128"};
		service.messageReceived(InetAddress.getByName(localNetAddr), osc, args);
		assertEquals(serviceAddr+"/"+localNetAddr+osc+" 128", out.toString().trim());
		
		out.reset();
		dispatcher.removeListeners(new ClientSession(new MockSocket(out)));
		args[0] = "129";
		service.messageReceived(InetAddress.getByName(localNetAddr), osc, args);
		assertEquals(serviceAddr+"/"+localNetAddr+osc+" 129", out.toString().trim());
		
		out.reset();
		dispatcher.removeListener("/noeffect", response);
		args[0] = "130";
		service.messageReceived(InetAddress.getByName(localNetAddr), osc, args);
		assertEquals(serviceAddr+"/"+localNetAddr+osc+" 130", out.toString().trim());
		
		out.reset();
		dispatcher.removeListeners(client);
		service.messageReceived(InetAddress.getByName(localNetAddr), osc, args);
		assertEquals(0, out.toString().length());
		
		out.reset();
		dispatcher.addListener(osc, response);
		args[0] = "131";
		service.messageReceived(InetAddress.getByName(localNetAddr), osc, args);
		assertEquals(serviceAddr+"/"+localNetAddr+osc+" 131", out.toString().trim());
		
		out.reset();
		dispatcher.removeListener(osc, response);
		service.messageReceived(InetAddress.getByName(localNetAddr), osc, args);
		assertEquals(0, out.toString().length());
		
	}

}
