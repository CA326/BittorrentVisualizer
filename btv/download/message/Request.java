package btv.download.message;

import java.nio.ByteBuffer;
import java.math.BigInteger;
public class Request extends Message {
    private int index, begin, blength;

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

    public String toString() {
        return "ID: " + super.getID() + " Len: " + super.getLength() 
                + "Index: " + index + " Begin: " + begin + " BLength: " + 
                blength;
    }

    public int getIndex() {
        return index;
    }

    public int getOffset() {
        return begin;
    }

    public int getBlockLength() {
        return blength;
    }
}