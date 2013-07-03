package netlab.hub.test.unit;

import netlab.hub.core.ServiceMessage;

import org.junit.Test;
import static org.junit.Assert.*;

public class ServiceMessageTest {
	
	@Test
	public void testParse() {
		
		ServiceMessage msg;
		
		msg = new ServiceMessage("/service/test/abc {my argument} arg2");
		assertTrue(msg.hasArgument(0));
		assertTrue(msg.hasArgument(1));
		assertEquals("my argument", msg.getArgument(0));
		assertEquals("arg2", msg.getArgument(1));
		
		msg = new ServiceMessage("/service/test/abc 1 1.0 1s 1.0s abc abcs {abc}");
		Object[] args = msg.getArgumentsAsObjectArray();
		assertTrue(args[0] instanceof Integer);
		assertTrue(args[1] instanceof Float);
		assertTrue(args[2] instanceof String);
		assertEquals("1", args[2]);
		assertTrue(args[3] instanceof String);
		assertEquals("1.0", args[3]);
		assertTrue(args[4] instanceof String);
		assertTrue(args[5] instanceof String);
		assertEquals("abcs", args[5]);
		assertTrue(args[6] instanceof String);
		assertEquals("abc", args[6]);
		
	}
	
	@Test
	public void testTokenize() {
		
		String input;
		String[] results;
		
		input = "/a/b/c";
		results = ServiceMessage.tokenize(input, "/");
		assertArrayEquals(new String[]{"a", "b", "c"}, results);
		
		input = "/a/b/c/";
		results = ServiceMessage.tokenize(input, "/");
		assertArrayEquals(new String[]{"a", "b", "c"}, results);
		
		input = "a b c";
		results = ServiceMessage.tokenize(input, " ");
		assertArrayEquals(new String[]{"a", "b", "c"}, results);
		
		input = "/a/{x/y}/c";
		results = ServiceMessage.tokenize(input, "/");
		assertArrayEquals(new String[]{"a", "x/y", "c"}, results);
		
		input = "/a/{x/{y/z}}/c";
		results = ServiceMessage.tokenize(input, "/");
		assertArrayEquals(new String[]{"a", "x/{y/z}", "c"}, results);
		
		input = "/a/b/{x}";
		results = ServiceMessage.tokenize(input, " ");
		assertArrayEquals(new String[]{"/a/b/{x}"}, results);	
		
	}
	
}
