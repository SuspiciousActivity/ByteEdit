package me.ByteEdit.edit;

import java.util.HashMap;

public class HugeStringsRev {

	private final HashMap<Integer, String> map = new HashMap<>();

	public void add(int i, String s) {
		map.put(Integer.valueOf(i), s);
	}

	public String get(int i) {
		String s = map.get(Integer.valueOf(i));
		if (s == null)
			s = "#" + Integer.toString(i);
		return s;
	}

}
