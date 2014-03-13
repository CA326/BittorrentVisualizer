/*
	This interface can be implemented by classes which will be able
	to deal with PeerCommunicationEvents.

	Author: Stephan McLean
	Date: 12th March 2014

*/

package btv.event.peer;

import java.util.EventListener;

public interface PeerCommunicationListener extends EventListener {
	public void handlePeerEvent(PeerCommunicationEvent e);
}