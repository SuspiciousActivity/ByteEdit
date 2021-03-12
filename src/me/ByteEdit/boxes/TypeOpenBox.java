package me.ByteEdit.boxes;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import me.ByteEdit.decompiler.SingleThreadedExecutor;
import me.ByteEdit.main.Main;
import me.ByteEdit.utils.UnicodeUtils;

public class TypeOpenBox extends JFrame {

	public JPanel contentPane;
	public JTextField txtSearch;

	public TypeOpenBox() {
		setResizable(false);
		setTitle("Open Type");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 200),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 140), 400, 280);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		DefaultListModel<String> model = new DefaultListModel<>();
		JList<String> list = new JList(model);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				String val = list.getSelectedValue();
				if (val != null && !val.equals(Main.currentNodeName)) {
					SingleThreadedExecutor.submit(() -> Main.selectFile(val));
				}
			}
		});

		txtSearch = new JTextField();
		txtSearch.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (model.size() == 1) {
					list.setSelectedIndex(0);
				}
			}
		});
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				updateList();
			}

			public void removeUpdate(DocumentEvent e) {
				updateList();
			}

			public void insertUpdate(DocumentEvent e) {
				updateList();
			}

			public void updateList() {
				model.clear();
				if (txtSearch.getText().isEmpty()) {
					return;
				}
				String search = UnicodeUtils.unescape(null, txtSearch.getText(), true).toLowerCase();
				if (search.contains("/")) {
					for (String className : Main.classNodes.keySet()) {
						if (!className.contains("$") && className.toLowerCase().startsWith(search)) {
							model.addElement(className);
						}
					}
				} else {
					for (String className : Main.classNodes.keySet()) {
						String[] split = className.split("/");
						className = split[split.length - 1];
						if (!className.contains("$") && className.toLowerCase().startsWith(search)) {
							model.addElement(className);
						}
					}
				}
			}
		});
		txtSearch.setBounds(10, 11, 374, 20);
		contentPane.add(txtSearch);
		txtSearch.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 46, 374, 194);
		contentPane.add(scrollPane);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
	}

}
