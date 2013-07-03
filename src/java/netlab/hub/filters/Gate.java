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

package netlab.hub.filters;

import netlab.hub.core.Filter;
import netlab.hub.core.ResponseMessage;
import netlab.hub.util.Logger;

public abstract class Gate extends Filter {

	@Override
	public void apply(ResponseMessage message) {
		if (getParameter("threshold") == null) {
			Logger.debug("Missing threshold parameter");
			return;
		}
		double threshold;
		try {
			threshold = Double.parseDouble(getParameter("threshold"));
		} catch (Exception e) {
			Logger.debug("Threshold parameter must be a number");
			return;
		}
		int argIdx = 0;
		if (getParameter("argumentIndex") != null) {
			try {
				argIdx = Integer.parseInt(getParameter("index"));
			} catch (Exception e) {
				Logger.error("Error applying thresholding: illegal index value in config");
				return;
			}
		}
		if (0 <= argIdx && argIdx < (message.getArguments().size() - 1)) {
			Object arg = message.getArguments().get(argIdx);
			// If the value of the specified argument is rejected
			// by the gate implementation then suppress output.
			if (arg instanceof Double || arg instanceof Float || arg instanceof Integer)
			if (!passes(((Double)arg), threshold)) {
				message.suppressOutput();
			}
		}
	}
	
	public abstract boolean passes(double value, double threshold);

}
