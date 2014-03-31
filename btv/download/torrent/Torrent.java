package btv.download.torrent;

import btv.bencoding.BDecoder;
import btv.bencoding.BEncoder;
import btv.bencoding.BDecodingException;
import btv.download.utils.SHA1;
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
    private Map metainfo, infoDict;
    private String hash;
    private String basePeerID = "-BTV001-";
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
    private ArrayList<Peer> peers;
    private Tracker tracker;
    private ArrayList<Tracker> trackers;
    private Bitfield bitfield;
    private HashMap<Request, Integer> requested;
    private int percentDownloaded;
    private boolean started = false;
    private boolean paused = false;

    private final static int MAX_CONNECTIONS = 10;

    // File related vars
    private RandomAccessFile file;
    private File tempFile;
    private ArrayList<TorrentFile> torrentFiles;

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
    *
    */
    public Torrent(String fileName) throws FileNotFoundException, 
                                        BDecodingException {

        String metaFileData = readFile(fileName);
        if(metaFileData != null) {
            
            metainfo = (Map) BDecoder.decode(fileName);
            infoDict = (Map) metainfo.get("info");
            pieceLength = (int) infoDict.get("piece length");
            pieces = (String) infoDict.get("pieces");
            setUpFiles();
            numberOfPieces = pieces.length() / 20;
            requested = new HashMap<Request, Integer>();

            initBitfield();
            initRequested();

            hash = computeHash();
            peerID = generatePeerID();
            peers = new ArrayList<Peer>();
            trackers = new ArrayList<Tracker>();

            relayer = new EventRelayer(this);
        }
        else {
            throw new FileNotFoundException();
        }
    }

    private void setUpFiles() {
        /*
            Create directories and create the temporary download file.
        */
        torrentFiles = new ArrayList<TorrentFile>();
        String tempFilePath;

        if(infoDict.containsKey("files")) {
            ArrayList<Map> files = (ArrayList<Map>) infoDict.get("files");
            name = (String) infoDict.get("name");
            
            tempFilePath = name + "/" + name + ".temp";

            for(Map map : files) {
                ArrayList<String> path = (ArrayList<String>) map.get("path");
                int fileLen = (int) map.get("length");
                totalLength += fileLen;
                String p = name;
                for(String s : path) {
                    p += "/" + s;
                }
                torrentFiles.add(new TorrentFile(p, fileLen));
            }
        }
        else {
            name = (String)infoDict.get("name");
            totalLength = (int)infoDict.get("length");
            tempFilePath = name + ".temp";
            torrentFiles.add(new TorrentFile(name, totalLength));
        }

        left = "" + totalLength;
        createTempFile(tempFilePath);
    }

    private void createTempFile(String tempFilePath) {
        /*
            We will use a tempory file to write to while downloading
            which will be split into all the files associated with the 
            Torrent at the end of the download
        */
        try {
            tempFile = new File(tempFilePath);
            if(tempFile.getParentFile() != null) {
                tempFile.getParentFile().mkdirs();
            }
            tempFile.createNewFile();
            file = new RandomAccessFile(tempFile, "rw"); // This is what we write to.
        }
        catch(IOException e) {
            System.out.println("Could not create temporary file.");
        }
    }

    private void deleteTempFile() {
        /*
            Delete the temporary file at the end of the download.
        */
        try {
            file.close();
            tempFile.delete();
        }
        catch(IOException e) {
            System.out.println("Could not delete temporary file.");
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
        synchronized(peers) {
            peers.add(p);
            p.addPeerConnectionListener(peerConnectionListener);
            p.addPeerCommunicationListener(peerCommunicationListener);
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
            if(peers.size() < MAX_CONNECTIONS) {
                addPeer(Peer.parse(binaryPeer.substring(i, i + 12), this));
            }
            else {
                break;
            }
        }
    }

    public void run() {
        started = true;
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
            started = false; // Name this better later.
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
        /*
            Close all peer connections.
        */
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

        // Make this a method ------------------------------------------
        if(torrentFiles.size() > 1) {
            int start = 0;
            // Now finalise the files.
            for(TorrentFile t : torrentFiles) {
                t.write(file, start);
                start += t.getLength();
            }
            deleteTempFile();
        }
        else {
            // Only one file, rename it to avoid writing to
            // the disk
            // Need to check for null torrent file here 
            File rename = new File(torrentFiles.get(0).getPath());
            tempFile.renameTo(rename);
            
        }

        // ------------------------------------------------------------   
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

    private String generatePeerID() {
        /*
            Return a String containging the peer id, which consists of
            the base id and a sequence of 12 random bytes.
        */
        String random = "";
        for(int i = 0; i < 12; i++) {
            random += 1 + ((int)(Math.random() * 9));
        }
        return basePeerID + random;
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

    private String readFile(String fileName) {
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
            return null;
        }
    }

    /*
        Peer related methods ----------------------------------------------
    */

    public synchronized void piece(Piece p) {
        /*
            A peer has downloaded a block, write it to file.

            TODO: Implement a file caching algorithm
                  Also, need to check hash of pieces.
        */
        try {
            int index = p.getIndex();
            int offset = p.getOffset();
            byte [] block = p.getBlock();

            file.seek((index * pieceLength) + offset);
            file.write(block);
            if(offset + block.length == getPieceLength()) {
                bitfield.setBit(index);
            }
            left = "" + (Integer.parseInt(left) - block.length);
            downloaded = "" + (Integer.parseInt(downloaded) + block.length);
            double percent = (Double.parseDouble(left) / totalLength) * 100;
            percent = 100 - percent;
            percentDownloaded = (int) percent;

            if(percentDownloaded >= 99) {
                cancelPiece(p);
            }
        }
        catch(IOException e) {
            System.out.println("Could not write block to file.");
        }
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
    public boolean isStarted() {
        return started;
    }

    /**
    *   @return     True if this Torrent is paused, false otherwise.
    */
    public boolean paused() {
        return paused;
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