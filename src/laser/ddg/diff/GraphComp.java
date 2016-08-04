package laser.ddg.diff;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

import laser.ddg.diff.gui.DDGDiffPanel;
import laser.ddg.gui.DDGExplorer;
import laser.ddg.persist.JenaWriter;
import laser.ddg.persist.Parser;
import laser.ddg.visualizer.PrefuseGraphBuilder;

/**
 * The algorithms used to compute differences between ddgs.
 *
 * @author Hafeezul Rahman
 * @version July 29, 2016
 *
 */
public class GraphComp extends JPanel {

	/**
	 * Run the diff algorithm on the nodes of the left ddg and the nodes of the
	 * right ddg. If a node is present in left ddg and missing in the right ddg,
	 * it appears in Red. If a node is present in right ddg and missing in the
	 * left ddg, it appears in Green. The rest of the nodes in the left and
	 * right ddgs are colored white.
	 */
	public static void doDiff(DDGDiffPanel diffPanel, File leftFile, File rightFile) throws IOException {
		JenaWriter jenaWriter = JenaWriter.getInstance();

		PrefuseGraphBuilder leftBuilder = new PrefuseGraphBuilder(false, jenaWriter);
		String[] leftText = prepareForDiff(leftBuilder, "left_group", leftFile);

		PrefuseGraphBuilder rightBuilder = new PrefuseGraphBuilder(false, jenaWriter);
		String[] rightText = prepareForDiff(rightBuilder, "right_group", rightFile);

		ArrayList<String> diffResult = computeTextDiffResult(leftText, rightText);
		computeDDGDiffResult(diffResult, leftBuilder, rightBuilder);

		diffPanel.displayDiffResults(leftBuilder, rightBuilder);

	}

	private static String[] prepareForDiff(PrefuseGraphBuilder builder, String copyGroupName, File selectedFile)
			throws IOException {
		DDGExplorer.loadingDDG();

		builder.createCopiedGroup(copyGroupName);

		builder.processStartedForDiff();

		Parser parser = new Parser(selectedFile, builder);
		parser.addNodesAndEdges();

		return createTextToDiff(parser, builder);
	}

	/**
	 * Creates a text array to use in the comparison.  The array contains
	 * one entry for each procedural node in the ddg.  The contents of the entry
	 * is the node name with the node number and line number and all whitespace removed.  
	 * 
	 * @param parser
	 * @param builder corresponding to the graph
	 */
	private static String[] createTextToDiff(Parser parser, PrefuseGraphBuilder builder) {
		String[] text = new String[parser.getNumPins()];
		//System.out.println("numPins = " + parser.getNumPins());

		// Pins are numbered beginning at 1.
		for (int i = 1; i <= text.length; i++) {
			String curNodeName = builder.getName(builder.getNode(i));
			//System.out.println(curNodeName);
			
			// Remove whitespace
			String dummy = curNodeName.replaceAll("\\s+", "");

			// Remove the node number and line number, if present
			if (dummy.indexOf("[") < 0) {
				text[i - 1] = dummy.substring(dummy.indexOf('-') + 1);
			} else {
				text[i - 1] = dummy.substring(dummy.indexOf('-') + 1, dummy.indexOf('['));
			}
		}

		return text;
	}

	// Code adapted from
	// http://introcs.cs.princeton.edu/java/96optimization/Diff.java.html
	// From Section 9.6 of Introduction to Programming in Java by Robert Sedgewick and Kevin Wayne
	// Note that this identifies new lines and deleted lines.  Changed lines are treated as
	// an addition + a deletion.
	// Rahman's original code checked for changed lines, but then colored them as additions
	// and deletions rather than using a different color for changed lines, so this has the 
	// equivalent effect.
	private static ArrayList<String> computeTextDiffResult(String[] leftText, String[] rightText) {
		// System.out.println("\nIn computeTextDiffResult");

		// number of lines of each file
		int leftNumLines = leftText.length;
		int rightNumLines = rightText.length;

		// opt[i][j] = length of LCS of x[i..M] and y[j..N]
		int[][] opt = new int[leftNumLines + 1][rightNumLines + 1];

		// compute length of LCS and all subproblems via dynamic programming
		for (int i = leftNumLines - 1; i >= 0; i--) {
			for (int j = rightNumLines - 1; j >= 0; j--) {
				if (leftText[i].equals(rightText[j])) {
					opt[i][j] = opt[i + 1][j + 1] + 1;
				}
				else {
					opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
				}
			}
		}

		// recover LCS itself and print out non-matching lines to standard
		// output
		int i = 0, j = 0;
		ArrayList<String> diffResult = new ArrayList<>();
		while (i < leftNumLines && j < rightNumLines) {
			//System.out.println("Left line = " + leftText[i]);
			//System.out.println("Right line = " + rightText[j]);
			if (leftText[i].equals(rightText[j])) {
				//System.out.println("Lines " + i + " and " + j + " match");
				diffResult.add(leftText[i] + " " + rightText[j]);
				i++;
				j++;
			} else if (opt[i + 1][j] >= opt[i][j + 1]) {
				//System.out.println("< " + leftText[i]);
				diffResult.add("< " + leftText[i]);
				i++;
			} else {
				//System.out.println("> " + rightText[j]);
				diffResult.add("> " + rightText[j]);
				j++;
			}
		}

		// dump out one remainder of one string if the other is exhausted
		//System.out.println("Extra lines");
		while (i < leftNumLines || j < rightNumLines) {
			if (i == leftNumLines) {
				//System.out.println("> " + rightText[j]);
				diffResult.add("> " + rightText[j]);
				j++;
			} else if (j == rightNumLines) {
				//System.out.println("< " + leftText[i]);
				diffResult.add("< " + leftText[i]);
				i++;
			}
		}

		return diffResult;
	}

	/**
	 * Determines which nodes are added and removed in the 2nd ddg relative to the first ddg.
	 * Deleted nodes are added to the left_group.  Added nodes are added to the right_group.
	 * This will affect the colors that the nodes are displayed with.
	 * 
	 * @param textDiffResult the text differences in the procedural nodes.  There are 4 possibles line
	 * 		formats:
	 * 		<ul>
	 * 		<li> &gt; right_node_label - represents a node that is new in the right graph
	 * 		<li> &lt; left_node_label - represents a node that is deleted from the left graph
	 * 		<li> left_node_label | right_node_label - represents a node that is changed
	 * 		<li> left_node_label right_node_label - represents a node that is unchanged
	 * 		</ul>
	 * @param builderLeft the builder for the left graph
	 * @param builderRight the builder for the right graph
	 */
	private static void computeDDGDiffResult(ArrayList<String> textDiffResult, PrefuseGraphBuilder builderLeft,
			PrefuseGraphBuilder builderRight) {
		int leftnode = 1, rightnode = 1;
		//System.out.println("\nIn computeDDGDiffResult");
		for (String nextDiff : textDiffResult) {

			String currString = nextDiff.trim();
			//System.out.println(currString);
			
			// Split on the whitespace
			String[] dummyString = currString.split("\\s+");

			// Check for a changed line
			if (dummyString.length == 3) {
				assert dummyString[1].equals("|");
				// System.out.println("added left and right
				// "+"leftnode:"+leftnode+" rightnode:"+rightnode);
				builderLeft.updateCopiedGroup(leftnode, "left_group");
				builderRight.updateCopiedGroup(rightnode, "right_group");
				leftnode = leftnode + 1;
				rightnode = rightnode + 1;
			} 
			
			else if (dummyString.length == 2) {
				// System.out.println("length = 2");
				if (dummyString[0].equals(">")) {
					//System.out.println("added right node: " + rightnode);
					builderRight.updateCopiedGroup(rightnode, "right_group");
					rightnode = rightnode + 1;
				} else if (dummyString[0].equals("<")) {
					//System.out.println("added left node: " + leftnode);
					builderLeft.updateCopiedGroup(leftnode, "left_group");
					leftnode = leftnode + 1;
				} else {
					// Identical node in both sides
					leftnode = leftnode + 1;
					rightnode = rightnode + 1;
				}
			}
		}
	}
}

