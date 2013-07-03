package netlab.hub.core;

public interface IHubLifecycleMonitor {
	
	public void displayStatus(String status);
	public void initializationComplete();
	public void initializationFailed();
	public void displayAlert(String msg);

}
