package netlab.hub.plugins.arduino.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import netlab.hub.core.ServiceException;
import netlab.hub.core.ServiceMessage;
import netlab.hub.core.ServiceResponse;
import netlab.hub.plugins.arduino.Arduino;
import netlab.hub.plugins.arduino.ArduinoService;
import netlab.hub.serial.SerialException;
import netlab.hub.serial.SerialPort;
import netlab.hub.util.MockResponse;

import org.junit.BeforeClass;
import org.junit.Test;

public class ArduinoTests {
	
	static MockArduino board;
	static String portName;
	
	
	@BeforeClass
    public static void setUpClass() throws Exception {
		System.clearProperty(SerialPort.SERIAL_PORT_FACTORY_IMPL_CLASS);
		portName = "/dev/cu.usbserial-A1234Q";
		board = new MockArduino(portName);
    }
	
	@Test
	public void testProcess() throws ServiceException {
		class TestService extends ArduinoService {
			Arduino board;
			ServiceMessage request;
			String command;
			public TestService() {
				super();
				boards.put(portName, board);
			}
			public void commandConnect(ServiceMessage request, ServiceResponse response) throws ServiceException {
				this.request = request;
				this.command = "connect";
			}
			public void commandPinMode(ServiceMessage request, ServiceResponse response, Arduino arduino) throws ServiceException {
				this.request = request;
				this.command = "pinmode";
				this.board = arduino;
			}
			public void commandReadWrite(ServiceMessage request, ServiceResponse response, Arduino arduino) throws ServiceException {
				this.request = request;
				this.command = "readwrite";
				this.board = arduino;
			}
		}
		
		TestService service = new TestService();
		ServiceMessage request = new ServiceMessage("/service/arduino/reader-writer/connect "+portName);
		service.process(request, new MockResponse(request));
		assertEquals(service.request, request);
		assertEquals(service.command, "connect");
		
		request = new ServiceMessage("/service/arduino/reader-writer/{"+portName+"}/pinmode 9 input");
		service.setBoard(portName, board);
		service.process(request, new MockResponse(request));
		assertEquals(service.request, request);
		assertEquals(service.command, "pinmode");
		assertEquals(service.board.getSerialPortName(), portName);
		
		request = new ServiceMessage("/service/arduino/reader-writer/{"+portName+"}/digitalout 9 1");
		service.setBoard(portName, board);
		service.process(request, new MockResponse(request));
		assertEquals(service.request, request);
		assertEquals(service.command, "readwrite");
		assertEquals(service.board.getSerialPortName(), portName);
	}
	
	@Test
	public void testConnect() throws SerialException {
		String[] availablePorts = SerialPort.list("/dev/cu.usb*");
		if (availablePorts.length == 0) {
			System.out.println("No USB device found. Skipping testConnect()");
			return;
		}
		ArduinoService service = new ArduinoService();
		ServiceMessage request = new ServiceMessage("/service/arduino/reader-writer/connect /dev/cu.usb* 57600");
		MockResponse response = new MockResponse(request);
		try {
			service.process(request, response);
		} catch (ServiceException e) {
			assertTrue(e.toString(), false);
		}
		assertEquals(response.getMessage().getArguments().get(0).toString(), "OK");
		try {
			assertEquals(service.getBoard("*").getSerialPortName(), portName);
		} catch (ServiceException e) {
			assertTrue(e.toString(), false);
		}
		
		request = new ServiceMessage("/service/arduino/reader-writer/connect /dev/cu.usbxyz* 57600");
		response = new MockResponse(request);
		try {
			service.process(request, response);
		} catch (ServiceException e) {
			assertTrue(e.toString(), false);
		}
		assertEquals(response.getMessage().getArguments().get(0).toString(), "FAIL");

	}
	
	@Test
	public void testPinMode() {
		try {
			ArduinoService service = new ArduinoService();
			board.clearWriteBuffer();
			ServiceMessage request = new ServiceMessage("/service/arduino/reader-writer/{"+portName+"}/pinmode 9 input");
			service.commandPinMode(request, new MockResponse(request), board);
			int[] bytesWritten = board.getBytesWritten();
			assertEquals(bytesWritten.length, 3);
			assertEquals(bytesWritten[0], Arduino.SET_PIN_MODE);
			assertEquals(bytesWritten[1], 9);
			assertEquals(bytesWritten[2], Arduino.INPUT);
			board.clearWriteBuffer();
			request = new ServiceMessage("/service/arduino/reader-writer/{"+portName+"}/pinmode 9 output");
			service.commandPinMode(request, new MockResponse(request), board);
			bytesWritten = board.getBytesWritten();
			assertEquals(bytesWritten.length, 3);
			assertEquals(bytesWritten[2], Arduino.OUTPUT);
			board.clearWriteBuffer();
			request = new ServiceMessage("/service/arduino/reader-writer/{"+portName+"}/pinmode 9 analog");
			service.commandPinMode(request, new MockResponse(request), board);
			bytesWritten = board.getBytesWritten();
			assertEquals(bytesWritten.length, 3);
			assertEquals(bytesWritten[2], Arduino.ANALOG);
			board.clearWriteBuffer();
			request = new ServiceMessage("/service/arduino/reader-writer/{"+portName+"}/pinmode 9 pwm");
			service.commandPinMode(request, new MockResponse(request), board);
			bytesWritten = board.getBytesWritten();
			assertEquals(bytesWritten.length, 3);
			assertEquals(bytesWritten[2], Arduino.PWM);
			board.clearWriteBuffer();
			request = new ServiceMessage("/service/arduino/reader-writer/{"+portName+"}/pinmode 9 servo");
			service.commandPinMode(request, new MockResponse(request), board);
			bytesWritten = board.getBytesWritten();
			assertEquals(bytesWritten.length, 3);
			assertEquals(bytesWritten[2], Arduino.SERVO);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.toString(), false);
		}
	}
	
	@Test
	public void testReadWrite() {
		try {
			ArduinoService service = new ArduinoService();
			board.clearWriteBuffer();
			ServiceMessage request = new ServiceMessage("/service/arduino/reader-writer/{"+portName+"}/digitalout/9/value 1");
			service.commandReadWrite(request, new MockResponse(request), board);
			int[] bytesWritten = board.getBytesWritten();
			assertEquals(bytesWritten.length, 6);
			assertEquals(bytesWritten[0], Arduino.SET_PIN_MODE);
			assertEquals(bytesWritten[1], 9);
			assertEquals(bytesWritten[2], Arduino.OUTPUT);
			board.clearWriteBuffer();
			// TODO other messages
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.toString(), false);
		}
		
	}

}

