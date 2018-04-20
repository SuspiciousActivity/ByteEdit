package me.ByteEdit.main;

import java.awt.Color;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.BadLocationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class Assembler {

	public static ClassNode assemble(String input) {
		Main.textArea.removeAllLineHighlights();
		CustomBufferedReader read = null;
		try {
			ClassNode clazz = new ClassNode();
			read = new CustomBufferedReader(new StringReader(input));
			String s;

			while (!(s = read.readLine()).startsWith("// #Class v:")) {
				if (clazz.visibleAnnotations == null) {
					clazz.visibleAnnotations = new ArrayList<>();
				}
				clazz.visibleAnnotations.add(parseAnnotation(s));
			}

			clazz.version = Integer.parseInt(s.substring(12));

			while (!(s = read.readLine()).startsWith("// #SourceFile: ")) {
				s = s.substring(0, s.length() - 2);
				if (s.contains(" implements ")) {
					String interfaces = s.substring(s.indexOf(" implements ") + 12);
					clazz.interfaces = new ArrayList<>();
					for (String interf : interfaces.split(", ")) {
						clazz.interfaces.add(interf);
					}
					s = s.substring(0, s.indexOf(" implements "));
				}
				clazz.superName = s.substring(s.indexOf(" extends ") + 9);
				s = s.substring(0, s.indexOf(" extends "));

				String[] split = s.split(" ");
				clazz.name = split[split.length - 1];

				clazz.access = ClassUtil.ACC_SUPER;

				switch (split[split.length - 2]) {
				case "class":
					break;
				case "enum":
					clazz.access ^= ClassUtil.ACC_ABSTRACT;
					clazz.access ^= ClassUtil.ACC_ENUM;
					break;
				case "interface":
					clazz.access ^= ClassUtil.ACC_ABSTRACT;
					clazz.access ^= ClassUtil.ACC_INTERFACE;
					break;
				case "@interface":
					clazz.access ^= ClassUtil.ACC_ABSTRACT;
					clazz.access ^= ClassUtil.ACC_ANNOTATION;
					break;
				}

				if (split.length > 2) {
					String cons = consolidateStrings(split, 0, split.length - 2);
					if (cons.contains("public")) {
						clazz.access ^= ClassUtil.ACC_PUBLIC;
					}
					if (cons.contains("final")) {
						clazz.access ^= ClassUtil.ACC_FINAL;
					}
					if (cons.contains("synthetic")) {
						clazz.access ^= ClassUtil.ACC_SYNTHETIC;
					}
					if (cons.contains("abstract")) {
						clazz.access ^= ClassUtil.ACC_ABSTRACT;
					}
				}

			}

			clazz.sourceFile = s.substring(16).equals("null") ? null : s.substring(16);

			while (!(s = read.readLine()).equals("// #Fields")) {
			}

			clazz.fields = new ArrayList<>();
			clazz.methods = new ArrayList<>();

			ArrayList<String> annotationsForNext = new ArrayList<>();
			while (!(s = read.readLine()).equals("// #Methods")) {
				s = s.trim();
				if (s.isEmpty())
					continue;
				if (s.startsWith("@")) {
					annotationsForNext.add(s);
				} else {
					String[] split = s.split(" ");
					boolean hasValue = false;
					if (s.contains(" = ")) {
						hasValue = true;
					}
					Object value = hasValue ? split[split.length - 1] : null;
					if (hasValue) {
						split = s.substring(0, s.indexOf(" = ")).split(" ");
					}
					String name = split[split.length - 1];
					String desc = split[split.length - 2];

					value = getValue((String) value, desc);

					int access = 0;

					if (split.length > 2) {
						String cons = consolidateStrings(split, 0, split.length - 2);
						if (cons.contains("public")) {
							access ^= ClassUtil.ACC_PUBLIC;
						}
						if (cons.contains("private")) {
							access ^= ClassUtil.ACC_PRIVATE;
						}
						if (cons.contains("protected")) {
							access ^= ClassUtil.ACC_PROTECTED;
						}
						if (cons.contains("static")) {
							access ^= ClassUtil.ACC_STATIC;
						}
						if (cons.contains("final")) {
							access ^= ClassUtil.ACC_FINAL;
						}
						if (cons.contains("synchronized")) {
							access ^= ClassUtil.ACC_SYNCHRONIZED;
						}
						if (cons.contains("bridge")) {
							access ^= ClassUtil.ACC_BRIDGE;
						}
						if (cons.contains("varargs")) {
							access ^= ClassUtil.ACC_VARARGS;
						}
						if (cons.contains("native")) {
							access ^= ClassUtil.ACC_NATIVE;
						}
						if (cons.contains("interface")) {
							access ^= ClassUtil.ACC_INTERFACE;
						}
						if (cons.contains("abstract")) {
							access ^= ClassUtil.ACC_ABSTRACT;
						}
						if (cons.contains("strictfp")) {
							access ^= ClassUtil.ACC_STRICTFP;
						}
						if (cons.contains("synthetic")) {
							access ^= ClassUtil.ACC_SYNTHETIC;
						}
						if (cons.contains("annotation")) {
							access ^= ClassUtil.ACC_ANNOTATION;
						}
						if (cons.contains("enum")) {
							access ^= ClassUtil.ACC_ENUM;
						}
						if (cons.contains("mandated")) {
							access ^= ClassUtil.ACC_MANDATED;
						}
					}
					FieldNode node = new FieldNode(access, name, desc, null, value);
					if (!annotationsForNext.isEmpty()) {
						if (node.visibleAnnotations == null) {
							node.visibleAnnotations = new ArrayList<>();
						}
						for (String anno : annotationsForNext) {
							node.visibleAnnotations.add(parseAnnotation(anno));
						}
						annotationsForNext.clear();
					}
					clazz.fields.add(node);
				}
			}

			HashMap<String, HashMap<Label, Integer>> classLabelMap;
			if ((classLabelMap = Main.labels.get(clazz.name)) == null) {
				classLabelMap = new HashMap<String, HashMap<Label, Integer>>();
				Main.labels.put(clazz.name, classLabelMap);
			} else {
				classLabelMap.clear();
			}
			HashMap<Label, Integer> methodLabelMap = null;

			annotationsForNext.clear();
			MethodNode node = null;
			String temp = "";

			ArrayList<String> localVarsToParse = new ArrayList<>();

			while ((s = read.readLine()) != null) {
				if (s.trim().isEmpty() || s.equals("}"))
					continue;
				s = s.substring(1);
				if (s.startsWith("@")) {
					annotationsForNext.add(s.trim());
				} else {
					if (s.startsWith("// #Max: ")) {
						node = new MethodNode();
						node.exceptions = new ArrayList<>();
						node.tryCatchBlocks = new ArrayList<>();
						node.localVariables = new ArrayList<>();
						node.instructions = new InsnList();
						s = s.substring(11);
						node.maxLocals = Integer.parseInt(s.split(" ")[0]);
						node.maxStack = Integer.parseInt(s.split(" ")[1].substring(2));
						localVarsToParse.clear();
					} else if (s.equals("// #LocalVars:")) {
					} else if (s.startsWith("// ")) { // LocalVars
						s = s.substring(3);
						localVarsToParse.add(s);
					} else if (s.equals("}")) {
						if (!annotationsForNext.isEmpty()) {
							if (node.visibleAnnotations == null) {
								node.visibleAnnotations = new ArrayList<>();
							}
							for (String anno : annotationsForNext) {
								node.visibleAnnotations.add(parseAnnotation(anno));
							}
							annotationsForNext.clear();
						}

						for (String st : localVarsToParse) {
							String[] sp = st.split(":");
							int start = Integer.parseInt(sp[3].substring(0, sp[3].length() - 2));
							int end = Integer.parseInt(sp[4]);
							LabelNode _start = null;
							LabelNode _end = null;
							for (Entry<Label, Integer> entry : methodLabelMap.entrySet()) {
								if (entry.getValue() == start) {
									_start = new LabelNode(entry.getKey());
								}
								if (entry.getValue() == end) {
									_end = new LabelNode(entry.getKey());
								}
							}
							node.localVariables.add(new LocalVariableNode(sp[0], sp[1].substring(1, sp[1].length() - 2),
									null, _start, _end, Integer.parseInt(sp[2].substring(0, sp[2].length() - 2))));
						}
						clazz.methods.add(node);
					} else if (!s.startsWith("\t")) {
						s = s.substring(0, s.length() - 2);
						if (s.contains(" throws ")) {
							String exceptions = s.substring(s.indexOf(" throws ") + 8);
							for (String excp : exceptions.split(", ")) {
								node.exceptions.add(excp);
							}
							s = s.substring(0, s.indexOf(" throws "));
						}
						String[] split = s.split(" ");
						node.desc = split[split.length - 1];
						node.name = split[split.length - 2];
						int access = 0;
						if (split.length > 2) {
							String cons = consolidateStrings(split, 0, split.length - 2);
							if (cons.contains("public")) {
								access ^= ClassUtil.ACC_PUBLIC;
							}
							if (cons.contains("private")) {
								access ^= ClassUtil.ACC_PRIVATE;
							}
							if (cons.contains("protected")) {
								access ^= ClassUtil.ACC_PROTECTED;
							}
							if (cons.contains("static")) {
								access ^= ClassUtil.ACC_STATIC;
							}
							if (cons.contains("final")) {
								access ^= ClassUtil.ACC_FINAL;
							}
							if (cons.contains("synchronized")) {
								access ^= ClassUtil.ACC_SYNCHRONIZED;
							}
							if (cons.contains("bridge")) {
								access ^= ClassUtil.ACC_BRIDGE;
							}
							if (cons.contains("varargs")) {
								access ^= ClassUtil.ACC_VARARGS;
							}
							if (cons.contains("native")) {
								access ^= ClassUtil.ACC_NATIVE;
							}
							if (cons.contains("interface")) {
								access ^= ClassUtil.ACC_INTERFACE;
							}
							if (cons.contains("abstract")) {
								access ^= ClassUtil.ACC_ABSTRACT;
							}
							if (cons.contains("strictfp")) {
								access ^= ClassUtil.ACC_STRICTFP;
							}
							if (cons.contains("synthetic")) {
								access ^= ClassUtil.ACC_SYNTHETIC;
							}
							if (cons.contains("annotation")) {
								access ^= ClassUtil.ACC_ANNOTATION;
							}
							if (cons.contains("enum")) {
								access ^= ClassUtil.ACC_ENUM;
							}
							if (cons.contains("mandated")) {
								access ^= ClassUtil.ACC_MANDATED;
							}
						}
						node.access = access;
						if ((methodLabelMap = classLabelMap.get(node.name + "|" + node.desc)) == null) {
							methodLabelMap = new HashMap<Label, Integer>();
							classLabelMap.put(node.name + "|" + node.desc, methodLabelMap);
						} else {
							methodLabelMap.clear();
						}
					} else {
						s = s.substring(1);
						if (!temp.isEmpty()) {
							if (s.equals("]")) {
								temp += s;
								node.instructions.add(getNode(temp, methodLabelMap));
								temp = "";
							} else {
								temp += s + "\n";
							}
						} else {
							if (s.endsWith("[")) {
								temp += s + "\n";
							} else {
								node.instructions.add(getNode(s, methodLabelMap));
							}
						}
					}
				}
			}

			return clazz;
		} catch (Throwable e) {
			System.err.println("Error at line: " + read.currentLine);
			try {
				Main.textArea.addLineHighlight(read.currentLine, Color.RED.darker());
			} catch (BadLocationException e1) {
				System.err.println("Can't show line!");
			}
			e.printStackTrace();
			return null;
		} finally {
			try {
				read.close();
			} catch (Exception e) {
			}
		}
	}

	public static AnnotationNode parseAnnotation(String s) {
		s = s.substring(1);
		String[] split = s.split(" ");
		AnnotationNode node = new AnnotationNode(split[0]);
		if (split.length > 1) {
			if (node.values == null) {
				node.values = new ArrayList<>();
			}
			s = consolidateStrings(split, 1);
			s = s.substring(1, s.length() - 1);

			split = s.split("\\], ");

			for (int i = 0; i < split.length; i++) {
				String[] split2 = split[i].split(" = \\[");
				node.values.add(split2[0]);
				String value = split2[1];
				if (value.endsWith("]"))
					value = value.substring(0, value.length() - 1);
				if (value.startsWith("{ ")) {
					String split3[] = value.substring(2, value.length() - 2).split(", ");
					List<Object> list = new ArrayList<>();
					for (String rofl : split3) {
						if (!rofl.startsWith("(")) {
							int index = rofl.indexOf(";");
							list.add(new String[] { rofl.substring(0, index) + ";",
									rofl.substring(index).substring(2) });
						} else { // anderer typ mit casten
							list.add(ClassUtil.getCastedValue(rofl.split(" ")[1], rofl.split("\\) ")[0].substring(1)));
						}
					}
				} else {
					if (!value.startsWith("(")) {
						int index = value.indexOf(";");
						node.values.add(
								new String[] { value.substring(0, index) + ";", value.substring(index).substring(2) });
					} else { // anderer typ mit casten
						node.values.add(
								ClassUtil.getCastedValue(value.split(" ")[1], value.split("\\) ")[0].substring(1)));
					}
				}
			}
		}
		return node;
	}

	public static String consolidateStrings(String[] args, int start) {
		if (args.length == 0) {
			return null;
		}
		String ret = args[start];
		if (args.length > start + 1) {
			for (int i = start + 1; i < args.length; i++) {
				ret = ret + " " + args[i];
			}
		}
		return ret;
	}

	public static String consolidateStrings(String[] args, int start, int end) {
		if (end == 0) {
			return null;
		}
		String ret = args[start];
		if (end > start + 1) {
			for (int i = start + 1; i < end; i++) {
				ret = ret + " " + args[i];
			}
		}
		return ret;
	}

	public static Object getValue(String s, String to) {
		if (s == null) {
			return null;
		}
		switch (to) {
		case "B":
			return Byte.parseByte(s);
		case "Z":
			return Boolean.parseBoolean(s);
		case "I":
			return Integer.parseInt(s);
		case "J":
			return Long.parseLong(s.substring(0, s.length() - 1));
		case "C":
			return (char) Integer.parseInt(s);
		case "D":
			return Double.parseDouble(s);
		case "F":
			return Float.parseFloat(s.substring(0, s.length() - 1));
		case "S":
			return Short.parseShort(s);
		case "Ljava/lang/String;":
			return StringEscapeUtils.unescapeJava(s.substring(1, s.length() - 1)).replace("\\n", "\n").replace("\\r",
					"\r");
		default: {
			return null;
		}
		}
	}

	public static AbstractInsnNode getNode(String s, HashMap<Label, Integer> labels) throws Exception {
		if (s.startsWith("// label ")) {
			int labelNr = Integer.parseInt(s.substring(9));
			for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
				if (entry.getValue() == labelNr) {
					return new LabelNode(entry.getKey());
				}
			}
			Label l = new Label();
			labels.put(l, labelNr);
			return new LabelNode(l);
		} else if (s.startsWith("// line ")) {
			s = s.substring(8);
			int labelNr = Integer.parseInt(s.split(" ")[1]);
			for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
				if (entry.getValue() == labelNr) {
					return new LineNumberNode(Integer.parseInt(s.split(" ")[0]), new LabelNode(entry.getKey()));
				}
			}
			Label l = new Label();
			labels.put(l, labelNr);
			return new LineNumberNode(Integer.parseInt(s.split(" ")[0]), new LabelNode(l));
		} else if (s.startsWith("// frame ")) {
			String str1 = s.substring(9);
			String str = str1.substring(2);
			Object[] local;
			Object[] stack;
			ArrayList<Object> arr = new ArrayList<>();
			if (str.startsWith("l:null")) {
				local = new Object[0];
			} else {
				String l = str.substring(3, str.indexOf("s:") - 2);
				for (String asd : l.split(", ")) {
					if (!asd.startsWith("(")) {
						arr.add(asd);
					} else { // anderer typ mit casten
						if (asd.startsWith("(label) ")) {
							int labelNr = Integer.parseInt(asd.split(" ")[1]);
							boolean found = false;
							for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
								if (entry.getValue() == labelNr) {
									arr.add(new LabelNode(entry.getKey()));
									found = true;
								}
							}
							if (!found) {
								Label lb = new Label();
								labels.put(lb, labelNr);
								arr.add(new LabelNode(lb));
							}
						} else {
							arr.add(ClassUtil.getCastedValue(asd.split(" ")[1], asd.split("\\) ")[0].substring(1)));
						}
					}
				}
				local = new Object[arr.size()];
				int c = 0;
				for (Object o : arr) {
					local[c] = o;
					c++;
				}
			}
			arr.clear();
			if (str.endsWith("s:null")) {
				stack = new Object[0];
			} else {
				String st = str.substring(str.indexOf("s:") + 3, str.length() - 1);
				for (String asd : st.split(", ")) {
					if (!asd.startsWith("(")) {
						arr.add(asd);
					} else { // anderer typ mit casten
						if (asd.startsWith("(label) ")) {
							int labelNr = Integer.parseInt(asd.split(" ")[1]);
							boolean found = false;
							for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
								if (entry.getValue() == labelNr) {
									arr.add(new LabelNode(entry.getKey()));
									found = true;
								}
							}
							if (!found) {
								Label l = new Label();
								labels.put(l, labelNr);
								arr.add(new LabelNode(l));
							}
						} else {
							arr.add(ClassUtil.getCastedValue(asd.split(" ")[1], asd.split("\\) ")[0].substring(1)));
						}
					}
				}
				stack = new Object[arr.size()];
				int c = 0;
				for (Object o : arr) {
					stack[c] = o;
					c++;
				}
			}
			return new FrameNode(Integer.parseInt(str1.split(" ")[0]), local != null ? local.length : 0, local,
					stack != null ? stack.length : 0, stack);
		} else {
			String[] split = s.split(" ");
			String command = split[0];
			switch (command) {
			case "nop":
				return new InsnNode(0);
			case "aconst_null":
				return new InsnNode(1);
			case "iconst_m1":
				return new InsnNode(2);
			case "iconst_0":
				return new InsnNode(3);
			case "iconst_1":
				return new InsnNode(4);
			case "iconst_2":
				return new InsnNode(5);
			case "iconst_3":
				return new InsnNode(6);
			case "iconst_4":
				return new InsnNode(7);
			case "iconst_5":
				return new InsnNode(8);
			case "lconst_0":
				return new InsnNode(9);
			case "lconst_1":
				return new InsnNode(10);
			case "fconst_0":
				return new InsnNode(11);
			case "fconst_1":
				return new InsnNode(12);
			case "fconst_2":
				return new InsnNode(13);
			case "dconst_0":
				return new InsnNode(14);
			case "dconst_1":
				return new InsnNode(15);
			case "bipush": {
				return new IntInsnNode(16, Integer.parseInt(split[1]));
			}
			case "sipush": {
				return new IntInsnNode(17, Integer.parseInt(split[1]));
			}
			case "ldc": {
				String str = consolidateStrings(split, 1);
				Object val;
				try {
					if (str.startsWith("Type: ")) {
						str = str.substring(8, str.length() - 2);
						String[] split2 = str.split("\n\t");
						Constructor<Type> constr = Type.class.getDeclaredConstructor(int.class, char[].class, int.class,
								int.class);
						constr.setAccessible(true);
						val = constr.newInstance(new Object[] { Integer.parseInt(split2[0].substring(7)),
								split2[3].substring(6, split2[3].length() - 1).toCharArray(),
								Integer.parseInt(split2[1].substring(5)), Integer.parseInt(split2[2].substring(5)) });
					} else if (str.startsWith("\"") && str.endsWith("\"")) {
						val = StringEscapeUtils.unescapeJava(str.substring(1, str.length() - 1)).replace("\\n", "\n")
								.replace("\\r", "\r");
					} else if (str.endsWith("l")) {
						val = Long.parseLong(str.substring(0, str.length() - 1));
					} else if (str.endsWith("f")) {
						val = Float.parseFloat(str.substring(0, str.length() - 1));
					} else if (str.contains(".")) {
						val = Double.parseDouble(str);
					} else {
						val = Integer.parseInt(str);
					}
				} catch (Exception e) {
					val = null;
					throw e;
				}
				return new LdcInsnNode(val);
			}
			case "iload": {
				return new VarInsnNode(21, Integer.parseInt(split[1]));
			}
			case "lload": {
				return new VarInsnNode(22, Integer.parseInt(split[1]));
			}
			case "fload": {
				return new VarInsnNode(23, Integer.parseInt(split[1]));
			}
			case "dload": {
				return new VarInsnNode(24, Integer.parseInt(split[1]));
			}
			case "aload": {
				return new VarInsnNode(25, Integer.parseInt(split[1]));
			}
			case "iaload": {
				return new InsnNode(46);
			}
			case "laload": {
				return new InsnNode(47);
			}
			case "faload": {
				return new InsnNode(48);
			}
			case "daload": {
				return new InsnNode(49);
			}
			case "aaload": {
				return new InsnNode(50);
			}
			case "baload": {
				return new InsnNode(51);
			}
			case "caload": {
				return new InsnNode(52);
			}
			case "saload": {
				return new InsnNode(53);
			}
			case "istore": {
				return new VarInsnNode(54, Integer.parseInt(split[1]));
			}
			case "lstore": {
				return new VarInsnNode(55, Integer.parseInt(split[1]));
			}
			case "fstore": {
				return new VarInsnNode(56, Integer.parseInt(split[1]));
			}
			case "dstore": {
				return new VarInsnNode(57, Integer.parseInt(split[1]));
			}
			case "astore": {
				return new VarInsnNode(58, Integer.parseInt(split[1]));
			}
			case "iastore": {
				return new InsnNode(79);
			}
			case "lastore": {
				return new InsnNode(80);
			}
			case "fastore": {
				return new InsnNode(81);
			}
			case "dastore": {
				return new InsnNode(82);
			}
			case "aastore": {
				return new InsnNode(83);
			}
			case "bastore": {
				return new InsnNode(84);
			}
			case "castore": {
				return new InsnNode(85);
			}
			case "sastore": {
				return new InsnNode(86);
			}
			case "pop": {
				return new InsnNode(87);
			}
			case "pop2": {
				return new InsnNode(88);
			}
			case "dup": {
				return new InsnNode(89);
			}
			case "dup_x1": {
				return new InsnNode(90);
			}
			case "dup_x2": {
				return new InsnNode(91);
			}
			case "dup2": {
				return new InsnNode(92);
			}
			case "dup2_x1": {
				return new InsnNode(93);
			}
			case "dup2_x2": {
				return new InsnNode(94);
			}
			case "swap": {
				return new InsnNode(95);
			}
			case "iadd": {
				return new InsnNode(96);
			}
			case "ladd": {
				return new InsnNode(97);
			}
			case "fadd": {
				return new InsnNode(98);
			}
			case "dadd": {
				return new InsnNode(99);
			}
			case "isub": {
				return new InsnNode(100);
			}
			case "lsub": {
				return new InsnNode(101);
			}
			case "fsub": {
				return new InsnNode(102);
			}
			case "dsub": {
				return new InsnNode(103);
			}
			case "imul": {
				return new InsnNode(104);
			}
			case "lmul": {
				return new InsnNode(105);
			}
			case "fmul": {
				return new InsnNode(106);
			}
			case "dmul": {
				return new InsnNode(107);
			}
			case "idiv": {
				return new InsnNode(108);
			}
			case "ldiv": {
				return new InsnNode(109);
			}
			case "fdiv": {
				return new InsnNode(110);
			}
			case "ddiv": {
				return new InsnNode(111);
			}
			case "irem": {
				return new InsnNode(112);
			}
			case "lrem": {
				return new InsnNode(113);
			}
			case "frem": {
				return new InsnNode(114);
			}
			case "drem": {
				return new InsnNode(115);
			}
			case "ineg": {
				return new InsnNode(116);
			}
			case "lneg": {
				return new InsnNode(117);
			}
			case "fneg": {
				return new InsnNode(118);
			}
			case "dneg": {
				return new InsnNode(119);
			}
			case "ishl": {
				return new InsnNode(120);
			}
			case "lshl": {
				return new InsnNode(121);
			}
			case "ishr": {
				return new InsnNode(122);
			}
			case "lshr": {
				return new InsnNode(123);
			}
			case "iushr": {
				return new InsnNode(124);
			}
			case "lushr": {
				return new InsnNode(125);
			}
			case "iand": {
				return new InsnNode(126);
			}
			case "land": {
				return new InsnNode(127);
			}
			case "ior": {
				return new InsnNode(128);
			}
			case "lor": {
				return new InsnNode(129);
			}
			case "ixor": {
				return new InsnNode(130);
			}
			case "lxor": {
				return new InsnNode(131);
			}
			case "iinc": {
				return new IincInsnNode(Integer.parseInt(split[1]), Integer.parseInt(split[3]));
			}
			case "i2l": {
				return new InsnNode(133);
			}
			case "i2f": {
				return new InsnNode(134);
			}
			case "i2d": {
				return new InsnNode(135);
			}
			case "l2i": {
				return new InsnNode(136);
			}
			case "l2f": {
				return new InsnNode(137);
			}
			case "l2d": {
				return new InsnNode(138);
			}
			case "f2i": {
				return new InsnNode(139);
			}
			case "f2l": {
				return new InsnNode(140);
			}
			case "f2d": {
				return new InsnNode(141);
			}
			case "d2i": {
				return new InsnNode(142);
			}
			case "d2l": {
				return new InsnNode(143);
			}
			case "d2f": {
				return new InsnNode(144);
			}
			case "i2b": {
				return new InsnNode(145);
			}
			case "i2c": {
				return new InsnNode(146);
			}
			case "i2s": {
				return new InsnNode(147);
			}
			case "lcmp": {
				return new InsnNode(148);
			}
			case "fcmpl": {
				return new InsnNode(149);
			}
			case "fcmpg": {
				return new InsnNode(150);
			}
			case "dcmpl": {
				return new InsnNode(151);
			}
			case "dcmpg": {
				return new InsnNode(152);
			}
			case "ifeq": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(153, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(153, new LabelNode(l));
			}
			case "ifne": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(154, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(154, new LabelNode(l));
			}
			case "iflt": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(155, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(155, new LabelNode(l));
			}
			case "ifge": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(156, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(156, new LabelNode(l));
			}
			case "ifgt": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(157, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(157, new LabelNode(l));
			}
			case "ifle": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(158, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(158, new LabelNode(l));
			}
			case "if_icmpeq": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(159, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(159, new LabelNode(l));
			}
			case "if_icmpne": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(160, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(160, new LabelNode(l));
			}
			case "if_icmplt": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(161, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(161, new LabelNode(l));
			}
			case "if_icmpge": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(162, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(162, new LabelNode(l));
			}
			case "if_icmpgt": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(163, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(163, new LabelNode(l));
			}
			case "if_icmple": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(164, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(164, new LabelNode(l));
			}
			case "if_acmpeq": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(165, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(165, new LabelNode(l));
			}
			case "if_acmpne": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(166, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(166, new LabelNode(l));
			}
			case "goto": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(167, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(167, new LabelNode(l));
			}
			case "tableswitch": {
				String str = s.substring(14, s.length() - 2).replace("\t", "");
				String split2[] = str.split("\n");
				HashMap<Integer, LabelNode> labelMap = new HashMap<>();

				for (int i = 4; i < split2.length - 1; i++) {
					labelMap.put(Integer.parseInt(split2[i]), null);
				}

				LabelNode dflt = null;
				int dfltLabelNr = Integer.parseInt(split2[2].substring(9));
				for (Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == dfltLabelNr) {
						dflt = new LabelNode(entry.getKey());
					}
					if (labelMap.containsKey(entry.getValue())) {
						labelMap.replace(entry.getValue(), new LabelNode(entry.getKey()));
					}
				}
				if (dflt == null) {
					Label l = new Label();
					labels.put(l, dfltLabelNr);
					dflt = new LabelNode(l);
				}

				LabelNode[] arr = new LabelNode[labelMap.size()];

				int counter = 0;
				for (Entry<Integer, LabelNode> entry : labelMap.entrySet()) {
					LabelNode ln;
					if (entry.getValue() == null) {
						Label l = new Label();
						labels.put(l, entry.getKey());
						ln = new LabelNode(l);
					} else {
						ln = entry.getValue();
					}
					arr[counter] = ln;
					counter++;
				}

				return new TableSwitchInsnNode(Integer.parseInt(split2[0].substring(5)),
						Integer.parseInt(split2[1].substring(5)), dflt, arr);
			}
			case "lookupswitch": {
				String str = s.substring(15, s.length() - 2).replace("\t", "");
				String split2[] = str.split("\n");
				ArrayList<Integer> keys = new ArrayList<>();
				HashMap<Integer, LabelNode> labelMap = new HashMap<>();

				int stage = 0;
				for (int i = 2; i < split2.length; i++) {
					String spl = split2[i];
					if (spl.equals("]")) {
						stage = 1;
					} else if (spl.equals("labels: [")) {
						stage = 2;
					} else if (stage == 0) {
						keys.add(Integer.parseInt(spl));
					} else if (stage == 2) {
						labelMap.put(Integer.parseInt(spl), null);
					}
				}

				LabelNode dflt = null;
				int labelNr = Integer.parseInt(split2[0].substring(9));
				for (Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						dflt = new LabelNode(entry.getKey());
					}
					if (labelMap.containsKey(entry.getValue())) {
						labelMap.replace(entry.getValue(), new LabelNode(entry.getKey()));
					}
				}
				if (dflt == null) {
					Label l = new Label();
					labels.put(l, labelNr);
					dflt = new LabelNode(l);
				}

				LabelNode[] arr = new LabelNode[labelMap.size()];

				int counter = 0;
				for (Entry<Integer, LabelNode> entry : labelMap.entrySet()) {
					LabelNode ln;
					if (entry.getValue() == null) {
						Label l = new Label();
						labels.put(l, entry.getKey());
						ln = new LabelNode(l);
					} else {
						ln = entry.getValue();
					}
					arr[counter] = ln;
					counter++;
				}

				int[] _keys = new int[keys.size()];
				counter = 0;

				for (int i : keys) {
					_keys[counter] = i;
					counter++;
				}

				return new LookupSwitchInsnNode(dflt, _keys, arr);
			}
			case "ireturn": {
				return new InsnNode(172);
			}
			case "lreturn": {
				return new InsnNode(173);
			}
			case "freturn": {
				return new InsnNode(174);
			}
			case "dreturn": {
				return new InsnNode(175);
			}
			case "areturn": {
				return new InsnNode(176);
			}
			case "return": {
				return new InsnNode(177);
			}
			case "getstatic": {
				String target = split[2];
				int index = target.lastIndexOf("/");
				return new FieldInsnNode(178, target.substring(0, index), target.substring(index + 1), split[1]);
			}
			case "putstatic": {
				String target = split[2];
				int index = target.lastIndexOf("/");
				return new FieldInsnNode(179, target.substring(0, index), target.substring(index + 1), split[1]);
			}
			case "getfield": {
				String target = split[2];
				int index = target.lastIndexOf("/");
				return new FieldInsnNode(180, target.substring(0, index), target.substring(index + 1), split[1]);
			}
			case "putfield": {
				String target = split[2];
				int index = target.lastIndexOf("/");
				return new FieldInsnNode(181, target.substring(0, index), target.substring(index + 1), split[1]);
			}
			case "invokevirtual": {
				String target = split[2];
				int index = target.lastIndexOf("/");
				return new MethodInsnNode(182, target.substring(0, index), target.substring(index + 1), split[1],
						split.length == 4);
			}
			case "invokespecial": {
				String target = split[2];
				int index = target.lastIndexOf("/");
				return new MethodInsnNode(183, target.substring(0, index), target.substring(index + 1), split[1],
						split.length == 4);
			}
			case "invokestatic": {
				String target = split[2];
				int index = target.lastIndexOf("/");
				return new MethodInsnNode(184, target.substring(0, index), target.substring(index + 1), split[1],
						split.length == 4);
			}
			case "invokeinterface": {
				String target = split[2];
				int index = target.lastIndexOf("/");
				return new MethodInsnNode(185, target.substring(0, index), target.substring(index + 1), split[1],
						split.length == 4);
			}
			case "invokedynamic": {
				String str = s.substring(16, s.length() - 2).replace("", "");
				String sp[] = str.split("\n\t");
				ArrayList<Object> args = new ArrayList<>();

				ArrayList<String> vals = new ArrayList<>();

				int stage = 0;
				for (int i = 10; i < sp.length - 1; i++) {
					String st = sp[i].substring(1);
					if (st.equals("]")) {
						if (stage == 1) {
							try {
								Constructor<Type> constr = Type.class.getDeclaredConstructor(int.class, char[].class,
										int.class, int.class);
								constr.setAccessible(true);
								args.add(constr.newInstance(new Object[] { Integer.parseInt(vals.get(0).substring(6)),
										vals.get(3).substring(6, vals.get(3).length() - 1).toCharArray(),
										Integer.parseInt(vals.get(1).substring(5)),
										Integer.parseInt(vals.get(2).substring(5)) }));
							} catch (Exception e) {
								throw e;
							}
						} else if (stage == 2) {
							args.add(new Handle(Integer.parseInt(vals.get(4).substring(5)), vals.get(1).substring(7),
									vals.get(0).substring(6), vals.get(2).substring(6),
									Boolean.parseBoolean(vals.get(3).substring(13))));
							vals.clear();
						}
						stage = 0;
					} else if (st.equals("Type: [")) {
						stage = 1;
					} else if (stage == 1) {
						st = st.substring(1);
						vals.add(st);
					} else if (st.equals("Handle: [")) {
						vals.clear();
						stage = 2;
					} else if (stage == 2) {
						st = st.substring(1);
						vals.add(st);
					}
				}

				Object[] _args = new Object[args.size()];

				stage = 0;
				for (Object o : args) {
					_args[stage] = o;
					stage++;
				}

				return new InvokeDynamicInsnNode(sp[0].substring(7), sp[1].substring(6),
						new Handle(Integer.parseInt(sp[7].substring(6)), sp[4].substring(8), sp[3].substring(7),
								sp[5].substring(7), Boolean.parseBoolean(sp[6].substring(14))),
						_args);
			}
			case "new": {
				return new TypeInsnNode(187, split[1]);
			}
			case "newarray": {
				return new IntInsnNode(188, Integer.parseInt(split[1]));
			}
			case "anewarray": {
				return new TypeInsnNode(189, split[1]);
			}
			case "arraylength": {
				return new InsnNode(190);
			}
			case "athrow": {
				return new InsnNode(191);
			}
			case "checkcast": {
				return new TypeInsnNode(192, split[1]);
			}
			case "instanceof": {
				return new TypeInsnNode(193, split[1]);
			}
			case "monitorenter": {
				return new InsnNode(194);
			}
			case "monitorexit": {
				return new InsnNode(195);
			}
			case "multianewarray": {
				return new MultiANewArrayInsnNode(split[1], Integer.parseInt(split[2]));
			}
			case "ifnull": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(198, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(198, new LabelNode(l));
			}
			case "ifnonnull": {
				int labelNr = Integer.parseInt(s.split(" ")[1]);
				for (Map.Entry<Label, Integer> entry : labels.entrySet()) {
					if (entry.getValue() == labelNr) {
						return new JumpInsnNode(199, new LabelNode(entry.getKey()));
					}
				}
				Label l = new Label();
				labels.put(l, labelNr);
				return new JumpInsnNode(199, new LabelNode(l));
			}
			default:
				throw new IllegalArgumentException("Illegal Instruction: " + s);
			}
		}
	}
}