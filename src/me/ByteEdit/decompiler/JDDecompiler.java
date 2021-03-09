package me.ByteEdit.decompiler;

import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.core.v1.api.printer.Printer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import me.ByteEdit.main.Main;

public class JDDecompiler implements Loader, Printer, IDecompiler {

	@Override
	public String decompile(ClassNode cn) {
		ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
		try {
			decompiler.decompile(this, this, cn.name);
		} catch (Throwable e) {
			e.printStackTrace();
			String str = e.getClass().getName() + " - " + e.getMessage() + System.lineSeparator();
			for (int i = 0; i < e.getStackTrace().length; i++) {
				str += e.getStackTrace()[i].toString() + System.lineSeparator();
			}
			return str;
		}
		String decomp = toString();
		indentationCount = 0;
		sb = new StringBuilder();
		return decomp;
	}

	@Override
	public boolean canLoad(String internalName) {
		return Main.classNodes.containsKey(internalName + ".class");
	}

	@Override
	public byte[] load(String internalName) throws LoaderException {
		ClassNode cl = Main.classNodes.get(internalName + ".class");
		ClassWriter cw = new ClassWriter(0);
		cl.accept(cw);
		return cw.toByteArray();
	}

	protected static final String TAB = "  ";
	protected static final String NEWLINE = "\n";

	protected int indentationCount = 0;
	protected StringBuilder sb = new StringBuilder();

	@Override
	public String toString() {
		return sb.toString();
	}

	@Override
	public void start(int maxLineNumber, int majorVersion, int minorVersion) {
	}

	@Override
	public void end() {
	}

	@Override
	public void printText(String text) {
		sb.append(text);
	}

	@Override
	public void printNumericConstant(String constant) {
		sb.append(constant);
	}

	@Override
	public void printStringConstant(String constant, String ownerInternalName) {
		sb.append(constant);
	}

	@Override
	public void printKeyword(String keyword) {
		sb.append(keyword);
	}

	@Override
	public void printDeclaration(int type, String internalTypeName, String name, String descriptor) {
		sb.append(name);
	}

	@Override
	public void printReference(int type, String internalTypeName, String name, String descriptor,
			String ownerInternalName) {
		sb.append(name);
	}

	@Override
	public void indent() {
		this.indentationCount++;
	}

	@Override
	public void unindent() {
		this.indentationCount--;
	}

	@Override
	public void startLine(int lineNumber) {
		for (int i = 0; i < indentationCount; i++)
			sb.append(TAB);
	}

	@Override
	public void endLine() {
		sb.append(NEWLINE);
	}

	@Override
	public void extraLine(int count) {
		while (count-- > 0)
			sb.append(NEWLINE);
	}

	@Override
	public void startMarker(int type) {
	}

	@Override
	public void endMarker(int type) {
	}
}
