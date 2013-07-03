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

public class MathUtils {
	
	public static int clamp(int floor, int ceiling, int value) {
		return Math.min(ceiling, Math.max(floor, value));
	}
	
	public static int normalizeAngle(int angle) {
		return normalizeAngle(angle, false);
	}
	
	public static int normalizeAngle(int angle, boolean allowNegativeValues) {
		if (Math.abs(angle) >= 360) {
			angle = angle % 360;
		}
		if (angle < 0 && !allowNegativeValues) {
			angle = angle + 360;
		}
		return angle;
	}
}
