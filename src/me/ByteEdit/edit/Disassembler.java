package me.ByteEdit.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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

	private static final ExecutorService exec = Executors
			.newFixedThreadPool((Runtime.getRuntime().availableProcessors() + 1) / 2);

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
			AtomicInteger lineFound = new AtomicInteger(-1);
			StringContext ctx = new StringContext(16);
			ctx.next("// #Annotations:\n");
			ctx.next(doAnnotations(classNode.visibleAnnotations));
			ctx.next("// #Class v:" + classNode.version + "\n");
			if (classNode.signature != null)
				ctx.next("// #Signature: " + UnicodeUtils.escapeWithSpaces(classNode.signature) + "\n");
			if (classNode.outerMethod != null)
				ctx.next("// #OuterMethod: " + (classNode.outerMethod == null ? "null"
						: (UnicodeUtils.escapeWithSpaces(classNode.outerMethod) + " "
								+ UnicodeUtils.escapeWithSpaces(classNode.outerMethodDesc)))
						+ "\n");
			if (classNode.outerClass != null)
				ctx.next("// #OuterClass: " + UnicodeUtils.escapeWithSpaces(classNode.outerClass) + "\n");
			ctx.next("// #InnerClasses:\n");
			if (classNode.innerClasses != null) {
				String ics = "";
				for (InnerClassNode icn : classNode.innerClasses) {
					ics += "// " + UnicodeUtils.escapeWithSpaces(icn.name) + " "
							+ UnicodeUtils.escapeWithSpaces(icn.outerName) + " "
							+ UnicodeUtils.escapeWithSpaces(icn.innerName)
							+ (icn.access == 0 ? "" : " " + ClassUtil.getAccessFlagsFull(icn.access).trim()) + "\n";
				}
				ctx.next(ics);
			}
			ctx.next(ClassUtil.getAccessFlagsClass(classNode.access) + UnicodeUtils.escapeWithSpaces(classNode.name)
					+ " ");
			ctx.next("extends " + UnicodeUtils.escapeWithSpaces(classNode.superName) + " ");
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
					ctx.next("implements " + interfaceStr + " ");
			}
			ctx.next("{\n// #SourceFile: "
					+ (classNode.sourceFile == null ? "null" : UnicodeUtils.escape(classNode.sourceFile))
					+ "\n\n// #Fields\n");
			ctx.next(doFields(classNode.fields, nodeToFind, lineFound));
			ctx.next("\n// #Methods\n");
			ctx.next(doMethods(classNode.methods, classNode.name, nodeToFind, lineFound));
			ctx.next("}\n");
			return new DisassembleTuple(ctx.finish(), lineFound.get());
		} catch (Throwable e) {
			return new DisassembleTuple("Class couldn't be decompiled:\n" + e.getClass().getName() + ": "
					+ e.getMessage() + "\n"
					+ Arrays.toString(e.getStackTrace()).replace(", ", "\n\tat ").replace("[", "\tat ").replace("]", "")
					+ "\n");
		}
	}

	private static String doAnnotations(List<AnnotationNode> annotations) {
		if (annotations == null)
			return "";
		StringContext ctx = new StringContext(annotations.size());
		for (AnnotationNode annotationNode : annotations) {
			String s = "@" + UnicodeUtils.escapeWithSpaces(annotationNode.desc);
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
			ctx.next(s);
		}
		return ctx.finish();
	}

	private static String doMethods(List<MethodNode> methods, String className, Object nodeToFind,
			AtomicInteger lineFound) throws InterruptedException, ExecutionException {
		Future<String>[] futures = new Future[methods.size()];
		for (int i = 0; i < futures.length; i++) {
			MethodNode mn = methods.get(i);
			futures[i] = exec.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					String ms = "";
					try {
						ms += "\t// #Max: l:" + mn.maxLocals + " s:" + mn.maxStack + "\n";
						if (mn.signature != null)
							ms += "\t// #Signature: " + UnicodeUtils.escapeWithSpaces(mn.signature) + "\n";
						String[] dis = disassembleMethod(className, mn);
						ms += dis[2];
						ms += dis[1];
						if (mn.visibleAnnotations != null && !mn.visibleAnnotations.isEmpty()) {
							for (AnnotationNode annotationNode : mn.visibleAnnotations) {
								ms += "\t@" + annotationNode.desc;
								if (annotationNode.values != null && !annotationNode.values.isEmpty()) {
									ms += " (";
									boolean valBefore = true;
									for (Object o : annotationNode.values) {
										if (valBefore) {
											ms += o + " = [";
											valBefore = false;
											continue;
										} else {
											if (o instanceof String[]) {
												String[] arr = (String[]) o;
												boolean w8ing = false;
												for (String rofl : arr) {
													if (!w8ing) {
														ms += UnicodeUtils.escapeWithSpaces(rofl) + "/";
														w8ing = true;
													} else {
														ms += UnicodeUtils.escapeWithSpaces(rofl) + "]";
													}
												}
												ms += ", ";
											} else if (o instanceof List) {
												List list = (List) o;
												ms += "{ ";
												for (Object obj : list) {
													if (obj instanceof String[]) {
														String[] arr = (String[]) obj;
														boolean w8ing = false;
														for (String rofl : arr) {
															if (!w8ing) {
																ms += UnicodeUtils.escapeWithSpaces(rofl) + "/";
																w8ing = true;
															} else {
																ms += UnicodeUtils.escapeWithSpaces(rofl);
															}
														}
													} else {
														ms += "\"" + UnicodeUtils.escape(String.valueOf(obj)) + "\"";
													}
													ms += ", ";
												}
												ms = ms.substring(0, ms.length() - 2);
												ms += " }]";
											} else {
												ms += "(" + o.getClass().getName().replace(".", "/") + ") " + o + "], ";
											}
											valBefore = true;
										}
									}
									if (ms.endsWith(", "))
										ms = ms.substring(0, ms.length() - 2);
									ms += ")";
								}
								ms += "\n";
							}
						}
						if (mn.equals(nodeToFind)) {
							lineFound.set(ms.split("\\n").length);
						}
						ms += "\t" + ClassUtil.getAccessFlagsFull(mn.access) + UnicodeUtils.escapeWithSpaces(mn.name)
								+ " " + UnicodeUtils.escapeWithSpaces(mn.desc) + " ";
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
								ms += "throws " + exceptionStr + " ";
						}
						ms += "{\n";
						ms += dis[0];
						ms += "\t}\n\n";
					} catch (Exception e) {
						ms += "\t\t// Method couldn't be disassembled:\n\t\t// " + e.getClass().getName() + ": "
								+ e.getMessage() + "\n" + Arrays.toString(e.getStackTrace())
										.replace(", ", "\n\t\t// \tat ").replace("[", "\t\t// \tat ").replace("]", "")
								+ "\n\t}\n\n";
					}
					return ms;
				}
			});
		}
		StringContext ctx = new StringContext(futures.length);
		for (Future<String> f : futures) {
			ctx.next(f.get());
		}
		return ctx.finish();
	}

	private static String doFields(List<FieldNode> fields, Object nodeToFind, AtomicInteger lineFound) {
		StringContext ctx = new StringContext(fields.size());
		for (FieldNode fn : fields) {
			String s = "";
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
				lineFound.set(s.split("\\n").length);
			}
			s += "\t" + ClassUtil.getAccessFlagsFull(fn.access).replace("varargs", "transient")
					+ UnicodeUtils.escapeWithSpaces(fn.desc) + " " + UnicodeUtils.escapeWithSpaces(fn.name);
			if (fn.value != null) {
				s += " = " + ClassUtil.getDecompiledValue(fn.value, fn.desc, true);
			}
			s += "\n";
			ctx.next(s);
		}
		return ctx.finish();
	}

	private static String[] disassembleMethod(String className, MethodNode mn) {
		HashMap<Label, Integer> labels = new HashMap<Label, Integer>();
		String localVarTable;
		String tryCatchTable;
		for (AbstractInsnNode n : mn.instructions.toArray()) {
			if (n instanceof LabelNode) {
				labels.put(((LabelNode) n).getLabel(), labels.size() + 1);
			}
		}
		if (mn.localVariables != null && !mn.localVariables.isEmpty()) {
			List<LocalVariableNode> list = mn.localVariables;
			StringContext ctx = new StringContext(list.size() + 1);
			ctx.next("\t// #LocalVars:\n");
			for (LocalVariableNode lvn : list) {
				ctx.next("\t// " + UnicodeUtils.escapeWithSpaces(lvn.name) + ": "
						+ UnicodeUtils.escapeWithSpaces(lvn.desc) + " i:" + lvn.index + " s:"
						+ labels.get(lvn.start.getLabel()) + " e:" + labels.get(lvn.end.getLabel()) + " sig:"
						+ UnicodeUtils.escapeWithSpaces(lvn.signature) + "\n");
			}
			localVarTable = ctx.finish();
		} else
			localVarTable = "";
		if (mn.tryCatchBlocks != null && !mn.tryCatchBlocks.isEmpty()) {
			List<TryCatchBlockNode> list = mn.tryCatchBlocks;
			StringContext ctx = new StringContext(list.size() + 1);
			ctx.next("\t// #TryCatch:\n");
			for (TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
				ctx.next("\t// " + UnicodeUtils.escapeWithSpaces(tcbn.type) + " s:" + labels.get(tcbn.start.getLabel())
						+ " e:" + labels.get(tcbn.end.getLabel()) + " h:" + labels.get(tcbn.handler.getLabel()) + "\n");
			}
			tryCatchTable = ctx.finish();
		} else
			tryCatchTable = "";
		AbstractInsnNode[] insns = mn.instructions.toArray();
		StringContext ctx = new StringContext(insns.length);
		for (AbstractInsnNode n : insns) {
			ctx.next(disassembleInstruction(n, labels));
		}
		return new String[] { ctx.finish(), localVarTable, tryCatchTable };
	}

	public static final HashMap<Class, InstructionInterface> iiMap = new HashMap<>();

	static {
		iiMap.put(FieldInsnNode.class, (n, labels) -> {
			FieldInsnNode node = (FieldInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + " " + UnicodeUtils.escapeWithSpaces(node.owner) + "/"
					+ UnicodeUtils.escapeWithSpaces(node.name).replace("/", "\\u002F") + "\n";
		});
		iiMap.put(LabelNode.class, (n, labels) -> {
			LabelNode node = (LabelNode) n;
			return "\t\t// label " + labels.get(node.getLabel()) + "\n";
		});
		iiMap.put(FrameNode.class, (n, labels) -> {
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
		});
		iiMap.put(LineNumberNode.class, (n, labels) -> {
			LineNumberNode node = (LineNumberNode) n;
			return "\t\t// line " + node.line + " " + labels.get(node.start.getLabel()) + "\n";
		});
		iiMap.put(InvokeDynamicInsnNode.class, (n, labels) -> {
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
				if (l instanceof Type) {
					Type type = (Type) l;
					int valueBegin = type.valueBegin;
					int valueEnd = type.valueEnd;
					String buf = type.valueBuffer;
					// Reflection
					s += "\t\t\t\tType: [\n\t\t\t\t\ttype: " + ClassUtil.getClassNameFromType(type)
							+ "\n\t\t\t\t\tstart: " + valueBegin + "\n\t\t\t\t\tend: " + valueEnd + "\n\t\t\t\t\tbuf: "
							+ ClassUtil.getDecompiledValue(buf, "");
					s += "\n\t\t\t\t]\n";
				} else if (l instanceof Handle) {
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
		});
		iiMap.put(MethodInsnNode.class, (n, labels) -> {
			MethodInsnNode node = (MethodInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + " " + UnicodeUtils.escapeWithSpaces(node.owner) + "/"
					+ UnicodeUtils.escapeWithSpaces(node.name).replace("/", "\\u002F") + "\n";
		});
		iiMap.put(TypeInsnNode.class, (n, labels) -> {
			TypeInsnNode node = (TypeInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + "\n";
		});
		iiMap.put(MultiANewArrayInsnNode.class, (n, labels) -> {
			MultiANewArrayInsnNode node = (MultiANewArrayInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(node.desc) + " " + node.dims + "\n";
		});
		iiMap.put(LdcInsnNode.class, (n, labels) -> {
			LdcInsnNode node = (LdcInsnNode) n;
			if (node.cst instanceof Type) {
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
			} else {
				return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
						+ ClassUtil.getDecompiledValue(node.cst, "") + "\n";
			}
		});
		iiMap.put(VarInsnNode.class, (n, labels) -> {
			VarInsnNode node = (VarInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.var + "\n";
		});
		iiMap.put(InsnNode.class, (n, labels) -> {
			InsnNode node = (InsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + "\n";
		});
		iiMap.put(IntInsnNode.class, (n, labels) -> {
			IntInsnNode node = (IntInsnNode) n;
			if (n.getOpcode() == Opcodes.NEWARRAY) {
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " "
						+ ClassUtil.getArrayTypeByID(node.operand) + "\n";
			} else {
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.operand + "\n";
			}
		});
		iiMap.put(JumpInsnNode.class, (n, labels) -> {
			JumpInsnNode node = (JumpInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + labels.get(node.label.getLabel())
					+ "\n";
		});
		iiMap.put(TableSwitchInsnNode.class, (n, labels) -> {
			TableSwitchInsnNode node = (TableSwitchInsnNode) n;
			String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " [\n\t\t\tmin: " + node.min
					+ "\n\t\t\tmax: " + node.max + "\n\t\t\tdefault: " + labels.get(node.dflt.getLabel())
					+ "\n\t\t\tlabels: [\n";
			for (LabelNode l : node.labels) {
				s += "\t\t\t\t" + labels.get(l.getLabel()) + "\n";
			}
			s += "\t\t\t]\n\t\t]\n";
			return s;
		});
		iiMap.put(LookupSwitchInsnNode.class, (n, labels) -> {
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
		});
		iiMap.put(IincInsnNode.class, (n, labels) -> {
			IincInsnNode node = (IincInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.var + " " + node.incr + "\n";
		});
	}

	static interface InstructionInterface {
		String apply(AbstractInsnNode n, HashMap<Label, Integer> labels);
	}

	public static String disassembleInstruction(AbstractInsnNode n, HashMap<Label, Integer> labels) {
		InstructionInterface ii = iiMap.get(n.getClass());
		if (ii != null)
			return ii.apply(n, labels);
		else
			return "\t\tNOT HANDLED! " + OpcodesReverse.reverseOpcode(n.getOpcode()) + " "
					+ n.getClass().getSimpleName() + "\n";
	}

}
