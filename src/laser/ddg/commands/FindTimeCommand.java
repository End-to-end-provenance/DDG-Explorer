package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import laser.ddg.gui.DDGExplorer;
import laser.ddg.gui.DDGPanel;
import laser.ddg.search.OperationSearchElement;
import laser.ddg.search.SearchElement;

public class FindTimeCommand implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {

		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		ArrayList<OperationSearchElement> nodeList = panel.getSearchIndex().getOperationList();

		// order nodelist and show in GUI.
		Collections.sort(nodeList, new Comparator<OperationSearchElement>() {
			@Override
			public int compare(OperationSearchElement p1, OperationSearchElement p2) {
				if (p2.getTimeTaken() < p1.getTimeTaken()) {
					return -1;
				} else if (p2.getTimeTaken() > p2.getTimeTaken()) {
					return 1;
				}
				return 0;
			}
		});

		panel.showSearchResults(nodeList);

	}

}
