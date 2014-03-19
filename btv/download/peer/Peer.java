package btv.download.peer;
import btv.download.torrent.Torrent;
import btv.download.message.*;
import btv.event.peer.PeerConnectionListener;
import btv.event.peer.PeerConnectionEvent;
import btv.event.peer.PeerCommunicationListener;
import btv.event.peer.PeerCommunicationEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import java.net.Socket;
import java.net.InetSocketAddress;
import javax.xml.bind.DatatypeConverter;

/**
*    This class represents a BitTorrent peer. This class will 
*    set up a connection with a remote peer and download pieces of the 
*    Torrent it is associated with until the Torrent is fully downloaded.
*    This class will also send events to event listeners when it is connecting
*    to the Peer or when it is communicating with the Peer.
*    
*
*    @author Stephan McLean
*    @date 6th February 2014
*/
public class Peer extends Thread {
    private String ip;
    private int port;
    private Torrent torrent;
    private String infoHash, peerID;
    private Socket s = null;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean connected = false;
    
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

    private boolean paused = false;
    private long pauseStart;

    private int numOfPendingRequests = 0;

    private Bitfield bitfield;
    private HashSet<Request> requested;

    // Event handling
    private ArrayList<PeerConnectionListener> connectionListeners;
    private ArrayList<PeerCommunicationListener> communicationListeners;

    /**
    *   This is the constructor used when we are parsing peers from
    *   a tracker response.
    *
    *   @param ip1  The IP address of this peer.
    *   @param port1    The port used to conenct to this peer.
    *   @param t        The Torrent this Peer is associated with.
    *
    */
    public Peer(String ip1, int port1, Torrent t) {
        ip = ip1;
        port = port1;
        torrent = t;
        infoHash = torrent.infoHash();
        peerID = torrent.peerID();
        requested = new HashSet<Request>();
        connectionListeners = new ArrayList<PeerConnectionListener>();
        communicationListeners = new ArrayList<PeerCommunicationListener>();
    }

    /**
    *   This constructor is used when our {@code PeerListener}
    *   receives a new connection.
    *
    *   @param s1   The socket the {@code PeerListener} has set up.
    */
    public Peer(Socket s1) {
        connected = true;
        s = s1;
        requested = new HashSet<Request>();
    }

    /**
    *    Given a String containing 6 bytes, parse these bytes
    *    into ip:port and return a Peer object associated with t.
    *
    *    @param bytes   The byte String to parse the IP and Port from.
    *    @param t       The Torrent this peer is associated with.
    */
    public static Peer parse(String bytes, Torrent t) {

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
        try {
            connectAndSetUp();
        }
        catch(IOException e) {
            torrent.removePeer(this);
            return;
        }

        try {
            performHandShake();
        }
        catch(IOException e) {
            System.out.println("Could not complete peer handshake");
            e.printStackTrace();
            closePeerConnection();
            torrent.removePeer(this);
        }

        try {
            while(!torrent.isDownloaded()) {
                int available = 0;
                try {
                    available = in.available();
                }   
                catch(IOException e) {
                    continue;
                }

                if(available > 0) {
                    // Peer has something to say, parse the message
                    try {
                        readMessage();
                    }
                    catch(IOException e) {
                        continue;
                    }

                    Thread.sleep(10);
                }
                else {
                    // Peer doesn't have anything to say, we will send a message.
                    sendPeerMessage();
                    Thread.sleep(10);
                }
            }
        }
        catch(InterruptedException e) {
            return;
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
        peerCommunicationEvent(false, true, m.getID());
    }

    private void sendPeerMessage() {
        /*
            If the peer has nothing to say we will
            send an Interested message if we are choked.
            otherwise we will request a piece of the file.
            If we are paused we will wait, periodically sending
            keep alive messages.
        */
        if(peerChoking) {
            if(canSend) {
                new Message(Message.INTERESTED, 1, null).send(out);
                peerCommunicationEvent(true, false, Message.INTERESTED);
                canSend = false;
            }
        }
        else {
            if(paused) {
                canSend = true;
                try {
                    // Paused peers need to send a keep alive message to keep
                    // the connection open.
                    if(System.currentTimeMillis() - pauseStart > 60000) {
                        new Message(Message.KEEP_ALIVE, 0, null).send(out);
                        peerCommunicationEvent(true, false, Message.KEEP_ALIVE);
                    }
                    Thread.sleep(10000);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else if(canSend) {
                chainRequests();
            }
        }
    }

    private void chainRequests() {
        /*
            Send up to 5 requests to this peer. We are chaining requests
            to improve download performance.
        */
        while(numOfPendingRequests < 5) {
            Request r = torrent.getNextRequest(this);
            if(r != null) {
                r.send(out);
                peerCommunicationEvent(true, false, r.getID());
                requested.add(r);
                numOfPendingRequests++;
            }
            else {
               break;
            }
        }
        canSend = false;
    }

    private void connectAndSetUp() throws IOException {
        /*
            Connect to the peer and set up the data streams
        */

        peerConnectingEvent(true, false, false);    

        if(s == null) {
            // Try to connect to the peer.
            // Timeout after 20 seconds.
            s = new Socket();
            s.connect(new InetSocketAddress(ip, port), 20000);
        }
        in = new DataInputStream(s.getInputStream());
        out = new DataOutputStream(s.getOutputStream());

        peerConnectingEvent(false, true, false);
        connected = true;
    }

    private void performHandShake() throws IOException {
        /*
            We must exchange a handshake after initially connecting
            to this peer.
            From the spec: 
                handshake: <pstrlen><pstr><reserved><info_hash><peer_id>
        */
        
        byte [] handshake = formHandShake();

        if(!incomingPeer) {
            out.write(handshake);   
        }

        byte [] peerHandshake = receiveHandShake();

        if(!handShakeOK(handshake, peerHandshake)) {
            /*
                There is a problem with the handshake, we must
                drop the connection. We need to exit the thread here. **
            */
            closePeerConnection();
        }

        if(incomingPeer) {
            out.write(handshake);
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

    /*
        Public methods ------------------------------------------------------
    */

    /**
    *   Close all connections to this Peer.
    */
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
        }
        catch(IOException e) {
            System.out.println("Could not close connections");
        }
        peerConnectingEvent(false, false, true);
        connected = false;
    }

    /**
    *   Pausing the peer involves keeping the communication open
    *   but the peer will stop requesting pieces. When paused the Peer
    *   will periodically send keep alive messages to keep the connection
    *   open.
    */
    public void pause() {
        paused = true;
        pauseStart = System.currentTimeMillis();
    }

    /**
    *   Tell this peer to continue downloading. This method is used
    *   on Peers that are paused.
    */
    public void resumeDownload() {
        System.out.println("Peer: " + this + " resuming");
        paused = false;
    }

    /**
    *   This method is used to set this peer as an incoming connection
    *   or a Peer that we are connecting to.
    *
    *   @param b    True if this is an incoming connection, false otherwise.
    */
    public void setIncoming(boolean b) {
        incomingPeer = b;
    }

    /**
    *   @return     A String representation of this Peer.
    *               It will contain the IP and port of this peer.
    */  
    public String toString() {
        return ip + ":" + port;
    }

    /**
    *   @return     The IP address of this peer.
    */
    public String getIP() {
        return ip;
    }

    /**
    *   @return     True if this Peer has an established connection, 
    *               false otherwise.
    */
    public boolean connected() {
        return connected;
    }

    /**
    *   This method is used to check if this peer can download the Piece at
    *   position {@code i}
    *
    *   @param i    The index of the piece to check if this Peer can download.
    *   @return     True if the bit is set in this Peers Bitfield, false
    *               otherwise.
    *
    *   @see btv.download.message.Piece
    *   @see btv.download.message.Bitfield
    */
    public boolean canDownload(int i) {
        return bitfield.bitSet(i);
    }

    /**
    *   Tell this Peer to cancel a specific request.
    *
    *   @param r    The Request to cancel.
    *
    */
    public void cancel(Request r) {
        if(requested.contains(r)) {
            new Cancel(Message.CANCEL, 13, r.getIndex(), r.getOffset(),
                 r.getBlockLength()).send(out);
            peerCommunicationEvent(true, false, Message.CANCEL);
        }
    }

    /**
    *   Add a PeerConnectionListener to this peer.
    *
    *   @param p    The PeerConnectionListener to add.
    *   @see    btv.event.peer.PeerConnectionListener
    *
    */
    public void addPeerConnectionListener(PeerConnectionListener p) {
        if(p != null) {
            connectionListeners.add(p);
        }
    }

    /**
    *   Add a PeerCommunicationListener to this Peer.
    *
    *   @param p    The PeerCommunicationListener to add.
    *   @see btv.event.peer.PeerCommunicationListener
    *
    */
    public void addPeerCommunicationListener(PeerCommunicationListener p) {
        if(p != null) {
            communicationListeners.add(p);
        }
    }

    /*
        End of public methods -----------------------------------------------
    */

    /*
        Message receiving methods. ------------------
    */

    private void choke() {
        peerChoking = true;
    }

    private void unChoke() {
        canSend = true;
        peerChoking = false;
    }

    private void interested() {
        peerInterested = true;
    }

    private void notInterested() {
        peerInterested = false;
    }

    private void have(Message m) {
        Have h = (Have) m;
        bitfield.setBit(h.getPieceIndex());
    }

    private void bitfield(Message m) {
        Bitfield b = (Bitfield) m;
        bitfield = b;
    }

    private void piece(Message m) {
        Piece p = (Piece) m;
        numOfPendingRequests--;
        if(numOfPendingRequests == 0)
            canSend = true;
        torrent.piece(p);
    }

    /*
        End of message receiving methods ------------
    */

    /*
        Event handling methods. -----------------------------------------------
    */

    private void peerConnectingEvent(boolean connecting, boolean connected, 
                                        boolean disconnected) {
        /*
            Inform our listeners that we are connecting.
        */
        PeerConnectionEvent e = new PeerConnectionEvent(this, ip, connecting, 
                                connected, disconnected);
        for(PeerConnectionListener p : connectionListeners) {
            p.handlePeerConnectionEvent(e);
        }
    }

    private void peerCommunicationEvent(boolean dataSent, boolean dataReceived, 
                                            int messageType) {
        /*
            Inform our listeners that we are communicating with this peer.
        */
        PeerCommunicationEvent e = new PeerCommunicationEvent(this, ip,
                                    dataReceived, dataSent, messageType);
        for(PeerCommunicationListener p : communicationListeners) {
            p.handlePeerEvent(e);
        }
    }
    /*
        End of event handling methods. ----------------------------------------
    */
}