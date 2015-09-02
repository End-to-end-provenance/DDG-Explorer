package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import laser.ddg.query.Query;
import laser.ddg.query.ResultsQuery;

public class ShowComputedFromValueCommand implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		final Query resultsQuery = new ResultsQuery();
		new DBBrowserFrame (resultsQuery);
	}

}
