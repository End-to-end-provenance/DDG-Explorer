package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import laser.ddg.gui.DDGExplorer;

/**
 * Command to display a command overview help message to the user.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class CommandOverviewCommand implements ActionListener {
	private String help;

	@Override
	public void actionPerformed(ActionEvent e) {
		createCommandOverviewFrame();
	}

	/**
	 * Create a pop-up window with command summary
	 */
	private void createCommandOverviewFrame() {
		if (help == null) {
			help = createHelpText();
		}

		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		JOptionPane.showMessageDialog(ddgExplorer, help, "Command Overview", JOptionPane.PLAIN_MESSAGE);
	}

	private static String createHelpText() {
		StringBuilder helpBuffer = new StringBuilder();
		
		helpBuffer.append("To collapse a section of the graph\n");
		helpBuffer.append("   Left click on a green start or finish node.\n\n");

		helpBuffer.append("To expand a collapsed node\n");
		helpBuffer.append("   Left click on a light blue node.\n\n");

		helpBuffer.append("To move a node\n");
		helpBuffer.append("   Drag the node\n\n");

		helpBuffer.append("To scroll to a different portion of the graph\n");
		helpBuffer.append("   Drag the overview box OR\n");
		helpBuffer.append("   Drag on the background\n\n");

		helpBuffer.append("To re-center the graph\n");
		helpBuffer.append("   Click the Refocus button\n\n");

		helpBuffer.append("To change the magnification\n");
		helpBuffer.append("   Use the slider at the top of the window\n\n");
		return helpBuffer.toString();
	}


}
