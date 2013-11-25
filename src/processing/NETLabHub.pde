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

/**
 * This Processing sketch launches the Hub, attaches a GUI with
 * with access to all of the Processing functionality, including
 * access to serial port functionality through the processing.serial.* library.
 * This sketch is for generating the standalone applications only.
 * To communicate with the Hub from a Processing sketch, use the 
 * netlab.hub.processing.client.HubClient class instead.
 *
 */
import netlab.hub.processing.desktop.*;
import controlP5.*;
import processing.serial.*;
import java.io.File;

Hub hub;
HubDesktopApplication app;

void setup() {

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
  }
  , "Hub-launcher").start();
}

void draw() {
  background(0);
}

public void controlEvent(ControlEvent e) {
  app.controlEvent(e);
}