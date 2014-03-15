/*
	This class represents a Peer connection event.
	When our listener receives this event our visualization
	will be updated based on the connection state of the peer.

	Author: Stephan McLean
	Date: 12th March 2014.

*/

package btv.event.peer;

import java.util.EventObject;

public class PeerConnectionEvent extends EventObject {
	private String ip;
	private boolean connecting, connected, disconnected;

	public PeerConnectionEvent(Object source, String i, boolean connecting1,
								boolean connected1, boolean disconnected1) {
		super(source);
		ip = i;
		connecting = connecting1;
		connected = connected1;
		disconnected = disconnected1;
	}

	public boolean connecting() {
		return connecting;
	}

	public boolean connected() {
		return connected;
	}

	public boolean disconnected() {
		return disconnected;
	}

	public String getIP() {
		return ip;
	}
}