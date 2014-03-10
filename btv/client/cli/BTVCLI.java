/*
	A very simple command line client which shows the use of our API

	Author: Stephan McLean
	Date: 10th March 2014.

*/

package btv.client.cli;

import btv.download.DLManager;
import java.util.*;

public class BTVCLI {
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
			d.start(torrents.get(index));
		}

		// Wait for downloads to finish
		while(!d.downloadsFinished()) {
			try {
				Thread.sleep(5000);
			}
			catch(Exception e) {}

			for(int i = 0; i < numTorrents; i++) {
				System.out.println(d.get(torrents.get(i)));
			}
		}
	}

	public void addTorrent(String fileName) {
		torrents.put(numTorrents, d.add(fileName));
		numTorrents++;
	}

	public static void main(String [] args) {
		BTVCLI b = new BTVCLI();
		for(int i = 0; i < args.length; i++) {
			b.addTorrent(args[i]);
		}
		b.run();
	}
}