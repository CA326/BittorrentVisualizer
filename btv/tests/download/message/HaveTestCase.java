package btv.tests.download.message;

import btv.download.message.Have;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class HaveTestCase {
	private Have have;

	@Before
	public void setUpTest() {
		have = new Have(4, 5, new byte[] {0});
	}

	@Test
	public void testGetPieceIndex() {
		assertEquals(have.getPieceIndex(), 0);
	}
}