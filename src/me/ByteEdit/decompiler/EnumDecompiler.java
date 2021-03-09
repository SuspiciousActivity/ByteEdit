package me.ByteEdit.decompiler;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import me.ByteEdit.edit.Disassembler;

public enum EnumDecompiler {
	BYTEEDIT("ByteEdit", new Disassembler(), false), PROCYON("Procyon", new ProcyonDecompiler()),
	FERNFLOWER("FernFlower", new FernflowerDecompiler()), JD_GUI("JD-GUI", new JDDecompiler()),
	CFR("CFR", new CFRDecompiler());

	private final String name;
	private final IDecompiler decompiler;
	private final String syntaxStyle;
	private final boolean isEditable;

	private EnumDecompiler(String name, IDecompiler decompiler) {
		this(name, decompiler, true);
	}

	private EnumDecompiler(String name, IDecompiler decompiler, boolean isDecompiler) {
		this.name = name;
		this.decompiler = decompiler;
		this.syntaxStyle = isDecompiler ? SyntaxConstants.SYNTAX_STYLE_JAVA
				: SyntaxConstants.SYNTAX_STYLE_JAVA_DISASSEMBLE;
		this.isEditable = !isDecompiler;
	}

	public IDecompiler getDecompiler() {
		return decompiler;
	}

	public String getSyntaxStyle() {
		return syntaxStyle;
	}

	public boolean isEditable() {
		return isEditable;
	}

	@Override
	public String toString() {
		return name;
	}
}
