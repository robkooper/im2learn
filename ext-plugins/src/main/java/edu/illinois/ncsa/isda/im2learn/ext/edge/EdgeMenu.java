package edu.illinois.ncsa.isda.im2learn.ext.edge;
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
import edu.illinois.ncsa.isda.im2learn.ext.edge.Edge;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ColorModels;

	/** This class is a menu that lets the user to extract different edge features from the image in the main panel**/
	//------------------------------------------------------------
	public class EdgeMenu 
	implements Im2LearnMenu {
		int methods=3;
		private JMenuItem[] menus = new JMenuItem[methods];
		private ImageObject feature;
	    private ImageObject image;
	    private Edge edgeObj=new Edge();
	    
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
		getLog(EdgeMenu.class);

		public EdgeMenu() {
			
			imagepanel = null;
		
			
			menus[0]= new JMenuItem(new AbstractAction("Sobel") {
		          public void actionPerformed(ActionEvent e) {
		         	extract_Feature("Sobel");
		         
		          }
		      });
			menus[1]= new JMenuItem(new AbstractAction("Sobel Magnitude") {
		          public void actionPerformed(ActionEvent e) {
		          	extract_Feature("Sobel_mag");
		          	
		          }
		      });
			menus[2]= new JMenuItem(new AbstractAction("Roberts") {
		          public void actionPerformed(ActionEvent e) {
		         	extract_Feature("Roberts");
		         
		          }
		      });
		

	}
			
	private void extract_Feature(String method){

		image=imagepanel.getImageObject();
		if(method.equals("Sobel_mag")){
			if(!edgeObj.SobelMag(image)) 
				logger.error("Could not extract Edge Features");
			else
				feature =edgeObj.GetEdgeImageObject();
		}
		else if(method.equals("Sobel")){
			if(!edgeObj.Sobel(image)) 
				logger.error("Could not extract Edge Features");
			else
				feature =edgeObj.GetEdgeImageObject();
		}
		else if(method.equals("Roberts")){
			if(!edgeObj.RobertsVar(image)) 
				logger.error("Could not extract Edge Features");
			else
				feature =edgeObj.GetEdgeImageObject();
		}
		
		if(feature==null)
			feature=imagepanel.getImageObject();
		else
			imagepanel.setImageObject(feature);
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
			JMenu type = new JMenu("Edge");
	        tools.add(type);
	        for(int i=0;i<methods;i++)
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
			if (menu.equals("Edge")) {
				return getClass().getResource("help/EdgeMenu.html");
			}
				return null;
		
			
			
		}
	}

