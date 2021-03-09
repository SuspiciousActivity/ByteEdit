package me.ByteEdit.main;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class FurtherExpandingTreeExpansionListener implements TreeExpansionListener {

	private final JTree tree;

	public FurtherExpandingTreeExpansionListener(JTree tree) {
		this.tree = tree;
	}

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
