package me.ByteEdit.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InnerClassNode;
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
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.ByteEdit.utils.ClassUtil;
import me.ByteEdit.utils.OpcodesReverse;
import me.ByteEdit.utils.UnicodeUtils;

public class Disassembler {

	public static String disassemble(ClassNode classNode) {
		return disassemble(classNode, null).getDisassembly();
	}

	public static class DisassembleTuple {

		private final String dis;
		private final int line;

		public DisassembleTuple(String dis) {
			this.dis = dis;
			this.line = -1;
		}

		public DisassembleTuple(String dis, int line) {
			this.dis = dis;
			this.line = line;
		}

		public String getDisassembly() {
			return dis;
		}

		public int getLine() {
			return line;
		}

	}

	public static DisassembleTuple disassemble(ClassNode classNode, Object nodeToFind) {
		if (classNode == null) {
			return new DisassembleTuple("ClassNode is null! This is not a valid java class file!");
		}
		try {
			int lineFound = -1;
			String s = "";
			s += "// #Annotations:\n";
			if (classNode.visibleAnnotations != null && !classNode.visibleAnnotations.isEmpty()) {
				for (AnnotationNode annotationNode : classNode.visibleAnnotations) {
					s += "@" + UnicodeUtils.escapeWithSpaces(annotationNode.desc);
					if (annotationNode.values != null && !annotationNode.values.isEmpty()) {
						s += " (";
						boolean valBefore = true;
						for (Object o : annotationNode.values) {
							if (valBefore) {
								s += o + " = [";
								valBefore = false;
								continue;
							} else {
								if (o instanceof String[]) {
									String[] arr = (String[]) o;
									boolean w8ing = false;
									for (String rofl : arr) {
										if (!w8ing) {
											s += UnicodeUtils.escapeWithSpaces(rofl) + "/";
											w8ing = true;
										} else {
											s += UnicodeUtils.escapeWithSpaces(rofl) + "]";
										}
									}
									s += ", ";
								} else if (o instanceof List) {
									List list = (List) o;
									s += "{ ";
									for (Object obj : list) {
										if (obj instanceof String[]) {
											String[] arr = (String[]) obj;
											boolean w8ing = false;
											for (String rofl : arr) {
												if (!w8ing) {
													s += UnicodeUtils.escapeWithSpaces(rofl) + "/";
													w8ing = true;
												} else {
													s += UnicodeUtils.escapeWithSpaces(rofl);
												}
											}
										} else {
											s += "\"" + UnicodeUtils.escape(String.valueOf(obj)) + "\"";
										}
										s += ", ";
									}
									s = s.substring(0, s.length() - 2);
									s += " }]";
								} else if (o instanceof String) {
									s += "\"" + UnicodeUtils.escapeWithSpaces((String) o) + "\"], ";
								} else {
									s += "(" + o.getClass().getName().replace(".", "/") + ") " + o + "], ";
								}
								valBefore = true;
							}
						}
						if (s.endsWith(", "))
							s = s.substring(0, s.length() - 2);
						s += ")";
					}
					s += "\n";
				}
			}
			s += "// #Class v:" + classNode.version + "\n";
			if (classNode.signature != null)
				s += "// #Signature: " + UnicodeUtils.escapeWithSpaces(classNode.signature) + "\n";
			if (classNode.outerMethod != null)
				s += "// #OuterMethod: " + (classNode.outerMethod == null ? "null"
						: (UnicodeUtils.escapeWithSpaces(classNode.outerMethod) + " "
								+ UnicodeUtils.escapeWithSpaces(classNode.outerMethodDesc)))
						+ "\n";
			if (classNode.outerClass != null)
				s += "// #OuterClass: " + UnicodeUtils.escapeWithSpaces(classNode.outerClass) + "\n";
			s += "// #InnerClasses:\n";
			if (classNode.innerClasses != null) {
				for (InnerClassNode icn : classNode.innerClasses) {
					s += "// " + UnicodeUtils.escapeWithSpaces(icn.name) + " "
							+ UnicodeUtils.escapeWithSpaces(icn.outerName) + " "
							+ UnicodeUtils.escapeWithSpaces(icn.innerName)
							+ (icn.access == 0 ? "" : " " + ClassUtil.getAccessFlagsFull(icn.access).trim()) + "\n";
				}
			}
			s += ClassUtil.getAccessFlagsClass(classNode.access) + UnicodeUtils.escapeWithSpaces(classNode.name) + " ";
			s += "extends " + UnicodeUtils.escapeWithSpaces(classNode.superName) + " ";
			if (classNode.interfaces != null && !classNode.interfaces.isEmpty()) {
				String interfaceStr = "";
				for (String interfc : classNode.interfaces) {
					if (interfaceStr.isEmpty()) {
						interfaceStr += UnicodeUtils.escapeWithSpaces(interfc);
					} else {
						interfaceStr += ", " + UnicodeUtils.escapeWithSpaces(interfc);
					}
				}
				if (!interfaceStr.isEmpty())
					s += "implements " + interfaceStr + " ";
			}
			s += "{\n// #SourceFile: "
					+ (classNode.sourceFile == null ? "null" : UnicodeUtils.escape(classNode.sourceFile))
					+ "\n\n// #Fields\n";
			for (FieldNode fn : classNode.fields) {
				if (fn.signature != null)
					s += "\t// #Signature: " + UnicodeUtils.escapeWithSpaces(fn.signature) + "\n";
				if (fn.visibleAnnotations != null && !fn.visibleAnnotations.isEmpty()) {
					for (AnnotationNode annotationNode : fn.visibleAnnotations) {
						s += "\t@" + UnicodeUtils.escapeWithSpaces(annotationNode.desc);
						if (annotationNode.values != null && !annotationNode.values.isEmpty()) {
							s += " (";
							boolean valBefore = true;
							for (Object o : annotationNode.values) {
								if (valBefore) {
									s += o + " = [";
									valBefore = false;
									continue;
								} else {
									if (o instanceof String[]) {
										String[] arr = (String[]) o;
										boolean w8ing = false;
										for (String rofl : arr) {
											if (!w8ing) {
												s += UnicodeUtils.escapeWithSpaces(rofl) + "/";
												w8ing = true;
											} else {
												s += UnicodeUtils.escapeWithSpaces(rofl) + "]";
											}
										}
										s += ", ";
									} else if (o instanceof List) {
										List list = (List) o;
										s += "{ ";
										for (Object obj : list) {
											if (obj instanceof String[]) {
												String[] arr = (String[]) obj;
												boolean w8ing = false;
												for (String rofl : arr) {
													if (!w8ing) {
														s += UnicodeUtils.escapeWithSpaces(rofl) + "/";
														w8ing = true;
													} else {
														s += UnicodeUtils.escapeWithSpaces(rofl);
													}
												}
											} else {
												s += "\"" + UnicodeUtils.escape(String.valueOf(obj)) + "\"";
											}
											s += ", ";
										}
										s = s.substring(0, s.length() - 2);
										s += " }]";
									} else {
										s += "(" + o.getClass().getName().replace(".", "/") + ") " + o + "], ";
									}
									valBefore = true;
								}
							}
							if (s.endsWith(", "))
								s = s.substring(0, s.length() - 2);
							s += ")";
						}
						s += "\n";
					}
				}
				if (fn.equals(nodeToFind)) {
					lineFound = s.split("\\n").length;
				}
				s += "\t" + ClassUtil.getAccessFlagsFull(fn.access).replace("varargs", "transient")
						+ UnicodeUtils.escapeWithSpaces(fn.desc) + " " + UnicodeUtils.escapeWithSpaces(fn.name);
				if (fn.value != null) {
					s += " = " + ClassUtil.getDecompiledValue(fn.value, fn.desc, true);
				}
				s += "\n";
			}
			s += "\n// #Methods\n";
			for (MethodNode mn : classNode.methods) {
				try {
					s += "\t// #Max: l:" + mn.maxLocals + " s:" + mn.maxStack + "\n";
					if (mn.signature != null)
						s += "\t// #Signature: " + UnicodeUtils.escapeWithSpaces(mn.signature) + "\n";
					String[] dis = disassembleMethod(classNode.name, mn);
					s += dis[2];
					s += dis[1];
					if (mn.visibleAnnotations != null && !mn.visibleAnnotations.isEmpty()) {
						for (AnnotationNode annotationNode : mn.visibleAnnotations) {
							s += "\t@" + annotationNode.desc;
							if (annotationNode.values != null && !annotationNode.values.isEmpty()) {
								s += " (";
								boolean valBefore = true;
								for (Object o : annotationNode.values) {
									if (valBefore) {
										s += o + " = [";
										valBefore = false;
										continue;
									} else {
										if (o instanceof String[]) {
											String[] arr = (String[]) o;
											boolean w8ing = false;
											for (String rofl : arr) {
												if (!w8ing) {
													s += UnicodeUtils.escapeWithSpaces(rofl) + "/";
													w8ing = true;
												} else {
													s += UnicodeUtils.escapeWithSpaces(rofl) + "]";
												}
											}
											s += ", ";
										} else if (o instanceof List) {
											List list = (List) o;
											s += "{ ";
											for (Object obj : list) {
												if (obj instanceof String[]) {
													String[] arr = (String[]) obj;
													boolean w8ing = false;
													for (String rofl : arr) {
														if (!w8ing) {
															s += UnicodeUtils.escapeWithSpaces(rofl) + "/";
															w8ing = true;
														} else {
															s += UnicodeUtils.escapeWithSpaces(rofl);
														}
													}
												} else {
													s += "\"" + UnicodeUtils.escape(String.valueOf(obj)) + "\"";
												}
												s += ", ";
											}
											s = s.substring(0, s.length() - 2);
											s += " }]";
										} else {
											s += "(" + o.getClass().getName().replace(".", "/") + ") " + o + "], ";
										}
										valBefore = true;
									}
								}
								if (s.endsWith(", "))
									s = s.substring(0, s.length() - 2);
								s += ")";
							}
							s += "\n";
						}
					}
					if (mn.equals(nodeToFind)) {
						lineFound = s.split("\\n").length;
					}
					s += "\t" + ClassUtil.getAccessFlagsFull(mn.access) + UnicodeUtils.escapeWithSpaces(mn.name) + " "
							+ UnicodeUtils.escapeWithSpaces(mn.desc) + " ";
					if (mn.exceptions != null && !mn.exceptions.isEmpty()) {
						String exceptionStr = "";
						for (String exc : mn.exceptions) {
							if (exceptionStr.isEmpty()) {
								exceptionStr += UnicodeUtils.escapeWithSpaces(exc);
							} else {
								exceptionStr += ", " + UnicodeUtils.escapeWithSpaces(exc);
							}
						}
						if (!exceptionStr.isEmpty())
							s += "throws " + exceptionStr + " ";
					}
					s += "{\n";
					s += dis[0];
					s += "\t}\n\n";
				} catch (Exception e) {
					s += "\t\t// Method couldn't be disassembled:\n\t\t// "
							+ e.getClass().getName() + ": " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace())
									.replace(", ", "\n\t\t// \tat ").replace("[", "\t\t// \tat ").replace("]", "")
							+ "\n\t}\n\n";
				}
			}
			s += "}\n";
			return new DisassembleTuple(s, lineFound);
		} catch (Throwable e) {
			return new DisassembleTuple("Class couldn't be decompiled:\n" + e.getClass().getName() + ": "
					+ e.getMessage() + "\n"
					+ Arrays.toString(e.getStackTrace()).replace(", ", "\n\tat ").replace("[", "\tat ").replace("]", "")
					+ "\n");
		}
	}

	private static String[] disassembleMethod(String className, MethodNode mn) {
		HashMap<Label, Integer> labels = new HashMap<Label, Integer>();
		String s = "";
		String localVarTable = "";
		String tryCatchTable = "";
		for (AbstractInsnNode n : mn.instructions.toArray()) {
			if (n instanceof LabelNode) {
				labels.put(((LabelNode) n).getLabel(), labels.size() + 1);
			}
		}
		if (mn.localVariables != null && !mn.localVariables.isEmpty()) {
			localVarTable = "\t// #LocalVars:\n";
			for (LocalVariableNode lvn : mn.localVariables) {
				localVarTable += "\t// " + UnicodeUtils.escapeWithSpaces(lvn.name) + ": "
						+ UnicodeUtils.escapeWithSpaces(lvn.desc) + " i:" + lvn.index + " s:"
						+ labels.get(lvn.start.getLabel()) + " e:" + labels.get(lvn.end.getLabel()) + " sig:"
						+ UnicodeUtils.escapeWithSpaces(lvn.signature) + "\n";
			}
		}
		if (mn.tryCatchBlocks != null && !mn.tryCatchBlocks.isEmpty()) {
			tryCatchTable = "\t// #TryCatch:\n";
			for (TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
				tryCatchTable += "\t// " + UnicodeUtils.escapeWithSpaces(tcbn.type) + " s:"
						+ labels.get(tcbn.start.getLabel()) + " e:" + labels.get(tcbn.end.getLabel()) + " h:"
						+ labels.get(tcbn.handler.getLabel()) + "\n";
			}
		}
		for (AbstractInsnNode n : mn.instructions.toArray()) {
			s += disassembleInstruction(n, labels);
		}
		return new String[] { s, localVarTable, tryCatchTable };
	}

	public static String disassembleInstruction(AbstractInsnNode n, HashMap<Label, Integer> labels) {
		if (FieldInsnNode.class.isInstance(n)) {
			FieldInsnNode node = (FieldInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + " " + UnicodeUtils.escapeWithSpaces(node.owner) + "/"
					+ UnicodeUtils.escapeWithSpaces(node.name).replace("/", "\\u002F") + "\n";
		} else if (LabelNode.class.isInstance(n)) {
			LabelNode node = (LabelNode) n;
			return "\t\t// label " + labels.get(node.getLabel()) + "\n";
		} else if (FrameNode.class.isInstance(n)) {
			FrameNode node = (FrameNode) n;
			String type = "";
			switch (node.type) {
			case Opcodes.F_NEW:
				type = "NEW";
				break;
			case Opcodes.F_FULL:
				type = "FULL";
				break;
			case Opcodes.F_APPEND:
				type = "APPEND";
				break;
			case Opcodes.F_CHOP:
				type = "CHOP";
				break;
			case Opcodes.F_SAME:
				type = "SAME";
				break;
			case Opcodes.F_SAME1:
				type = "SAME1";
				break;
			}
			String s = "\t\t// frame " + type + " l:";
			ArrayList<String> arr = new ArrayList<>();
			if (node.local != null) {
				for (Object o : node.local) {
					if (o == null) {
						arr.add(null);
					} else {
						if (o instanceof String) {
							arr.add("\"" + UnicodeUtils.escapeWithSpaces((String) o) + "\"");
						} else if (o instanceof LabelNode) {
							arr.add("(label) " + labels.get(((LabelNode) o).getLabel()));
						} else if (o instanceof Integer) {
							String frameType = ClassUtil.getFrameTypeByID(((Integer) o).intValue());
							if (frameType != null)
								arr.add(frameType);
							else
								arr.add("(" + o.getClass().getName().replace(".", "/") + ") " + o);
						} else {
							arr.add("(" + o.getClass().getName().replace(".", "/") + ") " + o);
						}
					}
				}
				s += arr.toString();
			} else {
				s += "null";
			}
			arr.clear();
			s += " s:";
			if (node.stack != null) {
				for (Object o : node.stack) {
					if (o == null) {
						arr.add(null);
					} else {
						if (o instanceof String) {
							arr.add("\"" + UnicodeUtils.escapeWithSpaces((String) o) + "\"");
						} else if (o instanceof LabelNode) {
							arr.add("(label) " + labels.get(((LabelNode) o).getLabel()));
						} else if (o instanceof Integer) {
							String frameType = ClassUtil.getFrameTypeByID(((Integer) o).intValue());
							if (frameType != null)
								arr.add(frameType);
							else
								arr.add("(" + o.getClass().getName().replace(".", "/") + ") " + o);
						} else {
							arr.add("(" + o.getClass().getName().replace(".", "/") + ") " + o);
						}
					}
				}
				s += arr.toString();
			} else {
				s += "null";
			}
			s += "\n";
			return s;
		} else if (LineNumberNode.class.isInstance(n)) {
			LineNumberNode node = (LineNumberNode) n;
			return "\t\t// line " + node.line + " " + labels.get(node.start.getLabel()) + "\n";
		} else if (InvokeDynamicInsnNode.class.isInstance(n)) {
			InvokeDynamicInsnNode node = (InvokeDynamicInsnNode) n;
			String s = "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " [\n\t\t\tname: "
					+ UnicodeUtils.escapeWithSpaces(node.name).replace("/", "\\u002F") + "\n\t\t\tdesc: "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + "\n\t\t\tHandle: [\n\t\t\t\tname: "
					+ UnicodeUtils.escapeWithSpaces(node.bsm.getName()) + "\n\t\t\t\towner: "
					+ UnicodeUtils.escapeWithSpaces(node.bsm.getOwner()) + "\n\t\t\t\tdesc: "
					+ UnicodeUtils.escapeWithSpaces(node.bsm.getDesc()) + "\n\t\t\t\tisInterface: "
					+ node.bsm.isInterface() + "\n\t\t\t\ttag: " + OpcodesReverse.reverseHandleOpcode(node.bsm.getTag())
					+ "\n\t\t\t]\n\t\t\targs: [\n";
			for (Object l : node.bsmArgs) {
				if (l.getClass().getName().equals("org.objectweb.asm.Type")) {
					Type type = (Type) l;
					int valueBegin = type.valueBegin;
					int valueEnd = type.valueEnd;
					String buf = type.valueBuffer;
					// Reflection
					s += "\t\t\t\tType: [\n\t\t\t\t\ttype: " + ClassUtil.getClassNameFromType(type)
							+ "\n\t\t\t\t\tstart: " + valueBegin + "\n\t\t\t\t\tend: " + valueEnd + "\n\t\t\t\t\tbuf: "
							+ ClassUtil.getDecompiledValue(buf, "");
					s += "\n\t\t\t\t]\n";
				} else if (l.getClass().getName().equals("org.objectweb.asm.Handle")) {
					Handle h = (Handle) l;
					s += "\t\t\t\tHandle: [\n\t\t\t\t\tname: "
							+ UnicodeUtils.escapeWithSpaces(h.getName()).replace("/", "\\u002F") + "\n\t\t\t\t\towner: "
							+ UnicodeUtils.escapeWithSpaces(h.getOwner()) + "\n\t\t\t\t\tdesc: "
							+ UnicodeUtils.escapeWithSpaces(h.getDesc()) + "\n\t\t\t\t\tisInterface: " + h.isInterface()
							+ "\n\t\t\t\t\ttag: " + OpcodesReverse.reverseHandleOpcode(h.getTag());
					s += "\n\t\t\t\t]\n";
				} else {
					s += "\t\t\t\t" + ClassUtil.getDecompiledValue(l, "") + "\n";
				}
			}
			s += "\t\t\t]\n\t\t]\n";
			return s;
		} else if (MethodInsnNode.class.isInstance(n)) {
			MethodInsnNode node = (MethodInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + " " + UnicodeUtils.escapeWithSpaces(node.owner) + "/"
					+ UnicodeUtils.escapeWithSpaces(node.name).replace("/", "\\u002F") + "\n";
		} else if (TypeInsnNode.class.isInstance(n)) {
			TypeInsnNode node = (TypeInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + "\n";
		} else if (MultiANewArrayInsnNode.class.isInstance(n)) {
			MultiANewArrayInsnNode node = (MultiANewArrayInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + " " + node.dims + "\n";
		} else if (LdcInsnNode.class.isInstance(n)) {
			LdcInsnNode node = (LdcInsnNode) n;
			switch (node.cst.getClass().getSimpleName()) {
			case "String":
			case "Double":
			case "Integer":
			case "Float":
			case "Long": {
				return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
						+ ClassUtil.getDecompiledValue(node.cst, "") + "\n";
			}
			case "Type": {
				Type type = (Type) node.cst;
				int valueBegin = type.valueBegin;
				int valueEnd = type.valueEnd;
				String buf = type.valueBuffer;
				// Reflection
				String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " Type: [\n\t\t\ttype: "
						+ ClassUtil.getClassNameFromType(type) + "\n\t\t\tstart: " + valueBegin + "\n\t\t\tend: "
						+ valueEnd + "\n\t\t\tbuf: " + ClassUtil.getDecompiledValue(buf, "");
				s += "\n\t\t]\n";
				return s;
			}
			default: {
				return "\t\tUNRESOLVED TYPE! " + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
						+ node.cst.getClass().getName().replace(".", "/") + " " + node.cst + "\n";
			}
			}
		} else if (VarInsnNode.class.isInstance(n)) {
			VarInsnNode node = (VarInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.var + "\n";
		} else if (InsnNode.class.isInstance(n)) {
			InsnNode node = (InsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + "\n";
		} else if (IntInsnNode.class.isInstance(n)) {
			IntInsnNode node = (IntInsnNode) n;
			if (n.getOpcode() == Opcodes.NEWARRAY) {
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " "
						+ ClassUtil.getArrayTypeByID(node.operand) + "\n";
			} else {
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.operand + "\n";
			}
		} else if (JumpInsnNode.class.isInstance(n)) {
			JumpInsnNode node = (JumpInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + labels.get(node.label.getLabel())
					+ "\n";
		} else if (TableSwitchInsnNode.class.isInstance(n)) {
			TableSwitchInsnNode node = (TableSwitchInsnNode) n;
			String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " [\n\t\t\tmin: " + node.min
					+ "\n\t\t\tmax: " + node.max + "\n\t\t\tdefault: " + labels.get(node.dflt.getLabel())
					+ "\n\t\t\tlabels: [\n";
			for (LabelNode l : node.labels) {
				s += "\t\t\t\t" + labels.get(l.getLabel()) + "\n";
			}
			s += "\t\t\t]\n\t\t]\n";
			return s;
		} else if (LookupSwitchInsnNode.class.isInstance(n)) {
			LookupSwitchInsnNode node = (LookupSwitchInsnNode) n;
			String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " [\n\t\t\tdefault: "
					+ labels.get(node.dflt.getLabel()) + "\n\t\t\tkeys: [\n";
			for (Integer l : node.keys) {
				s += "\t\t\t\t" + l + "\n";
			}
			s += "\t\t\t]" + "\n\t\t\tlabels: [\n";
			for (LabelNode l : node.labels) {
				s += "\t\t\t\t" + labels.get(l.getLabel()) + "\n";
			}
			s += "\t\t\t]\n\t\t]\n";
			return s;
		} else if (IincInsnNode.class.isInstance(n)) {
			IincInsnNode node = (IincInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.var + " " + node.incr + "\n";
		} else {
			return "\t\tNOT HANDLED! " + OpcodesReverse.reverseOpcode(n.getOpcode()) + " "
					+ n.getClass().getSimpleName() + "\n";
		}
	}

}
