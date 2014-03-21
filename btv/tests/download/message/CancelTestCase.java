package btv.tests.download.message;

import btv.download.message.Cancel;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;

public class CancelTestCase {
	private Cancel cancel;

	@Before
	public void setUpTest() {
		cancel = new Cancel(8, 8, 0, 0, 32);
	}

	@Test
	public void testGetID() {
		assertEquals(cancel.getID(), 8);
	}

	@Test
	public void testGetLength() {
		assertEquals(cancel.getLength(), 8);
	}
}