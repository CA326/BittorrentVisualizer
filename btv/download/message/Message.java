package btv.download.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
*   This class represents a Peer Wire Message.
*   It is used to communicate with BitTorrent peers.
*   This class will parse incoming messages from Peers and 
*   send messages to peers.
*
*   @author Stephan McLean
*
*/
public class Message {
    private int id, length;
    private byte [] payload = null;

    /*
        ID's
    */
    public static final int KEEP_ALIVE = -1;
    public static final int CHOKE = 0;
    public static final int UNCHOKE = 1;
    public static final int INTERESTED = 2;
    public static final int NOT_INTERESTED = 3;
    public static final int HAVE = 4;
    public static final int BITFIELD = 5;
    public static final int REQUEST = 6;
    public static final int PIECE = 7;
    public static final int CANCEL = 8;

    /**
    *   Constructor to set up a new messge
    *
    *   @param id1  The id of the message
    *   @param len  The length of the message
    *   @param b    The payload of the message, may be null.
    *
    */
    public Message(int id1, int len, byte [] b) {
        id = id1;
        length = len;
        payload = b;
    }

    /**
    *   Parse a {@code Message} from data coming from a DataInputStream
    *   
    *   @param len  The length of the message to parse.
    *   @param in   The DataInputStream to read the Message from
    *   @return     A new Message object containing the data read from the stream.
    *   @throws IOException     If there was an issue reading the data from
    *                           {@code in}
    *
    */
    public static Message parse(int len, DataInputStream in) throws IOException {
        if(len == 0) {
            return new Message(KEEP_ALIVE, len, null);
        }
        
        int messageID = in.readByte();

        if(messageID < 4) {
            // Normal message with no payload
            return new Message(messageID, len, null);
        }

        // Message with payload.
        byte [] payload = new byte[len - 1];
        in.readFully(payload);
        switch(messageID) {
            case 4:
                return new Have(HAVE, len, payload);
            case 5:
                return new Bitfield(BITFIELD, len, payload);
            case 7:
                return new Piece(PIECE, len, payload);
            default:
                return new Message(messageID, len, payload);
        }
    }

    /**
    *   @return     The ID of this {@code Message}
    */
    public int getID() {
        return id;
    }

    /**
    *   @return     The length of this {@code Message}
    */
    public int getLength() {
        return length;
    }

    /**
    *   @return     The payload of this {@code Message} whic may be null.
    */
    public byte [] getPayload() {
        return payload;
    }

    /**
    *   Sets the payload of this {@code Message} to {@code b}
    *
    *   @param b    The byte array to set as the payload for this {@code Message}
    */
    public void setPayload(byte [] b) {
        payload = b;
    }

    /**
    *   Send this message over {@code out}.
    *
    *   @param out  The DataOutputStream to send this message to.
    *
    */
    public void send(DataOutputStream out) {
        try {
            out.writeInt(length);

            if(id >= 0) {
                out.writeByte(id);
            }

            if(payload != null) {
                out.write(payload);
            }
        }
        catch(IOException e) {
            /*
                TODO: Make this method throw an IOException 
                rather than catch it.
            */
            System.out.println("Could not send message");
        }
    }
}