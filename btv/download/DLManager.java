/*
	Class DLManager

	This class is the main interface to our BitTorrent API.
	It will be used to start Torrents and keep track of the downloads.

	Author: Stephan McLean
	Date: 10th March 2014

*/

package btv.download;

import btv.download.torrent.Torrent;
import btv.event.torrent.TorrentListener;

import java.util.HashMap;
import java.util.Set;

public class DLManager {
	private HashMap<String, Torrent> downloads;

	public DLManager() {
		downloads = new HashMap<String, Torrent>();
	}

	public String add(String fileName) {
		/*
			Given a fileName add this torrent
			and return its SHA1 hash which the client can use to
			add, pause, stop or remove the torrent later.

		*/
		Torrent t = new Torrent(fileName);
		String hash = t.infoHash();
		downloads.put(hash, t);
		return hash;
	}

	public void start(String hash) {
		/*
			Start the download with the appropriate hash
		*/
		if(downloads.containsKey(hash)) {
			downloads.get(hash).start();
		}
	}

	public void pause(String hash) {
		/*
			Pause the download with the appropriate hash
		*/
	}

	public void stop(String hash) {
		/*
			Stop the download with the appropriate hash.
		*/
	}

	public Torrent get(String hash) {
		return downloads.get(hash);
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

	public void addTorrentListener(TorrentListener t, String hash) {
		if(downloads.containsKey(hash)) {
			downloads.get(hash).addTorrentListener(t);
		}
	}

}