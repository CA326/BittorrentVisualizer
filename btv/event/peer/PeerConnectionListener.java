/*
	This interface can be implemented to handle PeerConnectionEvents.

	Author: Stephan McLean
	Date: 12th March 2014.

*/

package btv.event.peer;

import java.util.EventListener;

public interface PeerConnectionListener extends EventListener {
	public void handlePeerConnectionEvent(PeerConnectionEvent e);
}