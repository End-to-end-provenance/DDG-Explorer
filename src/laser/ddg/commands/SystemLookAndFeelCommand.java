/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import laser.ddg.gui.DDGExplorer;

/**
 *  Command to control the look and feel.
 * 
 * @author Thomas Pasquier
 * @version 01/07/2016
 */
public class SystemLookAndFeelCommand implements ActionListener {
    /**
	 * Sets whether line numbers are shown based on the setting of the
	 * corresponding menu item.
         * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		useSystemLookAndFeel((JCheckBoxMenuItem) e.getSource());
	}
        
        /**
	 * Sets whether to use default or system look and feel.
	 * 
         * @param systemLookAndFeelMenuItem
	 */
	public static void useSystemLookAndFeel(final JCheckBoxMenuItem systemLookAndFeelMenuItem) {
		DDGExplorer ddgExplorer = DDGExplorer.getInstance();
		ddgExplorer.useSystemLookAndFeel(systemLookAndFeelMenuItem.isSelected());
	}
}
