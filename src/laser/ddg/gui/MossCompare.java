package laser.ddg.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import laser.ddg.persist.JenaLoader;

import com.qarks.util.files.diff.ui.DiffPanel;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import javax.swing.*;
import java.awt.*;




/**
 * The panel used for the Explorer tabs that show two R scripts side-by-side for comparison.
 * 
 * @author Barbara Lerner
 * @version Oct 21, 2013
 *
 */
public class MossCompare extends JPanel {
	// Object that can load information from the database
	private JenaLoader jenaLoader;
	
	// The panel that shows the side-by-side files and their differences
	private DiffPanel diffPanel = new DiffPanel("");

	//private JPanel finalContent = new JPanel();
	
	// The object used to load R scripts that are not in the database
	private JFileChooser chooser;
	
	// The file shown on the left side
	private File leftFile;
	
	// The file shown on the right side
	private File rightFile;
	
	// The button used to select the left file from the file system
	private JButton selectFile1Button = new JButton ("Select from file");
	
	// The button used to select the left file from the database
	private JButton selectFromDB1Button = new JButton ("Select from database");
	
	// The button used to select the right file from the file system
	private JButton selectFile2Button = new JButton ("Select from file");
	
	// The button used to select the right file from the database
	private JButton selectFromDB2Button = new JButton ("Select from database");
	
	// The field to display the left file name
	private JTextField leftFileField = new JTextField();
	
	// The field to display the right file name
	private JTextField rightFileField = new JTextField();

	private Vector leftFileCopied = new Vector<String>();

    private Vector rightFileCopied = new Vector<String>();
	
	// references for pop-ups
	private JFrame frame;
	private Browser browser = new Browser();

	/**
	 * Create the window that allows the user to select files to compare and
	 * to see the results of the comparison
	 * @param frame 
	 * @param jenaLoader the object that reads from the database
	 */
	public MossCompare(JFrame frame, JenaLoader jenaLoader) {
		super(new BorderLayout());
		this.frame = frame;
		this.jenaLoader = jenaLoader;
		JPanel northPanel = new JPanel();
		JPanel leftPanel = createButtonPanel(selectFile1Button, selectFromDB1Button, leftFileField);
		leftPanel.setBorder(BorderFactory.createTitledBorder("Left file"));
		JPanel rightPanel = createButtonPanel(selectFile2Button, selectFromDB2Button, rightFileField);
		rightPanel.setBorder(BorderFactory.createTitledBorder("Right file"));
		northPanel.setLayout(new GridLayout(1,0, 8, 0));
		northPanel.add(leftPanel);
		northPanel.add(rightPanel);
		add(northPanel, BorderLayout.NORTH);
		//Browser browser = new Browser();
        BrowserView browserView = new BrowserView(browser);

        //JFrame frame = new JFrame();
        //this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(false);
        this.add(browserView, BorderLayout.CENTER);
        this.setSize(700, 500);
        //this.setLocationRelativeTo(null);
        
        

        
		//add(diffPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates a panel containing a button to select from a file, a button to select from a 
	 * database, and a field to display the name of the selected file
	 * @param selectFileButton 
	 * @param selectFromDBButton
	 * @param fileField
	 * @return the panel constructed
	 */
	private JPanel createButtonPanel(JButton selectFileButton, JButton selectFromDBButton, JTextField fileField) {
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JPanel topRow = new JPanel();
		selectFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					selectFile(e.getSource());
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(buttonPanel, 
							"Unable to load file: " + e1.getMessage(), 
							"Error loading file", JOptionPane.ERROR_MESSAGE);
				}				
			}
		});
		topRow.add(selectFileButton);
		
		selectFromDBButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					selectFromDB(e.getSource());
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(buttonPanel, 
							"Unable to load file from the database: " + e1.getMessage(), 
							"Error loading file from the database", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		topRow.add(selectFromDBButton);
		buttonPanel.add(topRow);

		fileField.setEditable(false);
		buttonPanel.add(fileField);

		return buttonPanel;
	}

	/**
	 * Selects a file and displays its filename.  If both files have been
	 * selected, the file contents are displayed and the diff is executed, with
	 * the results displayed. 
	 * @param button the button clicked.  We need this to determine if we
	 * 		are setting the left or right file
	 */
	private void selectFile(Object button) {
		if (chooser == null) {
			chooser = new JFileChooser(System.getProperty("user.home"));
		}
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (button == selectFile1Button) {
				selectLeftFile(selectedFile);
			}
			else {
				selectRightFile(selectedFile);
			}
			
			if(leftFile!=null && rightFile!=null){
			displayDiffMoss(frame);
		}
		}
	}

	/**
	 * Set the information for the right file
	 * @param f the file selected.
	 */
	private void selectRightFile(File f) {
		if (f == null) {
			JOptionPane.showMessageDialog(diffPanel, "Could not open " + f);
			return;
		}
		rightFile = f;
		rightFileField.setText(rightFile.getAbsolutePath());
		selectFile2Button.setEnabled(false);
		selectFromDB2Button.setEnabled(false);
	}

	/**
	 * Set the information for the left file
	 * @param f the file selected
	 */
	private void selectLeftFile(File f) {
		if (f == null) {
			JOptionPane.showMessageDialog(diffPanel, "Could not open " + f);
			return;
		}
		leftFile = f;
		leftFileField.setText(leftFile.getAbsolutePath());
		selectFile1Button.setEnabled(false);
		selectFromDB1Button.setEnabled(false);
	}
	

	/**
	 * Displays a DDG Browser that allows the user to select a file from the databsae.
	 * Displays its filename in the text field.  If both files have been
	 * selected, the file contents are displayed and the diff is executed, with
	 * the results displayed. 
	 * @param button the button clicked.  We need this to determine if we
	 * 		are setting the left or right file
	 */
	private void selectFromDB(final Object button) {
		final JDialog selectFrame = new JDialog(frame, "Select from Database", true);
		
		final DBBrowser browser = new ScriptBrowser(jenaLoader);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectFrame.dispose();
			}
			
		});
		
		JButton openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedFile = browser.getSelectedFile();
				selectFrame.dispose();
				
				try {
					if (button == selectFromDB1Button) {
						selectLeftFile(selectedFile);
					}
					else {
						selectRightFile(selectedFile);
					}
					if(leftFile!=null && rightFile!=null){
					
					displayDiffMoss(frame);
				}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(selectFrame, 
							"Unable to compare files: " + e1.getMessage(), 
							"Error comparing files", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Border padding = BorderFactory.createEmptyBorder(0, 8, 8, 8);
		buttonPanel.setBorder(padding);
		buttonPanel.add(openButton);
		buttonPanel.add(cancelButton);
		
		selectFrame.add(browser, BorderLayout.CENTER);
		selectFrame.add(buttonPanel, BorderLayout.SOUTH);
		selectFrame.pack();		
		selectFrame.setLocationRelativeTo(this);
		selectFrame.setVisible(true);
	}
	
	/**
	 * Run the diff algorithm and display the results if both files have been selected.
	 * If the two files are the same, it displays a pop-up box telling the user that.
	 * If the two files are different, but have the same contents, it just displays the diff.
	 * In that case, the user needs to determine that there are no differences.
	 * 
	 * If either file is not yet chosen, it does nothing.
	 */
	private void displayDiff() {
		if (leftFile != null && rightFile != null) {
			diffPanel.launchDiff(leftFile, rightFile);
			if (leftFile.equals(rightFile)) {
				JOptionPane.showMessageDialog(diffPanel, "These are the same file.");
			}
		}
	}

	private void displayDiffMoss(JFrame frame){
		
		String str = null;
        String dummy = null;
        int counter = 0;
        String URL = null;


        try {
    
            Process p = Runtime.getRuntime().exec("./moss -l ml "+leftFile.getAbsolutePath()+" "+rightFile.getAbsolutePath());        
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));                        
            System.out.println("MOSS output:\n");
            while ((str = stdInput.readLine()) != null) {
                System.out.println(str);
                dummy = str;
            }
            URL = dummy; 
            System.out.println("\n");
            URL url = new URL(URL+"/match0-top.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            
            String line;
            while ((line = in.readLine()) != null) {
                if(line.toLowerCase().contains("<TD><A".toLowerCase()) && !(line.toLowerCase().contains("IMG SRC=".toLowerCase())))
                {                
                String[] s = (line).split(">");
                if(counter%2==0)
                {
                    leftFileCopied.addElement(s[s.length-1].substring(0,s[s.length-1].length()-3));
                }
                else{
                    rightFileCopied.addElement(s[s.length-1].substring(0,s[s.length-1].length()-3));
                }
                counter++;
               }
            }
            for(int i=0;i<leftFileCopied.size();i++)
            {
                    System.out.println("File 1:"+leftFileCopied.elementAt(i)+" matches with "+"File 2:"+rightFileCopied.elementAt(i));
            }            
            in.close();       
            //System.exit(0);
        }
        catch (IOException e) {
            System.out.println("Exception: ");
            e.printStackTrace();            
            //System.exit(-1);
        }
 


        frame.setVisible(true);

		browser.loadURL(URL);
		
	}
	

}
