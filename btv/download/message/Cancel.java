package btv.download.message;

import java.nio.ByteBuffer;
import java.math.BigInteger;

public class Cancel extends Message {
	private int index, begin, blength;


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