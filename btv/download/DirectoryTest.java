package btv.download;
import btv.bencoding.*;

import java.util.*;
import java.io.*;
class DirectoryTest {

	private static String readFile(String fileName) {
        /*
            Read the contents of the metainfo file and return
            as a String.
        */
        StringBuilder builder = new StringBuilder();
        int c;
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(fileName));

            while((c = in.read()) != -1) {
                builder.append((char) c);
            }
            in.close();
        }
        catch(IOException e) {
            System.out.println("Error reading metainfo file.");
        }
        return builder.toString();
    }

	public static void main(String [] args) {
		Map m = (Map) BDecoder.decode(readFile(args[0]));
		Map info = (Map) m.get("info");

		if(info.containsKey("files")) {
			System.out.println("Multi-file mode");
			ArrayList<Map> files = (ArrayList<Map>) info.get("files");
			String name = (String) info.get("name");
			
			try {
				File f = new File(name + "/" + name + ".temp");
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			catch(IOException e) {}

			for(Map map : files) {
				ArrayList<String> path = (ArrayList<String>) map.get("path");
				String p = name;
				for(String s : path) {
					p += "/" + s;
				}
				TorrentFile t = new TorrentFile(p, (int) map.get("length"));
				t.write();
			}
		}
		else {
			System.out.println("Single file mode.");
			TorrentFile t = new TorrentFile((String)info.get("name"), (int)info.get("length"));
			t.write();
		}
	}

}

class TorrentFile {
	private String path;
	private int length;

	public TorrentFile(String p, int l) {
		path = p;
		length = l;
	}

	public void write() {
		try {
			File f = new File(path);
			if(f.getParentFile() != null)
				f.getParentFile().mkdirs();
			f.createNewFile();
		}
		catch(IOException e) {

		}
		System.out.println("Created file in: " + path);
	}
}