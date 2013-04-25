package edu.illinois.ncsa.isda.im2learn.ext.conversion;

import java.awt.event.ActionEvent;
import java.net.URL;


import javax.swing.AbstractAction;

import javax.swing.JMenu;
import javax.swing.JMenuItem;




import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.PCA;
import edu.illinois.ncsa.isda.im2learn.ext.edge.Edge;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ColorModels;

/** This class is a menu that lets the user converting the image in the main panel to different color spaces**/
//------------------------------------------------------------
public class Colorspace implements Im2LearnMenu {

	private ColorModels cm = new ColorModels();
	int options=19;
	private JMenuItem[] menus = new JMenuItem[options];
	private ImageObject Feature;
    private ImageObject image;
    
	/**
	/**
	 * Reference to the imagepanel this menu is associated
	 * with.
	 */
	private ImagePanel imagepanel;
	/**
	 * Logger used for debug and warning messages.
	 */
	static private Log logger = LogFactory.
	getLog(Colorspace.class);

	public Colorspace() {
		
		imagepanel = null;
	
		
		menus[0]= new JMenuItem(new AbstractAction("RGB to HSV") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("RGB to HSV");
	         
	          }
	      });
		menus[1]= new JMenuItem(new AbstractAction("RGB to YUV") {
	          public void actionPerformed(ActionEvent e) {
	          	extract_Feature("RGB to YUV");
	          	
	          }
	      });
		menus[2]= new JMenuItem(new AbstractAction("RGB to YIQ") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("RGB to YIQ");
	         
	          }
	      });
		menus[3]= new JMenuItem(new AbstractAction("RGB to CMY") {
	          public void actionPerformed(ActionEvent e) {
	          	extract_Feature("RGB to CMY");
	          	
	          }
	      });
		menus[4]= new JMenuItem(new AbstractAction("RGB to XYZ") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("RGB to XYZ");
	         
	          }      
	      });

		
		menus[5]= new JMenuItem(new AbstractAction("HSV to RGB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("HSV to RGB");
	         
	          }      
	      });
		
		menus[6]= new JMenuItem(new AbstractAction("YUV to RGB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("YUV to RGB");
	         
	          }      
	      });
	
		menus[7]= new JMenuItem(new AbstractAction("YIQ to RGB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("YIQ to RGB");
	         
	          }      
	      });
		
		menus[8]= new JMenuItem(new AbstractAction("CMY to RGB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("CMY to RGB");
	         
	          }      
	      });		
		
		menus[9]= new JMenuItem(new AbstractAction("XYZ to RGB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("XYZ to RGB");
	         
	          }
	          
	      });

		menus[10]= new JMenuItem(new AbstractAction("XYZ to LUV") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("XYZ to LUV");
	         
	          }
	          
	      });
		menus[11]= new JMenuItem(new AbstractAction("XYZ to LAB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("XYZ to LAB");
	         
	          }
	          
	      });
		menus[12]= new JMenuItem(new AbstractAction("LUV to XYZ") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("LUV to XYZ");
	         
	          }
	          
	      });
		menus[13]= new JMenuItem(new AbstractAction("LAB to XYZ") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("LAB to XYZ");
	         
	          }
	          
	      });
		
		
		menus[14]= new JMenuItem(new AbstractAction("RGB to GRAY") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("RGB to GRAY");
	         
	          }      
	      });

		menus[15]= new JMenuItem(new AbstractAction("RGB to LUV") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("RGB to LUV");
	         
	          }      
	      });

		menus[16]= new JMenuItem(new AbstractAction("RGB to LAB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("RGB to LAB");
	         
	          }      
	      });
		
		menus[17]= new JMenuItem(new AbstractAction("LUV to RGB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("LUV to RGB");
	         
	          }      
	      });
		
		menus[18]= new JMenuItem(new AbstractAction("LAB to RGB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("LAB to RGB");
	         
	          }      
	      });
		// GRAY Conversion not yet implemented
		/*
		menus[15]= new JMenuItem(new AbstractAction("GRAY to RGB") {
	          public void actionPerformed(ActionEvent e) {
	         	extract_Feature("GRAY to RGB");
	         
	          }
	          
	      });	
	      */

}
		
private void extract_Feature(String method){

	image=imagepanel.getImageObject();
	if(method.equals("RGB to HSV")){
		if(!cm.convertRGB2HSV(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("RGB to YUV")){
		if(!cm.convertRGB2YUV(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("RGB to YIQ")){
		if(!cm.convertRGB2YIQ(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("RGB to CMY")){
		if(!cm.convertRGB2CMY(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("RGB to XYZ")){
		if(!cm.convertRGB2XYZ(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
// GRAY
	
	else if(method.equals("RGB to GRAY")){
		if(!cm.convertRGB2GRAY(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	
	else if(method.equals("HSV to RGB")){
		if(!cm.convertHSV2RGB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("YUV to RGB")){
		if(!cm.convertYUV2RGB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("YIQ to RGB")){
		if(!cm.convertYIQ2RGB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("CMY to RGB")){
		if(!cm.convertCMY2RGB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("XYZ to RGB")){
		if(!cm.convertXYZ2RGB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	// Gray converstion not yet implemented
	/*
	else if(method.equals("GRAY to RGB")){
		if(!cm.convertGRAY2RGB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	*/
	else if(method.equals("XYZ to LUV")){
		if(!cm.convertXYZ2LUV(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("XYZ to LAB")){
		if(!cm.convertXYZ2LAB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("LUV to XYZ")){
		if(!cm.convertLUV2XYZ(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("LAB to XYZ")){
		if(!cm.convertLAB2XYZ(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("RGB to LUV")){
		if(!cm.convertRGB2LUV(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("LUV to RGB")){
		if(!cm.convertLUV2RGB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("LAB to RGB")){
		if(!cm.convertLAB2RGB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	else if(method.equals("RGB to LAB")){
		if(!cm.convertRGB2LAB(image)) 
			logger.error("Could not convert image.");
		else
			Feature = cm.getConvertedIm();
	}
	if(Feature==null)
		Feature=imagepanel.getImageObject();
	else
		imagepanel.setImageObject(Feature);
	}	


	public JMenuItem[] getPanelMenuItems() {
		return null;
	}

	/**
	 * Create a menu entry called Tools, and attach a
	 * submenu entry to this for this class, called detect. If
	 * the user selects this class
	 */
	public JMenuItem[] getMainMenuItems() {
		JMenu menu = new JMenu("Tools");

		JMenu tools = new JMenu("Tools");
		JMenu type = new JMenu("Colorspace");
        tools.add(type);
        for(int i=0;i<options;i++)
        type.add(menus[i]);
		return new JMenuItem[] {tools };
	}
	
	 public void setImagePanel(ImagePanel imagepanel) {
	        this.imagepanel = imagepanel;
	        for (int i = 0; i < menus.length; i++) {
	            menus[i].setEnabled(true);
	        }
	           
	}

	/**
	 * When a new image is loaded make sure that the current
	 * swapped image is removed, i.e. perform a reset. Only
	 * need to reset the image if this frame is visible.
	 */
	public void imageUpdated(ImageUpdateEvent event) {
		
	}

	/**
	 * Return the right documentation depending on what node
	 * is selected, in this case only the Swap node should
	 * exist.
	 */
	public URL getHelp(String menu) {
		if (menu.equals("Colorspace")) {
			return getClass().getResource("help/Colorspace.html");
		}
			return null;
	
		
	}
}