package me.ByteEdit.boxes;

import java.awt.BorderLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import me.ByteEdit.main.ThemeManager;

public class CompilationSuccess extends JFrame {

	private JPanel contentPane;
	RSyntaxTextArea textArea = new RSyntaxTextArea();

	public CompilationSuccess() {
		setTitle("Java Compiler - Result");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 400),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 200), 800, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		ThemeManager.registerTextArea(textArea);
		textArea.setEditable(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA_DISASSEMBLE);
		textArea.setCodeFoldingEnabled(true);

		RTextScrollPane scrollPane = new RTextScrollPane();
		scrollPane.setViewportView(textArea);
		scrollPane.setLineNumbersEnabled(true);
		scrollPane.setFoldIndicatorEnabled(true);
		contentPane.add(scrollPane);
	}
}
