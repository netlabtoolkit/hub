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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netlab.hub.util.Logger;

public class Autodispatcher {
	
	@SuppressWarnings("unchecked")
	public static boolean dispatch(Service service, ServiceMessage request, ServiceResponse response) throws ServiceException {
		
		List<Class> parameterTypes = new ArrayList<Class>();
		List<Object> args = new ArrayList<Object>();
		try {
			parameterTypes.add(Class.forName("netlab.hub.core.ServiceResponse"));
		} catch (ClassNotFoundException e) {
			throw new ServiceException(e);
		}
		args.add(response);
		
		StringBuffer methodName = new StringBuffer("command");
		for (Iterator<String> it=request.getPath().iterator(); it.hasNext();) {
			String element = it.next();
			boolean isInteger = false;
			boolean isDouble = false;
			try {
				Integer val = new Integer(Integer.parseInt(element));
				parameterTypes.add(val.getClass());
				args.add(val);
				isInteger = true;
			} catch (NumberFormatException e) {}
			if (!isInteger) {
				try {
					Double val = new Double(Double.parseDouble(element));
					parameterTypes.add(val.getClass());
					args.add(val);
					isDouble = true;
				} catch (NumberFormatException e) {}
			}
			boolean isNumeric = isInteger || isDouble;
			if (!isNumeric) {
				methodName.append(capitalize(element));
			}
		}
		
		if (!request.getArguments().isEmpty()) {
			for (Iterator<String> it=request.getArguments().iterator(); it.hasNext();) {
				String arg = it.next();
				Object val = null;
				try {
					val = new Integer(Integer.parseInt(arg));
				} catch (NumberFormatException e) {}
				if (val == null) {
					try {
						val = new Double(Double.parseDouble(arg));
					} catch (NumberFormatException e) {}
				}
				if (val == null) {
					val = arg;
				}
				parameterTypes.add(val.getClass());
				args.add(val);
			}
		}
		
		Class[] parameterTypesArr = new Class[parameterTypes.size()];
		for (int i=0; i<parameterTypes.size(); i++) {
			parameterTypesArr[i] = parameterTypes.get(i);
		}
		Object[] argsArr = new Object[args.size()];
		for (int i=0; i<args.size(); i++) {
			argsArr[i] = args.get(i);
		}
		if (Logger.isDebug()) {
			//Logger.debug("Attempting to dispatch to "+debug(methodName.toString(), parameterTypesArr, argsArr)+" in service class "+service.getClass().getName());
		}
		try {
			//System.out.println(methodName);
			Method method = service.getClass().getMethod(methodName.toString(), parameterTypesArr);
			method.invoke(service, argsArr);
			if (Logger.isDebug()) {
				Logger.debug("Dispatched to "+debug(methodName.toString(), parameterTypesArr, argsArr));
			}
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		} catch (InvocationTargetException e) {
			throw (ServiceException)e.getTargetException();
		} catch (IllegalAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	public static String capitalize(String input) {
        if (input.length() == 0) {
        	return input;
        }
        return new StringBuffer().
        	append(input.substring(0, 1).toUpperCase()).
        	append(input.substring(1).toLowerCase()).toString();
    }
	
	@SuppressWarnings("unchecked")
	public static String debug(String methodName, Class[] parameterTypes, Object[] args) {
		StringBuffer sb = new StringBuffer();
		sb.append("public void ").append(methodName);
		sb.append("(");
		for (int i=0; i<parameterTypes.length; i++) {
			sb.append(parameterTypes[i].getName()).append(" a").append(i);
			if (i < (parameterTypes.length - 1)) {
				sb.append(", ");
			}
		}
		sb.append(")");
		sb.append("[calling ").append(methodName);
		sb.append("(");
		for (int i=0; i<args.length; i++) {
			if (args[i] instanceof String) sb.append("\"");
			sb.append((args[i] instanceof String || args[i] instanceof Number) ? args[i] : args[i].getClass().getName());
			if (args[i] instanceof String) sb.append("\"");
			if (i < (args.length - 1)) {
				sb.append(", ");
			}
		}
		sb.append(")]");
		return sb.toString();
	}

}
