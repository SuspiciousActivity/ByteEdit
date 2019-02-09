package me.ByteEdit.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.io.IOUtils;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import me.ByteEdit.boxes.OptionBox;
import me.ByteEdit.boxes.RenameBox;
import me.ByteEdit.boxes.SearchBox;
import me.ByteEdit.boxes.TypeOpenBox;
import me.ByteEdit.boxes.UnicodeBox;
import me.ByteEdit.edit.Assembler;
import me.ByteEdit.edit.Disassembler;
import me.ByteEdit.edit.Disassembler.DisassembleTuple;
import me.ByteEdit.utils.ClassUtil;
import me.ByteEdit.utils.OpcodesReverse;
import me.ByteEdit.utils.UnicodeUtils;

public class Main extends JFrame {

	public static Main INSTANCE;
	private JPanel contentPane;
	private boolean isChangingFile;
	private static File jarFile;
	public static HashMap<String, byte[]> OTHER_FILES = new HashMap<>();
	public static HashMap<String, ClassNode> classNodes = new HashMap<>();
	public static String currentNodeName;
	public static RSyntaxTextArea txtByteEditView;
	public static SearchBox searchBox;
	public static TypeOpenBox typeOpenBox;
	public static OptionBox optionBox;
	public static UnicodeBox unicodeBox;
	public static RenameBox renameBox;
	public static JTree tree;
	public static RTextScrollPane scrollPane_ByteEdit;
	public static Theme theme;
	public static File saveFolder;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create a simple provider that adds some Java-related completions.
	 */
	private CompletionProvider createCompletionProvider() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		for (int i = 0; i < 200; i++) {
			String s = OpcodesReverse.reverseOpcode(i);
			if (!s.startsWith("Unknown "))
				provider.addCompletion(
						new BasicCompletion(provider, s, null, OpcodesReverse.generateCompletionDesc(s)));
		}
		provider.addCompletion(
				new BasicCompletion(provider, "public", null, OpcodesReverse.generateCompletionDesc("public")));
		provider.addCompletion(
				new BasicCompletion(provider, "private", null, OpcodesReverse.generateCompletionDesc("private")));
		provider.addCompletion(
				new BasicCompletion(provider, "protected", null, OpcodesReverse.generateCompletionDesc("protected")));
		provider.addCompletion(
				new BasicCompletion(provider, "final", null, OpcodesReverse.generateCompletionDesc("final")));
		provider.addCompletion(
				new BasicCompletion(provider, "class", null, OpcodesReverse.generateCompletionDesc("class")));
		provider.addCompletion(
				new BasicCompletion(provider, "enum", null, OpcodesReverse.generateCompletionDesc("enum")));
		provider.addCompletion(
				new BasicCompletion(provider, "static", null, OpcodesReverse.generateCompletionDesc("static")));
		provider.addCompletion(
				new BasicCompletion(provider, "strictfp", null, OpcodesReverse.generateCompletionDesc("strictfp")));
		provider.addCompletion(
				new BasicCompletion(provider, "throws", null, OpcodesReverse.generateCompletionDesc("throws")));
		provider.addCompletion(
				new BasicCompletion(provider, "synthetic", null, OpcodesReverse.generateCompletionDesc("synthetic")));
		provider.addCompletion(
				new BasicCompletion(provider, "bridge", null, OpcodesReverse.generateCompletionDesc("bridge")));
		provider.addCompletion(
				new BasicCompletion(provider, "label", null, OpcodesReverse.generateCompletionDesc("label")));
		provider.addCompletion(
				new BasicCompletion(provider, "line", null, OpcodesReverse.generateCompletionDesc("line")));
		provider.addCompletion(
				new BasicCompletion(provider, "extends", null, OpcodesReverse.generateCompletionDesc("extends")));
		provider.addCompletion(
				new BasicCompletion(provider, "implements", null, OpcodesReverse.generateCompletionDesc("implements")));
		provider.addCompletion(new ShorthandCompletion(provider, "invvi", "invokevirtual desc owner/name", null,
				"<html><b><u>invvi</u></b><br>Creates an example invokevirtual</html>"));
		provider.addCompletion(new ShorthandCompletion(provider, "invif", "invokeinterface desc owner/name", null,
				"<html><b><u>invif</u></b><br>Creates an example invokeinterface</html>"));
		provider.addCompletion(new ShorthandCompletion(provider, "invst", "invokestatic desc owner/name", null,
				"<html><b><u>invst</u></b><br>Creates an example invokestatic</html>"));
		provider.addCompletion(new ShorthandCompletion(provider, "invsp", "invokespecial desc owner/name", null,
				"<html><b><u>invsp</u></b><br>Creates an example invokespecial</html>"));
		provider.addCompletion(new ShorthandCompletion(provider, "sysout",
				"getstatic Ljava/io/PrintStream; java/lang/System/out\n\t\tldc \"text\"\n\t\tinvokevirtual (Ljava/lang/String;)V java/io/PrintStream/println",
				null, "<html><b><u>sysout</u></b><br>Writes a System.out.println(\"text\"); as bytecode</html>"));
		provider.addCompletion(new ShorthandCompletion(provider, "clazz",
				"// #Annotations\n// #Class v:52\n// #Signature: null\n// #OuterClass: null\n// #InnerClasses:\npublic class Main extends java/lang/Object {\n// #SourceFile: Main.java\n\n// #Fields\n\n// #Methods\n\n}\n",
				null, "<html><b><u>clazz</u></b><br>Creates an example class</html>"));
		provider.addCompletion(new ShorthandCompletion(provider, "method",
				"// #Max: l:0 s:0\n\t// #TryCatch:\n\t// #LocalVars:\n\tpublic static method ()V {\n\t\treturn\n\t}",
				null, "<html><b><u>method</u></b><br>Creates an example method</html>"));
		provider.addCompletion(new ShorthandCompletion(provider, "frame", "frame FULL l:[] s:[]", null,
				"<html><b><u>frame</u></b><br>Creates a stack map frame</html>"));
		return provider;
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		INSTANCE = this;
		searchBox = new SearchBox();
		typeOpenBox = new TypeOpenBox();
		optionBox = new OptionBox();
		unicodeBox = new UnicodeBox();
		renameBox = new RenameBox();
		setTitle("ByteEdit");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 407),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 237), 814, 474);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		contentPane = new JPanel();
		contentPane.setBackground(Color.GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		JSplitPane splitPane = new JSplitPane();
		splitPane.setBackground(Color.GRAY);
		splitPane.setResizeWeight(0.2);
		contentPane.add(splitPane);
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		ac.setShowDescWindow(true);
		FoldParserManager.get().addFoldParserMapping(SyntaxConstants.SYNTAX_STYLE_JAVA_DISASSEMBLE,
				new CurlyFoldParser(false, true));
		try {
			Theme theme = Theme.load(getClass().getClassLoader().getResourceAsStream("org/fife/eclipse.xml"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		tree = new JTree(new DefaultTreeModel(null));
		tree.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 116 && jarFile != null) {
					try {
						isChangingFile = true;
						try {
							tree.setModel(new ArchiveTreeModel(new JarFile(jarFile)));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						isChangingFile = false;
					} catch (Throwable t) {
						t.printStackTrace();
						showError(t);
					}
				}
			}
		});
		tree.setFont(new Font("Verdana", Font.PLAIN, 11));
		tree.setBackground(Color.LIGHT_GRAY);
		if (tree.getCellRenderer() instanceof DefaultTreeCellRenderer) {
			final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) (tree.getCellRenderer());
			renderer.setBackgroundNonSelectionColor(Color.LIGHT_GRAY);
			renderer.setBackgroundSelectionColor(Color.GRAY);
			renderer.setTextNonSelectionColor(Color.BLACK);
			renderer.setTextSelectionColor(Color.BLACK);
		}
		scrollPane.setViewportView(tree);
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				if (!isChangingFile) {
					String s = "";
					boolean b = false;
					for (Object o : e.getPath().getPath()) {
						DefaultMutableTreeNode path = (DefaultMutableTreeNode) o;
						if (!b) {
							b = true;
							continue;
						}
						s += (s.isEmpty() || s.replace("/", "").isEmpty() ? "" : "/")
								+ (path.toString().isEmpty() ? "/" : path.toString());
					}
					selectFile(s);
				}
			}
		});
		new DropTarget(tree, new DropTargetListener() {

			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					Transferable tr = dtde.getTransferable();
					DataFlavor[] flavors = tr.getTransferDataFlavors();
					for (int i = 0; i < flavors.length; i++) {
						if (flavors[i].isFlavorJavaFileListType()) {
							dtde.acceptDrop(dtde.getDropAction());
							java.util.List<File> files = (java.util.List<File>) tr.getTransferData(flavors[i]);
							final File file = files.get(0);
							if (file.getName().endsWith(".jar")) {
								jarFile = file;
								saveFolder = file.getParentFile();
								isChangingFile = true;
								try {
									tree.setModel(new ArchiveTreeModel(new JarFile(jarFile)));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								txtByteEditView.setText("");
								currentNodeName = null;
								searchBox.setVisible(false);
								typeOpenBox.setVisible(false);
								optionBox.setVisible(false);
								unicodeBox.setVisible(false);
								renameBox.setVisible(false);
								isChangingFile = false;
							}
							dtde.dropComplete(true);
						}
					}
					return;
				} catch (Throwable t) {
					t.printStackTrace();
					showError(t);
				}
				dtde.rejectDrop();
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
			}
		});

		txtByteEditView = new RSyntaxTextArea();
		ac.install(txtByteEditView);
		txtByteEditView.setCodeFoldingEnabled(true);
		txtByteEditView.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 116 && currentNodeName != null) { // F5
					try {
						ClassNode classNode = classNodes.get(currentNodeName);
						int prev = txtByteEditView.getCaretPosition();
						String dis = Disassembler.disassemble(classNode);
						String substr = currentNodeName.substring(0, currentNodeName.length() - 6);
						for (String key : Main.classNodes.keySet()) {
							if (key.contains("$")) {
								String[] split = key.split("\\$");
								if (split[0].equals(substr)) {
									dis += "\n" + Disassembler.disassemble(classNodes.get(key));
								}
							}
						}
						txtByteEditView.setText(dis);
						txtByteEditView.setCaretPosition(prev);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		});
		KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlT = KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlG = KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlU = KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlR = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
		// global
		txtByteEditView.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				searchBox.setVisible(true);
				searchBox.txtFind.requestFocusInWindow();
				searchBox.txtFind.select(0, searchBox.txtFind.getText().length());
			}
		}, ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtByteEditView.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				typeOpenBox.setVisible(true);
				typeOpenBox.txtSearch.requestFocusInWindow();
				typeOpenBox.txtSearch.select(0, typeOpenBox.txtSearch.getText().length());
			}
		}, ctrlT, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtByteEditView.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				optionBox.setVisible(true);
			}
		}, ctrlO, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtByteEditView.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				unicodeBox.setVisible(true);
			}
		}, ctrlU, JComponent.WHEN_IN_FOCUSED_WINDOW);
		// specific
		tree.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		}, ctrlS, JComponent.WHEN_FOCUSED);
		txtByteEditView.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveCurrentClassNode();
			}
		}, ctrlS, JComponent.WHEN_FOCUSED);
		txtByteEditView.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					goToSelected();
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		}, ctrlG, JComponent.WHEN_FOCUSED);
		txtByteEditView.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					renameSelected();
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		}, ctrlR, JComponent.WHEN_FOCUSED);
		txtByteEditView.setEditable(true);
		txtByteEditView.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA_DISASSEMBLE);
		txtByteEditView.setCodeFoldingEnabled(true);
		scrollPane_ByteEdit = new RTextScrollPane();
		try {
			theme = Theme.load(getClass().getClassLoader().getResourceAsStream("org/fife/eclipse.xml"));
			theme.apply(txtByteEditView);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		splitPane.setRightComponent(scrollPane_ByteEdit);
		txtByteEditView.setBackground(Color.LIGHT_GRAY);
		scrollPane_ByteEdit.setViewportView(txtByteEditView);
		scrollPane_ByteEdit.setLineNumbersEnabled(true);
		scrollPane_ByteEdit.setFoldIndicatorEnabled(true);
		scrollPane_ByteEdit.getGutter().setBackground(Color.LIGHT_GRAY);
	}

	private final Pattern jumpableInstructionPattern = Pattern.compile("^\t\t(?!//|\t+).+ .+");

	public void goToSelected() throws BadLocationException {
		int lineStart = txtByteEditView.getLineStartOffsetOfCurrentLine();
		int lineEnd = txtByteEditView.getLineEndOffsetOfCurrentLine() - 1;
		String line = txtByteEditView.getText(lineStart, lineEnd - lineStart);
		if (!jumpableInstructionPattern.matcher(line).matches()) {
			return;
		}
		line = line.substring(2);
		if (line.startsWith("invoke")) {
			String[] split = line.split(" ");
			String desc = UnicodeUtils.unescape(split[1]);
			String className = UnicodeUtils.unescape(split[2]);
			String[] nameSplit = className.split("/");
			String methodName = nameSplit[nameSplit.length - 1];
			className = className.substring(0, className.length() - methodName.length() - 1);
			ClassNode classNode = classNodes.get(className + ".class");
			if (classNode == null) {
				return;
			}
			for (MethodNode mn : classNode.methods) {
				if (mn.name.equals(methodName) && mn.desc.equals(desc)) {
					while (classNode.outerClass != null) {
						classNode = classNodes.get(classNode.outerClass + ".class");
					}
					int lineFound = selectFileWithSearch(classNode.name + ".class", mn);
					if (lineFound != -1) {
						Main.txtByteEditView.setCaretPosition(Main.txtByteEditView.getLineStartOffset(lineFound));
					}
					break;
				}
			}
		} else if (line.startsWith("new ")) {
			String[] split = line.split(" ");
			String className = UnicodeUtils.unescape(split[1]);
			ClassNode classNode = classNodes.get(className + ".class");
			if (classNode == null) {
				return;
			}
			selectFile(className + ".class");
		} else if (line.startsWith("getstatic ") || line.startsWith("putstatic ") || line.startsWith("getfield ")
				|| line.startsWith("putfield ")) {
			String[] split = line.split(" ");
			String desc = UnicodeUtils.unescape(split[1]);
			String className = UnicodeUtils.unescape(split[2]);
			String[] nameSplit = className.split("/");
			String fieldName = nameSplit[nameSplit.length - 1];
			className = className.substring(0, className.length() - fieldName.length() - 1);
			ClassNode classNode = classNodes.get(className + ".class");
			if (classNode == null) {
				return;
			}
			for (FieldNode fn : classNode.fields) {
				if (fn.name.equals(fieldName) && fn.desc.equals(desc)) {
					while (classNode.outerClass != null) {
						classNode = classNodes.get(classNode.outerClass + ".class");
					}
					int lineFound = selectFileWithSearch(classNode.name + ".class", fn);
					if (lineFound != -1) {
						Main.txtByteEditView.setCaretPosition(Main.txtByteEditView.getLineStartOffset(lineFound));
					}
					break;
				}
			}
		}
	}

	private final Pattern renameableFieldPattern = Pattern
			.compile("^\t(?:[a-z]+ |0x[0-9a-fA-F]+ )*?(\\[*(?:V|Z|C|B|S|I|F|J|D|L.+?;)) ([^ ]+) ?.*");
	private final Pattern renameableMethodPattern = Pattern
			.compile("^\t(?:[a-z]+ |0x[0-9a-fA-F]+ )*?([^ ]+) (\\((?:\\[*(?:V|Z|C|B|S|I|F|J|D|L.+?;))*\\)[^ ]+) .*\\{");

	public void renameSelected() throws BadLocationException {
		int lineStart = txtByteEditView.getLineStartOffsetOfCurrentLine();
		int lineEnd = txtByteEditView.getLineEndOffsetOfCurrentLine() - 1;
		String line = txtByteEditView.getText(lineStart, lineEnd - lineStart);
		Matcher m = renameableFieldPattern.matcher(line);
		if (m.find()) {
			setRenameInfo(m, lineEnd);
		} else {
			m = renameableMethodPattern.matcher(line);
			if (m.find()) {
				if (ClassUtil.isObjectClassMethod(m.group(1), m.group(2), false)) {
					JOptionPane.showMessageDialog(null, "This method can not be renamed!", "Error!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				setRenameInfo(m, lineEnd);
			}
		}
	}

	public void setRenameInfo(Matcher m, int lineEnd) throws BadLocationException {
		String[] arr = txtByteEditView.getText(0, lineEnd).split("\n");
		int lastClassLine = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].startsWith("// #Class")) {
				lastClassLine = i;
			}
		}
		while (lastClassLine < arr.length && arr[lastClassLine].startsWith("// ")) {
			lastClassLine++;
		}
		if (lastClassLine < arr.length) {
			String s = arr[lastClassLine];
			s = s.substring(0, s.length() - 2);
			if (s.contains(" implements "))
				s = s.substring(0, s.lastIndexOf(" implements "));
			if (s.contains(" extends "))
				s = s.substring(0, s.lastIndexOf(" extends "));
			String[] split = s.split(" ");
			String className = UnicodeUtils.unescape(split[split.length - 1]);
			renameBox.className = className;
			String name = m.pattern() == renameableFieldPattern ? m.group(2) : m.group(1);
			String desc = m.pattern() == renameableFieldPattern ? m.group(1) : m.group(2);
			renameBox.name = name;
			renameBox.desc = desc;
			renameBox.txtName.setText(name);
			renameBox.txtDesc.setText(desc);
			renameBox.setVisible(true);
			renameBox.txtName.requestFocusInWindow();
		}
	}

	private void saveCurrentClassNode() {
		String txt = txtByteEditView.getText();
		for (String s : txt.split("\\/\\/ #Annotations\n")) {
			if (s.isEmpty())
				continue;
			ClassNode node = Assembler.assemble(s);
			if (node == null) {
				continue;
			}
			if (classNodes.replace(node.name + ".class", node) == null) {
				classNodes.put(node.name + ".class", node);
			}
		}
	}

	public static int selectFileWithSearch(String s, Object nodeToFind) {
		if (s.endsWith(".class")) {
			currentNodeName = s;
			int lineFound = -1;
			ClassNode classNode = classNodes.get(s);
			String dis = "";
			DisassembleTuple tuple = Disassembler.disassemble(classNode, nodeToFind);
			if (tuple.getLine() != -1) {
				lineFound = dis.split("\\n").length + tuple.getLine() - 1;
			}
			dis += tuple.getDisassembly();
			String substr = s.substring(0, s.length() - 6);
			for (String key : Main.classNodes.keySet()) {
				if (key.contains("$")) {
					String[] split = key.split("\\$");
					if (split[0].equals(substr)) {
						tuple = Disassembler.disassemble(classNodes.get(key), nodeToFind);
						if (tuple.getLine() != -1) {
							lineFound = dis.split("\\n").length + tuple.getLine() + 1;
						}
						dis += "\n" + tuple.getDisassembly();
					}
				}
			}
			txtByteEditView.setText(dis);
			txtByteEditView.setCaretPosition(0);
			return lineFound;
		}
		return -1;
	}

	public static int selectFile(String s) {
		return selectFileWithSearch(s, null);
	}

	public void save() {
		final JFileChooser fileChooser = new JFileChooser(saveFolder) {

			@Override
			protected JDialog createDialog(final Component parent) throws HeadlessException {
				final JDialog dialog = super.createDialog(parent);
				dialog.setAlwaysOnTop(true);
				return dialog;
			}
		};
		fileChooser.setDialogTitle("Save File");
		fileChooser.setFileSelectionMode(0);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Jar File", "jar"));
		final int action = fileChooser.showSaveDialog(Main.this);
		if (action == 0) {
			final File file = fileChooser.getSelectedFile();
			saveFolder = file.getParentFile();
			if (file.exists()) {
				int dialogResult = JOptionPane.showConfirmDialog(null, "This file already exists! Overwrite it?",
						"Warning", JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION) {
					save(file, classNodes.values());
				}
			} else {
				save(file, classNodes.values());
			}
		}
	}

	public void save(File jar, Collection<ClassNode> classes) {
		try {
			final JarOutputStream output = new JarOutputStream(new FileOutputStream(jar));
			boolean refreshTree = false;
			if (tree.getModel().getClass().equals(DefaultTreeModel.class)) {
				refreshTree = true;
				int acc = ClassUtil.ACC_PUBLIC | ClassUtil.ACC_STATIC;
				String name = "";
				for (ClassNode e : classes) {
					for (MethodNode mn : e.methods) {
						if (mn.name.equals("main") && mn.desc.equals("([Ljava/lang/String;)V") && mn.access == acc) {
							name = e.name.replace('/', '.');
							break;
						}
					}
				}
				name = JOptionPane.showInputDialog(INSTANCE,
						"If you want a runnable jar file, please enter the class name.\nIf you only want a library, leave it empty.",
						name);
				if (name != null && !name.isEmpty()) {
					String val = "Manifest-Version: 1.0\n" + "Class-Path: .\n" + "Main-Class: " + name.replace('/', '.')
							+ "\n\n";
					OTHER_FILES.put("META-INF/MANIFEST.MF", val.getBytes());
				}
			}
			for (Entry<String, byte[]> entry : OTHER_FILES.entrySet()) {
				JarEntry ent = new JarEntry(entry.getKey());
				output.putNextEntry(ent);
				output.write(entry.getValue());
				output.closeEntry();
			}
			int computeFlags = 0;
			if (optionBox.chckbxComputeFrames.isSelected()) {
				computeFlags |= ClassWriter.COMPUTE_FRAMES;
			}
			if (optionBox.chckbxComputeMax.isSelected()) {
				computeFlags |= ClassWriter.COMPUTE_MAXS;
			}
			for (ClassNode element : classes) {
				ClassWriter writer = new ClassWriter(computeFlags);
				element.accept(writer);
				output.putNextEntry(new JarEntry(element.name.replaceAll("\\.", "/") + ".class"));
				output.write(writer.toByteArray());
				output.closeEntry();
			}
			output.finish();
			output.close();
			if (refreshTree) {
				isChangingFile = true;
				jarFile = jar;
				try {
					tree.setModel(new ArchiveTreeModel(new JarFile(jarFile)));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				isChangingFile = false;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			showError(e);
		}
	}

	public static void showError(Throwable e) {
		String s = e.toString() + "\n";
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (StackTraceElement ste : stackTrace) {
			if (ste.getClassName().startsWith("java"))
				break;
			s += "\tat " + ste + "\n";
		}
	}

	public static void showError(String s) {
		JOptionPane.showMessageDialog(INSTANCE, s, "Error!", JOptionPane.ERROR_MESSAGE);
	}

	class ArchiveTreeModel extends DefaultTreeModel {

		public ArchiveTreeModel(JarFile jar) {
			super(new DefaultMutableTreeNode(jar.getName().split(File.separator.equals("\\") ? "\\\\"
					: File.separator)[jar.getName().split(File.separator.equals("\\") ? "\\\\" : File.separator).length
							- 1]));
			try {
				ArrayList<String> paths = new ArrayList<>();
				classNodes.clear();
				OTHER_FILES.clear();
				Enumeration<JarEntry> enumeration = jar.entries();
				while (enumeration.hasMoreElements()) {
					JarEntry next = enumeration.nextElement();
					byte[] data = IOUtils.toByteArray(jar.getInputStream(next));
					if (next.getName().endsWith(".class")) {
						if (next.getName().contains("/")
								? (!next.getName().split("/")[next.getName().split("/").length - 1].contains("$"))
								: (!next.getName().contains("$"))) {
							paths.add(next.getName());
						} else if (next.getName().startsWith("$") || next.getName().contains("$$")
								|| next.getName().endsWith("$")) { // obfuscated
																	// with
																	// $
							paths.add(next.getName());
						}
						ClassReader reader;
						try {
							reader = new ClassReader(data);
						} catch (Exception e) {
							OTHER_FILES.put(next.getName(), data);
							continue;
						}
						ClassNode node = new ClassNode();
						reader.accept(node, 0);
						classNodes.put(next.getName(), node);
					} else {
						OTHER_FILES.put(next.getName(), data);
					}
				}
				jar.close();
				Collections.sort(paths);
				for (String s : paths) {
					String[] elements = s.split("/");
					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) getRoot();
					for (int i = 0; i < elements.length; i++) {
						String token = elements[i];
						DefaultMutableTreeNode nextNode = findNode(currentNode, token);
						if (nextNode == null) {
							nextNode = new DefaultMutableTreeNode(token);
							currentNode.add(nextNode);
						}
						currentNode = nextNode;
					}
				}
			} catch (Throwable e) {
				classNodes.clear();
				OTHER_FILES.clear();
				e.printStackTrace();
				showError(e);
			}
		}

		private DefaultMutableTreeNode findNode(DefaultMutableTreeNode parent, String name) {
			Enumeration<?> e = parent.children();
			while (e.hasMoreElements()) {
				DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();
				if (element.getUserObject().equals(name)) {
					return element;
				}
			}
			return null;
		}
	}
}
