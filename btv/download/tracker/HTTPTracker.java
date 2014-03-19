package btv.download.tracker;
import btv.bencoding.BDecoder;
import btv.bencoding.BDecodingException;

import java.net.URL;
import java.util.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
/**
*    This class represents a HTTP tracker. It can be used to 
*    create a query to send to a tracker, contact the tracker
*    and return the Bdecoded response
*      
*    @author Stephan McLean
*/
public class HTTPTracker extends Tracker {
    private String request;
    private URL u;
    private BufferedReader in;

    /**
    *   This constructor takes a tracker address and various query values
    *   and sets up a query ready to send to the tracker.
    *
    *   @param tracker     The tracker address.
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
    public HTTPTracker(String tracker, String hash, String peerID, String port,
                    String downloaded, String uploaded, String left) {
        /*
            Format our announce String which will be used to contact
            this tracker.
        */
       super(tracker, hash, peerID, port, downloaded, uploaded, left);
       request = super.getTracker() + super.getQueries();
    }

    /**
    *   Send a request to this tracker to get a bdecoded response
    *
    *   @return     The Bdecoded tracker response in the form of a Map.
    *   @throws BDecodingException  If the tracker response cannot be parsed.
    */
    public Map contact() throws BDecodingException {
        StringBuffer response = new StringBuffer();
        String read = "";
        try {
            u = new URL(request);
            in = new BufferedReader(new InputStreamReader(u.openStream(), "ISO-8859-1"));

            while((read = in.readLine()) != null) {
                response.append(read);
            }

            in.close();
        }
        catch(IOException e) {
            System.out.println("Error contacting tracker.");
            e.printStackTrace();
        }
        return (Map) BDecoder.decode(response.toString());
    }
}