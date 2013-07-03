package netlab.hub.test.unit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import netlab.hub.util.ThreadUtil;

public class SocketTest {
	
	public static void main(String[] args) {
		
		try {
			ServerSocket serverSock = new ServerSocket(51000);
			while (true) {
				System.out.println("Waiting for connections...");
				final Socket socket = serverSock.accept(); // Blocks until client makes connection to server
				System.out.println("Connection received from "+socket.getRemoteSocketAddress());
				new Thread(new Runnable() {
					public void run() {
						try {
							InputStream clientIn = socket.getInputStream();
							BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn));
							String line;
							System.out.println("Waiting for client input...");
							while ((line = reader.readLine()) != null) { // Blocks until data received
								System.out.println(clientIn.available()+" bytes available");
								try {
									System.out.println(line);
								} catch (Exception e) {
									e.printStackTrace();
								}
								ThreadUtil.pause(5000);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
