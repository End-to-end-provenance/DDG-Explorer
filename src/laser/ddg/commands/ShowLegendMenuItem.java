package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import laser.ddg.gui.DDGExplorer;

/**
 * Command to display and remove the legend.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class ShowLegendMenuItem implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		JCheckBoxMenuItem showLegendMenuItem = (JCheckBoxMenuItem) e.getSource();
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();

		if (showLegendMenuItem.isSelected()) {
			ddgExplorer.addLegend();
		}
		else {
			ddgExplorer.removeLegend();
		}
	}

}
