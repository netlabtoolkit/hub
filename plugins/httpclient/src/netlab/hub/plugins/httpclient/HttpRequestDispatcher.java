package netlab.hub.plugins.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.ThreadUtil;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpRequestDispatcher {
	
	CloseableHttpClient httpClient;
	Queue<ServiceRequest> requests = new ConcurrentLinkedQueue<ServiceRequest>();
	int maximumRequestQueueSize = 10;
	boolean running = false;
	
	/**
	 * @param requestsPerSecond
	 */
	public HttpRequestDispatcher() {
		super();
	}
	
	public void setMaximumRequestQueueSize(int max) {
		this.maximumRequestQueueSize = max;
	}
	
	/**
	 * Add a request to the list of requests waiting to be processed.
	 * @param request
	 */
	public void add(ServiceMessage request, ServiceResponse response) 
			throws DispatcherException, RequestQueueOverflowException {
		//System.out.println("queue size = "+requests.size());
		if (requests.size() == maximumRequestQueueSize) {
			throw new RequestQueueOverflowException();
		}
		String uri = request.getPathString(false, 1);
		if (uri == null || uri.length() < 2)
			throw new DispatcherException("Missing URL parameter for get command");
		uri = "http://"+uri.substring(1);
		Properties headers = new Properties();
		if (request.hasArgument(0)) {
			try {
				headers.load(new StringReader(request.getArgument(0).replaceAll(",", "\n")));
			} catch (IOException e) {
				throw new DispatcherException("Error parsing headers", e);
			}
		}
		ServiceRequest req = new ServiceRequest(ServiceRequest.GET, uri, headers, response);
		if (!requests.contains(req)) {
			requests.add(req);
		}
	}
	
	/**
	 * Define a worker thread that will iterate through all waiting requests
	 * and do any required updating of values along with cleanup.
	 */
	public void start() {
		RequestConfig requestConfig = RequestConfig.custom()
	            .setSocketTimeout(3000)
	            .setConnectTimeout(3000).build();
	    httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		new Thread(new Runnable() {
			public void run() {
				running = true;
				while (running) {
					while (requests.size() > 0) {
						final ServiceRequest request = requests.poll(); // Fetch and remove the next request in the queue
						//System.out.println("queue size after fetch = "+requests.size());
		            	HttpGet get = new HttpGet(request.uri);
		            	addHeaders(request, get);
		    			try {
		    				HttpResponse httpResponse = httpClient.execute(get);
		    				String content = getResponseContent(httpResponse);
	                        if (content != null && content.length() > 0) {
	                        	//request.client.write("Elapsed="+((System.currentTimeMillis() - request.created) / 1000.0)+"s "+content);
	                        	request.client.write(content);
	                        }
		    			} catch (Exception e) {
		    				// TODO
		    			}
						ThreadUtil.pause(20);
					}
					ThreadUtil.pause(20);
				}
			}
		}).start();
	}
	
	
	/**
	 * @param response
	 * @return
	 */
	public String getResponseContent(HttpResponse response) {
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			try {
				StringWriter writer = new StringWriter();
				InputStream input = entity.getContent();
				try {
					IOUtils.copy(input, writer, (String)null);
					return writer.toString();
				} finally {
					input.close();
				}
			} catch (IOException e) {
				// TODO
			}
		}
		return null;
	}
	
	/**
	 * @param request
	 * @param req
	 */
	public void addHeaders(ServiceRequest request, HttpMessage req) {
		for (Iterator<Object> it=request.headers.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();
			Object value = request.headers.get(key);
			if (value != null) {
				req.addHeader(key, value.toString());
			}
		}
	}

	/**
	 * @throws IOException
	 */
	public void stop() throws IOException {
		if (running) {
			running = false;
		}
		if (httpClient != null) {
			httpClient.close();
		}
	}
}

class ServiceRequest {
	
	public static final int GET = 100;
	public static final int POST = 101;
	
	int type;
	String uri;
	Properties headers;
	ServiceResponse client;
	long created;
	
	public ServiceRequest(int type, String uri, Properties headers, ServiceResponse client) {
		this.type = type;
		this.uri = uri;
		this.headers = headers;
		this.client = client;
		this.created = System.currentTimeMillis();
	}
	
	public boolean equals(Object other) {
		if (other instanceof ServiceRequest) {
			ServiceRequest otherReq = (ServiceRequest)other;
			return this.uri.equals(otherReq.uri) && this.client.clientEquals(otherReq.client);
		}
		return false;
	}

}

@SuppressWarnings("serial")
class RequestQueueOverflowException extends Exception {
	
}

@SuppressWarnings("serial")
class DispatcherException extends Exception {
	public DispatcherException(String msg) {
		super(msg);
	}
	public DispatcherException(String msg, Exception cause) {
		super(msg, cause);
	}
}
