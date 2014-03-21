package btv.tests.download.message;

import btv.download.message.Piece;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class PieceTestCase {
	private Piece piece;

	@Before
	public void setUpTest() {
		piece = new Piece(7, 12, new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
	}

	@Test
	public void testGetIndex() {
		assertEquals(piece.getIndex(), 0);
	}

	@Test
	public void testGetOffset() {
		assertEquals(piece.getOffset(), 0);
	}

	@Test
	public void testGetBlock() {
		assertEquals(new String(piece.getBlock()), "");
	}
}