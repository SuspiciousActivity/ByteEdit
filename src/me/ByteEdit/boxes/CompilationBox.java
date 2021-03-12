package me.ByteEdit.boxes;

import java.awt.BorderLayout;
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
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import me.ByteEdit.decompiler.EnumDecompiler;
import me.ByteEdit.decompiler.SingleThreadedExecutor;
import me.ByteEdit.main.Main;
import me.ByteEdit.main.ThemeManager;

public class CompilationBox extends JFrame {

	private JPanel contentPane;
	private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	RSyntaxTextArea textArea = new RSyntaxTextArea();
	CompilationSuccess compSuccess;

	public CompilationBox() {
		compSuccess = new CompilationSuccess();
		ThemeManager.registerFrames(compSuccess);
		setTitle("Java Compiler");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 400),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 200), 800, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		ThemeManager.registerTextArea(textArea);
		textArea.setEditable(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);

		RTextScrollPane scrollPane = new RTextScrollPane();
		scrollPane.setViewportView(textArea);
		scrollPane.setLineNumbersEnabled(true);
		scrollPane.setFoldIndicatorEnabled(true);
		contentPane.add(scrollPane);

		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
		textArea.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (compiler == null) {
					Main.showError("No Java Compiler found!\nYou need JDK installed!");
					return;
				}
				try (StringWriter out = new StringWriter(); PrintWriter outWriter = new PrintWriter(out)) {
					File tmpFolder = Files.createTempDirectory("compiled").toFile();

					String source = textArea.getText();
					if (!source.contains("\npublic class Compiled {\n")) {
						int importIndex = source.lastIndexOf("\nimport");
						if (importIndex == -1) {
							if (source.startsWith("import")) {
								importIndex = 0;
							}
						} else {
							importIndex++;
						}
						if (importIndex != -1) {
							int newLineIndex = source.indexOf('\n', importIndex);
							source = source.substring(0, newLineIndex) + "\npublic class Compiled {\n"
									+ source.substring(newLineIndex) + "\n}";
						} else {
							source = "public class Compiled {\n" + source + "\n}";
						}
					}

					compiler.getTask(outWriter, null, null,
							Main.jarFile != null
									? Arrays.asList("-d", tmpFolder.getAbsolutePath(), "-classpath",
											Main.jarFile.getAbsolutePath())
									: Arrays.asList("-d", tmpFolder.getAbsolutePath()),
							null, Arrays.asList(new JavaSourceFromString("Compiled", source))).call();
					String res = out.toString();
					if (res.isEmpty()) {
						SingleThreadedExecutor.submit(() -> {
							try {
								File clazz = new File(tmpFolder, "Compiled.class");
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								InputStream is = new FileInputStream(clazz);
								byte[] tmp = new byte[1024];
								int r;
								while ((r = is.read(tmp)) > 0) {
									baos.write(tmp, 0, r);
								}
								is.close();

								ClassReader read = new ClassReader(baos.toByteArray());
								ClassNode node = new ClassNode();
								read.accept(node, 0);
								EnumDecompiler decompiler = EnumDecompiler.BYTEEDIT;
								String dis = decompiler.getDecompiler().decompile(node);
								File[] files = tmpFolder.listFiles();
								if (files != null)
									for (File f : files) {
										if (f.getName().equals("Compiled.class"))
											continue;
										baos = new ByteArrayOutputStream();
										is = new FileInputStream(f);
										while ((r = is.read(tmp)) > 0) {
											baos.write(tmp, 0, r);
										}
										is.close();
										read = new ClassReader(baos.toByteArray());
										node = new ClassNode();
										read.accept(node, 0);
										dis += "\n" + decompiler.getDecompiler().decompile(node);
									}
								compSuccess.textArea.setText(dis);
								compSuccess.setVisible(true);
							} catch (IOException e2) {
								e2.printStackTrace();
							}
						});
					} else {
						JOptionPane.showMessageDialog(CompilationBox.this, res, "Error!", JOptionPane.ERROR_MESSAGE);
					}
					for (File f : tmpFolder.listFiles()) {
						Files.delete(f.toPath());
					}
					Files.delete(tmpFolder.toPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}, ctrlS, JComponent.WHEN_FOCUSED);
	}

	private static class JavaSourceFromString extends SimpleJavaFileObject {
		final String code;

		public JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}
}
