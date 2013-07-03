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

package netlab.hub.core;

import java.util.Iterator;
import java.util.Properties;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

public class FilterConfig {
	
	String type;
	Properties parameters;
	ServiceConfig serviceConfig;
	
	@SuppressWarnings("unchecked")
	public FilterConfig(Element filterElem, ServiceConfig serviceConfig) throws ServiceException {
		this.serviceConfig = serviceConfig;
		this.type = filterElem.getAttributeValue("type");
		this.parameters = new Properties();
		try {
			for (Iterator<Object> pit=XPath.selectNodes(filterElem, "Parameter").iterator(); pit.hasNext();) {
				Element paramElem = (Element)pit.next();
				String name = paramElem.getAttributeValue("name");
				String value = paramElem.getAttributeValue("value");
				if (name == null) {
					throw new ServiceException("Missing name parameter for output filter");
				}
				if (value == null) {
					throw new ServiceException("Missing value for parameter "+name+" for output filter");
				}
				this.parameters.setProperty(paramElem.getAttributeValue("name"), paramElem.getAttributeValue("value"));
			}
		} catch (JDOMException e) {
			throw new ServiceException(e);
		}
		
		
	}
	
	public String getType() {
		return this.type;
	}
	
	public Properties getParameters() {
		return this.parameters;
	}
	
	public String getParameter(String name) {
		return this.parameters.getProperty(name);
	}
	
	public ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

}
