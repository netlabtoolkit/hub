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

package netlab.hub.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import netlab.hub.util.Logger;

public class FilterSet {
	
	List<IResponseFilter> filters = new ArrayList<IResponseFilter>();
	
	public FilterSet() {
		this(null);
	}
	
	public FilterSet(List<FilterConfig> configs) {
		super();
		if (configs != null) {
			for (Iterator<FilterConfig> it=configs.iterator(); it.hasNext();) {
				try {
					FilterConfig config = it.next();
					Filter filter = (Filter)Class.forName(config.getType()).newInstance();
					filter.setConfig(config);
					this.filters.add(filter);
				} catch (Exception e) {
					Logger.error("Error loading filters from configurations", e);
				}
			}
		}
	}
	
	public synchronized void add(IResponseFilter filter) {
		this.filters.add(filter);
	}
	
	public synchronized void apply(ResponseMessage message) {
		for (Iterator<IResponseFilter> it=this.filters.iterator(); it.hasNext();) {
			it.next().apply(message);
		}
	}

}
