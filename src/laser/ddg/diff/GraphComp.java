package laser.ddg.diff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;

import laser.ddg.gui.DBBrowser;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.ScriptBrowser;
import laser.ddg.persist.JenaLoader;
import laser.ddg.persist.JenaWriter;
import laser.ddg.persist.Parser;
import laser.ddg.visualizer.DDGDisplay;
import laser.ddg.visualizer.PrefuseGraphBuilder;
import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

/**
 * The panel used for the Explorer tabs that show differences between 2 DDGs
 * side-by-side with unified scrolling.
 *
 * @author Hafeezul Rahman
 * @version July 29, 2016
 *
 */
public class GraphComp extends JPanel {

	// Object that can load information from the database
	private JenaLoader jenaLoader;

	// The object used to load R scripts that are not in the database
	private static JFileChooser chooser;

	// The panel that shows the side-by-side files and their differences
	private DDGDiffPanel diffPanel = new DDGDiffPanel();

	// The file shown on the left side
	private File leftFile;

	// The file shown on the right side
	private File rightFile;

	// The button used to select the left file from the file system
	private JButton selectFile1Button = new JButton("Select from file");

	// The button used to select the left file from the database
	private JButton selectFromDB1Button = new JButton("Select from database");

	// The button used to select the right file from the file system
	private JButton selectFile2Button = new JButton("Select from file");

	// The button used to select the right file from the database
	private JButton selectFromDB2Button = new JButton("Select from database");

	// The field to display the left file name
	JTextField leftFileField = new JTextField();

	// The field to display the right file name
	JTextField rightFileField = new JTextField();

	// references for pop-ups
	private JFrame frame;

	// JPanel to hold the left and right file upload options.
	private JPanel northPanel = new JPanel();

	/**
	 * Create the window that allows the user to select files to compare and to
	 * see the results of the comparison
	 * 
	 * @param frame
	 * @param jenaLoader
	 *            the object that reads from the database
	 */
	public GraphComp(JFrame frame, JenaLoader jenaLoader) {
		super(new BorderLayout());
		this.frame = frame;
		this.jenaLoader = jenaLoader;

		JPanel leftPanel = createButtonPanel(selectFile1Button, selectFromDB1Button, leftFileField, "Left file");
		JPanel rightPanel = createButtonPanel(selectFile2Button, selectFromDB2Button, rightFileField, "Right file");

		northPanel.setLayout(new GridLayout(1, 2, 8, 0));
		northPanel.add(leftPanel);
		northPanel.add(rightPanel);
		add(northPanel, BorderLayout.NORTH);
		add(diffPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates a panel containing a button to select from a file, a button to
	 * select from a database, and a field to display the name of the selected
	 * file
	 * 
	 * @param selectFileButton
	 * @param selectDdgButton
	 * @param fileField
	 * @param panelLabel
	 * @return the panel constructed
	 */
	private JPanel createButtonPanel(JButton selectFileButton, JButton selectFromDBButton, JTextField fileField,
			String panelLabel) {
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JPanel topRow = new JPanel();
		selectFileButton.addActionListener((ActionEvent e) -> {
			try {
				selectFile(e.getSource());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(buttonPanel, "Unable to load file: " + e1.getMessage(),
						"Error loading file", JOptionPane.ERROR_MESSAGE);
			}
		});
		topRow.add(selectFileButton);

		selectFromDBButton.addActionListener((ActionEvent e) -> {
			try {
				selectFromDB(e.getSource());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(buttonPanel, "Unable to load file from the database: " + e1.getMessage(),
						"Error loading file from the database", JOptionPane.ERROR_MESSAGE);
			}
		});
		topRow.add(selectFromDBButton);
		buttonPanel.add(topRow);

		fileField.setEditable(false);
		buttonPanel.add(fileField);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(panelLabel));
		return buttonPanel;
	}

	/**
	 * Selects a file and displays its filename. If both files have been
	 * selected, the file contents are displayed and the diff is executed, with
	 * the results displayed.
	 * 
	 * @param button
	 *            the button clicked. We need this to determine if we are
	 *            setting the left or right file
	 */
	private void selectFile(Object button) {
		if (chooser == null) {
			chooser = new JFileChooser(System.getProperty("user.home"));
		}

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (button == selectFile1Button) {
				selectLeftFile(selectedFile, selectedFile.getAbsolutePath());
			} else {
				selectRightFile(selectedFile, selectedFile.getAbsolutePath());
			}
			if (leftFile != null && rightFile != null) {
				doDiff();
				// remove(northPanel);
			}
		}
	}

	/**
	 * Set the information for the right file
	 * 
	 * @param f
	 *            the file selected.
	 */
	private void selectRightFile(File f, String nameToDisplay) {
		if (f == null) {
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "Could not open " + f);
			return;
		}
		rightFile = f;
		rightFileField.setText(nameToDisplay);

		selectFile2Button.setEnabled(false);
		selectFromDB2Button.setEnabled(false);
	}

	/**
	 * Set the information for the left file
	 * 
	 * @param f
	 *            the file selected
	 */
	private void selectLeftFile(File f, String nameToDisplay) {
		if (f == null) {
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "Could not open " + f);
			return;
		}
		leftFile = f;
		leftFileField.setText(nameToDisplay);

		selectFile1Button.setEnabled(false);
		selectFromDB1Button.setEnabled(false);
	}

	/**
	 * Displays a DDG Browser that allows the user to select a file from the
	 * databsae. Displays its filename in the text field. If both files have
	 * been selected, the file contents are displayed and the diff is executed,
	 * with the results displayed.
	 * 
	 * @param button
	 *            the button clicked. We need this to determine if we are
	 *            setting the left or right file
	 */
	private void selectFromDB(final Object button) {
		final JDialog selectFrame = new JDialog(frame, "Select from Database", true);

		final DBBrowser browser = new ScriptBrowser(jenaLoader);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((ActionEvent e) -> {
			selectFrame.dispose();
		});

		JButton openButton = new JButton("Open");
		openButton.addActionListener((ActionEvent e) -> {
			String selectedScript = browser.getSelectedFile().getName();
			String timestamp = browser.getSelectedTimestamp();
			File selectedFile = new File(
					browser.getSelectedFile().getParent() + "/" + browser.getSelectedTimestamp() + "/ddg.txt");
			selectFrame.dispose();
			try {
				if (button == selectFromDB1Button) {
					selectLeftFile(selectedFile, selectedScript + " " + timestamp);
				} else {
					selectRightFile(selectedFile, selectedScript + " " + timestamp);
				}
				if (leftFile != null && rightFile != null) {
					doDiff();
					remove(northPanel);
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(selectFrame, "Unable to compare files: " + e1.getMessage(),
						"Error comparing files", JOptionPane.ERROR_MESSAGE);
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Border padding = BorderFactory.createEmptyBorder(0, 8, 8, 8);
		buttonPanel.setBorder(padding);
		buttonPanel.add(openButton);
		buttonPanel.add(cancelButton);

		selectFrame.add(browser, BorderLayout.CENTER);
		selectFrame.add(buttonPanel, BorderLayout.SOUTH);
		selectFrame.pack();
		selectFrame.setLocationRelativeTo(this);
		selectFrame.setVisible(true);
	}

	/**
	 * Run the diff algorithm on the nodes of the left ddg and the nodes of the
	 * right ddg. If a node is present in left ddg and missing in the right ddg,
	 * it appears in Red. If a node is present in right ddg and missing in the
	 * left ddg, it appears in Green. The rest of the nodes in the left and
	 * right ddgs are colored white.
	 */
	private void doDiff() {
		try {
			PrefuseGraphBuilder builderLeft = prepareForDiff("left_group", leftFile, "leftTemp.txt");
			PrefuseGraphBuilder builderRight = prepareForDiff("right_group", rightFile, "rightTemp.txt");

			computeDiffResult(builderLeft, builderRight);

			diffPanel.displayDiffResults(builderLeft, builderRight);

			deleteTempFile("leftTemp.txt");
			deleteTempFile("rightTemp.txt");
		} catch (Exception e) {
			// System.out.println("Exceptions caught" + e.getMessage());
		}

	}

	private static PrefuseGraphBuilder prepareForDiff(String copyGroupName, File selectedFile, String tempFileName)
			throws IOException {
		JenaWriter jenaWriter = JenaWriter.getInstance();

		PrefuseGraphBuilder builder = new PrefuseGraphBuilder(false, jenaWriter);

		DDGExplorer.loadingDDG();

		builder.createCopiedGroup(copyGroupName);

		String selectedFileName = selectedFile.getName();
		builder.processStarted(selectedFileName, true);

		Parser parser = new Parser(selectedFile, builder);
		parser.addNodesAndEdges();

		createFilesToDiff(tempFileName, parser, builder);

		return builder;
	}

	/**
	 * Creates a temporary file to store the list of all nodes(formatted) of the
	 * DDG. The file contains the procedural node names in order, with the node
	 * numbers removed and anything following a [ removed.
	 * 
	 * @param name
	 *            of the temporary file
	 * @param parser
	 * @param builder
	 *            corresponding to the graph
	 */
	private static void createFilesToDiff(String filename, Parser parser, PrefuseGraphBuilder builder) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8));

			for (int i = 1; i <= parser.getNumPins(); i++) {
				String curNodeName = builder.getName(builder.getNode(i));
				// System.out.println(curNodeName.replaceAll("\\s+", ""));
				// Remove whitespace
				String dummy = curNodeName.replaceAll("\\s+", "");

				// Remove the node number and any part of the name following a [
				if (dummy.indexOf("[") < 0) {
					String add = dummy.substring(dummy.indexOf('-') + 1);
					writer.write(add + "\n");
				} else {
					String add = dummy.substring(dummy.indexOf('-') + 1, dummy.indexOf('['));
					writer.write(add + "\n");
				}
			}
		} catch (IOException e1) {
			System.err.println(e1.getStackTrace());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e2) {
					// System.out.println("Exception caught in write close" +
					// e.getMessage());
				}
			}
			// System.out.println("File creation exception raised" +
			// e.getMessage());
		}
	}

	/**
	 * Computes the Unix diff result on the left and right DDGs and groups them.
	 * Groups are populated to color the nodes on the graph accordingly. Left
	 * Group consists of nodes which are uniquely present in the left DDG and
	 * colored in Red Right Group consists of nodes which are uniquely present
	 * in the right DDG and colored in Green
	 */
	private static void computeDiffResult(PrefuseGraphBuilder builderLeft, PrefuseGraphBuilder builderRight) {
		// TODO:  Look for ways to do this without a Unix command
		// https://code.google.com/p/google-diff-match-patch/wiki/API
		// http://introcs.cs.princeton.edu/java/96optimization/Diff.java.html
		String diffOutput = new ExecuteShellCommand().executeCommand("diff -y -w -b leftTemp.txt rightTemp.txt").trim();
		// System.out.println(diffOutput);
		String[] diffOutputArray = diffOutput.split("\n");
		int leftnode = 1, rightnode = 1;
		for (int i = 0; i < diffOutputArray.length; i++) {

			String currString = diffOutputArray[i].trim();
			String[] dummyString = currString.split("\\s+");
			// System.out.println(dummyString.length);
			if (dummyString.length == 3) {
				if (dummyString[1].equals("|")) {
					// System.out.println("added left and right
					// "+"leftnode:"+leftnode+" rightnode:"+rightnode);
					builderLeft.updateCopiedGroup(leftnode, "left_group");
					builderRight.updateCopiedGroup(rightnode, "right_group");
					leftnode = leftnode + 1;
					rightnode = rightnode + 1;
				}
			}
			if (dummyString.length == 2) {
				// System.out.println("length = 2");
				if (dummyString[0].equals(">")) {
					// System.out.println("added right node
					// "+"rightnode:"+rightnode);
					builderRight.updateCopiedGroup(rightnode, "right_group");
					rightnode = rightnode + 1;
				} else if (dummyString[1].equals("<")) {
					// System.out.println("added left node
					// "+"leftnode:"+leftnode);
					builderLeft.updateCopiedGroup(leftnode, "left_group");
					leftnode = leftnode + 1;
				} else {
					leftnode = leftnode + 1;
					rightnode = rightnode + 1;
				}
			}
		}
	}

	/**
	 * Delete the temporary files which were created to perform diff operation.
	 * 
	 * @param filenames
	 *            of the files to be deleted
	 */
	private static void deleteTempFile(String filename) {
		try {
			Path path = Paths.get(filename);
			Files.delete(path);
		} catch (Exception e) {
			// System.out.println(e.getMessage());
		}
	}
}

/**
 * Listen for clicks or drags in the overview, move the viewFinder accordingly
 */
class VfListener implements MouseInputListener {
	private DDGDisplay userDisplayLeft;
	private DDGDisplay overviewLeft;
	private DDGDisplay userDisplayRight;
	private DDGDisplay overviewRight;
	private boolean draggingRect;
	private Point prev;

	public VfListener(DDGDisplay userDisplayLeft, DDGDisplay overviewLeft, DDGDisplay userDisplayRight,
			DDGDisplay overviewRight) {
		super();
		this.userDisplayLeft = userDisplayLeft;
		this.overviewLeft = overviewLeft;
		this.userDisplayRight = userDisplayRight;
		this.overviewRight = overviewRight;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Rectangle viewFinderLeft = calcViewFinder(userDisplayLeft, overviewLeft);
		Rectangle viewFinderRight = calcViewFinder(userDisplayRight, overviewRight);
		if (viewFinderLeft.contains(e.getPoint()) || viewFinderRight.contains(e.getPoint())) {
			prev = e.getPoint();
			draggingRect = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// find where mouse was clicked on the Overview, transform it
		// out of the overview and onto the userDisplay. Then pan to that
		// location
		if (!draggingRect) {
			Point p = transPoint(e.getPoint());
			userDisplayLeft.animatePanTo(p, 1000);
			userDisplayRight.animatePanTo(p, 1000);
		} else {
			draggingRect = false; // reset draggingRect for next time.
		}
	}

	/**
	 * translate point from overview coordinates to userDisplay coordinates
	 * 
	 * @param p
	 *            Point in question
	 * @return transformed point
	 */
	private Point transPoint(Point p) {
		AffineTransform overTransI = overviewLeft.getInverseTransform();
		overTransI.transform(p, p);
		AffineTransform userTrans = userDisplayLeft.getTransform();
		userTrans.transform(p, p);
		return p;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (draggingRect) {
			Point p = transPoint(e.getPoint());
			prev = transPoint(prev);

			int xMovement = prev.x - p.x;
			int yMovement = prev.y - p.y;
			userDisplayLeft.animatePan(xMovement, yMovement, 1);
			userDisplayRight.animatePan(xMovement, yMovement, 1);
			prev = e.getPoint();
		}
	}

	public static Rectangle calcViewFinder(Display userDisplay, Display overview) {
		// retrieve width and height of the userDisplay's window on the screen
		Rectangle compBounds = userDisplay.getBounds();
		Point topLeft = new Point(0, (int) compBounds.getMinY());
		Point bottomRight = new Point((int) (compBounds.getMaxX() - compBounds.getMinX()), (int) compBounds.getMaxY());
		AffineTransform userTransI = userDisplay.getInverseTransform();
		userTransI.transform(topLeft, topLeft);
		userTransI.transform(bottomRight, bottomRight);
		AffineTransform overTrans = overview.getTransform();
		overTrans.transform(topLeft, topLeft);
		overTrans.transform(bottomRight, bottomRight);

		int x = topLeft.x;
		int y = topLeft.y;
		int width = bottomRight.x - x;
		int height = bottomRight.y - y;

		return new Rectangle(x, y, width, height);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}
}

/**
 * To animate the display when the Graph display is dragged with the mouse.
 */
class PanMyControl extends ControlAdapter {

	private boolean mPanOverItem;
	private int mXDown, mYDown;
	private int mButton;
	private Display displayLeft;
	private Display displayRight;

	/**
	 * Create a new PanControl.
	 */
	public PanMyControl(Display displayLeft, Display displayRight) {
		this(LEFT_MOUSE_BUTTON, false);
		this.displayLeft = displayLeft;
		this.displayRight = displayRight;
	}

	/**
	 * Create a new PanControl.
	 * 
	 * @param panOverItem
	 *            if true, the panning control will work even while the mouse is
	 *            over a visual item.
	 */
	public PanMyControl(boolean panOverItem) {
		this(LEFT_MOUSE_BUTTON, panOverItem);
	}

	/**
	 * Create a new PanControl.
	 * 
	 * @param mouseButton
	 *            the mouse button that should initiate a pan. One of
	 *            {@link Control#LEFT_MOUSE_BUTTON},
	 *            {@link Control#MIDDLE_MOUSE_BUTTON}, or
	 *            {@link Control#RIGHT_MOUSE_BUTTON}.
	 */
	public PanMyControl(int mouseButton) {
		this(mouseButton, false);
	}

	/**
	 * Create a new PanControl
	 * 
	 * @param mouseButton
	 *            the mouse button that should initiate a pan. One of
	 *            {@link Control#LEFT_MOUSE_BUTTON},
	 *            {@link Control#MIDDLE_MOUSE_BUTTON}, or
	 *            {@link Control#RIGHT_MOUSE_BUTTON}.
	 * @param panOverItem
	 *            if true, the panning control will work even while the mouse is
	 *            over a visual item.
	 */
	public PanMyControl(int mouseButton, boolean panOverItem) {
		mButton = mouseButton;
		mPanOverItem = panOverItem;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (UILib.isButtonPressed(e, mButton)) {
			e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			mXDown = e.getX();
			mYDown = e.getY();
		}
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (UILib.isButtonPressed(e, mButton)) {
			// Display display_left = (Display)e.getComponent();
			int x = e.getX(), y = e.getY();
			int dx = x - mXDown, dy = y - mYDown;
			displayLeft.pan(dx, dy);
			displayRight.pan(dx, dy);
			mXDown = x;
			mYDown = y;
			displayLeft.repaint();
			displayRight.repaint();
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (UILib.isButtonPressed(e, mButton)) {
			e.getComponent().setCursor(Cursor.getDefaultCursor());
			mXDown = -1;
			mYDown = -1;
		}
	}

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	@Override
	public void itemPressed(VisualItem item, MouseEvent e) {
		if (mPanOverItem) {
			mousePressed(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	@Override
	public void itemDragged(VisualItem item, MouseEvent e) {
		if (mPanOverItem) {
			mouseDragged(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	@Override
	public void itemReleased(VisualItem item, MouseEvent e) {
		if (mPanOverItem) {
			mouseReleased(e);
		}
	}
} // end of class PanMyControl

/**
 * Keeps track of bounds of left DDG and right DDG so that the corresponding
 * overviews will accommodate changes
 */
class FitOverviewListener implements ItemBoundsListener {
	private Rectangle2D mBounds = new Rectangle2D.Double();
	private Rectangle2D mTemp = new Rectangle2D.Double();
	private static final double M_D = 15;

	public FitOverviewListener() {
		super();
	}

	@Override
	public void itemBoundsChanged(Display displayGiven) {
		displayGiven.getItemBounds(mTemp);
		GraphicsLib.expand(mTemp, 25 / displayGiven.getScale());
		double dd = M_D / displayGiven.getScale();
		double xd = Math.abs(mTemp.getMinX() - mBounds.getMinX());
		double yd = Math.abs(mTemp.getMinY() - mBounds.getMinY());
		double wd = Math.abs(mTemp.getWidth() - mBounds.getWidth());
		double hd = Math.abs(mTemp.getHeight() - mBounds.getHeight());
		if (xd > dd || yd > dd || wd > dd || hd > dd) {
			mBounds.setFrame(mTemp);
			DisplayLib.fitViewToBounds(displayGiven, mBounds, 0);
		}
	}
}

/**
 * Draws viewFinder's borders onto the overview after paint is called for both
 * left and right ddgs.
 */
class VfBorders implements PaintListener {
	private DDGDisplay userDisplay;

	public VfBorders(DDGDisplay userDisplay) {
		super();
		this.userDisplay = userDisplay;
	}

	@Override
	public void prePaint(Display overview, Graphics2D g) {
	}

	@Override
	/**
	 * after both ddg displays have been drawn, create a rectangle in the
	 * overview that represents the regular display's view.
	 */
	public void postPaint(Display overview, Graphics2D g) {
		// retrieve rectangle for viewFinder
		Rectangle rect = calcViewFinder(userDisplay, overview);

		// draw the rectangle
		int x = rect.x;
		int y = rect.y;
		int width = rect.width;
		int height = rect.height;
		g.setColor(Color.LIGHT_GRAY);
		g.drawRoundRect(x, y, width, height, 10, 10);
		g.setColor(new Color(150, 150, 200, 50));
		g.fillRoundRect(x, y, width, height, 10, 10);
	}

	public static Rectangle calcViewFinder(Display userDisplay, Display overview) {
		Rectangle compBounds = userDisplay.getBounds();
		Point topLeft = new Point(0, (int) compBounds.getMinY()); // (int)compBounds.getMinX(),
																	// (int)compBounds.getMinY());
		Point bottomRight = new Point((int) (compBounds.getMaxX() - compBounds.getMinX()), (int) compBounds.getMaxY());
		AffineTransform userTransI = userDisplay.getInverseTransform();
		userTransI.transform(topLeft, topLeft);
		userTransI.transform(bottomRight, bottomRight);
		AffineTransform overTrans = overview.getTransform();
		overTrans.transform(topLeft, topLeft);
		overTrans.transform(bottomRight, bottomRight);

		int x = topLeft.x;
		int y = topLeft.y;
		int width = bottomRight.x - x;
		int height = bottomRight.y - y;

		return new Rectangle(x, y, width, height);
	}
}

/**
 * To update the display overview
 */
class UpdateOverview implements PaintListener {
	private DDGDisplay overview;

	public UpdateOverview(DDGDisplay overview) {
		super();
		this.overview = overview;
	}

	@Override
	public void prePaint(Display d, Graphics2D g) {
	}

	@Override
	public void postPaint(Display d, Graphics2D g) {
		overview.repaint();
	}
}

/**
 * To execute a shell level command in the Java Runtime.
 */
class ExecuteShellCommand {
	final JPanel errorPanel = new JPanel();

	public String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(errorPanel, "Unable to compare ddgs: Cygwin missing", "Error loading file",
					JOptionPane.ERROR_MESSAGE);
		}

		return output.toString();
	}
}
