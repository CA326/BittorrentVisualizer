package btv.event.peer;

import java.util.EventListener;

/**
*	This interface can be implemented to handle PeerConnectionEvents.
*
*	@author Stephan McLean
*	@date 12th March 2014.
*
*/
public interface PeerConnectionListener extends EventListener {
	/**
	*	This method will be called when a PeerConnectionEvent occurs.
	*	If a new BitTorrent peer arrives or leaves, this method will be called.
	*
	*	@param e 	The connection event that has occured.
	*
	*/
	public void handlePeerConnectionEvent(PeerConnectionEvent e);
}