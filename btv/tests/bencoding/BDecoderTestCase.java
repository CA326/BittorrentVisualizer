package btv.tests.bencoding;

import btv.bencoding.BDecoder;
import btv.bencoding.BDecodingException;

import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.assertEquals;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

/*	
	Test case for class BDecoder.

	Author: Stephan McLean

*/
public class BDecoderTestCase {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public BDecoderTestCase() {}

	@Test
	public void testDecodeString() throws BDecodingException {
		String toDecode = "4:spam";
		String expected = "spam";
		
		String result = (String) BDecoder.decode(toDecode);
		assertEquals(result, expected);
	}

	@Test
	public void testDecodeInt() throws BDecodingException {
		String toDecode = "i42e";
		int expected = 42;
		
		int	result = (int) BDecoder.decode(toDecode);
		assertEquals(result, expected);
	}

	@Test
	public void testDecodeList() throws BDecodingException {
		String toDecode = "l4:spam4:eggse";
		ArrayList expected = new ArrayList();
		expected.add("spam"); expected.add("eggs");
		
		ArrayList result = (ArrayList) BDecoder.decode(toDecode);
		assertEquals(result, expected);
	}

	@Test
	public void testDecodeMap() throws BDecodingException {
		String toDecode = "d3:cow3:moo4:spam4:eggse";
		Map expected = new LinkedHashMap(); // new Map() ?
		expected.put("cow", "moo");
		expected.put("spam", "eggs");
		
		Map	result = (Map) BDecoder.decode(toDecode);
		assertEquals(result, expected);
	}

	@Test
	public void testUnexpectedEOF() throws BDecodingException {
		thrown.expect(BDecodingException.class);
		thrown.expectMessage("Unexpected end of file.");
		String toDecode = "4:sp";
		String result = (String) BDecoder.decode(toDecode);
	}

	@Test
	public void testUnexpectedChar() throws BDecodingException {
		String toDecode = "b1234e";
		thrown.expect(BDecodingException.class);
		thrown.expectMessage("Unable to process item: b");
		BDecoder.decode(toDecode);
	}
}