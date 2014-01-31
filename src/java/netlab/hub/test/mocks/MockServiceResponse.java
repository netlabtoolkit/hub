package netlab.hub.test.mocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netlab.hub.core.ClientSession;
import netlab.hub.core.ResponseMessage;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;

public class MockServiceResponse extends ServiceResponse {
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