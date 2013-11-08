/*

    Class used to decode bencoded torrent files.

    Author: Stephan McLean

*/

import java.util.*;
import java.io.*;
class BDecoder {
	private static FileInputStream in = null;

    static Map decode(String fileName) {
		// Set up an input stream & decode the file.
		try {
		    in = new FileInputStream(new File(fileName));
		}
		catch(IOException e) {
		    System.out.println("Could not open file: " + fileName);
		}

		Map items = new LinkedHashMap();
		try {
		    while(in.available() > 0) {
			char c = readChar();
			items = readDictionary();
		    }
		}
		catch(IOException e) {
		    System.out.println("IO Error");
		}
		return items;
    }

    static String readString(int length) {
		// Read a string from in given the length
	
		String result = "";
    
		for(int i = 0; i < length; i++) {
		    char c = readChar();
		    result = result + c;
		}
    
		return result;
    }

    static int readInt() {
		// Read the int until we hit an "e"
    
		String intToParse = "";
    
		char c = readChar();
		while(c != 'e') {
		    intToParse = intToParse + c;
		    c = readChar();
		}
    
		return Integer.parseInt(intToParse);
    }

    static ArrayList<Object> readList() {
		// Read a list of items from the file.
		ArrayList<Object> items = new ArrayList<Object>();
    
		char c = readChar();
		while(c != 'e') {
		    Object i = readItem(c);
		    items.add(i);
		    c = readChar();
		}
    
		return items;
    }

    static Map readDictionary() {
		Map m = new LinkedHashMap();
		Object key, value;
		char c = readChar();
		while(c != 'e') {
	
		    key = readItem(c);
		    char d  = readChar();
		    value = readItem(d);
		    m.put(key, value);
		    c = readChar();
		}
		return m;
    }

    /*
	Helper methods ------------------------------

    */
    static Object readItem(char c) {
    	/*
			Return an appropriate item based on c having been read
			from the file.
    	*/

		Object o = new Object();
		if(Character.isDigit(c)) {
		    // Begins with a digit we know it's a string.
		    String len = getStringLength();
		    len = c + len;
		    String s = readString(Integer.parseInt(len));
		    o = (Object) s;
		}
		else {
		    switch(c) {
			case 'i':
			    int n = readInt();
			    o = (Object) n;
			    break;
			case 'l':
			    ArrayList<Object> list = readList();
			    o = (Object) list;
			    break;
			case 'd':
			    Map map = readDictionary();
			    o = (Object) map;
			    break;
			default:
			    System.out.println("Could not parse file.");	
			    System.exit(1);
	    	}
		}
		return o;
    }

    static char readChar() {
		// Reads a single character from the file.
		char c = '0';
		try {
		    c = (char)in.read();
		}
		catch(IOException e) {
		    System.out.println("IO Error");
		}
    
		return c;
    }

    static String getStringLength() {
	
    
		String result = "";
		char c = readChar();

		while(c != ':') {
		    result = result + c;
		    c = readChar();
		}
    
		return result;
    }

    /*
		End of helper methods ----------------------------
    */
}