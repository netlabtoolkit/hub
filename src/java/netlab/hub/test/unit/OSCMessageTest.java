/*
 * Created on Sep 5, 2007
 */
package netlab.hub.test.unit;

import junit.framework.TestCase;

/**
 * @author ebranda
 */
public class OSCMessageTest extends TestCase {
	
	/*
	public void testIllegals() throws Exception {
		@SuppressWarnings("unused")
		ServiceRequest msg = null;
		try {
			msg = new ServiceRequest("/service/grp/name/");
			msg = new ServiceRequest("/service/grp/name");
			msg = new ServiceRequest("/service/grp/name ");
			msg = new ServiceRequest("/service/grp/nameabc/");
			//assertFalse("Should never get here", true);
		} catch (Exception e) {}
	}

	public void testGetSubsystem() throws Exception {
		ServiceRequest msg = null;
		msg = new ServiceRequest("/service/grp/name/subsystem");
		assertEquals(msg.getSegment(3), "subsystem");
		msg = new ServiceRequest("/service/grp/name/subsystem/device/property arg1");
		assertEquals(msg.getSegment(3), "subsystem");
		msg = new ServiceRequest("/service/grp/name/subsystem/device/property ./");
		assertEquals(msg.getSegment(3), "subsystem");
	}

	public void testGetDevice() throws Exception {
		ServiceRequest msg = null;
		msg = new ServiceRequest("/service/grp/name/subsystem");
		assertNull(msg.getSegment(4));
		msg = new ServiceRequest("/service/grp/name/subsystem/device");
		assertEquals(msg.getSegment(4), "device");
		msg = new ServiceRequest("/service/grp/name/subsystem/device/property arg1");
		assertEquals(msg.getSegment(4), "device");
	}

	public void testGetProperty() throws Exception {
		ServiceRequest msg = null;
		msg = new ServiceRequest("/service/grp/name/subsystem");
		assertEquals(msg.getLastSegment(), "subsystem");
		msg = new ServiceRequest("/service/grp/name/subsystem/property");
		assertEquals(msg.getLastSegment(), "property");
		msg = new ServiceRequest("/service/grp/name/subsystem/device/property ");
		assertEquals(msg.getLastSegment(), "property");
		msg = new ServiceRequest("/service/grp/name/subsystem/device/property arg1");
		assertEquals(msg.getLastSegment(), "property");
	}

	public void testEqualsObject() throws Exception {
		ServiceRequest msg1 = new ServiceRequest("/service/grp/name/abc");
		ServiceRequest msg2 = new ServiceRequest("/service/grp/name/abc");
		assertTrue(msg1.equals(msg2));
		msg2 = new ServiceRequest("/service/grp/name/def");
		assertFalse(msg1.equals(msg2));
		assertFalse(msg1.equals(new String("abc")));
	}

	public void testToString() throws Exception {
		ServiceRequest msg = new ServiceRequest("/service/grp/name/subsystem/device/property arg1");
		assertEquals(msg.toString(), "/service/grp/name/subsystem/device/property arg1");
		msg = new ServiceRequest("/service/grp/name /subsystem/device/property arg1  ");
		assertEquals(msg.toString(), "/service/grp/name /subsystem/device/property arg1");
	}

	public void testGetArguments() throws Exception {
		ServiceRequest msg = new ServiceRequest("/service/grp/name/subsystem/device/property 10");
		assertEquals(msg.getArguments().length, 1);
		assertEquals(msg.getArguments()[0].getInt(), 10);
		msg = new ServiceRequest("/service/grp/name/subsystem/device/property 10 20");
		assertEquals(msg.getArguments().length, 2);
		assertEquals(msg.getArguments()[0].getInt(), 10);
		assertEquals(msg.getArguments()[1].getInt(), 20);
		msg = new ServiceRequest("/service/grp/name/subsystem/device/property ");
		assertEquals(msg.getArguments().length, 0);
	}

	public void testGetFirstArgument() throws Exception {
		ServiceRequest msg = new ServiceRequest("/service/grp/name/subsystem/device/property 10 20");
		assertEquals(msg.getFirstArgument().getInt(), 10);
		msg = new ServiceRequest("/service/grp/name/subsystem/device/property");
		try {
			msg.getFirstArgument().getInt();
			assertFalse("Should never get here", true);
		} catch(Exception e) {}
	}

	public void testGetPath() throws Exception {
		ServiceRequest msg = new ServiceRequest("/service/grp/name/subsystem/device/property arg1");
		assertEquals(msg.getPath(), "/service/grp/name/subsystem/device/property");
		msg = new ServiceRequest("/service/grp/name/subsystem/device");
		assertEquals(msg.getPath(), "/service/grp/name/subsystem/device");
		msg = new ServiceRequest("/service/grp/name/subsystem");
		assertEquals(msg.getPath(), "/service/grp/name/subsystem");
	}
*/
}
