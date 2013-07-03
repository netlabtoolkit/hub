package netlab.hub.plugins.tools.pipe.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.plugins.tools.pipe.PipeService;

import org.junit.Test;

public class PipeTest {
	
	@Test
	public void testPipe() throws Exception {
		
		PipeService service = new PipeService();
		ServiceMessage senderRequest = null;
		ServiceResponse senderResponse = null;
		ServiceMessage receiverRequest = null;
		ServiceResponse receiverResponse = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
		
		senderRequest = new ServiceMessage("/service/core/pipe/send");
		senderResponse = new ServiceResponse(senderRequest, null);
		try {
			service.process(senderRequest, senderResponse);
			assertTrue("Should never get here", false);
		} catch (ServiceException e) {
		}
		assertEquals(0, service.getPipes().size());
		out.reset();
		
		senderRequest = new ServiceMessage("/service/core/pipe/connect");
		senderResponse = new ServiceResponse(senderRequest, null);
		service.process(senderRequest, senderResponse);
		senderResponse.send(writer);
		assertEquals("/service/core/pipe/connect OK", out.toString().trim());
		assertEquals(1, service.getPipes().size());
		out.reset();
		
		senderRequest = new ServiceMessage("/service/core/pipe/connect/abc");
		senderResponse = new ServiceResponse(senderRequest, null);
		service.process(senderRequest, senderResponse);
		senderResponse.send(writer);
		assertEquals("/service/core/pipe/connect/abc OK", out.toString().trim());
		assertEquals(2, service.getPipes().size());
		out.reset();
		
		senderRequest = new ServiceMessage("/service/core/pipe/send 123");
		senderResponse = new ServiceResponse(senderRequest, null);
		service.process(senderRequest, senderResponse);
		assertArrayEquals(new String[]{"123"}, service.getPipes().get("default").getLatestValue());
		out.reset();
		
		senderRequest = new ServiceMessage("/service/core/pipe/send 123 456");
		senderResponse = new ServiceResponse(senderRequest, null);
		service.process(senderRequest, senderResponse);
		assertArrayEquals(new String[]{"123","456"}, (String[])service.getPipes().get("default").getLatestValue());
		out.reset();
		
		receiverRequest = new ServiceMessage("/service/core/pipe/latestvalue");
		receiverResponse = new ServiceResponse(receiverRequest, null);
		service.process(receiverRequest, receiverResponse);
		receiverResponse.send(writer);
		assertEquals("/service/core/pipe/latestvalue 123 456", out.toString().trim());
		out.reset();
		
		senderRequest = new ServiceMessage("/service/core/pipe/send/abc 456");
		senderResponse = new ServiceResponse(senderRequest, null);
		service.process(senderRequest, senderResponse);
		assertArrayEquals(new String[]{"456"}, service.getPipes().get("abc").getLatestValue());
		out.reset();
		
		receiverRequest = new ServiceMessage("/service/core/pipe/latestvalue/abc");
		receiverResponse = new ServiceResponse(receiverRequest, null);
		service.process(receiverRequest, receiverResponse);
		receiverResponse.send(writer);
		assertEquals("/service/core/pipe/latestvalue/abc 456", out.toString().trim());
		out.reset();
		
		receiverRequest = new ServiceMessage("/service/core/pipe/receive");
		receiverResponse = new ServiceResponse(receiverRequest, null);
		service.process(receiverRequest, receiverResponse);
		out.reset();
		senderRequest = new ServiceMessage("/service/core/pipe/send 789");
		senderResponse = new ServiceResponse(senderRequest, null);
		service.process(senderRequest, senderResponse);
		receiverResponse.send(writer);
		assertEquals("/service/core/pipe/receive 789", out.toString().trim());
		out.reset();
		
	}

}
