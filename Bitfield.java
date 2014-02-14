/*
	Class Bitfield.

	Responsibilities:
		Represent a Bittorrent Bitfield.
		Allow bits to be set in the bitfield
		Check for set bits.
		More...
*/
class Bitfield extends Message {
	private byte [] bitfield;

	public Bitfield(int id, int length, byte [] payload) {
		super(id, length, payload);
	}

	public byte [] getBitfield() {
		return super.getPayload();
	}

	public boolean bitSet(int i) {
		/*
			Is bit at index i set?
		*/
		return true;
	}

	public void setBit(int i) {
		/*
			Set bit at index i.
		*/
		return;
	}
}