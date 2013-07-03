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

package netlab.hub.plugins.tools.pipe;

import java.util.HashMap;

public class PipeRegistry {
	
	HashMap<String, Pipe> pipes = new HashMap<String, Pipe>();
	
	public Pipe get(String id) throws NoSuchPipeException {
		if (!exists(id)) {
			throw new NoSuchPipeException(id);
		}
		return pipes.get(id);
	}
	
	public void create(String id) {
		pipes.put(id, new Pipe());
	}
	
	public boolean exists(String id) {
		return pipes.get(id) != null;
	}
	
	public int size() {
		return pipes.size();
	}

}
