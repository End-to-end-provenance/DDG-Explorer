package laser.ddg.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import laser.ddg.search.SearchElement;
import laser.ddg.search.SearchIndex;
import laser.ddg.visualizer.DDGPanel;

class SearchPanel extends JPanel {
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
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();

		SearchIndex searchIndex = panel.getSearchIndex();

		// Gets which option was selected in the drop down
		System.out.println("panel = " + panel);
		System.out.println("searchIndex = " + searchIndex);
		if (ddgOption.equals("Error"))
			searchList(searchIndex.getErrorList());
		else if (ddgOption.equals("Data"))
			searchList(searchIndex.getDataList());
		else if (ddgOption.equals("File"))
			searchList(searchIndex.getFileList());
		else if (ddgOption.equals("URL"))
			searchList(searchIndex.getURLList());
		else if (ddgOption.equals("Function"))
			searchList(
					searchIndex.getOperationList());
		else
			searchList(searchIndex.getAllList());
	}

	public void searchList(ArrayList<SearchElement> nodesList) {
		DDGPanel ddgPanel = DDGExplorer.getCurrentDDGPanel();
		if (ddgPanel == null) {
			DDGExplorer explorer = DDGExplorer.getInstance();
			JOptionPane.showMessageDialog(explorer,
					"Need to load a DDG to search",
					"No DDG to search",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		boolean isText;
		String searchText = searchField.getText().toLowerCase();

		// checks if information was entered into the search field
		if (searchText.isEmpty())
			isText = false;
		else if (searchText.length() < 6)
			isText = true;
		else if (searchText.substring(0, 6).equals("search"))
			isText = false;
		else
			isText = true;
		
		ArrayList<SearchElement> newList = new ArrayList<SearchElement>();
		// if user entered information into the search bar
		if (isText) {
			for (SearchElement entry : nodesList)
				if (entry.getName().toLowerCase().contains(searchText))
					newList.add(entry);
			ddgPanel.showSearchResults(newList);
		}

		// if text in search is empty then give all associated information
		else {
			ddgPanel.showSearchResults(nodesList);
		}
	}

}