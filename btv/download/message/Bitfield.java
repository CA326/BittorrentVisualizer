package btv.download.message;

import javax.xml.bind.DatatypeConverter;

/**
*   This class represents a Bitfield message.
*   BitTorrent peers send Bitfield messages to inform other peers
*   about the parts of the file they have
*
*   @author Stephan McLean
*   @see btv.download.message.Message
*/
public class Bitfield extends Message {
    private char [] bitfield;

    /**
    *   Constructor to set up a new Bitfield message.
    *
    *   @param id   The ID of the Bitfield message
    *   @param length   The length of the Bitfield message
    *   @param payload  The payload of the bitfield message.
    *
    */
    public Bitfield(int id, int length, byte [] payload) {
        super(id, length, payload);

        String s = DatatypeConverter.printHexBinary(payload);
        String binary = "";
        for(int i = 0; i < s.length(); i += 2) {
            int n = Integer.parseInt(s.substring(i, i + 2), 16);
            String theByte = Integer.toBinaryString(n);
            if(theByte.length() < 8) {
                // Add leading 0
                theByte = "0" + theByte;
            }
            binary += theByte;
        }

        bitfield = binary.toCharArray();
    }

    /**
    *   @return The byte array representation of this Bitfield
    */
    public byte [] getBitfield() {
        return super.getPayload();
    }

    /**
    *   Check if the bit at position {@code i} is set in this Bitfield
    *
    *   @param i    The position in the Bitfield to check.
    *   @return     True if the bit at position {@code i} is set,
    *               false otherwise.
    */
    public boolean bitSet(int i) {
        if(i >= bitfield.length || i < 0) {
            return false;
        }
        else {
            return bitfield[i] == '1';
        }
    }

    /**
    *   Set the bit at position {@code i} in this Bitfield
    *
    *   @param i    The position of the bit to set
    *
    */
    public void setBit(int i) {
        bitfield[i] = '1';
    }
}