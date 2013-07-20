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

import netlab.hub.util.Logger;

/**
 * @author ewan
 *
 */
public abstract class DispatcherAction {
	
	public static DispatcherAction parse(ServiceMessage message) {
		String command = null;
		if (!message.getPath().isEmpty()) {
			command = message.getPath().getLast();
		}
		if ("poll".equalsIgnoreCase(command)) {
			return new PollAction();
		} else
		if ("stoppoll".equalsIgnoreCase(command)) {
			return new StopPollLoopAction();
		} else 
		if ("pollsamplerate".equalsIgnoreCase(command)) {
			return new PollSampleRateAction();
		} else 
		if ("setverbose".equalsIgnoreCase(command)) {
			return new SetVerboseAction();
		} else 
		if ("filterresponse".equalsIgnoreCase(command)) {
			return new SetResponseFilterAction();
		} else {
			return new DefaultDispatchAction();
		}
	}
	
	public abstract void perform(ServiceMessage message, ClientSession client, Dispatcher dispatcher) throws ServiceException;
	
	public ServiceMessage argumentToMessage(ServiceMessage request, int argIdx) throws ServiceException {
		if (request.getArguments().isEmpty()) {
			throw new ServiceException("Missing poll pattern argument");
		}
		StringBuffer sb = new StringBuffer();
		sb.append(request.getServiceAddressString());
		String pattern = request.getArgument(argIdx);
		if (!pattern.startsWith("/")) {
			sb.append("/");
		}
		sb.append(pattern);
		return new ServiceMessage(sb.toString());
	}
}

/**
 * @author ewan
 *
 */
class DefaultDispatchAction extends DispatcherAction {
	public void perform(ServiceMessage message, ClientSession client, Dispatcher dispatcher) {
		dispatcher.dispatch(message, client);
	}
}

/**
 * @author ewan
 *
 */
class PollAction extends DispatcherAction {
	
	public void perform(ServiceMessage message, ClientSession client, Dispatcher dispatcher) throws ServiceException {
		ServiceMessage pollMessage = argumentToMessage(message, 0);
		if (dispatcher.getService(pollMessage) == null) {
			throw new ServiceException("No service exists for address ["+
					pollMessage.getServiceAddressString()+"]... cowardly refusing to start polling.");
		}
		if (parse(pollMessage) instanceof PollAction) {
			throw new ServiceException("To avoid infinite recursion, you may not invoke polling functions from within a poll command");
		}
		DispatcherPoller poller = new DispatcherPoller(pollMessage, client);
		poller.setMaxNumberOfPolls(message.argInt(1, 0));
		poller.setSampleRate(message.argInt(2, poller.getSampleRate()));
		dispatcher.registerPoller(poller);
	}
}

/**
 * @author ewan
 *
 */
class StopPollLoopAction extends PollAction {
	public void perform(ServiceMessage message, ClientSession client, Dispatcher dispatcher) throws ServiceException {
		dispatcher.deregisterPoller(new DispatcherPoller(argumentToMessage(message, 0), client)); 
	}
}

/**
 * @author ewan
 *
 */
class PollSampleRateAction extends PollAction {
	public void perform(ServiceMessage message, ClientSession client, Dispatcher dispatcher) throws ServiceException {
		DispatcherPoller poller = dispatcher.getRegisteredPoller(client, argumentToMessage(message, 0));
		if (poller != null) {
			poller.setSampleRate(message.argInt(1, poller.getSampleRate()));
		}
	}
}

/**
 * @author ewan
 *
 */
class SetVerboseAction extends DispatcherAction {
	public void perform(ServiceMessage message, ClientSession client, Dispatcher dispatcher) throws ServiceException {
		if (message.getArguments().size() < 2) {
			Logger.debug("Missing arguments for setverbose command."+
					" Usage: /setverbose [pattern] [0|1]");
			return;
		}
		boolean verbose = message.argInt(1, 0) > 0;
		client.setVerboseResponse(argumentToMessage(message, 0).toString(), verbose);
	}
}

/**
 * @author ewan
 *
 */
class SetResponseFilterAction extends DispatcherAction {
	public void perform(ServiceMessage message, ClientSession client, Dispatcher dispatcher) throws ServiceException {
		if (message.getArguments().isEmpty()) {
			Logger.debug("Missing pattern argument for filterresponse command.");
			return;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(message.getServiceAddressString());
		String pattern = message.getArgument(0);
		if (!pattern.startsWith("/")) {
			sb.append("/");
		}
		sb.append(pattern);
		client.addResponseAddressFilterPattern(sb.toString());
	}
}
