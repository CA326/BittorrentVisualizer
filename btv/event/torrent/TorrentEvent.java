/*
	This class will be used to relay torrent information to
	a client which is using TorrentListeners.

	Author: Stephan McLean
	Date: 11th March 2014
*/

package btv.event.torrent;

import java.util.EventObject;

public class TorrentEvent extends EventObject {
	private String hash;
	private int downloaded;
	private int numberOfConnections;

	public TorrentEvent(Object source, String h, int d, int numConnections) {
		super(source);
		hash = h;
		downloaded = d;
		numberOfConnections = numConnections;
	}

	public void setHash(String h) {
		hash = h;
	}

	public void setDownloaded(int d) {
		downloaded = d;
	}

	public String getHash() {
		return hash;
	}

	public int getDownloaded() {
		return downloaded;
	}

	public String toString() {
		return hash + " " + downloaded + " Connections: " + numberOfConnections;
	}
}