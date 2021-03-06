package me.ByteEdit.boxes;

import java.awt.Toolkit;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class OptionBox extends JFrame {

	private JPanel contentPane;
	public JCheckBox chckbxComputeFrames;
	public JCheckBox chckbxComputeMax;

	public OptionBox() {
		setResizable(false);
		setTitle("Options");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 150),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 100), 300, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		chckbxComputeFrames = new JCheckBox("Compute Frames");
		chckbxComputeFrames.setBounds(6, 7, 138, 23);
		contentPane.add(chckbxComputeFrames);
		chckbxComputeMax = new JCheckBox("Compute Max");
		chckbxComputeMax.setBounds(6, 33, 138, 23);
		contentPane.add(chckbxComputeMax);
	}
}
