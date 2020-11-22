package me.ByteEdit.boxes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import me.ByteEdit.main.Main;

public class CompilationSuccess extends JFrame {

	private JPanel contentPane;
	RSyntaxTextArea textArea = new RSyntaxTextArea();

	public CompilationSuccess() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Java Compiler - Result");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 400),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 200), 800, 400);
		contentPane = new JPanel();
		contentPane.setBackground(Main.dark ? new Color(0x2F2F2F) : Color.GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		Main.theme.apply(textArea);
		textArea.setEditable(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA_DISASSEMBLE);
		textArea.setCodeFoldingEnabled(true);
		textArea.setBackground(Main.dark ? new Color(0x2F2F2F) : Color.LIGHT_GRAY);

		RTextScrollPane scrollPane = new RTextScrollPane();
		scrollPane.setViewportView(textArea);
		scrollPane.setLineNumbersEnabled(true);
		scrollPane.setFoldIndicatorEnabled(true);
		scrollPane.getGutter().setBackground(Main.dark ? new Color(0x2F2F2F) : Color.LIGHT_GRAY);
		contentPane.add(scrollPane);
	}
}
