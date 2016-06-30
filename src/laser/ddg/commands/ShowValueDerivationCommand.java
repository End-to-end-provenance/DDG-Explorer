package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import laser.ddg.query.DerivationQuery;
import laser.ddg.query.Query;

/**
 * Command to get information from the user on the value whose
 * derivation history the user wants to know.  Then performs
 * the query.
 * 
 * @author Barbara Lerner
 * @version Sep 2, 2015
 *
 */
public class ShowValueDerivationCommand implements ActionListener {
	/**
	 * Loads the portion of a DDG that shows how a data value has been computed
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		final Query derivationQuery = new DerivationQuery();
		new DBBrowserFrame (derivationQuery);
	}

}
