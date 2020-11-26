package me.ByteEdit.main;

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
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.ToolTipSupplier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import me.ByteEdit.boxes.CompilationBox;
import me.ByteEdit.boxes.GlobalSearchBox;
import me.ByteEdit.boxes.RenameBox;
import me.ByteEdit.boxes.SearchBox;
import me.ByteEdit.boxes.ThemeBox;
import me.ByteEdit.boxes.TypeOpenBox;
import me.ByteEdit.boxes.UnicodeBox;
import me.ByteEdit.edit.Assembler;
import me.ByteEdit.edit.Disassembler;
import me.ByteEdit.edit.Disassembler.DisassembleTuple;
import me.ByteEdit.edit.HugeStrings;
import me.ByteEdit.utils.ClassUtil;
import me.ByteEdit.utils.OpcodesReverse;
import me.ByteEdit.utils.UnicodeUtils;

public class Main extends JFrame {

	public static Main INSTANCE;
	private JPanel contentPane;
	private volatile boolean isChangingFile;
	public static File jarFile;
	public static HashMap<String, byte[]> OTHER_FILES = new HashMap<>();
	public static HashMap<String, ClassNode> classNodes = new HashMap<>();
	public static String currentNodeName;
	public static RSyntaxTextArea txtByteEditView;
	public static GlobalSearchBox globalSearchBox;
	public static SearchBox searchBox;
	public static TypeOpenBox typeOpenBox;
	public static UnicodeBox unicodeBox;
	public static RenameBox renameBox;
	public static CompilationBox compilationBox;
	public static ThemeBox themeBox;
	public static JTree tree;
	public static RTextScrollPane scrollPane_ByteEdit;
	public static File saveFolder;

	private static final Pattern SLASH = Pattern.compile("/");
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOpenJar;
	private JMenuItem mntmSaveBytecode;
	private JMenuItem mntmSaveJar;
	private JMenu mnWindow;
	private JMenuItem mntmThemes;
	private JMenuItem mntmSearchMembers;
	private JMenuItem mntmSearch;
	private JMenuItem mntmOpenType;
	private JMenuItem mntmUnicode;
	private JMenuItem mntmCompiler;
	private JMenu mnSearch;
	private JMenu mnOptions;
	private JCheckBoxMenuItem mntmComputeFrames;
	private JCheckBoxMenuItem mntmComputeMax;
	private JMenu mnStringLimit;
	private JRadioButtonMenuItem rdbtnmntm0;
	private JMenuItem rdbtnmntm100;
	private JMenuItem rdbtnmntm1000;
	private JMenuItem rdbtnmntm500;
	private JMenuItem rdbtnmntm5000;
	private JMenuItem rdbtnmntm10000;
	public JCheckBoxMenuItem mntmMultithreaded;
	private JMenu mnDeobfuscation;
	public JCheckBoxMenuItem mntmNumbers;

	public static final Object treeLock = new Object();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
					ThemeManager.applyRSTATheme();
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
				"// #Annotations:\n// #Class v:52\n// #Signature: null\n// #OuterClass: null\n// #InnerClasses:\npublic class Main extends java/lang/Object {\n// #SourceFile: Main.java\n\n// #Fields\n\n// #Methods\n\n}\n",
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
		ThemeManager.registerFrames(this);
		globalSearchBox = new GlobalSearchBox();
		searchBox = new SearchBox();
		typeOpenBox = new TypeOpenBox();
		unicodeBox = new UnicodeBox();
		renameBox = new RenameBox();
		compilationBox = new CompilationBox();
		themeBox = new ThemeBox();
		ThemeManager.registerFrames(globalSearchBox);
		ThemeManager.registerFrames(searchBox);
		ThemeManager.registerFrames(typeOpenBox);
		ThemeManager.registerFrames(unicodeBox);
		ThemeManager.registerFrames(renameBox);
		ThemeManager.registerFrames(compilationBox);
		ThemeManager.registerFrames(themeBox);
		ThemeManager.load();
		setTitle("ByteEdit");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 407),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 237), 814, 474);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOpenJar = new JMenuItem("Open Jar");
		mntmOpenJar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fileChooser = new JFileChooser(saveFolder) {

					@Override
					protected JDialog createDialog(final Component parent) throws HeadlessException {
						final JDialog dialog = super.createDialog(parent);
						dialog.setAlwaysOnTop(true);
						return dialog;
					}
				};
				fileChooser.setDialogTitle("Open File");
				fileChooser.setFileSelectionMode(0);
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Jar File", "jar"));
				final int action = fileChooser.showOpenDialog(Main.this);
				if (action == 0) {
					final File file = fileChooser.getSelectedFile();
					saveFolder = file.getParentFile();
					boolean shouldSave = false;
					if (file.exists()) {
						synchronized (treeLock) {
							jarFile = file;
							saveFolder = file.getParentFile();
							isChangingFile = true;
							try {
								Main.this.setTitle("ByteEdit - Loading '" + jarFile.getCanonicalPath() + "'");
							} catch (IOException ex) {
								Main.this.setTitle("ByteEdit - Loading '" + jarFile.getAbsolutePath() + "'");
							}
							new Thread(new Runnable() {
								public void run() {
									try {
										ArchiveTreeModel model = new ArchiveTreeModel(new JarFile(jarFile));
										EventQueue.invokeLater(new Runnable() {
											public void run() {
												synchronized (treeLock) {
													tree.setModel(model);
													txtByteEditView.setText("");
													currentNodeName = null;
													globalSearchBox.setVisible(false);
													searchBox.setVisible(false);
													typeOpenBox.setVisible(false);
													unicodeBox.setVisible(false);
													renameBox.setVisible(false);
													compilationBox.setVisible(false);
													isChangingFile = false;
													Main.this.setTitle("ByteEdit");
												}
											}
										});
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}).start();
						}
					}
				}
			}
		});
		mnFile.add(mntmOpenJar);

		mntmSaveBytecode = new JMenuItem("Save Bytecode");
		mntmSaveBytecode.addActionListener(e -> saveCurrentClassNode());
		mnFile.add(mntmSaveBytecode);

		mntmSaveJar = new JMenuItem("Save Jar");
		mntmSaveJar.addActionListener(e -> save());
		mnFile.add(mntmSaveJar);

		mnSearch = new JMenu("Search");
		menuBar.add(mnSearch);

		mntmSearchMembers = new JMenuItem("Search Members");
		mnSearch.add(mntmSearchMembers);

		mntmSearch = new JMenuItem("Search");
		mnSearch.add(mntmSearch);
		mntmSearch.addActionListener(e -> searchBox.setVisible(true));
		mntmSearchMembers.addActionListener(e -> globalSearchBox.setVisible(true));

		mnWindow = new JMenu("Window");
		menuBar.add(mnWindow);

		mntmThemes = new JMenuItem("Themes");
		mntmThemes.addActionListener(e -> themeBox.setVisible(true));
		mnWindow.add(mntmThemes);

		mntmOpenType = new JMenuItem("Open Type");
		mntmOpenType.addActionListener(e -> typeOpenBox.setVisible(true));
		mnWindow.add(mntmOpenType);

		mntmUnicode = new JMenuItem("Unicode Helper");
		mntmUnicode.addActionListener(e -> unicodeBox.setVisible(true));
		mnWindow.add(mntmUnicode);

		mntmCompiler = new JMenuItem("Compiler");
		mntmCompiler.addActionListener(e -> compilationBox.setVisible(true));
		mnWindow.add(mntmCompiler);

		mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);

		mntmComputeFrames = new JCheckBoxMenuItem("Compute Frames");
		mnOptions.add(mntmComputeFrames);

		mntmComputeMax = new JCheckBoxMenuItem("Compute Max");
		mnOptions.add(mntmComputeMax);

		mntmMultithreaded = new JCheckBoxMenuItem("Multithreaded");
		mntmMultithreaded.setSelected(true);
		mnOptions.add(mntmMultithreaded);

		mnStringLimit = new JMenu("Huge String Limit");
		mnOptions.add(mnStringLimit);

		rdbtnmntm0 = new JRadioButtonMenuItem("0");
		rdbtnmntm0.addActionListener(e -> HugeStrings.THRESHOLD = 0);
		mnStringLimit.add(rdbtnmntm0);

		rdbtnmntm100 = new JRadioButtonMenuItem("100");
		rdbtnmntm100.addActionListener(e -> HugeStrings.THRESHOLD = 100);
		mnStringLimit.add(rdbtnmntm100);

		rdbtnmntm500 = new JRadioButtonMenuItem("500");
		rdbtnmntm500.addActionListener(e -> HugeStrings.THRESHOLD = 500);
		mnStringLimit.add(rdbtnmntm500);

		rdbtnmntm1000 = new JRadioButtonMenuItem("1000");
		rdbtnmntm1000.addActionListener(e -> HugeStrings.THRESHOLD = 1000);
		rdbtnmntm1000.setSelected(true);
		mnStringLimit.add(rdbtnmntm1000);

		rdbtnmntm5000 = new JRadioButtonMenuItem("5000");
		rdbtnmntm5000.addActionListener(e -> HugeStrings.THRESHOLD = 5000);
		mnStringLimit.add(rdbtnmntm5000);

		rdbtnmntm10000 = new JRadioButtonMenuItem("10000");
		rdbtnmntm10000.addActionListener(e -> HugeStrings.THRESHOLD = 10000);
		mnStringLimit.add(rdbtnmntm10000);

		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnmntm0);
		group.add(rdbtnmntm100);
		group.add(rdbtnmntm500);
		group.add(rdbtnmntm1000);
		group.add(rdbtnmntm5000);
		group.add(rdbtnmntm10000);
		
		mnDeobfuscation = new JMenu("Deobfuscation");
		menuBar.add(mnDeobfuscation);
		
		mntmNumbers = new JCheckBoxMenuItem("Numbers");
		mnDeobfuscation.add(mntmNumbers);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.2);
		contentPane.add(splitPane);
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		ac.setShowDescWindow(true);
		FoldParserManager.get().addFoldParserMapping(SyntaxConstants.SYNTAX_STYLE_JAVA_DISASSEMBLE,
				new CurlyFoldParser(false, true));
		tree = new JTree(new DefaultTreeModel(null));
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeExpansionListener(new FurtherExpandingTreeExpansionListener());
		tree.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 116 && jarFile != null) { // F5
					synchronized (treeLock) {
						if (isChangingFile) {
							return;
						}
						try {
							isChangingFile = true;
							Main.this.setTitle("ByteEdit - Reloading...");
							new Thread(new Runnable() {
								public void run() {
									try {
										ArchiveTreeModel model = new ArchiveTreeModel(new JarFile(jarFile));
										EventQueue.invokeLater(new Runnable() {
											public void run() {
												synchronized (treeLock) {
													tree.setModel(model);
													isChangingFile = false;
													Main.this.setTitle("ByteEdit");
												}
											}
										});
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}).start();
						} catch (Throwable t) {
							t.printStackTrace();
							showError(t);
						}
					}
				} else if (e.getKeyCode() == 127) { // del
					if (tree.getSelectionPath() == null)
						return;
					String s = ((ByteEditTreeNode) tree.getSelectionPath().getLastPathComponent()).path;
					if (JOptionPane.showConfirmDialog(INSTANCE, "Do you want to delete\n\"" + s + "\"?", "Delete",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						String substr = s.substring(0, s.length() - 6);
						ArrayList<String> toRemove = new ArrayList<>();
						for (String key : Main.classNodes.keySet()) {
							if (key.contains("$")) {
								String[] split = key.split("\\$");
								if (split[0].equals(substr)) {
									toRemove.add(key);
								}
							}
						}
						ArchiveTreeModel model = (ArchiveTreeModel) tree.getModel();
						classNodes.remove(s);
						model.paths.remove(s);
						for (String rem : toRemove) {
							classNodes.remove(rem);
						}
						model.refresh();
						model.reload();
					}
				}
			}
		});
		tree.setFont(new Font("Verdana", Font.PLAIN, 11));
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			{
				putClientProperty("html.disable", Boolean.TRUE);
			}
		});
		scrollPane.setViewportView(tree);
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				synchronized (treeLock) {
					if (!isChangingFile) {
						selectFile(((ByteEditTreeNode) e.getPath().getLastPathComponent()).path);
					}
				}
			}
		});
		new DropTarget(tree, new DropTargetListener() {

			@Override
			public void drop(DropTargetDropEvent dtde) {
				synchronized (treeLock) {
					if (isChangingFile) {
						dtde.rejectDrop();
						return;
					}
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
										Main.this.setTitle("ByteEdit - Loading '" + jarFile.getCanonicalPath() + "'");
									} catch (IOException ex) {
										Main.this.setTitle("ByteEdit - Loading '" + jarFile.getAbsolutePath() + "'");
									}
									new Thread(new Runnable() {
										public void run() {
											try {
												ArchiveTreeModel model = new ArchiveTreeModel(new JarFile(jarFile));
												EventQueue.invokeLater(new Runnable() {
													public void run() {
														synchronized (treeLock) {
															tree.setModel(model);
															txtByteEditView.setText("");
															currentNodeName = null;
															globalSearchBox.setVisible(false);
															searchBox.setVisible(false);
															typeOpenBox.setVisible(false);
															unicodeBox.setVisible(false);
															renameBox.setVisible(false);
															compilationBox.setVisible(false);
															isChangingFile = false;
															Main.this.setTitle("ByteEdit");
														}
													}
												});
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
									}).start();
								}
								dtde.dropComplete(true);
								break;
							}
						}
						return;
					} catch (Throwable t) {
						t.printStackTrace();
						showError(t);
					}
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
		// ToolTips for HugeStrings
		txtByteEditView.setToolTipSupplier(new ToolTipSupplier() {

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
						} else {
							len += asm.length();
						}
					}
				} catch (BadLocationException e1) {
				}
				return null;
			}
		});
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
						if (dis.length() > prev)
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
		KeyStroke ctrlU = KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlR = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlE = KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
		// global
		txtByteEditView.registerKeyboardAction(e -> themeBox.setVisible(true), ctrlB,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtByteEditView.registerKeyboardAction(e -> {
			globalSearchBox.setVisible(true);
			globalSearchBox.txtString.requestFocusInWindow();
			globalSearchBox.txtString.select(0, globalSearchBox.txtString.getText().length());
		}, ctrlG, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtByteEditView.registerKeyboardAction(e -> {
			searchBox.setVisible(true);
			searchBox.txtFind.requestFocusInWindow();
			searchBox.txtFind.select(0, searchBox.txtFind.getText().length());
		}, ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtByteEditView.registerKeyboardAction(e -> {
			typeOpenBox.setVisible(true);
			typeOpenBox.txtSearch.requestFocusInWindow();
			typeOpenBox.txtSearch.select(0, typeOpenBox.txtSearch.getText().length());
		}, ctrlT, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtByteEditView.registerKeyboardAction(e -> unicodeBox.setVisible(true), ctrlU,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtByteEditView.registerKeyboardAction(e -> compilationBox.setVisible(true), ctrlE,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		// specific
		tree.registerKeyboardAction(e -> save(), ctrlS, JComponent.WHEN_FOCUSED);
		txtByteEditView.registerKeyboardAction(e -> saveCurrentClassNode(), ctrlS, JComponent.WHEN_FOCUSED);
		txtByteEditView.registerKeyboardAction(e -> {
			try {
				goToSelected();
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}, ctrlG, JComponent.WHEN_FOCUSED);
		txtByteEditView.registerKeyboardAction(e -> {
			try {
				renameSelected();
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}, ctrlR, JComponent.WHEN_FOCUSED);
		txtByteEditView.setEditable(true);
		txtByteEditView.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA_DISASSEMBLE);
		txtByteEditView.setCodeFoldingEnabled(true);
		scrollPane_ByteEdit = new RTextScrollPane();
		ThemeManager.registerTextArea(txtByteEditView);

		splitPane.setRightComponent(scrollPane_ByteEdit);
		scrollPane_ByteEdit.setViewportView(txtByteEditView);
		scrollPane_ByteEdit.setLineNumbersEnabled(true);
		scrollPane_ByteEdit.setFoldIndicatorEnabled(true);
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
			String desc = UnicodeUtils.unescape(null, split[1], true);
			String target = split[2];
			int index = target.lastIndexOf("/");
			String className = UnicodeUtils.unescape(null, target.substring(0, index), true);
			String methodName = UnicodeUtils.unescape(null, target.substring(index + 1), true);
			ClassNode classNode = classNodes.get(getFullName(className));
			if (classNode == null) {
				return;
			}
			for (MethodNode mn : classNode.methods) {
				if (mn.name.equals(methodName) && mn.desc.equals(desc)) {
					while (classNode.outerClass != null) {
						classNode = classNodes.get(getFullName(classNode.outerClass));
					}
					int lineFound = selectFileWithSearch(getFullName(classNode.name), mn);
					if (lineFound != -1) {
						Main.txtByteEditView.setCaretPosition(Main.txtByteEditView.getLineStartOffset(lineFound));
					}
					break;
				}
			}
		} else if (line.startsWith("new ")) {
			String[] split = line.split(" ");
			String className = UnicodeUtils.unescape(null, split[1], true);
			ClassNode classNode = classNodes.get(getFullName(className));
			if (classNode == null) {
				return;
			}
			selectFile(getFullName(className));
		} else if (line.startsWith("getstatic ") || line.startsWith("putstatic ") || line.startsWith("getfield ")
				|| line.startsWith("putfield ")) {
			String[] split = line.split(" ");
			String desc = UnicodeUtils.unescape(null, split[1], true);
			String target = split[2];
			int index = target.lastIndexOf("/");
			String className = UnicodeUtils.unescape(null, target.substring(0, index), true);
			String fieldName = UnicodeUtils.unescape(null, target.substring(index + 1), true);
			ClassNode classNode = classNodes.get(getFullName(className));
			if (classNode == null) {
				return;
			}
			for (FieldNode fn : classNode.fields) {
				if (fn.name.equals(fieldName) && fn.desc.equals(desc)) {
					while (classNode.outerClass != null) {
						classNode = classNodes.get(getFullName(classNode.outerClass));
					}
					int lineFound = selectFileWithSearch(getFullName(classNode.name), fn);
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
			String className = UnicodeUtils.unescape(null, split[split.length - 1], true);
			renameBox.className = className;
			String name = UnicodeUtils.unescape(null, m.pattern() == renameableFieldPattern ? m.group(2) : m.group(1),
					true);
			String desc = UnicodeUtils.unescape(null, m.pattern() == renameableFieldPattern ? m.group(1) : m.group(2),
					true);
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
		for (String s : txt.split("\\/\\/ #Annotations:\n")) {
			if (s.isEmpty())
				continue;
			ClassNode node = Assembler.assemble(s);
			if (node == null) {
				continue;
			}
			if (classNodes.replace(getFullName(node.name), node) == null) {
				classNodes.put(getFullName(node.name), node);
				ArchiveTreeModel model;
				if (tree.getModel().getClass().equals(DefaultTreeModel.class)) {
					tree.setModel(model = new ArchiveTreeModel());
				} else {
					model = (ArchiveTreeModel) tree.getModel();
				}
				String name = getFullName(node.name);
				if (name.contains("/") ? (!name.split("/")[name.split("/").length - 1].contains("$"))
						: (!name.contains("$"))) {
					model.paths.add(name);
				} else if (name.startsWith("$") || name.contains("$$") || name.endsWith("$")) { // obfuscated
					model.paths.add(name);
				}
				model.refresh();
				model.reload();
			}
		}
	}

	public static int selectFileWithSearch(String s, Object nodeToFind) {
		if (s != null && s.endsWith(".class")) {
			currentNodeName = s;
			int lineFound = -1;
			ClassNode classNode = classNodes.get(s);
			if (classNode == null)
				return -1;
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
			boolean shouldSave = false;
			if (file.exists()) {
				int dialogResult = JOptionPane.showConfirmDialog(null, "This file already exists! Overwrite it?",
						"Warning", JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION) {
					save(file, classNodes.values());
					shouldSave = true;
				}
			} else {
				shouldSave = true;
			}

			if (shouldSave)
				synchronized (treeLock) {
					if (isChangingFile) {
						return;
					}
					try {
						isChangingFile = true;
						try {
							Main.this.setTitle("ByteEdit - Saving '" + file.getCanonicalPath() + "'");
						} catch (IOException ex) {
							Main.this.setTitle("ByteEdit - Saving '" + file.getAbsolutePath() + "'");
						}
						new Thread(new Runnable() {
							public void run() {
								synchronized (treeLock) {
									save(file, classNodes.values());
									EventQueue.invokeLater(new Runnable() {
										public void run() {
											synchronized (treeLock) {
												isChangingFile = false;
												Main.this.setTitle("ByteEdit");
											}
										}
									});
								}
							}
						}).start();
					} catch (Throwable t) {
						t.printStackTrace();
						showError(t);
					}
				}
		}
	}

	public void save(File jar, Collection<ClassNode> classes) {
		try {
			final JarOutputStream output = new JarOutputStream(new FileOutputStream(jar));
			if (tree.getModel().getClass().equals(DefaultTreeModel.class)
					|| ((ArchiveTreeModel) tree.getModel()).newCreated) {
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
				if (tree.getModel() instanceof ArchiveTreeModel)
					((ArchiveTreeModel) tree.getModel()).newCreated = false;
			}
			for (Entry<String, byte[]> entry : OTHER_FILES.entrySet()) {
				JarEntry ent = new JarEntry(entry.getKey());
				output.putNextEntry(ent);
				output.write(entry.getValue());
				output.closeEntry();
			}
			int computeFlags = 0;
			if (mntmComputeFrames.isSelected()) {
				computeFlags |= ClassWriter.COMPUTE_FRAMES;
			}
			if (mntmComputeMax.isSelected()) {
				computeFlags |= ClassWriter.COMPUTE_MAXS;
			}
			for (ClassNode node : classes) {
				ClassWriter writer = new ClassWriter(computeFlags);
				node.accept(writer);
				output.putNextEntry(new JarEntry(getFullName(node.name)));
				output.write(writer.toByteArray());
				output.closeEntry();
			}
			output.finish();
			output.close();
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
		JOptionPane.showMessageDialog(INSTANCE, s, "Error!", JOptionPane.ERROR_MESSAGE);
	}

	public static void showError(String s) {
		JOptionPane.showMessageDialog(INSTANCE, s, "Error!", JOptionPane.ERROR_MESSAGE);
	}

	public static String getFullName(String s) {
		return s + ".class";
	}

	class ArchiveTreeModel extends DefaultTreeModel {

		public ArrayList<String> paths = new ArrayList<>();
		public boolean newCreated = false;

		public ArchiveTreeModel() {
			super(new ByteEditTreeNode("New"));
			newCreated = true;
		}

		public ArchiveTreeModel(JarFile jar) {
			super(new ByteEditTreeNode(jar.getName().split(File.separator.equals("\\") ? "\\\\"
					: File.separator)[jar.getName().split(File.separator.equals("\\") ? "\\\\" : File.separator).length
							- 1]));
			try {
				classNodes.clear();
				OTHER_FILES.clear();
				Enumeration<JarEntry> enumeration = jar.entries();
				while (enumeration.hasMoreElements()) {
					JarEntry next = enumeration.nextElement();
					byte[] data = toByteArray(jar.getInputStream(next));
					if (next.getSize() != 0 && !next.getName().startsWith("META-INF")
							&& (next.getName().endsWith(".class") || next.getName().endsWith(".class/"))) {
						try {
							ClassReader reader = new ClassReader(data);
							ClassNode node = new ClassNode();
							reader.accept(node, 0);
							classNodes.put(getFullName(node.name), node);

							if ((next.getName().contains("/")
									? (!SLASH.split(next.getName())[SLASH.split(next.getName()).length - 1]
											.contains("$"))
									: (!next.getName().contains("$")))
									|| (next.getName().startsWith("$") || next.getName().contains("$$")
											|| next.getName().endsWith("$"))) {
								paths.add(getFullName(node.name));
							}
						} catch (Exception e) {
							OTHER_FILES.put(next.getName(), data);
						}
					} else {
						OTHER_FILES.put(next.getName(), data);
					}
				}
				jar.close();
				refresh();
			} catch (Throwable e) {
				classNodes.clear();
				OTHER_FILES.clear();
				e.printStackTrace();
				showError(e);
			}
		}

		private int getSlashCount(String s) {
			int count = 0;
			for (char c : s.toCharArray()) {
				if (c == '/')
					count++;
			}
			return count;
		}

		public void refresh() {
			setRoot(new ByteEditTreeNode(((ByteEditTreeNode) getRoot()).toString()));
			Collections.sort(paths, String.CASE_INSENSITIVE_ORDER);
			Collections.sort(paths, new Comparator<String>() {
				public int compare(String s1, String s2) {
					return getSlashCount(s2) - getSlashCount(s1);
				}
			});

			// Windows-like sorting (like file explorer)
			List<String> tmp = new ArrayList<>(paths);

			List<String> folders = tmp.stream().filter(s -> {
				int c = countSlashes(s, 50);
				return c > 0 && c < 50;
			}).map(s -> {
				int idx = s.lastIndexOf('/');
				if (idx == s.length() - 1)
					idx = s.lastIndexOf('/', idx - 1);
				if (idx == -1)
					return s;
				return s.substring(0, idx);
			}).distinct().sorted(new Comparator<String>() {
				@Override
				public int compare(String s, String s2) {
					// From String#compareToIgnoreCase(String)
					int n = s.length();
					int n2 = s2.length();
					int n3 = Math.min(n, n2);
					for (int i = 0; i < n3; ++i) {
						char c;
						char c2 = s.charAt(i);
						if (c2 == (c = s2.charAt(i))
								|| (c2 = Character.toUpperCase(c2)) == (c = Character.toUpperCase(c))
								|| (c2 = Character.toLowerCase(c2)) == (c = Character.toLowerCase(c)))
							continue;
						return c2 - c;
					}
					// Other way around
					return n2 - n;
				}
			}).collect(Collectors.toList());

			tmp.removeAll(folders);
			tmp.addAll(0, folders);

			for (String s : tmp) {
				String[] elements = SLASH.split(s, 50);
				if (elements.length >= 50) {
					((ByteEditTreeNode) getRoot()).add(new ByteEditTreeNode(s, s));
				} else {
					ByteEditTreeNode currentNode = (ByteEditTreeNode) getRoot();
					for (int i = 0; i < elements.length; i++) {
						String token = elements[i];
						ByteEditTreeNode nextNode = findNode(currentNode, token);
						if (nextNode == null) {
							nextNode = new ByteEditTreeNode(token, s);
							currentNode.add(nextNode);
						}
						currentNode = nextNode;
					}
				}
			}
		}

		private ByteEditTreeNode findNode(ByteEditTreeNode parent, String name) {
			Enumeration<?> e = parent.children();
			while (e.hasMoreElements()) {
				ByteEditTreeNode element = (ByteEditTreeNode) e.nextElement();
				if (element.getUserObject().equals(name)) {
					return element;
				}
			}
			return null;
		}

		private int countSlashes(String s, int max) {
			int count = 0;
			for (char c : s.toCharArray()) {
				if (c == '/') {
					count++;
					if (count == max)
						break;
				}
			}
			return count;
		}
	}

	private static class FurtherExpandingTreeExpansionListener implements TreeExpansionListener {
		public void treeExpanded(TreeExpansionEvent event) {
			TreePath treePath = event.getPath();
			Object expandedTreePathObject = treePath.getLastPathComponent();
			if (!(expandedTreePathObject instanceof TreeNode))
				return;
			TreeNode expandedTreeNode = (TreeNode) expandedTreePathObject;
			if (expandedTreeNode.getChildCount() == 1) {
				TreeNode descendantTreeNode = expandedTreeNode.getChildAt(0);
				if (descendantTreeNode.isLeaf())
					return;
				TreePath nextTreePath = treePath.pathByAddingChild(descendantTreeNode);
				tree.expandPath(nextTreePath);
			}
		}

		public void treeCollapsed(TreeExpansionEvent event) {
		}
	}

	public static byte[] toByteArray(final InputStream input) throws IOException {
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return output.toByteArray();
		}
	}
}
