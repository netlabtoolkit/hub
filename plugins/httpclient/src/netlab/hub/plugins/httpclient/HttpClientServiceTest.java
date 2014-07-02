package netlab.hub.plugins.httpclient;

import java.io.IOException;

import netlab.hub.core.ClientSession;
import netlab.hub.core.ResponseMessage;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.test.mocks.MockClientSession;
import netlab.hub.util.ThreadUtil;

public class HttpClientServiceTest {

	public static void main(String[] args) {

		//		try {
		//			InetAddress host = InetAddress.getByName("www.google.com");
		//			if (host.isReachable(5000))
		//				System.out.printf("%s is reachable%n", host);
		//			else
		//				System.out.printf("%s could not be contacted%n", host);
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		
		ClientSession client = null;
		try {
			client = new MockClientSession();
		} catch (IOException e1) {
		}
		
		ServiceMessage request;
		ServiceResponse response;

		HttpClientService service = new HttpClientService();
		service.init();
		for (int i=0; i<1000; i++) {
			request = new ServiceMessage("/service/rest/reader-writer/get/localhost/hubtest/yun.php");
			response = new TestServiceResponse(request, client);
			try {
				service.process(request, response);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			ThreadUtil.pause(100);
		}

//		request = new ServiceMessage("/service/rest/reader-writer/getrate 0.5");
//		response = TestServiceResponse.newInstance(request);
//		try {
//			System.out.println("Update rate was "+service.dispatcher.requestsPerSecond);
//			service.process(request, response);
//			System.out.println("Update rate now "+service.dispatcher.requestsPerSecond);
//			System.out.println(response);
//		} catch (ServiceException e) {
//			e.printStackTrace();
//		}
//	
//		for (int i=0; i<200; i++) {
//			request = new ServiceMessage("/service/rest/reader-writer/get/localhost/hubtest/yun.php");
//			response = TestServiceResponse.newInstance(request);
//			try {
//				service.process(request, response);
//			} catch (ServiceException e) {
//				e.printStackTrace();
//			}
//			ThreadUtil.pause(20);
//		}
		 
	}
}

class TestServiceResponse extends ServiceResponse {
	public TestServiceResponse(ServiceMessage request, ClientSession client) {
		super(request, client);
	}
	public void write(ServiceMessage returnAddress, Object value) {
		System.out.println(new ResponseMessage(returnAddress, value));
	}
}
