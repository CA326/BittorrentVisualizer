/*
	This is a simple class which will return a string representing
	how many bytes are in the given form e.g
	1 -> 1byte
	1024 -> 1 kb
	etc...

	Author: Stephan McLean
	Date: 16th March.
*/

package btv.download.utils;

public class ByteCalculator {
	private static final int KB = 1024;
	private static final int MB = 1048576;
	private static final int GB = 1073741824;

	public static String convert(String bytes) {
		int b = Integer.parseInt(bytes);
		if(b < KB) {
			return bytes + "bytes";
		}
		else if(b < MB) {
			double d = b / KB;
			return d + "KB";
		}
		else if(b < GB) {
			double d = b / MB;
			return d + "MB";
		}
		else {
			double d = b / GB;
			return d + "GB";
		}
	}
}