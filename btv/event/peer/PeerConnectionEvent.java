package btv.event.peer;

import java.util.EventObject;

/**
*	This class represents a Peer connection event.
*	When our listener receives this event our visualization
*	will be updated based on the connection state of the peer.
*
*	@author Stephan McLean
*	@date 12th March 2014.
*
*/
public class PeerConnectionEvent extends EventObject {
	private String ip;
	private boolean connecting, connected, disconnected;

	/**
	*	Constructor to set up a new PeerConnectionEvent
	*
	*	@param source	The source of the connection event.
	*	@param i 		The ip address of the peer who caused the connection event.
	*	@param connecting1	Will be set to true if the Peer is establishing
	*						 a connection.
	*	@param connected1	Will be set to true if the Peer has successfully
	*						 connected.
	*	@param disconnected1 Will be set to true if the Peer has been
	*							disconnected.
	*
	*/
	public PeerConnectionEvent(Object source, String i, boolean connecting1,
								boolean connected1, boolean disconnected1) {
		super(source);
		ip = i;
		connecting = connecting1;
		connected = connected1;
		disconnected = disconnected1;
	}

	/**
	*	Returns true if this event describes a Peer that is connecting
	*
	*	@return True if the Peer associated with this event is connecting
	*			, false otherwise
	*/
	public boolean connecting() {
		return connecting;
	}

	/**
	*	Returns true if this event describes a Peer that is connected.
	*
	*	@return True if the Peer associated with this event is connected,
	*			false otherwise.
	*/
	public boolean connected() {
		return connected;
	}

	/**
	*	Returns true if this event describes a Peer that is disconnecting.
	*
	*	@return True if the Peer associated with this event is disconnecting,
	*			false otherwise.
	*/
	public boolean disconnected() {
		return disconnected;
	}

	/**
	*	Returns the IP address of the Peer associated with this event.
	*
	*	@return The IP address of the peer associated with this event.
	*/
	public String getIP() {
		return ip;
	}
}