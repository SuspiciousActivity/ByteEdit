package me.ByteEdit.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;

public class ThemeManager {

	private static boolean dark = true;

	private static File file;

	private static Theme rstaThemeDark, rstaThemeLight;

	private static List<RSyntaxTextArea> textAreas = new ArrayList();
	private static List<JFrame> frames = new ArrayList();

	static {
		String s = System.getenv("APPDATA");
		if (s == null)
			s = System.getProperty("user.home");
		file = new File(s, "ByteEditTheme.conf");
	}

	public static void save() {
		try (BufferedWriter wr = new BufferedWriter(new FileWriter(file))) {
			wr.write(UIManager.getLookAndFeel().getClass().getName() + "\n");
			wr.write(Boolean.toString(dark));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void load() {
		try {
			rstaThemeDark = Theme
					.load(ThemeManager.class.getClassLoader().getResourceAsStream("org/fife/eclipse_dark.xml"));
			rstaThemeLight = Theme
					.load(ThemeManager.class.getClassLoader().getResourceAsStream("org/fife/eclipse.xml"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		String theme = null;
		if (file.exists()) {
			try (BufferedReader read = new BufferedReader(new FileReader(file))) {
				theme = read.readLine();
				boolean dark = Boolean.parseBoolean(read.readLine());
				changeUIManager(theme, dark);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			theme = FlatArcDarkIJTheme.class.getName();
			changeUIManager(theme, true);
		}
	}

	public static void changeUIManager(String clazz, Boolean isDark) {
		try {
			UIManager.setLookAndFeel(clazz);
			LookAndFeel laf = UIManager.getLookAndFeel();
			boolean nextDark = false;
			if (laf instanceof IntelliJTheme.ThemeLaf) {
				IntelliJTheme.ThemeLaf t = (IntelliJTheme.ThemeLaf) laf;
				nextDark = t.isDark();
			} else if (isDark != null) {
				nextDark = isDark.booleanValue();
			}
			if (nextDark != dark) {
				dark = nextDark;
				applyRSTATheme();
			}
			frames.forEach(SwingUtilities::updateComponentTreeUI);
			save();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void applyRSTATheme() {
		Theme theme = dark ? rstaThemeDark : rstaThemeLight;
		textAreas.forEach(theme::apply);
	}

	public static void registerFrames(JFrame frame) {
		frames.add(frame);
	}

	public static void registerTextArea(RSyntaxTextArea textArea) {
		textAreas.add(textArea);
	}

}
