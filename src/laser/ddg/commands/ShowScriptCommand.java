package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import laser.ddg.ProvenanceData;
import laser.ddg.ScriptInfo;
import laser.ddg.gui.DDGExplorer;

/**
 * Command to display the R script associated with the current DDG.
 * The user is shown a menu with the name of the main script and all
 * scripts that were included with R's source function.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class ShowScriptCommand extends MouseAdapter {
	// The DDG that the menu is for.  The menu varies by
	// DDG since each DDG likely uses different scripts.
	private ProvenanceData menuDDG;
	
	/**
	 * Creates the menu of scripts for the current ddg
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// Find out what ddg the user is currently viewing
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ProvenanceData curDDG = ddgExplorer.getCurrentDDG();
		
		// If this is the first time the menu is used or if the
		// user is viewing a different ddg than the one that the
		// menu was created for, create a new menu.
		if (menuDDG == null || menuDDG != curDDG) {
			menuDDG = curDDG;
		
			// Remove the previous entries
			JMenu scriptMenu = (JMenu) e.getSource();
			scriptMenu.removeAll();
			
			// Create the new entries
			Collection<ScriptInfo> scripts= curDDG.scripts();
			for (ScriptInfo script : scripts) {
				JMenuItem scriptItem = new JMenuItem(script.toString());
				scriptItem.addActionListener((ActionEvent e2) -> {
						DDGExplorer.getCurrentDDGPanel().displaySourceCode(script);
					}
				);
				
				scriptMenu.add(scriptItem);
			}
		}
	}
}
