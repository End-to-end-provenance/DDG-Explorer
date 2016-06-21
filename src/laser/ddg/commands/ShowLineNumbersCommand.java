package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import laser.ddg.gui.DDGExplorer;

/**
 * Command to control whether line numbers are shown as part of node names
 * 
 * @author Barbara Lerner
 * @version May 23, 2016
 * 
 */
public class ShowLineNumbersCommand implements ActionListener {

	/**
	 * Sets whether line numbers are shown based on the setting of the
	 * corresponding menu item.
         * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		showLineNumbers((JCheckBoxMenuItem) e.getSource());
	}

	/**
	 * Sets whether line numbers are displayed in node names
	 * 
	 * @param lineNumberMenuItem
	 */
	public static void showLineNumbers(final JCheckBoxMenuItem lineNumberMenuItem) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();

		ddgExplorer.showLineNumbers(lineNumberMenuItem.isSelected());
	}

}
