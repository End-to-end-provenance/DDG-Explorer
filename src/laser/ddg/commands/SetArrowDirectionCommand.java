package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import laser.ddg.gui.DDGExplorer;

/**
 * Command to change the direction of arrows. They can either go from inputs
 * towards outputs, or from outputs towards inputs.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 * 
 */
public class SetArrowDirectionCommand implements ActionListener {

	/**
	 * Sets the direction that arrows are drawn based on the setting of the
	 * corresponding menu item.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		setArrowDirection((JCheckBoxMenuItem) e.getSource());
	}

	/**
	 * Sets the directions that arrows point in the DDG based on the value of
	 * the checkbox.
	 * 
	 * @param inToOutMenuItem
	 */
	public static void setArrowDirection(final JCheckBoxMenuItem inToOutMenuItem) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();

		if (inToOutMenuItem.isSelected()) {
			ddgExplorer.setArrowDirectionDown();
		} else {
			ddgExplorer.setArrowDirectionUp();
		}
	}

}
