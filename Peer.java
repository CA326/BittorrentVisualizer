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
			Move all Message creation and parsing to Message classes.

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

		/*
			Now start communicating.
			After performing the handshake the peer
			should send the *bitfield* and possibly some 
			*have* messages.
		*/
		try {
			while(!torrent.isDownloaded()) {
				if(in.available() > 0) {
					// Peer has something to say, parse the message
					int len = in.readInt();
					System.out.println("Length: " + len);
					if(len == 0) {
						// Keep alive message
						continue;
					}

					int id = in.readByte();
					switch(id) {
						case 0:
							System.out.println("Choke message");
							peerChoking = true;
							break;
						case 1:
							System.out.println("Unchoke");
							canSend = true;
							peerChoking = false;
							break;
						case 2:
							System.out.println("Interested");
							peerInterested = true;
							break;
						case 3:
							System.out.println("Not interested");
							peerInterested = false;
							break;
						case 4:
							int h = in.readInt();
							System.out.println("have: " + h);
							break;
						case 5:
							System.out.println("Bitfield");
							byte [] bitfield = new byte[len - 1];
							in.read(bitfield);
							String s = DatatypeConverter.printHexBinary(bitfield);
							System.out.println(s);
							break;	
						case 7:
							// Received a piece
							System.out.println("Received piece");
							int index = in.readInt();
							int offset = in.readInt();
							byte [] block = new byte[len - 9];
							in.readFully(block);
							currentPiece++;
							canSend = true;
							torrent.piece(index, offset, block);
							break;
					}
				}
				else {
					if(peerChoking) {
						if(canSend) {
							out.writeInt(1); out.writeByte(2);
							canSend = false;
						}
					}
					else {
						// Request piece.
						if(canSend) {
							out.writeInt(13);
							out.writeByte(6);
							out.writeInt(currentPiece);
							System.out.println("Requesting piece: " + currentPiece);
							out.writeInt(0);
							out.writeInt(torrent.getBlockSize());

							canSend = false;
						}
					}
				}
			}
		}
		catch(IOException e) {
			System.out.println("Error communicating with peer: " + this);
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
}