package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import laser.ddg.ProvenanceData;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.GraphComp;
import laser.ddg.persist.JenaLoader;

/**
 * Command to load two R scripts and do a diff on them.
 * The panel in which this is done is a tab in the DDG Explorer.
 * 
 * @author Barbara Lerner
 * @version Aug 31, 2015
 *
 */
public class CompareGraphsCommand implements ActionListener {
	/**
	 * Creates the window that allows the user to compare R scripts used
	 * to create 2 different DDGs.
	 */
	private static void execute() {
		JenaLoader jenaLoader = JenaLoader.getInstance();
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		JPanel diffPanel = new GraphComp(ddgExplorer, jenaLoader);
		/*diffFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		diffFrame.setSize(800,600);
		DialogUtilities.centerWindow(diffFrame, frame);	*/
		//new tab!
		ddgExplorer.addTab("Comparing DDGraphs", diffPanel);
		// ProvenanceData currentDDG = ddgExplorer.getCurrentDDG();
		// System.out.println(currentDDG.toString());
		//DDGExplorer.doneLoadingDDG();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			execute();
		} catch (Exception e) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to compare DDGs: ",
					"Error comparing DDGs",
					JOptionPane.ERROR_MESSAGE);
			//System.out.println();
		}
	}

}
