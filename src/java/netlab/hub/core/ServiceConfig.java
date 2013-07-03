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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import netlab.hub.util.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 * @author ebranda
 */
public class ServiceConfig {
	
	static List<ServiceConfig> configs = new ArrayList<ServiceConfig>();
	
	public static synchronized List<ServiceConfig> getAll() {
		return configs;
	}
	
	public static synchronized void clearAll() {
		configs.clear();
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized void loadAll(XMLConfig config, String group) throws ConfigurationException {
		Document configDoc = config.getDocument();
		Element servicesElem = configDoc.getRootElement();
		if (!servicesElem.getName().equals("Services")) {
			try {
				servicesElem = (Element)XPath.selectSingleNode(servicesElem, "Services");
			} catch (JDOMException e) {
				throw new ConfigurationException(e);
			}
		}
		try {
			// Override the default group name if specified in config file
			String customGroupName = servicesElem.getAttributeValue("group");
			String groupName = (customGroupName != null && customGroupName.trim().length() > 0) ? customGroupName.trim() : group;
			// Load the global startup messages
			List<String> globalStartupMessages = new ArrayList<String>();
			for (Iterator<Element> it=XPath.selectNodes(servicesElem, "StartupMessages/Message").iterator(); it.hasNext();) {
				Element msgElem = (Element)it.next();
				globalStartupMessages.add(msgElem.getValue());
			}
			// Load any specified system properties
			for (Iterator it=XPath.selectNodes(servicesElem, "SystemProperties/Property").iterator(); it.hasNext();) {
				Element propElem = (Element)it.next();
				String name = propElem.getAttributeValue("name");
				String value = propElem.getValue();
				if (value != null && value.trim().length() == 0) value = null;
				if (name != null) {
					Logger.debug("Setting system property "+name+"="+value);
					System.getProperties().setProperty(name, value);
				}
			}
			// Load the service configs
			for (Iterator it=XPath.selectNodes(servicesElem, "Service").iterator(); it.hasNext();) {
				Element serviceElem = (Element)((Element)it.next()).clone();
				//if (!serviceElem.getAttributeValue("enabled").equals("true")) continue;
				String serviceName = "unknown";
				try {
					serviceName = serviceElem.getAttributeValue("name");
					ServiceConfig serviceConfig = new ServiceConfig(serviceElem, groupName, serviceName);
					serviceConfig.build(serviceElem);
					configs.add(serviceConfig);
					// Now add the global startup messages to the service config
					for (Iterator<String> m=globalStartupMessages.iterator(); m.hasNext();) {
						serviceConfig.addGlobalStartupMessage(m.next());
					}
				} catch (Exception e) {
					Logger.error("Error loading configuration for service: "+e);
				}
			}
		} catch(JDOMException e) {
			throw new ConfigurationException(e);
		}
	}
	
	public static void processSubclasses() {
		for (Iterator<ServiceConfig> it=configs.iterator(); it.hasNext();) {
			ServiceConfig config = it.next();
			if (config.serviceClass == null) {
				// Find the superclass service
				for (Iterator<ServiceConfig> candidates=configs.iterator(); candidates.hasNext();) {
					ServiceConfig candidate = candidates.next();
					if (candidate.address.equals(config.superclass) && candidate.serviceClass != null) {
						config.serviceClass = candidate.serviceClass;
						break;
					}
				}
			}
		}
	}
	
	String serviceClass;
	String description;
	String group;
	String name;
	String address;
	String superclass;
	Properties parameters;
	boolean enabled = true;
	
	List<String> startupMessages;
	List<String> globalStartupMessages;
	List<FilterConfig> outputFilters;
	

	/**
	 * 
	 */
	public ServiceConfig(Element elem, String group, String name) throws ConfigurationException {
		super();
		this.group = group;
		this.name = name;
		this.address = "/service/"+group+"/"+name;
		this.globalStartupMessages = new ArrayList<String>();
		this.startupMessages = new ArrayList<String>();
		this.outputFilters = new ArrayList<FilterConfig>();
		this.parameters = new Properties();
		if (elem != null) {
			build(elem);
		}
		// this.validateClasses(); This can't be done here because we want plug-ins to be able to use classes in a plug-in that may not be loaded yet.
	}
	
	public String getAddress() {
		return this.address;
	}

	@SuppressWarnings("unchecked")
	public void build(Element elem) throws ConfigurationException {
		try {
			if (elem.getAttribute("enabled") != null) {
				this.enabled = elem.getAttributeValue("enabled").equals("true");
			} else {
				this.enabled = true;
			}
			if (elem.getAttribute("type") != null) {
				this.serviceClass = elem.getAttributeValue("type");
			} else if (elem.getAttribute("extends") != null) {
				this.superclass = elem.getAttributeValue("extends");
			} else {
				throw new ConfigurationException("Configuration error: service must specify either a 'type' or an 'extends' attribute.");
			}
			if (elem.getChild("Description") != null)
				this.description = elem.getChildText("Description");
			if (elem.getChild("Parameters") != null) {
				for (Iterator<Object> it=XPath.selectNodes(elem, "Parameters/Parameter").iterator(); it.hasNext();) {
					Element paramElem = (Element)it.next();
					this.parameters.setProperty(paramElem.getAttributeValue("name"), paramElem.getValue());
				}
			}
			this.startupMessages = new ArrayList<String>(); // Must be a list because order is important
			if (elem.getChild("StartupMessages") != null) {
				try {
					for (Iterator<Object> it=XPath.selectNodes(elem, "StartupMessages/Message").iterator(); it.hasNext();) {
						Element cmdElem = (Element)it.next();
						this.startupMessages.add(buildStartupMessagePath(cmdElem.getValue()));
					}
				} catch (JDOMException e) {
					throw new ConfigurationException(e);
				}
			}
			this.outputFilters = new ArrayList<FilterConfig>();
			if (elem.getChild("OutputFilters") != null) {
				for (Iterator<Object> it=XPath.selectNodes(elem, "OutputFilters/Filter").iterator(); it.hasNext();) {
					Element filterElem = (Element)it.next();
					String enabled = filterElem.getAttributeValue("enabled");
					if (enabled != null && !"true".equals(enabled)) {
						continue;
					}
					FilterConfig config = new FilterConfig(filterElem, this);
					this.outputFilters.add(config);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationException("Error configuring service "+this, e);
		}
	}
	
	protected void copyFields(ServiceConfig other) {
		serviceClass = other.serviceClass;
		description = other.description;
		startupMessages = new ArrayList<String>();
		startupMessages.addAll(other.startupMessages);
		outputFilters = new ArrayList<FilterConfig>();
		outputFilters.addAll(other.outputFilters);
	}
	
	public String getPathToLibDirectory() {
		return "."+File.separator+"plugins"+File.separator+
			getGroup()+File.separator+"lib"+File.separator;
	}
	
	public void addGlobalStartupMessage(String msg) {
		this.globalStartupMessages.add(buildStartupMessagePath(msg));
	}
	
	protected void validateClasses() throws ConfigurationException {
		try {
			Class.forName(this.serviceClass);
			for (Iterator<FilterConfig> it=this.outputFilters.iterator(); it.hasNext();) {
				Class.forName(it.next().getType());
			}
		} catch (Throwable e) {
			throw new ConfigurationException(e);
		}
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getServiceClass() {
		return this.serviceClass;
	}
	
	public List<String> getStartupMessages() {
		List<String> messages = new ArrayList<String>();
		messages.addAll(this.globalStartupMessages); // Globals come first so locals can override them
		messages.addAll(this.startupMessages);
		return messages;
	}
	
	public List<FilterConfig> getOutputFilters() {
		return this.outputFilters;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public void setEnabled(boolean e) {
		this.enabled = e;
	}
	
	/**
	 * @return Returns the group.
	 */
	public String getGroup() {
		return group;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	public String getParameter(String name) {
		return this.parameters.getProperty(name);
	}
	
	public boolean addressEquals(ServiceMessage request) {
		String serviceGroup = request.getServiceAddress().get(1);
		String serviceName = request.getServiceAddress().get(2);
		return addressEquals(serviceGroup, serviceName);
	}
	
	public boolean addressEquals(String group, String name) {
		return getGroup().equals(group) && getName().equals(name);
	}
	
	private String buildStartupMessagePath(String startupMessage) {
		StringBuffer sb = new StringBuffer();
		sb.append("/service/").append(getGroup()).append("/").append(getName());
		if (startupMessage.startsWith(sb.toString())) {
			return startupMessage;
		}
		if (!startupMessage.startsWith("/")) {
			sb.append("/");
		}
		sb.append(startupMessage);
		return sb.toString();
	}
	
	public String toString() {
		return new StringBuffer().append(group).append("/").append(name).toString();
	}
}
