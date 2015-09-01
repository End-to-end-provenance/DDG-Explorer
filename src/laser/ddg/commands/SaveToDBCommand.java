package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaWriter;

/**
 * Command to save the current DDG to the database.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class SaveToDBCommand implements ActionListener {

	private static final DDGExplorer ddgExplorer = DDGExplorer.getInstance();

	@Override
	public void actionPerformed(ActionEvent e) {
		JenaWriter jenaWriter = JenaWriter.getInstance();
		jenaWriter.persistDDG (ddgExplorer.getCurrentDDG());
	}
}
