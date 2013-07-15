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

package netlab.hub.processing.desktop;

import java.io.File;

import netlab.hub.core.Hub;
import processing.core.PApplet;
import controlP5.ControlEvent;

/**
 * Applet that launches the Hub in a Processing environment. It is used
 * primarily for testing, but this code could be dropped into a 
 * Processing sketch to launch the Hub. We use this Applet as the 
 * launcher from Eclipse for development when we want to test the
 * Hub inside the Processing stack rather than as a bare Hub instance.
 * <p />
 * This Applet simply dispatches all of its Hub functionality to a 
 * HubRunner instance, which takes care of managing the Hub.
 */
@SuppressWarnings("serial")
public class DevLauncherApplet extends PApplet {
	
	Hub hub;
	HubDesktopApplication app;
	
	
	public void setup() {
		frameRate(10); // Looping is needed for ControlP5 GUI, but 10 fps is plenty.

		// Create the Hub instance
		String base = System.getProperty("netlab.hub.base");
		if (base == null) {
			base = sketchPath(".");
		}
		hub = new Hub(new File(base));
		
		// Create the gui and attach it to the hub
		app = new HubDesktopApplication(hub, this);
		
		// Start the Hub in a new thread so that Processing can move on
		// and start calling the "draw" method during Hub init.
		new Thread(new Runnable() {
			public void run() {
				hub.start();
			}
		}, "Hub-launcher").start();
	}
	
	public void draw() {
		// We don't do anything here, but we need to loop in order for
		// the GUI can update the user interface and respond to events.
		background(0);
	}
	
	public void controlEvent(ControlEvent e) {
		app.controlEvent(e);
	}
	
	public void stop() {
	  	if (hub != null) {
			hub.quit();
		}
	}
}
