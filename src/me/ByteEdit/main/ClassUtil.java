package me.ByteEdit.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.objectweb.asm.Type;

public class ClassUtil {
	
	public static final int ACC_PUBLIC = 0x0001;
	public static final int ACC_PRIVATE = 0x0002;
	public static final int ACC_PROTECTED = 0x0004;
	public static final int ACC_STATIC = 0x0008;
	public static final int ACC_FINAL = 0x0010;
	public static final int ACC_SYNCHRONIZED = 0x0020;
	public static final int ACC_SUPER = 0x0020;
	public static final int ACC_BRIDGE = 0x0040;
	public static final int ACC_VARARGS = 0x0080;
	public static final int ACC_NATIVE = 0x0100;
	public static final int ACC_INTERFACE = 0x0200;
	public static final int ACC_ABSTRACT = 0x0400;
	public static final int ACC_STRICTFP = 0x0800;
	public static final int ACC_SYNTHETIC = 0x1000;
	public static final int ACC_ANNOTATION = 0x2000;
	public static final int ACC_ENUM = 0x4000;
	public static final int ACC_MANDATED = 0x8000;
	
	public static int getIDFromClassNameForType(String s) {
		s = s.toUpperCase();
		switch (s) {
			case "V": {
				return 0;
			}
			case "Z": {
				return 1;
			}
			case "C": {
				return 2;
			}
			case "B": {
				return 3;
			}
			case "S": {
				return 4;
			}
			case "I": {
				return 5;
			}
			case "F": {
				return 6;
			}
			case "J": {
				return 7;
			}
			case "D": {
				return 8;
			}
			case "NAME": {
				return 10;
			}
			default: {
				return Integer.parseInt(s);
			}
		}
	}
	
	public static String getClassNameFromType(Type type) {
		switch (type.getSort()) {
			case 0: {
				return "V";
			}
			case 1: {
				return "Z";
			}
			case 2: {
				return "C";
			}
			case 3: {
				return "B";
			}
			case 4: {
				return "S";
			}
			case 5: {
				return "I";
			}
			case 6: {
				return "F";
			}
			case 7: {
				return "J";
			}
			case 8: {
				return "D";
			}
			case 9: {
				return getClassNameFromType(type.getElementType());
			}
			case 10: {
				return "Name";
			}
		}
		return Integer.toString(type.getSort());
	}
	
	public static String getArrayTypeByID(int id) {
		switch (id) {
			case 4:
				return "Z";
			case 5:
				return "C";
			case 6:
				return "F";
			case 7:
				return "D";
			case 8:
				return "B";
			case 9:
				return "S";
			case 10:
				return "I";
			case 11:
				return "J";
			default:
				return Integer.toString(id);
		}
	}
	
	public static int getArrayIDByType(String type) {
		switch (type) {
			case "Z":
				return 4;
			case "C":
				return 5;
			case "F":
				return 6;
			case "D":
				return 7;
			case "B":
				return 8;
			case "S":
				return 9;
			case "I":
				return 10;
			case "J":
				return 11;
			default:
				return Integer.parseInt(type);
		}
	}
	
	public static String getAccessFlagsClass(int access) {
		String s = "";
		if ((ACC_PUBLIC & access) != 0) {
			s += "public ";
		}
		if ((ACC_FINAL & access) != 0) {
			s += "final ";
		}
		if ((ACC_ABSTRACT & access) != 0 && (ACC_ANNOTATION & access) == 0 && (ACC_ENUM & access) == 0 && (ACC_INTERFACE & access) == 0) {
			s += "abstract ";
		}
		if ((ACC_SYNTHETIC & access) != 0) {
			s += "synthetic ";
		}
		if ((ACC_ANNOTATION & access) != 0) {
			s += "@interface ";
		} else if ((ACC_INTERFACE & access) != 0) {
			s += "interface ";
		} else if ((ACC_ENUM & access) != 0) {
			s += "enum ";
		} else {
			s += "class ";
		}
		return s;
	}
	
	public static String getAccessFlagsFull(int access) {
		int acc = 0;
		String s = "";
		if ((ACC_PUBLIC & access) != 0) {
			s += "public ";
			acc ^= ACC_PUBLIC;
		}
		if ((ACC_PRIVATE & access) != 0) {
			s += "private ";
			acc ^= ACC_PRIVATE;
		}
		if ((ACC_PROTECTED & access) != 0) {
			s += "protected ";
			acc ^= ACC_PROTECTED;
		}
		if ((ACC_STATIC & access) != 0) {
			s += "static ";
			acc ^= ACC_STATIC;
		}
		if ((ACC_FINAL & access) != 0) {
			s += "final ";
			acc ^= ACC_FINAL;
		}
		if ((ACC_SYNCHRONIZED & access) != 0) {
			s += "synchronized ";
			acc ^= ACC_SYNCHRONIZED;
		}
		if ((ACC_VARARGS & access) != 0) {
			s += "varargs ";
			acc ^= ACC_VARARGS;
		}
		if ((ACC_NATIVE & access) != 0) {
			s += "native ";
			acc ^= ACC_NATIVE;
		}
		if ((ACC_INTERFACE & access) != 0) {
			s += "interface ";
			acc ^= ACC_INTERFACE;
		}
		if ((ACC_ABSTRACT & access) != 0) {
			s += "abstract ";
			acc ^= ACC_ABSTRACT;
		}
		if ((ACC_STRICTFP & access) != 0) {
			s += "strictfp ";
			acc ^= ACC_STRICTFP;
		}
		if ((ACC_SYNTHETIC & access) != 0) {
			s += "synthetic ";
			acc ^= ACC_SYNTHETIC;
		}
		if ((ACC_BRIDGE & access) != 0) {
			s += "bridge ";
			acc ^= ACC_BRIDGE;
		}
		if ((ACC_ANNOTATION & access) != 0) {
			s += "annotation ";
			acc ^= ACC_ANNOTATION;
		}
		if ((ACC_ENUM & access) != 0) {
			s += "enum ";
			acc ^= ACC_ENUM;
		}
		if ((ACC_MANDATED & access) != 0) {
			s += "mandated ";
			acc ^= ACC_MANDATED;
		}
		if (acc == access) {
			return s;
		} else {
			return "0x" + Integer.toHexString(access) + " ";
		}
	}
	
	/**
	 * @return { JavaType, Import }
	 */
	public static String[] reverseDescField(String desc) {
		String type;
		String importName = null;
		int arrSize = 0;
		while (desc.startsWith("[")) {
			arrSize++;
			desc = desc.substring(1);
		}
		switch (desc.substring(0, 1)) {
			case "B":
				type = "byte";
				break;
			case "Z":
				type = "boolean";
				break;
			case "I":
				type = "int";
				break;
			case "J":
				type = "long";
				break;
			case "C":
				type = "char";
				break;
			case "D":
				type = "double";
				break;
			case "F":
				type = "float";
				break;
			case "S":
				type = "short";
				break;
			case "V":
				type = "void";
				break;
			case "L": {
				importName = desc.substring(1, desc.length() - 1).replace("/", ".");
				if (!importName.contains(".")) {
					type = importName;
					importName = null;
				} else {
					type = importName.split("\\.")[importName.split("\\.").length - 1];
				}
				break;
			}
			default:
				type = "ERROR";
				break;
		}
		for (int i = 0; i < arrSize; i++) {
			type += "[]";
		}
		return new String[] { type, importName };
	}
	
	/**
	 * @return { ReturnValue, ParameterValues, Imports } getrennt durch ";"
	 */
	public static String[] reverseDescMethod(String desc) {
		HashSet<String> imports = new HashSet<>();
		String[] returnType = reverseDescField(desc.split("\\)")[1]);
		if (returnType[1] != null) {
			imports.add(returnType[1]);
		}
		ArrayList<String> parameters = new ArrayList<>();
		String parameterType = desc.split("\\)")[0].substring(1);
		while (!parameterType.isEmpty()) {
			String type = "";
			int arrSize = 0;
			while (parameterType.startsWith("[")) {
				arrSize++;
				parameterType = parameterType.substring(1);
			}
			switch (parameterType.substring(0, 1)) {
				case "B":
					type = "byte";
					parameterType = parameterType.substring(1);
					break;
				case "Z":
					type = "boolean";
					parameterType = parameterType.substring(1);
					break;
				case "I":
					type = "int";
					parameterType = parameterType.substring(1);
					break;
				case "J":
					type = "long";
					parameterType = parameterType.substring(1);
					break;
				case "C":
					type = "char";
					parameterType = parameterType.substring(1);
					break;
				case "D":
					type = "double";
					parameterType = parameterType.substring(1);
					break;
				case "F":
					type = "float";
					parameterType = parameterType.substring(1);
					break;
				case "S":
					type = "short";
					parameterType = parameterType.substring(1);
					break;
				case "V":
					type = "void";
					parameterType = parameterType.substring(1);
					break;
				case "L": {
					String rofl = "";
					while (!parameterType.startsWith(";")) {
						rofl += parameterType.substring(0, 1);
						parameterType = parameterType.substring(1);
					}
					rofl += parameterType.substring(0, 1);
					parameterType = parameterType.substring(1);
					String importName = rofl.substring(1, rofl.length() - 1).replace("/", ".");
					if (!importName.contains(".")) {
						type = importName;
						importName = null;
					} else {
						type = importName.split("\\.")[importName.split("\\.").length - 1];
					}
					if (importName != null) {
						imports.add(importName);
					}
					break;
				}
				default:
					type = "ERROR";
					break;
			}
			for (int i = 0; i < arrSize; i++) {
				type += "[]";
			}
			parameters.add(type);
		}
		String importStr = "";
		for (String s : imports) {
			if (importStr.isEmpty()) {
				importStr += s;
			} else {
				importStr += ";" + s;
			}
		}
		String parameterStr = "";
		for (String s : parameters) {
			if (parameterStr.isEmpty()) {
				parameterStr += s;
			} else {
				parameterStr += ";" + s;
			}
		}
		return new String[] { returnType[0], parameterStr, importStr };
	}
	
	public static int countMethodParams(String desc) {
		int count = 0;
		String parameterType = desc.split("\\)")[0].substring(1);
		while (!parameterType.isEmpty()) {
			while (parameterType.startsWith("[")) {
				parameterType = parameterType.substring(1);
			}
			switch (parameterType.substring(0, 1)) {
				case "B":
				case "Z":
				case "I":
				case "J":
				case "C":
				case "D":
				case "F":
				case "S":
				case "V":
					parameterType = parameterType.substring(1);
					count++;
					break;
				case "L": {
					while (!parameterType.startsWith(";")) {
						parameterType = parameterType.substring(1);
					}
					parameterType = parameterType.substring(1);
					count++;
					break;
				}
				default:
					break;
			}
		}
		return count;
	}
	
	public static String getDecompiledValue(Object o, String desc) {
		switch (o.getClass().getName()) {
			case "java.lang.String": {
				return "\"" + StringEscapeUtils.escapeJava((String) o).replace("\n", "\\n").replace("\r", "\\r") + "\"";
			}
			case "java.lang.Integer": {
				if (desc.equals("Z")) {
					return ((int) o) == 0 ? "false" : "true";
				}
				return o.toString();
			}
			case "java.lang.Long": {
				return o.toString() + "l";
			}
			case "java.lang.Float": {
				return o.toString() + "f";
			}
			// case "java.util.ArrayList": {
			// ArrayList list = (ArrayList) o;
			// for (Object obj : list) {
			// System.out.println(obj.getClass().getName());
			// }
			// }
			default:
				return o.toString();
		}
	}
	
	public static Object getCastedValue(String o, String className) {
		switch (className) {
			case "java/lang/String": {
				return o;
			}
			case "java/lang/Integer": {
				return Integer.parseInt(o);
			}
			case "java/lang/Long": {
				return Long.parseLong(o);
			}
			case "java/lang/Float": {
				return Float.parseFloat(o);
			}
			case "java/lang/Double": {
				return Double.parseDouble(o);
			}
			case "java/lang/Boolean": {
				return Boolean.parseBoolean(o);
			}
			case "java/lang/Short": {
				return Short.parseShort(o);
			}
			case "java/lang/Byte": {
				return Byte.parseByte(o);
			}
			default:
				return o.toString();
		}
	}
	
	public static String getSimpleDesc(String type) {
		String desc = "";
		for (int i = 0; i < countArraySize(type); i++) {
			desc += "[";
		}
		type = type.replace("[]", "");
		if (VARS.containsKey(type)) {
			desc += VARS.get(type);
		} else {
			desc += "L" + type + ";";
		}
		return desc;
	}
	
	private static int countArraySize(String type) {
		int size = 0;
		for (byte b : type.getBytes()) {
			if (b == ']') {
				size++;
			}
		}
		return size;
	}
	
	public static final HashMap<String, String> VARS = new HashMap<>();
	static {
		VARS.put("byte", "B");
		VARS.put("char", "C");
		VARS.put("double", "D");
		VARS.put("float", "F");
		VARS.put("int", "I");
		VARS.put("long", "J");
		VARS.put("short", "S");
		VARS.put("boolean", "Z");
		VARS.put("void", "V");
	}
	
	public static int externalArrayTypeDimensionCount(String s) {
		int i = 0;
		int j = "[]".length();
		int k = s.length() - j;
		while (s.regionMatches(k, "[]", 0, j)) {
			i++;
			k -= j;
		}
		return i;
	}
	
	public static String internalMethodDescriptor(String returnType, String methodArgs) {
		returnType = returnType.replace(".", "/");
		methodArgs = methodArgs.replace(".", "/");
		StringBuffer buf = new StringBuffer();
		buf.append('(');
		ExternalTypeEnumeration type = new ExternalTypeEnumeration(methodArgs);
		while (type.hasMoreTypes()) {
			buf.append(getSimpleDesc(type.nextType()));
		}
		buf.append(')');
		buf.append(getSimpleDesc(returnType));
		return buf.toString();
	}
}
