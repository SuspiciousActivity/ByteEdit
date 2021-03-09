package me.ByteEdit.decompiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public interface IDecompiler {

	String decompile(ClassNode cn);

	default byte[] getBytes(ClassNode cn) {
		ClassWriter classWriter = new ClassWriter(0);
		cn.accept(classWriter);
		return classWriter.toByteArray();
	}

}
