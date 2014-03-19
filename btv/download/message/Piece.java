package btv.download.message;
import java.math.BigInteger;

/**
*   This class describes a Peer wire Piece message.
*   Peers send and receive Pieces to download their respective Torrent
*
*   @author Stephan McLean
*   @see btv.download.message.Message
*
*/
public class Piece extends Message {
    private int index, begin;
    private byte [] block;

    /**
    *   This constructor sets up a new Piece message.
    *
    *   @param id   The ID of this message
    *   @param length   The length of this message.
    *   @param payload  The payload of this message.
    *
    */
    public Piece(int id, int length, byte [] payload) {
        super(id, length, payload);

        byte [] i = new byte[4];
        byte [] b = new byte[4];
        block = new byte[length - 9];

        System.arraycopy(payload, 0, i, 0, 4);
        System.arraycopy(payload, 4, b, 0, 4);
        System.arraycopy(payload, 8, block, 0, block.length);

        index = new BigInteger(i).intValue();
        begin = new BigInteger(b).intValue();
    }

    /**
    *   @return     The index of this piece.
    */
    public int getIndex() {
        return index;
    }

    /**
    *   @return     The offset within this piece index.
    */
    public int getOffset() {
        return begin;
    }

    /**
    *   @return     The data associated with this piece.
    */
    public byte [] getBlock() {
        return block;
    }
}