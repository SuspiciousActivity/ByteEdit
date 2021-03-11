package me.ByteEdit.edit;

import static org.objectweb.asm.tree.AbstractInsnNode.FIELD_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.FRAME;
import static org.objectweb.asm.tree.AbstractInsnNode.IINC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INT_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INVOKE_DYNAMIC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.JUMP_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.LABEL;
import static org.objectweb.asm.tree.AbstractInsnNode.LDC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.LINE;
import static org.objectweb.asm.tree.AbstractInsnNode.LOOKUPSWITCH_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.METHOD_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.MULTIANEWARRAY_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.TABLESWITCH_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.TYPE_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.VAR_INSN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

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
import org.objectweb.asm.tree.InsnList;
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

import me.ByteEdit.decompiler.IDecompiler;
import me.ByteEdit.main.Main;
import me.ByteEdit.utils.ClassUtil;
import me.ByteEdit.utils.OpcodesReverse;
import me.ByteEdit.utils.UnicodeUtils;

public class Disassembler implements IDecompiler {

	private static final ExecutorService exec = Executors
			.newFixedThreadPool((Runtime.getRuntime().availableProcessors() + 1) / 2);

	@Override
	public String decompile(ClassNode cn) {
		return disassemble(cn);
	}

	private static String disassemble(ClassNode classNode) {
		if (classNode == null) {
			return "ClassNode is null! This is not a valid java class file!";
		}

		try {
			HugeStrings hs = new HugeStrings();
			StringContext ctx = new StringContext(18);
			ctx.next("// #Annotations:\n");
			ctx.next(doAnnotations(classNode.visibleAnnotations, hs, ""));
			ctx.next("// #Class v:" + classNode.version + "\n");
			if (classNode.signature != null)
				ctx.next("// #Signature: " + UnicodeUtils.escapeWithSpaces(hs, classNode.signature) + "\n");
			if (classNode.outerMethod != null)
				ctx.next(
						"// #OuterMethod: "
								+ (classNode.outerMethod == null ? "null"
										: (UnicodeUtils.escapeWithSpaces(hs, classNode.outerMethod) + " "
												+ UnicodeUtils.escapeWithSpaces(hs, classNode.outerMethodDesc)))
								+ "\n");
			if (classNode.outerClass != null)
				ctx.next("// #OuterClass: " + UnicodeUtils.escapeWithSpaces(hs, classNode.outerClass) + "\n");
			ctx.next("// #InnerClasses:\n");
			if (classNode.innerClasses != null) {
				String ics = "";
				for (InnerClassNode icn : classNode.innerClasses) {
					ics += "// " + UnicodeUtils.escapeWithSpaces(hs, icn.name) + " "
							+ UnicodeUtils.escapeWithSpaces(hs, icn.outerName) + " "
							+ UnicodeUtils.escapeWithSpaces(hs, icn.innerName)
							+ (icn.access == 0 ? "" : " " + ClassUtil.getAccessFlagsFull(icn.access).trim()) + "\n";
				}
				ctx.next(ics);
			}
			ctx.next(ClassUtil.getAccessFlagsClass(classNode.access) + UnicodeUtils.escapeWithSpaces(hs, classNode.name)
					+ " ");
			ctx.next("extends " + UnicodeUtils.escapeWithSpaces(hs, classNode.superName) + " ");
			if (classNode.interfaces != null && !classNode.interfaces.isEmpty()) {
				String interfaceStr = "";
				for (String interfc : classNode.interfaces) {
					if (interfaceStr.isEmpty()) {
						interfaceStr += UnicodeUtils.escapeWithSpaces(hs, interfc);
					} else {
						interfaceStr += ", " + UnicodeUtils.escapeWithSpaces(hs, interfc);
					}
				}
				if (!interfaceStr.isEmpty())
					ctx.next("implements " + interfaceStr + " ");
			}
			ctx.next("{\n// #SourceFile: "
					+ (classNode.sourceFile == null ? "null" : UnicodeUtils.escape(hs, classNode.sourceFile)) + "\n");
			int hugeIdx = ctx.next("");
			ctx.next("\n// #Fields\n");
			ctx.next(doFields(classNode.fields, hs));
			ctx.next("\n// #Methods\n");
			ctx.next(doMethods(classNode.methods, hs));
			ctx.next("}\n");

			if (!hs.isEmpty())
				ctx.set(hugeIdx, hs.makeStringsList());
			return ctx.finish();
		} catch (Throwable e) {
			return "Class couldn't be decompiled:\n" + e.getClass().getName() + ": " + e.getMessage() + "\n"
					+ Arrays.toString(e.getStackTrace()).replace(", ", "\n\tat ").replace("[", "\tat ").replace("]", "")
					+ "\n";
		}
	}

	private static String doAnnotations(List<AnnotationNode> annotations, HugeStrings hs, String linePrefix) {
		if (annotations == null)
			return "";
		StringContext ctx = new StringContext(annotations.size());
		for (AnnotationNode annotationNode : annotations) {
			String s = linePrefix + "@" + UnicodeUtils.escapeWithSpaces(hs, annotationNode.desc);
			if (annotationNode.values != null && !annotationNode.values.isEmpty()) {
				s += " (";
				boolean valBefore = true;
				for (Object o : annotationNode.values) {
					if (valBefore) {
						s += o + " = [";
						valBefore = false;
						continue;
					}
					if (o instanceof String[]) {
						String[] arr = (String[]) o;
						boolean w8ing = false;
						for (String rofl : arr) {
							if (!w8ing) {
								s += UnicodeUtils.escapeWithSpaces(hs, rofl) + "/";
								w8ing = true;
							} else {
								s += UnicodeUtils.escapeWithSpaces(hs, rofl) + "]";
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
										s += UnicodeUtils.escapeWithSpaces(hs, rofl) + "/";
										w8ing = true;
									} else {
										s += UnicodeUtils.escapeWithSpaces(hs, rofl);
									}
								}
							} else {
								s += "\"" + UnicodeUtils.escape(hs, String.valueOf(obj)) + "\"";
							}
							s += ", ";
						}
						s = s.substring(0, s.length() - 2);
						s += " }], ";
					} else if (o instanceof String) {
						s += "\"" + UnicodeUtils.escapeWithSpaces(hs, (String) o) + "\"], ";
					} else {
						s += "(" + o.getClass().getName().replace(".", "/") + ") " + o + "], ";
					}
					valBefore = true;
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

	private static String doMethods(List<MethodNode> methods, HugeStrings hs)
			throws InterruptedException, ExecutionException {
		boolean multithreaded = Main.INSTANCE.mntmMultithreaded.isSelected();
		Future<String>[] futures = new Future[methods.size()];
		for (int i = 0; i < futures.length; i++) {
			MethodNode mn = methods.get(i);
			Callable<String> callable = new Callable<String>() {
				@Override
				public String call() throws Exception {
					String ms = "";
					try {
						ms += "\t// #Max: l:" + mn.maxLocals + " s:" + mn.maxStack + "\n";
						if (mn.signature != null)
							ms += "\t// #Signature: " + UnicodeUtils.escapeWithSpaces(hs, mn.signature) + "\n";
						String[] dis = disassembleMethod(mn, hs);
						ms += dis[2];
						ms += dis[1];
						if (mn.visibleAnnotations != null && !mn.visibleAnnotations.isEmpty())
							ms += doAnnotations(mn.visibleAnnotations, hs, "\t");
						ms += "\t" + ClassUtil.getAccessFlagsFull(mn.access)
								+ UnicodeUtils.escapeWithSpaces(hs, mn.name) + " "
								+ UnicodeUtils.escapeWithSpaces(hs, mn.desc) + " ";
						if (mn.exceptions != null && !mn.exceptions.isEmpty()) {
							String exceptionStr = "";
							for (String exc : mn.exceptions) {
								if (exceptionStr.isEmpty()) {
									exceptionStr += UnicodeUtils.escapeWithSpaces(hs, exc);
								} else {
									exceptionStr += ", " + UnicodeUtils.escapeWithSpaces(hs, exc);
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
			};
			futures[i] = multithreaded ? exec.submit(callable) : new FutureTask<>(callable);
		}
		StringContext ctx = new StringContext(futures.length);
		for (Future<String> f : futures) {
			if (f instanceof FutureTask)
				((FutureTask) f).run();
			ctx.next(f.get());
		}
		String s = ctx.finish();
		if (s.isEmpty())
			return "\n";
		return s;
	}

	private static String doFields(List<FieldNode> fields, HugeStrings hs) {
		StringContext ctx = new StringContext(fields.size());
		for (FieldNode fn : fields) {
			String s = "";
			if (fn.signature != null)
				s += "\t// #Signature: " + UnicodeUtils.escapeWithSpaces(hs, fn.signature) + "\n";
			if (fn.visibleAnnotations != null && !fn.visibleAnnotations.isEmpty())
				s += doAnnotations(fn.visibleAnnotations, hs, "\t");
			s += "\t" + ClassUtil.getAccessFlagsFull(fn.access).replace("varargs", "transient")
					+ UnicodeUtils.escapeWithSpaces(hs, fn.desc) + " " + UnicodeUtils.escapeWithSpaces(hs, fn.name);
			if (fn.value != null) {
				s += " = " + ClassUtil.getDecompiledValue(fn.value, fn.desc, true, hs);
			}
			s += "\n";
			ctx.next(s);
		}
		return ctx.finish();
	}

	private static String[] disassembleMethod(MethodNode mn, HugeStrings hs) {
		HashMap<Label, Integer> labels = new HashMap<Label, Integer>();
		String localVarTable;
		String tryCatchTable;

		InsnList insnList = mn.instructions;
		boolean deobfNumbers = Main.INSTANCE.mntmNumbers.isSelected();

		ArrayList<Label> labelsReversed = deobfNumbers ? new ArrayList<>() : null;

		for (AbstractInsnNode n = insnList.getFirst(); n != null; n = n.getNext()) {
			if (n instanceof LabelNode) {
				int id = labels.size() + 1;
				if (deobfNumbers && labelsReversed != null)
					labelsReversed.add(((LabelNode) n).getLabel());
				labels.put(((LabelNode) n).getLabel(), id);
			}
		}
		if (mn.localVariables != null && !mn.localVariables.isEmpty()) {
			List<LocalVariableNode> list = mn.localVariables;
			StringContext ctx = new StringContext(list.size() + 1);
			ctx.next("\t// #LocalVars:\n");
			for (LocalVariableNode lvn : list) {
				ctx.next("\t// " + UnicodeUtils.escapeWithSpaces(hs, lvn.name) + ": "
						+ UnicodeUtils.escapeWithSpaces(hs, lvn.desc) + " i:" + lvn.index + " s:"
						+ labels.get(lvn.start.getLabel()) + " e:" + labels.get(lvn.end.getLabel()) + " sig:"
						+ UnicodeUtils.escapeWithSpaces(hs, lvn.signature) + "\n");
			}
			localVarTable = ctx.finish();
		} else
			localVarTable = "";
		if (mn.tryCatchBlocks != null && !mn.tryCatchBlocks.isEmpty()) {
			List<TryCatchBlockNode> list = mn.tryCatchBlocks;
			StringContext ctx = new StringContext(list.size() + 1);
			ctx.next("\t// #TryCatch:\n");
			for (TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
				ctx.next("\t// " + UnicodeUtils.escapeWithSpaces(hs, tcbn.type) + " s:"
						+ labels.get(tcbn.start.getLabel()) + " e:" + labels.get(tcbn.end.getLabel()) + " h:"
						+ labels.get(tcbn.handler.getLabel()) + "\n");
			}
			tryCatchTable = ctx.finish();
		} else
			tryCatchTable = "";

		if (deobfNumbers) {
			StackBasedCalculator calc = new StackBasedCalculator(insnList);
			calc.run();
			insnList = calc.get();
			int i = 0;
			for (AbstractInsnNode n = insnList.getFirst(); n != null; n = n.getNext()) {
				if (n instanceof LabelNode && labelsReversed != null) {
					((LabelNode) n).setLabel(labelsReversed.get(i++));
				}
			}
		}

		StringContext ctx = new StringContext(insnList.size());
		for (AbstractInsnNode n = insnList.getFirst(); n != null; n = n.getNext()) {
			ctx.next(disassembleInstruction(n, labels, hs));
		}
		return new String[] { ctx.finish(), localVarTable, tryCatchTable };
	}

	public static String disassembleInstruction(AbstractInsnNode n, HashMap<Label, Integer> labels, HugeStrings hs) {
		switch (n.getType()) {
		case INSN: {
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + "\n";
		}
		case INT_INSN: {
			IntInsnNode node = (IntInsnNode) n;
			if (n.getOpcode() == Opcodes.NEWARRAY) {
				return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " "
						+ ClassUtil.getArrayTypeByID(node.operand) + "\n";
			}
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.operand + "\n";
		}
		case VAR_INSN: {
			VarInsnNode node = (VarInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.var + "\n";
		}
		case TYPE_INSN: {
			TypeInsnNode node = (TypeInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(hs, node.desc) + "\n";
		}
		case FIELD_INSN: {
			FieldInsnNode node = (FieldInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(hs, node.desc) + " " + UnicodeUtils.escapeWithSpaces(hs, node.owner)
					+ "/" + UnicodeUtils.escapeWithSpaces(hs, node.name).replace("/", "\\u002F") + "\n";
		}
		case METHOD_INSN: {
			MethodInsnNode node = (MethodInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(hs, node.desc) + " " + UnicodeUtils.escapeWithSpaces(hs, node.owner)
					+ "/" + UnicodeUtils.escapeWithSpaces(hs, node.name).replace("/", "\\u002F") + "\n";
		}
		case INVOKE_DYNAMIC_INSN: {
			InvokeDynamicInsnNode node = (InvokeDynamicInsnNode) n;
			String s = "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " [\n\t\t\tname: "
					+ UnicodeUtils.escapeWithSpaces(hs, node.name).replace("/", "\\u002F") + "\n\t\t\tdesc: "
					+ UnicodeUtils.escapeWithSpaces(hs, node.desc) + "\n\t\t\tHandle: [\n\t\t\t\tname: "
					+ UnicodeUtils.escapeWithSpaces(hs, node.bsm.getName()) + "\n\t\t\t\towner: "
					+ UnicodeUtils.escapeWithSpaces(hs, node.bsm.getOwner()) + "\n\t\t\t\tdesc: "
					+ UnicodeUtils.escapeWithSpaces(hs, node.bsm.getDesc()) + "\n\t\t\t\tisInterface: "
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
							+ ClassUtil.getDecompiledValue(buf, "", hs);
					s += "\n\t\t\t\t]\n";
				} else if (l instanceof Handle) {
					Handle h = (Handle) l;
					s += "\t\t\t\tHandle: [\n\t\t\t\t\tname: "
							+ UnicodeUtils.escapeWithSpaces(hs, h.getName()).replace("/", "\\u002F")
							+ "\n\t\t\t\t\towner: " + UnicodeUtils.escapeWithSpaces(hs, h.getOwner())
							+ "\n\t\t\t\t\tdesc: " + UnicodeUtils.escapeWithSpaces(hs, h.getDesc())
							+ "\n\t\t\t\t\tisInterface: " + h.isInterface() + "\n\t\t\t\t\ttag: "
							+ OpcodesReverse.reverseHandleOpcode(h.getTag());
					s += "\n\t\t\t\t]\n";
				} else {
					s += "\t\t\t\t" + ClassUtil.getDecompiledValue(l, "", hs) + "\n";
				}
			}
			s += "\t\t\t]\n\t\t]\n";
			return s;
		}
		case JUMP_INSN: {
			JumpInsnNode node = (JumpInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + labels.get(node.label.getLabel())
					+ "\n";
		}
		case LABEL: {
			LabelNode node = (LabelNode) n;
			return "\t\t// label " + labels.get(node.getLabel()) + "\n";
		}
		case LDC_INSN: {
			LdcInsnNode node = (LdcInsnNode) n;
			if (node.cst instanceof Type) {
				Type type = (Type) node.cst;
				int valueBegin = type.valueBegin;
				int valueEnd = type.valueEnd;
				String buf = type.valueBuffer;
				// Reflection
				String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " Type: [\n\t\t\ttype: "
						+ ClassUtil.getClassNameFromType(type) + "\n\t\t\tstart: " + valueBegin + "\n\t\t\tend: "
						+ valueEnd + "\n\t\t\tbuf: " + ClassUtil.getDecompiledValue(buf, "", hs);
				s += "\n\t\t]\n";
				return s;
			}
			String decomp = ClassUtil.getDecompiledValue(node.cst, "", hs);
			if (node.cst instanceof String) {
				if (decomp.startsWith("\"#") && decomp.endsWith("\""))
					decomp = decomp.substring(1, decomp.length() - 1);
				else if (decomp.startsWith("\"\\u0023"))
					decomp = "\"#" + decomp.substring(7);
			}
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " " + decomp + "\n";
		}
		case IINC_INSN: {
			IincInsnNode node = (IincInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " " + node.var + " " + node.incr + "\n";
		}
		case TABLESWITCH_INSN: {
			TableSwitchInsnNode node = (TableSwitchInsnNode) n;
			String s = "\t\t" + OpcodesReverse.reverseOpcode(n.getOpcode()) + " [\n\t\t\tmin: " + node.min
					+ "\n\t\t\tmax: " + node.max + "\n\t\t\tdefault: " + labels.get(node.dflt.getLabel())
					+ "\n\t\t\tlabels: [\n";
			for (LabelNode l : node.labels) {
				s += "\t\t\t\t" + labels.get(l.getLabel()) + "\n";
			}
			s += "\t\t\t]\n\t\t]\n";
			return s;
		}
		case LOOKUPSWITCH_INSN: {
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
		}
		case MULTIANEWARRAY_INSN: {
			MultiANewArrayInsnNode node = (MultiANewArrayInsnNode) n;
			return "\t\t" + OpcodesReverse.reverseOpcode(node.getOpcode()) + " "
					+ UnicodeUtils.escapeWithSpaces(hs, node.desc) + " " + node.dims + "\n";
		}
		case FRAME: {
			FrameNode node = (FrameNode) n;
			String out = "";
			switch (node.type) {
			case Opcodes.F_SAME:
				out = "SAME";
				break;
			case Opcodes.F_SAME1:
				out = "SAME1 " + getFrameArray(hs, node.stack, labels);
				break;
			case Opcodes.F_CHOP:
				out = "CHOP " + Integer.toString(node.local.size());
				break;
			case Opcodes.F_APPEND:
				out = "APPEND " + getFrameArray(hs, node.local, labels);
				break;
			case Opcodes.F_FULL:
				out = "FULL " + getFrameArray(hs, node.local, labels) + " " + getFrameArray(hs, node.stack, labels);
				break;
			}
			return "\t\t// frame " + out + "\n";
		}
		case LINE: {
			LineNumberNode node = (LineNumberNode) n;
			return "\t\t// line " + node.line + " " + labels.get(node.start.getLabel()) + "\n";
		}
		default: {
			return "\t\tNOT HANDLED! " + OpcodesReverse.reverseOpcode(n.getOpcode()) + " "
					+ n.getClass().getSimpleName() + "\n";
		}
		}
	}

	private static String getFrameArray(HugeStrings hs, List<Object> list, HashMap<Label, Integer> labels) {
		ArrayList<String> arr = new ArrayList<>();
		if (list != null) {
			for (Object o : list) {
				if (o == null) {
					arr.add(null);
				} else {
					if (o instanceof String) {
						arr.add("\"" + UnicodeUtils.escapeWithSpaces(hs, (String) o) + "\"");
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
			return arr.toString();
		}
		return "null";
	}
}
