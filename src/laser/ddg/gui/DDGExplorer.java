package laser.ddg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
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
import laser.ddg.commands.CompareScriptsCommand;
import laser.ddg.commands.FindFilesCommand;
import laser.ddg.commands.LoadFileCommand;
import laser.ddg.commands.LoadFromDBCommand;
import laser.ddg.commands.ManageDatabaseCommand;
import laser.ddg.persist.DBWriter;
import laser.ddg.persist.FileUtil;
import laser.ddg.persist.JenaLoader;
import laser.ddg.persist.JenaWriter;
import laser.ddg.query.QueryListener;
import laser.ddg.visualizer.DDGPanel;
import laser.ddg.visualizer.ErrorLog;

/**
 * Class with a main program that allows the user to view DDGs previously stored in
 * a Jena database.  The user selects which execution of which process to see a DDG of.
 *
 * @author Barbara Lerner
 * @version Jul 25, 2012
 *
 */
public class DDGExplorer extends JFrame implements QueryListener {
	private static final Color MENU_COLOR = new Color(171,171,171);

	// An area where messages could be displayed.
	private ErrorLog log = ErrorLog.getInstance();

	// The process that the user selected.
	private String selectedProcessName;

	// The timestamp for the DDG that the user selected.
	private String selectedTimestamp;

	// The object that writes DDGs to the Jena database
	private static DBWriter jenaWriter = JenaWriter.getInstance();

	// The object that loads the DDG from a Jena database
	private static JenaLoader jenaLoader = JenaLoader.getInstance();

	private static JFrame frame;

	static JTabbedPane tabbed;

	private DBBrowser dbBrowser;

	//preferences on window size
	private static Hashtable<String,String> preferences = new Hashtable<String, String>();
	private static final File PREFERENCE_FILE = new File(FileUtil.DDG_DIRECTORY + "prefs.txt");

	// Color of a tab label when the tab is selected.
	private static final Color SELECTED_TAB_COLOR = Color.GREEN;

	// The singleton
	private static DDGExplorer instance;
	
	/**
	 * Creates the contents of the main GUI window.
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
		LanguageConfigurator.addLanguageBuilder("Little-JIL", "laser.juliette.ddgbuilder.DDGTextBuilder");

		// DON'T DELETE THIS:  Sample of how to load a query using reflection
//        try {
//			ClassLoader classLoader = getClass().getClassLoader();
//			Class queryClass = classLoader.loadClass("laser.juliette.ddg.gui.QDerivationQuery");
//			queryObj =
//			        (Query) queryClass.newInstance();
//		} catch (InstantiationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IllegalAccessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	public static DDGExplorer getInstance() {
		if (instance == null) {
			instance = new DDGExplorer();
		}
		return instance;
	}




	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setBackground(MENU_COLOR);

		//allow the user to load a DDG from a text file
		JMenuItem openFile = new JMenuItem("Open from File");
		openFile.addActionListener(new LoadFileCommand());
		
		//allow the user to load a DDG from the database
		JMenuItem openDB = new JMenuItem("Open from Database");
		openDB.addActionListener(new LoadFromDBCommand());

		//option to save to DB- for DDG tabs only
		JMenuItem saveDB = new JMenuItem("Save to Database");
		saveDB.setEnabled(false);

		//allow the user to compare two R scripts
		JMenuItem compareR = new JMenuItem("Compare R Scripts");
		compareR.addActionListener(new CompareScriptsCommand());
		
		//allow the user to look for a particular data file
		JMenuItem findFiles = new JMenuItem("Find Data Files");
		findFiles.addActionListener(new FindFilesCommand());
		
		//allow the user to manage the database
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





	@Override
	public void queryFinished(String name, JComponent panel) {
		addTab(name, panel);
	}






	public void addTab(String name, JComponent panel) {
		tabbed.addTab(name, panel);
		int tabNum = tabbed.getTabCount()-1;
		tabbed.setTabComponentAt(tabNum, new TabComp(tabbed, name));
		tabbed.setSelectedIndex(tabNum);
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		String title;
		Properties props = new Properties();

		try {
			InputStream propResource = DDGExplorer.class.getResourceAsStream("/.properties");
			if (propResource == null) {
				title = "DDG Explorer";
			}
			else {
				props.load(propResource);
				title = "DDG Explorer v." + props.getProperty("version");
			}
		} catch (IOException e1) {
			title = "DDG Explorer";
		}

		frame = new JFrame(title);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add content to the window.
		final DDGExplorer explorer = new DDGExplorer();

		//Add search bar to top of DDG Explorer
		frame.add(new SearchPanel(), BorderLayout.NORTH);

		//add tabbed pane
//		UIManager.put("TabbedPane.selected",Color.YELLOW);
//		UIManager.put("TabbedPane.tabAreaBackground",
//				Color.pink);
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
		tabbed.addChangeListener(new setupMenu(explorer));
		tabbed.addTab(" ", null, new HomePanel(), "Home Tab");
		frame.add(tabbed, BorderLayout.CENTER);

		//add log to bottom of frame
		JLabel logLabel = new JLabel("Error Log");
		JScrollPane logScrollPane = new JScrollPane(ErrorLog.getInstance());
			logScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			logScrollPane.setViewportBorder(BorderFactory.createLoweredBevelBorder());
			logScrollPane.setPreferredSize(new Dimension(logScrollPane.getPreferredSize().width, 80));
		JPanel logPanel = new JPanel(new BorderLayout());
			Border raised = BorderFactory.createRaisedBevelBorder();
			Border lowered = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			logPanel.setBorder(BorderFactory.createCompoundBorder(raised, lowered));
			logPanel.add(logLabel, BorderLayout.NORTH);
			logPanel.add(logScrollPane, BorderLayout.CENTER);
		frame.add(logPanel, BorderLayout.SOUTH);
		//.
		//Nikki is the best programmer! ~ Ariel & Bruce


		//edit preferences when frame is resized
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e){
				Rectangle bounds = e.getComponent().getBounds();
				preferences.put("WindowWidth", "" + (int) bounds.getWidth());
				preferences.put("WindowHeight", "" + (int) bounds.getHeight());
				try {
					savePreferences();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(explorer,
							"Unable to save preferences: " + e1.getMessage(),
							"Error saving preferences", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		//open size based upon preferences, or default 950x700
		if (preferences.containsKey("WindowWidth") && preferences.containsKey("WindowHeight")) {
			int preferredWidth = Integer.parseInt(preferences.get("WindowWidth"));
			int preferredHeight = Integer.parseInt(preferences.get("WindowHeight"));
			frame.setSize(preferredWidth, preferredHeight);
		}
		else {
			frame.setSize(950, 700);
		}
		// Display the window.
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * Loads user preferences from a file or sets to the default if
	 * there is no preference file.
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
	private static void savePreferences() throws Exception {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(PREFERENCE_FILE));
			out.println("# DDG Explorer preferences");
			for (String prefVar : preferences.keySet()) {
				out.println(prefVar + " = " + preferences.get(prefVar));
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Returns the main frame for the DDG Explorer application.  This allows other windows
	 * to display relative to the main window
	 * @return the main window
	 */
	public static JFrame mainFrame() {
		return frame;
	}

	/**
	 * Returns the tabbed frame for the DDG Explorer application.  This allows other windows
	 * to display onto the main window
	 * @return the tabbed frame window
	 */
	public static JTabbedPane tabbedPane() {
		return tabbed;
	}

	

	 static class setupMenu implements ChangeListener{
		 private JMenuBar menuBar;
		 private JMenu fileMenu;

		public setupMenu(DDGExplorer explorer) {
			fileMenu = explorer.createFileMenu();

			menuBar = new JMenuBar();
			menuBar.setBackground(MENU_COLOR);
			menuBar.add(explorer.createFileMenu());
			frame.setJMenuBar(menuBar);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			Component openTab = tabbed.getSelectedComponent();
			if (openTab instanceof laser.ddg.visualizer.DDGPanel){
				//System.out.println("DDG tab!");
				frame.setJMenuBar(((DDGPanel)openTab).createMenuBarDDG(fileMenu));
				((DDGPanel)openTab).validate();
	        }else{
	        	//System.out.println("not DDG" + menuBar.toString());
	        	frame.setJMenuBar(menuBar);
	        }
		}

	 }

	/**
	 * Test program to see that the GUI looks right.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			createAndShowGUI();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to start DDG Explorer: " + e.getMessage(),
					"Error starting DDG Explorer", JOptionPane.ERROR_MESSAGE);
		}
	}




	protected static DDGPanel getCurrentDDGPanel() {
		// TODO Auto-generated method stub
		return (DDGPanel)tabbed.getSelectedComponent();
	}







}
