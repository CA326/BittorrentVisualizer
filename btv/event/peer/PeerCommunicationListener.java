package btv.event.peer;

import java.util.EventListener;

/**
*	This interface can be implemented by classes which will be able
*	to deal with PeerCommunicationEvents.
*
*	@author Stephan McLean
*	@date 12th March 2014
*
*/
public interface PeerCommunicationListener extends EventListener {
	/**
	*	This method will be called when a Peer communicates with our client.
	*	It will be called when a message is sent or received
	*
	*	@param e 	The communication event that occured.
	*/
	public void handlePeerEvent(PeerCommunicationEvent e);
}