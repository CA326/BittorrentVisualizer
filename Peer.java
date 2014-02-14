/*
	Class Peer.
	Responsibilities:
		Communicate with a remote peer.
		Request pieces.
		Receive pieces.
		Send pieces to the associated torrent to be written to the disk.

		TODO:
			Need to take Bitfield into account, currently requesting pieces
			without knowing if the peer has them or not.
	Author: Stephan McLean
	Date: 6th February 2014
*/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import javax.xml.bind.DatatypeConverter;
public class Peer {
	private String ip;
	private int port;
	private Torrent torrent;
	private String infoHash, peerID;
	private Socket s = null;
	private DataInputStream in;
	private DataOutputStream out;
	
	// Are we connecting or connection received?
	// Needed to determine who sends handshake first.
	private boolean incomingPeer = false;

	private boolean amChoking = true;
	private boolean amInterested = false;
	private boolean peerChoking = true;
	private boolean peerInterested = false;

	// Need to know if we have sent a message to the peer
	// for which we expect a response. If we have then
	// don't send any more until we get the response.
	private boolean canSend = true;

	private int currentPiece = 0; // Very crude, change later.

	public Peer(String ip1, int port1, Torrent t) {
		/*
			Constructor used when tracker sends peers as a Map.
		*/
		ip = ip1;
		port = port1;
		torrent = t;
		infoHash = torrent.infoHash();
		peerID = torrent.peerID();
	}

	public Peer(Socket s1) {
		s = s1;
	}

	public static Peer parse(String bytes, Torrent t) {
		/*
			Given a String containing 6 bytes, parse these bytes
			into ip:port and return a Peer object associated with t.

			TODO: Error checking on length of input
		*/

		String peerIP  = "";
		int peerPort;
		// Get IP first
		for(int i = 0; i < 8; i += 2) {
			String b = bytes.substring(i, i + 2);
			peerIP = peerIP + Integer.parseInt(b, 16);
			if(i != 6) {
				peerIP += ".";
			}
		}

		// Now parse the port
		String largePort = bytes.substring(8, 10);
		String smallPort = bytes.substring(10);
		peerPort =  ((Integer.parseInt(largePort, 16) * 256) + Integer.parseInt(smallPort, 16)); 

		return new Peer(peerIP, peerPort, t);
	}

	public void run() {
		/*
			This method handles communication with the peer.
			Here we will request pieces, and respond to messages received.
		*/
		System.out.println("Peer: " + this + " starting");
		try {
			connectAndSetUp();
		}
		catch(IOException e) {
			System.out.println("Could not connect to peer");
			torrent.removePeer(this);
			return;
		}
		performHandShake();

		while(!torrent.isDownloaded()) {
			int available = 0;
			try {
				available = in.available();
			}	
			catch(IOException e) {
				System.out.println("Error communicating with peer: " + this);
			}

			if(available > 0) {
				// Peer has something to say, parse the message
				try {
					readMessage();
				}
				catch(IOException e) {
					System.out.println("Error reading peer message");
				}
			}
			else {
				sendPeerMessage();
			}
		}
	}

	private void readMessage() throws IOException {
		/*
			The peer has sent a message.
			Parse it here.
		*/
		int len = in.readInt();
		Message m = Message.parse(len, in);

		switch(m.getID()) {
			case Message.CHOKE:
				choke();
				break;
			case Message.UNCHOKE:
				unChoke();
				break;
			case Message.INTERESTED:
				interested();
				break;
			case Message.NOT_INTERESTED:
				notInterested();
				break;
			case Message.HAVE:
				have(m);
				break;
			case Message.BITFIELD:
				bitfield(m);
				break;	
			case Message.PIECE:
				piece(m);
				break;
		}
	}

	private void sendPeerMessage() {
		/*
			If the peer has nothing to say we will
			send an Interested message if we are choked.
			otherwise we will request a piece of the file.
		*/
		if(peerChoking) {
			if(canSend) {
				new Message(Message.INTERESTED, 1, null).send(out);
				canSend = false;
			}
		}
		else {
			// Request piece.
			if(canSend) {
				new Request(6, Message.REQUEST, currentPiece, 0,
							torrent.getBlockSize()).send(out);
				canSend = false;
			}
		}
	}

	private void connectAndSetUp() throws IOException {
		/*
			May need to change this.
			The name doesn't describe what's happening well enough.
		*/

		if(s == null) {
			// Try to connect to the peer.
			// Timeout after 20 seconds.
			s = new Socket();
			s.connect(new InetSocketAddress(ip, port), 20000);
		}
		in = new DataInputStream(s.getInputStream());
		out = new DataOutputStream(s.getOutputStream());
	}

	private void performHandShake() {
		/*
			We must exchange a handshake after initially connecting
			to this peer.
			From the spec: 
				handshake: <pstrlen><pstr><reserved><info_hash><peer_id>
		*/
		
		byte [] handshake = formHandShake();

		if(!incomingPeer) {
			try {
				out.write(handshake);	
			}
			catch(IOException e) {}
		}

		byte [] peerHandshake = receiveHandShake();

		// Compare handshakes here
		System.out.println(new String(peerHandshake));
		if(!handShakeOK(handshake, peerHandshake)) {
			/*
				There is a problem with the handshake, we must
				drop the connection
			*/
			closePeerConnection();
		}

		if(incomingPeer) {
			try {
				out.write(handshake);
			}
			catch(IOException e) {}
		}

	}

	private boolean handShakeOK(byte [] ourHandshake, byte [] peerHandshake) {
		/*
			Compare the peers handshake with ours.
			TODO:
				Implement this
		*/
		return true;
	}

	private byte [] formHandShake() {
		/*
			Return the handshake to be sent to the peer
			as an array of bytes.
		*/
		byte [] pstr = "BitTorrent protocol".getBytes();
		byte pstrlen = 19;
		byte [] reserved = "\0\0\0\0\0\0\0\0".getBytes();
		byte [] info = DatatypeConverter.parseHexBinary(infoHash);
		byte [] id = peerID.getBytes();

		byte [] result = new byte[68];
		result[0] = pstrlen;
		System.arraycopy(pstr, 0, result, 1, pstr.length);
		System.arraycopy(reserved, 0, result, 20, reserved.length);
		System.arraycopy(info, 0, result, 28, info.length);
		System.arraycopy(id, 0, result, 48, id.length);

		return result;
	}

	private byte [] receiveHandShake() {
		/*
			Read the peers handshake and return it.
		*/
		byte [] handshake = new byte[68];
		try {
			in.read(handshake);
		}
		catch(IOException e) {
			System.out.println("Could not retrieve peer handshake");
			e.printStackTrace();
		}
		return handshake;
	}

	public void closePeerConnection() {
		try {
			if(s != null) {
				s.close();
			}
			if(in != null) {
				in.close();
			}
			if(out != null) {
				out.close();
			}
			torrent.removePeer(this);
		}
		catch(IOException e) {
			System.out.println("Could not close connections");
		}
	}

	public void setIncoming(boolean b) {
		incomingPeer = b;
	}

	public String toString() {
		return ip + ":" + port;
	}

	/*
		Message receiving methods.
	*/

	public void choke() {
		peerChoking = true;
	}

	public void unChoke() {
		canSend = true;
		peerChoking = false;
	}

	public void interested() {
		peerInterested = true;
	}

	public void notInterested() {
		peerInterested = false;
	}

	public void have(Message m) {
		Have h = (Have) m;
		System.out.println(h);
	}

	public void bitfield(Message m) {
		Bitfield b = (Bitfield) m;
		byte [] bitfield = b.getBitfield();
		String s = DatatypeConverter.printHexBinary(bitfield);
		System.out.println(s);
	}

	public void piece(Message m) {
		Piece p = (Piece) m;
		int index = p.getIndex();
		int offset = p.getOffset();
		byte [] block = p.getBlock();
		currentPiece++;
		canSend = true;
		torrent.piece(index, offset, block);
	}
}