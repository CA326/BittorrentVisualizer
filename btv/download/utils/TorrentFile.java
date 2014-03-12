/*
	This class represents a file that will be written to the disk.

	Author: Stephan McLean
	Date: 3rd March 2014.
*/

package btv.download.utils;

import java.nio.channels.FileChannel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
public class TorrentFile {
	private String path;
	private int length;

	public TorrentFile(String p, int l) {
		path = p;
		length = l;
	}

	public void write(RandomAccessFile r, int start) {
		
		try {

			FileChannel in = r.getChannel();
			File f = new File(path);
			if(f.getParentFile() != null)
				f.getParentFile().mkdirs();
			f.createNewFile();

			FileChannel out = new FileOutputStream(f).getChannel();

			// Copy the file.
			in.position(start);
			in.transferTo(0, length, out);

			in.close();
			out.close();

		}
		catch(IOException e) {

		}
	}

	public String getPath() {
		return path;
	}

	public int getLength() {
		return length;
	}
}