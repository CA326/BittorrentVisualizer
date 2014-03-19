package btv.bencoding;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
*	This class will bencode Strings, Lists, Integers and Maps.
*
*	@author Stephan McLean
*
*/
public class BEncoder {

	private BEncoder() {}
	
	/**
	*	Returns a bencoded String representation of a String, Integer
	*	List or Map.
	*
	*	@param o  The data to be bencoded.
	*	@return   A bencoded String.
	*
	*/
	public static String bencode(Object o) {
		if(o instanceof String)
			return bencode((String) o);
		else if(o instanceof Integer)
			return bencode((int) o);
		else if(o instanceof ArrayList)
			return bencode((ArrayList<Object>) o);
		else
			return bencode((Map) o);
	}
	
	private static String bencode(String s) {
		return s.length() + ":" + s;
	}
	
	private static String bencode(int n) {
		return "i" + n + "e";
	}
	
	private static String bencode(ArrayList<Object> a) {
		String result = "l";
	
		for(int i = 0; i < a.size(); i++) {
			result = result + bencode(a.get(i));
		}
		
		return result + "e";
	}
	
	private static String bencode(Map m) {
		String result = "d";
	
		Set<Object> keys = m.keySet();
		for(Object o : keys) {
			result = result + bencode(o) + bencode(m.get(o));
		}
	
		return result + "e";
	}
}