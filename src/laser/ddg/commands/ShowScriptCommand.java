package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;

import laser.ddg.ProvenanceData;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.visualizer.FileViewer;

/**
 * Command to display the R script associated with the current DDG.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class ShowScriptCommand implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ProvenanceData curDDG = ddgExplorer.getCurrentDDG();
		
		// TODO: Put up a menu to allow the user to select which script if there is more than one sourced
		String scriptFileName = curDDG.getScript(0);
		//System.out.println("ShowScriptCommand: scriptFileName = " + scriptFileName);
		if (scriptFileName == null) {
			JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "There is no script available for " + curDDG.getProcessName());
		}
		else {
			File scriptFile = new File (scriptFileName);
			if (scriptFile.exists()) {
				FileViewer fileViewer = new FileViewer(scriptFileName, "");
				fileViewer.displayFile();
			}
			else {
				JOptionPane.showMessageDialog(DDGExplorer.getInstance(), "There is no script available for " + curDDG.getProcessName());
			}				

		}
	}

}
