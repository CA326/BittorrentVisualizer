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

import java.text.DecimalFormat;

public class ByteCalculator {
	private static final int KB = 1024;
	private static final int MB = 1048576;
	private static final int GB = 1073741824;

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