package btv.tests.download.message;

import btv.download.message.Bitfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class BitfieldTestCase {
	private Bitfield bitfield;

	@Before
	public void setUpTest() {
		bitfield = new Bitfield(5, 5,"FFFFFFFF".getBytes());
	}

	@Test
	public void testGetBitfield() {
		assertEquals(new String(bitfield.getBitfield()), "FFFFFFFF");
	}

	@Test
	public void testBitSet() {
		assertTrue(bitfield.bitSet(0));
	}
}