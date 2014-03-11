package btv.event.torrent;

import java.util.EventListener;

public interface TorrentListener extends EventListener {

	public void handleTorrentEvent(TorrentEvent e);
}