package btv.event.torrent;

import java.util.EventObject;

/**
*	This class will be used to relay torrent information to
*	a client which is using TorrentListeners.
*
*	@author Stephan McLean
*	@date 11th March 2014
*/
public class TorrentEvent extends EventObject {
	private String name;
	private int downloadedPercent;
	private String downloadedBytes;
	private int numberOfConnections;

	/**
	*	Constructor to set up a new TorrentEvent.
	*
	*	@param source 	The source of the event.
	*	@param name1 	The name of the Torrent associated with this event.
	*	@param down1 	The percent of the Torrent downloaded at the time of 
	*					this event occuring.
	*	@param numConnections 	The number of connected peers this torrent has
	*							when this event occurs.
	*	@param bytes 			The number of bytes the torrent has downloaded
	*							when this event occurs.
	*
	*/
	public TorrentEvent(Object source, String name1, int down1, int numConnections,
						String bytes) {
		super(source);
		name = name1;
		downloadedPercent = down1;
		downloadedBytes = bytes;
		numberOfConnections = numConnections;
	}

	/**
	*	Get the name of the Torrent associated with this event.
	*
	*	@return 	The name of the torrent associated with this event.
	*
	*/
	public String getName() {
		return name;
	}

	/**
	*	Get the percentage of the file download by the Torrent
	*
	*	@return		The percentage of the torrent file downloaded
	*				at the time of this event occuring.
	*/
	public int getDownloadedPercent() {
		return downloadedPercent;
	}

	/**
	*	Get the number of connected peers the Torrent has.
	*
	*	@return 	The number of connected peers the Torrent has when
	*				this event occurs.
	*/
	public int getConnections() {
		return numberOfConnections;
	}

	/**
	*	The number of bytes the Torrent has downloaded
	*
	*	@return 	The number of bytes downloaded by the torrent at the time
	*				of this event occuring.
	*/
	public String getDownloadedBytes() {
		return downloadedBytes;
	}
}