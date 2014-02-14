import java.math.BigInteger;
class Have extends Message {
	private int pieceIndex;

	public Have(int id, int length, byte [] payload) {
		super(id, length, payload);
		pieceIndex = new BigInteger(payload).intValue();
	}

	public int getPieceIndex() {
		return pieceIndex;
	}

	public String toString() {
		return "" + pieceIndex;
	}
}