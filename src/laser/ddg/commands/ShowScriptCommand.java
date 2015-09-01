package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	private static final DDGExplorer ddgExplorer = DDGExplorer.getInstance();

	@Override
	public void actionPerformed(ActionEvent e) {
		ProvenanceData curDDG = ddgExplorer.getCurrentDDG();
		String scriptFileName = curDDG.getScript();
		FileViewer fileViewer = new FileViewer(scriptFileName, "");
		fileViewer.displayFile();
	}

}
