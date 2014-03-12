/*
	Classes that realize this interface can add a listener to a Torrent
	to receive information about the Torrent periodically.

	Author: Stephan McLean
	Date: 11th March 2014.

*/

package btv.event.torrent;

import java.util.EventListener;

public interface TorrentListener extends EventListener {
	public void handleTorrentEvent(TorrentEvent e);
}