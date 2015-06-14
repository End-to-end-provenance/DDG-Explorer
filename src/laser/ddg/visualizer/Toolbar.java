package laser.ddg.visualizer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Display;
import prefuse.util.display.PaintListener;

/**
 * Toolbar to interact with the DDGDisplay
 * 
 * @author Nikki Hoffler
 *
 */
public class Toolbar extends JToolBar implements ActionListener{
	private DDGDisplay ddgDisplay;
	private JComponent[] tools;
	private JSlider zoomSetting;
	private int sliderSetting = 10;
	
    public Toolbar(DDGDisplay ddgDisplay) {   
    	super("DDG Tools", SwingConstants.HORIZONTAL);
    	this.ddgDisplay = ddgDisplay;
        populateTools();
        addTools();
        
        ddgDisplay.addPaintListener(new ZoomListener(zoomSetting));
		this.addPropertyChangeListener(createListener());   
    }    
    
    /**
     * create and populate array of components
     */
    private void populateTools() {   
    	// Code for unimplemented buttons is commented out. 
//    	JButton pointer = new JButton("Pointer");
//    		pointer.addActionListener(new ActionListener(){
//    			@Override
//    			public void actionPerformed(ActionEvent arg0){
//    				System.out.println("pointer clicked");
//    			}
//    		});
//    	JButton selector = new JButton("Select");
//	    	selector.addActionListener(new ActionListener(){
//				@Override
//				public void actionPerformed(ActionEvent arg0){
//					System.out.println("select clicked");
//				}
//			});
//    	JButton fader = new JButton("Fade");
//	    	fader.addActionListener(new ActionListener(){
//				@Override
//				public void actionPerformed(ActionEvent arg0){
//					System.out.println("fade clicked");
//				}
//			});
//    	JButton commenter = new JButton("Comment");
//	    	commenter.addActionListener(new ActionListener(){
//				@Override
//				public void actionPerformed(ActionEvent arg0){
//					System.out.println("comment clicked");
//				}
//			});
    	
	    //slider
	    //Zoom is typically between 0.3 and 5. It starts at 1.0.
	    //So, I will use the slider's value/10 to change the ddg's zoom
	    zoomSetting = new JSlider(JSlider.HORIZONTAL, 1, 50, sliderSetting);
	    zoomSetting.setPreferredSize(new Dimension(10, 20));
	    zoomSetting.setMinorTickSpacing(10);
	    zoomSetting.setPaintTicks(true);
	    zoomSetting.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				//slider value = desired scale	
				double zoomedScale = zoomSetting.getValue();
				zoomedScale = zoomedScale/10;
				//System.out.println(zoomedScale + " slider/10");
				
				//current scale
				double currentScale = ddgDisplay.getScale();
				//System.out.println(currentScale + " current display scale");
				
				// Technically, we should not do an exact comparison of doubles, but
				// not really a problem here.  It is not controlling a loop and the
				// code inside the if is fast.  Better to keep the code easy to
				// understand.
				if(currentScale != zoomedScale){
					//To set absolute scale, use desiredScale*(1/currentScale) as parameter to zoom()	
					double scaleFactor = (zoomedScale/currentScale);
					//System.out.println(scaleFactor + " scale Factor");
							
					//find center of screen, second parameter to zoom()
					Rectangle frameBounds = ddgDisplay.getBounds();
					int xMiddle = (int)(frameBounds.getWidth()-frameBounds.getX())/2;
					int yMiddle = (int)(frameBounds.getHeight()-frameBounds.getY())/2;
					Point2D centerScreen = new Point(xMiddle, yMiddle);
					//System.out.println("center at " + centerScreen.toString());
					
					//call zoom!
					ddgDisplay.zoom(centerScreen, scaleFactor);
					//System.out.println(ddgDisplay.getScale() + " new scale");
					ddgDisplay.repaint();
				}
			}
		});
	    	
	    JButton refocuser = new JButton("Refocus");
    	refocuser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0){
				ddgDisplay.zoomToFit();
				int newZoom = (int)(ddgDisplay.getScale())*10;
				zoomSetting.setValue(newZoom);
			}
		});	
	    
	    
//    	tools = new JComponent[8];
//    		tools[0] = pointer;
//    		tools[1] = selector;
//    		tools[2] = fader;
//    		tools[3] = commenter;
//    		tools[4] = zoomSetting;
//    		tools[5] = refocuser;

        tools = new JComponent[2];
    		tools[0] = zoomSetting;
    		tools[1] = refocuser;

    	/*image icons 
        JButton four = new JButton(new ImageIcon("zoomOut.gif"));*/
    }
    
    /**
     * adds components from array to the JToolBar
     */
    private void addTools(){
    	for (int i = 0; i < tools.length; i++){
    		JComponent current = tools[i];
    		if (current != null){
    			this.add(current);
    		}
    	}
    }
    
    /**
     * creates a PropertyChangeListener that will listen for a change
     * in the toolbar's orientation and set the slider to that direction
     * @return	propertyChangeListener
     */
    private PropertyChangeListener createListener(){
    	PropertyChangeListener propListener = new PropertyChangeListener() {		
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String propName = e.getPropertyName();
				if ("orientation".equals(propName)){
					if ((Integer)e.getNewValue() == JToolBar.VERTICAL) {
						zoomSetting.setOrientation(JSlider.VERTICAL);
					}else{
						zoomSetting.setOrientation(JSlider.HORIZONTAL);
					}
				}
			}
		};
    	return propListener;
    }
    
    public static class ZoomListener implements PaintListener{
    	private JSlider slider;
    	
    	public ZoomListener(JSlider slider){
    		super();
    		this.slider = slider;
    	}
    	
		@Override
		public void prePaint(Display displayGiven, Graphics2D g) {
			// TODO Auto-generated method stub		
		}
		
		@Override
		public void postPaint(Display displayGiven, Graphics2D g) {
			int zoom = (int) Math.round((displayGiven.getScale()*10));
			//System.out.println("zoomListener resetting slider to value " + zoom);
			if(slider.getValue() != zoom){
				slider.setValue(zoom);
			}	
		}	
    	
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//if(e.getSource() == tools){
			//System.out.println("tools clicked!");
		//}
	}  
}
