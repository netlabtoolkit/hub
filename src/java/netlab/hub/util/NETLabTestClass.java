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

package netlab.hub.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;




/**
 * @author ebranda
 */
public class NETLabTestClass {

	/**
	 * 
	 */
	public NETLabTestClass() {
		super();
	}
	
	public static void main(String[] args) {
		new NETLabTestClass().runTest();
	}
	
	public void runTest() {
//		try {
//			ServiceRequest request = new ServiceRequest("/service/arduino/reader-writer/analogout/0/value");
//			System.out.println(request.debug());
//		} catch (ServiceRequestFormatException e) {
//			e.printStackTrace();
//		}
		
		
		String test = "/test \"abc 123\" def\"";
		String[] segments = test.split(" ");
		
		List<String> arguments = new ArrayList<String>();
		boolean insideQuotes = false;
		StringBuffer arg = new StringBuffer();
		for (int i=1; i<segments.length; i++) {
			String segment = segments[i];
			arg.append(segment).append(" ");
			if (segment.startsWith("\"")) {
				insideQuotes = true;
			}
			if (segment.endsWith("\"")) {
				insideQuotes = false;
			}
			if (!insideQuotes) {
				arguments.add(arg.toString().trim().replace("\"", ""));
				arg.setLength(0);
			}
		}
		
		for (Iterator<String> it=arguments.iterator(); it.hasNext();) {
			System.out.println("argument="+it.next());
		}
		
	}
}
