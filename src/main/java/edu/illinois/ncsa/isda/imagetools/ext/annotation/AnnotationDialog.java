package edu.illinois.ncsa.isda.imagetools.ext.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.ext.annotation.Annotation.AnnotationType;


public class AnnotationDialog extends Im2LearnFrame implements Im2LearnMenu, MouseListener, MouseMotionListener {

	private ImagePanel imagepanel;
	private JCheckBoxMenuItem enable;
	private Annotation annotation;
	private boolean oldselection;
	private JButton color;
	private JSlider linethickness;
	private JTextArea text;
	private JComboBox combo;
	private JSlider arrowhead;
	private boolean _autoLoc = false;
	private boolean _inColorChooser = false;
	
	public AnnotationDialog() {
		super("Annotation Dialog");
		init();
	}
	
	public AnnotationDialog(boolean autoLoc, boolean visibleColorChooser) {
		super("Annotation Dialog");
		_autoLoc = autoLoc;
		_inColorChooser = visibleColorChooser;
		init();
	}
	
	void init() {
	
		imagepanel = null;
		annotation = new Annotation();
		
		// create the UI
		JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, BorderLayout.SOUTH);
		
		JPanel centerP = new JPanel(new BorderLayout());
		text = new JTextArea(10, 60);
				
		centerP.add(new JScrollPane(text), BorderLayout.CENTER);
		
		if(_inColorChooser) {
			final JColorChooser cc = new JColorChooser(annotation.getColor());
			cc.getSelectionModel().addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					color.setForeground(cc.getColor());
					annotation.setColor(cc.getColor());
					if (imagepanel != null) {
						imagepanel.repaint();
					}
									}});
			centerP.add(cc, BorderLayout.SOUTH);
		}
		
		add(centerP, BorderLayout.CENTER);
		
		JPanel top = new JPanel(new FlowLayout());
		add(top, BorderLayout.NORTH);
		
		combo = new JComboBox(AnnotationType.values());
		top.add(combo);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				annotation.setType(AnnotationType.valueOf(combo.getSelectedItem().toString()));
				if (imagepanel != null) {
					imagepanel.repaint();
				}
			}			
		});
		combo.setSelectedItem(annotation.getType().toString());
		
		JPanel tmp = new JPanel(new BorderLayout());
		top.add(tmp);
		tmp.add(new JLabel("Linethickness", JLabel.CENTER), BorderLayout.NORTH);
		linethickness = new JSlider(1, 50, 1);
		tmp.add(linethickness, BorderLayout.CENTER);
		linethickness.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				annotation.setLineThickness(linethickness.getValue());
				if (imagepanel != null) {
					imagepanel.repaint();
				}
			}			
		});
		linethickness.setValue(annotation.getLineThickness());
		
		tmp = new JPanel(new BorderLayout());
		top.add(tmp);
		tmp.add(new JLabel("Arrowhead size", JLabel.CENTER), BorderLayout.NORTH);
		arrowhead = new JSlider(1, 50, 1);
		tmp.add(arrowhead, BorderLayout.CENTER);
		arrowhead.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				annotation.setArrowHeadSize(arrowhead.getValue());
				if (imagepanel != null) {
					imagepanel.repaint();
				}
			}			
		});
		arrowhead.setValue(annotation.getArrowHeadSize());
		
		color = new JButton("Color");
		color.setOpaque(true);
		color.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = JColorChooser.showDialog(AnnotationDialog.this, "Annotation Color", annotation.getColor());
				if (c != null) {
					color.setForeground(c);
					annotation.setColor(c);
					if (imagepanel != null) {
						imagepanel.repaint();
					}
				}
			}
		});
		color.setForeground(annotation.getColor());
		
		color.setVisible(!_inColorChooser);
		
		
		
		top.add(color);
		
		buttons.add(new JButton(new AbstractAction("Append") {
			public void actionPerformed(ActionEvent e) {
				Annotation a = (Annotation)Im2LearnUtilities.deepclone(annotation);
				a.setText(text.getText());
				ImageObject imgobj = imagepanel.getImageObject();
				ArrayList<Annotation> al;
				al = (ArrayList<Annotation>)imgobj.getProperty("IMAGE_ANNOTATION");
				if (al == null) {
					al = new ArrayList<Annotation>();
					imgobj.setProperty("IMAGE_ANNOTATION", al);
				}
				al.add(a);
				reset();
				imagepanel.addAnnotationImage(a);	
				imagepanel.repaint();
			}			
		}));
		
		buttons.add(new JButton(new AbstractAction("Reset") {
			public void actionPerformed(ActionEvent e) {
				reset();
			}			
		}));	
		
		buttons.add(new JButton(new AbstractAction("Apply All to image") {
            public void actionPerformed(ActionEvent e) {
            	
				ImageAnnotation[] anns = imagepanel.getAllAnnotationsImage();
				imagepanel.setImageObject(getAnnotatedImage(imagepanel.getImageObject(), anns));
				
//            	try {
//            		ImageAnnotation[] anns = imagepanel.getAllAnnotationsImage();
//            		if(anns == null)
//            			return;
//            		
//            		ImageObject orgIm = imagepanel.getImageObject();
//            		
//            		BufferedImage im = orgIm.makeBufferedImage();
//            		Graphics2D g2d = (Graphics2D)im.getGraphics();
//            		
//            		for(int i=0;i<anns.length; i++) {
//            			((Annotation)anns[i]).paint(g2d, null);
//            		}
//            	
//	            	ImageObject imobj = ImageObject.getImageObject(im);
//	            	
//	            	
//	            	HashMap<String, Object> hm = imagepanel.getImageObject().getProperties();
//	            	hm.remove("_MIP");
//	            	imobj.setProperties(hm);
//	            	imagepanel.setImageObject(imobj);
//				
//            	} catch (ImageException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
            	
            }
        }));
		
		buttons.add(new JButton(new AbstractAction("Done") {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}			
		}));	
		
		pack();
	}
	
		private void reset() {
		annotation.reset();

		// TODO reset the UI
		linethickness.setValue(annotation.getLineThickness());
		color.setForeground(annotation.getColor());
		combo.setSelectedItem(annotation.getType().toString());
		text.setText(annotation.getText());

		imagepanel.repaint();
}
		
		public static ImageObject getAnnotatedImage(ImageObject orgIm, ImageAnnotation[] anns) {
			try {

				if(anns == null)
					return null;
				
			
				BufferedImage im = orgIm.makeBufferedImage();
				Graphics2D g2d = (Graphics2D)im.getGraphics();
	            		
				for(int i=0;i<anns.length; i++) {
					if(anns[i] instanceof Annotation) {
						((Annotation)anns[i]).setVisible(true);
						((Annotation)anns[i]).paint(g2d, null);
					}
				}
	            	
				ImageObject imobj = ImageObject.getImageObject(im);
		            	
		            	
				HashMap<String, Object> hm = orgIm.getProperties();
				hm.remove("_MIP");
				imobj.setProperties(hm);
				
							
			return imobj;
				
					
			} catch (ImageException e1) {
						// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
			
			
		}

		
	public void showing() {
		reset();		
		oldselection = imagepanel.isSelectionAllowed();
		imagepanel.setSelectionAllowed(false);
		imagepanel.addMouseListener(this);
		imagepanel.addMouseMotionListener(this);
		annotation.setVisible(true);	
	}
	
	public void closing() {
		imagepanel.setSelectionAllowed(oldselection);
		imagepanel.removeMouseListener(this);
		imagepanel.removeMouseMotionListener(this);
		annotation.setVisible(false);	
	}
		
	// ----------------------------------------------------------------------
	// Im2LearnMenu
	// ----------------------------------------------------------------------
	public URL getHelp(String topic) {
		return null;
	}

	public JMenuItem[] getMainMenuItems() {
		return null;
	}

	public JMenuItem[] getPanelMenuItems() {
		JMenuItem menu = new JMenuItem(new AbstractAction("Annotation") {
			public void actionPerformed(ActionEvent e) {
				
				if(_autoLoc) {
					setLocationRelativeTo(getOwner());
					Point p = imagepanel.getParent().getLocationOnScreen();//_imPanel.getLocationOnScreen();
					setLocation((int)p.getX()+imagepanel.getParent().getWidth()+5,0);
				}
				 
				setVisible(true);
			}
		});
		return new JMenuItem[]{menu};		
	}

	public void setImagePanel(ImagePanel imagepanel) {
		this.imagepanel = imagepanel;
		imagepanel.addAnnotationPanel(annotation);
	}

	public void imageUpdated(ImageUpdateEvent event) {
		if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
			// TODO add code to check for annotations and add
		}
	}

	// ----------------------------------------------------------------------
	// MouseListener
	// ----------------------------------------------------------------------
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}
		annotation.clearPoints();
		annotation.addPoint(imagepanel.getImageLocation(e.getPoint()));
		imagepanel.repaint();
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}
		annotation.addPoint(imagepanel.getImageLocation(e.getPoint()));
		imagepanel.repaint();
		
		this.setVisible(true);
	}

	// ----------------------------------------------------------------------
	// MouseMotionListener
	// ----------------------------------------------------------------------
	public void mouseDragged(MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0) {
			return;
		}
		annotation.addPoint(imagepanel.getImageLocation(e.getPoint()));
		imagepanel.repaint();
	}

	public void mouseMoved(MouseEvent e) {
	}
}
