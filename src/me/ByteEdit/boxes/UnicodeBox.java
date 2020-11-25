package me.ByteEdit.boxes;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import me.ByteEdit.main.Main;
import me.ByteEdit.utils.UnicodeUtils;

public class UnicodeBox extends JFrame {

	private JPanel contentPane;

	public UnicodeBox() {
		setResizable(false);
		setTitle("Unicode Converter");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 150),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 100), 300, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JTextArea textArea = new JTextArea();

		JButton btnEscape = new JButton("Escape");
		btnEscape.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					textArea.setText(UnicodeUtils.escape(null, textArea.getText(), true));
				} catch (IllegalArgumentException ex) {
					Main.showError(ex.getMessage());
				}
			}
		});
		btnEscape.setBounds(10, 137, 89, 23);
		contentPane.add(btnEscape);

		JButton btnUnescape = new JButton("Unescape");
		btnUnescape.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					textArea.setText(UnicodeUtils.unescape(null, textArea.getText(), true));
				} catch (IllegalArgumentException ex) {
					Main.showError(ex.getMessage());
				}
			}
		});
		btnUnescape.setBounds(195, 137, 89, 23);
		contentPane.add(btnUnescape);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 274, 115);
		contentPane.add(scrollPane);

		scrollPane.setViewportView(textArea);
	}
}
