package btv.tests.download.message;

import btv.download.message.Message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.Before;

public class MessageTestCase {
	private Message message;

	@Before
	public void setUpTest() {
		message = new Message(-1, 0, null);
	}

	@Test
	public void testGetID() {
		assertEquals(message.getID(), -1);
	}

	@Test
	public void testGetLength() {
		assertEquals(message.getLength(), 0);
	}

	@Test
	public void testGetPayload() {
		assertNull(message.getPayload());
	}

	@Test
	public void testSetPayload() {
		byte [] b = "\0\0\0\0\0\0\0\0".getBytes();
		message.setPayload(b);
		assertEquals("\0\0\0\0\0\0\0\0", new String(message.getPayload()));
	}
}