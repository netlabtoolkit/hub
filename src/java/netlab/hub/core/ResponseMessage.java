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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ResponseMessage {
	
	String address;
	List<Object> arguments = new ArrayList<Object>();
	String header;
	boolean escapeOutputStrings = true;
	
	public ResponseMessage(ServiceMessage request, Object value) {
		super();
		address = request.getAbsoluteAddressString();
		// arguments.addAll(request.getArguments()); // don't include original arguments
		if (value instanceof Object[]) {
			Object[] arr = (Object[])value;
			for (int i=0; i<arr.length; i++) {
				arguments.add(arr[i]);
			}
		} else {
			arguments.add(value);
		}
	}
	
	public String getAddress() {
		return address;
	}
	
	public List<Object> getArguments() {
		return arguments;
	}
	
	public void suppressAddress() {
		address = null;
	}
	
	public void suppressArguments() {
		arguments.clear();
	}
	
	public void suppressOutput() {
		suppressAddress();
		suppressArguments();
	}

	public void disableOutputStringEscaping() {
		escapeOutputStrings = false;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}
	
	public boolean isEmpty() {
		return address == null && arguments.isEmpty();
	}
	
	public String format() {
		StringBuffer sb = new StringBuffer();
		if (address != null) {
			sb.append(address);
			if (!arguments.isEmpty()) {
				sb.append(" ");
			}
		}
		formatArguments(sb);
		return sb.toString();
	}
	
	public String formatArguments() {
		return formatArguments(new StringBuffer()).toString();
	}
	
	public StringBuffer formatArguments(StringBuffer sb) {
		for (Iterator<Object> it=arguments.iterator(); it.hasNext();) {
			formatArgument(sb, it.next());
			if (it.hasNext()) sb.append(" ");
		}
		return sb;
	}
	
	public void formatArgument(StringBuffer sb, Object arg) {
		boolean escapeString = escapeOutputStrings && (arg instanceof String && ((String)arg).indexOf(" ") > -1);
		if (escapeString) sb.append("{");
		sb.append(arg);
		if (escapeString) sb.append("}");
	}
	
	public String toString() {
		return format();
	}
	
	public boolean equals(Object other) {
		if (other instanceof ResponseMessage) {
			if (((ResponseMessage)other).address == null) {
				return this.address == null;
			}
			if (!((ResponseMessage)other).address.equals(this.address)) {
				return false;
			}
			if (((ResponseMessage)other).arguments.size() != this.arguments.size()) {
				return false;
			}
			return ((ResponseMessage)other).formatArguments().equals(this.formatArguments());
 		}
		return false;
	}

}
