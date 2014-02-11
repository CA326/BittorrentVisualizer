/*
	This class will take Strings, ints, Lists
	& Maps and return a bencoded String.

	Author: Stephan McLean

*/

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
class BEncoder {
	
	static String bencode(Object o) {
		if(o instanceof String)
			return bencode((String) o);
		else if(o instanceof Integer)
			return bencode((int) o);
		else if(o instanceof ArrayList)
			return bencode((ArrayList<Object>) o);
		else
			return bencode((Map) o);
	}
	
	static String bencode(String s) {
		return s.length() + ":" + s;
	}
	
	static String bencode(int n) {
		return "i" + n + "e";
	}
	
	static String bencode(ArrayList<Object> a) {
		String result = "l";
	
		for(int i = 0; i < a.size(); i++) {
			result = result + bencode(a.get(i));
		}
		
		return result + "e";
	}
	
	static String bencode(Map m) {
		String result = "d";
	
		Set<Object> keys = m.keySet();
		for(Object o : keys) {
			result = result + bencode(o) + bencode(m.get(o));
		}
	
		return result + "e";
	}
}