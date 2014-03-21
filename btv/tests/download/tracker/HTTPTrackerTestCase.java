package btv.tests.download.tracker;

import btv.download.tracker.HTTPTracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.Before;

public class HTTPTrackerTestCase {
	private HTTPTracker tracker;

	@Before
	public void setUpTest() {
		tracker = new HTTPTracker("tracker.com", "hash", "peerID", "port", 
					"downloaded", "uploaded", "left");
	}

	@Test
	public void testGetTracker() {
		assertEquals(tracker.getTracker(), "tracker.com");
	}

	@Test
	public void testGetQueries() {
		String expected = "?info_hash=hash&peer_id=peerID&port=port&downloaded=" +
							"downloaded&uploaded=uploaded&left=left&compact=1";

		assertEquals(expected, tracker.getQueries());
	}
}