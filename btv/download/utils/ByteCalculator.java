package btv.download.utils;

import java.text.DecimalFormat;

/**
*	This is a simple class which will return a string representing
*	how many bytes are in the given form e.g
*	1 -> 1byte
*	1024 -> 1 kb
*	etc...
*
*	Author: Stephan McLean
*	Date: 16th March.
*/
public class ByteCalculator {
	/** The number of bytes in a kilobyte */
	private static final int KB = 1024;
	/** The number of bytes in a megabyte */
	private static final int MB = 1048576;
	/** The number of bytes in a gigabyte */
	private static final int GB = 1073741824;

	/**
	*	Convert a String containing a number of bytes
	*	to a String representing that number in either
	*	bytes, kilobytes, megabytes or gigabytes.
	*
	*	@param bytes 	The number of bytes to format.
	*	@return 	The formatted String as described before.
	*/
	public static String convert(String bytes) {
		int b = Integer.parseInt(bytes);
		DecimalFormat df = new DecimalFormat("#.##");
		if(b < KB) {
			return bytes + "bytes";
		}
		else if(b < MB) {
			double d = (double) b / KB;

			return df.format(d).toString() + "KB";
		}
		else if(b < GB) {
			double d = (double) b / MB;
			return df.format(d).toString() + "MB";
		}
		else {
			double d = (double) b / GB;
			return df.format(d).toString() + "GB";
		}
	}
}