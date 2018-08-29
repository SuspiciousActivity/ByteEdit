package me.ByteEdit.edit;

import java.lang.reflect.Field;
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
	
	public static DisassembleTuple disassemble(ClassNode classNode, MethodNode methodToFind) {
		if (classNode == null) {
			return new DisassembleTuple("ClassNode is null! This is not a valid java class file!");
		}
		try {
			int lineFound = -1;
			String s = "";
			s += "// #Annotations\n";
			if (classNode.visibleAnnotations != null && !classNode.visibleAnnotations.isEmpty()) {
				for (AnnotationNode annotationNode : classNode.visibleAnnotations) {
					s += "@" + annotationNode.desc;
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
											s += rofl + "/";
											w8ing = true;
										} else {
											s += rofl + "]";
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
													s += rofl + "/";
													w8ing = true;
												} else {
													s += rofl;
												}
											}
										} else {
											s += "\"" + obj + "\"";
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
			s += "// #Class v:" + classNode.version + "\n";
			s += "// #Signature: " + classNode.signature + "\n";
			s += "// #OuterMethod: " + (classNode.outerMethod == null ? "null" : (classNode.outerMethod + " " + classNode.outerMethodDesc))
					+ "\n";
			s += "// #OuterClass: " + classNode.outerClass + "\n";
			s += "// #InnerClasses:\n";
			if (classNode.innerClasses != null) {
				for (InnerClassNode icn : classNode.innerClasses) {
					s += "// " + icn.name + " " + icn.outerName + " " + icn.innerName + " 0x" + Integer.toHexString(icn.access) + "\n";
				}
			}
			s += ClassUtil.getAccessFlagsClass(classNode.access) + classNode.name + " ";
			s += "extends " + classNode.superName + " ";
			if (classNode.interfaces != null && !classNode.interfaces.isEmpty()) {
				String interfaceStr = "";
				for (String interfc : classNode.interfaces) {
					if (interfaceStr.isEmpty()) {
						interfaceStr += interfc;
					} else {
						interfaceStr += ", " + interfc;
					}
				}
				if (!interfaceStr.isEmpty())
					s += "implements " + interfaceStr + " ";
			}
			s += "{\n// #SourceFile: " + (classNode.sourceFile == null ? "null" : classNode.sourceFile) + "\n\n// #Fields\n";
			for (FieldNode fn : classNode.fields) {
				if (fn.signature != null)
					s += "\t// #Signature: " + fn.signature + "\n";
				if (fn.visibleAnnotations != null && !fn.visibleAnnotations.isEmpty()) {
					for (AnnotationNode annotationNode : fn.visibleAnnotations) {
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
												s += rofl + "/";
												w8ing = true;
											} else {
												s += rofl + "]";
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
														s += rofl + "/";
														w8ing = true;
													} else {
														s += rofl;
													}
												}
											} else {
												s += "\"" + obj + "\"";
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
				s += "\t" + ClassUtil.getAccessFlagsFull(fn.access) + fn.desc + " " + fn.name;
				if (fn.value != null) {
					s += " = " + ClassUtil.getDecompiledValue(fn.value, fn.desc);
				}
				s += "\n";
			}
			s += "\n// #Methods\n";
			for (MethodNode mn : classNode.methods) {
				try {
					s += "\t// #Max: l:" + mn.maxLocals + " s:" + mn.maxStack + "\n";
					if (mn.signature != null)
						s += "\t// #Signature: " + mn.signature + "\n";
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
													s += rofl + "/";
													w8ing = true;
												} else {
													s += rofl + "]";
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
															s += rofl + "/";
															w8ing = true;
														} else {
															s += rofl;
														}
													}
												} else {
													s += "\"" + obj + "\"";
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
					if (mn.equals(methodToFind)) {
						lineFound = s.split("\\n").length;
					}
					s += "\t" + ClassUtil.getAccessFlagsFull(mn.access) + mn.name + " " + mn.desc + " ";
					if (mn.exceptions != null && !mn.exceptions.isEmpty()) {
						String exceptionStr = "";
						for (String exc : mn.exceptions) {
							if (exceptionStr.isEmpty()) {
								exceptionStr += exc;
							} else {
								exceptionStr += ", " + exc;
							}
						}
						if (!exceptionStr.isEmpty())
							s += "throws " + exceptionStr + " ";
					}
					s += "{\n";
					s += dis[0];
					s += "\t}\n\n";
				} catch (Exception e) {
					s += "\t\t// Method couldn't be disassembled:\n\t\t// " + e.getClass().getName() + ": " + e.getMessage() + "\n" + Arrays
							.toString(e.getStackTrace()).replace(", ", "\n\t\t// \tat ").replace("[", "\t\t// \tat ").replace("]", "")
							+ "\n\t}\n\n";
				}
			}
			s += "}\n";
			return new DisassembleTuple(s, lineFound);
		} catch (Throwable e) {
			return new DisassembleTuple("Class couldn't be decompiled:\n" + e.getClass().getName() + ": " + e.getMessage() + "\n"
					+ Arrays.toString(e.getStackTrace()).replace(", ", "\n\tat ").replace("[", "\tat ").replace("]", "") + "\n");
		}
	}
	
	private static String[] disassembleMethod(String className, MethodNode mn) {
		HashMap<Label, Integer> labels = new HashMap<Label, Integer>();
		String s = "";
		String localVarTable = "\t// #LocalVars:\n";
		String tryCatchTable = "\t// #TryCatch:\n";
		for (AbstractInsnNode n : mn.instructions.toArray()) {
			if (n instanceof LabelNode) {
				labels.put(((LabelNode) n).getLabel(), labels.size() + 1);
			}
		}
		if (mn.localVariables != null) {
			for (LocalVariableNode lvn : mn.localVariables) {
				localVarTable += "\t// " + lvn.name + ": " + lvn.desc + " i:" + lvn.index + " s:" + labels.get(lvn.start.getLabel()) + " e:"
						+ labels.get(lvn.end.getLabel()) + " sig:" + lvn.signature + "\n";
			}
		}
		if (mn.tryCatchBlocks != null) {
			for (TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
				tryCatchTable += "\t// " + tcbn.type + " s:" + labels.get(tcbn.start.getLabel()) + " e:" + labels.get(tcbn.end.getLabel())
						+ " h:" + labels.get(tcbn.handler.getLabel()) + "\n";
			}
		}
		for (AbstractInsnNode n : mn.instructions.toArray()) {
			s += disassembleInstruction(n, labels);
		}
		return new String[] { s, localVarTable, tryCatchTable };
	}
	
	public static String disassembleInstruction(AbstractInsnNode n, HashMap<Label, Integer> labels) {
		switch (n.getClass().getSimpleName()) {
			case "FieldInsnNode": {
				FieldInsnNode node = (FieldInsnNode) n;
				return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " " + node.desc + " " + node.owner + "/" + node.name
						+ "\n";
			}
			case "LabelNode": {
				LabelNode node = (LabelNode) n;
				return "\t\t// label " + labels.get(node.getLabel()) + "\n";
			}
			case "FrameNode": {
				FrameNode node = (FrameNode) n;
				String s = "\t\t// frame " + node.type + " l:";
				ArrayList<String> arr = new ArrayList<>();
				if (node.local != null) {
					for (Object o : node.local) {
						if (o == null) {
							arr.add(null);
						} else {
							if (o instanceof String) {
								arr.add((String) o);
							} else if (o instanceof LabelNode) {
								arr.add("(label) " + labels.get(((LabelNode) o).getLabel()));
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
								arr.add((String) o);
							} else if (o instanceof LabelNode) {
								arr.add("(label) " + labels.get(((LabelNode) o).getLabel()));
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
			}
			case "LineNumberNode": {
				LineNumberNode node = (LineNumberNode) n;
				return "\t\t// line " + node.line + " " + labels.get(node.start.getLabel()) + "\n";
			}
			case "InvokeDynamicInsnNode": {
				InvokeDynamicInsnNode node = (InvokeDynamicInsnNode) n;
				String s = "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " [\n\t\t\tname: " + node.name + "\n\t\t\tdesc: "
						+ node.desc + "\n\t\t\tHandle: [\n\t\t\t\tname: " + node.bsm.getName() + "\n\t\t\t\towner: " + node.bsm.getOwner()
						+ "\n\t\t\t\tdesc: " + node.bsm.getDesc() + "\n\t\t\t\tisInterface: " + node.bsm.isInterface() + "\n\t\t\t\ttag: "
						+ node.bsm.getTag() + "\n\t\t\t]\n\t\t\targs: [\n";
				for (Object l : node.bsmArgs) {
					if (l.getClass().getName().equals("org.objectweb.asm.Type")) {
						Type type = (Type) l;
						int off = 0;
						int len = 0;
						char[] buf = null;
						try {
							Field f_off = Type.class.getDeclaredField("off");
							f_off.setAccessible(true);
							off = f_off.getInt(type);
							Field f_len = Type.class.getDeclaredField("len");
							f_len.setAccessible(true);
							len = f_len.getInt(type);
							Field f_buf = Type.class.getDeclaredField("buf");
							f_buf.setAccessible(true);
							buf = (char[]) f_buf.get(type);
						} catch (Exception e) {}
						s += "\t\t\t\tType: [\n\t\t\t\t\ttype: " + ClassUtil.getClassNameFromType(type) + "\n\t\t\t\t\toff: " + off
								+ "\n\t\t\t\t\tlen: " + len + "\n\t\t\t\t\tbuf: " + ClassUtil.getDecompiledValue(new String(buf), "");
						s += "\n\t\t\t\t]\n";
					} else if (l.getClass().getName().equals("org.objectweb.asm.Handle")) {
						Handle h = (Handle) l;
						s += "\t\t\t\tHandle: [\n\t\t\t\t\tname: " + h.getName() + "\n\t\t\t\t\towner: " + h.getOwner()
								+ "\n\t\t\t\t\tdesc: " + h.getDesc() + "\n\t\t\t\t\tisInterface: " + h.isInterface() + "\n\t\t\t\t\ttag: "
								+ h.getTag();
						s += "\n\t\t\t\t]\n";
					} else {
						s += "\t\t\t\t" + ClassUtil.getDecompiledValue(l, "") + "\n";
					}
				}
				s += "\t\t\t]\n\t\t]\n";
				return s;
			}
			case "MethodInsnNode": {
				MethodInsnNode node = (MethodInsnNode) n;
				return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " " + node.desc + " " + node.owner + "/" + node.name
						+ "\n";
			}
			case "TypeInsnNode": {
				TypeInsnNode node = (TypeInsnNode) n;
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.desc + "\n";
			}
			case "MultiANewArrayInsnNode": {
				MultiANewArrayInsnNode node = (MultiANewArrayInsnNode) n;
				return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " " + node.desc + " " + node.dims + "\n";
			}
			case "LdcInsnNode": {
				LdcInsnNode node = (LdcInsnNode) n;
				switch (node.cst.getClass().getSimpleName()) {
					case "String":
					case "Double":
					case "Integer":
					case "Float":
					case "Long": {
						return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " " + ClassUtil.getDecompiledValue(node.cst, "")
								+ "\n";
					}
					case "Type": {
						Type type = (Type) node.cst;
						int off = 0;
						int len = 0;
						char[] buf = null;
						try {
							Field f_off = Type.class.getDeclaredField("off");
							f_off.setAccessible(true);
							off = f_off.getInt(type);
							Field f_len = Type.class.getDeclaredField("len");
							f_len.setAccessible(true);
							len = f_len.getInt(type);
							Field f_buf = Type.class.getDeclaredField("buf");
							f_buf.setAccessible(true);
							buf = (char[]) f_buf.get(type);
						} catch (Exception e) {}
						String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " Type: [\n\t\t\ttype: "
								+ ClassUtil.getClassNameFromType(type) + "\n\t\t\toff: " + off + "\n\t\t\tlen: " + len + "\n\t\t\tbuf: "
								+ ClassUtil.getDecompiledValue(new String(buf), "");
						s += "\n\t\t]\n";
						return s;
					}
					default: {
						return "\t\tUNRESOLVED TYPE! " + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
								+ node.cst.getClass().getName().replace(".", "/") + " " + node.cst + "\n";
					}
				}
			}
			case "VarInsnNode": {
				VarInsnNode node = (VarInsnNode) n;
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.var + "\n";
			}
			case "InsnNode": {
				InsnNode node = (InsnNode) n;
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + "\n";
			}
			case "IntInsnNode": {
				IntInsnNode node = (IntInsnNode) n;
				if (n.getOpcode() == Opcodes.NEWARRAY) {
					return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + ClassUtil.getArrayTypeByID(node.operand) + "\n";
				} else {
					return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.operand + "\n";
				}
			}
			case "JumpInsnNode": {
				JumpInsnNode node = (JumpInsnNode) n;
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + labels.get(node.label.getLabel()) + "\n";
			}
			case "TableSwitchInsnNode": {
				TableSwitchInsnNode node = (TableSwitchInsnNode) n;
				String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " [\n\t\t\tmin: " + node.min + "\n\t\t\tmax: " + node.max
						+ "\n\t\t\tdefault: " + labels.get(node.dflt.getLabel()) + "\n\t\t\tlabels: [\n";
				for (LabelNode l : node.labels) {
					s += "\t\t\t\t" + labels.get(l.getLabel()) + "\n";
				}
				s += "\t\t\t]\n\t\t]\n";
				return s;
			}
			case "LookupSwitchInsnNode": {
				LookupSwitchInsnNode node = (LookupSwitchInsnNode) n;
				String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " [\n\t\t\tdefault: " + labels.get(node.dflt.getLabel())
						+ "\n\t\t\tkeys: [\n";
				for (Integer l : node.keys) {
					s += "\t\t\t\t" + l + "\n";
				}
				s += "\t\t\t]" + "\n\t\t\tlabels: [\n";
				for (LabelNode l : node.labels) {
					s += "\t\t\t\t" + labels.get(l.getLabel()) + "\n";
				}
				s += "\t\t\t]\n\t\t]\n";
				return s;
			}
			case "IincInsnNode": {
				IincInsnNode node = (IincInsnNode) n;
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.var + " " + node.incr + "\n";
			}
			default:
				return "\t\tNOT HANDLED! " + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + n.getClass().getSimpleName() + "\n";
		}
	}
	
}
