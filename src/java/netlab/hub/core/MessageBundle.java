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

/**
 * @author ebranda
 */
public class MessageBundle {
	
	String originalInput;
	String[] messages = {};
	int currentIdx = 0;

	/**
	 * 
	 */
	public MessageBundle(String input) {
		super();
		this.originalInput = input;
		if (originalInput != null) {
			String in = originalInput.trim();
			if (in.length() > 0) {
				String[] strings = in.split(" : ");
				messages = new String[strings.length];
				for (int i=0; i<strings.length; i++) {
					messages[i] = strings[i].trim();
				}
			}
		}
	}
	
	public boolean hasMoreMessages() {
		return currentIdx < messages.length;
	}
	
	public String nextMessage() {
		return messages[currentIdx++];
	}
	
	public String[] getMessages() throws ServiceException {
		return messages;
	}
	
	public String getFirstMessage() throws ServiceException {
		String[] messages = getMessages();
		if (messages.length > 0) return messages[0];
		return null;
	}
	
	public String toString() {
		return originalInput;
	}

}
