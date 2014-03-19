package btv.download.message;

import java.nio.ByteBuffer;
import java.math.BigInteger;

/**
*   This class represents a Peer wire Cancel message.
*   Peers send cancel messages when they want to inform another peer
*   that they no longer want to receive a Piece that they previously requested
*
*   @author Stephan McLean
*   @see btv.download.message.Message
*
*/
public class Cancel extends Message {
	private int index, begin, blength;

    /**
    *   Constructor to set up a new Cancel message.
    *
    *   @param id   The id of this cancel message.
    *   @param len  The length of this cancel message.
    *   @param i    The index of the request to cancel.
    *   @param b    The offset of the request to cancel.
    *   @param bl   The length of the request to cancel.
    *
    */
	public Cancel(int id, int len, int i, int b, int bl) {
	    super(id, len, null);
            byte [] payload = new byte[12];
            System.arraycopy(ByteBuffer.allocate(4).putInt(i).array(), 0, payload, 0, 4);
            System.arraycopy(ByteBuffer.allocate(4).putInt(b).array(), 0, payload, 4, 4);
            System.arraycopy(ByteBuffer.allocate(4).putInt(bl).array(), 0, payload, 8, 4);
            super.setPayload(payload);
            index = i;
            begin = b;
            blength = bl;
	}
}