package btv.download.message;

import java.nio.ByteBuffer;
import java.math.BigInteger;

/**
*   This class represents a Peer wire Request message.
*   Peers send request messages when they want to receive a piece of the file.
*
*   @author Stephan McLean
*   @see btv.download.message.Message
*
*/
public class Request extends Message {
    private int index, begin, blength;

    /**
    *   This constructor is used when we receive a Request from a peer.
    *
    *   @param id   The ID of this message
    *   @param len  The length of this message.
    *   @param payload  The payload of this message.
    *
    */
    public Request(int id, int len, byte [] payload) {
        super(id, len, payload);
        byte [] i = new byte[4];
        byte [] b = new byte[4];
        byte [] bl = new byte [4];

        System.arraycopy(payload, 0, i, 0, 4);
        System.arraycopy(payload, 4, b, 0, 4);
        System.arraycopy(payload, 12, bl, 0, 4);

        index = new BigInteger(i).intValue();
        begin = new BigInteger(b).intValue();
        blength = new BigInteger(bl).intValue();
    }

    /**
    *   This constructor is used to set up a new Request message.
    *
    *   @param id   The ID of this message.
    *   @param len  The length of this message.
    *   @param i    The index of the piece to request.
    *   @param b    The offset of the piece to request.
    *   @param bl   The length of the piece to request.
    *
    */
    public Request(int id, int len, int i, int b, int bl) {
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

    /**
    *   @return     The index of the piece to request.
    */
    public int getIndex() {
        return index;
    }

    /**
    *   @return     The offset of the piece to request.
    */
    public int getOffset() {
        return begin;
    }

    /**
    *   @return     The length of the piece to request.
    */
    public int getBlockLength() {
        return blength;
    }

    /**
    *   Used to check if one Request is the same as another.
    *   Two requests are the same if they hold the same piece index
    *   and offset.
    *
    *   @param obj  The Request to compare this Request to.
    *   @return     True if the requests are equal, false otherwise.
    */
    public boolean equals(Object obj) {
        Request r = (Request) obj;
        return index == r.getIndex() && begin == r.getOffset();
    }
}