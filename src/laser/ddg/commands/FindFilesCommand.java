package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaLoader;
import laser.ddg.query.FileUseQuery;

/**
 * Command to find files in the database.
 * 
 * @author Barbara Lerner
 * @version Aug 31, 2015
 *
 */
public class FindFilesCommand implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent arg0) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();

		try {
			JenaLoader jenaLoader = JenaLoader.getInstance();
			FileUseQuery query = new FileUseQuery();
			query.performQuery(jenaLoader, null, null, ddgExplorer);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to find data files: " + e.getMessage(),
					"Error finding data files",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}
