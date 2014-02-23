package btv.download.utils;
import java.security.*;
public class SHA1 {
    /*
     * A convenience class for generating a SHA1 checksum for a string.
     * To use this class:
     * 
     * String myString = "blah blah blah";
     * String cs = SHA1.hexdigest(myString);
     *
     * This class came from http://khaidoan.wikidot.com/java-sha1
     *
     */

    public static String hexdigest(String text) throws Exception {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

}