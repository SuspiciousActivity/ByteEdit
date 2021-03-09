package me.ByteEdit.boxes;

import java.awt.Toolkit;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.objectweb.asm.tree.MethodNode;

import me.ByteEdit.decompiler.SingleThreadedExecutor;
import me.ByteEdit.main.Main;

public class GlobalSearchOpenBox extends JFrame {

	public JPanel contentPane;
	DefaultListModel<Info> model;

	public GlobalSearchOpenBox() {
		setResizable(false);
		setTitle("Global Search - Result");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 200),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 140), 400, 280);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		model = new DefaultListModel<>();
		JList<Info> list = new JList(model);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				SingleThreadedExecutor.execute(() -> {
					Info val = list.getSelectedValue();
					if (val != null && !val.owner.equals(Main.currentNodeName)) {
						Main.selectFile(val.nodePath);
					}
				});
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 374, 229);
		contentPane.add(scrollPane);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
	}

	static class Info {
		String owner;
		String name;
		String desc;
		MethodNode mn;
		String nodePath;

		public Info(String owner, String name, String desc, MethodNode mn) {
			this.owner = owner;
			this.name = name;
			this.desc = desc;
			this.mn = mn;
			nodePath = Main.getFullName(owner);
		}

		@Override
		public String toString() {
			return owner + "/" + name + " " + desc;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((desc == null) ? 0 : desc.hashCode());
			result = prime * result + ((mn == null) ? 0 : mn.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((owner == null) ? 0 : owner.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Info other = (Info) obj;
			if (desc == null) {
				if (other.desc != null)
					return false;
			} else if (!desc.equals(other.desc))
				return false;
			if (mn == null) {
				if (other.mn != null)
					return false;
			} else if (!mn.equals(other.mn))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (owner == null) {
				if (other.owner != null)
					return false;
			} else if (!owner.equals(other.owner))
				return false;
			return true;
		}

	}

}
