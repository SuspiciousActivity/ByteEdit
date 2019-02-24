package me.ByteEdit.boxes;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.ByteEdit.boxes.GlobalSearchOpenBox.Info;
import me.ByteEdit.main.Main;

public class GlobalSearchBox extends JFrame {

	public JPanel contentPane;
	public JTextField txtString;
	public JCheckBox chckbxCaseSensitive;
	private JLabel lblOwner;
	private JTextField txtOwner;
	private JLabel lblName;
	private JTextField txtName;
	private JLabel lblDesc;
	private JTextField txtDesc;
	private GlobalSearchOpenBox openBox;

	public GlobalSearchBox() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		openBox = new GlobalSearchOpenBox();
		setResizable(false);
		setTitle("Global Search");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 128),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 181), 256, 262);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		JLabel lblFind = new JLabel("String Search");
		lblFind.setBounds(10, 25, 71, 14);
		contentPane.add(lblFind);
		txtString = new JTextField();
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		txtString.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				find();
			}
		}, enter, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtString.setBounds(91, 22, 149, 20);
		contentPane.add(txtString);
		txtString.setColumns(10);
		chckbxCaseSensitive = new JCheckBox("Case sensitive");
		chckbxCaseSensitive.setBounds(10, 199, 114, 23);
		contentPane.add(chckbxCaseSensitive);
		JButton btnFind = new JButton("Find");
		btnFind.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				find();
			}
		});
		btnFind.setBounds(130, 199, 110, 23);
		contentPane.add(btnFind);

		JLabel lblFieldmethodSearch = new JLabel("Field/Method Search");
		lblFieldmethodSearch.setBounds(10, 53, 230, 14);
		contentPane.add(lblFieldmethodSearch);

		lblOwner = new JLabel("Owner");
		lblOwner.setBounds(10, 81, 71, 14);
		contentPane.add(lblOwner);

		txtOwner = new JTextField();
		txtOwner.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				find();
			}
		});
		txtOwner.setColumns(10);
		txtOwner.setBounds(91, 78, 149, 20);
		contentPane.add(txtOwner);

		lblName = new JLabel("Name");
		lblName.setBounds(10, 109, 71, 14);
		contentPane.add(lblName);

		txtName = new JTextField();
		txtName.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				find();
			}
		});
		txtName.setColumns(10);
		txtName.setBounds(91, 106, 149, 20);
		contentPane.add(txtName);

		lblDesc = new JLabel("Desc");
		lblDesc.setBounds(10, 137, 71, 14);
		contentPane.add(lblDesc);

		txtDesc = new JTextField();
		txtDesc.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				find();
			}
		});
		txtDesc.setColumns(10);
		txtDesc.setBounds(91, 134, 149, 20);
		contentPane.add(txtDesc);
	}

	public void find() {
		boolean ignoreCase = !chckbxCaseSensitive.isSelected();
		openBox.model.clear();
		Set<Info> set = new HashSet<>();
		if (!txtString.getText().isEmpty()) {
			String str = txtString.getText();
			if (ignoreCase) {
				str = str.toLowerCase();
			}
			for (ClassNode cn : Main.classNodes.values()) {
				for (MethodNode mn : cn.methods) {
					for (AbstractInsnNode insn : mn.instructions.toArray()) {
						if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof String) {
							String s = (String) ((LdcInsnNode) insn).cst;
							if (ignoreCase) {
								s = s.toLowerCase();
							}
							if (s.startsWith(str)) {
								set.add(new Info(cn.name, mn.name, mn.desc, mn));
							}
						}
					}
				}
			}
		} else {
			String owner = txtOwner.getText();
			String name = txtName.getText();
			String desc = txtDesc.getText();
			for (ClassNode cn : Main.classNodes.values()) {
				for (MethodNode mn : cn.methods) {
					for (AbstractInsnNode insn : mn.instructions.toArray()) {
						if (insn instanceof FieldInsnNode) {
							if (((FieldInsnNode) insn).owner.equals(owner) && ((FieldInsnNode) insn).name.equals(name)
									&& ((FieldInsnNode) insn).desc.equals(desc)) {
								set.add(new Info(cn.name, mn.name, mn.desc, mn));
							}
						} else if (insn instanceof MethodInsnNode) {
							if (((MethodInsnNode) insn).owner.equals(owner) && ((MethodInsnNode) insn).name.equals(name)
									&& ((MethodInsnNode) insn).desc.equals(desc)) {
								set.add(new Info(cn.name, mn.name, mn.desc, mn));
							}
						}
					}
				}
			}
		}
		for (Info s : set) {
			openBox.model.addElement(s);
		}
		openBox.setVisible(true);
	}
}
