package laser.ddg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import laser.ddg.LanguageConfigurator;
import laser.ddg.ProvenanceData;
import laser.ddg.persist.DBWriter;
import laser.ddg.persist.FileUtil;
import laser.ddg.persist.JenaLoader;
import laser.ddg.persist.JenaWriter;
import laser.ddg.persist.Parser;
import laser.ddg.query.DerivationQuery;
import laser.ddg.query.FileUseQuery;
import laser.ddg.query.Query;
import laser.ddg.query.QueryListener;
import laser.ddg.query.ResultsQuery;
import laser.ddg.visualizer.DDGPanel;
import laser.ddg.visualizer.ErrorLog;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import laser.ddg.visualizer.DDGSearchGUI;

/**
 * Class with a main program that allows the user to view DDGs previously stored in
 * a Jena database.  The user selects which execution of which process to see a DDG of.
 *
 * @author Barbara Lerner
 * @version Jul 25, 2012
 *
 */
public class DDGExplorer extends JPanel implements QueryListener {
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

	private static JTabbedPane tabbed;

	private DBBrowser dbBrowser;

	//preferences on window size
	private static Hashtable<String,String> preferences = new Hashtable<String, String>();
	private static final File PREFERENCE_FILE = new File(FileUtil.DDG_DIRECTORY + "prefs.txt");

	// Color of a tab label when the tab is selected.
	private static final Color SELECTED_TAB_COLOR = Color.GREEN;

	public static final JFileChooser FILE_CHOOSER = new JFileChooser(System.getProperty("user.dir"));

	/**
	 * Creates the contents of the main GUI window.
	 */
	public DDGExplorer() {
		super(new BorderLayout());
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

		// Create a button to allow the user to load a DDG from a text file
		JButton loadFileButton = new JButton("Open from file");
		loadFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					loadFile();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to load the file: " + e.getMessage(),
							"Error loading file", JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		// Create a button to allow the user to load a DDG from the database
		JButton loadFromDBButton = new JButton("Open from database");
		loadFromDBButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					loadFromDB();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to load the DDG: " + e.getMessage(),
							"Error loading DDG", JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		// Create a button to allow the user to load a DDG from the database
		JButton compareButton = new JButton("Compare R Scripts");
		compareButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					compareRScripts();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to compare R scripts: " + e.getMessage(),
							"Error comparing R scripts", JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		// Create a button to allow the user to load a DDG from the database
		JButton findFilesButton = new JButton("Find Data Files");
		findFilesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					log.setText("");
					FileUseQuery query = new FileUseQuery();
					query.setFrameReferences(frame, tabbed);
					query.performQuery(jenaLoader, null, null, DDGExplorer.this);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to find data files: " + e.getMessage(),
							"Error finding data files", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// Create a button to allow the user to manage the database
				JButton manageButton = new JButton("Manage Database");
				manageButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						try {
							manageDB();
						} catch (Exception e) {
							JOptionPane.showMessageDialog(DDGExplorer.this,
									"Unable to manage the database: " + e.getMessage(),
									"Error managing the database", JOptionPane.ERROR_MESSAGE);
						}
					}

				});

		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); // use FlowLayout
		buttonPanel.add(loadFileButton);
		buttonPanel.add(loadFromDBButton);
		buttonPanel.add(compareButton);
		buttonPanel.add(findFilesButton);
		buttonPanel.add(manageButton);
		add(buttonPanel, BorderLayout.PAGE_START);

		JScrollPane logScrollPane = new JScrollPane(log);
		add(logScrollPane, BorderLayout.CENTER);
	}




	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setBackground(MENU_COLOR);

		//allow the user to load a DDG from a text file
		JMenuItem openFile = new JMenuItem("Open from File");
		openFile.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					loadFile();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to load the file: " + e.getMessage(),
							"Error loading file", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		//allow the user to load a DDG from the database
		JMenuItem openDB = new JMenuItem("Open from Database");
		openDB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					loadFromDB();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to load the DDG: " + e.getMessage(),
							"Error loading DDG", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		//option to save to DB- for DDG tabs only
		JMenuItem saveDB = new JMenuItem("Save to Database");
		saveDB.setEnabled(false);

		//allow the user to compare two R scripts
		JMenuItem compareR = new JMenuItem("Compare R Scripts");
		compareR.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					compareRScripts();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to compare R scripts: " + e.getMessage(),
							"Error comparing R scripts", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		//allow the user to look for a particular data file
		JMenuItem findFiles = new JMenuItem("Find Data Files");
		findFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					log.setText("");
					FileUseQuery query = new FileUseQuery();
					query.setFrameReferences(frame, tabbed);
					query.performQuery(jenaLoader, null, null, DDGExplorer.this);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to find data files: " + e.getMessage(),
							"Error finding data files", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		//allow the user to manage the database
		JMenuItem manageDB = new JMenuItem("Manage Database");
		manageDB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					manageDB();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to manage the database: " + e.getMessage(),
							"Error managing the database", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

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
	 * Creates the window that allows the user to compare R scripts used
	 * to create 2 different DDGs.
	 */
	private static void compareRScripts() {
		JPanel diffPanel = new DiffTab(frame, jenaLoader);
		/*diffFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		diffFrame.setSize(800,600);
		DialogUtilities.centerWindow(diffFrame, frame);	*/
		//new tab!
		tabbed.addTab("Comparing Scripts", diffPanel);
		int tabNum = tabbed.getTabCount()-1;
		tabbed.setTabComponentAt(tabNum, new TabComp(tabbed, diffPanel));
		tabbed.setSelectedIndex(tabNum);

	}

	/**
	 * Displays a window that allows the user to specify details about what they wish to load
	 * from the database.  On return, the window has been disposed.
	 */
	private void loadFromDB() {
		final JDialog loadFromDBFrame = new JDialog(frame, "Open from Database", true);
		loadFromDBFrame.setLocationRelativeTo(frame);

		// Create the buttons used to pick an action
		final JButton openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {
			/**
			 * Loads an entire DDG
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					log.setText("");
					loadFromDBFrame.dispose();
					loadDDGFromDB();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to load the DDG: " + e1.getMessage(),
							"Error loading DDG", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			/**
			 * Remove the window on cancel.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				loadFromDBFrame.dispose();
			}
		});

		// Build the GUI layout
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Border padding = BorderFactory.createEmptyBorder(0, 8, 8, 8);
		buttonPanel.setBorder(padding);
		openButton.setEnabled(false);
		buttonPanel.add(openButton);
		buttonPanel.add(cancelButton);

		dbBrowser = new DDGBrowser(jenaLoader);
		dbBrowser.addDBBrowserListener(new DBBrowserListener() {
			@Override
			public void scriptSelected(String script) {
				selectedProcessName = script;
			}

			@Override
			public void timestampSelected(String timestamp) {
				selectedTimestamp = timestamp;
				openButton.setEnabled(true);
			}
		});

		loadFromDBFrame.add(dbBrowser, BorderLayout.CENTER);
		loadFromDBFrame.add(buttonPanel, BorderLayout.SOUTH);
		loadFromDBFrame.pack();
		loadFromDBFrame.setVisible(true);
	}

	/**
	 * Displays a window that allows the user load and removed DDGs, and
	 * show Values. On return, the window has been disposed.
	 */
	private void manageDB() {
		final JDialog loadFromDBFrame = new JDialog(frame, "Manage Database", true);
		loadFromDBFrame.setLocationRelativeTo(frame);

		// Create the buttons used to pick an action
		final JButton deleteAllButton = new JButton("Delete all");
		final JButton deleteOneButton = new JButton("Delete DDG");
		final JButton openButton = new JButton("Open DDG");
		final Query derivationQuery = new DerivationQuery();
		final JButton derivationQueryButton = new JButton(derivationQuery.getMenuItem());
		final Query computedFromQuery = new ResultsQuery();
		final JButton computedFromQueryButton = new JButton(computedFromQuery.getMenuItem());
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			/**
			 * Remove the window on cancel.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				loadFromDBFrame.dispose();
			}

		});

		openButton.addActionListener(new ActionListener() {

			/**
			 * Loads an entire DDG
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					log.setText("");
					loadFromDBFrame.dispose();
					loadDDGFromDB();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to load the DDG: " + e1.getMessage(),
							"Error loading DDG", JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		derivationQueryButton.addActionListener(new ActionListener() {
			/**
			 * Loads the portion of a DDG that shows how a data value has been computed
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					log.setText("");
					loadFromDBFrame.dispose();
					derivationQuery.performQuery(jenaLoader, selectedProcessName, selectedTimestamp, DDGExplorer.this);
					derivationQuery.addQueryListener(DDGExplorer.this);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to perform the query: " + e1.getMessage(),
							"Error performing query", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		computedFromQueryButton.addActionListener(new ActionListener() {
			/**
			 * Loads the portion of a DDG that shows what has been computed from a data value.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					log.setText("");
					loadFromDBFrame.dispose();
					computedFromQuery.performQuery(jenaLoader, selectedProcessName, selectedTimestamp, DDGExplorer.this);
					computedFromQuery.addQueryListener(DDGExplorer.this);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(DDGExplorer.this,
							"Unable to perform the query: " + e1.getMessage(),
							"Error performing query", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		deleteOneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(loadFromDBFrame,
						"Are you sure that you want to delete this DDG permanently from the database?") == JOptionPane.YES_OPTION) {
					try {
						deleteDDG(selectedProcessName, selectedTimestamp);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(DDGExplorer.this,
								"Unable to delete the DDG: " + e1.getMessage(),
								"Error deleting DDG", JOptionPane.ERROR_MESSAGE);
					}
					// loadFromDBFrame.dispose();
					dbBrowser.removeTimestamp(selectedTimestamp);
					deleteOneButton.setEnabled(false);
					deleteAllButton.setEnabled(true);
					openButton.setEnabled(false);
					derivationQueryButton.setEnabled(false);
					computedFromQueryButton.setEnabled(false);
				}
			}

		});

		deleteAllButton.addActionListener (new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(loadFromDBFrame,
						"Are you sure that you want to delete *ALL* DDGs with this name permanently from the database?") == JOptionPane.YES_OPTION) {
					try {
						deleteAll(dbBrowser.getDisplayedTimestamps());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(DDGExplorer.this,
								"Unable to delete all DDGs: " + e1.getMessage(),
								"Error deleting DDGs", JOptionPane.ERROR_MESSAGE);
					}
					// loadFromDBFrame.dispose();
					dbBrowser.clearTimestamps();
					dbBrowser.removeProcess(selectedProcessName);
				}
			}
		});

		// Build the GUI layout
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		deleteAllButton.setEnabled(false);
		deleteOneButton.setEnabled(false);
		openButton.setEnabled(false);
		derivationQueryButton.setEnabled(false);
		computedFromQueryButton.setEnabled(false);
		buttonPanel.add(deleteAllButton);
		buttonPanel.add(deleteOneButton);
		buttonPanel.add(openButton);
		buttonPanel.add(derivationQueryButton);
		buttonPanel.add(computedFromQueryButton);
		buttonPanel.add(cancelButton);

		dbBrowser = new DDGBrowser(jenaLoader);
		dbBrowser.addDBBrowserListener(new DBBrowserListener() {
			@Override
			public void scriptSelected(String script) {
				selectedProcessName = script;
				deleteAllButton.setEnabled(true);
			}

			@Override
			public void timestampSelected(String timestamp) {
				selectedTimestamp = timestamp;
				deleteAllButton.setEnabled(false);
				deleteOneButton.setEnabled(true);
				openButton.setEnabled(true);
				derivationQueryButton.setEnabled(true);
				computedFromQueryButton.setEnabled(true);
			}
		});

		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BoxLayout (selectionPanel, BoxLayout.X_AXIS));
		selectionPanel.add(dbBrowser);
		selectionPanel.add(buttonPanel);

		loadFromDBFrame.add(selectionPanel);
		loadFromDBFrame.setMinimumSize(new Dimension(800, 400));
		loadFromDBFrame.setVisible(true);
	}

	/**
	 * Load the selected DDG from the database and display it.
	 */
	private void loadDDGFromDB() {
		loadDDGFromDB(selectedProcessName, selectedTimestamp);
	}

	/**
	 * Loads a ddg from the database and creates the visual graph
	 * @param processName the name of the process executed
	 * @param timestamp the time the ddg was created
	 * @return the object that builds the visual graph
	 */
	public static PrefuseGraphBuilder loadDDGFromDB (String processName, String timestamp) {
		final ProvenanceData provData = new ProvenanceData(processName);
		final PrefuseGraphBuilder graphBuilder = new PrefuseGraphBuilder(false, jenaWriter);
		graphBuilder.setProvData(provData);
		graphBuilder.setTitle(processName, timestamp);
		provData.addProvenanceListener(graphBuilder);
		provData.setQuery("Entire DDG");
		jenaLoader.loadDDG(processName, timestamp, provData);
		graphBuilder.createLegend(provData.getLanguage());
		addTab(graphBuilder.getPanel().getName(), graphBuilder.getPanel());
		return graphBuilder;
	}

	@Override
	public void queryFinished(String name, JComponent panel) {
		addTab(name, panel);
	}

	/**
	 * Delete all the DDGs in the database for the selected process name.
	 * @param listModel all of the timestamps associated with the selected process
	 */
	private void deleteAll(List<String> timestamps) {
		for (String timestamp : timestamps) {
			deleteDDG(selectedProcessName, timestamp);
		}
	}

	/**
	 * Delete the selected DDG from the database.
	 * @param timestamp
	 * @param processName
	 */
	private void deleteDDG(String processName, String timestamp) {
		try {
			FileUtil.deleteFiles (processName, timestamp);
		} catch (IOException e) {
			log.append("Could not delete saved files\n");
		}
		jenaLoader.deleteDDG(processName, timestamp);
	}

	/**
	 * Loads a text file containing a ddg
	 * @param fileChooser the file chooser object
	 */
	private void loadFile() throws Exception {
		if (FILE_CHOOSER.showOpenDialog(DDGExplorer.this) == JFileChooser.APPROVE_OPTION) {
			log.setText("");
			PrefuseGraphBuilder builder = new PrefuseGraphBuilder(false, jenaWriter);
			File selectedFile = FILE_CHOOSER.getSelectedFile();
			String selectedFileName = selectedFile.getName();
			builder.processStarted(selectedFileName, null);
			Parser parser = new Parser(selectedFile, builder);
			parser.addNodesAndEdges();

			//new tab!
			addTab(builder.getPanel().getName(), builder.getPanel());
		}
	}




	private static void addTab(String name, JComponent panel) {
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
		frame.add(new SetupSearch(explorer), BorderLayout.NORTH);

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
		tabbed.addTab(" ", null, explorer, "Home Tab");
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

	

	 static class SetupSearch extends JPanel{
			private JTextField searchField;
			private JComboBox<String> optionsBox, databaseOptionsBox, ddgOptionsBox;
			private String ddgOption;
			
			public SetupSearch(DDGExplorer mainFrame){
				searchUI(mainFrame);
			}

			private void searchUI(DDGExplorer mainFrame){
				searchField = new JTextField("Search");
				JButton advancedSearchButton = new JButton("Advanced Search");
				
				String[] options = {"Current DDG", "R Script", "Database"};
				String[] databaseOptions = {"DDGs", "R Scripts"};
				String[] ddgOptions = {"Error", "Data", "File", "URL", "Function", "All Options"};
				
				optionsBox = new JComboBox<>(options);
				databaseOptionsBox = new JComboBox<>(databaseOptions);
				ddgOptionsBox = new JComboBox<>(ddgOptions);

				
				ddgOption = ddgOptions[0];
				
				JLabel optionsDisplay = new JLabel(options[0]);
				JLabel databaseOptionsDisplay = new JLabel(databaseOptions[0]);
				JLabel ddgOptionsDisplay = new JLabel(ddgOptions[0]);

				setLayout(new GridBagLayout());

				GridBagConstraints preferences = new GridBagConstraints();
				preferences.fill = GridBagConstraints.BOTH;

				preferences.weightx = 0.0;
				preferences.weighty = 0.0;
				preferences.gridx = 0;
				preferences.gridy = 0;
				add(optionsBox, preferences);

				preferences.weightx = 0.0;
				preferences.weighty = 0.0;
				preferences.gridx = 1;
				preferences.gridy = 0;
				add(ddgOptionsBox, preferences);
				
				preferences.weightx = 0.5;
				preferences.gridx = 2;
				preferences.gridy = 0;
				add(searchField, preferences);

				preferences.weightx = 0.0;
				preferences.gridx = 3;
				preferences.gridy = 0;
				add(advancedSearchButton, preferences);
			
				//Changes text in search feild in response to the selected ddgOptions box 
				ddgOptionsBox.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent select){
						searchField.setText("Search for " + ddgOptionsBox.getSelectedItem().toString());
						ddgOption = ddgOptionsBox.getSelectedItem().toString();
					}
				});				
				
				advancedSearchButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						DDGSearchGUI searchList;
						DDGPanel panel = (DDGPanel)mainFrame.tabbed.getSelectedComponent();
						PrefuseGraphBuilder build = panel.getBuilder();
						
						boolean isText; 
						String searchFieldText = searchField.getText().toLowerCase();

						//checks if information was entered into the search field
						if(searchFieldText.isEmpty())	
							isText = false;
						else if(searchFieldText.length() < 6)
							isText = true;
						else if(searchFieldText.substring(0, 6).equals("search"))
							isText = false;
						else
							isText = true;	
						
						if(ddgOption.equals("Error"))
							mainFrame.getCurrentDDGPanel().SearchList(build.getErrorList(), isText, searchFieldText);
						else if(ddgOption.equals("Data"))
							mainFrame.getCurrentDDGPanel().SearchList(build.getDataList(), isText, searchFieldText);
						else if(ddgOption.equals("File"))
							mainFrame.getCurrentDDGPanel().SearchList(build.getFileList(), isText, searchFieldText);
						else if(ddgOption.equals("URL"))
							mainFrame.getCurrentDDGPanel().SearchList(build.getURLList(), isText, searchFieldText);
						else if(ddgOption.equals("Function"))
							mainFrame.getCurrentDDGPanel().SearchList(build.getOperationList(), isText, searchFieldText);
						else
							mainFrame.getCurrentDDGPanel().SearchList(build.getAllList(), isText, searchFieldText);
					}
				});
				
			}
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




	protected DDGPanel getCurrentDDGPanel() {
		// TODO Auto-generated method stub
		return (DDGPanel)tabbed.getSelectedComponent();
	}




}
