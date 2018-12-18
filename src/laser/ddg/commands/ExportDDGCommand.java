package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.freehep.graphicsbase.util.export.ExportDialog;

import laser.ddg.gui.DDGExplorer;

public class ExportDDGCommand implements ActionListener {
	private DDGExplorer frame;

	public ExportDDGCommand(DDGExplorer ddgExplorer) {
		frame = ddgExplorer;
	}

	@Override
    public void actionPerformed( ActionEvent e ) {
        ExportDialog export = new ExportDialog();
        export.showExportDialog( frame, "Export view as ...", DDGExplorer.getCurrentDDGPanel().getDDGDisplay(), "export" );
//	    Properties p = new Properties();
//	    p.setProperty("PageSize","A5");
//	    try {
//			DDGPanel currentDDGPanel = DDGExplorer.getCurrentDDGPanel();
//			DDGDisplay currentDDGDisplay = currentDDGPanel.getDDGDisplay();
//			//VectorGraphics g = new PSGraphics2D(new File("Output.eps"), currentDDGPanel.getSize()); 
//	    	VectorGraphics g = new PDFGraphics2D(new File("Output.pdf"), currentDDGDisplay.getSize()); 
//			g.setProperties(p); 
//			g.startExport(); 
//			currentDDGDisplay.print(g); 
//			g.endExport();
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			JOptionPane.showMessageDialog(frame, "Unable to create output file");
//		}

    }

}
