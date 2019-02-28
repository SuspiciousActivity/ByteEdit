package me.ByteEdit.boxes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import me.ByteEdit.edit.Disassembler;
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
