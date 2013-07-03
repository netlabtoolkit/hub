package netlab.hub.test.unit;

import netlab.hub.util.WildcardPatternMatch;

import org.junit.Test;
import static org.junit.Assert.*;

public class WildcardPatternMatchTest {
	
	@Test
	public void testMatch() throws Exception {
		
		String pattern, candidate;
		
		pattern = "";
		candidate = "";
		assertTrue(WildcardPatternMatch.matches(pattern, candidate));
		
		pattern = "abc";
		candidate = "";
		assertFalse(WildcardPatternMatch.matches(pattern, candidate));
		
		pattern = "*";
		candidate = "abc";
		assertTrue(WildcardPatternMatch.matches(pattern, candidate));
		
		pattern = "/*";
		candidate = "/abc";
		assertTrue(WildcardPatternMatch.matches(pattern, candidate));
		
		pattern = "/ab*";
		candidate = "/abc";
		assertTrue(WildcardPatternMatch.matches(pattern, candidate));
		
		pattern = "/*c";
		candidate = "/abc";
		assertTrue(WildcardPatternMatch.matches(pattern, candidate));
		
		pattern = "/*/abc";
		candidate = "/xyz/abc";
		assertTrue(WildcardPatternMatch.matches(pattern, candidate));
	}

}
