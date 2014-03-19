package btv.download.message;

import java.math.BigInteger;
/**
*   This class describes a Peer wire Have message.
*
*   @author Stephan McLean
*   @see btv.download.message.Message
*
*/
public class Have extends Message {
    private int pieceIndex;

    /**
    *   Constructor to set up a new Have message.
    *
    *   @param id   The ID of this Have message
    *   @param length   The length of this message
    *   @param payload  The payload of this message
    *
    */
    public Have(int id, int length, byte [] payload) {
        super(id, length, payload);
        pieceIndex = new BigInteger(payload).intValue();
    }

    /**
    *   Get the piece index of this message
    *
    *   @return     The piece index of this message
    */
    public int getPieceIndex() {
        return pieceIndex;
    }
}