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

package netlab.hub.processing.runner;

import java.io.File;

import netlab.hub.core.Hub;
import netlab.hub.serial.SerialPort;
import processing.core.PApplet;
import controlP5.ControlEvent;

/**
 * A convenience wrapper around the netlab.hub.core.Hub class that 
 * allows for running the Hub from inside a Processing sketch.
 * This class configures the Hub, provides methods for starting
 * and stopping the Hub, and attaches a GUI.
 *
 */
public class HubRunner {
	
	PApplet parent;
	Hub hub;
	HubRunnerGUI gui;
		
	public HubRunner(PApplet parent) {
		
		this.parent = parent;
		
		// Determine the file system path to the current application base
		String base = System.getProperty("netlab.hub.base");
		if (base == null) {
			base = parent.sketchPath(".");
		}
		
		// Set the serial port implementation to use
		System.setProperty(SerialPort.SERIAL_PORT_FACTORY_IMPL_CLASS, 
								"netlab.hub.processing.runner.ProcessingSerialPortFactoryImpl");
		
		// Create the Hub instance
		hub = new Hub(new File(base));
		
		// Create the gui and attach it to the hub
		gui = new HubRunnerGUI(hub, parent);
		hub.setHubLifecycleMonitor(gui);
		hub.setDataActivityMonitor(gui);
		hub.setSessionLifecycleMonitor(gui);
		
		// Start the Hub in a new thread so that Processing can move on
		// and start calling the "draw" method during Hub init.
		new Thread(new Runnable() {
			public void run() {
				hub.start();
			}
		}, "Hub-launcher").start();
	
	}
	
	public void draw() {
		// We don't do anything here, but we need to loop in order that
		// ControlP5 can update the user interface.
		parent.background(0);
	}
	
	public void controlEvent(ControlEvent e) {
		gui.controlEvent(e);
	}
	
	public void stop() {
		if (hub != null) {
			hub.quit();
		}
	}
}



