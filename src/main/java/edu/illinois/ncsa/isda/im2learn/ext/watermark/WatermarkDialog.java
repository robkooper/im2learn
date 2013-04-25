package edu.illinois.ncsa.isda.im2learn.ext.watermark;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectionDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;



public class WatermarkDialog extends Im2LearnFrame implements Im2LearnMenu{
	private ImagePanel imagePanel = null;
	private Watermark wm;
	private WatermarkAnnotation wma;

	private JSlider slider;
	private JTextField sliderField;
	private JComboBox sizeBox;
	private JComboBox colorBox;
	private JTextField textField = new JTextField();
	private  JPanel cont;
	private JPanel row1;
	private JPanel row2;
	private JPanel row3;

	public WatermarkDialog() {
		wm = new Watermark();
		wma = new WatermarkAnnotation(wm);
		setPreferredSize(new Dimension(320, 240));
		createGUI();
		pack();
	}
	
    private void createGUI() {
        cont = new JPanel();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        add(cont, BorderLayout.CENTER);      
        cont.setSize(new Dimension(100, 100));
        
        //set up panels/locations
        /*
        JPanel bottomLeft = new JPanel();
        cont.add(bottomLeft, BorderLayout.WEST);
        JPanel bottomRight = new JPanel();
        cont.add(bottomRight, BorderLayout.EAST);     
        JPanel bottomRightUp = new JPanel();
        bottomRight.add(bottomRightUp, BorderLayout.NORTH);
        JPanel bottomRightDown = new JPanel();
        bottomRight.add(bottomRightDown, BorderLayout.SOUTH);
        */
        //labels
        
        row1 = new JPanel();
        row2 = new JPanel();
        row3 = new JPanel();
        
        row1.setLayout(new FlowLayout());
        row2.setLayout(new FlowLayout());
        row3.setLayout(new FlowLayout());
        
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cont.add(row1);
        cont.add(row2);
        cont.add(row3);
        
        JLabel testLabel = new JLabel ("Enter Text:");
        row1.add(testLabel);
        
        JLabel sliderLabel = new JLabel("    Opacity:");
        row3.add(sliderLabel, BorderLayout.CENTER);
        

        
        //adding slider
        slider = new JSlider();
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        row3.add(slider);
        
        	        

        //listen to slider
        slider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		
        		JSlider slider = (JSlider)e.getSource();
        		if (!slider.getValueIsAdjusting()){
        			//code to handle slider change
        			int value = slider.getValue();
        			System.out.println(value);
        			updateSlider(value);
        		}
        		
        	}
            });
        
        
        //adding text field
        //allows for annotation input

        textField.setColumns(18);
        row1.add(textField);
        
        //listen to annotation textfield
        textField.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		JTextField field = (JTextField) e.getSource();
        		String str = field.getText();
        		wm.setText(str);
        		imagePanel.repaint();
        	}
        });
        
        textField.addCaretListener(new CaretListener(){
        	public void caretUpdate(CaretEvent e)
        	{
        		JTextField field = (JTextField) e.getSource();
        		String str = field.getText();
        		wm.setText(str);
        		imagePanel.repaint();
        	}
        });

        //adding slider textfield
        sliderField = new JTextField();
        sliderField.setColumns(2);
        row3.add(sliderField, BorderLayout.EAST);
        String str = java.lang.String.valueOf(slider.getValue());
        sliderField.setText(str);
        
        //listen to slider text field
        sliderField.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		JTextField field = (JTextField) e.getSource();
        		int value = java.lang.Integer.decode(field.getText());
        		updateSlider(value);
        	}
        });
        
        
/* This section is for adding the lower left corner of the panel
 * Includes: two label fields and two text fiels for size and color
*/
//adding labels

        JLabel sizeLabel = new JLabel("Size:  ");
        sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel colorLabel = new JLabel("       Color:  ");
        colorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
        row2.add(sizeLabel);

        //adding combobox for font size
        String[] sizes = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};
        sizeBox = new JComboBox(sizes);
        sizeBox.setMaximumRowCount(8);
        sizeBox.setSelectedIndex(5);
        sizeBox.setEditable(true);
        sizeBox.setPreferredSize(new Dimension(60,25));
        sizeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(sizeBox);
        
        //listen to size combo box
        sizeBox.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		JComboBox source = (JComboBox)e.getSource();
        		int size;
        		try
        		{
        		   size= Integer.parseInt((String)(source.getSelectedItem()));
        		}
        		catch(java.lang.NumberFormatException a)
        		{
        			size=14;
        	    }
        		wm.setFontSize(size);
        		imagePanel.repaint();
        	}
        });
        
        row2.add(colorLabel);
        //add combobox for color
        String[] colors = {"White", "Black", "Blue", "Red", "Green"};
        colorBox = new JComboBox(colors);
        colorBox.setSelectedIndex(1);
        colorBox.setPreferredSize(new Dimension(70,25));
        colorBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(colorBox);
       
        JLabel space = new JLabel("        ");
        row2.add(space);
        
        //listen to color combobox
        colorBox.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		JComboBox source = (JComboBox)e.getSource();
        		String color = (String) (source.getSelectedItem());
        		if (color.equals("White"))
        			wm.setTextColor(Color.WHITE);
        		else if (color.equals("Black"))
        			wm.setTextColor(Color.BLACK);
        		else if (color.equals("Blue"))
        			wm.setTextColor(Color.BLUE);
        		else if (color.equals("Red"))
        			wm.setTextColor(Color.RED);
        		else
        			wm.setTextColor(Color.GREEN);
        		imagePanel.repaint();
        	}
        });      	
        
        // buttons
        JPanel buttons = new JPanel(new FlowLayout());
        add(buttons, BorderLayout.SOUTH);
        
        buttons.add(new JButton(new AbstractAction("APPLY ALL") {
			public void actionPerformed(ActionEvent e) {
				System.out.println("apply");
				ImageObject imgobj = imagePanel.getImageObject();
				try {
					ImageObject newimg = wm.apply(imgobj);
//					, imagePanel.getRedBand(), imagePanel.getGreenBand(), imagePanel.getBlueBand());
					imagePanel.setImageObject(newimg);
				} catch (ImageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}        	
        }));
        buttons.add(new JButton(new AbstractAction("APPLY") {
			public void actionPerformed(ActionEvent e) {
				System.out.println("apply");
				ImageObject imgobj = imagePanel.getImageObject();
				try {
					int r = imagePanel.getRedBand();
					int g = imagePanel.getGreenBand();
					int b = imagePanel.getBlueBand();
					ImageObject newimg = wm.apply(imgobj, r, g, b);					
					imagePanel.setImageObject(newimg);
					imagePanel.setRGBBand(r, g, b);
				} catch (ImageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}        	
        }));
        buttons.add(new JButton(new AbstractAction("RESET") {
			public void actionPerformed(ActionEvent e) {
				System.out.println("reset");
				//wm.reset();
				imagePanel.repaint();
			}        	
        }));
        
        //Create the menu bar.  Make it have a green background.
        JMenuBar greenMenuBar = new JMenuBar();
        greenMenuBar.setOpaque(true);
        greenMenuBar.setBackground(new Color(154, 165, 127));
       // greenMenuBar.setPreferredSize(new Dimension(200, 20));
        
//    	adding menu items
    	JMenu menu = new JMenu("File");
    	greenMenuBar.add(menu);
    	menu.add("Load Image...");
    	menu.add("Save Image...");
    	//cont.add(greenMenuBar);
    	
//  	listen to "load" command
    	menu.getItem(0).addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent e)
    		{
    			ImageObject img = null;
    			System.out.println("Load Selected.");
    	          FileChooser fc = new FileChooser();
    	          fc.setTitle("Load Image:");
    	          try{
    	              String filename = fc.showOpenDialog();
    	              if(filename != null){
    	            	  
    	                 img = ImageLoader.readImage(filename);
    	              }
    	             
    	          }catch(Exception exc){
    	              System.err.println("Error");
    	          }


    	    	imagePanel.setImageObject(img);
    		}
    	});

//        listen to "save" command
        menu.getItem(1).addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		System.out.println("Save Selected.");
        		FileChooser fc = new FileChooser();
        		fc.setTitle("Save Image");
        		try {
        			String filename = fc.showSaveDialog();
        			if (filename != null) {
        				ImageLoader.writeImage(filename, imagePanel.getImageObject());
        			}
        		} catch (IOException exc) {
        		}
        	}
        });        	
    }
    

    @Override
	public void closing() {
		imagePanel.removeAnnotationImage(wma);
		System.out.println("Closing");
		super.closing();
	}

	@Override
	public void showing() {
		imagePanel.addAnnotationImage(wma);
		System.out.println("Showing");
		super.showing();
	}

	private void updateSlider(int value)
	{
		slider.setValue(value);
		sliderField.setText(java.lang.String.valueOf(value));
		wm.setAlpha((float)value/100);
		imagePanel.repaint();
	}
	// ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    
    //What does this do?
    public JMenuItem[] getPanelMenuItems() {
        JMenuItem menu = new JMenuItem(new AbstractAction("Add Watermark") {
            public void actionPerformed(ActionEvent e) {
                setLocationRelativeTo(getOwner());
                setVisible(true);
            }
        });
        return new JMenuItem[]{menu};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    //What does this do?
    public void imageUpdated(ImageUpdateEvent event) {
    	if (isVisible()) {
    		switch (event.getId()) {
    		case ImageUpdateEvent.NEW_IMAGE:
    			showing();
    			break;
    		case ImageUpdateEvent.MOUSE_CLICKED:
    			System.out.println(event);
    			System.out.println(event.getObject());
    			wm.setX((int)((Point)event.getObject()).getX());
    			wm.setY((int)((Point)event.getObject()).getY());
    			wm.setText(textField.getText());
    			imagePanel.repaint();
    		}
    	}
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}
