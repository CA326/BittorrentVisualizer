package btv.tests.download.utils;

import btv.download.utils.TorrentFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.Before;

public class TorrentFileTestCase {
	private TorrentFile torrentFile;

	@Before
	public void setUpTest() {
		torrentFile = new TorrentFile("path", 0);
	}

	@Test
	public void testGetPath() {
		assertEquals(torrentFile.getPath(), "path");
	}

	@Test
	public void testGetLength() {
		assertEquals(torrentFile.getLength(), 0);
	}
}