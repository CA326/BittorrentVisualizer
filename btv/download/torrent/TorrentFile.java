package btv.download.torrent;

import java.nio.channels.FileChannel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;

/**
*	This class represents a file that will be written to the disk.
*
*	@author Stephan McLean
*	@date 3rd March 2014.
*/
public class TorrentFile {
	private String path;
	private int length;

	/**
	*	This constructor sets up a new TorrentFile
	*
	*	@param p 	The path of the file, including the file name
	*	@param l 	The length of the file.
	*/
	public TorrentFile(String p, int l) {
		path = p;
		length = l;
	}

	/**
	*	This method takes a RandomAccessFile which contains this files
	*	data and write the data to the disk.
	*
	*	@param r 	The RandomAccessFile which contains this files data.
	*	@start 		The position in the RandomAccessFile where this
	*				files data starts.
	*/
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

	/**
	*	@return 	The path of this file, including the file name.
	*/
	public String getPath() {
		return path;
	}

	/**
	*	@return 	The length of this file.
	*/
	public int getLength() {
		return length;
	}
}