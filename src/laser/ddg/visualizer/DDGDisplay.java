package laser.ddg.visualizer;

import java.awt.Color;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import laser.ddg.DataInstanceNode;
import laser.ddg.NoScriptFileException;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.SourcePos;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.data.tuple.TupleSet;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Displays a DDG using prefuse. Manages panning and zooming and things in the
 * right-click menu.
 * 
 * @author Antonia Miruna Oprescu
 * 
 */
public class DDGDisplay extends Display {

	// proportions for the position of the focus center
	private double proportionX = 0;
	private double proportionY = 0.25;

	// Builds the nodes and edges that comprise the graph
	private PrefuseGraphBuilder builder;

	private static final int FILE_CURRENT = 0;
	private static final int FILE_INCONSISTENT_WITH_DDG = 1;
	private static final int FILE_INCONSISTENT_WITH_DDG_CANCEL = -1;
	private static final int FILE_MISSING = -2;
	private static final String FUNCTION = "#ddg.function";

	/**
	 * Create a display for a prefuse DDG
	 * 
	 * @param builder
	 *            the object that is building the Prefuse graph of the ddg
	 */
	public DDGDisplay(PrefuseGraphBuilder builder) {
		this.builder = builder;
		this.setHighQuality(true); // higher quality rendering, aka anti-aliased
									// lines
	}

	/**
	 * Removes this window from the display.
	 */
	void closeWindow() {
		Window frame = (Window) SwingUtilities.getRoot(this);
		frame.dispose();
	}

	/**
	 * Focusing is used to adjust what is visible to the user. When the focus
	 * stops, we see the last focused node in the bottom center of the display
	 */
	void stopRefocusing() {
		// change the position of the focus center
		proportionX = 0;
		proportionY = -0.25;
	}

	public void zoomToFit() {
		if (!this.isTranformInProgress()) {
			int margin = 100;
			int duration = 1500; // 1.5 seconds
			Visualization vis = this.getVisualization();
			Rectangle2D bounds = vis.getBounds(Visualization.ALL_ITEMS);
			GraphicsLib.expand(bounds, margin + (int) (1 / this.getScale()));
			DisplayLib.fitViewToBounds(this, bounds, duration);
		}
	}

	public void zoomToExactFit() {
		if (!this.isTranformInProgress()) {
			Visualization vis = this.getVisualization();
			synchronized (vis) {
				// System.out.println("Fitting overview");
				Rectangle2D bounds = vis.getBounds(Visualization.ALL_ITEMS);
				GraphicsLib.expand(bounds, (int) (1 / this.getScale()));
				DisplayLib.fitViewToBounds(this, bounds, 0);
			}
		}
	}

	private void openFile(final NodeItem n) throws IOException {
		// Get timeStamp if one has been included
		String ddgTime = PrefuseUtils.getTimestamp(n);

		// Get the extension of the node's value
		String value = PrefuseUtils.getValue(n);
		String valueExt;
		if (value != null) {
			int index = value.lastIndexOf(".");
			valueExt = value.substring(index);
			// only works for .csv or .txt files now
			if (valueExt.equals(".csv") || valueExt.equals(".txt")) {
				// make sure it has the correct slashes in the path
				value = getOS(value);
				createFileFrame(value, ddgTime);
			} else if (valueExt.equals(".jpeg") || valueExt.equals(".png") || valueExt.equals(".gif")) {
				createPlotFrame(value, ddgTime);
			} else if (valueExt.equals(".RData")) {
				JOptionPane.showMessageDialog(DDGDisplay.this, "R Checkpoint file: " + value);
			} else { // if(valueExt.equals(".pdf") || valueExt.equals(".html")
						// || valueExt.equals(".htm"))
						// Should work for all kinds of files. Uses a
						// platform-specific
						// application.
				new FileViewer(value, ddgTime).displayFile();
			}
			// else {
			// JOptionPane.showMessageDialog(DDGDisplay.this,"This data does not
			// have an associated file");
			// }
		} else {
			JOptionPane.showMessageDialog(DDGDisplay.this, "This data does not have an associated file");
		}
	}

	/**
	 * Method that will read in a value file and display it as a table in a new
	 * frame but must have comma separated values and each row must begin on a
	 * new line with the first line containing the column names only
	 * 
	 * @param path
	 *            path of the file (either .csv or .txt)
	 * @param time
	 *            timestamp of the file given by the DDG
	 */
	private void createFileFrame(String path, String time) throws IOException {
		// Check the timestamps
		// assume no change if timestamp was never given
		int tChange = FILE_CURRENT;
		if (time != null) {
			tChange = timeChanged(path, time);
		}

		// Timestamp of file is not consistent with the ddg and the
		// user canceled the request to view the file
		if (tChange == FILE_INCONSISTENT_WITH_DDG_CANCEL || tChange == FILE_MISSING) {
			return;
		}

		FileViewer fileViewer = new FileViewer(path, time);
		if (tChange == FILE_INCONSISTENT_WITH_DDG) {
			// Add warning border if file is inconsistent with the ddg
			fileViewer.addBorder(Color.RED);
		}
		fileViewer.displayFile();

	}

	/**
	 * Create the frame that will display an image file as an ImageIcon in a new
	 * panel
	 * 
	 * @param path
	 *            path name that the image file is found or. Can be .jpeg, .gif,
	 *            .png or a URL
	 * @param time
	 *            timestamp of the plot given by the DDG
	 * @exception IOException if the image file cannot be read
	 */
	private void createPlotFrame(String path, String time) throws IOException {
		// Check the timestamps
		// assume no change if timestamp was never given
		int tChange = FILE_CURRENT;
		if (time != null) {
			tChange = timeChanged(path, time);
		}

		// Timestamp of file is not consistent with the ddg and the
		// user canceled the request to view the file
		if (tChange == FILE_INCONSISTENT_WITH_DDG_CANCEL || tChange == FILE_MISSING) {
			return;
		}

		FileViewer fileViewer = new FileViewer(path, time);
		if (tChange == FILE_INCONSISTENT_WITH_DDG) {
			// Add warning border if file is inconsistent with the ddg
			fileViewer.addBorder(Color.RED);
		}
		fileViewer.displayFile();
	}

	/**
	 * Function to check timestamps of files and plots against the time that
	 * they were created
	 * 
	 * @param path
	 *            the file/plot timestamp given by the system
	 * @param time
	 *            the timestamp associated with the file/plot given from the DDG
	 * @return returns FILE_INCONSISTENT_WITH_DDG(conflict but viewable),
	 *         FILE_CURRENT(no conflict) or FILE_INCONSISTENT_WITH_DDG_CANCEL
	 *         (conflict but don't view)
	 */
	private int timeChanged(String path, String time) {
		File file = new File(path);

		if (!file.exists()) {
			JOptionPane.showMessageDialog(this, "File " + path + " does not exist.");
			return FILE_MISSING;
		}

		// Determine the timeStamp of the file now
		long timeStamp = file.lastModified();
		Date fileTime = new Date(timeStamp);

		// make a date object out of the original timestamp so they can be
		// compared
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.sszzz");
		Date ddgTime;

		try {
			ddgTime = formatter.parse(time);

			// find difference between the dates, acceptable if not more than a
			// minute apart.
			long diff = Math.abs(fileTime.getTime() - ddgTime.getTime());
			if (diff <= 6000) {
				return FILE_CURRENT;
			}

			// Time on the file is after time stored in DDG
			int choice = JOptionPane.showConfirmDialog(DDGDisplay.this,
					"There is a conflict between the timestamps. File may be modified. Would you like to open the file anyway?",
					"File Timestamps Warning", JOptionPane.OK_CANCEL_OPTION);
			if (choice == JOptionPane.OK_OPTION) {
				// conflict but still show file
				return FILE_INCONSISTENT_WITH_DDG;
			} else {
				// conflict but do not open file
				return FILE_INCONSISTENT_WITH_DDG_CANCEL;
			}
		} catch (ParseException e) {
			DDGExplorer.showErrMsg("Error with parsing the DDG timestamp. " + e.getMessage());
			e.printStackTrace(System.err);
			return FILE_INCONSISTENT_WITH_DDG_CANCEL;
		}
	}

	/**
	 * Function that will determine whether the program is being run on a PC or
	 * MAC If windows, it will change all paths to reflect the syntax accepted
	 * by R.
	 * 
	 * @param path
	 *            entire path for the file to be displayed
	 * @return return the new path, changed only if on a Windows machine
	 */
	private static String getOS(String path) {
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows")) {
			// Replace all backslashes (\) with forward ones (/)
			return path.replace('\\', '/');
		}
		return path;
	}

	/**
	 * Method that will display the code of an R function in a JTextArea for
	 * procedure nodes or data nodes which contain FUNCTION values
	 * 
	 * @param leaf
	 *            leaf process node that holds the name of the function
	 * @throws NoScriptFileException if there is no script file associated with this node
	 */
	private void displayFunc(VisualItem leaf) throws NoScriptFileException {
		// Get the Data node for this leaf
		String leafName = PrefuseUtils.getName((NodeItem) leaf);		
		DataInstanceNode funcDin = builder.getDataNode (leafName);
		
		// Find the node that sets the function value
		ProcedureInstanceNode funcPin = funcDin.getProducer();

		// Find out where the function definition starts in the script
		SourcePos sourcePos = funcPin.getSourcePos();

		// Display the script highlighting the first line
		// of the function.
		DDGPanel curPanel = DDGExplorer.getCurrentDDGPanel();
		curPanel.displaySourceCode(sourcePos);
		
	}

	/**
	 * Allows the user to move to a desired portion of the DDG
	 */
	class AutoPanAction extends Action {
		private Point2D mCur = new Point2D.Double();
		private int xBias;
		private int yBias;

		@Override
		public void run(double frac) {
			TupleSet ts = m_vis.getFocusGroup(Visualization.FOCUS_ITEMS);
			if (ts.getTupleCount() == 0) {
				return;
			}

			xBias = (int) (getWidth() * proportionX);
			yBias = (int) (getHeight() * proportionY);
			VisualItem vi = (VisualItem) ts.tuples().next();
			assert mCur != null : "mCur is null";
			assert vi != null : "vi is null   tupleCount = " + ts.getTupleCount() + "   TupleSet type is "
					+ ts.getClass().getName();
			mCur.setLocation(vi.getX() + xBias, vi.getY() - yBias);
			panToAbs(mCur);
		}

	}

	class PopupMenu {

		private Point p = new Point();

		public void createPopupMenu() {
			// Add listener so the popup menu can come up.
			MouseListener popupListener = new PopupListener();
			addMouseListener(popupListener);
		}

		private void addMenuItem(JPopupMenu popup, PopupCommand option) {
			JMenuItem menuItem;
			menuItem = new JMenuItem(option.toString());
			menuItem.addActionListener(option);
			popup.add(menuItem);
		}

		abstract class PopupCommand implements ActionListener {
			private String command;

			public PopupCommand(String name) {
				command = name;
			}

			@Override
			public String toString() {
				return command;
			}

		}

		private PopupCommand expandCommand = new PopupCommand("Expand") {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualItem item = findItem(p);
				if (item instanceof NodeItem) {
					builder.handleNodeClick((NodeItem) item);
				}
			}
		};

		private PopupCommand expandAllCommand = new PopupCommand("Expand All") {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualItem item = findItem(p);
				if (item instanceof NodeItem) {
					NodeItem expandedNode = builder.expandRecursively((NodeItem) item);
					builder.layout(expandedNode);
				}
			}
		};

		private PopupCommand collapseCommand = new PopupCommand("Collapse") {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualItem item = findItem(p);
				if (item instanceof NodeItem && (PrefuseUtils.isStart(item) || PrefuseUtils.isFinish(item)
						|| PrefuseUtils.isRestoreNode((NodeItem) item))) {
					try {
						builder.handleNodeClick((NodeItem) item);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(builder.getPanel(), "Unable to collapse node: " + e1.getMessage(),
								"Error collapsing node", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};

		private PopupCommand showElapsedTimeCommand = new PopupCommand("Show Elapsed Execution Time") {

			@Override
			public void actionPerformed(ActionEvent e) {
				VisualItem item = findItem(p);
				String timestamp = PrefuseUtils.getTimestamp((NodeItem) item);
				if (timestamp != null) {
					JOptionPane.showMessageDialog(DDGDisplay.this, timestamp + " seconds");
				} else {
					JOptionPane.showMessageDialog(DDGDisplay.this,
							"There is no elapsed time associated with this node.");
				}
			}

		};

		private PopupCommand showFunctionCommand = new PopupCommand("Show Code") {


			@Override
			public void actionPerformed(ActionEvent e) {
				VisualItem item = findItem(p);
				SourcePos sourcePos;
				if (PrefuseUtils.isCollapsed(item)) {
					// Get the first member & its line number
					NodeItem firstMember = builder.getFirstMember(item);
					SourcePos firstSourcePos = PrefuseUtils.getSourcePos(firstMember);

					// Get the last member & its line number
					NodeItem lastMember = builder.getLastMember(item);
					SourcePos lastSourcePos = PrefuseUtils.getSourcePos(lastMember);

					sourcePos = new SourcePos(firstSourcePos.getScriptNumber(), 
							firstSourcePos.getStartLine(), firstSourcePos.getStartCol(),
							lastSourcePos.getEndLine(), lastSourcePos.getEndCol());
				} else {
					sourcePos = PrefuseUtils.getSourcePos((NodeItem)item);
				}
				try {
					displaySourceCode(sourcePos);
				} catch (NoScriptFileException e1) {
					JOptionPane.showMessageDialog(DDGExplorer.getInstance(), e1.getMessage());
				}
			}

			private void displaySourceCode(SourcePos sourcePos) throws NoScriptFileException {
				// display source code between those lines
				if (sourcePos == null || sourcePos.getStartLine() == -1) {
					JOptionPane.showMessageDialog(DDGDisplay.this,
							"There are no line numbers associated with this node.");
					return;
				}

				// Just read the file in one time.
				//System.out.println("scriptNum = " + scriptNum);
				//System.out.println("fileDisplayers.size() = " + fileDisplayers.size());

				builder.displaySourceCode(sourcePos);
			}

		};

		private PopupCommand showValueCommand = new PopupCommand("Show Value") {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualItem node = findItem(p);
				String time = null;
				// display time without the seconds
				if (PrefuseUtils.getTimestamp((NodeItem) node) != null) {
					time = PrefuseUtils.getTimestamp((NodeItem) node).substring(0, 16);
				}
				// if the node is a data node only show dialog boxes with values
				// and/or timestamps
				String nodeType = node.getString(PrefuseUtils.TYPE);
				if (nodeType.equals(PrefuseUtils.DATA_NODE) || nodeType.equals(PrefuseUtils.EXCEPTION)
						|| nodeType.equals(PrefuseUtils.CHECKPOINT_FILE)) {
					String valueClause = "";
					String timestampClause = "";
					String locationClause = "";

					String value = PrefuseUtils.getValue((NodeItem) node);
					if (value != null) {
						if (nodeType.equals(PrefuseUtils.DATA_NODE)) {
							valueClause = "Value = " + value + "\n";
						} else {
							valueClause = value + "\n";
						}
					}

					if (time != null) {
						timestampClause = "Timestamp = " + time + "\n";
					}

					String location = PrefuseUtils.getLocation((NodeItem) node);
					if (location != null) {
						locationClause = "Location = " + location + "\n";
					}

					if (value == null && time == null && location == null) {
						JOptionPane.showMessageDialog(DDGDisplay.this, "There is no information about this file.");
					} else if (value != null && value.equals(FUNCTION)) {
						try {
							displayFunc(node);
						} catch (NoScriptFileException e1) {
							JOptionPane.showMessageDialog(DDGDisplay.this, e1.getMessage());
						}
					} else {
						JOptionPane.showMessageDialog(DDGDisplay.this, valueClause + timestampClause + locationClause);
					}
				}

				// If the node is a URL type data node
				else if (nodeType.equals(PrefuseUtils.URL)
						|| (PrefuseUtils.isFile(node) && PrefuseUtils.getValue((NodeItem) node) != null
								&& (PrefuseUtils.getValue((NodeItem) node).endsWith(".html")
										|| PrefuseUtils.getValue((NodeItem) node).endsWith(".htm")))) {
					int choice = JOptionPane
							.showConfirmDialog(DDGDisplay.this,
									"The referenced URL is \n" + PrefuseUtils.getValue((NodeItem) node)
											+ "\nDo you want to view it?\n",
									"URL destination", JOptionPane.OK_CANCEL_OPTION);
					if (choice == JOptionPane.OK_OPTION) {
				    	try {
				    		new FileViewer(PrefuseUtils.getValue((NodeItem) node), null).displayFile();
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(DDGExplorer.getInstance(), 
									"Could not load the URL " + PrefuseUtils.getValue((NodeItem) node));
						}
					}
				}

				// If the node is a file node
				else if (PrefuseUtils.isFile(node)) {
					try {
						openFile((NodeItem) node);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(DDGExplorer.getInstance(), 
								"Could not find the file " + PrefuseUtils.getValue((NodeItem) node));
					}

				}
			}
		};

		private PopupCommand showMessageCommand = new PopupCommand("Show Message") {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualItem node = findItem(p);

				String value = PrefuseUtils.getValue((NodeItem) node);
				if (value == null) {
					JOptionPane.showMessageDialog(DDGDisplay.this, "There is no information about this file.");
				} else {
					JOptionPane.showMessageDialog(DDGDisplay.this, value);
				}
			}

		};

		class PopupListener extends MouseAdapter {

			@Override
			public void mousePressed(MouseEvent e) {
				showPopupMenu(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupMenu(e);
			}

			private void showPopupMenu(MouseEvent e) {
				if (e.isPopupTrigger()) {
					p = e.getPoint();
					VisualItem item = findItem(p);
					// popup menu comes up only if the node is type "Step" or
					// "Start"
					if (item == null || item instanceof prefuse.visual.tuple.TableEdgeItem) {
						return;
					}

					if (PrefuseUtils.isCollapsed(item)) {
						showPopup(e, expandCommand, expandAllCommand, showFunctionCommand,
								showElapsedTimeCommand/* , showLineNumberCommand */);
					}

					else if (PrefuseUtils.isStart(item) || PrefuseUtils.isFinish(item)) {
						showPopup(e, collapseCommand, expandAllCommand, showFunctionCommand,
								showElapsedTimeCommand/* , showLineNumberCommand */);
					}

					else if (PrefuseUtils.isException((NodeItem) item)) {
						showPopup(e, showMessageCommand);
					}

					else if (PrefuseUtils.isAnyDataNode((NodeItem) item)) {
						showPopup(e, showValueCommand);
					}

					else if (PrefuseUtils.isLeafNode((NodeItem) item)) {
						showPopup(e, showFunctionCommand,
								showElapsedTimeCommand/* , showLineNumberCommand */);
					}

					else if (PrefuseUtils.isRestoreNode((NodeItem) item)) {
						showPopup(e, collapseCommand);
					}

				}
			}

			private void showPopup(MouseEvent e, PopupCommand... commands) {
				JPopupMenu popup = new JPopupMenu();

				for (PopupCommand command : commands) {
					addMenuItem(popup, command);
				}

				// System.out.println("Showing popup menu.");
				// ((JMenuItem) (popup.getSubElements())[0]).setText(command);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}

		}

	}
}
