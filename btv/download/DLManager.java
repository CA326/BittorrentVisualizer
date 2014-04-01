package btv.download;

import btv.bencoding.BDecodingException;
import btv.download.torrent.Torrent;
import btv.download.peer.Peer;
import btv.event.torrent.TorrentListener;
import btv.event.peer.PeerConnectionListener;
import btv.event.peer.PeerCommunicationListener;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.io.FileNotFoundException;

/**
*	The main interface to the BTV API. This class handles adding Torrents
*	, removing Torrents, pausing and stopping Torrents. It also handles
*	adding the event listeners to Torrents
*
*	@author Stephan McLean
*	@date 10th March 2014
*
*/
public class DLManager {
	private HashMap<String, Torrent> downloads;
	private String basePeerID = "-BTV001-";
	private String peerID;

	/**
	*	Constructor to set up a new download manager.
	*/
	public DLManager() {
		downloads = new HashMap<String, Torrent>();
		peerID = generatePeerID();
	}

	/**
	*	Add a new Torrent to the download manager.
	*
	*	@param fileName 	The name of the meta-info file of the Torrent.
	*
	*	@return 	The name of the Torrent which can be used to start, stop, 
	*				pause and remove the Torrent. It can also be used to add
	*				listeners to the torrent.
	*/
	public String add(String fileName) throws FileNotFoundException,
							 BDecodingException {

		Torrent t = new Torrent(fileName, peerID);
		String name = t.name();
		downloads.put(name, t);
		return name;	
	}

	/**
	*	Start the torrent with name downloading.
	*
	* 	@param name 	The name of the Torrent to start. 
	*
	*/
	public void start(String name) throws FileNotFoundException, 
												BDecodingException {
		if(downloads.containsKey(name)) {
			Torrent t = downloads.get(name);
			if(t.stopped()) {
				restart(t);
			}
			else if(t.paused()) {
				t.resumeDownload();
			}
			else if(!t.isDownloading()) {
				t.start();
			}
		}
	}

	private void restart(Torrent t) throws FileNotFoundException, 
													BDecodingException {
		Torrent newInstance = new Torrent(t.getFileName(), peerID);
		ArrayList<TorrentListener> listeners = t.getTorrentListeners();
		// TODO: Need to add peer listeners here as well
		for(TorrentListener listener : listeners) {
			newInstance.addTorrentListener(listener);
		}
		downloads.put(newInstance.name(), newInstance);
		newInstance.start();
	}

	/**
	*	Pauses an actively downloading Torrent. When a Torrent is paused
	*	it will attempt to retain it's active Peer connections
	*	while not downloading the file. It retains Peer connections by
	*	periodically sending keep alive messages to the peers.
	*
	*	@param name 	The name of the Torrent to pause.
	*
	*/
	public void pause(String name) {
		if(downloads.containsKey(name)) {
			downloads.get(name).pause();
		}
	}

	/**
	*	Stops an actively downloading Torrent. Stopping a Torrent will close
	*	all of it's connections to it's Peers. The Torrent will still be kept
	*	in the list of downloads.
	*
	*	@param name 	The name of the Torrent to stop
	*
	*/
	public void stop(String name) {
		if(downloads.containsKey(name)) {
			Torrent t = downloads.get(name);
			if(t.isDownloading()) {
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

	/**
	*	Remove a Torrent from the list of downloads. If the Torrent is actively
	*	downloading it will first be stopped and then removed.
	*
	*	@param name 	The name of the Torrent to remove.
	*	@see 	#stop(String)
	*
	*/
	public void remove(String name) {
		stop(name);
		downloads.remove(name);
	}

	/**
	*	Get the Torrent associated with with associated {@code name}
	*
	*	@param name 	The name of the Torrent to get.
	*	@see 	btv.download.torrent.Torrent
	*/
	public Torrent get(String name) {
		if(downloads.containsKey(name)) {
			return downloads.get(name);
		}
		else {
			return null; // Fix this
		}
	}

	/**
	*	Check if all downloads have finished.
	*
	*	@return 	True if every Torrent has finished downloading,
	*				false otherwise.
	*
	*/
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

	/**
	*	Get a list of the Peers the Torrent with {@code name} is currently
	*	connected to.
	*
	* 	@param name 	The name of the Torrent to get the list of Peers from.
	*	@see	btv.download.peer.Peer
	*
	*/
	public ArrayList<Peer> getConnections(String name) {
		ArrayList<Peer> result = null;
		if(downloads.containsKey(name)) {
			result = downloads.get(name).getConnections();
		}
		return result;
	}

	/**
	*	Add a TorrentListener to the Torrent with {@code name}
	*
	* 	@param t 	The TorrentListener to add to the Torrent
	*	@param name 	The name of the Torrent to add {@code t} to.
	*	@see	btv.event.torrent.TorrentListener
	*
	*/
	public void addTorrentListener(TorrentListener t, String name) {
		if(downloads.containsKey(name)) {
			downloads.get(name).addTorrentListener(t);
		}
	}

	/**
	*	Add a PeerConnectionListener to the Torrent with {@code name}
	*
	*	@param p 	The PeerConnectionListener to add to the Torrent.
	*	@param name 	The name of the Torrent to add the listener to.
	*	@see 	btv.event.peer.PeerConnectionListener
	*
	*/
	public void addPeerConnectionListener(PeerConnectionListener p, String name) {
		if(downloads.containsKey(name)) {
			downloads.get(name).addPeerConnectionListener(p);
		}
	}
	
	/**
	*	Add a PeerCommunicationListener to the Torrent with {@code name}
	*
	*	@param p 	The PeerCommunicationListener to add to the Torrent
	*	@param name 	The name of the Torrent to add the listener to.
	*	@see	btv.event.peer.PeerCommunicationListener
	*
	*/
	public void addPeerCommunicationListener(PeerCommunicationListener p, String name) {
		if(downloads.containsKey(name)) {
			downloads.get(name).addPeerCommunicationListener(p);
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
}