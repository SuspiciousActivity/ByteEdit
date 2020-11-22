package me.ByteEdit.edit;

import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class OptionBox extends JFrame {
	
	private JPanel contentPane;
	public JCheckBox chckbxComputeFrames;
	public JCheckBox chckbxComputeMax;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				try {
					OptionBox frame = new OptionBox();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public OptionBox() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
