package btv.tests.bencoding;

import btv.bencoding.BEncoder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

/*
	Test case for class BEncoder.

	Author: Stephan McLean

*/
public class BEncoderTestCase {

	public BEncoderTestCase() {}

	@Test
	public void testEncodeString() {
		String toEncode = "spam";
		String expected = "4:spam";

		String result = BEncoder.bencode(toEncode);
		assertEquals(result, expected);
	}

	@Test
	public void testEncodeInt() {
		int toEncode = 42;
		String expected = "i42e";

		String result = BEncoder.bencode(toEncode);
		assertEquals(result, expected);
	}

	@Test
	public void testEncodeList() {
		ArrayList toEncode = new ArrayList();
		toEncode.add("spam");
		toEncode.add("eggs");
		String expected = "l4:spam4:eggse";

		String result = BEncoder.bencode(toEncode);
		assertEquals(result, expected);
	}

	@Test
	public void testEncodeMap() {
		Map toEncode = new LinkedHashMap();
		toEncode.put("cow", "moo");
		toEncode.put("spam", "eggs");
		String expected = "d3:cow3:moo4:spam4:eggse";

		String result = BEncoder.bencode(toEncode);
		assertEquals(result, expected);
	}
}