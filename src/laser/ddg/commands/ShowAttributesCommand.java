package laser.ddg.commands;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import laser.ddg.Attributes;
import laser.ddg.DDGBuilder;
import laser.ddg.LanguageConfigurator;
import laser.ddg.ProvenanceData;
import laser.ddg.gui.DDGExplorer;

/**
 * Command to show the attributes associated with a DDG.
 * The attributes are displayed in a separate frame.
 * 
 * @author Barbara Lerner
 * @version Sep 1, 2015
 *
 */
public class ShowAttributesCommand implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		Attributes attributes;
		try {
			ProvenanceData curDDG = ddgExplorer.getCurrentDDG();
			attributes = curDDG.getAttributes();
			createAttributeFrame(curDDG, attributes);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(ddgExplorer, 
					"Unable to get the attributes: " + e1.getMessage(), 
					"Error getting the attributes", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Creates a window to display all the attributes of the ddg
	 * @param curDDG the ddg the user is viewing in the active tab
	 * @param attributes the list of attibutes
	 */
	private static void createAttributeFrame(ProvenanceData curDDG, Attributes attributes){
		JFrame f = new JFrame("Attribute List");
		f.setSize(new Dimension(200, 200));
		JFrame.setDefaultLookAndFeelDecorated(true);

		// make a new JTextArea appear will all the attribute values
		JTextArea attrText = new JTextArea(15, 40);
		attrText.setText(createAttributeText(curDDG.getLanguage(), attributes));
		attrText.setEditable(false);
		attrText.setLineWrap(true);
		
		f.add(attrText);
		f.pack();
		f.setVisible(true);
	}

	/**
	 * Creates the text to display to the user showing attribute names and values
	 * @param language the language to add the legend for
	 * @param attrs the attributes to turn into text
	 * @return the text to display to the user or null there is an error creating the text
	 */
	public static String createAttributeText(String language, Attributes attrs) {
		Class<DDGBuilder> ddgBuilderClass = LanguageConfigurator.getDDGBuilder(language);
		try {
			String text = (String) ddgBuilderClass.getMethod("getAttributeString", Attributes.class).invoke(null, attrs);
			return text;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.out.println("Can't create attribute text");
			e.printStackTrace(System.err);
			return null;
		}
	}

}
