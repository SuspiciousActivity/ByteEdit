package me.ByteEdit.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.tree.DefaultTreeModel;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class ArchiveTreeModel extends DefaultTreeModel {

	public final ArrayList<String> paths = new ArrayList<>();
	private final HashMap<String, ClassNode> classNodes;
	private final HashMap<String, byte[]> otherFiles;
	private final ZipFile jar;

	public boolean newCreated = false;

	public ArchiveTreeModel(HashMap<String, ClassNode> classNodes, HashMap<String, byte[]> otherFiles) {
		super(new ByteEditTreeNode("New"));
		this.newCreated = true;
		this.jar = null;
		this.classNodes = classNodes;
		this.otherFiles = otherFiles;
	}

	public ArchiveTreeModel(ZipFile jar, HashMap<String, ClassNode> classNodes, HashMap<String, byte[]> otherFiles) {
		super(new ByteEditTreeNode(jar.getName().split(File.separator.equals("\\") ? "\\\\"
				: File.separator)[jar.getName().split(File.separator.equals("\\") ? "\\\\" : File.separator).length
						- 1]));
		this.newCreated = false;
		this.jar = jar;
		this.classNodes = classNodes;
		this.otherFiles = otherFiles;
		initialize();
	}

	private void initialize() {
		try {
			classNodes.clear();
			otherFiles.clear();
			Enumeration<? extends ZipEntry> enumeration = jar.entries();
			while (enumeration.hasMoreElements()) {
				ZipEntry next = enumeration.nextElement();
				byte[] data = toByteArray(jar.getInputStream(next));
				if (next.getSize() != 0 && !next.getName().startsWith("META-INF")
						&& (next.getName().endsWith(".class") || next.getName().endsWith(".class/"))) {
					try {
						ClassReader reader = new ClassReader(data);
						ClassNode node = new ClassNode();
						reader.accept(node, 0);
						classNodes.put(node.name, node);

						if ((next.getName().contains("/")
								? (!Main.patternSlash
										.split(next.getName())[Main.patternSlash.split(next.getName()).length - 1]
												.contains("$"))
								: (!next.getName().contains("$")))
								|| (next.getName().startsWith("$") || next.getName().contains("$$")
										|| next.getName().endsWith("$"))) {
							paths.add(node.name + ".class");
						}
					} catch (Exception e) {
						otherFiles.put(next.getName(), data);
					}
				} else {
					otherFiles.put(next.getName(), data);
				}
			}
			jar.close();
			refresh();
		} catch (Throwable e) {
			classNodes.clear();
			otherFiles.clear();
			e.printStackTrace();
			Main.showError(e);
		}
	}

	private static int getSlashCount(String s) {
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
					if (c2 == (c = s2.charAt(i)) || (c2 = Character.toUpperCase(c2)) == (c = Character.toUpperCase(c))
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
			String[] elements = Main.patternSlash.split(s, 50);
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

	private static ByteEditTreeNode findNode(ByteEditTreeNode parent, String name) {
		Enumeration<?> e = parent.children();
		while (e.hasMoreElements()) {
			ByteEditTreeNode element = (ByteEditTreeNode) e.nextElement();
			if (element.getUserObject().equals(name)) {
				return element;
			}
		}
		return null;
	}

	private static int countSlashes(String s, int max) {
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

	private static byte[] toByteArray(final InputStream input) throws IOException {
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
