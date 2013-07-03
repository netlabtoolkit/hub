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

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author ebranda
 */
public class MonitoredOutputStream extends OutputStream {
	
	OutputStream theStream;
	IDataActivityMonitor monitor;

	/**
	 * 
	 */
	public MonitoredOutputStream(OutputStream theStream, IDataActivityMonitor monitor) {
		super();
		this.theStream = theStream;
		this.monitor = monitor;
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int arg0) throws IOException {
		theStream.write(arg0);
	}
	
	public void flush() throws IOException {
		theStream.flush();
		monitor.dataSent();
	}

}
