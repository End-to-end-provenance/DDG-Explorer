package laser.ddg.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import laser.ddg.persist.FileUtil;

/**
 * Manages the preferences for the user.  The user changes the 
 * preferences using the preference menu.  The preferences are
 * saved persistently in a file whenever they change and are
 * loaded from the file when DDGExplorer starts.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class Preferences {
	private static final File PREFERENCE_FILE = new File(FileUtil.DDG_DIRECTORY
			+ "prefs.txt");
	private static Hashtable<String, String> preferences = new Hashtable<String, String>();


	/**
	 * Loads user preferences from a file or sets to the default if there is no
	 * preference file.
	 */
	void load() {
		// Set default values
		preferences.put("WindowWidth", "950");
		preferences.put("WindowHeight", "700");

		try {
			if (PREFERENCE_FILE.exists()) {
				BufferedReader in = null;
				try {
					in = new BufferedReader(new FileReader(PREFERENCE_FILE));
					String nextLine = in.readLine();
					while (nextLine != null) {
						String[] tokens = nextLine.split("[\\s=]+");
						if (!tokens[0].startsWith("#")) {
							String prefVar = tokens[0];
							String prefValue = tokens[1];
							preferences.put(prefVar, prefValue);
						}
						nextLine = in.readLine();
					}
				} finally {
					if (in != null) {
						in.close();
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to load preferences: " + e.getMessage(),
					"Error loading preferences", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves the current settings to a preference file.
	 */
	private void savePreferences() {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(PREFERENCE_FILE));
			out.println("# DDG Explorer preferences");
			for (String prefVar : preferences.keySet()) {
				out.println(prefVar + " = " + preferences.get(prefVar));
			}
		}
		catch (Exception e) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to save the preferences: " + e.getMessage(),
					"Error saving preferences", JOptionPane.WARNING_MESSAGE);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Persistently changes the preferred window size
	 * @param bounds the preferred width and height
	 */
	public void setWindowSize(Rectangle bounds) {
		preferences.put("WindowWidth", "" + (int) bounds.getWidth());
		preferences.put("WindowHeight", "" + (int) bounds.getHeight());
		savePreferences();
	}

	/**
	 * Returns the user's preferred window size.  If there is
	 * no preference a default size is returned.
	 * @return the user's preferred window size.  If there is
	 * no preference a default size is returned.
	 */
	public Dimension getWindowSize() {
		if (preferences.containsKey("WindowWidth")
				&& preferences.containsKey("WindowHeight")) {
			int preferredWidth = Integer.parseInt(preferences
					.get("WindowWidth"));
			int preferredHeight = Integer.parseInt(preferences
					.get("WindowHeight"));
			return new Dimension(preferredWidth, preferredHeight);
		} 
			
		return new Dimension(950, 700);
	}

	/**
	 * @return true if the user prefers arrows that put from inputs to outputs
	 */
	public boolean isArrowDirectionDown() {
		return preferences.get("ArrowDirection").toLowerCase().equals("intoout");
	}

	/**
	 * Persistently records the user's preference for arrows that point from
	 * inputs to outputs.
	 */
	public void setArrowDirectionDown() {
		preferences.put("ArrowDirection", "InToOut");
		savePreferences();
	}

	/**
	 * Persistently records the user's preference for arrows that point from
	 * outputs to inputs.
	 */
	public void setArrowDirectionUp() {
		preferences.put("ArrowDirection", "OutToIn");
		savePreferences();
	}

	/**
	 * @return true if the user wants to see the legend
	 */
	public boolean isShowLegend() {
		return preferences.get("ShowLegend").toLowerCase().equals("true");
	}

	/**
	 * Persistently store the user's preference about seeing the legend
	 * @param show if true, the user wants to see the legend.
	 */
	public void setShowLegend(boolean show) {
		if (show) {
			preferences.put("ShowLegend", "true");
		}
		else {
			preferences.put("ShowLegend", "false");
		}
		savePreferences();
	}



}
