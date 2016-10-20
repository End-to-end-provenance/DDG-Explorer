package laser.ddg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;

import laser.ddg.LanguageConfigurator;
import laser.ddg.ProvenanceData;
import laser.ddg.commands.CommandOverviewCommand;
import laser.ddg.commands.CompareScriptsCommand;
import laser.ddg.commands.CompareGraphsCommand;
import laser.ddg.commands.FindFilesCommand;
import laser.ddg.commands.FindTimeCommand;
import laser.ddg.commands.LoadFileCommand;
import laser.ddg.commands.LoadFromDBCommand;
import laser.ddg.commands.ManageDatabaseCommand;
import laser.ddg.commands.QuitCommand;
import laser.ddg.commands.SaveToDBCommand;
import laser.ddg.commands.SetArrowDirectionCommand;
import laser.ddg.commands.ShowAttributesCommand;
import laser.ddg.commands.ShowComputedFromValueCommand;
import laser.ddg.commands.ShowLegendMenuItem;
import laser.ddg.commands.ShowLineNumbersCommand;
import laser.ddg.commands.ShowScriptCommand;
import laser.ddg.commands.ShowValueDerivationCommand;
import laser.ddg.commands.SystemLookAndFeelCommand;
import laser.ddg.query.DerivationQuery;
import laser.ddg.query.Query;
import laser.ddg.query.QueryListener;
import laser.ddg.query.ResultsQuery;

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
	// Color of a tab label when the tab is selected.
	private static final Color SELECTED_TAB_COLOR = Color.GREEN;

	// The tabbed pane that holds the Home panel and the ddg panels.
	private static JTabbedPane tabbed;

	// The singleton
	private static DDGExplorer instance;
	
	public static final Preferences PREFERENCES = new Preferences();

	private static final Color MENU_COLOR = new Color(171, 171, 171);

	// Menu items that get enabled and disabled during execution
	private static JMenuItem saveDB;   // Enabled when a ddg is read from a file
	private JMenuItem attributesItem;  // Enabled on everything but the home panel
	private JMenuItem showScriptItem;  // Enabled on everything but the home panel

	private JCheckBoxMenuItem showLegendMenuItem;
	
	private static boolean loadingDDG = false;
	
	// Accumulates error messages while a ddg is being loaded.
	// Added to the corresponding DDG panel's error log when 
	// loading is complete.
	private static String errors = "";

	/**
	 * Initializes the DDG Explorer by loading the preference file and
	 * loading information about known languages.
	 */
	private DDGExplorer() {
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
		// e1.printStackTrace();
		// } catch (IllegalAccessException e1) {
		// e1.printStackTrace();
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }

	}

	/**
	 * @return the singleton instance
	 */
	public synchronized static DDGExplorer getInstance() {
		if (instance == null) {
			instance = new DDGExplorer();
		}
		return instance;
	}

	/**
	 * Create the GUI and show it. 
	 */
	public void createAndShowGUI() {
		setWindowTitle();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add search bar to top of DDG Explorer
		add(new SearchPanel(), BorderLayout.NORTH);

		// Add menu bar
		JMenuBar menuBar = createMenus();
		setJMenuBar(menuBar);

		// add tabbed pane
		tabbed = createTabbedPane();
		tabbed.addTab(" ", null, new HomePanel(), "Home Tab");
		add(tabbed, BorderLayout.CENTER);

		// set size based upon preferences, or default 
		setSize (PREFERENCES.getWindowSize());

		// modify size preferences when frame is resized
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Rectangle bounds = e.getComponent().getBounds();
				PREFERENCES.setWindowSize(bounds);
			}
		});

		// Display the window.
		setLocationRelativeTo(null);
		setVisible(true);
	}

        /**
        * Load look and feel based on user preference.
        * @param system
        */
        public void loadLookAndFeel(boolean system) {
            try{
                String lookAndFeel;
                if(system) {
                    lookAndFeel = UIManager.getSystemLookAndFeelClassName();                    
                }else{
                    lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
                }
                UIManager.setLookAndFeel( lookAndFeel );
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace(System.err);
            }
        }

	private JTabbedPane createTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane() {

			@Override
			public Color getBackgroundAt(int index) {
				if (getSelectedIndex() == index) {
					return SELECTED_TAB_COLOR;
				}
				return super.getBackgroundAt(index);
			}

		};
		tabbedPane.setOpaque(true);
		
		tabbedPane.addChangeListener((ChangeEvent e) -> {
                    Component openTab = getCurrentDDGPanel();
                    if (openTab instanceof laser.ddg.gui.DDGPanel) {
                        DDGPanel openDDGPanel = (DDGPanel) openTab;
                        enableDDGCommands();
                        SearchPanel.enableSearch();
                        if (PREFERENCES.isArrowDirectionDown()) {
                            openDDGPanel.setArrowDirectionDown();
                        }
                        else {
                            openDDGPanel.setArrowDirectionUp();
                        }
                        if (PREFERENCES.isShowLegend()) {
                            openDDGPanel.addLegend();
                        }
                        else {
                            openDDGPanel.removeLegend();
                        }
                        openDDGPanel.showLineNumbers(PREFERENCES.isShowLineNumbers());
                    } else {
                        disableDDGCommands();
                        SearchPanel.disableSearch();
                    }
                });
		
		return tabbedPane;
	}

	private void setWindowTitle() {
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
		} catch (IOException e1) { // Thomas: that's never used, was it the intent?
			title = "DDG Explorer";
		} finally {
			if (propResource != null) {
				try {
					propResource.close();
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
		}

		setTitle(title);
	}

	private JMenuBar createMenus() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(MENU_COLOR);

		JMenu fileMenu = createFileMenu();
		menuBar.add(fileMenu);
		
		JMenu ddgMenu = createDDGMenu();
		menuBar.add(ddgMenu);
		
		JMenu queryMenu = createQueryMenu();
		menuBar.add(queryMenu);
		
		JMenu prefMenu = createPreferencesMenu();
		menuBar.add(prefMenu);
		
		JMenu helpMenu = createHelpMenu();
		menuBar.add(helpMenu);
		return menuBar;
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


		//allow the user to compare 2 DDGs
		JMenuItem compareGraph = new JMenuItem("Compare DDGs");
		compareGraph.addActionListener(new CompareGraphsCommand());

		// allow the user to manage the database
		JMenuItem manageDB = new JMenuItem("Manage Database");
		manageDB.addActionListener(new ManageDatabaseCommand());

		// allow the user to quit ddg explorer
		JMenuItem quit = new JMenuItem("Quit");
		quit.addActionListener(new QuitCommand());

		fileMenu.add(openFile);
		fileMenu.add(openDB);
		fileMenu.add(saveDB);
		fileMenu.addSeparator();
		fileMenu.add(compareR);
		fileMenu.add(compareGraph);
		fileMenu.add(manageDB);
		fileMenu.add(quit);
		return fileMenu;
	}

	/**
	 * Create DDG Menu to be placed into the menuBar
	 * @return JMenu DDG menu
	 */
	private JMenu createDDGMenu() {
		final JMenu DDGMenu = new JMenu("DDG");
		DDGMenu.setBackground(MENU_COLOR);
		
		attributesItem = new JMenuItem("Show attributes");
		attributesItem.addActionListener(new ShowAttributesCommand());
		attributesItem.setEnabled(false);
		DDGMenu.add(attributesItem);
		
		showScriptItem = new JMenu("Show R script...");
		showScriptItem.addMouseListener(new ShowScriptCommand());
		showScriptItem.setEnabled(false);
		DDGMenu.add(showScriptItem);
		
		return DDGMenu;
	}

	private void enableDDGCommands() {
		saveDB.setEnabled(!getCurrentDDGPanel().alreadyInDB());
		attributesItem.setEnabled(true);
		showScriptItem.setEnabled(true);
	}

	private void disableDDGCommands() {
		saveDB.setEnabled(false);
		attributesItem.setEnabled(false);
		showScriptItem.setEnabled(false);
	}

	private static JMenu createQueryMenu() {
		final JMenu queryMenu = new JMenu("Query");
		queryMenu.setBackground(MENU_COLOR);
		
		JMenuItem findFilesItem = new JMenuItem("Find Data Files");
		findFilesItem.addActionListener(new FindFilesCommand());
		queryMenu.add(findFilesItem);
		
		JMenuItem timeItem = new JMenuItem("Display Execution Time of Operations"); 
		timeItem.addActionListener(new FindTimeCommand());
		queryMenu.add(timeItem); 
		
		final Query derivationQuery = new DerivationQuery();
		JMenuItem showValueDerivationItem = new JMenuItem(derivationQuery.getMenuItem());
		showValueDerivationItem.addActionListener(new ShowValueDerivationCommand());
		queryMenu.add(showValueDerivationItem);
		
		final Query computedFromQuery = new ResultsQuery();
		JMenuItem computedFromItem = new JMenuItem(computedFromQuery.getMenuItem());
		computedFromItem.addActionListener(new ShowComputedFromValueCommand());
		queryMenu.add(computedFromItem);
		
		return queryMenu;
	}

	/**
	 * Creates the menu with user preferences
	 * @return 
	 */
	private JMenu createPreferencesMenu() {
		JMenu prefMenu = new JMenu("Preferences");
		prefMenu.setBackground(MENU_COLOR);
		
		final JCheckBoxMenuItem arrowsDirectionMenuItem = new JCheckBoxMenuItem("Draw arrows from inputs to outputs", 
				PREFERENCES.isArrowDirectionDown());
		arrowsDirectionMenuItem.addActionListener(new SetArrowDirectionCommand());
		prefMenu.add(arrowsDirectionMenuItem);
		
		showLegendMenuItem = new JCheckBoxMenuItem("Show legend", 
				PREFERENCES.isShowLegend());
		showLegendMenuItem.addActionListener(new ShowLegendMenuItem());
		prefMenu.add(showLegendMenuItem);
		
		final JCheckBoxMenuItem showLineNumbersMenuItem = new JCheckBoxMenuItem("Show line numbers in node labels", 
				PREFERENCES.isShowLineNumbers());
		showLineNumbersMenuItem.addActionListener(new ShowLineNumbersCommand());
		prefMenu.add(showLineNumbersMenuItem);
		
		final JCheckBoxMenuItem useSystemLAFMenuItem = new JCheckBoxMenuItem("Use system Look and Feel", 
				PREFERENCES.isSystemLookAnFeel());
		useSystemLAFMenuItem.addActionListener(new SystemLookAndFeelCommand());
        prefMenu.add(useSystemLAFMenuItem);
		return prefMenu;
	}

	/**
	 * Create help menu
	 * @return 
	 */
	private static JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setBackground(MENU_COLOR);
		
		JMenuItem commandOverviewItem = new JMenuItem("Command overview");
		commandOverviewItem.addActionListener(new CommandOverviewCommand());
		helpMenu.add(commandOverviewItem);
		return helpMenu;
	}
	
	@Override
	public void queryFinished(String name, JComponent panel) {
		addTab(name, panel);
		doneLoadingDDG();
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
	 * @return the DDGPanel that the user is currently viewing.  Returns
	 *   null if the user is viewing the home panel (i.e., no DDG).
	 */
	public static DDGPanel getCurrentDDGPanel() {
		Component selectedTab = tabbed.getSelectedComponent();
		if (!(selectedTab instanceof DDGPanel)) {
			return null;
		}
		return (DDGPanel) selectedTab;
	}

	/**
	 * @return the ddg data that the user is currently viewing
	 */
	public ProvenanceData getCurrentDDG() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		return curDDGPanel.getProvData();
	}

	/**
	 * Change the arrow direction so that it points from inputs to outputs
	 * on the currently-viewed ddg.  Save this as the persistent value.
	 */
	public void setArrowDirectionDown() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			getCurrentDDGPanel().setArrowDirectionDown();
		}
		PREFERENCES.setArrowDirectionDown();
	}

	/**
	 * Change the arrow direction so that it points from outputs to inputs
	 * on the currently-viewed ddg.  Save this as the persistent value.
	 */
	public void setArrowDirectionUp() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			getCurrentDDGPanel().setArrowDirectionUp();
		}
		PREFERENCES.setArrowDirectionUp();
	}

	/**
	 * Change the way node labels are displayed.
	 * Save this as the persistent value.
	 * @param show if true line numbers are displayed
	 */
	public void showLineNumbers(boolean show) {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			getCurrentDDGPanel().showLineNumbers(show);
		}
		PREFERENCES.showLineNumbers(show);
	}

        public void useSystemLookAndFeel(boolean use){
            loadLookAndFeel(use);
            SwingUtilities.updateComponentTreeUI(this);
            PREFERENCES.useSystemLookAndFeel(use);
        }

	/**
	 * Show the legend.  Change the user's preferences to always show
	 * the legend.
	 */
	public void addLegend() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			curDDGPanel.addLegend();  
		}
		PREFERENCES.setShowLegend (true);
	}

	/**
	 * Remove the legend.  Change the user's preferences to never show the legend.
	 */
	public void removeLegend() {
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel != null) { 
			curDDGPanel.removeLegend();  
		}
		PREFERENCES.setShowLegend (false);
		if (showLegendMenuItem != null) {
			showLegendMenuItem.setSelected(false);
		}
	}
	
	public static void showErrMsg (String msg) {
		if (loadingDDG) {
			errors = errors + "\n" + msg;
			return;
		}
		DDGPanel curDDGPanel = getCurrentDDGPanel();
		if (curDDGPanel == null) {
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), msg);
		}
	}

	public static void loadingDDG() {
		loadingDDG = true;
		errors = "";
	}
	
	public static void doneLoadingDDG() {
		loadingDDG = false;
		getCurrentDDGPanel().showErrMsg(errors);
	}

	/**
	 * Main program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DDGExplorer explorer = DDGExplorer.getInstance();
			PREFERENCES.load();
            explorer.loadLookAndFeel(PREFERENCES.isSystemLookAnFeel());
			explorer.createAndShowGUI();
			if(args.length==1){
				LoadFileCommand.loadDDG(args[0]);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to start DDG Explorer: " + e.getMessage(),
					"Error starting DDG Explorer", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(System.err);
		}
	}


}
