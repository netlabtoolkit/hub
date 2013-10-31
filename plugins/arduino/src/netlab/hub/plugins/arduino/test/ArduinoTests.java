package netlab.hub.plugins.arduino.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import netlab.hub.core.ResponseMessage;
import netlab.hub.core.ServiceException;
import netlab.hub.plugins.arduino.Arduino;
import netlab.hub.plugins.arduino.ArduinoFactory;
import netlab.hub.plugins.arduino.ArduinoService;
import netlab.hub.serial.SerialException;
import netlab.hub.test.integration.ServiceMessageTester;
import netlab.hub.test.mocks.MockServiceResponse;
import netlab.hub.util.ThreadUtil;

import org.junit.Test;

public class ArduinoTests {
	
	@Test
	public void testArduinoService() throws ServiceException {
		
		ArduinoService service = new ArduinoService(new ArduinoFactory() {
			public Arduino newArduinoInstance(String port, int baud) {
				//return new Arduino(port, baud);
				try {
					return new MockArduino(port, baud);
				} catch (SerialException e) {
					e.printStackTrace();
					return null;
				}
			}
		});
		
		ServiceMessageTester test = new ServiceMessageTester(service);
		MockServiceResponse response;
		ResponseMessage responseMsg;
		MockArduino board;
		
		// Try sending a message before connecting.
		// Expected result: Service should throw an exception with the message "No board connected"
		try {
			response = test.send("/service/arduino/reader-writer/{/dev/cu.usb*}/analogin/0");
			assertTrue("Service should have thrown exception", false);
		} catch (ServiceException e) {
			assertEquals(e.getMessage().indexOf("No board connected"), 0);
		}
		
		// Try connecting to a non-existent serial port
		// Expected result: Service should report a FAIL message
		response = test.send("/service/arduino/reader-writer/connect {/xyz}");
		responseMsg = response.get(0);
		assertEquals("FAIL", responseMsg.getArguments().get(0));
		
		// Try connecting to a legal serial port
		// Expected result: Service should report an OK message
		response = test.send("/service/arduino/reader-writer/connect {/dev/cu.usb*}");
		responseMsg = response.get(0);
		assertEquals("OK", responseMsg.getArguments().get(0));
		
		// Try connecting to an already connected Arduino
		// Expected result: Service should report an OK message
		response = test.send("/service/arduino/reader-writer/connect {/dev/cu.usb*}");
		responseMsg = response.get(0);
		assertEquals("OK", responseMsg.getArguments().get(0));
		
		// Try setting digital pin 9 to high
		// Expected result: board should have digital pin 2 low, then high, then low
		board = (MockArduino)service.getBoard("/dev/cu.usb*");
		board.digitalPins = new int[]{0, 0, 0};
		test.send("/service/arduino/reader-writer/{/dev/cu.usb*}/digitalout/2 1");
		assertEquals(0, board.digitalPins[0]);
		assertEquals(0, board.digitalPins[1]);
		assertEquals(1, board.digitalPins[2]);
		test.send("/service/arduino/reader-writer/{/dev/cu.usb*}/digitalout/2 0");
		assertEquals(0, board.digitalPins[2]);

		// Try sending an illegal message
		// Expected result: Service should throw an exception
		try {
			response = test.send("/service/arduino/reader-writer/a/b");
			assertTrue("Service should have thrown exception", false);
		} catch (ServiceException e) {
			assertEquals(0, e.getMessage().indexOf("Illegal path"));
		}
		
		// Try reading from analog pin 0
		// Expected result: Service should return an int between 1 and 1023
		// Need to send a few messages and wait for board to initialize
		for (int i=0; i<50; i++) {
			response = test.send("/service/arduino/reader-writer/{/dev/cu.usb*}/analogin/0");
			ThreadUtil.pause(100);
		}
		response = test.send("/service/arduino/reader-writer/{/dev/cu.usb*}/analogin/0");
		responseMsg = response.get(0);
		assertEquals(0, responseMsg.toString().indexOf("/service/arduino/reader-writer/{/dev/cu.usb*}/analogin/0 "));
		int val = Integer.parseInt(responseMsg.getArguments().get(0).toString());
		assertTrue(0 < val && val < 1024);
		
		// Call release ports and try sending a message before reconnecting.
		// Expected result: Service should throw an exception with the message "No board connected"
		service.releasePorts();
		try {
			response = test.send("/service/arduino/reader-writer/{/dev/cu.usb*}/analogin/0");
			assertTrue("Service should have thrown exception", false);
		} catch (ServiceException e) {
			assertEquals(e.getMessage().indexOf("No board connected"), 0);
		}
	}

}

