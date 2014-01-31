/*
Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org

Copyright (c) 2006-2013 Ewan Branda

NETLab Hub is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

NETLab Hub is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
 */

package netlab.hub.plugins.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import netlab.hub.core.ClientSession;
import netlab.hub.core.ResponseMessage;
import netlab.hub.core.Service;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.test.mocks.MockClientSession;
import netlab.hub.util.Logger;
import netlab.hub.util.ThreadUtil;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientService extends Service {

	CloseableHttpClient httpClient;
	ResponseValueBuffer gets;


	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#init()
	 */
	public void init() {
		httpClient = HttpClients.createDefault();
		gets = new ResponseValueBuffer(10);
	}


	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#process(netlab.hub.core.ServiceMessage, netlab.hub.core.ServiceResponse)
	 */
	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
		String command = request.getPath().getFirst();
		if ("get".equals(command)) {
			commandGet(request, response);
		} else 
		if ("getrate".equals(command)) {
			commandGetrate(request, response);
		} else {
			throw new ServiceException("Unsupported command ["+command+"]");
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandGetrate(ServiceMessage request, ServiceResponse response) throws ServiceException {
		if (request.hasArgument(0)) {
			float rate = request.argFloat(0);
			gets.setUpdatesPerSecond(rate);
			response.write("OK");
		} else {
			throw new ServiceException("Missing parameter for getrate command");
		}
	}
	
	
	/**
	 * @param request
	 * @param response
	 * @throws ServiceException
	 */
	public void commandGet(ServiceMessage request, ServiceResponse response) throws ServiceException {
		String uri = request.getPathString(false, 1);
		if (uri == null || uri.length() < 2)
			throw new ServiceException("Missing URL parameter for get command");
		uri = "http://"+uri.substring(1);
		ResponseValue responseValue = gets.getValueFor(uri);
		if (responseValue.isStale()) {
			HttpGet get = new HttpGet(uri);
			try {
				addHeaders(request, get);
			} catch (IOException e) {
				Logger.warn("Unable to add HTTP headers: "+e);
			}
			HttpResponse httpResponse;
			try {
				httpResponse = httpClient.execute(get);
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					responseValue.setContent(inputStreamToString(entity.getContent()));
				}
			} catch (Exception e) {
				Logger.debug(e);
				response.write(new String[] {"FAIL", e.toString()});
			}
		}
		response.write(responseValue.getContent());
	}
	
	/**
	 * Helper method for extracting optional HTTP headers from the 
	 * first argument of the service request and setting them into 
	 * the current HTTP request object.
	 * @param request
	 * @param msg
	 * @throws IOException
	 */
	public void addHeaders(ServiceMessage request, HttpMessage msg) throws IOException {
		Properties headers = new Properties();
		if (request.hasArgument(0)) {
			headers.load(new StringReader(request.getArgument(0).replaceAll(",", "\n"))); // Convert Hub argument format to Properties list
		}
		for (Iterator<Object> it=headers.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();
			Object value = headers.get(key);
			if (value != null) {
				msg.addHeader(key, value.toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see netlab.hub.core.Service#dispose()
	 */
	public void dispose() throws ServiceException {
		try {
			if (httpClient != null)
				httpClient.close();
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * @author ewan
	 *
	 */
	class ResponseValueBuffer {

		HashMap<String, ResponseValue> values = new HashMap<String, ResponseValue>();

		float updatesPerSecond = 0;

		public ResponseValueBuffer(float updatesPerSecond) {
			super();
			setUpdatesPerSecond(updatesPerSecond);
		}
		
		public void setUpdatesPerSecond(float updatesPerSecond) {
			Logger.debug("Changing update rate to "+updatesPerSecond);
			System.out.println("Changing update rate to "+updatesPerSecond);
			this.updatesPerSecond = updatesPerSecond;
		}

		public ResponseValue getValueFor(String uri) {
			ResponseValue value = values.get(uri);
			if (value == null) {
				value = new ResponseValue();
				values.put(uri, value);
			}
			int updateInterval = Math.round(1000 / updatesPerSecond);
			long timeSinceLastUpdate = System.currentTimeMillis() - value.lastUpdateTime;
			if (timeSinceLastUpdate > updateInterval) {
				value.setStale(true);
			}
			return value;
		}
	}

	/**
	 * @author ewan
	 *
	 */
	class ResponseValue {

		boolean stale;
		long lastUpdateTime;
		String content;
		
		public ResponseValue() {
			super();
			stale = true;
			lastUpdateTime = 0;
		}
		
		public void setStale(boolean stale) {
			this.stale = stale;
		}

		public boolean isStale() {
			return stale;
		}

		public void setContent(String content) {
			this.content = content;
			setUpdated();
		}

		public String getContent() {
			return content;
		}
		
		public void setUpdated() {
			lastUpdateTime = System.currentTimeMillis();
			setStale(false);
		}
	}
	
	/**
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public String inputStreamToString(InputStream input) throws IOException {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(input, writer, (String)null);
			return writer.toString();
		} finally {
			input.close();
		}
	}

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
		
		HttpClientService service = new HttpClientService();
		
		service.init();
		for (int i=0; i<10; i++) {
			ServiceMessage request = new ServiceMessage("/service/rest/reader-writer/get/localhost/ebr-dev/keep-alive.php {Content-type=text/csv,X-ApiKey=apiKey}");
			ServiceResponse response = MockServiceResponse.newInstance(request);
			try {
				service.process(request, response);
				System.out.println(response);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			ThreadUtil.pause(100);
		}
		
		ServiceMessage request = new ServiceMessage("/service/rest/reader-writer/getrate 1");
		ServiceResponse response = MockServiceResponse.newInstance(request);
		try {
			System.out.println("Update rate was "+service.gets.updatesPerSecond);
			service.process(request, response);
			System.out.println("Update rate now "+service.gets.updatesPerSecond);
			System.out.println(response);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		
		for (int i=0; i<200; i++) {
			request = new ServiceMessage("/service/rest/reader-writer/get/localhost/ebr-dev/keep-alive.php {Content-type=text/csv,X-ApiKey=apiKey}");
			response = MockServiceResponse.newInstance(request);
			try {
				service.process(request, response);
				System.out.println(response);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			ThreadUtil.pause(100);
		}
	}
}

class MockServiceResponse extends ServiceResponse {
	public static MockServiceResponse newInstance(ServiceMessage request) {
		try {
			return new MockServiceResponse(request, new MockClientSession());
		} catch (IOException e) {
			return null;
		}
	}
	public MockServiceResponse(ServiceMessage request, ClientSession client) {
		super(request, client);
	}
	List<ResponseMessage> responses = new ArrayList<ResponseMessage>();
	public void write(ServiceMessage returnAddress, Object value) {
		responses.add(new ResponseMessage(returnAddress, value));
	}
	public List<ResponseMessage> getAll() {
		return responses;
	}
	public ResponseMessage get(int idx) {
		if (idx >= responses.size()) {
			return null;
		}
		return responses.get(idx);
	}
	public String toString() {
		if (responses.isEmpty()) {
			return "";
		} else if (responses.size() == 1) {
			return responses.get(0).format();
		} else {
			return "Multiple responses";
		}
	}
}
