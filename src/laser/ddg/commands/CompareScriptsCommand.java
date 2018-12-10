package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DiffTab;

/**
 * Command to load two R scripts and do a diff on them.
 * The panel in which this is done is a tab in the DDG Explorer.
 * 
 * @author Barbara Lerner
 * @version Aug 31, 2015
 *
 */
public class CompareScriptsCommand implements ActionListener {
	/**
	 * Creates the window that allows the user to compare R scripts used
	 * to create 2 different DDGs.
	 */
	private static void execute() {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		JPanel diffPanel = new DiffTab();
		/*diffFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		diffFrame.setSize(800,600);
		DialogUtilities.centerWindow(diffFrame, frame);	*/
		//new tab!
		ddgExplorer.addTab("Comparing Scripts", diffPanel);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			execute();
		} catch (Exception e) {
			DDGExplorer ddgExplorer = DDGExplorer.getInstance();
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to compare R scripts: " + e.getMessage(),
					"Error comparing R scripts",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}
