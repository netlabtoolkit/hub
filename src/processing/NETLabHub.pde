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
 * This Processing sketch launches the HubRunner and provides it
 * will access to all of the Processing functionality, including
 * access to serial port functionality through the processing.serial.* library.
 */
import netlab.hub.processing.runner.HubRunner;
import controlP5.*;
//import processing.net.*;
import processing.serial.*;

HubRunner hubRunner;

void setup() {
  frameRate(10); // Looping is needed for ControlP5 GUI, but 10 fps is plenty.
  hubRunner = new HubRunner(this);
}

void draw() {
  hubRunner.draw();
}

void stop() {
  hubRunner.stop();
}

public void controlEvent(ControlEvent e) {
  hubRunner.controlEvent(e);
}

