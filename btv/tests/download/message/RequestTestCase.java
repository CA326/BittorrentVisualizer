package btv.tests.download.message;

import btv.download.message.Request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class RequestTestCase {
	private Request request;

	@Before
	public void setUpTest() {
		request = new Request(6, 13, 0, 0, 0);
	}

	@Test
	public void testGetIndex() {
		assertEquals(request.getIndex(), 0);
	}

	@Test
	public void testGetOffset() {
		assertEquals(request.getOffset(), 0);
	}

	@Test
	public void testGetBlockLength() {
		assertEquals(request.getBlockLength(), 0);
	}
}