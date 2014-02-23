/*
    Class Message.

    Responsibilities:
        Represent a Peer wire Message.
        Parse incoming messages.
        Create messsages to be sent.
*/

package btv.download.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public Message(int id1, int len, byte [] b) {
        id = id1;
        length = len;
        payload = b;
    }

    public static Message parse(int len, DataInputStream in) {
        if(len == 0) {
            return new Message(KEEP_ALIVE, len, null);
        }

        try {
            int messageID = in.readByte();

            if(messageID < 4) {
                // Normal message with no payload
                return new Message(messageID, len, null);
            }

            byte [] payload = new byte[len - 1];
            in.readFully(payload);
            // Otherwise could be bitfield, piece, have etc.
            switch(messageID) {
                case 4:
                    return new Have(HAVE, len, payload);
                case 5:
                    return new Bitfield(BITFIELD, len, payload);
                case 7:
                    return new Piece(PIECE, len, payload);
            }

        }
        catch(IOException e) {
            System.out.println("Could not read message.");
        }
        return null;
    }

    public int getID() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public byte [] getPayload() {
        return payload;
    }

    public void setPayload(byte [] b) {
        payload = b;
    }

    public void send(DataOutputStream out) {
        try {
            out.writeInt(length);
            out.writeByte(id);

            if(payload != null) {
                out.write(payload);
            }
        }
        catch(IOException e) {
            System.out.println("Could not send message");
        }
    }
}