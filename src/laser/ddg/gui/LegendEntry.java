package laser.ddg.gui;

import java.awt.Color;

/**
 * A LegendEntry is a pair of a label and a color to
 * be displayed in a graphical legend.
 */
public class LegendEntry {
	private String label;
	private Color color;
	
	public LegendEntry(String label, Color color) {
		this.label = label;
		this.color = color;
	}
	
	public LegendEntry(String label, int color) {
		this.label = label;
		this.color = new Color(color);
	}
	
	public String getLabel() {
		return label;
	}
	
	public Color getColor() {
		return color;
	}
}
