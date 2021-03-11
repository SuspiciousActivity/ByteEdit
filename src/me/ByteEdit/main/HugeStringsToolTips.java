package me.ByteEdit.main;

import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.ToolTipSupplier;

import me.ByteEdit.edit.Assembler;

public class HugeStringsToolTips implements ToolTipSupplier {

	private final RSyntaxTextArea txtByteEditView;

	public HugeStringsToolTips(RSyntaxTextArea txtByteEditView) {
		this.txtByteEditView = txtByteEditView;
	}

	@Override
	public String getToolTipText(RTextArea textArea, MouseEvent e) {
		int idx = textArea.viewToModel(e.getPoint());
		if (idx < 0)
			return null;
		int subIdx = Math.max(idx - 15, 0);
		int subLen = Math.min(30, textArea.getDocument().getLength() - subIdx);
		if (subLen <= 0)
			return null;
		try {
			String cut = textArea.getText(subIdx, subLen);
			int hashIdx = -1;
			for (int off = idx - subIdx; off >= 0; off--) {
				if (cut.charAt(off) == '#') {
					hashIdx = off;
					break;
				}
			}
			if (hashIdx < 0) {
				for (int off = idx - subIdx; off < cut.length(); off++) {
					if (cut.charAt(off) == '#') {
						hashIdx = off;
						break;
					}
				}
			}
			if (hashIdx < 0)
				return null;
			if (hashIdx >= 5 && cut.substring(hashIdx - 5, hashIdx).equals("ldc \"")) // ldc "#..."
				return null;
			int numLen = 1;
			for (int i = hashIdx + 1; i < cut.length(); i++) {
				if (!(cut.charAt(i) - '0' >= 0 && cut.charAt(i) - '0' <= 9)) {
					break;
				}
				numLen++;
			}
			if (numLen < 2 || hashIdx > idx - subIdx || idx - subIdx - numLen >= hashIdx)
				return null;
			String hashNum = cut.substring(hashIdx, hashIdx + numLen);

			String txt = txtByteEditView.getText();
			int len = 0;
			for (String asm : txt.split("\\/\\/ #Annotations:\n")) {
				len += 19; // "\\/\\/ #Annotations:\n".length()
				if (asm.isEmpty())
					continue;
				if (idx >= len && idx < len + asm.length()) {
					try (BufferedReader reader = new BufferedReader(new StringReader(asm))) {
						String s;
						while (!(s = reader.readLine()).startsWith("// #SourceFile: ")) {
						}
						while (!(s = reader.readLine()).equals("// #Fields")) {
							if (s.startsWith("#")) {
								String[] split = Assembler.SPACE.split(s, 2);
								if (hashNum.equals(split[0].substring(0, split[0].length() - 1))) {
									String toolTip = split[1];
									if (toolTip.length() > 500)
										toolTip = toolTip.substring(0, 500) + "...";
									return toolTip;
								}
							}
						}
					} catch (IOException e1) {
					}
					break;
				}
				len += asm.length();
			}
		} catch (Exception e1) {
		}
		return null;
	}

}
