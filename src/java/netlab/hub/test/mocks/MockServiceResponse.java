package netlab.hub.test.mocks;

import java.util.ArrayList;
import java.util.List;

import netlab.hub.core.ClientSession;
import netlab.hub.core.ResponseMessage;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;

public class MockServiceResponse extends ServiceResponse {
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
}