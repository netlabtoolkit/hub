package netlab.hub.plugins.xbee.test;

import static org.junit.Assert.assertEquals;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.plugins.xbee.RemoteXBee;
import netlab.hub.plugins.xbee.XBeeService;
import netlab.hub.util.MockResponse;

import org.junit.Test;

public class XBeeTest {
	
//	@Test
//	public void testCommandFactory() throws Exception {
//		
//		XBeeService service = new XBeeService();
//		Command cmd;
//		
//		cmd = service.parseCommand(new ServiceMessage("/service/test/abc/unsupported"), null);
//		assertTrue(cmd instanceof CommandUnsupported);
//		
//		cmd = service.parseCommand(new ServiceMessage("/service/test/abc/connect"), null);
//		assertTrue(cmd instanceof CommandConnect);
//		
//		cmd = service.parseCommand(new ServiceMessage("/service/test/abc/com4/9/analogin/0"), null);
//		assertTrue(cmd instanceof CommandAnalogRead);
//		
//		cmd = service.parseCommand(new ServiceMessage("/service/test/abc/com4/9/digitalin/0"), null);
//		assertTrue(cmd instanceof CommandDigitalRead);
//		
//		cmd = service.parseCommand(new ServiceMessage("/service/test/abc/com4/9/digitalout/0 1"), null);
//		assertTrue(cmd instanceof CommandDigitalWrite);
//	}
//	
	@Test
	public void testLookup() throws Exception {
		MockXBeeNetwork network = new MockXBeeNetwork();
		String id = "7";
		RemoteXBee xbee = new RemoteXBee(id);
		network.put(xbee);
		assertEquals(xbee, network.get(id));
	}
	
	@Test
	public void testCommandExecute() throws Exception {
		
//		MockXBeeNetwork network;
//		ServiceMessage request;
//		ServiceResponse response;
//		XBeeService service = new XBeeService();
		
		
//		request = new ServiceMessage("/service/test/abc/connect com4");
//		service.process(request, new MockResponse(request));
//		network = ((MockXBeeNetwork)xbees.get("com4"));
//		assertEquals("com4", network.getPortName());
//		assertEquals(XBeeNetwork.DEFAULT_BAUD_RATE, network.baudRate);
//		xbees.clear();
//		
//		request = new ServiceMessage("/service/test/abc/connect com4 9999");
//		service.process(request, new ServiceResponse(request, null));
//		network = ((MockXBeeNetwork)xbees.get("com4"));
//		assertEquals("com4", network.getPortName());
//		assertEquals(9999, network.baudRate);
//		xbees.clear();
		
//		network = new MockXBeeNetwork();
//		xbees.put("com4", network);
//		network.connect("com4", 9600);
//		network.setAnalogSample("7", 1, 999);
//		request = new ServiceMessage("/service/test/abc/com4/7/analogin/1");
//		response = new MockResponse(request);
//		service.process(request, response);
//		assertEquals("/service/test/abc/com4/7/analogin/1 999", response.toString());
//		
//		network.setDigitalSample("7", 3, 0);
//		request = new ServiceMessage("/service/test/abc/com4/7/digitalin/3");
//		response = new MockResponse(request);
//		service.process(request, response);
//		assertEquals("/service/test/abc/com4/7/digitalin/3 0", response.toString());
//		
//		network.setDigitalSample("7", 3, 1);
//		request = new ServiceMessage("/service/test/abc/com4/7/digitalin/3");
//		response = new MockResponse(request);
//		service.process(request, response);
//		assertEquals("/service/test/abc/com4/7/digitalin/3 1", response.toString());
//		
//		request = new ServiceMessage("/service/test/abc/com5/7/digitalin/3");
//		response = new MockResponse(request);
//		service.process(request, response);
//		assertEquals("", response.toString());
//		request = new ServiceMessage("/service/test/abc/connect com5");
//		service.commandConnect(request, new MockResponse(request));
//		XBeeNetwork network2 = ((MockXBeeNetwork)xbees.get("com5"));
//		assertEquals("com5", network2.getPortName());
//		
//		network.clearSamples();
//		assertEquals(0, network.digitalRead("7", 3));
//		request = new ServiceMessage("/service/test/abc/com4/7/digitalout/3 1");
//		response = new MockResponse(request);
//		service.process(request, response);
//		assertEquals(1, network.digitalRead("7", 3));
//		
//		network.clearSamples();
//		network.connect("com4", 9600);
//		network.setAnalogSample("6", 1, 6661);
//		network.setAnalogSample("7", 2, 7772);
//		network.setAnalogSample("8", 1, 8881);
//		request = new ServiceMessage("/service/test/abc/com4/*/analogin/1");
//		response = new MockResponse(request);
//		service.process(request, response);
//		String[] lines = response.toString().split("\n");
//		assertEquals(3, lines.length);
//		ServiceMessage[] results = new ServiceMessage[lines.length];
//		results[0] = new ServiceMessage(lines[0]);
//		results[1] = new ServiceMessage(lines[1]);
//		results[2] = new ServiceMessage(lines[2]);
//		for (ServiceMessage result : results) {
//			if (result.getPath().get(1).equals("6")) {
//				assertEquals(6661, result.argInt(0, 0));
//			} else if (result.getPath().get(1).equals("7")) {
//				assertEquals(0, result.argInt(0, 0));
//			} else {
//				assertEquals(8881, result.argInt(0, 0));
//			}
//		}
	}
	
	//@Test disabled for now since it requires an xbee to be connected
	public void testIntegration() throws Exception {
		
		ServiceMessage request;
		ServiceResponse response;
		XBeeService service = new XBeeService();
		
		request = new ServiceMessage("/service/test/abc/connect com4 57600");
		response = new MockResponse(request);
		service.process(request, response);
		assertEquals("/service/test/abc/connect OK com4", response.toString());
		
		request = new ServiceMessage("/service/test/abc/com4/9/analogin/1");
		response = new MockResponse(request);
		service.process(request, response);
		assertEquals("/service/test/abc/com4/9/analogin/1 0", response.toString());
		
		request = new ServiceMessage("/service/test/abc/com4/9/digitalin/1");
		response = new MockResponse(request);
		service.process(request, response);
		assertEquals("/service/test/abc/com4/9/digitalin/1 0", response.toString());
		
		request = new ServiceMessage("/service/test/abc/com4/9/digitalout/1 1");
		response = new MockResponse(request);
		service.process(request, response);
		assertEquals("", response.toString());
		
		request = new ServiceMessage("/service/test/abc/com4/9/digitalin/1");
		response = new MockResponse(request);
		service.process(request, response);
		assertEquals("/service/test/abc/com4/9/digitalin/1 1", response.toString());
	}
}



