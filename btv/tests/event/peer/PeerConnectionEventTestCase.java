package btv.tests.event.peer;

import btv.event.peer.PeerConnectionEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

/*
	Test case for class PeerConnectionEvent

	Author: Stephan McLean

*/
public class PeerConnectionEventTestCase {
	private PeerConnectionEvent connecting;
	private PeerConnectionEvent connected;
	private PeerConnectionEvent disconnected;

	@Before
  	public void setUpTest() {
  		connecting = new PeerConnectionEvent(new Object(), "ip", true, 
  						false, false);
  		connected = new PeerConnectionEvent(new Object(), "ip", false, 
  						true, false);
  		disconnected = new PeerConnectionEvent(new Object(), "ip",	false, 
  						false, true);
  	}

	@Test
	public void testConnectedEvent() {
		assertTrue(connected.connected());
	}

	@Test
	public void testConnectingEvent() {
		assertTrue(connecting.connecting());
	}

	@Test
	public void testDisconnectedEvent() {
		assertTrue(disconnected.disconnected());
	}

	@Test
	public void testGetIP() {
		assertEquals(connecting.getIP(), "ip");
	}
}