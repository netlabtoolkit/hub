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

package netlab.hub.filters;

import java.io.PrintWriter;
import java.io.StringWriter;

import netlab.hub.core.Filter;
import netlab.hub.core.ResponseMessage;

public class HttpResponseHeader extends Filter {

	@Override
	public void apply(ResponseMessage msg) {
		StringWriter header = new StringWriter();
		PrintWriter writer = new PrintWriter(header);
		String name = getConfig().getParameter("name");
		writer.println("HTTP/1.1 200"+(name == null ? "" : " "+name));
		writer.println("Content-type: text/html");
		writer.println("");
		msg.setHeader(header.toString());
	}

}
