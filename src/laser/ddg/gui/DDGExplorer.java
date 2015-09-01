package laser.ddg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import laser.ddg.LanguageConfigurator;
import laser.ddg.ProvenanceData;
import laser.ddg.commands.CommandOverviewCommand;
import laser.ddg.commands.CompareScriptsCommand;
import laser.ddg.commands.FindFilesCommand;
import laser.ddg.commands.LoadFileCommand;
import laser.ddg.commands.LoadFromDBCommand;
import laser.ddg.commands.ManageDatabaseCommand;
import laser.ddg.commands.SaveToDBCommand;
import laser.ddg.commands.SetArrowDirectionCommand;
import laser.ddg.commands.ShowAttributesCommand;
import laser.ddg.commands.ShowLegendMenuItem;
import laser.ddg.commands.ShowScriptCommand;
import laser.ddg.persist.FileUtil;
import laser.ddg.query.QueryListener;
import laser.ddg.visualizer.DDGPanel;
import laser.ddg.visualizer.ErrorLog;

/**
 * Class with a main program that allows the user to view DDGs previously stored
 * in a Jena database. The user selects which execution of which process to see
 * a DDG of.
 * 
 * @author Barbara Lerner
 * @version Jul 25, 2012
 * 
 */
public class DDGExplorer extends JFrame implements QueryListener {
	private static final Color MENU_COLOR = new Color(171, 171, 171);

	// The tabbed pane that holds the Home panel and the ddg panels.
	private static JTabbedPane tabbed;

	// preferences on window size
	private static Hashtable<String, String> preferences = new Hashtable<String, String>();
	private static final File PREFERENCE_FILE = new File(FileUtil.DDG_DIRECTORY
			+ "prefs.txt");

	// Color of a tab label when the tab is selected.
	private static final Color SELECTED_TAB_COLOR = Color.GREEN;

	// The singleton
	private static DDGExplorer instance;

	private static JMenuItem saveDB;

	private JMenuItem attributesItem;

	private JMenuItem showScriptItem;

	/**
	 * Initializes the DDG Explorer by loading the preference file and
	 * loading information about known languages.
	 */
	private DDGExplorer() {
		try {
			loadPreferences();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(DDGExplorer.this,
					"Unable to load preferences: " + e1.getMessage(),
					"Error loading preferences", JOptionPane.ERROR_MESSAGE);
		}

		LanguageConfigurator.addLanguageBuilder("R", "laser.ddg.r.RDDGBuilder");
		LanguageConfigurator.addParser("R", "laser.ddg.r.RParser");
		LanguageConfigurator.addLanguageBuilder("Little-JIL",
				"laser.juliette.ddgbuilder.DDGTextBuilder");

		// DON'T DELETE THIS: Sample of how to load a query using reflection
		// try {
		// ClassLoader classLoader = getClass().getClassLoader();
		// Class queryClass =
		// classLoader.loadClass("laser.juliette.ddg.gui.QDerivationQuery");
		// queryObj =
		// (Query) queryClass.newInstance();
		// } catch (InstantiationException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// } catch (IllegalAccessException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// } catch (ClassNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	/**
	 * @return the singleton instance
	 */
	public static DDGExplorer getInstance() {
		if (instance == null) {
			instance = new DDGExplorer();
		}
		return instance;
	}

	private static JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setBackground(MENU_COLOR);

		// allow the user to load a DDG from a text file
		JMenuItem openFile = new JMenuItem("Open from File");
		openFile.addActionListener(new LoadFileCommand());

		// allow the user to load a DDG from the database
		JMenuItem openDB = new JMenuItem("Open from Database");
		openDB.addActionListener(new LoadFromDBCommand());

		saveDB = new JMenuItem("Save to Database");
		saveDB.addActionListener(new SaveToDBCommand());
		saveDB.setEnabled(false);

		// allow the user to compare two R scripts
		JMenuItem compareR = new JMenuItem("Compare R Scripts");
		compareR.addActionListener(new CompareScriptsCommand());

		// allow the user to look for a particular data file
		JMenuItem findFiles = new JMenuItem("Find Data Files");
		findFiles.addActionListener(new FindFilesCommand());

		// allow the user to manage the database
		JMenuItem manageDB = new JMenuItem("Manage Database");
		manageDB.addActionListener(new ManageDatabaseCommand());

		fileMenu.add(openFile);
		fileMenu.add(openDB);
		fileMenu.add(saveDB);
		fileMenu.addSeparator();
		fileMenu.add(compareR);
		fileMenu.add(findFiles);
		fileMenu.add(manageDB);
		return fileMenu;
	}

	/**
	 * Create DDG Menu to be placed into the menuBar
	 * @return JMenu DDG menu
	 */
	public JMenu createDDGMenu() {
		final JMenu DDGMenu = new JMenu("DDG");
		DDGMenu.setBackground(MENU_COLOR);
		
		attributesItem = new JMenuItem("Show attributes");
		attributesItem.addActionListener(new ShowAttributesCommand());
		DDGMenu.add(attributesItem);
		
		showScriptItem = new JMenuItem("Show R script");
		showScriptItem.addActionListener(new ShowScriptCommand());
		DDGMenu.add(showScriptItem);
		
		DDGMenu.add(createPreferencesMenu());	//preferences submenu
		
		return DDGMenu;
	}

	/**
	 * Creates the menu with user preferences
	 * @return 
	 */
	public JMenu createPreferencesMenu() {
		JMenu prefMenu = new JMenu("Preferences");
		prefMenu.setBackground(MENU_COLOR);
		
		final JCheckBoxMenuItem inToOutMenuItem = new JCheckBoxMenuItem("Draw arrows from inputs to outputs", 
				preferences.get("ArrowDirection").toLowerCase().equals("intoout"));
		inToOutMenuItem.addActionListener(new SetArrowDirectionCommand());
		prefMenu.add(inToOutMenuItem);
		
		JCheckBoxMenuItem showLegendMenuItem = new JCheckBoxMenuItem("Show legend", 
				preferences.get("ShowLegend").toLowerCase().equals("true"));
		showLegendMenuItem.addActionListener(new ShowLegendMenuItem());
		prefMenu.add(showLegendMenuItem);
		
		return prefMenu;
	}

	@Override
	public void queryFinished(String name, JComponent panel) {
		addTab(name, panel);
	}

	/**
	 * Adds a tab to the display and makes that tab current.
	 * @param name the title for the tab
	 * @param panel the component to display in the tab
	 */
	public void addTab(String name, JComponent panel) {
		tabbed.addTab(name, panel);
		int tabNum = tabbed.getTabCount() - 1;
		tabbed.setTabComponentAt(tabNum, new TabComp(tabbed, name));
		tabbed.setSelectedIndex(tabNum);
	}

	/**
	 * Create the GUI and show it. 
	 */
	private void createAndShowGUI() {
		String title;
		Properties props = new Properties();
		InputStream propResource = null;
		try {
			propResource = DDGExplorer.class
					.getResourceAsStream("/.properties");
			if (propResource == null) {
				title = "DDG Explorer";
			} else {
				props.load(propResource);
				title = "DDG Explorer v." + props.getProperty("version");
			}
		} catch (IOException e1) {
			title = "DDG Explorer";
		} finally {
			if (propResource != null) {
				try {
					propResource.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		setTitle(title);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add search bar to top of DDG Explorer
		add(new SearchPanel(), BorderLayout.NORTH);

		// add tabbed pane
		tabbed = new JTabbedPane() {

			@Override
			public Color getBackgroundAt(int index) {
				if (getSelectedIndex() == index) {
					return SELECTED_TAB_COLOR;
				}
				return super.getBackgroundAt(index);
			}

		};
		tabbed.setOpaque(true);
		
		// Add menu bar
		JMenuBar menuBar = createMenus();
		setJMenuBar(menuBar);

		tabbed.addChangeListener(new ChangeListener () {
			@Override
			public void stateChanged(ChangeEvent e) {
				Component openTab = getCurrentDDGPanel();
				if (openTab instanceof laser.ddg.visualizer.DDGPanel) {
					enableDDGCommands();
				} else {
					disableDDGCommands();
				}
			}

		});

		tabbed.addTab(" ", null, new HomePanel(), "Home Tab");
		add(tabbed, BorderLayout.CENTER);

		// add log to bottom of frame
		JLabel logLabel = new JLabel("Error Log");
		JScrollPane logScrollPane = new JScrollPane(ErrorLog.getInstance());
		logScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		logScrollPane.setViewportBorder(BorderFactory
				.createLoweredBevelBorder());
		logScrollPane.setPreferredSize(new Dimension(logScrollPane
				.getPreferredSize().width, 80));
		JPanel logPanel = new JPanel(new BorderLayout());
		Border raised = BorderFactory.createRaisedBevelBorder();
		Border lowered = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		logPanel.setBorder(BorderFactory.createCompoundBorder(raised, lowered));
		logPanel.add(logLabel, BorderLayout.NORTH);
		logPanel.add(logScrollPane, BorderLayout.CENTER);
		add(logPanel, BorderLayout.SOUTH);
		// .
		// Nikki is the best programmer! ~ Ariel & Bruce

		// edit preferences when frame is resized
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Rectangle bounds = e.getComponent().getBounds();
				preferences.put("WindowWidth", "" + (int) bounds.getWidth());
				preferences.put("WindowHeight", "" + (int) bounds.getHeight());
				try {
					savePreferences();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to save preferences: " + e1.getMessage(),
							"Error saving preferences",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		// open size based upon preferences, or default 950x700
		if (preferences.containsKey("WindowWidth")
				&& preferences.containsKey("WindowHeight")) {
			int preferredWidth = Integer.parseInt(preferences
					.get("WindowWidth"));
			int preferredHeight = Integer.parseInt(preferences
					.get("WindowHeight"));
			setSize(preferredWidth, preferredHeight);
		} else {
			setSize(950, 700);
		}
		// Display the window.
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JMenuBar createMenus() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(MENU_COLOR);

		JMenu fileMenu = createFileMenu();
		menuBar.add(fileMenu);
		
		JMenu ddgMenu = createDDGMenu();
		menuBar.add(ddgMenu);
		
		JMenu helpMenu = createHelpMenu();
		menuBar.add(helpMenu);
		return menuBar;
	}

	private void enableDDGCommands() {
		this.saveDB.setEnabled(!getCurrentDDGPanel().alreadyInDB());
		attributesItem.setEnabled(true);
		showScriptItem.setEnabled(true);
	}

	private void disableDDGCommands() {
		this.saveDB.setEnabled(false);
		attributesItem.setEnabled(false);
		showScriptItem.setEnabled(false);
	}

	
	/**
	 * Loads user preferences from a file or sets to the default if there is no
	 * preference file.
	 */
	private static void loadPreferences() throws Exception {
		// Set default values
		preferences.put("WindowWidth", "950");
		preferences.put("WindowHeight", "700");

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
			JOptionPane.showMessageDialog(this,
					"Unable to save the preferences: " + e.getMessage(),
					"Error saving preferences", JOptionPane.WARNING_MESSAGE);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Returns the tabbed frame for the DDG Explorer application. This allows
	 * other windows to display onto the main window
	 * 
	 * @return the tabbed frame window
	 */
	public static JTabbedPane tabbedPane() {
		return tabbed;
	}


	/**
	 * Create help menu
	 * @return 
	 */
	public JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setBackground(MENU_COLOR);
		
		JMenuItem commandOverviewItem = new JMenuItem("Command overview");
		commandOverviewItem.addActionListener(new CommandOverviewCommand());
		helpMenu.add(commandOverviewItem);
		return helpMenu;
	}
	
	protected static DDGPanel getCurrentDDGPanel() {
		Component selectedTab = tabbed.getSelectedComponent();
		if (!(selectedTab instanceof DDGPanel)) {
			return null;
		}
		return (DDGPanel) selectedTab;
	}

	/**
	 * Test program to see that the GUI looks right.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DDGExplorer explorer = new DDGExplorer();
			explorer.createAndShowGUI();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to start DDG Explorer: " + e.getMessage(),
					"Error starting DDG Explorer", JOptionPane.ERROR_MESSAGE);
		}
	}

	public ProvenanceData getCurrentDDG() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		return curDDGPanel.getProvData();
	}

	public void setArrowDirectionDown() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			getCurrentDDGPanel().setArrowDirectionDown();
		}
		preferences.put("ArrowDirection", "InToOut");
		savePreferences();
	}

	public void setArrowDirectionUp() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			getCurrentDDGPanel().setArrowDirectionDown();
		}
		preferences.put("ArrowDirection", "OutToIn");
		savePreferences();
	}

	public void addLegend() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			curDDGPanel.addLegend();  
		}
		preferences.put("ShowLegend", "true");
		savePreferences();
	}

	public void removeLegend() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			curDDGPanel.removeLegend();  
		}
		preferences.put("ShowLegend", "false");
		savePreferences();
	}

}
