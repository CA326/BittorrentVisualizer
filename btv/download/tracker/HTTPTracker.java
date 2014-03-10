/*
    This class will be used to communicate with the tracker.

    TODO: Make this class a thread so that we can query 
          the tracker at regular intervals.

*/

package btv.download.tracker;
import btv.bencoding.BDecoder;
import btv.bencoding.BDecodingException;

import java.net.URL;
import java.util.*; // TODO: Fix this
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
public class HTTPTracker extends Tracker {
    private String request;
    private URL u;
    private BufferedReader in;

    public HTTPTracker(String tracker, String hash, String peerID, String port,
                    String downloaded, String uploaded, String left) {
        /*
            Format our announce String which will be used to contact
            this tracker.
        */
       super(tracker, hash, peerID, port, downloaded, uploaded, left);
       request = super.getTracker() + super.getQueries();
    }

    public Map contact() throws BDecodingException {
        /*
            Send our request to the tracker and return the bdecoded
            response.
        */
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