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
import org.fife.ui.rtextarea.RTextScrollPane;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Main extends JFrame {

	public static Main INSTANCE;

	private JPanel contentPane;
	private boolean isChangingFile;
	private static File jarFile;
	public static HashMap<String, byte[]> OTHER_FILES = new HashMap<>();
	public static HashMap<String, ClassNode> classNodes = new HashMap<>();
	public static String currentNodeName;

	public static RSyntaxTextArea textArea;
	public static SearchBox searchBox;
	public static OptionBox optionBox;

	public static JTree tree;

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
				provider.addCompletion(new BasicCompletion(provider, s));
		}

		provider.addCompletion(new BasicCompletion(provider, "public"));
		provider.addCompletion(new BasicCompletion(provider, "private"));
		provider.addCompletion(new BasicCompletion(provider, "protected"));
		provider.addCompletion(new BasicCompletion(provider, "final"));
		provider.addCompletion(new BasicCompletion(provider, "class"));
		provider.addCompletion(new BasicCompletion(provider, "enum"));
		provider.addCompletion(new BasicCompletion(provider, "static"));
		provider.addCompletion(new BasicCompletion(provider, "strictfp"));
		provider.addCompletion(new BasicCompletion(provider, "throws"));
		provider.addCompletion(new BasicCompletion(provider, "synthetic"));
		provider.addCompletion(new BasicCompletion(provider, "bridge"));
		provider.addCompletion(new BasicCompletion(provider, "label"));
		provider.addCompletion(new BasicCompletion(provider, "line"));
		provider.addCompletion(new BasicCompletion(provider, "extends"));
		provider.addCompletion(new BasicCompletion(provider, "implements"));

		provider.addCompletion(new ShorthandCompletion(provider, "invvi", "invokevirtual desc owner/name"));
		provider.addCompletion(new ShorthandCompletion(provider, "invif", "invokeinterface desc owner/name"));
		provider.addCompletion(new ShorthandCompletion(provider, "invst", "invokestatic desc owner/name"));
		provider.addCompletion(new ShorthandCompletion(provider, "invsp", "invokespecial desc owner/name"));
		provider.addCompletion(new ShorthandCompletion(provider, "sysout",
				"getstatic Ljava/io/PrintStream; java/lang/System/out\n\t\tldc \"text\"\n\t\tinvokevirtual (Ljava/lang/String;)V java/io/PrintStream/println"));
		provider.addCompletion(new ShorthandCompletion(provider, "clazz",
				"// #Annotations\n// #Class v:52\n// #Signature: null\n// #OuterClass: null\n// #InnerClasses:\npublic class Main extends java/lang/Object {\n// #SourceFile: Main.java\n\n// #Fields\n\n// #Methods\n\n}\n"));
		provider.addCompletion(new ShorthandCompletion(provider, "method",
				"// #Max: l:0 s:0\n\t// #TryCatch:\n\t// #LocalVars:\n\tpublic static method ()V {\n\t\treturn\n\t}"));

		return provider;

	}

	/**
	 * Create the frame.
	 */
	public Main() {
		INSTANCE = this;
		searchBox = new SearchBox();
		optionBox = new OptionBox();
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

		textArea = new RSyntaxTextArea();

		CompletionProvider provider = createCompletionProvider();

		AutoCompletion ac = new AutoCompletion(provider);
		ac.install(textArea);

		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 116 && currentNodeName != null) {
					try {
						ClassNode classNode = classNodes.get(currentNodeName);
						int prev = textArea.getCaretPosition();
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
						textArea.setText(dis);
						textArea.setCaretPosition(prev);
					} catch (Exception e2) {
					}
				}
			}
		});
		KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
		textArea.registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchBox.setVisible(true);
				searchBox.txtFind.requestFocusInWindow();
				searchBox.txtFind.select(0, searchBox.txtFind.getText().length());
			}
		}, ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
		textArea.registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String txt = textArea.getText();
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
		}, ctrlS, JComponent.WHEN_FOCUSED);
		KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
		textArea.registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optionBox.setVisible(true);
			}
		}, ctrlO, JComponent.WHEN_IN_FOCUSED_WINDOW);
		textArea.setEditable(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA_DISASSEMBLE);
		textArea.setCodeFoldingEnabled(true);
		try {
			Theme theme = Theme.load(getClass().getClassLoader().getResourceAsStream("org/fife/eclipse.xml"));
			theme.apply(textArea);
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
					}
				}
			}
		});
		tree.registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		}, ctrlS, JComponent.WHEN_FOCUSED);
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
					if (s.endsWith(".class")) {
						currentNodeName = s;
						ClassNode classNode = classNodes.get(s);
						String dis = Disassembler.disassemble(classNode);
						String substr = s.substring(0, s.length() - 6);
						for (String key : Main.classNodes.keySet()) {
							if (key.contains("$")) {
								String[] split = key.split("\\$");
								if (split[0].equals(substr)) {
									dis += "\n" + Disassembler.disassemble(classNodes.get(key));
								}
							}
						}
						textArea.setText(dis);
						textArea.setCaretPosition(0);
					}
				}
			}
		});

		RTextScrollPane scrollPane_1 = new RTextScrollPane();
		scrollPane_1.setLineNumbersEnabled(true);
		scrollPane_1.setFoldIndicatorEnabled(true);
		textArea.setBackground(Color.LIGHT_GRAY);
		scrollPane_1.setViewportView(textArea);
		splitPane.setRightComponent(scrollPane_1);
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
								isChangingFile = true;
								try {
									tree.setModel(new ArchiveTreeModel(new JarFile(jarFile)));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								isChangingFile = false;
							}

							dtde.dropComplete(true);
						}
					}
					return;
				} catch (Throwable t) {
					t.printStackTrace();
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
	}

	public void save() {
		final JFileChooser fileChooser = new JFileChooser() {
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
							name = e.name.replace("/", ".");
							break;
						}
					}
				}
				String val = "Manifest-Version: 1.0\n" + "Class-Path: .\n" + "Main-Class: " + name + "\n\n";
				OTHER_FILES.put("META-INF/MANIFEST.MF", val.getBytes());
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
		} catch (IOException e) {
			e.printStackTrace();
		}
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
								|| next.getName().endsWith("$")) { // obfuscated with $
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
			} catch (IOException e) {
				classNodes.clear();
				OTHER_FILES.clear();
				e.printStackTrace();
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