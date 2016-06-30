/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package laser.ddg.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import laser.ddg.visualizer.PrefuseGraphBuilder;


/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and 
 * a JButton to close the tab it belongs to 
 */
public class TabComp extends JPanel {
    private final JTabbedPane pane;
    private Object tabObject;
 
    public TabComp(final JTabbedPane pane, Object tabObject) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.tabObject = tabObject;
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);
        //setOpaque(true);
         
        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            @Override
			public String getText() {
                int i = pane.indexOfTabComponent(TabComp.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
         
        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }
 
    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this); // Thomas: TODO passing this as parameter should be avoided from within the constructor.
        }
        
        /**
         * if button is clicked, close the tab
         * (and if this is a DDG, first ask if the
         *  current DDG should be saved to the database)
         */
        @Override
		public void actionPerformed(ActionEvent e) {
        	//if object is a DDG and is not in the database, ask if user wants the DDG to be saved to the database.
        	if (tabObject instanceof PrefuseGraphBuilder && !((PrefuseGraphBuilder)tabObject).alreadyInDB()){
				int answer = JOptionPane.showConfirmDialog(null, "Do you want to save the DDG in the database?", 
						"Save DDG?", JOptionPane.YES_NO_CANCEL_OPTION); //null used to be DDGPanel.this??
				if (answer == JOptionPane.YES_OPTION) {
					try {
						((PrefuseGraphBuilder) tabObject).saveToDB();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(TabComp.this, 
								"Unable to save the DDG: " + e1.getMessage(), 
								"Error saving the DDG", JOptionPane.ERROR_MESSAGE);
					}
				}
				else if (answer == JOptionPane.CANCEL_OPTION){
					//if user presses Cancel, return method and don't close tab.
					return;
				}
			}
        	//close tab
	        int i = pane.indexOfTabComponent(TabComp.this);
	        if (i != -1) {
	        pane.remove(i);
        	}
        }
 
        @Override
		public void updateUI() {
            //we don't want to update UI for this button
        }
 
        //paint the 'X'
        @Override
		protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.GRAY);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
    
    /**
     * if the mouse hovers over the button, draw
     * a border around the 'X'
     */
    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
		public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }
 
        @Override
		public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}