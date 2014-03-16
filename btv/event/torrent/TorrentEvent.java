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
	private int downloadedPercent;
	private String downloadedBytes;
	private int numberOfConnections;

	public TorrentEvent(Object source, String n, int d, int numConnections,
						String bytes) {
		super(source);
		name = n;
		downloadedPercent = d;
		downloadedBytes = bytes;
		numberOfConnections = numConnections;
	}


	public String getName() {
		return name;
	}

	public int getDownloadedPercent() {
		return downloadedPercent;
	}

	public int getConnections() {
		return numberOfConnections;
	}

	public String getDownloadedBytes() {
		return downloadedBytes;
	}

	public String toString() {
		return name + " " + downloadedPercent + " Connections: " 
		+ numberOfConnections;
	}
}