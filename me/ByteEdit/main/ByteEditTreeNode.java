package me.ByteEdit.main;

import javax.swing.tree.DefaultMutableTreeNode;

public class ByteEditTreeNode extends DefaultMutableTreeNode {

	public String path;

	public ByteEditTreeNode(String name) {
		super(name);
	}

	public ByteEditTreeNode(String name, String path) {
		this(name);
		if (name.endsWith(".class"))
			this.path = path;
	}

}
