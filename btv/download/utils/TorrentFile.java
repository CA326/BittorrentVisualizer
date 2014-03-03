/*
	This class represents a file that will be written to the disk.

	Author: Stephan McLean
	Date: 3rd March 2014.
*/

package btv.download.utils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
public class TorrentFile {
	private String path;
	private int length;

	public TorrentFile(String p, int l) {
		path = p;
		length = l;
	}

	public void write(RandomAccessFile r, int start) {
		/*
			TODO: Take seek into account here.
		*/
		System.out.println("Writing file: " + path + " Len: " + length);
		try {
			File f = new File(path);
			if(f.getParentFile() != null)
				f.getParentFile().mkdirs();
			f.createNewFile();

			RandomAccessFile out = new RandomAccessFile(f, "rw");
			r.seek(start);
			for(int i = 0; i < length; i++) {
				out.write(r.read());
			}

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