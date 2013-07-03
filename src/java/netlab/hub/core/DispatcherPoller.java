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
 * This class is a helper class used by the Dispatcher to manage poll messages.
 * An instance of this class is registered by the dispatcher for each 
 * client/message-pattern pair. Since the dispatcher iterates as fast as possible, 
 * this class simply throttles the rate at which messages are dispatched based on a 
 * specified sample rate.
 * 
 * @author ewan
 *
 */
public class DispatcherPoller {
	
	int delay;
	int sampleRate;
	long lastPollTime;
	int maxNumberPolls;
	int pollCount;
	
	ClientSession client;
	ServiceMessage pattern;
	
	/**
	 * Message format: [servicepath]/config/listen [pattern] [fps] [changedvaluesonly (0|1)]
	 * @param fps
	 */
	public DispatcherPoller(ServiceMessage pattern, ClientSession client) throws ServiceException {
		this.client = client;
		this.pattern = pattern;
		this.lastPollTime = System.currentTimeMillis();
		this.maxNumberPolls = 0;
		this.pollCount = 0;
		setSampleRate(30);
	}
	
	public void setSampleRate(int fps) {
		this.sampleRate = fps;
		this.delay = (int)Math.round(1000.0 / (double)fps);
	}
	
	public int getSampleRate() {
		return this.sampleRate;
	}
	
	public void setMaxNumberOfPolls(int m) {
		maxNumberPolls = m;
	}
	
	public boolean tryPoll(Dispatcher dispatcher) {
		long now = System.currentTimeMillis();
		if (now - lastPollTime > delay) {
			dispatcher.processMessage(pattern, client);
			lastPollTime = now;
			pollCount++;
			return true;
		}
		return false;
	}
	
	public boolean completed() {
		return maxNumberPolls <= 0 ? false : pollCount >= maxNumberPolls;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof DispatcherPoller) {
			return (((DispatcherPoller)other).client == this.client && ((DispatcherPoller)other).pattern.equals(this.pattern));
		}
		return false;
	}
	
	public ClientSession getClient() {
		return client;
	}
	
	/**
	 * Mainly to support unit testing.
	 * @return
	 */
	public ServiceMessage getPattern() {
		return this.pattern;
	}
	
	public int getDelay() {
		return delay;
	}
}
