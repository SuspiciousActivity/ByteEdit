package me.ByteEdit.boxes;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;

import me.ByteEdit.main.ThemeManager;

public class ThemeBox extends JFrame {

	private JPanel contentPane;
	private final JScrollPane scrollPane = new JScrollPane();
	private final JList<LAFI> list;

	public ThemeBox() {
		setTitle("Theme Chooser");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - 200),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - 200), 400, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		contentPane.add(scrollPane, BorderLayout.CENTER);
		DefaultListModel<LAFI> model = new DefaultListModel<>();
		list = new JList(model);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					LAFI lafi = model.get(list.locationToIndex(e.getPoint()));
					if (lafi.className != null)
						ThemeManager.changeUIManager(lafi.className, lafi.isDark);
				}
			}
		});
		list.setFont(new Font("Tahoma", Font.BOLD, 18));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		model.addElement(new LAFI(null, "--- Installed Themes ---"));
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			model.addElement(new LAFI(info.getClassName(), info.getName(), false));
		}

		model.addElement(new LAFI(null, "--- Core Themes ---"));
		model.addElement(new LAFI(FlatLightLaf.class.getName(), "Flat Light", false));
		model.addElement(new LAFI(FlatDarkLaf.class.getName(), "Flat Dark", true));
		model.addElement(new LAFI(FlatIntelliJLaf.class.getName(), "Flat IntelliJ", false));
		model.addElement(new LAFI(FlatDarculaLaf.class.getName(), "Flat Darcula", true));

		model.addElement(new LAFI(null, "--- IntelliJ Themes ---"));
		for (LookAndFeelInfo info : FlatAllIJThemes.INFOS) {
			model.addElement(new LAFI(info.getClassName(), info.getName()));
		}

		scrollPane.setViewportView(list);
	}

	static class LAFI {
		final String className;
		final String name;
		final Boolean isDark;

		public LAFI(String className, String name) {
			this.className = className;
			this.name = name;
			this.isDark = null;
		}

		public LAFI(String className, String name, boolean isDark) {
			this.className = className;
			this.name = name;
			this.isDark = Boolean.valueOf(isDark);
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
