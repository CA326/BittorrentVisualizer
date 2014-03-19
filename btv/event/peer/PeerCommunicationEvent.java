package btv.event.peer;

import java.util.EventObject;

/**
*	This class describes a communication event with a peer. A communication
*	event occurs when a message is sent to or received from a peer.
*
*	@author Stephan McLean
*	@date 12th March 2014.
*
*/
public class PeerCommunicationEvent extends EventObject {
	private String ip;
	private boolean dataReceived, dataSent;
	private int messageType;

	/**
	*	Constructor to set up a PeerCommunicationEvent.
	*
	*	@param source 	The source of the PeerCommunicationEvent
	*	@param ip1 		The IP address of the Peer responsible for this event.
	*	@param dataR 	Set to true if the peer received data.
	*	@param dataS 	Set to true if the peer sent data.
	*	@param msgType  The ID of the message sent or received.
	*
	*/
	public PeerCommunicationEvent(Object source, String ip1, boolean dataR, 
									boolean dataS, int msgType) {
		super(source);
		ip = ip1;
		dataReceived = dataR;
		dataSent = dataS;
		messageType = msgType;
	}

	/**
	*	Get the IP address of the peer associated with this event.
	*
	*	@return		The IP address of the peer associated with this event.
	*/
	public String getIP() {
		return ip;
	}

	/**
	*	Find out if this event involved data being received by our peer.
	*
	*	@return 	True if the peer received data, false otherwise.
	*/
	public boolean dataReceived() {
		return dataReceived;
	}

	/**
	*	Find out if this event involved data being sent by our peer.
	*
	*	@return 	True if our peer sent data, false otherwise.
	*/
	public boolean dataSent() {
		return dataSent;
	}

	/**
	*	Get the message type that was sent or received.
	*
	*	@return 	The ID of the message sent or received.
	*
	*/
	public int getMessageType() {
		return messageType;
	}
}