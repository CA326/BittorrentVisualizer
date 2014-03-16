/*
    This class will relay torrent events to listeners every 5 seconds.

    Author: Stephan McLean
    Date 12th March 2014
*/
package btv.download.torrent;

import btv.event.torrent.TorrentEvent;
import btv.event.torrent.TorrentListener;

import java.util.ArrayList;

public class EventRelayer extends Thread {
    private Torrent torrent;
    private ArrayList<TorrentListener> listeners;
    private boolean canRun = true;

    public EventRelayer(Torrent t) {
        torrent = t;
        listeners = new ArrayList<TorrentListener>();
    }

    public void run() {
        while(canRun) {
            try {
                sendEvent();
                Thread.sleep(5000);
            }
            catch(InterruptedException e) {
                canRun = false;
            }
        }
    }

    public void sendEvent() {
        // Send a TorrentEvent to every listener
        TorrentEvent event = new TorrentEvent(torrent, 
        torrent.name(), torrent.percentDownloaded(), 
        torrent.numberOfConnections(), torrent.downloaded());
        for(TorrentListener t : listeners) {
            t.handleTorrentEvent(event);
        }
    }

    public void addTorrentListener(TorrentListener t) {
        listeners.add(t);
    }
}