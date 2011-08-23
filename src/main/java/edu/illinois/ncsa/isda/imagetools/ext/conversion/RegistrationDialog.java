/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License according to
 * http://www.otm.uiuc.edu/faculty/forms/opensource.asp
 * 
 * Copyright (c) 2006,    NCSA/UIUC.  All rights reserved.
 * 
 * Developed by:
 * 
 * Name of Development Groups:
 * Image Spatial Data Analysis Group (ISDA Group)
 * http://isda.ncsa.uiuc.edu/
 * 
 * Name of Institutions:
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.uiuc.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 *   Neither the names of University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.isda.imagetools.ext.conversion;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.jai.JAIutil;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.imagetools.ext.misc.JSelectFile;

/**
 * 
 * <p>
 * <B>The class Registration provides a tool for manual registration of two
 * images by mouse-selecting corresponding control points and performing affine
 * transformation to bring the second image into the coordinate system of the
 * first image. </B>
 * </p>
 * 
 * <p>
 * <b>Description:</b> This class can be used for registering any two images
 * where a set of at least three corresponding pairs of control points can be
 * established.
 * 
 * The user first selects 2 images by using the file choosers invoked by
 * clicking on the button "...". The image can be previewed by pressing the
 * button "View". After pressing the button "Load", both images will be loaded
 * into their image panel. With the left mouse click, a user selects
 * corresponding salient points. The points are shown as overlaid crosses with a
 * small index number next to each cross (see the picture below). If a user made
 * a mistake in the feature/point selection, the points can be removed by
 * pressing the button "Reset". The order in which the features/points are
 * selected is important since the same order is used to match point X in image1
 * with point X in image 2. <p/> <img src="help/imageRegistrationDialog.jpg"> <BR>
 * <BR>
 * After selecting the features, a user might preview the transformation outcome
 * by pressing the button "Check". A new frame will appear with the slider bar
 * at the bottom (see below). By moving the slider bar, the transparency of
 * image1 with respect to the transparency of image2 is changing from seeing
 * only image1 (left position) through seeing both (middle position) and seeing
 * only image2 (right position). <BR>
 * <img src="help/imageRegistrationDialog1.jpg"> <BR>
 * <BR>
 * The final transformation is performed by pressing the button
 * "Transform Image 2" and viewing the trasnformed image2 in the coordinate
 * system of the image1 by pressing the button "Preview". The resulting image
 * can be saved (the button "Save") or transfered to the main frame (the button
 * "Apply"). <BR>
 * <img src="help/imageRegistrationDialog2.jpg"> <BR>
 * <BR>
 * 
 * 
 * 
 * 
 * @author Sang-Chul Lee
 * @author Rob Kooper
 * @author Peter Bajcsy (documentation)
 * @version 1.0
 */
public class RegistrationDialog extends Im2LearnFrame implements Im2LearnMenu {
	private JButton		btnTransform;
	private JButton		btnPreview;
	private JButton		btnSave;
	private JButton		btnApply;

	private ImageObject	imgResult;
	private ImagePanel	imagepanel	= null;
	private UI			ui			= null;
	private JSelectFile	sfImages;
	private ImageFrame	preview;

	private static Log	logger		= LogFactory.getLog(RegistrationDialog.class);

	/**
	 * Default constructor.
	 * 
	 * @param owner
	 *            the mainframe which this dialog belongs to.
	 */
	public RegistrationDialog() {
		super("Image Registration");
		setResizable(true);
		if (JAIutil.haveJAI()) {
			try {
				createUI();
			} catch (ClassNotFoundException exc) {
				logger.error("Error createing registration UI", exc);
			}
		}
	}

	/**
	 * Create the UI, will have 3 sections, one to load the images, one to
	 * select the features, and one with all the buttons.
	 * 
	 * @throws ClassNotFoundException
	 *             if JAI is not found.
	 */
	private void createUI() throws ClassNotFoundException {
		// preview frame
		preview = new ImageFrame("Preview Calibrate");
		preview.setSize(640, 480);

		// ncsa.im2learn.main ui allowing to select features.
		ui = new UI();
		ui.setBorder(new TitledBorder("Feature Selection"));
		ui.setEnabled(false);

		// Selection of files to use
		sfImages = new JSelectFile(new String[] { "Image 1", "Image 2" });
		sfImages.setToolTipText(0, "Leave blank to use image shown in mainframe.");
		sfImages.setBorder(new TitledBorder("Select Images"));

		// convert preview save apply close buttons
		JPanel buttons = new JPanel(new FlowLayout());

		JButton btn = new JButton(new AbstractAction("Load") {
			public void actionPerformed(ActionEvent e) {
				try {
					ui.setImage1(sfImages.getImageObject(0, imagepanel.getImageObject()));
					ui.setImage2(sfImages.getImageObject(1));
				} catch (IOException exc) {
					logger.error("Could not load images.");
				} catch (ImageException exc) {
					logger.error("Could not set images.");
				}
			}
		});
		buttons.add(btn);

		btn = new JButton(new AbstractAction("Reset") {
			public void actionPerformed(ActionEvent e) {
				ui.resetMarkers();
				btnTransform.setEnabled(false);
				btnPreview.setEnabled(false);
				btnSave.setEnabled(false);
				btnApply.setEnabled(false);
			}
		});
		buttons.add(btn);

		btnTransform = new JButton(new AbstractAction("Transform Image 2") {
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				btnPreview.setEnabled(false);
				btnSave.setEnabled(false);
				btnApply.setEnabled(false);
				try {
					imgResult = transform();
					btnPreview.setEnabled(true);
					btnSave.setEnabled(true);
					btnApply.setEnabled(true);
				} catch (IOException exc) {
					logger.error("Error loading image.", exc);
				} catch (ImageException exc) {
					logger.error("Error transforming image.", exc);
				}
				setCursor(Cursor.getDefaultCursor());
			}
		});
		btnTransform.setEnabled(false);
		buttons.add(btnTransform);

		// show a preview of the result, frame is forced to be 640x480 by
		// default (user can always resize). The frame is reused when
		// previewing many results.
		btnPreview = new JButton(new AbstractAction("Preview") {
			public void actionPerformed(ActionEvent e) {
				preview.setImageObject(imgResult);
				preview.setVisible(true);
			}
		});
		btnPreview.setEnabled(false);
		buttons.add(btnPreview);

		// save the result to disk
		btnSave = new JButton(new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				FileChooser dialog = new FileChooser();
				dialog.setTitle("Save Result RGB Conversion");
				try {
					String filename = dialog.showSaveDialog();
					if (filename != null) {
						ImageLoader.writeImage(filename, imgResult);
					}
				} catch (IOException exc) {
					logger.error("Error saving file", exc);
				}
				setCursor(Cursor.getDefaultCursor());
			}
		});
		btnSave.setEnabled(false);
		buttons.add(btnSave);

		// copy the result back to imagepanel
		btnApply = new JButton(new AbstractAction("Apply") {
			public void actionPerformed(ActionEvent e) {
				imagepanel.setImageObject(imgResult);
			}
		});
		btnApply.setEnabled(false);
		buttons.add(btnApply);

		btn = new JButton(new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				RegistrationDialog.this.setVisible(false);
			}
		});
		buttons.add(btn);

		// combine all the pieces
		this.getContentPane().add(sfImages, BorderLayout.NORTH);
		this.getContentPane().add(ui, BorderLayout.CENTER);
		this.getContentPane().add(buttons, BorderLayout.SOUTH);

		pack();
	}

	/**
	 * Transform image 2 with respect to image 1. This uses the features that
	 * are selected by the user.
	 * 
	 * @return second image transformed to match image 1
	 * @throws ImageException
	 *             if the transform did not work.
	 * @throws IOException
	 *             if the second image could not be loaded.
	 */
	private ImageObject transform() throws ImageException, IOException {
		if (!ui.isTransformValid()) {
			throw (new ImageException("Not enough points selected, min is 3."));
		}

		// calculate the affine transform base on the points selected
		Point[] f1 = ui.getPointsInImage1();
		Point[] f2 = ui.getPointsInImage2();

		if (ui.getTransformationModel() == 0) {
			return Registration.getImageTransformed(sfImages.getImageObject(1), f1, f2);
		} else if (ui.getTransformationModel() == 1) {
			return Registration.getImageTransformedRigid(sfImages.getImageObject(1), f1, f2);
		} else if (ui.getTransformationModel() == 2) {
			return Registration.getImageTransformedWarp(sfImages.getImageObject(1), ui.getWarpModel());
		} else {
			logger.error("Transformation model is not specified");
			return null;
		}
	}

	/**
	 * Hides the Dialog. This will also hide the preview and free all memory.
	 */
	@Override
	public void closing() {
		ui.resetImages();
		preview.setVisible(false);
		btnTransform.setEnabled(false);
		btnPreview.setEnabled(false);
		btnSave.setEnabled(false);
		btnApply.setEnabled(false);
		imgResult = null;
		sfImages.reset(false);
	}

	// ------------------------------------------------------------------------
	// generic UI part
	// ------------------------------------------------------------------------

	/**
	 * Extend the UI to use the knowledge of the image and add buttons.
	 */
	class UI extends RegistrationDialogUI {
		public UI() throws ClassNotFoundException {
			super();
		}

		/**
		 * Allow to transform if enough markers are placed.
		 */
		@Override
		public void addMarker() {
			btnTransform.setEnabled(isTransformValid());
		}
	}

	// ------------------------------------------------------------------------
	// Im2LearnMenu implementation
	// ------------------------------------------------------------------------
	public void setImagePanel(ImagePanel imagepanel) {
		this.imagepanel = imagepanel;
	}

	public JMenuItem[] getPanelMenuItems() {
		return null;
	}

	public JMenuItem[] getMainMenuItems() {
		// no ui means no menu
		if (ui == null) {
			return null;
		}

		JMenu tools = new JMenu("Tools");

		JMenuItem menu = new JMenuItem("Image Registration");
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setLocationRelativeTo(getOwner());
				RegistrationDialog.this.setVisible(true);
			}
		});
		tools.add(menu);

		return new JMenuItem[] { tools };
	}

	public void imageUpdated(ImageUpdateEvent event) {
	}

	public URL getHelp(String menu) {
		if (menu.equals("Image Registation")) {
			return getClass().getResource("help/RegistrationDialog.html");
		}
		return null;
	}
}
