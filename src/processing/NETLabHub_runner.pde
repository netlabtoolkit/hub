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

