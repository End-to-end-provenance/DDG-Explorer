package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;
import laser.ddg.gui.WorkflowPanel;
import laser.ddg.search.OperationSearchElement;

public class FindTimeCommand implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {

		ArrayList<OperationSearchElement> nodeList;
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		WorkflowPanel wfpanel = DDGExplorer.getCurrentWorkflowPanel();
		if (panel != null) {
		nodeList = 
				(ArrayList<OperationSearchElement>) panel.getSearchIndex().getOperationList().clone();
		} else {
			wfpanel = DDGExplorer.getCurrentWorkflowPanel();
			nodeList = 
					(ArrayList<OperationSearchElement>) wfpanel.getSearchIndex().getOperationList().clone();
		}

		// order nodelist and show in GUI.
		Collections.sort(nodeList, (OperationSearchElement p1, OperationSearchElement p2) -> {
                    if (p2.getTimeTaken() < p1.getTimeTaken()) {
                        return -1;
                    } else if (p2.getTimeTaken() > p2.getTimeTaken()) {
                        return 1;
                    }
                    return 0;
                });

		if (panel != null) {
			panel.showSearchResults(nodeList);
		} else {
			wfpanel.showSearchResults(nodeList);
		}

	}

}
