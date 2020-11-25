package me.ByteEdit.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.ByteEdit.utils.UnicodeUtils;

public class HugeStrings {

	public static int THRESHOLD = 1000;
	private final HashMap<String, HugeData> map = new HashMap<>();

	public String onString(String s) {
		synchronized (map) {
			HugeData i = map.get(s);
			if (i == null)
				map.put(s, i = new HugeData(map.size(), UnicodeUtils.escape(this, s, true)));
			return "#" + Integer.toString(i.id);
		}
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public String makeStringsList() {
		StringContext ctx = new StringContext(map.size());
		List<HugeData> list = new ArrayList<>(map.values());
		list.sort((o1, o2) -> Integer.compare(o1.id, o2.id));
		for (HugeData hd : list) {
			StringBuilder sb = new StringBuilder(20 + hd.escaped.length());
			sb.append('#');
			sb.append(Integer.toString(hd.id));
			sb.append(": ");
			sb.append(hd.escaped);
			sb.append('\n');
			ctx.next(sb.toString());
		}
		return ctx.finish();
	}

	private static class HugeData {
		int id;
		String escaped;

		public HugeData(int id, String escaped) {
			this.id = id;
			this.escaped = escaped;
		}
	}

}
