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
package edu.illinois.ncsa.isda.imagetools.ext.geo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileLoader;
import edu.illinois.ncsa.isda.imagetools.ext.geo.dlg.DLG3Loader;
import edu.illinois.ncsa.isda.imagetools.ext.geo.dlg.StatsgoLoader;


public class VectorUI
    extends JPanel {

  // -----------------------------------------------------------
  /** a map from filename to the ShapefileImageAnnotation object */
  private HashMap annotations;
  private ImagePanel imagepanel;
  private static Log logger = LogFactory.getLog(VectorUI.class);

  public VectorUI() {

    annotations = new HashMap();

    // button to load shapefile
    JButton loadShape = new JButton("Load shapefile");
    // button to load dlg
    JButton loadDlg = new JButton("Load DLG");
    //loadDlg.setEnabled(false);
    JButton loadStatsgo = new JButton("Load Statsgo");
    // button to remove a shape from the jtable
    JButton removeShape = new JButton("Remove");

    // layout the buttons
    GridBagLayout gbl = new GridBagLayout();
    JPanel buttons = new JPanel(gbl);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbl.setConstraints(loadShape, gbc);
    buttons.add(loadShape);
    gbc.gridy = 1;
    gbl.setConstraints(loadDlg, gbc);
    buttons.add(loadDlg);
    gbc.gridy = 2;
    gbl.setConstraints(loadStatsgo, gbc);
    buttons.add(loadStatsgo);

    // the loaded shapefiles are kept in a jtable
    final ShapefileTableModel tableModel = new ShapefileTableModel();
    final JTable tbl = new JTable(tableModel);
    // make these columns smaller
    tbl.getColumnModel().getColumn(1).setMaxWidth(50);
    tbl.getColumnModel().getColumn(2).setMaxWidth(50);
    // fancy renderer and editor
    tbl.setDefaultEditor(Color.class, new ColorEditor());
    tbl.setDefaultRenderer(Color.class, new ColorRenderer(true));

    // put it in a scroller
    JScrollPane scrollPane = new JScrollPane(tbl);

    // push the buttons to the upper left
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(buttons, BorderLayout.NORTH);
    buttonPanel.add(new JPanel(), BorderLayout.CENTER);

    JPanel right = new JPanel(new BorderLayout());
    JPanel rightBottom = new JPanel();
    rightBottom.add(removeShape);
    right.add(scrollPane, BorderLayout.CENTER);
    right.add(rightBottom, BorderLayout.SOUTH);

    // add everything
    JPanel pnl = new JPanel(new BorderLayout());
    pnl.add(buttonPanel, BorderLayout.WEST);
    pnl.add(right, BorderLayout.CENTER);
    setLayout(new BorderLayout());
    add(pnl, BorderLayout.CENTER);

    /*    createImage.addActionListener(new AbstractAction() {
        public void actionPerformed(ActionEvent ae) {
          ImageObjectByte io = new ImageObjectByte(1010, 1537, 3);
          Projection geo = new Projection();
          geo.setRasterSpaceI(0);
          geo.setRasterSpaceJ(0 + 1010);
          //geo.SetColumnResolution(demheader.cellsize);
          geo.setScaleX(0.0027777777);
          //geo.SetRowResolution(demheader.cellsize);
          geo.setScaleY(0.0027777777);
          geo.setInsertionY(39.7627777);
          geo.setInsertionX(-91.636388);
          geo.setNumRows(1010);
          geo.setNumCols(1537);
          io.setProperty(ImageObject.GEOINFO, geo);
          imagepanel.setImageObject(io);
        }
      });*/

    loadShape.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        // show a file dialog
        JFileChooser fc = new JFileChooser(FileChooser.getInitialDirectory());
        fc.setFileFilter(new ShapefileFileFilter());
        File thefile = null;
        int retVal = fc.showOpenDialog(null);
        if (retVal == JFileChooser.APPROVE_OPTION) {
          thefile = fc.getSelectedFile();
          FileChooser.setInitialDirectory(thefile.getAbsolutePath());
        }
        // if the file was not null, load it
        if (thefile != null) {
          try {
            ShapefileLoader shapefile = new ShapefileLoader(thefile);
            ShapeObject shape = shapefile.getShapeObject();

            // if it was loaded correctly, add it to the table model
            // by default the ShapeObject is not drawn (displayed is set to false)
            if (shape != null) {
              tableModel.filenames.add(thefile);
              tableModel.shapes.add(shape);
              tableModel.colors.add(Color.red);
              
              logger.debug("Shape: " + shape + ", projection= "+ shape.getProjection());

              tableModel.displayed.add(new Boolean(false));
              tableModel.fireTableDataChanged();
            }
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    loadDlg.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        // show a file dialog
        JFileChooser fc = new JFileChooser(FileChooser.getInitialDirectory());
        //fc.setFileFilter(new ShapefileFileFilter());
        File thefile = null;
        int retVal = fc.showOpenDialog(null);
        if (retVal == JFileChooser.APPROVE_OPTION) {
          thefile = fc.getSelectedFile();
          FileChooser.setInitialDirectory(thefile.getAbsolutePath());
        }
        // if the file was not null, load it
        if (thefile != null) {
          try {
            DLG3Loader dlgLoader = new DLG3Loader(thefile);
            //ShapefileLoader shapefile = new ShapefileLoader(thefile);
            ShapeObject shape = dlgLoader.GetShapeObject();

            // if it was loaded correctly, add it to the table model
            // by default the ShapeObject is not drawn (displayed is set to false)
            if (shape != null) {
              tableModel.filenames.add(thefile);
              tableModel.shapes.add(shape);
              tableModel.colors.add(Color.red);

              tableModel.displayed.add(new Boolean(false));
              tableModel.fireTableDataChanged();
            }
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    loadStatsgo.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        // show a file dialog
        JFileChooser fc = new JFileChooser(FileChooser.getInitialDirectory());
        //fc.setFileFilter(new ShapefileFileFilter());
        File thefile = null;
        int retVal = fc.showOpenDialog(null);
        if (retVal == JFileChooser.APPROVE_OPTION) {
          thefile = fc.getSelectedFile();
          FileChooser.setInitialDirectory(thefile.getAbsolutePath());
        }
        // if the file was not null, load it
        if (thefile != null) {
          try {
            StatsgoLoader dlgLoader = new StatsgoLoader(thefile);
            //ShapefileLoader shapefile = new ShapefileLoader(thefile);
            ShapeObject shape = dlgLoader.GetShapeObject();

            // if it was loaded correctly, add it to the table model
            // by default the ShapeObject is not drawn (displayed is set to false)
            if (shape != null) {
              tableModel.filenames.add(thefile);
              tableModel.shapes.add(shape);
              tableModel.colors.add(Color.red);

              tableModel.displayed.add(new Boolean(false));
              tableModel.fireTableDataChanged();
            }
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    // remove the selected row of the jtable and its associated shape object
    removeShape.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        int selectedrow = tbl.getSelectedRow();
        if (selectedrow != -1) {
          // remove this row
          Boolean isDisplayed = (Boolean) tableModel.displayed.get(selectedrow);
          if (isDisplayed.booleanValue()) {
            // don't show this shape
            ShapeImageAnnotation sia = (ShapeImageAnnotation) annotations.get(
                tableModel.filenames.get(selectedrow));
            if (sia != null) {
              imagepanel.removeAnnotation(sia);
              annotations.remove(tableModel.filenames.get(selectedrow));
            }
          }

          tableModel.filenames.remove(selectedrow);
          tableModel.colors.remove(selectedrow);
          tableModel.shapes.remove(selectedrow);
          tableModel.displayed.remove(selectedrow);
          tableModel.fireTableDataChanged();
        }
      }
    });
  } // shapefile dialog

  /**
   * A table model to hold shape objects
   * @author clutter
   */
  class ShapefileTableModel
      extends DefaultTableModel {
    ArrayList filenames = new ArrayList();
    ArrayList shapes = new ArrayList();
    ArrayList colors = new ArrayList();
    ArrayList displayed = new ArrayList();

    public int getRowCount() {
      if (filenames != null) {
        return filenames.size();
      }
      return 0;
    } // get row count

    public int getColumnCount() {
      return 3;
    } // get column count

    public Object getValueAt(int row, int col) {
      switch (col) {
        case 0:
          return filenames.get(row);
        case 1:
          return colors.get(row);
        case 2:
          return displayed.get(row);
        default:
          return "";
      }
    } // get value at

    public String getColumnName(int col) {
      switch (col) {
        case 0:
          return "File";
        case 1:
          return "Color";
        case 2:
          return "Display";
        default:
          return "";
      }
    } // get column name

    public boolean isCellEditable(int row, int col) {
      if (col == 0) {
        return false;
      }
      return true;
    } // is cell editable

    public Class getColumnClass(int col) {
      switch (col) {
        case 0:
          return String.class;
        case 1:
          return Color.class;
        case 2:
          return Boolean.class;
        default:
          return String.class;
      }
    } // get column class

    public void setValueAt(Object val, int row, int col) {
      if (col == 1) {
        Color c = (Color) val;
        colors.set(row, c);

        // should repaint the image marker with this color!
        ShapeImageAnnotation sia = (ShapeImageAnnotation) annotations.get(
            filenames.get(row));
        if (sia != null) {
          imagepanel.removeAnnotation(sia);
          annotations.remove(filenames.get(row));
          ShapeObject so = (ShapeObject) shapes.get(row);
          //Color c = (Color)colors.get(row);
          sia = new ShapeImageAnnotation(so, c);
          imagepanel.addAnnotation(sia);
          annotations.put(filenames.get(row), sia);
        }
      }

      if (col == 2) {
        Boolean b = (Boolean) val;
        displayed.set(row, b);

        if (b.booleanValue()) {
          // show this shape
          if (imagepanel != null) {
            ShapeObject so = (ShapeObject) shapes.get(row);
            Color c = (Color) colors.get(row);
            ShapeImageAnnotation sia = new ShapeImageAnnotation(so, c);
            imagepanel.addAnnotation(sia);
            annotations.put(filenames.get(row), sia);
          }
        }
        else {
          // don't show this shape
          ShapeImageAnnotation sia = (ShapeImageAnnotation) annotations.get(
              filenames.get(row));
          if (sia != null) {
            imagepanel.removeAnnotation(sia);
            annotations.remove(filenames.get(row));
          }
        }
      }
    } // set value at

  } // shapefile table model

  /**
   * copied from java tutorial
   */
  private class ColorEditor
      extends AbstractCellEditor
      implements TableCellEditor,
      ActionListener {
    Color currentColor;
    JButton button;
    JColorChooser colorChooser;
    JDialog dialog;
    protected static final String EDIT = "edit";

    public ColorEditor() {
      //Set up the editor (from the table's point of view),
      //which is a button.
      //This button brings up the color chooser dialog,
      //which is the editor from the user's point of view.
      button = new JButton();
      button.setActionCommand(EDIT);
      button.addActionListener(this);
      button.setBorderPainted(false);

      //Set up the dialog that the button brings up.
      colorChooser = new JColorChooser();
      dialog = JColorChooser.createDialog(button,
                                          "Pick a Color",
                                          true, //modal
                                          colorChooser,
                                          this, //OK button handler
                                          null); //no CANCEL button handler
    }

    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     */
    public void actionPerformed(ActionEvent e) {
      if (EDIT.equals(e.getActionCommand())) {
        //The user has clicked the cell, so
        //bring up the dialog.
        button.setBackground(currentColor);
        colorChooser.setColor(currentColor);
        dialog.setVisible(true);

        //Make the renderer reappear.
        fireEditingStopped();

      }
      else { //User pressed dialog's "OK" button.
        currentColor = colorChooser.getColor();
      }
    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue() {
      return currentColor;
    }

    //Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
      currentColor = (Color) value;
      return button;
    }
  }

  /**
   * copied from java tutorial
   */
  private class ColorRenderer
      extends JLabel
      implements TableCellRenderer {
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;

    public ColorRenderer(boolean isBordered) {
      this.isBordered = isBordered;
      setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(
        JTable table, Object color,
        boolean isSelected, boolean hasFocus,
        int row, int column) {
      Color newColor = (Color) color;
      setBackground(newColor);
      if (isBordered) {
        if (isSelected) {
          if (selectedBorder == null) {
            selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                table.getSelectionBackground());
          }
          setBorder(selectedBorder);
        }
        else {
          if (unselectedBorder == null) {
            unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                table.getBackground());
          }
          setBorder(unselectedBorder);
        }
      }

      setToolTipText("RGB value: " + newColor.getRed() + ", "
                     + newColor.getGreen() + ", "
                     + newColor.getBlue());
      return this;
    }
  }

  class ShapefileFileFilter
      extends javax.swing.filechooser.FileFilter {
    public boolean accept(File f) {
      if (f.isDirectory() || f.getAbsolutePath().endsWith(".shp")) {
        return true;
      }
      return false;
    }

    public String getDescription() {
      return "ESRI Shapefiles (.shp)";
    }
  }

  // ------------------------------------------------------------------------
  // Im2LearnMenu implementation
  // ------------------------------------------------------------------------
  public void setImagePanel(ImagePanel imagepanel) {
    this.imagepanel = imagepanel;
  }
}
