package netlab.hub.test.mocks;

import netlab.hub.core.ConfigurationException;
import netlab.hub.core.ServiceConfig;

import org.jdom.Element;

public class MockServiceConfig extends ServiceConfig {

	public MockServiceConfig(Element elem, String group, String name)
			throws ConfigurationException {
		super(elem, group, name);
	}

}
