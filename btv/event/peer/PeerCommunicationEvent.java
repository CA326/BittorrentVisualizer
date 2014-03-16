/*
	This class will be used as part of our visualization
	to show when we are communicating with a Peer.
	It will be handled by PeerCommunicationListeners.

	Author: Stephan McLean
	Date: 12th March 2014.

*/

package btv.event.peer;

import java.util.EventObject;

public class PeerCommunicationEvent extends EventObject {
	private String ip;
	private boolean dataReceived, dataSent;
	private int messageType;

	public PeerCommunicationEvent(Object source, String i, boolean dataR, 
									boolean dataS, int m) {
		super(source);
		ip = i;
		dataReceived = dataR;
		dataSent = dataS;
		messageType = m;
	}

	public String getIP() {
		return ip;
	}

	public boolean dataReceived() {
		return dataReceived;
	}

	public boolean dataSent() {
		return dataSent;
	}

	public int getMessageType() {
		return messageType;
	}
}