package btv.tests.download.utils;

import btv.download.utils.ByteCalculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.Before;

public class ByteCalculatorTestCase {
	
	@Test
	public void testConvertBytes() {
		assertEquals(ByteCalculator.convert("200"), "200bytes");
	}

	@Test
	public void testConvertKB() {
		assertEquals(ByteCalculator.convert("1024"), "1KB");
	}

	@Test
	public void testConvertMB() {
		assertEquals(ByteCalculator.convert("1048576"), "1MB");
	}

	@Test
	public void testConvertGB() {
		assertEquals(ByteCalculator.convert("1073741824"), "1GB");
	}
}