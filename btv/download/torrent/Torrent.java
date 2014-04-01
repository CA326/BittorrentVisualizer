package btv.download.torrent;

import btv.bencoding.BDecoder;
import btv.bencoding.BEncoder;
import btv.bencoding.BDecodingException;
import btv.download.utils.SHA1;
import btv.download.utils.ByteCalculator;
import btv.download.peer.Peer;
import btv.download.tracker.Tracker;
import btv.download.tracker.HTTPTracker;
import btv.download.tracker.UDPTracker;
import btv.download.message.Message;
import btv.download.message.Bitfield;
import btv.download.message.Request;
import btv.download.message.Piece;
import btv.event.torrent.TorrentListener;
import btv.event.torrent.TorrentEvent;
import btv.event.peer.PeerConnectionListener;
import btv.event.peer.PeerCommunicationListener;

import java.util.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.math.BigInteger;
import javax.xml.bind.DatatypeConverter;
/**
*    This class represents a BitTorrent Torrent. This class is used to
*    parse the meta-info file, contact trackers and set up our Peers
*    which will download the Torrent files.
*
*    @author Stephan McLean
*    @date 6th February 2014
*
*/
public class Torrent extends Thread {
    private String fileName;
    private Map metainfo, infoDict;
    private String hash;
    private String peerID;
    private String port = "6881";
    private String downloaded = "0";
    private String uploaded = "0";
    private String left;
    private String name;
    private int pieceLength;
    private int totalLength;
    private String pieces; // SHA1 values of each piece.
    private int numberOfPieces;
    private ArrayList<Peer> peers; // Active peers.
    private ArrayList<Peer> allPeers;
    private Tracker tracker;
    private ArrayList<Tracker> trackers;
    private Bitfield bitfield;
    private HashMap<Request, Integer> requested;
    private int percentDownloaded;
    private boolean downloading = false;
    private boolean paused = false;
    private boolean stopped = false;

    private final static int MAX_CONNECTIONS = 10;

    private TorrentFileWriter fileWriter;

    private EventRelayer relayer;

    // Peer event handling.
    private PeerConnectionListener peerConnectionListener;
    private PeerCommunicationListener peerCommunicationListener;

    /**
    *   This constructor will set up a new Torrent. When a Torrent is
    *   created the meta-info will be parsed, and the directory to store the
    *   Torrent will be created. A temporary file used for downloading will
    *   also be created.
    *
    *   @param fileName     The name of the meta-info file.
    *   @param peerID       The peer ID of our client.
    *
    */
    public Torrent(String fileName, String peerID) throws FileNotFoundException, 
                                        BDecodingException {

        this.fileName = fileName;
        this.peerID = peerID;
        initMetaInfo(fileName);
        initBitfield();
        initRequested();

        peers = new ArrayList<Peer>();
        allPeers = new ArrayList<Peer>();
        trackers = new ArrayList<Tracker>();
        relayer = new EventRelayer(this);
        fileWriter = new TorrentFileWriter(this);
    }

    private void checkResumeDownload() {
        /*
            Check the temporary file for data.
            Update the bitfield & requested map if any
            data is found.
        */
        try {
            RandomAccessFile tempFile = fileWriter.getTempFile();
            if(tempFile.length() > 0) {
                Set<Request> requests = requested.keySet();
                for(Request r : requests) {
                    tempFile.seek((r.getIndex() * pieceLength) + r.getOffset());
                    byte [] data = new byte[r.getBlockLength()];
                    tempFile.read(data);
                    String expectedSHA = pieces.substring(r.getIndex() * 20, (r.getIndex() * 20) + 20);
                    expectedSHA = DatatypeConverter.printHexBinary(expectedSHA.getBytes("ISO-8859-1"));
                    String actualSHA = SHA1.hexdigest(data);
                    if(expectedSHA.toLowerCase().equals(actualSHA.toLowerCase())) {
                        // Downloaded already
                        requested.put(r, 1);
                        updateDownloadStats(r.getBlockLength());
                    }
                }
                System.out.println("Starting from: " + percentDownloaded + "%");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void initMetaInfo(String fileName) throws FileNotFoundException,
                                         BDecodingException {
        String metaFileData = readFile(fileName);            
        metainfo = (Map) BDecoder.decode(metaFileData);
        infoDict = (Map) metainfo.get("info");
        name = (String)infoDict.get("name");
        pieceLength = (int) infoDict.get("piece length");
        pieces = (String) infoDict.get("pieces");
        numberOfPieces = pieces.length() / 20;
        hash = computeHash();
        calculateFileLength();
        left = "" + totalLength;
    }

    private void calculateFileLength() {
        if(infoDict.containsKey("files")) {
            ArrayList<Map> files = (ArrayList<Map>) infoDict.get("files");
            for(Map map : files) {
                int fileLen = (int) map.get("length");
                totalLength += fileLen;
            }
        }
        else {
            totalLength = (int) infoDict.get("length");
        }
    }

    private void contactTracker() {
        /*
            Set up our tracker and retrieve the response.
            We must then parse the response to retrieve our Peer list.
            Will deal with other data in the response later.

            TODO: Check for keys not in map.
        */
        Map trackerResponse = null;
        if(trackers.size() > 0) {
            Tracker tracker = trackers.remove(0);
            boolean validResponse = false;
            int attempt = 0;
            while(!validResponse && attempt < 3) {
                try {
                    trackerResponse = tracker.contact();
                    validResponse = true;
                }
                catch(BDecodingException e) {
                    attempt++;
                    e.printStackTrace();
                }
            }
        }
        /*
            Still need to check for bad tracker response here.
        */
        if(trackerResponse != null) {
            parsePeers(trackerResponse);
        }
        else {
            System.out.println("Bad tracker response, not parsing peers.");
        }
    }

    private void parseTracker() {
        /*
            For now we are only dealing with HTTP trackers.
            This method will search the meta-info file for a HTTP Tracker
            and set it up.
        */
        String announce = (String) metainfo.get("announce");
        if(announce.startsWith("http")) {
            tracker = createTracker(announce);
            trackers.add(tracker);
        }
        else {
            if(metainfo.containsKey("announce-list")) {
                ArrayList<ArrayList<String>> announceList = 
                (ArrayList<ArrayList<String>>)metainfo.get("announce-list");

                for(ArrayList<String> a : announceList) {
                    if(a.get(0).startsWith("http")) { // Check for null here.
                        tracker = createTracker(a.get(0));
                        trackers.add(tracker);
                    }
                }
            }
        }
    }

    private Tracker createTracker(String announce) {
        return new HTTPTracker(announce, percentEncode(hash), 
            peerID, port, downloaded, uploaded, left);
    }

    private void addPeer(Peer p) {
        p.addPeerConnectionListener(peerConnectionListener);
        p.addPeerCommunicationListener(peerCommunicationListener);
        synchronized(peers) {
            if(peers.size() < MAX_CONNECTIONS) {
                peers.add(p);
            }
            else {
                // Save for later.
                allPeers.add(p);
            }
        }
    }

    private void startNewPeers() {
        // Try to keep peers at MAX_CONNECTIONS
        if(allPeers.size() > 0) {
            while(peers.size() < MAX_CONNECTIONS) {
                Peer p = allPeers.get(0);
                allPeers.remove(p);
                peers.add(p);
                p.start();
            }
        }
    }

    private void parsePeers(Map trackerResponse) {
        // Need to deal with Map model here.
        String binaryPeer = (String) trackerResponse.get("peers");
        try {
            binaryPeer = String.format("%x", 
                        new BigInteger(1, binaryPeer.getBytes("ISO-8859-1")));
        }
        catch(Exception e) {}
        
        // Now parse the peers
        for(int i = 0; i < binaryPeer.length(); i += 12) {
            addPeer(Peer.parse(binaryPeer.substring(i, i + 12), this));
        }
    }

    public void run() {
        downloading = true;
        checkResumeDownload();
        parseTracker();
        contactTracker();
        startPeers();
        relayer.start();
        try {
            while(!isDownloaded()) {
                Thread.sleep(1000);
            }

            cleanUp();
        }
        catch(InterruptedException e) {
            // Download manager has interrupted, we will need to
            // clean up here so that we can resume the download later. TODO
            // For now we are just going to exit.
            // Eventually we will use the cleanUp method to handle this, 
            // maybe pass a boolean to specify an interrupted download 
            // clean up.
            downloading = false; // Name this better later.
            stopped = true;
            closePeerConnections();
            stopRelayer();
        }    
    }

    private void startPeers() {
        /*
            Start our peers downloading.
        */
        for(Peer p : peers) {
            p.start();
        }
    }

    private void closePeerConnections() {
        synchronized(peers) {
            for(Peer p : peers) {
                p.closePeerConnection();
            }
        }
    }

    private void initBitfield() {
        byte [] b = new byte[pieces.length() / 20];
        bitfield = new Bitfield(Message.BITFIELD, b.length + 1, b);
    }

    private void initRequested() {
        requested = new HashMap<Request, Integer>();
        int l = Integer.parseInt(left);
        for(int i = 0; i < numberOfPieces; i++) {
            int put = 0;
            while(put < pieceLength) {
                if(l < getBlockSize()) {
                    // Last piece.
                    requested.put(new Request(Message.REQUEST, 13, i, put, l), 0);
                    put += l;
                    l -= put;
                    break;
                }
                else {
                    requested.put(new Request(Message.REQUEST, 13, i, put, 
                                                getBlockSize()), 0);
                    put += getBlockSize();
                    l -= getBlockSize();
                }
            }
        }
    }

    public void cleanUp() {
        closePeerConnections();
        fileWriter.writeFilesForFinishedDownload(); 
        stopRelayer();
    }

    private void stopRelayer() {
        try {
            relayer.sendEvent(); // Make sure interface is updated.
            relayer.interrupt();
            relayer.join();
        }
        catch(InterruptedException e) {
            System.out.println("Could not end Relayer");
            e.printStackTrace();
        }
    }

    private String computeHash() {
        /*
            Return the hash of the bencoded info dictionary.
        */
        String bencodedInfo = BEncoder.bencode(infoDict);
        String hex = null;
        try {
            hex = SHA1.hexdigest(bencodedInfo);
        }
        catch(Exception e) {}

        return hex;
    }

    private String percentEncode(String s) {
        /*
            Return a percent encoded version of String s
            Used when sending the infoHash to the tracker.
        */
        int len = s.length();
        char[] output = new char[len+len/2];
        int i=0;
        int j=0;
        while(i<len){
            output[j++] = '%';
            output[j++] = s.charAt(i++);
            output[j++] = s.charAt(i++);
        }
        return new String(output);
    }

    private String readFile(String fileName) throws FileNotFoundException {
        /*
            Read the contents of the metainfo file and return
            as a String.
        */
        StringBuilder builder = new StringBuilder();
        int c;
        FileInputStream in = null;
        if(new File(fileName).isFile()) {
            try {
                in = new FileInputStream(new File(fileName));

                while((c = in.read()) != -1) {
                    builder.append((char) c);
                }
                in.close();
            }
            catch(IOException e) {
              System.out.println("Error reading metainfo file.");
            }
            return builder.toString();
        }
        else {
            throw new FileNotFoundException();
        }
    }

    /*
        Peer and piece related methods ----------------------------------------
    */

    public void pieceFailed(Piece p) {
        /*
            Piece download wasn't valid, mark it as not downloaded
            so a Peer will pick it up as a request again.
        */
    }

    public synchronized void piece(Piece p) {
        /*
            TODO: Better error handling. Need to set a piece as failed if it
                    cannot be written.
        */
        try {
            fileWriter.piece(p);
            updateBitfield(p);
            updateDownloadStats(p.getBlock().length);
            if(percentDownloaded >= 99) {
                cancelPiece(p);
            }
        }
        catch(IOException e) {
            System.out.println("Could not write block to file.");
        }
    }

    private void updateBitfield(Piece p) {
        if(p.getOffset() + p.getBlock().length == getPieceLength()) {
            bitfield.setBit(p.getIndex());
        }
    }

    private void updateDownloadStats(int numBytesDownloaded) {
        left = "" + (Integer.parseInt(left) - numBytesDownloaded);
        downloaded = "" + (Integer.parseInt(downloaded) + numBytesDownloaded);
        double percent = (Double.parseDouble(left) / totalLength) * 100;
        percent = 100 - percent;
        percentDownloaded = (int) percent;
    }

    private int getBlockSize() {
        /*
            The size of a block to download from the client.
            The most commonly used is 2^14 (16kb)
        */
        return (int) Math.pow(2, 14);
    }

    public synchronized Request getNextRequest(Peer p) {
        Set<Request> requests = requested.keySet();
        for(Request r : requests) {
            if(requested.get(r) != 1) {
                if(p.canDownload(r.getIndex())) {
                    // Don't set a piece as requested if we are in end game.
                    if(percentDownloaded < 99)
                        requested.put(r, 1);
                    return r;
                }
            }
        }
        return null;
    }

    /**
    *   Close a peer connection and remove the peer from our list 
    *   of connections.
    *
    *   @param p    The peer to remove.
    *
    */
    public void removePeer(Peer p) {
        synchronized(peers) {
            p.closePeerConnection();
            peers.remove(p);
        }
        startNewPeers();
    }

    private void cancelPiece(Piece p) {
        /*
            During end game we send requests for pieces to more than one peer.
            Once we have received the piece we need to cancel the other requests.
        */
        Request toCancel = new Request(Message.REQUEST, 13, p.getIndex()
                    , p.getOffset(), p.getBlock().length);

        for(Peer peer : peers) {
            peer.cancel(toCancel);
        }
    }

    /*
        End of peer related methods -------------------------------------
    */

    /*
        Getters & Setters -----------------------------------------------------
    */

    /**
    *   @return     The name of the meta-info file of this Torrent.
    */
    public String getFileName() {
        return fileName;
    }

    /**
    *   @return     The info-dictionary of the meta-info file.
    */
    public Map infoDict() {
        return infoDict;
    }

    /**
    *   @return     The SHA1 hash of the info dictionary in the meta-info file.
    */
    public String infoHash() {
        return hash;
    }

    /**
    *   @return     The name of this Torrent.
    */
    public String name() {
        return name;
    }

    /**
    *   @return     The Peer ID of our client.
    */
    public String peerID() {
        return peerID;
    }

    /**
    *   @return     The port we are listening for incoming connections on.
    */
    public String port() {
        return port;
    }

    /**
    *   @return     The number of bytes downloaded by this Torrent.
    */
    public String downloaded() {
        return downloaded;
    }

    /**
    *   @return     The percentage of the download completed by this Torrent.
    */
    public int percentDownloaded() {
        return percentDownloaded;
    }

    /**
    *   @return     The number of bytes uploaded by this Torrent.
    */
    public String uploaded() {
        return uploaded;
    }

    /**
    *   @return     The number of bytes left to download.
    */
    public int left() {
        return Integer.parseInt(left);
    }

    /**
    *   @return     The number of pieces in this Torrent.
    */
    public int getNumberOfPieces() {
        return numberOfPieces;
    }

    /**
    *   @return     The length of a piece in this Torrent.
    */
    public int getPieceLength() {
        return pieceLength;
    }

    /**
    *   @return     True if this Torrent has finished downloading,
    *               false otherwise.
    */
    public boolean isDownloaded() {
        
        return Integer.parseInt(left) <= 0;
    }

    /**
    *   @return     The number of peers this Torrent is connected to.
    */
    public int numberOfConnections() {
        synchronized(peers) {
            return peers.size();
        }
    }

    /**
    *   @return     True if this Torrent is currently downloading, 
    *               false otherwise.
    */
    public boolean isDownloading() {
        return downloading;
    }

    /**
    *   @return     True if this Torrent is paused, false otherwise.
    */
    public boolean paused() {
        return paused;
    }

    /**
    *   @return     True if this download has been stopped.
    */
    public boolean stopped() {
        return stopped;
    }

    /*
        End of Getters & Setters ----------------------------------------------
    */

    /*
        Methods to be called by the download manager. -------------------------
    */

    /**
    *   Pause this Torrent. Pausing a Torrent involves pausing all of
    *   it's actively downloading Peers.
    *   
    *   @see btv.download.DLManager#pause(String)
    *   @see btv.download.peer.Peer#pause()
    */
    public void pause() {
        paused = true;
        synchronized(peers) {
            for(Peer p : peers) {
                p.pause();
            }
        }
    }

    /**
    *   This method is called on a paused Torrent which we wish to resume.
    */
    public void resumeDownload() {
        paused = false;
        synchronized(peers) {
            for(Peer p : peers) {
                p.resumeDownload();
            }
        }
    }

    /**
    *   Add a TorrentListener to this Torrent so information
    *   about this Torrent can be relayed to event handling classes.
    *
    *   @param t    The TorrentListener to add.
    */
    public void addTorrentListener(TorrentListener t) {
        if(t != null) {
            relayer.addTorrentListener(t);
        }
    }

    public ArrayList<TorrentListener> getTorrentListeners() {
        return relayer.getTorrentListeners();
    }

    /**
    *   @return     The list of peers this Torrent is currently connected to.
    */
    public ArrayList<Peer> getConnections() {
        synchronized(peers) {
            return peers;
        }
    }

    /**
    *   Add a PeerConnectionListener to this Torrent. This involves
    *   adding this listener to every connected peer as well as
    *   adding this listener to peers who connect in the future.
    *
    *   @param p    The PeerConnectionListener to add.
    *   @see btv.event.peer.PeerConnectionListener
    */
    public void addPeerConnectionListener(PeerConnectionListener p) {
        peerConnectionListener = p;
        synchronized(peers) {
            for(Peer peer : peers) {
                peer.addPeerConnectionListener(peerConnectionListener);
            }
        }
    }

    /**
    *   Add a PeerCommunicationListener to this Torrent. This involves
    *   adding this listener to the list of currently connected Peers
    *   as well as adding this listener to Peers who connect in the future.
    *
    *   @param p    The PeerCommunicationListener to add.
    *   @see btv.event.peer.PeerCommunicationListener
    *
    */
    public void addPeerCommunicationListener(PeerCommunicationListener p) {
        peerCommunicationListener = p;
        synchronized(peers) {
            for(Peer peer : peers) {
                peer.addPeerCommunicationListener(peerCommunicationListener);
            }
        }
    }

    /*
        End of download manager methods ---------------------------------------
    */
}