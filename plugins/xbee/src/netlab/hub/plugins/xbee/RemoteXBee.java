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

package netlab.hub.plugins.xbee;

import netlab.hub.util.Logger;

public class RemoteXBee {
	
	String id;
	int[] analogSamples = new int[6];
	int[] digitalSamples = new int[9];
	int rssi = 0;
	
	public RemoteXBee(String id) {
		this.id = id;
	}
	
	public void setAnalogSample(int pin, int value) {
		analogSamples[pin] = value;
	}
	
	public void setDigitalSample(int pin, int value) {
		digitalSamples[pin] = value;
	}
	
	public int analogRead(int pin) {
		if (0 > pin && pin >= analogSamples.length) {
			Logger.debug("Pin number must be in the range of 0 to "+(analogSamples.length-1));
			return 0;
		}
		return analogSamples[pin];
	}
	
	public int digitalRead(int pin) {
		if (0 > pin && pin >= digitalSamples.length) {
			Logger.debug("Pin number must be in the range of 0 to "+(digitalSamples.length-1));
			return 0;
		}
		return digitalSamples[pin];
	}
	
	public int getAnalogPinCount() {
		return analogSamples.length;
	}
	
	public int getDigitalPinCount() {
		return digitalSamples.length;
	}
	
	public String getId() {
		return id;
	}
	
	public void setRssi(int value) {
		this.rssi = value;
	}
	
	public int getRssi() {
		return this.rssi;
	}
}
