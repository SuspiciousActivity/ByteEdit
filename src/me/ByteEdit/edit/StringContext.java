package me.ByteEdit.edit;

/**
 * This is a quiet fast String concatenation helper class. It appears to be a
 * lot faster than Java's StringBuilder class, especially for very large
 * Strings, which will be made by the Disassembler.
 */
public class StringContext {

	private String[] arrs;
	private int idx;
	private int len;

	public StringContext(int amt) {
		if (amt > 0)
			arrs = new String[amt];
	}

	public void next(String s) {
		len += (arrs[idx++] = s).length();
	}

	public String finish() {
		if (arrs == null)
			return "";
		char[] full = new char[len];
		int off = 0;
		for (int i = 0; i < idx; i++) {
			int len = arrs[i].length();
			arrs[i].getChars(0, len, full, off);
			off += len;
			arrs[i] = null;
		}
		String s = new String(full);
		arrs = null;
		full = null;
		return s;
	}

}
