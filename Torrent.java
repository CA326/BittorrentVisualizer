/*
	Class Torrent.
	Responsibilities: 
		Get peer list from tracker.
		Set up Peers.
		Decide which pieces to download.
		Write fully downloaded pieces to disk.
		Keep track of download/upload speed.
		More...

	Author: Stephan McLean
	Date: 6th February 2014

*/
import java.util.*;
import java.io.*;
import java.net.*;
import java.math.BigInteger;
public class Torrent extends Thread {
	private Map metainfo, infoDict;
	private String tracker; // Will have a list later.
	private String hash;
	private String basePeerID = "-BTV001-";
	private String peerID;
	private String port = "6881";
	private String downloaded = "0";
	private String uploaded = "0";
	private String left;
	private String name;
	private int pieceLength;
	private String pieces; // SHA1 values of each piece.
	private ArrayList<Peer> peers;
	private RandomAccessFile file;

	// Testing only
	private long startTime;

	public Torrent(String fileName) {
		/*	
			Parse the file. 
			Contact the tracker and print out the peer list.

			Need to handle a bad file better here.
		*/
		metainfo = (Map) BDecoder.decode(readFile(fileName));
		tracker = (String) metainfo.get("announce");
		infoDict = (Map) metainfo.get("info");
		left = "" + infoDict.get("length");
		pieceLength = (int) infoDict.get("piece length");
		pieces = (String) infoDict.get("pieces");
		name = (String) infoDict.get("name");
		setUpFile();
		System.out.println("Pieces: " + pieces.length() / 20);

		hash = computeHash();
		peerID = generatePeerID();
		peers = new ArrayList<Peer>();

		// Start timer
		startTime = System.currentTimeMillis();
	}

	public void contactTracker() {
		/*
			Set up our tracker and retrieve the response.
			We must then parse the response to retrieve our Peer list.
			Will deal with other data in the response later.

			TODO: Check for keys not in map.
		*/
		Tracker t = new Tracker(tracker, percentEncode(hash), peerID, port, downloaded,
								 uploaded, left);
		Map trackerResponse = t.contact();
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
		// Will change this later.
		peers.get(1).run();
	}

	public void addPeer(Peer p) {
		peers.add(p);
	}

	public void removePeer(Peer p) {
		System.out.println("Removing peer: " + p);
		peers.remove(p);
	}

	public void run() {
		contactTracker();
		try {
			while(!isDownloaded()) {
				Thread.sleep(1000);
			}

			// Downloaded, do clean up
			cleanUp();

			long stopTime = System.currentTimeMillis();
			System.out.println("Download took: " + ((stopTime - startTime) / 1000) + " seconds");
		}
		catch(Exception e) {
			System.out.println("Something went wrong.");
			e.printStackTrace();
		}
	}

	private void startPeers() {
		for(Peer p : peers) {
			p.run();
		}
	}

	public void cleanUp() {
		//for(Peer p : peers) {
		//	p.closePeerConnection();
		//}

		try {
			file.close();
		}
		catch(Exception e) {
			System.out.println("Could not close the file.");
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
			TODO: Improve this.
				  This method currently returns the percent encoded hash.
				  Percent encoding should be done elsewhere.
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
    		output[j++]='%';
      		output[j++]=s.charAt(i++);
      		output[j++]=s.charAt(i++);
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

	private void setUpFile() {
		try {
			file = new RandomAccessFile(new File(name), "rw");
		}
		catch(IOException e) {
			System.out.println("Could not create file: " + name);
		}
	}

	public int getBlockSize() {
		/*
			The size of a block to download from the client.
			The most commonly used is 2^14 (16kb)

			TODO:
				Need to take into account peers requesting
				part of a block. 
				Move this method?
		*/
		int blockSize = (int) Math.pow(2, 14);
		if(Integer.parseInt(left) < blockSize) {
			return Integer.parseInt(left);
		}
		else {
			return blockSize;
		}
	}

	public String infoHash() {
		return hash;
	}

	public String peerID() {
		return peerID;
	}

	public String port() {
		return port;
	}

	public String downloaded() {
		return downloaded;
	}

	public String uploaded() {
		return uploaded;
	}

	public String left() {
		return left;
	}

	public boolean isDownloaded() {
		
		return left.equals("0");
	}

	public void piece(int index, int offset, byte [] block) {
		/*
			A peer has downloaded a block, write it to file.

			TODO: Implement a file caching algorithm
				  Also, need to check hash of pieces.
		*/
		try {
			file.seek((index * pieceLength) + offset);
			file.write(block);
			left = "" + (Integer.parseInt(left) - block.length);
			System.out.println("Left: " + left);
		}
		catch(IOException e) {
			System.out.println("Could not write block to file.");
		}
	}
	
	public static void main(String [] args) {
		Torrent t = new Torrent(args[0]);
		t.start();
	}

}