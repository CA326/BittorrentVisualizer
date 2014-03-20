package btv.tests.event.peer;

import btv.event.peer.PeerCommunicationEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class PeerCommunicationEventTestCase {
	private PeerCommunicationEvent sent;
	private PeerCommunicationEvent received;

	@Before
  	public void setUpTest() {
  		sent = new PeerCommunicationEvent(new Object(), "ip", false, true, -1);
  		received = new PeerCommunicationEvent(new Object(), "ip", 
  												true, false, -1);
  	}

	@Test
	public void testGetIP() {
		assertEquals(sent.getIP(), "ip");
		assertEquals(received.getIP(), "ip");
	}

	@Test
	public void testDataReceived() {
		assertTrue(received.dataReceived());
	}

	@Test
	public void testDataSent() {
		assertTrue(sent.dataSent());
	}

	@Test
	public void testMessageType() {
		assertEquals(sent.getMessageType(), -1);
		assertEquals(received.getMessageType(), -1);
	}
}