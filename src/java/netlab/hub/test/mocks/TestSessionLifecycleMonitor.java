package netlab.hub.test.mocks;

import netlab.hub.core.ISessionLifecycleMonitor;

public class TestSessionLifecycleMonitor implements ISessionLifecycleMonitor {
	
	boolean started, ended;
	
	public void reset() {
		started = false;
		ended = false;
	}
	
	public boolean active() {
		return started && !ended;
	}
	
	public boolean completed() {
		return started && ended;
	}
	
	public boolean notYetStarted() {
		return !started && !ended;
	}

	public void sessionEnded(String clientId) {
		ended = true;
	}

	public void sessionStarted(String clientId) {
		started = true;
	}

}
