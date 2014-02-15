/*
	Class Bitfield.

	Responsibilities:
		Represent a Bittorrent Bitfield.
		Allow bits to be set in the bitfield
		Check for set bits.
		More...
*/
import javax.xml.bind.DatatypeConverter;
class Bitfield extends Message {
	private char [] bitfield;

	public Bitfield(int id, int length, byte [] payload) {
		super(id, length, payload);

		String s = DatatypeConverter.printHexBinary(payload);
		String binary = "";
		for(int i = 0; i < s.length(); i += 2) {
			int n = Integer.parseInt(s.substring(i, i + 2), 16);
			String theByte = Integer.toBinaryString(n);
			binary += theByte;
		}

		bitfield = binary.toCharArray();
	}

	public byte [] getBitfield() {
		return super.getPayload();
	}

	public boolean bitSet(int i) {
		/*
			Is bit at index i set?
		*/
		return bitfield[i] == '1';
	}

	public void setBit(int i) {
		/*
			Set bit at index i.
		*/
		bitfield[i] = '1';
	}
}