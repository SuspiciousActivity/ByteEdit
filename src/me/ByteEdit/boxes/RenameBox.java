package me.ByteEdit.boxes;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.ByteEdit.edit.Disassembler;
import me.ByteEdit.main.Main;
import me.ByteEdit.utils.ClassUtil;
import me.ByteEdit.utils.UnicodeUtils;

public class RenameBox extends JFrame {

	private JPanel contentPane;
	public JTextField txtName;
	public JTextField txtDesc;

	public String className;
	public String name;
	public String desc;

	public RenameBox() {
		setResizable(false);
		setTitle("Rename");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 225),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 150), 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblName = new JLabel("Name");
		lblName.setBounds(10, 11, 33, 14);
		contentPane.add(lblName);

		JLabel lblDesc = new JLabel("Desc");
		lblDesc.setBounds(10, 36, 33, 14);
		contentPane.add(lblDesc);

		txtName = new JTextField();
		txtName.setBounds(53, 8, 381, 20);
		contentPane.add(txtName);
		txtName.setColumns(10);

		txtDesc = new JTextField();
		txtDesc.setColumns(10);
		txtDesc.setBounds(53, 36, 381, 20);
		contentPane.add(txtDesc);

		JButton btnRename = new JButton("Rename");
		btnRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (className == null) {
					JOptionPane.showMessageDialog(null, "ClassName not set!", "Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (name == null) {
					JOptionPane.showMessageDialog(null, "Name not set!", "Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (desc == null) {
					JOptionPane.showMessageDialog(null, "Desc not set!", "Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				ClassNode clazz = Main.classNodes.get(className + ".class");
				if (clazz == null) {
					JOptionPane.showMessageDialog(null, "ClassNode not found!", "Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String newName = UnicodeUtils.unescape(txtName.getText());
				String newDesc = UnicodeUtils.unescape(txtDesc.getText());
				if (ClassUtil.isObjectClassMethod(newName, newDesc, true)) {
					JOptionPane.showMessageDialog(null, "Invalid name!", "Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (desc.startsWith("(")) {
					if (newDesc.startsWith("(")) { // method
						ClassNode lastHit = clazz;
						while (clazz != null && clazz.superName != null) {
							ClassNode tmp = Main.classNodes.get(clazz.superName + ".class");
							if (tmp != null) {
								boolean found = false;
								for (MethodNode mn : clazz.methods) {
									if (mn.name.equals(name) && mn.desc.equals(desc)) {
										lastHit = clazz;
										clazz = tmp;
										found = true;
										break;
									}
								}
								if (!found) {
									clazz = tmp;
									lastHit = null;
								}
							} else {
								break;
							}
						}
						if (lastHit != null) {
							clazz = lastHit;
						}
						LinkedList<MethodNode> methodsToRename = new LinkedList<>();
						LinkedList<MethodInsnNode> minsToRename = new LinkedList<>();
						for (ClassNode cn : Main.classNodes.values()) {
							boolean extendsMyClass = cn == clazz;
							if (!extendsMyClass) {
								ClassNode checkCN = cn;
								while (checkCN != null && checkCN.superName != null) {
									if (clazz.name.equals(checkCN.superName)) {
										extendsMyClass = true;
										break;
									}
									ClassNode tmp = Main.classNodes.get(checkCN.superName + ".class");
									if (tmp != null) {
										checkCN = tmp;
									} else {
										break;
									}
								}
							}
							for (MethodNode mn : cn.methods) {
								if (extendsMyClass) {
									if (mn.name.equals(newName) && mn.desc.equals(newDesc)) {
										JOptionPane.showMessageDialog(null, "Method already exists in " + cn.name + "!",
												"Error!", JOptionPane.ERROR_MESSAGE);
										return;
									}
									if (mn.name.equals(name) && mn.desc.equals(desc)) {
										methodsToRename.add(mn);
									}
								}
								for (AbstractInsnNode ain : mn.instructions.toArray()) {
									if (ain instanceof MethodInsnNode) {
										MethodInsnNode min = (MethodInsnNode) ain;
										if (min.name.equals(name) && min.desc.equals(desc)
												&& min.owner.equals(clazz.name)) {
											minsToRename.add(min);
										}
									}
								}
							}
						}
						for (MethodNode mn : methodsToRename) {
							mn.name = newName;
							mn.desc = newDesc;
						}
						for (MethodInsnNode min : minsToRename) {
							min.name = newName;
							min.desc = newDesc;
						}
					} else {
						JOptionPane.showMessageDialog(null, "New Desc is not a method desc!", "Error!",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					if (!newDesc.startsWith("(")) { // field
						FieldNode currentFN = null;
						for (FieldNode fn : clazz.fields) {
							if (fn.name.equals(newName) && fn.desc.equals(newDesc)) {
								JOptionPane.showMessageDialog(null, "Field already exists!", "Error!",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
							if (fn.name.equals(name) && fn.desc.equals(desc)) {
								currentFN = fn;
							}
						}
						if (currentFN == null) {
							JOptionPane.showMessageDialog(null, "Field not found?!", "Error!",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						currentFN.name = newName;
						currentFN.desc = newDesc;
						for (ClassNode cn : Main.classNodes.values()) {
							for (MethodNode mn : cn.methods) {
								for (AbstractInsnNode ain : mn.instructions.toArray()) {
									if (ain instanceof FieldInsnNode) {
										FieldInsnNode fin = (FieldInsnNode) ain;
										if (fin.name.equals(name) && fin.desc.equals(desc)
												&& fin.owner.equals(className)) {
											fin.name = newName;
											fin.desc = newDesc;
										}
									}
								}
							}
						}
					} else {
						JOptionPane.showMessageDialog(null, "New Desc is not a field desc!", "Error!",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				try { // refresh
					ClassNode classNode = Main.classNodes.get(Main.currentNodeName);
					int prev = Main.txtByteEditView.getCaretPosition();
					String dis = Disassembler.disassemble(classNode);
					String substr = Main.currentNodeName.substring(0, Main.currentNodeName.length() - 6);
					for (String key : Main.classNodes.keySet()) {
						if (key.contains("$")) {
							String[] split = key.split("\\$");
							if (split[0].equals(substr)) {
								dis += "\n" + Disassembler.disassemble(Main.classNodes.get(key));
							}
						}
					}
					Main.txtByteEditView.setText(dis);
					Main.txtByteEditView.setCaretPosition(prev);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				name = newName;
				desc = newDesc;
			}
		});
		btnRename.setBounds(345, 237, 89, 23);
		contentPane.add(btnRename);
	}
}
