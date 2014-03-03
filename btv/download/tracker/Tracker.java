/*
    Class Tracker.

    Responsibilities:
        Incorporate functionality common to both HTTP & UDP trackers.

    Author: Stephan McLean
    Date: 3rd March 2014.

*/

package btv.download.tracker;

import btv.bencoding.BDecodingException;

import java.util.Map;

public abstract class Tracker {
    private String tracker, queries;

    protected Tracker() {}

    protected Tracker(String tracker1, String hash, String peerID, String port,
                    String downloaded, String uploaded, String left) {
        tracker = tracker1;

        queries = "?info_hash=" + hash + "&peer_id=" + peerID + 
                        "&port=" + port + "&downloaded=" + downloaded 
                        + "&uploaded=" + uploaded + "&left=" + left +
                        "&compact=1";
    }

    public String getTracker() {
        return tracker;
    }

    public String getQueries() {
        return queries;
    }

    public abstract Map contact() throws BDecodingException;
}