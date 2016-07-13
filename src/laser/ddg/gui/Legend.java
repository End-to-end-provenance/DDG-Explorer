package laser.ddg.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * Draws the legend explaining the node and edge colors.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class Legend extends Box {
	/**
	 * Initializes the layout.
	 */
	public Legend() {
		super(BoxLayout.Y_AXIS);
	}

	private static final int LEGEND_ENTRY_HEIGHT = 25;
	private static final Font LEGEND_FONT = new Font("Helvetica", Font.PLAIN,
			10);
	
	private boolean drawn = false;
	
	/**
	 * Draw the legend for this graph. The legend is specific to the language
	 * that the DDG is for, so each language must create the label and color
	 * pairings to be displayed in the legend, using the vocabulary natural for
	 * that language.
	 * 
	 * @param nodeColors
	 *            the node label, color pairs. May be null.
	 * @param edgeColors
	 *            the edge label, color pairs. May be null.
	 */
	public void drawLegend(ArrayList<LegendEntry> nodeColors,
			ArrayList<LegendEntry> edgeColors) {
		
		if (drawn) {
			return;
		}

		if (nodeColors == null && edgeColors == null) {
			return;
		}

		JPanel legend = new JPanel();
		legend.setLayout(new GridLayout(0, 1));

		Box headerPanel = new Box(BoxLayout.X_AXIS);
		headerPanel.add(new JLabel("Legend"));
		headerPanel.add(Box.createHorizontalGlue());
		JButton closeLegendButton = new JButton("X");
		closeLegendButton
				.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		closeLegendButton.setToolTipText("Hide legend.");
		closeLegendButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// System.out.println("Close button clicked");
				DDGExplorer.getInstance().removeLegend();
			}

		});
		headerPanel.add(closeLegendButton);
		legend.add(headerPanel);
		int numEntries = 1;

		if (nodeColors != null) {
			addNodesToLegend(nodeColors, legend);

			numEntries = nodeColors.size() + 1;

			if (edgeColors != null) {
				legend.add(new JPanel());
				// System.out.println("Adding spacer");
				numEntries++;
			}
		}

		if (edgeColors != null) {
			addEdgesToLegend(edgeColors, legend);

			numEntries = numEntries + edgeColors.size() + 1;
		}

		legend.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		legend.setPreferredSize(new Dimension(125, numEntries
				* LEGEND_ENTRY_HEIGHT));

		add(Box.createVerticalGlue());
		add(legend);
		add(Box.createVerticalGlue());

		drawn = true;
	}

	/**
	 * Add the edge labels and colors to the legend.
	 * 
	 * @param edgeColors
	 *            the edge label, color pairs
	 * @param legend
	 *            the legend to add them to.
	 */
	private static void addEdgesToLegend(ArrayList<LegendEntry> edgeColors,
			JPanel legend) {
		legend.add(new JLabel("Edges"));
		// System.out.println("Adding edges header");
		for (LegendEntry entry : edgeColors) {
			JLabel next = new JLabel(entry.getLabel(), SwingConstants.CENTER);
			next.setFont(LEGEND_FONT);
			next.setForeground(entry.getColor());
			legend.add(next);
			// System.out.println("Adding " + entry.getLabel());
		}
	}

	/**
	 * Add the node labels and colors to the legend.
	 * 
	 * @param nodeColors
	 *            the node label, color pairs
	 * @param legend
	 *            the legend to add them to.
	 */
	private static void addNodesToLegend(ArrayList<LegendEntry> nodeColors,
			JPanel legend) {
		legend.add(new JLabel("Nodes"));
		// System.out.println("Adding node header");
		for (LegendEntry entry : nodeColors) {
			JLabel next = new JLabel(entry.getLabel(), SwingConstants.CENTER);
			next.setFont(LEGEND_FONT);
			next.setOpaque(true);
			next.setBackground(entry.getColor());
			legend.add(next);
			// System.out.println("Adding " + entry.getLabel());
		}
	}


}
