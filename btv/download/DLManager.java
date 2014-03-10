/*
	Class DLManager

	This class is the main interface to our BitTorrent API.
	It will be used to start Torrents and keep track of the downloads.

	Author: Stephan McLean
	Date: 10th March 2014

*/

package btv.download;

import btv.download.torrent.Torrent;
import java.util.ArrayList;

public class DLManager {
	private ArrayList<Torrent> downloads;

	public DLManager() {
		downloads = new ArrayList<Torrent>();
	}

	public String add(String fileName) {
		/*
			Given a fileName add this torrent
			and return its SHA1 hash which the client can use to
			add, pause, stop or remove the torrent later.

		*/
		Torrent t = new Torrent(fileName);
		String hash = t.infoHash();
		downloads.add(t);
		return hash;
	}

	public void start(String hash) {
		/*
			Start the download with the appropriate hash
		*/
		for(Torrent t : downloads) {
			if(t.infoHash().equals(hash)) {
				t.start();
				break;
			}
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
		for(Torrent t : downloads) {
			if(t.infoHash().equals(hash)) {
				return t;
			}
		}
		return null;
	}

	public boolean downloadsFinished() {
		boolean result = true;
		for(Torrent t : downloads) {
			if(!t.isDownloaded()) {
				result = false;
			}
		}

		return result;
	}

}