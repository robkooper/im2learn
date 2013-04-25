package edu.illinois.ncsa.isda.im2learn.ext.test;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateListener;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.calculator.ImageCalculator;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.PCA;
import edu.illinois.ncsa.isda.im2learn.ext.edge.Edge;
import edu.illinois.ncsa.isda.im2learn.ext.geo.Sampling;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ColorModels;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.ImageCompare;

/**This class lets the user to click on the object of interest and we use that as a template then we move a window of the 
 * same size of the template and compute the normalized correlation  of that window to that template and 
 * assign the value to the top left corner of the window. This windows could be overlapping or non overlapping 
 * depending on the values specified in the move textfield. The feature image used could be HSV, Edge or PCA  **/
//------------------------------------------------------------
public class Detect extends Im2LearnFrame
implements Im2LearnMenu {

	private ColorModels cm = new ColorModels();
	private ImageCalculator Cal = new ImageCalculator();
	private JTextField txtCounter;
	private JSlider sldBand;
	private JTextField moveInput,samp_row,samp_col;
	private double row_scale,col_scale;
	private JCheckBox band1,band2,band3;	
	private JComboBox filter;
	private int x,y,width,height;
	private ImageObject image,result,feature;
	private ImageCompare ic ;

	/**
	 * Reference to the imagepanel this menu is associated
	 * with.
	 */
	private ImagePanel imagepanel;

	/**
	 * Preview of the detect operation.
	 */
	private ImagePanel ipPreview;

	/**
	 * Logger used for debug and warning messages.
	 */
	static private Log logger = LogFactory.
	getLog(Detect.class);

	/**
	 * Create the UI for this menu. Show a preview panel in the
	 * middle of the frame and buttons below to do the swap,
	 * apply changes to imagepanel and close the window.
	 *
	 */
	public Detect() {
		super("Detect");
		imagepanel = null;
		result = null;
		image = null;
		createUI();
	}

	/**
	 * Called when the user hides the window.
	 */

	public void closing() {
		ipPreview.setImageObject(null);
		image = null;
	}

	/**
	 * Called when the user shows the window.
	 */
	public void showing() {
		reset();
	}

	/**
	 * Create the UI. The UI consists of a preview imagepanel
	 * which will show the swapped image, and three buttons to
	 * swap the image, apply changes to the imagepanel and close
	 * the window.
	 */
	private void createUI() {
		ipPreview = new ImagePanel();
		ipPreview.setAutozoom(true);
		ipPreview.addMenu(new SelectBandDialog());
		ipPreview.addMenu(new InfoDialog());
		ipPreview.setSelectionAllowed(true);
		getContentPane().add(ipPreview, BorderLayout.CENTER);
		
	/*	ipPreview.addImageUpdateListener(new ImageUpdateListener() {
			@Override
			public void imageUpdated(ImageUpdateEvent event) {
				if (event.getId() == ImageUpdateEvent.MOUSE_CLICKED) {
					System.out.println("User clicked on : " + event.getObject());
					x=(int)((Point)event.getObject()).x;
					y=(int)((Point)event.getObject()).y;
				}
			}
		});*/

		JPanel controls = new JPanel(new BorderLayout()); 
		
		JPanel slider = new JPanel(new BorderLayout());

		filter = new JComboBox();
		
		filter.addItem("Original");
		filter.addItem("Correlation");
		filter.addItem("Feature");
		
		filter.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if (filter.getSelectedIndex() == 0) {
					sldBand.setVisible(true);
					txtCounter.setVisible(true);
				} else {
					sldBand.setVisible(false);
					txtCounter.setVisible(false);
				}
				updateThresh();				
			}
		});
		
		slider.add(filter, BorderLayout.WEST);
		
		sldBand = new JSlider(0, 100, 0);
		sldBand.setSnapToTicks(true);

		sldBand.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int txt = Integer.parseInt(txtCounter.getText());
				int thresh = sldBand.getValue();
				if (txt != thresh) {
					txtCounter.setText(thresh + "");
					updateThresh();
				}
			}
		});
		slider.add(sldBand, BorderLayout.CENTER);

		txtCounter = new JTextField(4);
		txtCounter.setHorizontalAlignment(JTextField.RIGHT);
		txtCounter.setEditable(true);
		txtCounter.setText("0");
		txtCounter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sldBand.setValue(Integer.parseInt(txtCounter.getText()));
			}
		});

		slider.add(txtCounter, BorderLayout.EAST);
		controls.add(slider, BorderLayout.NORTH);

		JPanel inputs = new JPanel(new FlowLayout());
		
		moveInput = new JTextField("5", 4);
		inputs.add(new JLabel("Move"));
		inputs.add(moveInput);
		samp_row= new JTextField("2",4);
		inputs.add(new JLabel(" Row"));
		inputs.add(samp_row);
		samp_col = new JTextField("2", 4);
		inputs.add(new JLabel("Col"));
		inputs.add(samp_col);
		
		band1 = new JCheckBox("Band 1", true);
		inputs.add(band1);
		band2 = new JCheckBox("Band 2", true);
		inputs.add(band2);
		band3 = new JCheckBox("Band 3", true);
		inputs.add(band3);
		
		controls.add(inputs, BorderLayout.CENTER);
		
		JPanel pnlButtons = new JPanel(new FlowLayout());
	
		pnlButtons.add(new JButton(new AbstractAction("Detect PCA") {
			public void actionPerformed(ActionEvent e) {
				try {
					detect("PCA");
				} catch (ImageException e1) {
					e1.printStackTrace();
				}
			}
		}));
		
		
		
		pnlButtons.add(new JButton(new AbstractAction("Detect edge") {
			public void actionPerformed(ActionEvent e) {
				try {
					detect("EDGE");
				} catch (ImageException e1) {
					e1.printStackTrace();
				}
			}
		}));

		pnlButtons.add(new JButton(new AbstractAction("Detect HSV") {
			public void actionPerformed(ActionEvent e) {
				try {
					detect("HSV");
				} catch (ImageException e1) {
					e1.printStackTrace();
				}
			}
		}));
		pnlButtons.add(new JButton(new AbstractAction("resize") {
			public void actionPerformed(ActionEvent e) {	
					my_resize();				
		
			}
		}));
		
		pnlButtons.add(new JButton(new AbstractAction("Apply") {
			public void actionPerformed(ActionEvent e) {
				if (ipPreview.getImageObject() == null) {
					try {
						detect("PCA");
					} catch (ImageException e1) {
						e1.printStackTrace();
					}
				}
				ImageObject imgobj = ipPreview.getImageObject();
				imagepanel.setImageObject(imgobj);
			}
		}));
		
	pnlButtons.add(new JButton(new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				Detect.this.setVisible(false);
			}
		}));
		controls.add(pnlButtons, BorderLayout.SOUTH);
		getContentPane().add(controls, BorderLayout.SOUTH);
		pack();
	}


	
	private void my_resize(){
		// TODO Auto-generated method stub
		row_scale=Double.parseDouble(samp_row.getText());
		col_scale=Double.parseDouble(samp_col.getText());
		ImageObject imgobj=null;
		try {
			imgobj = Sampling.SamplingLinearInt(imagepanel.getImageObject(),col_scale,row_scale);
		} catch (GeoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		image=imgobj;
		reset();
	/*	if(width==0|height==0)
			return;
		
			try {
				ImageLoader.writeImage("../images/"+index+".tif", image.crop(new SubArea(x-width/2,y-height/2,width,height)).convert(ImageObject.TYPE_BYTE, true));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		index++;*/
		
	}
	private void reset() {
		if (image == null)
			image = imagepanel.getImageObject();
		ipPreview.setImageObject(image);
	}

	private void updateThresh() {
		if (result == null)
			return;
		if (filter.getSelectedIndex() == 1)
			image = result;
		else if(filter.getSelectedIndex() == 0){
			double thresh = Integer.parseInt(txtCounter.getText());
			int move = Integer.parseInt(moveInput.getText());
			
			try {
				ImageObject original_image = Sampling.SamplingLinearInt(imagepanel.getImageObject(),col_scale,row_scale);
				image = ImageObject.createImage(original_image.getNumRows(),original_image.getNumCols(),original_image.getNumBands(),original_image.getType());
				System.out.println("Generating the processed image");
				for (int i = 0; i < result.getNumRows(); i+=move)
					for (int j = 0; j < result.getNumCols(); j+=move) {
						boolean is_set = false;
						for (int k = 0; k < result.getNumBands(); k++)
							if (result.getDouble(i, j, k)*100 >= thresh) {
								is_set= true;
								break;
							}
						if (is_set)
							setDoubleRegion(i,j,height,width,original_image,image);
					}
				
	
			} catch (ImageException e) {
				e.printStackTrace();
			}
		}else if (filter.getSelectedIndex() == 2)
				image = feature;
		
			
				try {
					System.out.println("Processed and now resizing");
					image=Sampling.SamplingLinearInt(image,1/col_scale, 1/row_scale);
					System.out.println("finished");
				} catch (GeoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ImageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				// TODO Auto-generated catch block
			
			
		
		reset();		
	}

	private void setDoubleRegion(int r, int c,int height,int width,ImageObject refImage,ImageObject targetImage){
		int r_max = Math.min(r+height, refImage.getNumRows());
		int c_max = Math.min(c+width, refImage.getNumCols());
		for (int b = 0; b < refImage.getNumBands(); b++)
			for(int i=r;i< r_max;i++)
				for(int j=c;j< c_max;j++)
					targetImage.setDouble(i,j,b,refImage.getDouble(i,j,b));	
	}
	
	private void detect(String method) throws ImageException {
		row_scale=Double.parseDouble(samp_row.getText());
		col_scale=Double.parseDouble(samp_col.getText());
		ImageObject imgobj =Sampling.SamplingLinearInt(imagepanel.getImageObject(),col_scale,row_scale); 
		System.out.println("original size "+imagepanel.getImageObject().getNumCols()+"  "+imagepanel.getImageObject().getNumRows());
		System.out.println("resized version "+ imgobj.getNumCols()+"  "+imgobj.getNumRows());
		int i=0;
		int j=0;
		ImageObject tmp; 
		int move = Integer.parseInt(moveInput.getText());
		int result_bands=1;
		
		ArrayList<Integer> bands = new ArrayList<Integer>();
		if (band1.isSelected())
			bands.add(new Integer(0));
		if (band2.isSelected())
			bands.add(new Integer(1));		
		if (band3.isSelected())
			bands.add(new Integer(2));		
		
		if ((imgobj == null) || !imgobj.isDataValid() || ipPreview.getSelection() == null) {
			return;
		}   
		ic=new ImageCompare();	
		
		if ( method.equalsIgnoreCase("PCA")){			
			PCA p=new PCA(); 	    	
			p.computeLoadings(imgobj);
			feature=(p.applyPCATransform(imgobj));
		} 
		else if(method.equalsIgnoreCase("HSV")){ 
			if(!cm.convertRGB2HSV(imgobj)) 
				logger.error("Could not convert image.");
			else
				feature = cm.getConvertedIm();
		}
		else if(method.equalsIgnoreCase("EDGE")){
			Edge ed=new Edge();
			if(ed.RobertsVar(imgobj))
				feature=ed.GetEdgeImageObject();
			else{
				logger.error("Can't compute the edge image");
				return;
			}
			// The edge image only has one band so we have to should remove all the other bands if they are selected from 
			//the band array
			bands.clear();
			bands.add(new Integer(0));
		}
		if(feature==null){
			logger.error("Edge image is null");
			return;
		}
		height=(int)(ipPreview.getSelection().height*row_scale);
		width=(int)(ipPreview.getSelection().width*col_scale);
		ImageObject crop = feature.crop(new SubArea((int)(ipPreview.getSelection().x*col_scale),(int)(ipPreview.getSelection().y*row_scale),width,height));
		result=ImageObject.createImage(feature.getNumRows(), feature.getNumCols(), result_bands, ImageObject.TYPE_DOUBLE);
		int[] band_array = new int[bands.size()];
		i = 0;
		for (Integer b : bands)
			band_array[i++] = b.intValue();

		ic.setOriginalImage(crop.extractBand(band_array));
		
		for(i=0; i+width < feature.getNumCols();i+=move)	 
			for(j=0; j+height < feature.getNumRows();j+=move){
				tmp = feature.crop(new SubArea(i,j,width,height));
				ic.setTestImage(tmp.extractBand(band_array));
				try {
					double[][][] foo = ic.correlation();
					for(int b=0;b<result_bands;b++)
						result.setDouble(j, i, b, Math.abs(foo[0][b][b]));
					

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		// set the preview pane
		logger.warn("Job is Done");
		
		updateThresh();
	}

	// ------------------------------------------------------------
	// Im2LearnMenu implementation
	// ------------------------------------------------------------
	/**
	 * Store the reference to the imagepanel for later use.
	 * The imagepanel passed in as argument is the
	 * imagepanel to which this tools is associated.
	 */
	public void setImagePanel(ImagePanel imagepanel) {
		this.imagepanel = imagepanel;
	}

	/**
	 * For this tool there is no operation that works on the
	 * imagepanel directly and thus we return null
	 * indicating that no menu entries need to be created on
	 * the panel.
	 */

	

	/* public double compareABS(ImageObject O1, ImageObject O2) throws ImageException {
    	if (O1.getSize() !=O2.getSize()) {
        	logger.error("Images should have the same size");
        	return -1;
        }

    	double result=0;

    	for (int i=0;i<O1.getSize();i++) 
   			result += Math.abs(O1.getDouble(i) - O2.getDouble(i));

    	return result / (O1.getNumCols()*O1.getNumRows());
    }

    public double compareL2(ImageObject O1, ImageObject O2) throws ImageException {
    	if (O1.getSize() !=O2.getSize()) {
        	logger.error("Images should have the same size");
        	return -1;
        }

    	double result=0;

    	for (int i=0;i<O1.getSize();i++) {
    		double diff = O1.getDouble(i) - O2.getDouble(i);
   			result += diff*diff;
    	}
    	result=Math.sqrt(result);

    	return result;
    }*/


	

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

		JMenuItem item = new JMenuItem(new AbstractAction("Detect") {
			public void actionPerformed(ActionEvent e) {
				if (!isVisible()) {
					Window win = SwingUtilities.
					getWindowAncestor(imagepanel);
					setLocationRelativeTo(win);
					setVisible(true);
				}
				toFront();
			}
		});
		menu.add(item);

		return new JMenuItem[] { menu };
	}

	/**
	 * When a new image is loaded make sure that the current
	 * swapped image is removed, i.e. perform a reset. Only
	 * need to reset the image if this frame is visible.
	 */
	public void imageUpdated(ImageUpdateEvent event) {
		if (!isVisible()) {
			return;
		}
		if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
			reset();
		}
	}

	/**
	 * Return the right documentation depending on what node
	 * is selected, in this case only the Swap node should
	 * exist.
	 */
	public URL getHelp(String menu) {
		if (menu.equals("Swap")) {
			return getClass().getResource("help/swap.html");
		} else {
			return null;
		}
	}
}
