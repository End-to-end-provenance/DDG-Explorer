package laser.ddg.search;

import java.awt.Color;

import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * SearchElement objects are placed into a JList of search results.
 * An element holds the name, id and color of node.  The name is displayed
 * in the list.  Its color is determined based on the type of node.
 * The id is used to find the node to highlight and focus on it.
 */
public class SearchElement {
	private final String name;
	private final int id;
	private final Color color;

	/**
	 * @param type the type of node as recorded in the ddg
	 * @param name the label of the node
	 * @param id the node's id as used by prefuse
	 */
	public SearchElement(String type, String name, int id) {
		this.name = name;
		this.id = id;

		// sets the color of the search item
		switch (type) {
		case "Exception":
			this.color = new Color(PrefuseGraphBuilder.EXCEPTION_COLOR);
			break;

		case "Data":
		case "Snapshot":
			this.color = new Color(PrefuseGraphBuilder.DATA_COLOR);
			break;

		case "URL":
			this.color = new Color(PrefuseGraphBuilder.URL_COLOR);
			break;

		case "File":
			this.color = new Color(PrefuseGraphBuilder.FILE_COLOR);
			break;

		case "Operation":
			this.color = new Color(PrefuseGraphBuilder.LEAF_COLOR);
			break;

		case "Binding":
			this.color = new Color(PrefuseGraphBuilder.INTERPRETER_COLOR);
			break;

		case "Start":
		case "Finish":
			this.color = new Color(PrefuseGraphBuilder.NONLEAF_COLOR);
			break;

		case "Step":
			this.color = new Color(PrefuseGraphBuilder.STEP_COLOR);
			break;

		default:
			this.color = Color.WHITE;
			break;
		}

	}

	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}

	public Color getColor() {
		return color;
	}


}