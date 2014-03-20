package btv.tests.event.torrent;

import btv.event.torrent.TorrentEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class TorrentEventTestCase {
	private TorrentEvent event;

	@Before
	public void setUpTest() {
		event = new TorrentEvent(new Object(), "torrent", 0, 0, "0");
	}

	@Test
	public void testGetName() {
		assertEquals(event.getName(), "torrent");
	}

	@Test
	public void testGetDownloadPercent() {
		assertEquals(event.getDownloadedPercent(), 0);
	}

	@Test
	public void testGetConnections() {
		assertEquals(event.getConnections(), 0);
	}

	@Test
	public void testDownloadedBytes() {
		assertEquals(event.getDownloadedBytes(), "0");
	}
}