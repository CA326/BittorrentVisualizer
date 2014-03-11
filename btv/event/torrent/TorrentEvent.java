/*
	This class will be used to relay torrent information to
	a client which is using TorrentListeners.

	Author: Stephan McLean
	Date: 11th March 2014
*/

package btv.event.torrent;

import java.util.EventObject;

public class TorrentEvent extends EventObject {
	private String name;
	private int downloaded;
	private int numberOfConnections;

	public TorrentEvent(Object source, String n, int d, int numConnections) {
		super(source);
		name = n;
		downloaded = d;
		numberOfConnections = numConnections;
	}

	public void setName(String n) {
		name = n;
	}

	public void setDownloaded(int d) {
		downloaded = d;
	}

	public String getName() {
		return name;
	}

	public int getDownloaded() {
		return downloaded;
	}

	public String toString() {
		return name + " " + downloaded + " Connections: " + numberOfConnections;
	}
}