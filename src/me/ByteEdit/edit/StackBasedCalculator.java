package me.ByteEdit.edit;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class StackBasedCalculator implements Opcodes, Runnable {

	private final InsnList insns;
	private final Stack<Number> stack = new Stack<>();
	private AbstractInsnNode start;
	private int startIdx = -1;
	private boolean isAtEnd = false;

	public StackBasedCalculator(InsnList insns) {
		this.insns = new InsnList();

		HashMap<LabelNode, LabelNode> labelMap = new HashMap<>();
		for (AbstractInsnNode node = insns.getFirst(); node != null; node = node.getNext()) {
			if (node.getType() == AbstractInsnNode.LABEL) {
				labelMap.put((LabelNode) node, new LabelNode());
			}
		}

		for (AbstractInsnNode node = insns.getFirst(); node != null; node = node.getNext()) {
			this.insns.add(node.clone(labelMap));
		}
	}

	public InsnList get() {
		return insns;
	}

	@Override
	public void run() {
		while (!isAtEnd) {
			out: {
				AbstractInsnNode[] arr = insns.toArray();
				for (int i = startIdx == -1 ? 0 : startIdx; i < arr.length; i++) {
					if (!handle(arr[i]))
						break out;
				}
				isAtEnd = true;
			}
		}
	}

	private boolean handle(AbstractInsnNode node) {
		try {
			switch (node.getOpcode()) {
			case ICONST_0:
				stack.push(Integer.valueOf(0));
				if (start == null)
					start = node;
				break;
			case ICONST_1:
				stack.push(Integer.valueOf(1));
				if (start == null)
					start = node;
				break;
			case ICONST_2:
				stack.push(Integer.valueOf(2));
				if (start == null)
					start = node;
				break;
			case ICONST_3:
				stack.push(Integer.valueOf(3));
				if (start == null)
					start = node;
				break;
			case ICONST_4:
				stack.push(Integer.valueOf(4));
				if (start == null)
					start = node;
				break;
			case ICONST_5:
				stack.push(Integer.valueOf(5));
				if (start == null)
					start = node;
				break;
			case ICONST_M1:
				stack.push(Integer.valueOf(-1));
				if (start == null)
					start = node;
				break;
			case DCONST_0:
				stack.push(Double.valueOf(0));
				if (start == null)
					start = node;
				break;
			case DCONST_1:
				stack.push(Double.valueOf(1));
				if (start == null)
					start = node;
				break;
			case FCONST_0:
				stack.push(Float.valueOf(0));
				if (start == null)
					start = node;
				break;
			case FCONST_1:
				stack.push(Float.valueOf(1));
				if (start == null)
					start = node;
				break;
			case FCONST_2:
				stack.push(Float.valueOf(2));
				if (start == null)
					start = node;
				break;
			case LCONST_0:
				stack.push(Long.valueOf(0));
				if (start == null)
					start = node;
				break;
			case LCONST_1:
				stack.push(Long.valueOf(1));
				if (start == null)
					start = node;
				break;
			case BIPUSH:
			case SIPUSH:
				stack.push(((IntInsnNode) node).operand);
				if (start == null)
					start = node;
				break;
			case D2F:
				stack.push(((Double) stack.pop()).floatValue());
				change(node);
				return false;
			case D2I:
				stack.push(((Double) stack.pop()).intValue());
				change(node);
				return false;
			case D2L:
				stack.push(((Double) stack.pop()).longValue());
				change(node);
				return false;
			case F2D:
				stack.push(((Float) stack.pop()).doubleValue());
				change(node);
				return false;
			case F2I:
				stack.push(((Float) stack.pop()).intValue());
				change(node);
				return false;
			case F2L:
				stack.push(((Float) stack.pop()).longValue());
				change(node);
				return false;
			case L2D:
				stack.push(((Long) stack.pop()).doubleValue());
				change(node);
				return false;
			case L2F:
				stack.push(((Long) stack.pop()).floatValue());
				change(node);
				return false;
			case L2I:
				stack.push(((Long) stack.pop()).intValue());
				change(node);
				return false;
			case I2B:
				error(node);
				return true;
			case I2C:
				error(node);
				return true;
			case I2D:
				stack.push(((Integer) stack.pop()).doubleValue());
				change(node);
				return false;
			case I2F:
				stack.push(((Integer) stack.pop()).floatValue());
				change(node);
				return false;
			case I2L:
				stack.push(((Integer) stack.pop()).longValue());
				change(node);
				return false;
			case I2S:
				error(node);
				return true;
			case NOP:
				break;
			case LDC:
				LdcInsnNode ldc = (LdcInsnNode) node;
				if (ldc.cst instanceof Number) {
					stack.push((Number) ldc.cst);
					if (start == null)
						start = node;
				} else {
					error(node);
					return true;
				}
				break;
			// long
			case LADD: {
				long top = ((Long) stack.pop()).longValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom + top);
				change(node);
				return false;
			}
			case LSUB: {
				long top = ((Long) stack.pop()).longValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom - top);
				change(node);
				return false;
			}
			case LMUL: {
				long top = ((Long) stack.pop()).longValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom * top);
				change(node);
				return false;
			}
			case LDIV: {
				long top = ((Long) stack.pop()).longValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom / top);
				change(node);
				return false;
			}
			case LREM: {
				long top = ((Long) stack.pop()).longValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom % top);
				change(node);
				return false;
			}
			case LNEG: {
				long top = ((Long) stack.pop()).longValue();
				stack.push(-top);
				change(node);
				return false;
			}
			case LAND: {
				long top = ((Long) stack.pop()).longValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom & top);
				change(node);
				return false;
			}
			case LOR: {
				long top = ((Long) stack.pop()).longValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom | top);
				change(node);
				return false;
			}
			case LXOR: {
				long top = ((Long) stack.pop()).longValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom ^ top);
				change(node);
				return false;
			}
			case LSHR: {
				int top = ((Integer) stack.pop()).intValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom >> top);
				change(node);
				return false;
			}
			case LSHL: {
				int top = ((Integer) stack.pop()).intValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom << top);
				change(node);
				return false;
			}
			case LUSHR: {
				int top = ((Integer) stack.pop()).intValue();
				long bottom = ((Long) stack.pop()).longValue();
				stack.push(bottom >>> top);
				change(node);
				return false;
			}
			// int
			case IADD: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom + top);
				change(node);
				return false;
			}
			case ISUB: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom - top);
				change(node);
				return false;
			}
			case IMUL: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom * top);
				change(node);
				return false;
			}
			case IDIV: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom / top);
				change(node);
				return false;
			}
			case IREM: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom % top);
				change(node);
				return false;
			}
			case INEG: {
				int top = ((Integer) stack.pop()).intValue();
				stack.push(-top);
				change(node);
				return false;
			}
			case IAND: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom & top);
				change(node);
				return false;
			}
			case IOR: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom | top);
				change(node);
				return false;
			}
			case IXOR: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom ^ top);
				change(node);
				return false;
			}
			case ISHR: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom >> top);
				change(node);
				return false;
			}
			case ISHL: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom << top);
				change(node);
				return false;
			}
			case IUSHR: {
				int top = ((Integer) stack.pop()).intValue();
				int bottom = ((Integer) stack.pop()).intValue();
				stack.push(bottom >>> top);
				change(node);
				return false;
			}
			// double
			case DADD: {
				double top = ((Double) stack.pop()).doubleValue();
				double bottom = ((Double) stack.pop()).doubleValue();
				stack.push(bottom + top);
				change(node);
				return false;
			}
			case DSUB: {
				double top = ((Double) stack.pop()).doubleValue();
				double bottom = ((Double) stack.pop()).doubleValue();
				stack.push(bottom - top);
				change(node);
				return false;
			}
			case DMUL: {
				double top = ((Double) stack.pop()).doubleValue();
				double bottom = ((Double) stack.pop()).doubleValue();
				stack.push(bottom * top);
				change(node);
				return false;
			}
			case DDIV: {
				double top = ((Double) stack.pop()).doubleValue();
				double bottom = ((Double) stack.pop()).doubleValue();
				stack.push(bottom / top);
				change(node);
				return false;
			}
			case DREM: {
				double top = ((Double) stack.pop()).doubleValue();
				double bottom = ((Double) stack.pop()).doubleValue();
				stack.push(bottom % top);
				change(node);
				return false;
			}
			case DNEG: {
				double top = ((Double) stack.pop()).doubleValue();
				stack.push(-top);
				change(node);
				return false;
			}
			// float
			case FADD: {
				float top = ((Float) stack.pop()).floatValue();
				float bottom = ((Float) stack.pop()).floatValue();
				stack.push(bottom + top);
				change(node);
				return false;
			}
			case FSUB: {
				float top = ((Float) stack.pop()).floatValue();
				float bottom = ((Float) stack.pop()).floatValue();
				stack.push(bottom - top);
				change(node);
				return false;
			}
			case FMUL: {
				float top = ((Float) stack.pop()).floatValue();
				float bottom = ((Float) stack.pop()).floatValue();
				stack.push(bottom * top);
				change(node);
				return false;
			}
			case FDIV: {
				float top = ((Float) stack.pop()).floatValue();
				float bottom = ((Float) stack.pop()).floatValue();
				stack.push(bottom / top);
				change(node);
				return false;
			}
			case FREM: {
				float top = ((Float) stack.pop()).floatValue();
				float bottom = ((Float) stack.pop()).floatValue();
				stack.push(bottom % top);
				change(node);
				return false;
			}
			case FNEG: {
				float top = ((Float) stack.pop()).floatValue();
				stack.push(-top);
				change(node);
				return false;
			}
			// dup
			case DUP: {
				Number num = stack.pop();
				if (num instanceof Double || num instanceof Long) {
					error(node);
					return true;
				}
				stack.push(num);
				stack.push(num);
				change(node);
				return false;
			}
			case DUP_X1: {
				Number top = stack.pop();
				Number bottom = stack.pop();
				if (bottom instanceof Double || bottom instanceof Long || top instanceof Double
						|| top instanceof Long) {
					error(node);
					return true;
				}
				stack.push(top);
				stack.push(bottom);
				stack.push(top);
				change(node);
				return false;
			}
			case DUP_X2: {
				Number top = stack.pop();
				Number bottom = stack.pop();
				if (top instanceof Double || top instanceof Long) {
					error(node);
					return true;
				}
				Number bottom2 = null;
				if (!(bottom instanceof Double || bottom instanceof Long)) {
					bottom2 = stack.pop();
					if (bottom instanceof Double || bottom instanceof Long) {
						error(node);
						return true;
					}
				}
				stack.push(top);
				if (bottom2 != null)
					stack.push(bottom2);
				stack.push(bottom);
				stack.push(top);
				change(node);
				return false;
			}
			case DUP2: {
				Number top = stack.pop();
				Number bottom = null;
				if (!(top instanceof Double || top instanceof Long)) {
					bottom = stack.pop();
					if (bottom instanceof Double || bottom instanceof Long) {
						error(node);
						return true;
					}
				}
				if (bottom != null)
					stack.push(bottom);
				stack.push(top);
				if (bottom != null)
					stack.push(bottom);
				stack.push(top);
				change(node);
				return false;
			}
			case DUP2_X1: {
				Number top = stack.pop();
				Number bottom = null;
				if (!(top instanceof Double || top instanceof Long)) {
					bottom = stack.pop();
					if (bottom instanceof Double || bottom instanceof Long) {
						error(node);
						return true;
					}
				}
				Number bottom2 = stack.pop();
				if (bottom != null)
					stack.push(bottom);
				stack.push(top);
				stack.push(bottom2);
				if (bottom != null)
					stack.push(bottom);
				stack.push(top);
				change(node);
				return false;
			}
			case DUP2_X2: {
				Number top = stack.pop();
				Number bottom = null;
				if (!(top instanceof Double || top instanceof Long)) {
					bottom = stack.pop();
					if (bottom instanceof Double || bottom instanceof Long) {
						error(node);
						return true;
					}
				}
				Number top2 = stack.pop();
				Number bottom2 = null;
				if (!(top2 instanceof Double || top2 instanceof Long)) {
					bottom2 = stack.pop();
					if (bottom2 instanceof Double || bottom2 instanceof Long) {
						error(node);
						return true;
					}
				}
				if (bottom != null)
					stack.push(bottom);
				stack.push(top);
				if (bottom2 != null)
					stack.push(bottom2);
				stack.push(top2);
				if (bottom != null)
					stack.push(bottom);
				stack.push(top);
				change(node);
				return false;
			}
			// stack
			case POP:
				stack.pop();
				change(node);
				return false;
			case POP2:
				Number val = stack.pop();
				if(!(val instanceof Long) && !(val instanceof Double)) {
					stack.pop();
				}
				change(node);
				return false;
			case SWAP: {
				Number top = stack.pop();
				Number bottom = stack.pop();
				if (bottom instanceof Double || bottom instanceof Long || top instanceof Double
						|| top instanceof Long) {
					error(node);
					return true;
				}
				stack.push(top);
				stack.push(bottom);
				change(node);
				return false;
			}
			default:
				error(node);
				return true;
			}
		} catch (ClassCastException | EmptyStackException ex) {
			error(node);
			return true;
		}
		return true;
	}

	private void error(AbstractInsnNode node) {
		change(node.getPrevious());
		stack.clear();
		start = null;
	}

	private void change(AbstractInsnNode node) {
		if (start == null)
			return;
		int idx = insns.indexOf(start);
		startIdx = idx;
		int idx2 = insns.indexOf(node);
		if (idx != idx2) {
			for (int i = 0; i <= idx2 - idx; i++) {
				insns.remove(insns.get(idx));
			}
			while (!stack.isEmpty()) {
				addNode(idx, stack.pop());
			}
		}
		stack.clear();
		start = null;
	}

	private void addNode(int idx, Number n) {
		if (n instanceof Double) {
			double val = n.doubleValue();
			if (val == 0.0)
				insns.insertBefore(insns.get(idx), new InsnNode(DCONST_0));
			else if (val == 1.0)
				insns.insertBefore(insns.get(idx), new InsnNode(DCONST_1));
			else
				insns.insertBefore(insns.get(idx), new LdcInsnNode(val));
		} else if (n instanceof Float) {
			float val = n.floatValue();
			if (val == 0.0f)
				insns.insertBefore(insns.get(idx), new InsnNode(FCONST_0));
			else if (val == 1.0f)
				insns.insertBefore(insns.get(idx), new InsnNode(FCONST_1));
			else if (val == 2.0f)
				insns.insertBefore(insns.get(idx), new InsnNode(FCONST_2));
			else
				insns.insertBefore(insns.get(idx), new LdcInsnNode(val));
		} else if (n instanceof Long) {
			long val = n.longValue();
			if (val == 0l)
				insns.insertBefore(insns.get(idx), new InsnNode(LCONST_0));
			else if (val == 1l)
				insns.insertBefore(insns.get(idx), new InsnNode(LCONST_1));
			else
				insns.insertBefore(insns.get(idx), new LdcInsnNode(val));
		} else if (n instanceof Integer) {
			int val = n.intValue();
			if (val == 0)
				insns.insertBefore(insns.get(idx), new InsnNode(ICONST_0));
			else if (val == 1)
				insns.insertBefore(insns.get(idx), new InsnNode(ICONST_1));
			else if (val == 2)
				insns.insertBefore(insns.get(idx), new InsnNode(ICONST_2));
			else if (val == 3)
				insns.insertBefore(insns.get(idx), new InsnNode(ICONST_3));
			else if (val == 4)
				insns.insertBefore(insns.get(idx), new InsnNode(ICONST_4));
			else if (val == 5)
				insns.insertBefore(insns.get(idx), new InsnNode(ICONST_5));
			else if (val == -1)
				insns.insertBefore(insns.get(idx), new InsnNode(ICONST_M1));
			else if (val == (byte) val)
				insns.insertBefore(insns.get(idx), new IntInsnNode(BIPUSH, (byte) val));
			else if (val == (short) val)
				insns.insertBefore(insns.get(idx), new IntInsnNode(SIPUSH, (short) val));
			else
				insns.insertBefore(insns.get(idx), new LdcInsnNode(val));
		} else {
			throw new RuntimeException("Unexpected number: " + n);
		}
	}

}
