package laser.ddg.search;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import laser.ddg.visualizer.PrefuseGraphBuilder;
import prefuse.visual.NodeItem;

/**
 * An element holding the name, type, row and color of node
 */
public class SearchElement {
	/**
	 * 
	 */
	private final PrefuseGraphBuilder prefuseGraphBuilder;
	private final String name, type;
	private final int id;
	private final Color color;

	public SearchElement(PrefuseGraphBuilder prefuseGraphBuilder, String type,
			String name, int id) {
		this.prefuseGraphBuilder = prefuseGraphBuilder;
		this.type = type;
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

//	public String getType() {
//		return type;
//	}
//
	public Color getColor() {
		return color;
	}


}