package btv.event.torrent;

import java.util.EventListener;

/**
*	Classes that realize this interface can add a listener to a Torrent
*	to receive information about the Torrent periodically.
*
*	@author Stephan McLean
*	@date 11th March 2014.
*
*/
public interface TorrentListener extends EventListener {
	/**
	*	This method will be called at regular intervals to update
	*	the listener on the state of the torrent.
	*
	*	@param e 	The TorrentEvent that occured.
	*
	*/
	public void handleTorrentEvent(TorrentEvent e);
}