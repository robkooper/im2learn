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
package edu.illinois.ncsa.isda.im2learn.ext.vis;

/*
 * ImageExtractDialog.java
 * 
 * 
 */

/**
 * 
 * @author Sang-Chul Lee
 * @version 1.0
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.ext.calculator.ImageCalculatorDialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.ChangeTypeDialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.GrayScaleDialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.PCADialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.RegistrationDialog;
import edu.illinois.ncsa.isda.im2learn.ext.info.CoordValDialog;
import edu.illinois.ncsa.isda.im2learn.ext.info.DebugDialog;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.AnnotationDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.CropDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.GammaDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectionDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.UseTotalsDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.BoundBoxDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.HSVThresholdDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.Seg2DBallDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.Seg2DDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.Seg2DSuperDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ThresholdDialog;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.HistogramDialog;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.ImageCompareDialog;


public class ImageExtractDialog extends Im2LearnFrame implements ActionListener, KeyListener, Im2LearnMenu {
	MI2LearnSubFrame		_subFrame		= null;
	private ImageObject		_sourceImObj	= null; // holds the image franes
	protected ImageObject	_subImageObject	= null;
	protected ImagePanel	_imPanel		= null;
	private Rectangle		_subarea;
	JButton					_bt_done, _bt_extract, _bt_set;

	JTextField				_row, _col, _width, _height, _from, _to;

	public ImageExtractDialog() {
		super("Image Extract");
		createUI();
	}

	private void createUI() {

//	  if(_subarea.height < 1 || _subarea.width < 1) {
//		  _subarea.setBounds(0,0,_sourceImObj.getNumRows(),_sourceImObj.getNumRows());
//	  }
//	  
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//   makeWindow();
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {

			//  updateEvent(false);
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();

		if (obj == _bt_done) {
			if (_subFrame != null) {
				_subFrame.dispose();
			}
			this.dispose();
		} else if (obj == _bt_extract) {
			try {
				launchSubArea();
			} catch (ImageException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (obj == _bt_set) {
			_row.setText(String.valueOf(0));
			_col.setText(String.valueOf(0));
			_width.setText(String.valueOf(_sourceImObj.getNumCols()));
			_height.setText(String.valueOf(_sourceImObj.getNumRows()));
		}
	}

	private void launchSubArea() throws ImageException {

		int from, to;
		try {

			from = Integer.parseInt(_from.getText());
			to = Integer.parseInt(_to.getText());
			if (_subarea == null) {
				_subarea = new Rectangle();
			}
			_subarea.setBounds(Integer.parseInt(_col.getText()), Integer.parseInt(_row.getText()), Integer.parseInt(_width.getText()), Integer.parseInt(_height.getText()));

		} catch (Exception ee) {
			ee.printStackTrace();
			System.err.println("Error in input values");
			return;
		}

		int[] band = new int[to - from + 1];
		for (int i = 0; i < band.length; i++) {
			band[i] = from + i;
		}
		if ((_subarea.x == 0) && (_subarea.y == 0) && (_subarea.width == this._sourceImObj.getNumCols()) && (_subarea.height == this._sourceImObj.getNumRows())) {
			_subImageObject = _sourceImObj.extractBand(band);
		}
		_subImageObject = _sourceImObj.crop(new SubArea(_subarea.x, _subarea.y, _subarea.width, _subarea.height)).extractBand(band);

		if (_subFrame == null) {
			_subFrame = new MI2LearnSubFrame(_subImageObject);
			_subFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		} else {
			_subFrame.getImagePanel().setImageObject(_subImageObject);
			_subFrame.setVisible(true);
		}

	}

	private void setBand(int from, int to) {
		_from.setText(String.valueOf(from));
		_to.setText(String.valueOf(to));
	}

	private void jbInit() throws Exception {
		JPanel controlPane = new JPanel(new BorderLayout());
		JPanel coordPane = new JPanel(new GridLayout(2, 4));
		JPanel buttonPane = new JPanel();
		JPanel propertyPane = new JPanel(new GridLayout(1, 5));

		JLabel Col = new JLabel("Col");
		JLabel Height = new JLabel("Height");
		JLabel Width = new JLabel("Width");
		JLabel Row = new JLabel("Row");

		JLabel band = new JLabel("Band : ");
		JLabel from = new JLabel("from");
		JLabel to = new JLabel("to");

		_row = new JTextField();
		_col = new JTextField();
		_width = new JTextField();
		_height = new JTextField();

		_from = new JTextField(5);
		_to = new JTextField(5);

		_row.addKeyListener(this);
		_col.addKeyListener(this);
		_width.addKeyListener(this);
		_height.addKeyListener(this);
		_from.addKeyListener(this);
		_to.addKeyListener(this);

		_bt_done = new JButton("Done");
		_bt_extract = new JButton("Extract");
		_bt_set = new JButton("Set Entire Image");

		_bt_done.addActionListener(this);
		_bt_extract.addActionListener(this);
		_bt_set.addActionListener(this);

		to.setHorizontalAlignment(SwingConstants.CENTER);
		from.setHorizontalAlignment(SwingConstants.CENTER);
		band.setHorizontalAlignment(SwingConstants.RIGHT);
		Row.setHorizontalAlignment(SwingConstants.CENTER);
		Col.setHorizontalAlignment(SwingConstants.CENTER);
		Width.setHorizontalAlignment(SwingConstants.CENTER);
		Height.setHorizontalAlignment(SwingConstants.CENTER);

		coordPane.setBorder(BorderFactory.createLoweredBevelBorder());
		propertyPane.setBorder(BorderFactory.createLoweredBevelBorder());
		buttonPane.setBorder(BorderFactory.createLoweredBevelBorder());

		coordPane.add(Col, null);
		coordPane.add(Row, null);
		coordPane.add(Width, null);
		coordPane.add(Height, null);
		coordPane.add(_col, null);
		coordPane.add(_row, null);
		coordPane.add(_width, null);
		coordPane.add(_height, null);

		propertyPane.add(band, null);
		propertyPane.add(from, null);
		propertyPane.add(_from, null);
		propertyPane.add(to, null);
		propertyPane.add(_to, null);

		controlPane.add(propertyPane, BorderLayout.NORTH);
		controlPane.add(buttonPane, BorderLayout.SOUTH);

		buttonPane.add(_bt_set, null);
		buttonPane.add(_bt_extract, null);
		buttonPane.add(_bt_done, null);

		this.getContentPane().add(controlPane, BorderLayout.SOUTH);
		this.getContentPane().add(coordPane, BorderLayout.NORTH);

		this.pack();
	}

	// ------------------------------------------------------------------------
	// Im2LearnMenu implementation
	// ------------------------------------------------------------------------
	public void setImagePanel(ImagePanel imagepanel) {
		this._imPanel = imagepanel;

	}

	public JMenuItem[] getPanelMenuItems() {
		JMenuItem imageextract = new JMenuItem(new AbstractAction("Image Extract") {
			public void actionPerformed(ActionEvent e) {
				_sourceImObj = _imPanel.getImageObject();
				setBand(0, _sourceImObj.getNumBands() - 1);

				_subarea = _imPanel.getSelection();
				if (_subarea != null) {
					_row.setText(String.valueOf(_subarea.y));
					_col.setText(String.valueOf(_subarea.x));
					_width.setText(String.valueOf(_subarea.width));
					_height.setText(String.valueOf(_subarea.height));
				}
				setLocationRelativeTo(getOwner());
				setVisible(true);
			}
		});

		return new JMenuItem[] { imageextract };
	}

	public JMenuItem[] getMainMenuItems() {
		JMenu tools = new JMenu("Visualization");

		JMenuItem imageextract = new JMenuItem(new AbstractAction("Image Extract") {
			public void actionPerformed(ActionEvent e) {
				_sourceImObj = _imPanel.getImageObject();
				setBand(0, _sourceImObj.getNumBands() - 1);

				_subarea = _imPanel.getSelection();
				if (_subarea != null) {
					_row.setText(String.valueOf(_subarea.y));
					_col.setText(String.valueOf(_subarea.x));
					_width.setText(String.valueOf(_subarea.width));
					_height.setText(String.valueOf(_subarea.height));
				}
				setLocationRelativeTo(getOwner());
				setVisible(true);
			}
		});
		tools.add(imageextract);

		return new JMenuItem[] { tools };
	}

	public void imageUpdated(ImageUpdateEvent event) {

		// System.out.println(event.getId());
		if (isVisible()) {

			if ((event.getId() == ImageUpdateEvent.NEW_IMAGE)) {
				_sourceImObj = _imPanel.getImageObject();
				setBand(0, _sourceImObj.getNumBands() - 1);
				_subarea = _imPanel.getSelection();
			}

			_subarea = _imPanel.getSelection();
			if (_subarea != null) {
				_row.setText(String.valueOf(_subarea.y));
				_col.setText(String.valueOf(_subarea.x));
				_width.setText(String.valueOf(_subarea.width));
				_height.setText(String.valueOf(_subarea.height));
			}
		}

	}

	public URL getHelp(String menu) {
		URL url = null;

		if (url == null) {
			String file = menu.toLowerCase().replaceAll("[\\s\\?\\*]", "") + ".html";
			url = this.getClass().getResource("help/" + file);
		}

		return url;
	}

	class MI2LearnSubFrame extends Im2LearnMainFrame {
		/**
		 * Create the MainFrame with the menus
		 */
		public MI2LearnSubFrame(ImageObject image) {
			super(image);
		}

		@Override
		public void addMenus() {
			// allow selection
			getImagePanel().setSelectionAllowed(true);

			// information dialogs
			addMenu(new InfoDialog());
			addMenu(new PlayBandDialog());
			addMenu(new PseudoImageDialog());
			addMenu(new CoordValDialog());

			addMenu(new ImageExtractDialog());

			// zoom
			addMenu(new ZoomDialog());

			// selection dialogs
			addMenu(new SelectionDialog());
			addMenu(new CropDialog());

			// markers
			addMenu(new AnnotationDialog());

			// color change
			addMenu(new GammaDialog());
			addMenu(new SelectBandDialog());
			addMenu(new UseTotalsDialog());
			addMenu(new GrayScaleDialog());
			addMenu(new FakeRGBColorDialog());

			// change image type
			addMenu(new ChangeTypeDialog());

			// perform PCA
			addMenu(new PCADialog());

			// image comparison
			addMenu(new ImageCompareDialog());
			addMenu(new HistogramDialog());
			addMenu(new ImageCalculatorDialog());
			addMenu(new Seg2DSuperDialog());
			addMenu(new Seg2DBallDialog());
			addMenu(new Seg2DDialog());
			addMenu(new ThresholdDialog());
			addMenu(new BoundBoxDialog());
			addMenu(new HSVThresholdDialog());

			// image registration
			addMenu(new RegistrationDialog());

			// debug
			addMenu(new DebugDialog());

		}

	}

}
