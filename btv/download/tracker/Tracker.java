

package btv.download.tracker;

import btv.bencoding.BDecodingException;

import java.util.Map;

/**
*    This abstract class encapsulates the notion of a BitTorrent Tracker.
*    This class will be used by subclasses to set up the query String
*    which will be used to contact Trackers.
*
*    @author Stephan McLean
*    @date 3rd March 2014.
*
*/
public abstract class Tracker {
    private String tracker, queries;

    protected Tracker() {}

    /**
    *   This constructor takes a tracker address and various query values
    *   and stores the tracker address and the query string.
    *
    *   @param tracker1     The tracker address.
    *   @param hash         The SHA1 hash of the info dictionary of the Torrent.
    *   @param peerID       The peer ID of the BitTorrent client.
    *   @param port         The port the BitTorrent client will use to listen
    *                       for incoming peer connections
    *   @param downloaded   The number of bytes of this Torrent that have been
    *                       downloaded already.
    *   @param uploaded     The number of bytes of this Torrent that we have 
    *                       uploaded already.
    *   @param left         The number of bytes of this Torrent that we have
    *                       yet to download.
    */
    protected Tracker(String tracker1, String hash, String peerID, String port,
                    String downloaded, String uploaded, String left) {
        tracker = tracker1;

        queries = "?info_hash=" + hash + "&peer_id=" + peerID + 
                        "&port=" + port + "&downloaded=" + downloaded 
                        + "&uploaded=" + uploaded + "&left=" + left +
                        "&compact=1";
    }

    /**
    *   @return     The address of this Tracker.
    */
    public String getTracker() {
        return tracker;
    }

    /**
    *   @return     The query string that will be sent to this Tracker.
    */
    public String getQueries() {
        return queries;
    }

    /**
    *   This method will be used to contact a Tracker and will return
    *   their response as a bdecoded Map
    *   
    *   @return     The bdecoded Tracker response
    *   @throws BDecodingException  If the BDecoder cannot parse the 
    *                               trackers response.
    */
    public abstract Map contact() throws BDecodingException;
}