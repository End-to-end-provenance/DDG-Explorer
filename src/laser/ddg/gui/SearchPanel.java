package laser.ddg.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import laser.ddg.search.SearchIndex;
import laser.ddg.visualizer.DDGPanel;
import laser.ddg.visualizer.PrefuseGraphBuilder;

class SearchPanel extends JPanel {
	private static final DDGExplorer ddgExplorer = DDGExplorer.getInstance();
	private JTextField searchField;
	private JComboBox<String> optionsBox, ddgOptionsBox;
	private String ddgOption;

	public SearchPanel() {
		searchUI();
	}

	private void searchUI() {
		searchField = new JTextField("Search");
		JButton advancedSearchButton = new JButton("Advanced Search");

		String[] options = { "Current DDG", "R Script", "Database" };
		String[] ddgOptions = { "Error", "Data", "File", "URL", "Function",
				"All Options" };

		optionsBox = new JComboBox<>(options);
		ddgOptionsBox = new JComboBox<>(ddgOptions);

		ddgOption = ddgOptions[0];
		setLayout(new GridBagLayout());

		GridBagConstraints preferences = new GridBagConstraints();
		preferences.fill = GridBagConstraints.BOTH;

		// Add options box
		preferences.weightx = 0.0;
		preferences.weighty = 0.0;
		preferences.gridx = 0;
		preferences.gridy = 0;
		add(optionsBox, preferences);

		// Add ddg search options box
		preferences.gridx = 1;
		preferences.gridy = 0;
		add(ddgOptionsBox, preferences);

		// Add Search field box (adjusts in response to change in window size)
		preferences.weightx = 0.5;
		preferences.gridx = 2;
		preferences.gridy = 0;
		add(searchField, preferences);

		// Add Advanced Search Button
		preferences.weightx = 0.0;
		preferences.gridx = 3;
		preferences.gridy = 0;
		add(advancedSearchButton, preferences);

		// Changes text in search field in response to the selected ddgOptions
		// box
		ddgOptionsBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent select) {
				searchField.setText("Search for "
						+ ddgOptionsBox.getSelectedItem().toString());
				ddgOption = ddgOptionsBox.getSelectedItem().toString();
			}
		});

		// Submit Search if the enter button is pressed in the search field
		searchField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSearch();
			}
		});

		// Submit Search if the advanced search button is pressed
		advancedSearchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSearch();
			}
		});

	}
	
	// Do a search
	public void doSearch() {
		DDGPanel panel = ddgExplorer.getCurrentDDGPanel();
		PrefuseGraphBuilder build = panel.getBuilder();

		boolean isText;
		String searchFieldText = searchField.getText().toLowerCase();

		// checks if information was entered into the search field
		if (searchFieldText.isEmpty())
			isText = false;
		else if (searchFieldText.length() < 6)
			isText = true;
		else if (searchFieldText.substring(0, 6).equals("search"))
			isText = false;
		else
			isText = true;
		
		SearchIndex searchIndex = panel.getSearchIndex();

		// Gets which option was selected in the drop down
		System.out.println("panel = " + panel);
		System.out.println("searchIndex = " + searchIndex);
		if (ddgOption.equals("Error"))
			panel.searchList(searchIndex.getErrorList(),
					isText, searchFieldText);
		else if (ddgOption.equals("Data"))
			panel.searchList(searchIndex.getDataList(),
					isText, searchFieldText);
		else if (ddgOption.equals("File"))
			panel.searchList(searchIndex.getFileList(),
					isText, searchFieldText);
		else if (ddgOption.equals("URL"))
			panel.searchList(searchIndex.getURLList(),
					isText, searchFieldText);
		else if (ddgOption.equals("Function"))
			panel.searchList(
					searchIndex.getOperationList(), isText, searchFieldText);
		else
			panel.searchList(searchIndex.getAllList(),
					isText, searchFieldText);
	}


}