package me.ByteEdit.main;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

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

public class SearchBox extends JFrame {
	
	public JPanel contentPane;
	public JTextField txtFind;
	public JTextField txtReplace;
	public JCheckBox chckbxCaseSensitive;
	
	/**
	 * Create the frame.
	 */
	public SearchBox() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setResizable(false);
		setTitle("Find/Replace");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 128),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 181), 256, 262);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		JLabel lblFind = new JLabel("Find:");
		lblFind.setBounds(10, 25, 71, 14);
		contentPane.add(lblFind);
		txtFind = new JTextField();
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		txtFind.registerKeyboardAction(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				find();
			}
		}, enter, JComponent.WHEN_IN_FOCUSED_WINDOW);
		txtFind.setBounds(91, 22, 149, 20);
		contentPane.add(txtFind);
		txtFind.setColumns(10);
		JLabel lblReplaceWith = new JLabel("Replace with:");
		lblReplaceWith.setBounds(10, 53, 71, 14);
		contentPane.add(lblReplaceWith);
		txtReplace = new JTextField();
		txtReplace.setColumns(10);
		txtReplace.setBounds(91, 50, 149, 20);
		contentPane.add(txtReplace);
		chckbxCaseSensitive = new JCheckBox("Case sensitive");
		chckbxCaseSensitive.setBounds(10, 103, 230, 23);
		contentPane.add(chckbxCaseSensitive);
		JButton btnFind = new JButton("Find");
		btnFind.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				find();
			}
		});
		btnFind.setBounds(10, 150, 110, 23);
		contentPane.add(btnFind);
		JButton btnReplacefind = new JButton("Replace/Find");
		btnReplacefind.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				replaceFind();
			}
		});
		btnReplacefind.setBounds(130, 150, 110, 23);
		contentPane.add(btnReplacefind);
		JButton btnReplace = new JButton("Replace");
		btnReplace.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				replace();
			}
		});
		btnReplace.setBounds(10, 184, 110, 23);
		contentPane.add(btnReplace);
		JButton btnReplaceAll = new JButton("Replace All");
		btnReplaceAll.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				replaceAll();
			}
		});
		btnReplaceAll.setBounds(130, 184, 110, 23);
		contentPane.add(btnReplaceAll);
	}
	
	public void find() {
		int startPos = Main.textArea.getCaretPosition();
		String toFind = txtFind.getText();
		String txt = chckbxCaseSensitive.isSelected() ? Main.textArea.getText().substring(startPos)
				: Main.textArea.getText().substring(startPos).toLowerCase();
		int index = txt.indexOf(chckbxCaseSensitive.isSelected() ? toFind : toFind.toLowerCase());
		if (index == -1) {
			txt = chckbxCaseSensitive.isSelected() ? Main.textArea.getText() : Main.textArea.getText().toLowerCase();
			index = txt.indexOf(chckbxCaseSensitive.isSelected() ? toFind : toFind.toLowerCase());
			startPos = 0;
			if (index == -1) {
				return;
			}
		}
		Main.textArea.select(startPos + index, startPos + index + toFind.length());
	}
	
	public void replaceFind() {
		replace();
		find();
	}
	
	public void replace() {
		String txt = Main.textArea.getText();
		int startPos = Main.textArea.getSelectionStart();
		Main.textArea.replaceSelection(txtReplace.getText());
		Main.textArea.select(startPos, startPos + txtReplace.getText().length());
	}
	
	public void replaceAll() {
		String toFind = txtFind.getText();
		String toReplace = txtReplace.getText();
		String txt = Main.textArea.getText();
		int prev = Main.textArea.getCaretPosition();
		if (chckbxCaseSensitive.isSelected()) {
			Main.textArea.replaceRange(txt.replace(toFind, toReplace), 0, txt.length());
		} else {
			Main.textArea.replaceRange(txt.replaceAll("(?i)" + Pattern.quote(toFind), toReplace), 0, txt.length());
		}
		Main.textArea.setCaretPosition(prev);
	}
}
