package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class ShowScriptCommand extends MouseAdapter {
	private ProvenanceData menuDDG;
	@Override
	public void mouseEntered(MouseEvent e) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ProvenanceData curDDG = ddgExplorer.getCurrentDDG();
		
		// If this is the first time the menu is used or if the
		// user is viewing a different ddg than the one that the
		// menu was created for, create a new menu.
		if (menuDDG == null || menuDDG != curDDG) {
			menuDDG = curDDG;
		
			JMenu scriptMenu = (JMenu) e.getSource();
			scriptMenu.removeAll();
			
			Collection<ScriptInfo> scripts= curDDG.scripts();
			for (ScriptInfo script : scripts) {
				JMenuItem scriptItem = new JMenuItem(script.toString());
				scriptItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						DDGExplorer.getCurrentDDGPanel().displaySourceCode(script);
					}
				});
				
				scriptMenu.add(scriptItem);
			}
		}
	}
		

//		ddgExplorer.getCurrentDDGPanel().displaySourceCode(0);
		
//		// TODO: Put up a menu to allow the user to select which script if there is more than one sourced
//		String scriptFileName = curDDG.getProcessName();
//		//System.out.println("ShowScriptCommand: scriptFileName = " + scriptFileName);
//		if (scriptFileName == null) {
//			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "There is no script available for " + curDDG.getProcessName());
//		}
//		else {
//			File scriptFile = new File (scriptFileName);
//			if (scriptFile.exists()) {
//				FileViewer fileViewer = new FileViewer(scriptFileName, "");
//				fileViewer.displayFile();
//			}
//			else {
//				JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "There is no script available for " + curDDG.getProcessName());
//			}				
//
//		}
//	}

}
