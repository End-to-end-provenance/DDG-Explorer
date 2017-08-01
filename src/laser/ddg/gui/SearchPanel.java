package laser.ddg.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import laser.ddg.search.SearchElement;
import laser.ddg.search.SearchIndex;
import laser.ddg.workflow.gui.WorkflowPanel;

class SearchPanel extends JPanel {
	private static final String ALL_OPTIONS = "All Options";
	private static final String FUNCTION_OPTION = "Function";
	private static final String URL_OPTION = "URL";
	private static final String FILE_OPTION = "File";
	private static final String DATA_OPTION = "Data";
	private static final String ERROR_OPTION = "Error";
	private JTextField searchField;
	private JComboBox<String> ddgOptionsBox;
	private boolean searchTyped = false;
	private static JButton searchButton;

	public SearchPanel() {
		searchField = new JTextField("Search");
		searchButton = new JButton("Search");
		searchButton.setEnabled(false);

		// Only search the current ddg so far.
		// TODO:  Add other search options
		//String[] options = { "Current DDG", "R Script", "Database" };
		//JComboBox<String>optionsBox = new JComboBox<>(options);

		String[] ddgOptions = { ALL_OPTIONS, ERROR_OPTION, FILE_OPTION, URL_OPTION, DATA_OPTION, FUNCTION_OPTION };
		ddgOptionsBox = createDDGOptionsBox(ddgOptions);

		setLayout(new GridBagLayout());
		GridBagConstraints preferences = new GridBagConstraints();
		preferences.fill = GridBagConstraints.BOTH;

		// Add options box
		//		preferences.weightx = 0.0;
		//		preferences.weighty = 0.0;
		//		preferences.gridx = 0;
		//		preferences.gridy = 0;
		//		add(optionsBox, preferences);

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
		add(searchButton, preferences);


		// Submit Search if the enter button is pressed in the search field
		searchField.addActionListener((ActionEvent e) -> {
			doSearch();
		});

		searchField.addKeyListener (new KeyAdapter () {

			@Override
			public void keyTyped(KeyEvent e) {
				searchTyped = true;
			}
		});

		// Submit Search if the advanced search button is pressed
		searchButton.addActionListener((ActionEvent e) -> {
			doSearch();
		});

	}

	private JComboBox<String> createDDGOptionsBox(String[] ddgOptions) {
		final JComboBox<String> box = new JComboBox<>(ddgOptions);
		// Changes text in search field in response to the selected ddgOptions
		// box
		box.addActionListener((ActionEvent select) -> {
			String ddgOption = box.getSelectedItem().toString();
			if (!searchTyped) {
				searchField.setText("Search for " + ddgOption);
			}
		});
		return box;
	}

	// Do a search
	private void doSearch() {
		DDGPanel panel = DDGExplorer.getCurrentDDGPanel();
		SearchIndex searchIndex;
		if (panel != null) {
			searchIndex = panel.getSearchIndex();
		} else {
			WorkflowPanel wfpanel = DDGExplorer.getCurrentWorkflowPanel();
			searchIndex = wfpanel.getSearchIndex();
		}

		// Gets which option was selected in the drop down
		String ddgOption = ddgOptionsBox.getSelectedItem().toString();
		if (ddgOption.equals(ERROR_OPTION))
			searchList(searchIndex.getErrorList());
		else if (ddgOption.equals(DATA_OPTION))
			searchList(searchIndex.getDataList());
		else if (ddgOption.equals(FILE_OPTION))
			searchList(searchIndex.getFileList());
		else if (ddgOption.equals(URL_OPTION))
			searchList(searchIndex.getURLList());
		else if (ddgOption.equals(FUNCTION_OPTION))
			searchList(searchIndex.getOperationList());
		else
			searchList(searchIndex.getAllList());
	}

	private void searchList(ArrayList<? extends SearchElement> nodesList) {
		DDGPanel ddgPanel = DDGExplorer.getCurrentDDGPanel();
		WorkflowPanel wfPanel = DDGExplorer.getCurrentWorkflowPanel();
		if (ddgPanel == null && wfPanel == null) {
			DDGExplorer explorer = DDGExplorer.getInstance();
			JOptionPane.showMessageDialog(explorer,
					"Need to load a DDG or workflow to search",
					"No DDG or workflow to search",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		ArrayList<SearchElement> newList = new ArrayList<>();
		// if user entered information into the search bar
		if (searchTyped) {
			String searchText = searchField.getText().toLowerCase();
			nodesList.stream().filter((entry) -> (entry.getName().toLowerCase().contains(searchText))).forEach((entry) -> {
				newList.add(entry);
			});
			if (ddgPanel != null) {
			ddgPanel.showSearchResults(newList);
			} else {
				wfPanel.showSearchResults(newList);
			}
		}

		// if text in search is empty then give all associated information
		else {
			if (ddgPanel != null) {
				ddgPanel.showSearchResults(nodesList);
				} else {
					wfPanel.showSearchResults(nodesList);
				}
		}
	}

	public static void enableSearch() {
		searchButton.setEnabled (true);
	}

	public static void disableSearch() {
		searchButton.setEnabled(false);
	}

}