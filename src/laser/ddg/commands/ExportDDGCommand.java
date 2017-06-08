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
        export.showExportDialog( frame, "Export view as ...", DDGExplorer.getCurrentDDGPanel(), "export" );
//	    Properties p = new Properties();
//	    p.setProperty("PageSize","A5");
//	    try {
//			//VectorGraphics g = new PSGraphics2D(new File("Output.eps"), new Dimension(400,300)); 
//	    	VectorGraphics g = new PDFGraphics2D(new File("Output.eps"), new Dimension(400,300)); 
//			g.setProperties(p); 
//			g.startExport(); 
//			DDGExplorer.getCurrentDDGPanel().print(g); 
//			g.endExport();
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			JOptionPane.showMessageDialog(frame, "Unable to create output file");
//		}

    }

}
