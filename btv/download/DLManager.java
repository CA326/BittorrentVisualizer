/*
	Class DLManager

	This class is the main interface to our BitTorrent API.
	It will be used to start Torrents and keep track of the downloads.

	Author: Stephan McLean
	Date: 10th March 2014

*/

package btv.download;

import btv.download.torrent.Torrent;
import btv.download.peer.Peer;
import btv.event.torrent.TorrentListener;
import btv.event.peer.PeerConnectionListener;
import btv.event.peer.PeerCommunicationListener;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

public class DLManager {
	private HashMap<String, Torrent> downloads;

	public DLManager() {
		downloads = new HashMap<String, Torrent>();
	}

	public String add(String fileName) {
		/*
			Given a fileName add this torrent
			and return its name which the client can use to
			add, pause, stop or remove the torrent later.

		*/
		Torrent t = new Torrent(fileName);
		String name = t.name();
		downloads.put(name, t);
		return name;
	}

	public void start(String name) {
		/*
			Start the download with the appropriate name
		*/
		if(downloads.containsKey(name)) {
			Torrent t = downloads.get(name);
			if(!t.isStarted()) {
				t.start();
			}
			else if(t.paused()) {
				t.resumeDownload();
			}
		}
	}

	public void pause(String name) {
		/*
			Pause the download with the appropriate name
		*/
		if(downloads.containsKey(name)) {
			downloads.get(name).pause();
		}
	}

	public void stop(String name) {
		/*
			Stop the download with the appropriate name.
		*/
		if(downloads.containsKey(name)) {
			Torrent t = downloads.get(name);
			if(t.isStarted()) {
				try {
					t.interrupt();
					t.join();
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void remove(String name) {
		stop(name);
		downloads.remove(name);
	}

	public Torrent get(String name) {
		return downloads.get(name);
	}

	public boolean downloadsFinished() {
		boolean result = true;
		Set<String> keys = downloads.keySet();
		for(String s : keys) {
			if(!downloads.get(s).isDownloaded()) {
				result = false;
			}
		}

		return result;
	}

	public ArrayList<Peer> getConnections(String name) {
		ArrayList<Peer> result = null;
		if(downloads.containsKey(name)) {
			result = downloads.get(name).getConnections();
		}
		return result;
	}

	public void addTorrentListener(TorrentListener t, String name) {
		if(downloads.containsKey(name)) {
			downloads.get(name).addTorrentListener(t);
		}
	}

	public void addPeerConnectionListener(PeerConnectionListener p, String name) {
		if(downloads.containsKey(name)) {
			downloads.get(name).addPeerConnectionListener(p);
		}
	}

	public void addPeerCommunicationListener(PeerCommunicationListener p, String name) {
		if(downloads.containsKey(name)) {
			downloads.get(name).addPeerCommunicationListener(p);
		}
	}

}