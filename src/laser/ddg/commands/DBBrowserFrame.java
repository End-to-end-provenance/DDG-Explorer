package laser.ddg.commands;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import laser.ddg.gui.DBBrowserListener;
import laser.ddg.gui.DDGBrowser;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaLoader;
import laser.ddg.query.Query;

public class DBBrowserFrame {
	private String selectedProcessName;
	private String selectedTimestamp;
	
	public DBBrowserFrame (final Query query) {
		final DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		try {
			final JDialog queryFrame = new JDialog(ddgExplorer, query.getMenuItem(), true);
			final JButton okButton = new JButton ("Ok");
			okButton.setEnabled(false);
			JButton cancelButton = new JButton ("Cancel");
			final JenaLoader jenaLoader = JenaLoader.getInstance();
			DDGBrowser dbBrowser = new DDGBrowser(jenaLoader);
			dbBrowser.addDBBrowserListener(new DBBrowserListener() {
				@Override
				public void scriptSelected(String script) {
					selectedProcessName = script;
				}

				@Override
				public void timestampSelected(String timestamp) {
					selectedTimestamp = timestamp;
					okButton.setEnabled(true);
				}
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(okButton);
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					queryFrame.dispose();
					DDGExplorer.loadingDDG();
					query.performQuery(jenaLoader, selectedProcessName, selectedTimestamp, ddgExplorer);
					query.addQueryListener(ddgExplorer);
				}
				
			});
			buttonPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					queryFrame.dispose();
				}
				
			});

			queryFrame.add(dbBrowser, BorderLayout.CENTER);
			queryFrame.add(buttonPanel, BorderLayout.SOUTH);
			queryFrame.setMinimumSize(new Dimension(800, 400));
			queryFrame.setLocationRelativeTo(ddgExplorer);
			queryFrame.setVisible(true);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(ddgExplorer,
					"Unable to perform the query: " + e1.getMessage(),
					"Error performing query", JOptionPane.ERROR_MESSAGE);
		}

	}
}
