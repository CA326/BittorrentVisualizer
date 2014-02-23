/*
	This class will listen for incoming peer connections.

	Author: Stephan McLean
	Date: 7th February 2014

*/

package btv.download.peer;
import btv.download.Torrent;

import java.net.*;
import java.io.*;

public class PeerListener extends Thread {
	private Torrent torrent;
	private ServerSocket server;

	public PeerListener(Torrent t) {
		torrent = t;
		try {
			server = new ServerSocket(Integer.parseInt(torrent.port()));
		}
		catch(IOException e) {
			System.out.println("Could not set up PeerListener");
		}
	}

	public void run() {
		try {
			while(true) {
				Socket s = server.accept();
				Peer p = new Peer(s);
				p.setIncoming(true);
				torrent.addPeer(p);
			}
		}
		catch(IOException e) {

		}
	}
}