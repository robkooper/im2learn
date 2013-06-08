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
package edu.illinois.ncsa.isda.im2learn.ext.conversion;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.Histogram;

/**
 * 
 * <p>
 * <B>The class ChangeType is a utility for changing image data types among the
 * following representations: byte, unsigned short, signed short short, signed
 * int, signed long, float, double and optimal based on the data range. </B>
 * </p>
 * <p>
 * <b>Description:</b> This class can be used for changing data types depending
 * on the accuracy needs of further computation and processing. The data type
 * can be verified using the "Image Info" dialog invoked by a right mouse click
 * on the image panel. The data type information is presented in the right panel
 * of the Image Info dialog (see below). <BR>
 * <img src="help/changeType.jpg"> <BR>
 * <BR>
 * 
 * 
 * @author Rob Kooper, Peter Bajcsy (documentation)
 * 
 * @version 2.0 Allow to change the type of an image. This will allow to change
 *          the type of an image to any type. If the new type has less precision
 *          than the original it will result in information loss. If the
 *          original image has values that fall outside the range of the new
 *          image, the values will be scaled such that they do fit in this new
 *          range. Each band is scaled with regards to its own min and max
 *          values, not those of the whole image.
 * 
 */
public class ChangeTypeDialog implements Im2LearnMenu {
	private ImagePanel			imagepanel	= null;
	private static Log			logger		= LogFactory.getLog(ChangeTypeDialog.class);
	private final JMenuItem[]	menus		= new JMenuItem[ImageObject.TYPE_UNKNOWN + 1];

	/**
	 * Default constructor. Will create the menu entries.
	 */
	public ChangeTypeDialog() {
		menus[ImageObject.TYPE_BYTE] = new JMenuItem(new AbstractAction("to byte") {
			public void actionPerformed(ActionEvent e) {
				convert(ImageObject.TYPE_BYTE);
			}
		});
		menus[ImageObject.TYPE_SHORT] = new JMenuItem(new AbstractAction("to signed short") {
			public void actionPerformed(ActionEvent e) {
				convert(ImageObject.TYPE_SHORT);
			}
		});
		menus[ImageObject.TYPE_USHORT] = new JMenuItem(new AbstractAction("to unsigned short") {
			public void actionPerformed(ActionEvent e) {
				convert(ImageObject.TYPE_SHORT);
			}
		});
		menus[ImageObject.TYPE_INT] = new JMenuItem(new AbstractAction("to signed int") {
			public void actionPerformed(ActionEvent e) {
				convert(ImageObject.TYPE_INT);
			}
		});
		menus[ImageObject.TYPE_LONG] = new JMenuItem(new AbstractAction("to signed long") {
			public void actionPerformed(ActionEvent e) {
				convert(ImageObject.TYPE_LONG);
			}
		});
		menus[ImageObject.TYPE_FLOAT] = new JMenuItem(new AbstractAction("to float") {
			public void actionPerformed(ActionEvent e) {
				convert(ImageObject.TYPE_FLOAT);
			}
		});
		menus[ImageObject.TYPE_DOUBLE] = new JMenuItem(new AbstractAction("to double") {
			public void actionPerformed(ActionEvent e) {
				convert(ImageObject.TYPE_DOUBLE);
			}
		});
		menus[ImageObject.TYPE_UNKNOWN] = new JMenuItem(new AbstractAction("to optimal") {
			public void actionPerformed(ActionEvent e) {
				convert(ImageObject.TYPE_UNKNOWN);
			}
		});
		menus[ImageObject.TYPE_BYTE] = new JMenuItem(new AbstractAction("to hist byte optimal") {
			public void actionPerformed(ActionEvent e) {
				convertHistOptimal(ImageObject.TYPE_BYTE);
			}
		});

	}

	/**
	 * Converts the image currently shown. Will take the current image and
	 * convert it to a new type. If the new type has less precision than the
	 * original this could result in information loss.
	 * 
	 * @param type
	 *            of the new image.
	 */
	private void convert(int type) {
		ImageObject tmp = imagepanel.getImageObject();
		if (tmp.getType() != type) {
			try {
				ImageObject res = tmp.convert(type, false);
				imagepanel.setImageObject(res);
			} catch (ImageException exc) {
				logger.error("Error converting image.", exc);
			}
		}
	}

	/**
	 * Converts the image currently shown. Will take the current image and
	 * re-map the values based on the histogram bins so that all non-empty bins
	 * are next to each other and then convert it to a new type.
	 * 
	 * @param type
	 *            of the new image.
	 */
	private void convertHistOptimal(int type) {
		ImageObject tmp = imagepanel.getImageObject();
		if (tmp.getType() != type) {
			try {
				Histogram hist = new Histogram();
				double numbinsTarget = hist.GetNumBinsPerType(type);
				double numbinsOrig = hist.GetNumBinsPerType(tmp.getType());
				if (numbinsTarget < numbinsOrig) {
					// try to re-map bins
					int[] histData = null;
					ImageObject tmp2 = ImageObject.createImage(tmp.getNumRows(), tmp.getNumCols(), tmp.getNumBands(), type);
					tmp2.setData(0.0);
					// re-map every single band
					for (int band = 0; band < tmp.getNumBands(); band++) {
						// setup the histogram
						hist.SetMinDataVal(tmp.getMin(band));
						hist.SetMaxDataVal(tmp.getMax(band));
						hist.SetNumBins((int) numbinsTarget);

						hist.Hist(tmp, band);
						hist.Count();
						logger.debug("band=" + band + ", count=" + hist.GetCount());

						// build the look up table
						double[] lut = new double[(int) hist.GetCount() + 1];
						histData = hist.GetHistData();
						double halfBin = 0.5 * hist.GetWideBins();
						int i, j, k = 0;
						for (j = 0; j < histData.length; j++) {
							if (histData[j] != 0) {
								if (k < lut.length) {
									lut[k] = hist.GetMinDataVal() + j * hist.GetWideBins() + halfBin;
									//logger.debug("lut values j=" + j + ",k=" + k + ", lut[k]=" + lut[k] + ", hist=" + histData[j]);
								} else {
									//logger.debug("test lut j=" + j + ",k=" + k);
								}
								k++;
							}
						}
						//test
						//double[] test = new double[(int) hist.GetCount() + 1];
						//for (k = 0; k < test.length; k++) {
						//		test[k] = 0;
						//}

						// re-map values of that particular band
						for (i = band; i < tmp.getSize(); i += tmp.getNumBands()) {
							for (k = 0; k < lut.length; k++) {
								//if (Math.abs(tmp.getDouble(i) - lut[k]) <= halfBin) {
								if ((tmp.getDouble(i) >= (lut[k]) - halfBin) && (tmp.getDouble(i) < (lut[k] + halfBin))) {
									tmp2.set(i, k);
									//test[k]++;
									k = lut.length;
								}
							}
						}
						//test				
						//for (k = 0; k < test.length; k++) {
						//		logger.debug("test k=" + k + ", test[k]=" + test[k]);
						//}

					}// end of all bands

					// re-assign the result to tmp
					tmp = tmp2;
				}
				ImageObject res = tmp.convert(type, false);
				imagepanel.setImageObject(res);
			} catch (ImageException exc) {
				logger.error("Error converting image.", exc);
			}
		}
	}

	// ------------------------------------------------------------------------
	// Im2LearnMenu implementation
	// ------------------------------------------------------------------------
	public void setImagePanel(ImagePanel imagepanel) {
		this.imagepanel = imagepanel;
		for (int i = 0; i < menus.length; i++) {
			menus[i].setEnabled(true);
		}
		menus[imagepanel.getImageObject().getType()].setEnabled(false);
	}

	public JMenuItem[] getPanelMenuItems() {
		return null;
	}

	public JMenuItem[] getMainMenuItems() {
		JMenu tools = new JMenu("Tools");

		JMenu type = new JMenu("Change Type");
		tools.add(type);

		type.add(menus[ImageObject.TYPE_BYTE]);
		type.add(menus[ImageObject.TYPE_SHORT]);
		type.add(menus[ImageObject.TYPE_USHORT]);
		type.add(menus[ImageObject.TYPE_INT]);
		type.add(menus[ImageObject.TYPE_LONG]);
		type.add(menus[ImageObject.TYPE_FLOAT]);
		type.add(menus[ImageObject.TYPE_DOUBLE]);
		type.add(menus[ImageObject.TYPE_UNKNOWN]);

		return new JMenuItem[] { tools };
	}

	public void imageUpdated(ImageUpdateEvent event) {
		if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
			for (int i = 0; i < menus.length; i++) {
				menus[i].setEnabled(true);
			}
			if (imagepanel.getImageObject() != null) {
				menus[imagepanel.getImageObject().getType()].setEnabled(false);
			} else {
				menus[ImageObject.TYPE_BYTE].setEnabled(false);
			}
		}
	}

	public URL getHelp(String menu) {
		if (menu.equals("Change Type")) {
			return getClass().getResource("help/ChangeTypeDialog.html");
		}
		return null;
	}
}
