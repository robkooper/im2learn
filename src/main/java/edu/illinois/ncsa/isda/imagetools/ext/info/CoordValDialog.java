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
package edu.illinois.ncsa.isda.imagetools.ext.info;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.Point2DDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageMarker;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoUtilities;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.ModelProjection;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileException;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileLoader;
import edu.illinois.ncsa.isda.imagetools.ext.misc.PlotComponent;
import edu.illinois.ncsa.isda.imagetools.ext.misc.TableSorter;


/**
 * Select points in the image and get relevant details about those points. You
 * can select multiple points by right clicking on the image and selecting coord
 * val. This will adds it to the list of points already selected. It will
 * collect the sample values from the point, and if possible the geo
 * coordinates.
 * 
 * @author Rob Kooper
 * @version 2.0
 */
public class CoordValDialog extends Im2LearnFrame implements Im2LearnMenu {
	private ImagePanel		imagepanel;
	private MyTableModel	dtm;
	private JToggleButton	btnHide;
	private TableSorter		sorter;
	private JTable			table;
	private final Color		selected	= Color.green;
	private final Color		normal		= Color.blue;
	private PlotComponent	pc;
	private JFrame			frmPlot;
	private JToggleButton	btnPlot;
	private int[]			ids			= null;

	/**
	 * Default constructor, takes owner so dialog gets minimized.
	 */
	public CoordValDialog() {
		super("Coord Value");

		createUI();
		pack();
	}

	/**
	 * Create both the plot gui, as well as the ncsa.im2learn.main gui. Allow
	 * for sorted tables, reset, close and plot buttons.
	 */
	private void createUI() {
		// create the plot
		pc = new PlotComponent();
		frmPlot = new Im2LearnFrame("Sample Plot of Coord Value") {
			@Override
			public void closing() {
				btnPlot.setSelected(false);
			}
		};
		frmPlot.getContentPane().add(pc, BorderLayout.CENTER);
		JPanel buttons = new JPanel(new FlowLayout());
		JButton button = new JButton(new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				frmPlot.setVisible(false);
				btnPlot.setSelected(false);
			}
		});
		buttons.add(button);
		frmPlot.getContentPane().add(buttons, BorderLayout.SOUTH);
		frmPlot.pack();

		// This table displays a tool tip text based on the string
		// representation of the cell value
		// http://javaalmanac.com/egs/javax.swing.table/Tips.html
		dtm = new MyTableModel();
		sorter = new TableSorter(dtm);
		table = new JTable(sorter) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if ((c instanceof JComponent) && (getValueAt(rowIndex, vColIndex) != null)) {
					JComponent jc = (JComponent) c;
					jc.setToolTipText(getValueAt(rowIndex, vColIndex).toString());
				}
				return c;
			}
		};
		sorter.setTableHeader(table.getTableHeader());
		JScrollPane scrollpane = new JScrollPane(table);
		getContentPane().add(scrollpane, BorderLayout.CENTER);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int row = table.getSelectedRow();
				if (!e.getValueIsAdjusting() && (row >= 0)) {
					int id = ((Integer) sorter.getValueAt(row, 0)).intValue();
					selectMarker(id);
				}
			}
		});

		buttons = new JPanel(new FlowLayout());
		getContentPane().add(buttons, BorderLayout.SOUTH);

		btnHide = new JToggleButton(new AbstractAction("Show Markers?") {
			public void actionPerformed(ActionEvent e) {
				dtm.toggleMarkers(btnHide.isSelected());
			}
		});
		btnHide.setSelected(true);
		buttons.add(btnHide);

		btnPlot = new JToggleButton(new AbstractAction("Plot Markers?") {
			public void actionPerformed(ActionEvent e) {
				showPlot(btnPlot.isSelected());
			}
		});
		buttons.add(btnPlot);

		button = new JButton(new AbstractAction("Reset Markers") {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		buttons.add(button);

		button = new JButton(new AbstractAction("Save"){
			public void actionPerformed(ActionEvent e){
		        FileChooser fc = new FileChooser();
		        fc.setTitle("Save Image Points");
		        try {
		            String filename = fc.showSaveDialog();
		            if (filename != null) {
						dtm.savePoints(filename);
		            }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ShapefileException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		buttons.add(button);
		
		button = new JButton(new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttons.add(button);
	}

	@Override
	public void showing() {
		dtm.toggleMarkers(btnHide.isSelected());
		showPlot(btnPlot.isSelected());
	}

	@Override
	public void closing() {
		dtm.toggleMarkers(true);
		showPlot(false);
	}

	private void reset() {
		dtm.reset();
		pc.reset();

		btnPlot.setSelected(false);
		frmPlot.setVisible(false);
	}

	private void selectMarker(int id) {
		for (int i = 0; i < table.getRowCount(); i++) {
			int row = ((Integer) sorter.getValueAt(i, 0)).intValue();
			ImageMarker marker = (ImageMarker) sorter.getValueAt(i, 8);
			if (id == row) {
				table.setRowSelectionInterval(i, i);
				marker.setColor(selected);
			} else {
				marker.setColor(normal);
			}
		}
		imagepanel.repaint();
	}

	private void showPlot(boolean vis) {
		if (vis) {
			if (!frmPlot.isVisible()) {
				ImageObject imgobj = imagepanel.getImageObject();
				if (imgobj == null) {
					btnPlot.setSelected(false);
					return;
				}

				int rows = table.getRowCount();
				if (rows == 0) {
					btnPlot.setSelected(false);
					return;
				}

				String[] val = dtm.getValueAt(0, 3).toString().split(",");
				int cols = val.length;

				String[][] items = new String[cols][rows];
				for (int j = 0; j < cols; j++) {
					items[j][0] = val[j];
				}

				for (int i = 1; i < rows; i++) {
					val = dtm.getValueAt(i, 3).toString().split(",");
					for (int j = 0; j < cols; j++) {
						items[j][i] = val[j];
					}
				}

				ids = new int[cols];
				for (int j = 0; j < cols; j++) {
					ids[j] = pc.addSeries("Band " + j);
					pc.setValue(ids[j], items[j]);
				}

				frmPlot.setVisible(true);
			}
		} else {
			frmPlot.setVisible(false);
			pc.reset();
		}
	}

	class MyTableModel extends AbstractTableModel {
		private final String[]	columnNames	= { "ID", "X/Col", "Y/Row", "Sample(s)", "Northing", "Easting", "Lat",
													"Lon" };
		private final ArrayList	data		= new ArrayList();

		public int addPoint(Point loc) {
			if (loc == null) {
				return -1;
			}
			int row = data.size();

			ImageMarker marker = new ImageMarker(loc.x, loc.y, 10, ImageMarker.CROSS);
			marker.setColor(normal);
			marker.setVisible(btnHide.isSelected());
			imagepanel.addAnnotationImage(marker);

			data.add(new Object[] { new Integer(row), new Integer(loc.x), new Integer(loc.y), "", "", "", "", "",
					marker });
			fireTableRowsInserted(row, row);

			update(row);

			return row;
		}

		public void reset() {
			for (Iterator iter = data.iterator(); iter.hasNext();) {
				Object[] value = (Object[]) iter.next();
				ImageMarker marker = (ImageMarker) value[8];
				imagepanel.removeAnnotationImage(marker);
			}

			int row = data.size();
			data.clear();
			fireTableRowsDeleted(0, row);
		}

		public void toggleMarkers(boolean b) {
			for (Iterator iter = data.iterator(); iter.hasNext();) {
				Object[] value = (Object[]) iter.next();
				ImageMarker marker = (ImageMarker) value[8];
				marker.setVisible(b);
			}
			imagepanel.repaint();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.size();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return ((Object[]) data.get(row))[col];
		}
		
		// Saves the selected points to a shape file
		public void savePoints(String filename) throws IOException, ShapefileException{
			
			
			int numpoints = this.getRowCount();
			Point2DDouble points = new Point2DDouble();
			double[] entries = new double[numpoints*2];
			int maxX = 0, maxY = 0;
			int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
			for(int i = 0; i < numpoints; i++){
				int row = ((Number)this.getValueAt(i, 2)).intValue();
				int col = ((Number)this.getValueAt(i, 1)).intValue();
				entries[2*i] = row;
				entries[2*i + 1] = col;
				if(col<minX) minX = col;
				if(col>maxX) maxX = col;
				if(row<minY) minY = row;
				if(row>maxY) maxY = row;
			}
			//int height = maxY-minY;
			//minY = maxY;
			double[] box = {minX, minY, maxX, maxY};
			
			// Correct for sense of y coordinate
			for(int i = 0; i < numpoints; i++){
				entries[2*i] = maxY - entries[2*i] + minY;
			}
			
			//ShapefileHeader header = new ShapefileHeader();
			//header.setFileCode(9994);
			//header.setShapeType(3);
			//header.setBBox(box);
			//header.setFileLength(fileLength)
			
			
			
			points.SetPoint2DDouble(numpoints, entries);
			//int[] parts = {1};
			//ShapeArc contour = new ShapeArc(box, parts, points);
		    //FileOutputStream fop=new FileOutputStream(contour);
		    //LEDataOutputStream los = new LEDataOutputStream(contour);
			//contour.write(los);
			
			
			int[] boundaryTypes = {3};
			int[] numBoundaryPoints = {numpoints};
			int[] boundaryPointsIndex = {0};
			int[] numBoundaryParts = {1};
			int[] boundaryPartsIndex = {0};

			ShapeObject contour = new ShapeObject(1, box, boundaryTypes, box, 
					numBoundaryPoints, new double[numpoints], boundaryPointsIndex,
					numBoundaryParts, boundaryPartsIndex, null);
			contour.setAllBoundaryPoints(points);
			contour.setIsInPixel(true);
	
			ShapefileLoader loader = new ShapefileLoader();
			loader.Write(filename, contour);
			

		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		@Override
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			return ((col == 1) || (col == 2));
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		@Override
		public void setValueAt(Object value, int row, int col) {
			if (isCellEditable(row, col)) {
				((Object[]) data.get(row))[col] = value;
				update(row);
				fireTableRowsUpdated(row, row);
			}
		}

		private void update(int row) {
			ImageObject imgobj = imagepanel.getImageObject();
			if (imgobj == null) {
				return;
			}

			Object[] value = (Object[]) data.get(row);

			int id = ((Integer) value[0]).intValue();
			int x = ((Integer) value[1]).intValue();
			int y = ((Integer) value[2]).intValue();

			// sample values
			String val = "";
			for (int i = 0; i < imgobj.getNumBands(); i++) {
				if (i != 0) {
					val += ", ";
				}
				val += imgobj.getDouble(y, x, i);
			}
			value[3] = val;

			// marker
			ImageMarker marker = (ImageMarker) value[8];
			marker.x = x;
			marker.y = y;

			// geo info
			// GeoInformation geo = (GeoInformation)
			// imgobj.getProperty(ImageObject.GEOINFO);
			Projection geo = (Projection) imgobj.getProperty(ImageObject.GEOINFO);
			if (geo != null) {
				// GeoConvert geoconv = new GeoConvert(geo,
				// imgobj.getNumRows());

				/*
				 * Point2DDouble p = new Point2DDouble(1); p.ptsDouble[0] = x;
				 * p.ptsDouble[1] = y; Point2DDouble m =
				 * geoconv.ColumnRow2UTMNorthingEasting(p); Point2DDouble e =
				 * geoconv.ColumnRow2LatLng(p);
				 */
				double[] pt = new double[] { x, y };
				double[] m = null;
				double[] e = null;
				try {
					if (geo instanceof ModelProjection) {
						m = ((ModelProjection) geo).rasterToModel(pt);
					}

					e = geo.rasterToEarth(pt);
				} catch (Exception ex) {

				}

				// Point3DDouble t = geoconv.DecimToDegMinSec(e.ptsDouble[0]);
				double[] t = GeoUtilities.decimToDegMinSec(e[0]);
				String str;
				NumberFormat nfint = NumberFormat.getIntegerInstance();
				nfint.setMinimumIntegerDigits(2);

				nfint.setMaximumIntegerDigits(3);
				NumberFormat nfdbl = NumberFormat.getNumberInstance();
				nfdbl.setMinimumFractionDigits(2);
				nfdbl.setMaximumFractionDigits(2);
				nfdbl.setMinimumIntegerDigits(2);
				nfdbl.setMaximumIntegerDigits(2);
				if (t[0] < 0) {
					str = nfint.format(t[0]) + "d " + nfint.format(-t[1]) + "' " + nfdbl.format(-t[2]) + "\" S";
				} else {
					str = nfint.format(t[0]) + "d " + nfint.format(t[1]) + "' " + nfdbl.format(t[2]) + "\" N";
				}
				if (m != null) {
					value[4] = "" + m[0];
				} else {
					value[4] = "";
				}
				value[6] = str;

				t = GeoUtilities.decimToDegMinSec(e[1]);
				if (t[0] < 0) {
					str = nfint.format(t[0]) + "d " + nfint.format(-t[1]) + "' " + nfdbl.format(-t[2]) + "\" S";
				} else {
					str = nfint.format(t[0]) + "d " + nfint.format(t[1]) + "' " + nfdbl.format(t[2]) + "\" N";
				}
				if (m != null) {
					value[5] = "" + m[1];
				} else {
					value[5] = "";
				}
				value[7] = str;

			} else {
				value[4] = "";
				value[5] = "";
				value[6] = "";
				value[7] = "";
			}

			selectMarker(id);
			imagepanel.repaint();
		}
	}

	// ------------------------------------------------------------------------
	// Im2LearnMenu implementation
	// ------------------------------------------------------------------------
	public void setImagePanel(ImagePanel imgpanel) {
		this.imagepanel = imgpanel;
		imagepanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (isVisible() && ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)) {
					Point loc = imagepanel.getImageLocationClicked();
					int row = dtm.addPoint(loc);

					if (frmPlot.isVisible() && row >= 0) {
						String[] val = dtm.getValueAt(row, 3).toString().split(",");
						int cols = val.length;
						for (int j = 0; j < cols; j++) {
							pc.setValue(ids[j], Double.parseDouble(val[j]));
						}
					}
				}
			}
		});
	}

	public JMenuItem[] getPanelMenuItems() {
		return null;
	}

	public JMenuItem[] getMainMenuItems() {
		JMenu vis = new JMenu("Visualization");
		JMenuItem coordval = new JMenuItem(new AbstractAction("Show Coord Values") {
			public void actionPerformed(ActionEvent e) {
				setVisible(true);
			}
		});
		vis.add(coordval);

		return new JMenuItem[] { vis };
	}

	public void imageUpdated(ImageUpdateEvent event) {
		if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
			reset();
		}
	}

	public URL getHelp(String topic) {
		// TODO Auto-generated method stub
		return null;
	}
}
