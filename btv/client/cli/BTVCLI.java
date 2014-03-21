/*
	A very simple command line client which shows the use of our API

	Author: Stephan McLean
	Date: 10th March 2014.

*/

package btv.client.cli;

import btv.download.DLManager;
import btv.event.torrent.TorrentEvent;
import btv.event.torrent.TorrentListener;
import btv.bencoding.BDecodingException;

import java.util.*;
import java.io.FileNotFoundException;

class BTVCLI {
	private DLManager d;
	private HashMap<Integer, String> torrents;
	private int numTorrents = 0;

	public BTVCLI() {
		d = new DLManager();
		torrents = new HashMap<Integer, String>();
	}

	public void run() {
		Set<Integer> keys = torrents.keySet();
		for(Integer index : keys) {
			d.addTorrentListener(new MyTorrentListener(), torrents.get(index));
			d.start(torrents.get(index));
		}

		// Wait for downloads to finish
		while(!d.downloadsFinished()) {
			try {
				Thread.sleep(5000);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void addTorrent(String fileName) {
		try {
			String name = d.add(fileName);
			torrents.put(numTorrents, );
			numTorrents++;
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found: " + fileName);
		}
		catch(BDecodingException e) {
			System.out.println("Could not decode: " + fileName);
		}
	}

	class MyTorrentListener implements TorrentListener {
		public void handleTorrentEvent(TorrentEvent e) {
			System.out.println(e);
		}
	}

	public static void main(String [] args) {
		BTVCLI b = new BTVCLI();
		for(int i = 0; i < args.length; i++) {
			b.addTorrent(args[i]);
		}
		b.run();
	}
}