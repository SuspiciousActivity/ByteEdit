package me.ByteEdit.decompiler;

import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public interface IDecompiler {

	String decompile(ClassNode cn, Map<String, ClassNode> classNodes);

	static byte[] getBytes(ClassNode cn) {
		ClassWriter classWriter = new ClassWriter(0);
		cn.accept(classWriter);
		return classWriter.toByteArray();
	}

}
