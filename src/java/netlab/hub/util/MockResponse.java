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

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;

public class MockResponse extends ServiceResponse {
	
	ByteArrayOutputStream out;

	public MockResponse(ServiceMessage request) {
		super(request, null);
		out = new ByteArrayOutputStream();
	}
	
	public void write(ServiceMessage returnAddress, Object value) {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
		super.write(returnAddress, value);
		send(writer);
	}
	
	public String toString() {
		return out.toString().trim();
	}

}
