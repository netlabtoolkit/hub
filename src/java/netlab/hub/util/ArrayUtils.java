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

import java.util.List;

/**
 * @author ebranda
 */
public class ArrayUtils {

	public static String toString(Object[] arr) {
		if (arr == null) return "null";
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i=0; i<arr.length; i++) {
			if (arr[i] == null)
				sb.append("null");
			else
				sb.append(arr[i].toString());
			if (i < (arr.length - 1)) sb.append (", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static String toString(int[] arr) {
		return new StringBuffer().append("[").append(join(", ", arr)).append("]").toString();
	}
	
	public static String join(Object[] arr) {
		return join(" ", arr);
	}
	
	public static String join(String separator, Object[] arr) {
		if (arr == null) return "";
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<arr.length; i++) {
			if (arr[i] == null)
				sb.append("null");
			else
				sb.append(arr[i].toString());
			if (i < (arr.length - 1)) sb.append (separator);
		}
		return sb.toString();
	}
	
	public static String join(String separator, int[] arr) {
		Integer[] ints = new Integer[arr.length];
		for (int i=0; i<arr.length; i++) {
			ints[i] = new Integer(arr[i]);
		}
		return join(separator, ints);
	}
	
	public static String[] toStringArray(List<String> items) {
		if (items == null) return new String[] {};
		String[] arr = new String[items.size()];
		for (int i=0; i<items.size(); i++) {
			arr[i] = items.get(i);
		}
		return arr;
	}
	
	public static boolean contains(Object value, Object[] array) {
		for (int i=0; i<array.length; i++) {
			if (value == null) {
				if (array[i] == null) return true;
			} else {
				if (value.equals(array[i])) return true;
			}
		}
		return false;
	}
	
	public static boolean doesNotContain(Object value, Object[] array) {
		return !contains(value, array);
	}

}
