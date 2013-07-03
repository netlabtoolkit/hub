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


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;



/**
 * @author ebranda
 */
public class GUILogger extends AppenderSkeleton {
	
	public static IGUILogger gui;

	/**
	 * 
	 */
	public GUILogger() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	protected void append(LoggingEvent evt) {
		if (gui == null) {
			// System.err.println(this.getClass().getName()+": No gui defined in GUILogger"); 
			return;
		}
		String msg = this.layout.format(evt);
		gui.print(msg);
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
	 */
	public boolean requiresLayout() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#close()
	 */
	public void close() {
	}

}
