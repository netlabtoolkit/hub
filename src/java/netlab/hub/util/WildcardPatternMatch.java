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

import java.util.HashMap;
import java.util.regex.Pattern;

public class WildcardPatternMatch {
	
	public static HashMap<String, Pattern> regExpressions = new HashMap<String, Pattern>();
	
	public static synchronized boolean matches(String pattern, String candidate) {
		if (pattern == null && candidate == null) return true;
		if (pattern == null || candidate == null) return false;
		boolean match = false;
		if (pattern.equals("*")) { // * means match all incoming patterns
			match = true;
		} else if (pattern.indexOf("*") != -1) { // * in the string means use a regexp
			 // Check for an already compiled regexp pattern object
			Pattern regExp = regExpressions.get(pattern);
			if (regExp == null) {
				String regExpStr = pattern.replaceAll("\\*", ".*"); // Replace * wildcard with regexp wildcard
				//regExpStr = "^"+regExpStr; // Always assume start of line match
				regExp = Pattern.compile(regExpStr);
				regExpressions.put(pattern, regExp);
			}
			// Finally, search for the pattern
			match = regExp.matcher(candidate).find(); 
		} else {
			match = pattern.equals(candidate);
		}
		return match;
	}

}
