package btv.download.torrent;

import btv.download.message.Piece;

import java.util.Map;
import java.util.ArrayList;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
/**
*	This class is responsible for writing Torrent data to the
*	disk. This includes creating the temporary file, 
*	writing files at the end of a download and deciding when
*	to write files (caching).
*
*	@author Stephan McLean
*	@date 31st March 2014
*
*/
class TorrentFileWriter {
	/*
		TODO: Rename these vars.
	*/
    private Torrent torrent;
	private RandomAccessFile file;
    private File tempFile;
    private ArrayList<TorrentFile> torrentFiles;
    private String tempFilePath;

    public TorrentFileWriter(Torrent t) {
    	torrent = t;
        torrentFiles = new ArrayList<TorrentFile>();
    	setUpFiles(torrent.infoDict());
    	createTempFile();
    }

    private void setUpFiles(Map infoDict) {
    	if(infoDict.containsKey("files")) {
    		setUpMultiFileMode(infoDict);
    	}
    	else {
    		setUpSingleFileMode(infoDict);
    	}
    }

    private void setUpMultiFileMode(Map infoDict) {
    	ArrayList<Map> files = (ArrayList<Map>) infoDict.get("files");
        String name = (String) infoDict.get("name");      
        tempFilePath = name + "/" + name + ".temp";

        for(Map map : files) {
        	// This arraylist contains a list of strings 
        	// Each string is the name of a directory
        	// for the file.
            ArrayList<String> paths = (ArrayList<String>) map.get("path");
            int fileLen = (int) map.get("length");
            String path = name;
            for(String s : paths) {
                path += "/" + s;
            }
            torrentFiles.add(new TorrentFile(path, fileLen));
        }
    }

    private void setUpSingleFileMode(Map infoDict) {
    	String name = (String)infoDict.get("name");
        tempFilePath = name + ".temp";
        int length = (int) infoDict.get("length");
        torrentFiles.add(new TorrentFile(name, length));
    }

    private void createTempFile() {
        /*
            We will use a tempory file to write to while downloading
            which will be split into all the files associated with the 
            Torrent at the end of the download
        */
        try {
            tempFile = new File(tempFilePath);
            if(tempFile.getParentFile() != null) {
                tempFile.getParentFile().mkdirs();
            }
            tempFile.createNewFile();
            file = new RandomAccessFile(tempFile, "rw"); 
        }
        catch(IOException e) {
            System.out.println("Could not create temporary file.");
        }
    }

    private void deleteTempFile() {
        /*
            Delete the temporary file at the end of the download.
        */
        try {
            file.close();
            tempFile.delete();
        }
        catch(IOException e) {
            System.out.println("Could not delete temporary file.");
        }
    }

    public void piece(Piece p) throws IOException {
    	/*
			Torrent file has sent a piece. Write it for now, eventually
			we will have a caching algorithm
    	*/
		int index = p.getIndex();
        int offset = p.getOffset();
        byte [] block = p.getBlock();

        if(validPiece(p)) {
            file.seek((index * torrent.getPieceLength()) + offset);
            file.write(block);
        }
        else {
            torrent.pieceFailed(p);
        }
    }

    private boolean validPiece(Piece p) {
        /* 
            Check the SHA value of this piece.
            TODO: Implement this.
        */
        return true;
    }

    public void writeFilesForFinishedDownload() {
    	if(torrentFiles.size() > 1) {
            int start = 0;
            for(TorrentFile t : torrentFiles) {
                t.write(file, start);
                start += t.getLength();
            }
            deleteTempFile();
        }
        else {
            // Only one file, rename it to avoid writing to
            // the disk
            // Need to check for null torrent file here 
            File rename = new File(torrentFiles.get(0).getPath());
            tempFile.renameTo(rename);
        }
    }

    public RandomAccessFile getTempFile() {
    	return file;
    }
}