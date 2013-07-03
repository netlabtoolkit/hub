package netlab.hub.test.unit;

import java.util.ArrayList;
import java.util.List;

import netlab.hub.core.FilterConfig;
import netlab.hub.core.FilterSet;
import netlab.hub.core.Service;
import netlab.hub.core.ServiceConfig;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.util.MockServiceConfig;

public class DocTest extends Service {
	
	public static void main(String[] args) {
		try {
			ServiceConfig serviceConfig = new MockServiceConfig("myplug", "hello");
			List<FilterConfig> filterConfigs = new ArrayList<FilterConfig>();
			//filterConfigs.add(new FilterConfig("", "", serviceConfig));
			ServiceMessage request = new ServiceMessage("/service/myplug/hello/say");
			ServiceResponse response = new ServiceResponse(request, null);
			response.setFilters(new FilterSet(filterConfigs));
			Service service = new DocTest();
			service.setConfig(serviceConfig);
			service.process(request, response);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(ServiceMessage msg, ServiceResponse resp) throws ServiceException {
		// TODO Auto-generated method stub
	}
}
