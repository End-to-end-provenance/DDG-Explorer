package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import laser.ddg.diff.gui.DDGDiffTab;
import laser.ddg.gui.DDGExplorer;

/**
 * Command to load two DDGs and display the comparison result
 * The panel in which this is done is a tab in the DDG Explorer.
 * 
 * @author Hafeezul Rahman
 * @version June 28, 2016
 *
 */
public class CompareGraphsCommand implements ActionListener {
	/**
	 * Creates the window that allows the user to compare 2 DDGs
	 */
	private static void execute() {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		JPanel diffPanel = new DDGDiffTab();
		ddgExplorer.addTab("Comparing DDGs", diffPanel);
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
