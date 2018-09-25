package me.ByteEdit.utils;

public class UnicodeUtils {
	
	public static String unescape(String s) {
		if (s == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		char unicodeBuffer = 0;
		int stage = 0;
		for (char c : s.toCharArray()) {
			if (c == '\\' && stage == 0) {
				stage = 1;
				continue;
			}
			if (stage == 1) {
				switch (c) {
					case 'b':
						sb.append('\b');
						stage = 0;
						break;
					case 't':
						sb.append('\t');
						stage = 0;
						break;
					case 'n':
						sb.append('\n');
						stage = 0;
						break;
					case 'r':
						sb.append('\r');
						stage = 0;
						break;
					case 'f':
						sb.append('\f');
						stage = 0;
						break;
					case '"':
						sb.append('"');
						stage = 0;
						break;
					case '\\':
						sb.append('\\');
						stage = 0;
						break;
					case 'u':
						stage++;
						break;
					default:
						throw new IllegalArgumentException("'\\" + c + "' is invalid.");
				}
				continue;
			} else if (stage > 1 && stage < 6) { // \u1234 => up to 6
				try {
					int i = Integer.parseInt(Character.toString(c), 16);
					unicodeBuffer += i << (4 * (5 - stage++)); // 4 bytes each, shift left byte * 4..3..2..1 and add to unicodeBuffer
					if (stage == 6) {
						sb.append(unicodeBuffer);
						stage = 0;
						unicodeBuffer = 0;
					}
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("'" + c + "' is not a valid hex value.");
				}
				continue;
			}
			sb.append(c);
		}
		if (stage != 0) {
			if (unicodeBuffer != 0) {
				throw new IllegalArgumentException("The unicode at the end is incomplete.");
			} else {
				throw new IllegalArgumentException("'\\' at the end is invalid.");
			}
		}
		return sb.toString();
	}
	
	public static String escapeWithSpaces(String s) {
		if (s == null) {
			return null;
		}
		return escape(s).replace(" ", "\\u0020");
	}
	
	public static String escape(String s) {
		if (s == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (isPrintable(c)) {
				sb.append(c);
			} else {
				switch (c) {
					case '\b':
						sb.append("\\b");
						break;
					case '\t':
						sb.append("\\t");
						break;
					case '\n':
						sb.append("\\n");
						break;
					case '\r':
						sb.append("\\r");
						break;
					case '\f':
						sb.append("\\f");
						break;
					case '"':
						sb.append("\\\"");
						break;
					case '\\':
						sb.append("\\\\");
						break;
					default:
						sb.append(String.format("\\u%04X", (int) c));
				}
			}
		}
		return sb.toString();
	}
	
	private static boolean isPrintable(char c) {
		switch (c) {
			case 'Ä':
			case 'ä':
			case 'Ö':
			case 'ö':
			case 'Ü':
			case 'ü':
			case 'ß':
			case '€':
			case '©':
			case '®':
			case '«':
			case '»':
			case '§':
			case '£':
			case '¥':
				return true;
			case '"':
			case '\\':
				return false;
			default:
				return c >= ' ' && c <= '~';
		}
	}
	
}
