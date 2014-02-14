import java.math.BigInteger;
class Piece extends Message {
	private int index, begin;
	private byte [] block;

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

	public int getIndex() {
		return index;
	}

	public int getOffset() {
		return begin;
	}

	public byte [] getBlock() {
		return block;
	}
}