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
import javax.swing.border.EmptyBorder;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.ByteEdit.boxes.GlobalSearchOpenBox.Info;
import me.ByteEdit.main.Main;
import me.ByteEdit.main.ThemeManager;
import me.ByteEdit.utils.UnicodeUtils;

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
		openBox = new GlobalSearchOpenBox();
		ThemeManager.registerFrames(openBox);
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
		txtString.registerKeyboardAction(e -> find(), enter, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtString.setBounds(91, 22, 149, 20);
		contentPane.add(txtString);
		txtString.setColumns(10);
		chckbxCaseSensitive = new JCheckBox("Case sensitive");
		chckbxCaseSensitive.setBounds(10, 199, 114, 23);
		contentPane.add(chckbxCaseSensitive);
		JButton btnFind = new JButton("Find");
		btnFind.addActionListener(e -> find());
		btnFind.setBounds(130, 199, 110, 23);
		contentPane.add(btnFind);

		JLabel lblFieldmethodSearch = new JLabel("Field/Method Search");
		lblFieldmethodSearch.setBounds(10, 53, 230, 14);
		contentPane.add(lblFieldmethodSearch);

		lblOwner = new JLabel("Owner");
		lblOwner.setBounds(10, 81, 71, 14);
		contentPane.add(lblOwner);

		txtOwner = new JTextField();
		txtOwner.addActionListener(e -> find());
		txtOwner.setColumns(10);
		txtOwner.setBounds(91, 78, 149, 20);
		contentPane.add(txtOwner);

		lblName = new JLabel("Name");
		lblName.setBounds(10, 109, 71, 14);
		contentPane.add(lblName);

		txtName = new JTextField();
		txtName.addActionListener(e -> find());
		txtName.setColumns(10);
		txtName.setBounds(91, 106, 149, 20);
		contentPane.add(txtName);

		lblDesc = new JLabel("Desc");
		lblDesc.setBounds(10, 137, 71, 14);
		contentPane.add(lblDesc);

		txtDesc = new JTextField();
		txtDesc.addActionListener(e -> find());
		txtDesc.setColumns(10);
		txtDesc.setBounds(91, 134, 149, 20);
		contentPane.add(txtDesc);
	}

	public void find() {
		boolean ignoreCase = !chckbxCaseSensitive.isSelected();
		openBox.model.clear();
		Set<Info> set = new HashSet<>();
		if (!txtString.getText().isEmpty()) {
			String str = UnicodeUtils.unescape(null, txtString.getText(), true);
			if (ignoreCase) {
				str = str.toLowerCase();
			}
			String fstr = str;
			for (ClassNode cn : Main.classNodes.values()) {
				for (MethodNode mn : cn.methods) {
					for (AbstractInsnNode insn : mn.instructions.toArray()) {
						if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof String) {
							String s = (String) ((LdcInsnNode) insn).cst;
							if (ignoreCase) {
								s = s.toLowerCase();
							}
							if (s.contains(str)) {
								set.add(new Info(cn.name, mn.name, mn.desc, mn));
							}
						}
					}
					if (mn.visibleAnnotations != null) {
						for (AnnotationNode anno : mn.visibleAnnotations) {
							if (anno.values == null)
								continue;
							anno.values.stream().filter(o -> o instanceof String).map(o -> (String) o)
									.map(s -> ignoreCase ? s.toLowerCase() : s).filter(s -> s.contains(fstr))
									.forEach(s -> set.add(new Info(cn.name, mn.name, mn.desc, mn)));
						}
					}
				}
			}
		} else {
			String owner = UnicodeUtils.unescape(null, txtOwner.getText(), true);
			boolean wildCardOwner = owner.isEmpty();
			String name = UnicodeUtils.unescape(null, txtName.getText(), true);
			boolean wildCardName = name.isEmpty();
			String desc = UnicodeUtils.unescape(null, txtDesc.getText(), true);
			boolean wildCardDesc = desc.isEmpty();
			for (ClassNode cn : Main.classNodes.values()) {
				for (MethodNode mn : cn.methods) {
					for (AbstractInsnNode insn : mn.instructions.toArray()) {
						if (insn instanceof FieldInsnNode) {
							if ((wildCardOwner || ((FieldInsnNode) insn).owner.equals(owner))
									&& (wildCardName || ((FieldInsnNode) insn).name.equals(name))
									&& (wildCardDesc || ((FieldInsnNode) insn).desc.equals(desc))) {
								set.add(new Info(cn.name, mn.name, mn.desc, mn));
							}
						} else if (insn instanceof MethodInsnNode) {
							if ((wildCardOwner || ((MethodInsnNode) insn).owner.equals(owner))
									&& (wildCardName || ((MethodInsnNode) insn).name.equals(name))
									&& (wildCardDesc || ((MethodInsnNode) insn).desc.equals(desc))) {
								set.add(new Info(cn.name, mn.name, mn.desc, mn));
							}
						}
					}
				}
			}
		}
		set.forEach(openBox.model::addElement);
		openBox.setVisible(true);
	}
}
