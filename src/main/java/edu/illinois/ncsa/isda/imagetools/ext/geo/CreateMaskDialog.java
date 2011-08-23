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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageMarker;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.calculator.ImageCalculatorDialog;
import edu.illinois.ncsa.isda.imagetools.ext.geo.CreateMask;
import edu.illinois.ncsa.isda.imagetools.ext.segment.ThresholdDialog;



public class CreateMaskDialog extends Im2LearnFrame implements Im2LearnMenu{
    static private Log logger = LogFactory.getLog(CreateMaskDialog.class);
    
    public CreateMaskDialog() {
        this.boundaries = new ArrayList<Point>();
        this.raster = null;
        this.table = null;
        this.raster_Categorical = null;
        this.raster_Threshold = null;
        this.mask = null;
        this.mask2 = null;
        this.finalraster = null;
        this.tableMask = null;
        this.shapeMask = null;
        this.categoricalMask = null;
        this.thresholdMask = null;
        this.paintMask = null;
        createUI();
        checkUI();
    }
    
    private JSplitPane spMain;
    private JSplitPane spOperations;
    private ImagePanel imagepanel;
    private ImagePanel ip;
    private JPanel pnlimageMain;
    private JPanel pnlip;
    private JPanel pnlbtns;
    private JPanel pnlparameters;
    private JPanel pnlcbx;
    private JPanel pnltypes;
    private JPanel pnltypeTable;
    private JPanel pnltypeShape;
    private JPanel pnltypeCategorical;
    private JPanel pnltypeThreshold;
    private JPanel pnltypePaint;
    private JPanel pnlbtnsTable;
    private JPanel pnlbtnsShape;
    private JPanel pnlbtnsCategorical;
    private JPanel pnlbtnsThreshold;
    private JPanel pnlbtnsPaint;
    private JPanel pnlInclude;
    private JTextArea taCounts;
    private JTextField tfradius;
    private JTextField tfsheet;
    private JTextField tfpaintRadius;
    private JLabel lblradius;
    private JLabel lblsheet;
    private JLabel lblpara1;
    private JLabel lblpara2;
    private JLabel lblpara3;
    private JLabel lblprj;
    private JLabel lblpaintType;
    private JLabel lblpaintRadius;
    private JComboBox cbxtype;
    private JComboBox cbxpara1 = new JComboBox();
    private JComboBox cbxpara2 = new JComboBox();
    private JComboBox cbxpara3 = new JComboBox();
    private JComboBox cbxprj = new JComboBox();
    private JComboBox cbxpaintType;
    private JCheckBox include = new JCheckBox("Include");
    private JCheckBox header = new JCheckBox("Header");
    private JCheckBox editName = new JCheckBox("Edit Mask Name");
    
    private JButton btnloadVector_Table = new JButton();
    private JButton btnshowTable_Table = new JButton();
    private JButton btncreate_Table = new JButton();
    private JButton btnapplyMask_Table = new JButton();
    private JButton btnaddToList_Table = new JButton();
    private JButton btnsave_Table = new JButton();
    
    private JButton btnloadVector_Shape = new JButton();
    private JButton btncreate_Shape = new JButton();
    private JButton btnreset_Shape = new JButton();
    private JButton btnapplyMask_Shape = new JButton();
    private JButton btnaddToList_Shape = new JButton();
    private JButton btnsave_Shape = new JButton();
    
    private JButton btnloadRaster_Categorical = new JButton();
    private JButton btncreate_Categorical = new JButton();
    private JButton btnreset_Categorical = new JButton();
    private JButton btnapplyMask_Categorical = new JButton();
    private JButton btnaddToList_Categorical = new JButton();
    private JButton btnsave_Categorical = new JButton();
    
    private JButton btnloadRaster_Threshold = new JButton();
    private JButton btnapplyMask_Threshold = new JButton();
    private JButton btnaddToList_Threshold = new JButton();
    private JButton btnsave_Threshold = new JButton();
    
    private JButton btncreate_Paint = new JButton();
    private JButton btnreset_Paint = new JButton();
    private JButton btnapplyMask_Paint = new JButton();
    private JButton btnaddToList_Paint = new JButton();
    private JButton btnsave_Paint = new JButton();
    
    private ArrayList<Point>  boundaries = new ArrayList();
    
    private boolean isTableLoaded;
    private boolean isShapeLoaded;
    private boolean isCategoricalLoaded;
    private boolean isThreshImageLoaded;
    private boolean isPaintFinished;
    
    private boolean isShp = false;
    private boolean isXls = false;
    private boolean isCSV = false;
    private boolean isCV = false;
    private boolean isRasterLoaded = false;
    private boolean isVectorLoaded = false;
    private boolean drawCircle = false;
    private boolean drawRec = false;
    private boolean firstPaint = true;
    private boolean firstThreshold = true;
    private boolean isTableMaskOut = false;
    private boolean isShapeMaskOut = false;
    private boolean isCategoricalMaskOut = false;
    private boolean isPaintMaskOut = false;
    

    private int radius;
    private int sheet = 0;
    private int lastCbxIndex = 0;
    private int cbxTypeLastIdx = 0;
    private int prjVariableCounts = 2;
    
    private String rasterfileIn = null;
    private String categoricalfileIn = null;
    private String thresholdfileIn = null;
    private String vectorfileIn = null;
    private String csvfileOut = null;
    private String xlsfileOut = null;
    private String voidText = null;
    
    final static String TABLE = "table";
    final static String SHAPEFILE = "shape";
    final static String CATEGORICAL = "categorical";
    final static String THRESHOLD = "threshold";
    final static String PAINT = "paint";
   
    private CreateMask myCreateMask = new CreateMask(); 
    private ImageObject raster;
    private ImageObject mask;
    private ImageObject mask2;
    private ImageObject finalraster;
    
    
    private ImageObject tableMask;
    private ImageObject shapeMask;
    private ImageObject categoricalMask;
    private ImageObject thresholdMask;
    private ImageObject paintMask;
    
    private Projection proj;
    
    private TableModel table;
    private ImageObject raster_Categorical;
    private ImageObject raster_Threshold;
    
    private ThresholdDialog myThreshold = new ThresholdDialog();
    private ImageCalculatorDialog myImageCalculator = new ImageCalculatorDialog(true);
    
    private void createUI() {
       
       ip = new ImagePanel();
       //ip.setImageObject(mask);
       ip.setFakeRGBcolor(true);
       ip.setAutozoom(true);
       
////////////////////////////////////////////////////Buttons-Over/////////////////////////////////////////////////
       tfradius = new JTextField();
       tfradius.setText("0");
       
       tfsheet = new JTextField();
       tfsheet.setText("1");
       
           lblradius = new JLabel("Radius(in pixels)",JLabel.TRAILING);
           lblradius.setLabelFor(tfradius);
           lblsheet = new JLabel("Sheet",JLabel.TRAILING);
           lblsheet.setLabelFor(tfsheet);
           lblpara1 = new JLabel("Parameter 1",JLabel.TRAILING);
           lblpara1.setLabelFor(cbxpara1);
           lblpara2 = new JLabel("Parameter 2",JLabel.TRAILING);
           lblpara2.setLabelFor(cbxpara2);
           lblpara3 = new JLabel("Parameter 3",JLabel.TRAILING);
           lblpara3.setLabelFor(cbxpara3);
           lblprj = new JLabel("Projection",JLabel.TRAILING);
           lblprj.setLabelFor(cbxprj);
           
           pnltypeTable = new JPanel(new GridBagLayout());
           GridBagConstraints ct = new GridBagConstraints();
           ct.fill = GridBagConstraints.HORIZONTAL;
           ct.anchor = GridBagConstraints.PAGE_START;
           ct.insets = new Insets(1,5,1,5);
           ct.gridx = 0;
           ct.gridy = 0;
           ct.weightx = 0;
           pnltypeTable.add(lblprj,ct);
           ct.gridx = 1;
           ct.gridy = 0;
           ct.weightx = 1;
           pnltypeTable.add(cbxprj,ct);
           ct.gridx = 0;
           ct.gridy = 1;
           ct.weightx = 0;
           pnltypeTable.add(lblpara1,ct);
           ct.gridx = 1;
           ct.gridy = 1;
           ct.weightx = 1;
           pnltypeTable.add(cbxpara1,ct);
           ct.gridx = 0;
           ct.gridy = 2;
           ct.weightx = 0;
           pnltypeTable.add(lblpara2,ct);
           ct.gridx = 1;
           ct.gridy = 2;
           ct.weightx = 1;
           pnltypeTable.add(cbxpara2,ct);
           ct.gridx = 0;
           ct.gridy = 3;
           ct.weightx = 0;
           pnltypeTable.add(lblpara3,ct);
           ct.gridx = 1;
           ct.gridy = 3;
           ct.weightx = 1;
           pnltypeTable.add(cbxpara3,ct);
           ct.gridx = 0;
           ct.gridy = 4;
           ct.weightx = 0;
           pnltypeTable.add(lblsheet,ct);
           ct.gridx = 1;
           ct.gridy = 4;
           ct.weightx = 1;
           pnltypeTable.add(tfsheet,ct); 
           ct.gridx = 0;
           ct.gridy = 5;
           ct.weightx = 0;
           pnltypeTable.add(lblradius,ct);
           ct.gridx = 1;
           ct.gridy = 5;
           ct.weightx = 1;
           pnltypeTable.add(tfradius,ct); 
           ct.weightx = 1;
           ct.weighty = 1;
           ct.gridx = 0;
           ct.gridy = 7;
           ct.gridwidth = GridBagConstraints.REMAINDER;
           ct.anchor = GridBagConstraints.CENTER;
           ct.fill = GridBagConstraints.NONE;
           pnltypeTable.add(header,ct); 
           header.setSelected(true);
       //pnltypeTable.setBorder(BorderFactory.createTitledBorder("Table"));
               
       pnltypeShape = new JPanel(new GridLayout(1,2));
       //pnltypeShape.setBorder(BorderFactory.createTitledBorder("Shape"));
       
       pnltypeCategorical = new JPanel(new GridLayout(1,2));
       //pnltypeCategorical.setBorder(BorderFactory.createTitledBorder("Categorical"));
       
       pnltypeThreshold = new JPanel();
       BoxLayout boxLayoutThreshold = new BoxLayout(pnltypeThreshold,BoxLayout.Y_AXIS);
       pnltypeThreshold.setLayout(boxLayoutThreshold);
       
       //pnltypeThreshold.setBorder(BorderFactory.createTitledBorder("Threshold"));
       myThreshold.createUI();
       myThreshold.cbxThreshType.remove(0);
       myThreshold.cbxThreshType.insertItemAt("Scalar",0);

       pnltypeThreshold.add(myThreshold.pnlType);
       pnltypeThreshold.add(myThreshold.pnlSlider);
       
       lblpaintType = new JLabel("what to paint",JLabel.TRAILING);
       lblpaintRadius = new JLabel("Radius",JLabel.TRAILING);
       tfpaintRadius = new JTextField();
       tfpaintRadius.setText("0");
       tfpaintRadius.setEditable(false);
       cbxpaintType = new JComboBox();
       cbxpaintType.addItem("Rectangle");
       cbxpaintType.addItem("Circle");
       cbxpaintType.setSelectedIndex(0);
       lblpaintType.setLabelFor(cbxpaintType);
       lblpaintRadius.setLabelFor(tfpaintRadius);
       pnltypePaint = new JPanel(new GridBagLayout());
       GridBagConstraints cp = new GridBagConstraints();
       cp.fill = GridBagConstraints.HORIZONTAL;
       cp.insets = new Insets(1,5,1,5);
       cp.gridx = 0;
       cp.gridy = 0;
       cp.weightx = 0;
       pnltypePaint.add(lblpaintType,cp);
       cp.gridx = 1;
       cp.gridy = 0;
       cp.weightx = 1;
       pnltypePaint.add(cbxpaintType,cp);
       cp.gridx = 0;
       cp.gridy = 1;
       cp.weightx = 0;
       cp.weighty = 1;
       cp.anchor = GridBagConstraints.PAGE_START;
       pnltypePaint.add(cbxpaintType,cp);
       cp.gridx = 1;
       cp.gridy = 1;
       cp.weightx = 1;
       cp.weighty = 1;
       pnltypePaint.add(cbxpaintType,cp);
       
       pnltypes = new JPanel(new CardLayout());
       pnltypes.add(pnltypeShape,"SHAPEFILE");
       pnltypes.add(pnltypeTable,"TABLE");
       pnltypes.add(pnltypeCategorical,"CATEGORICAL");
       pnltypes.add(pnltypeThreshold,"THRESHOLD");
       pnltypes.add(pnltypePaint,"USER DEFINED");
       
       GridBagConstraints btnscl = new GridBagConstraints();
       btnscl.weightx = 1;
       btnscl.fill = GridBagConstraints.HORIZONTAL;
       
       pnlbtnsTable = new JPanel(new GridBagLayout());
       pnlbtnsTable.add(btnloadVector_Table,btnscl);
       pnlbtnsTable.add(btnshowTable_Table,btnscl);
       pnlbtnsTable.add(btncreate_Table,btnscl);
       pnlbtnsTable.add(btnaddToList_Table,btnscl);
       pnlbtnsTable.add(btnapplyMask_Table,btnscl);
       pnlbtnsTable.add(btnsave_Table,btnscl);
       
       pnlbtnsShape = new JPanel(new GridBagLayout());
       pnlbtnsShape.add(btnloadVector_Shape,btnscl);
       pnlbtnsShape.add(btncreate_Shape,btnscl);
       pnlbtnsShape.add(btnaddToList_Shape,btnscl);
       pnlbtnsShape.add(btnreset_Shape,btnscl);
       pnlbtnsShape.add(btnapplyMask_Shape,btnscl);
       pnlbtnsShape.add(btnsave_Shape,btnscl);
       
       pnlbtnsCategorical = new JPanel(new GridBagLayout());
       pnlbtnsCategorical.add(btnloadRaster_Categorical,btnscl);
       pnlbtnsCategorical.add(btncreate_Categorical,btnscl);
       pnlbtnsCategorical.add(btnaddToList_Categorical,btnscl);
       pnlbtnsCategorical.add(btnreset_Categorical,btnscl);
       pnlbtnsCategorical.add(btnapplyMask_Categorical,btnscl);
       pnlbtnsCategorical.add(btnsave_Categorical,btnscl);
       
       pnlbtnsThreshold = new JPanel(new GridBagLayout());
       pnlbtnsThreshold.add(btnloadRaster_Threshold,btnscl);
       pnlbtnsThreshold.add(btnaddToList_Threshold,btnscl);
       pnlbtnsThreshold.add(btnapplyMask_Threshold,btnscl);
       pnlbtnsThreshold.add(btnsave_Threshold,btnscl);
       
       pnlbtnsPaint = new JPanel(new GridBagLayout());
       pnlbtnsPaint.add(btncreate_Paint,btnscl);
       pnlbtnsPaint.add(btnaddToList_Paint,btnscl);
       pnlbtnsPaint.add(btnreset_Paint,btnscl);
       pnlbtnsPaint.add(btnapplyMask_Paint,btnscl);
       pnlbtnsPaint.add(btnsave_Paint,btnscl);
       
       pnlbtns = new JPanel(new CardLayout());
       pnlbtns.add(pnlbtnsTable,"TABLE");
       pnlbtns.add(pnlbtnsShape,"SHAPEFILE");
       pnlbtns.add(pnlbtnsCategorical,"CATEGORICAL");
       pnlbtns.add(pnlbtnsThreshold,"THRESHOLD");
       pnlbtns.add(pnlbtnsPaint,"USER DEFINED");
       
       final String [] cbxItems  = {"SHAPEFILE","TABLE","CATEGORICAL","THRESHOLD","USER DEFINED"};
       cbxtype = new JComboBox(cbxItems);
       cbxtype.setSelectedIndex(0);
       cbxtype.setEditable(false);
       
       pnlcbx = new JPanel();
       pnlcbx.add(cbxtype);
       
       pnlInclude = new JPanel(new GridBagLayout());
       GridBagConstraints cii = new GridBagConstraints();
       cii.gridx = 0;
       cii.gridy = 0;
       cii.weightx = 1;
       cii.weighty = 1;
       cii.anchor = GridBagConstraints.LINE_START;
       pnlInclude.add(include,cii);
       cii.gridx = 1;
       cii.gridy = 0;
       pnlInclude.add(editName,cii);
       
       include.setSelected(true);
       editName.setSelected(false);
       
       GridBagConstraints ci = new GridBagConstraints();
       pnlparameters = new JPanel(new GridBagLayout());
       ci.gridx = 0;
       ci.gridy = 0;
       ci.fill = GridBagConstraints.HORIZONTAL;
       ci.weightx = 1;
       pnlparameters.add(pnlcbx,ci);
       ci.gridx = 0;
       ci.gridy = 1;
       ci.fill = GridBagConstraints.HORIZONTAL;
       ci.weightx = 1;
       pnlparameters.add(pnltypes,ci);
       ci.gridx = 0;
       ci.gridy = 2;
       ci.weightx = 1;
       ci.weighty = 1;
       ci.fill = GridBagConstraints.HORIZONTAL;
       ci.anchor = GridBagConstraints.PAGE_START;
       pnlparameters.add(pnlInclude,ci);
       
       pnlparameters.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Parameters"),BorderFactory.createEmptyBorder(5,5,5,5)));
       
       GridLayout gl = new GridLayout(3,3);
       gl.setHgap(5);
       gl.setVgap(2);
       
       myImageCalculator._cboxLoad.setText("Load Mask");
       myImageCalculator.btnMaskApply.setEnabled(false);
       
       myImageCalculator.btnSave.setEnabled(false);
       myImageCalculator._cboxDone.setVisible(false);
       
       spOperations = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,pnlparameters,myImageCalculator.pnl);
       spOperations.setOneTouchExpandable(true);
       spOperations.setDividerLocation(0.5);
       
       taCounts = new JTextArea();
       taCounts.setEditable(false);
       voidText = " Invalid Pixels     =     " + 0 + "     (Color : black, Value : 0)" + "\n   Valid Pixels     =     " + 0 +  "      (Color : white, Value : 255)" +"\n   Total Pixels     =     " + 0;
       taCounts.setText(voidText);
       
       pnlip = new JPanel();
       pnlip.setLayout(new BorderLayout());
       pnlip.add(new JScrollPane(ip),BorderLayout.CENTER);
       pnlip.add(taCounts,BorderLayout.SOUTH);
       
       pnlimageMain = new JPanel(new BorderLayout());
       pnlimageMain.add(pnlip,BorderLayout.CENTER);
       pnlimageMain.add(pnlbtns,BorderLayout.SOUTH);
       
       //spMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(pnlip), new JScrollPane(spOperations));
       spMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, spOperations, pnlimageMain);
       spMain.setOneTouchExpandable(true);
       spMain.setDividerLocation(0.3);
       
       
       btnapplyMask_Shape.setEnabled(false);
       btnsave_Shape.setEnabled(false);
       btnaddToList_Shape.setEnabled(false);
       btncreate_Shape.setEnabled(false);
       btnreset_Shape.setEnabled(false);
       
       setLayout(new BorderLayout());
       add(spMain,BorderLayout.CENTER);
       pack();
       
       //implementating the actions of gui components:
       ip.addMouseListener(new MouseAdapter() {
           public void mouseClicked(MouseEvent e) {
               if (e.getButton() != MouseEvent.BUTTON1) {
                   return;
               }
               if (isShp || isCV){
                   addMarker(e.getX(),e.getY());
               }else{
                   System.out.println("This image is unable to add marker on");
               }
               
               }
       });

       
//////////////////////////////////////////////Buttons-Table///////////////////////////////////////////////////////////
       btnloadVector_Table.setAction(new AbstractAction("Load Table") {
           public void actionPerformed(ActionEvent e) {
               loadTableFile();
           }
       });

       btncreate_Table.setAction(new AbstractAction("Create Mask") {
           public void actionPerformed(ActionEvent e) {
               create("Table");
           }
       });
       
       btnaddToList_Table.setAction(new AbstractAction("Add To List") {
           public void actionPerformed(ActionEvent e) {
               addToList(tableMask,"TableMask");
               checkUI();
           }
       });
       
       btnapplyMask_Table.setAction(new AbstractAction("Apply Mask") {
           public void actionPerformed(ActionEvent e) {
              applyMask(mask2);
              }
       });
       
       btnsave_Table.setAction(new AbstractAction("Save") {
           public void actionPerformed(ActionEvent e) {
               saveImage(ip.getImageObject());
           }
       });
       
       btnshowTable_Table.setAction(new AbstractAction("ShowTable") {
           public void actionPerformed(ActionEvent e) {
               showTable(isTableLoaded);
            }
       });

//////////////////////////////////////////////Buttons-Shape///////////////////////////////////////////////////////////
       btnloadVector_Shape.setAction(new AbstractAction("Load Shape") {
           public void actionPerformed(ActionEvent e) {
               loadShapeFile();
           }
       });

       btncreate_Shape.setAction(new AbstractAction("Create Mask") {
           public void actionPerformed(ActionEvent e) {
               create("Shape");
           }
       });
       
       btnaddToList_Shape.setAction(new AbstractAction("Add To List") {
           public void actionPerformed(ActionEvent e) {
               addToList(shapeMask,"ShapeMask");
               checkUI();
               }
       });

       btnreset_Shape.setAction(new AbstractAction("Reset") {
           public void actionPerformed(ActionEvent e) {
              reset();
              checkUI();
           }
       });
       
       btnapplyMask_Shape.setAction(new AbstractAction("Apply Mask") {
           public void actionPerformed(ActionEvent e) {
              applyMask(mask2);
              }
       });
       
       btnsave_Shape.setAction(new AbstractAction("Save") {
           public void actionPerformed(ActionEvent e) {
               saveImage(ip.getImageObject());
           }
       });

//////////////////////////////////////////////Buttons-Categorical/////////////////////////////////////////////////////
       btnloadRaster_Categorical.setAction(new AbstractAction("Load Raster") {
           public void actionPerformed(ActionEvent e) {
               loadCategoricalFile();
           }
       });

       btncreate_Categorical.setAction(new AbstractAction("Create Mask") {
           public void actionPerformed(ActionEvent e) {
               create("Categorical");
           }
       });
       
       btnaddToList_Categorical.setAction(new AbstractAction("Add To List") {
           public void actionPerformed(ActionEvent e) {
               addToList(categoricalMask,"CategoricalMask");
               checkUI();}
       });

       btnreset_Categorical.setAction(new AbstractAction("Reset") {
           public void actionPerformed(ActionEvent e) {
              reset();
              checkUI();
           }
       });
       
       btnapplyMask_Categorical.setAction(new AbstractAction("Apply Mask") {
           public void actionPerformed(ActionEvent e) {
              applyMask(mask2);
              }
       });
       
       btnsave_Categorical.setAction(new AbstractAction("Save") {
           public void actionPerformed(ActionEvent e) {
               saveImage(ip.getImageObject());
           }
       });

//////////////////////////////////////////////Buttons-Threshold//////////////////////////////////////////////////////
       btnloadRaster_Threshold.setAction(new AbstractAction("Load Threshold Image") {
           public void actionPerformed(ActionEvent e) {
               loadThresholdFile();
           }
       });
       
       btnaddToList_Threshold.setAction(new AbstractAction("Add To List") {
           public void actionPerformed(ActionEvent e) {
               thresholdMask = myThreshold.myThresh.getImageObject();
               addToList(thresholdMask,"ThresholdMask");
               checkUI();
           }
       });
       
       btnapplyMask_Threshold.setAction(new AbstractAction("Apply Mask") {
           public void actionPerformed(ActionEvent e) {
              thresholdMask = myThreshold.myThresh.getImageObject();
              applyMask(thresholdMask);
              }
       });
       
       btnsave_Threshold.setAction(new AbstractAction("Save") {
           public void actionPerformed(ActionEvent e) {
               saveImage(myThreshold.ip.getImageObject());
           }
       });
//////////////////////////////////////////////Buttons-Paint///////////////////////////////////////////////////////////
       
       btncreate_Paint.setAction(new AbstractAction("Create Mask") {
           public void actionPerformed(ActionEvent e) {
               create("Paint");
           }
       });
       
       btnaddToList_Paint.setAction(new AbstractAction("Add To List") {
           public void actionPerformed(ActionEvent e) {
               addToList(paintMask,"PaintMask");
               checkUI();
           }
       });

       btnreset_Paint.setAction(new AbstractAction("Reset") {
           public void actionPerformed(ActionEvent e) {
              reset();
              checkUI();
           }
       });
       
       btnapplyMask_Paint.setAction(new AbstractAction("Apply Mask") {
           public void actionPerformed(ActionEvent e) {
              applyMask(mask2);
              }
       });
       
       btnsave_Paint.setAction(new AbstractAction("Save") {
           public void actionPerformed(ActionEvent e) {
               saveImage(ip.getImageObject());
           }
       });
       
       tfradius.setAction(new AbstractAction() {
           public void actionPerformed(ActionEvent e) {
                   if(proj.getScaleX()==proj.getScaleY()){
                       radius = Integer.parseInt(tfradius.getText());
                   }else{
                       System.out.println("The image has different scales in X,Y directions.");
                       return;
                   }
               try{
                   //radius = Double.parseDouble((tfradius.getText()));
                   sheet = Integer.parseInt(tfsheet.getText())-1;
                   ImageObject imgobj = raster;
                   mask2 = CreateMask.createMask(imgobj,vectorfileIn,radius,cbxpara1.getSelectedIndex(),cbxpara2.getSelectedIndex(),include.isSelected(),sheet,header.isSelected());
                   setImagePanel(mask2);
                   getCounts(mask2);
                   tableMask = mask2;
                   //isMaskOut = true;
                   isTableMaskOut = true;
                   checkUI();
               }catch(Exception exc){
                   
               }
               checkUI();
            }
        });
       
       tfsheet.setAction(new AbstractAction() {
           public void actionPerformed(ActionEvent e) {
               try{
                   if(vectorfileIn.endsWith(".xls")){
                       radius = Integer.parseInt((tfradius.getText()));
                       sheet = Integer.parseInt(tfsheet.getText())-1;
                   myCreateMask.table = ExcelTable.excelToTable(vectorfileIn,sheet,header.isSelected());
                   ImageObject imgobj = raster;
                   mask2 = CreateMask.createMask(imgobj,vectorfileIn,radius,cbxpara1.getSelectedIndex(),cbxpara2.getSelectedIndex(),include.isSelected(),sheet,header.isSelected());
                   setImagePanel(mask2);
                   tableMask = mask2;
                   getCounts(mask2);
                   isTableMaskOut = true;
                   //isMaskOut = true;
                   checkUI();
               }
             }catch (Exception exc) {
                 logger.error("Error loading vector file.", exc);
           }} 
        });
       
       cbxpaintType.setAction(new AbstractAction(){
           public void actionPerformed(ActionEvent e){
               if(cbxpaintType.getSelectedIndex() == 0){
                   tfpaintRadius.setEnabled(false);
               }else if(cbxpaintType.getSelectedIndex() == 1){
                   tfpaintRadius.setEnabled(true);
               }
           }
       });
       
       cbxtype.setAction(new AbstractAction() {
           public void actionPerformed(ActionEvent e) {
               if ("comboBoxChanged".equals(e.getActionCommand())) {
                   CardLayout cltype = (CardLayout)(pnltypes.getLayout());
                   CardLayout clbtns = (CardLayout)(pnlbtns.getLayout());
                   if(cbxtype.getSelectedIndex()==0){
                       cltype.show(pnltypes,cbxItems[0]);
                       clbtns.show(pnlbtns,cbxItems[0]);
                       if(cbxTypeLastIdx == 3){
                           pnlimageMain.remove(myThreshold.geoleanrpnlip);
                           pnlimageMain.add(pnlip,BorderLayout.CENTER);
                           taCounts.setText(voidText);
                       }
                       setImagePanel(shapeMask);
                       checkUI();
                       cbxTypeLastIdx = 0;
                   }else if(cbxtype.getSelectedIndex()==1){
                       cltype.show(pnltypes,cbxItems[1]);
                       clbtns.show(pnlbtns,cbxItems[1]);
                       if(cbxTypeLastIdx == 3){
                           pnlimageMain.remove(myThreshold.geoleanrpnlip);
                           pnlimageMain.add(pnlip,BorderLayout.CENTER);
                           taCounts.setText(voidText);
                       }
                       setImagePanel(tableMask);
                       checkUI();
                       cbxTypeLastIdx = 1;
                   }else if(cbxtype.getSelectedIndex()==2){
                       cltype.show(pnltypes,cbxItems[2]);
                       clbtns.show(pnlbtns,cbxItems[2]);
                       if(cbxTypeLastIdx == 3){
                           pnlimageMain.remove(myThreshold.geoleanrpnlip);
                           pnlimageMain.add(pnlip,BorderLayout.CENTER);
                           taCounts.setText(voidText);
                       }
                       setImagePanel(categoricalMask);
                       checkUI();
                       cbxTypeLastIdx = 2;
                   }else if(cbxtype.getSelectedIndex()==3){
                       
                       cltype.show(pnltypes,cbxItems[3]);
                       clbtns.show(pnlbtns,cbxItems[3]);
                       if(firstThreshold){
                           myThreshold.geolearn = true;
                           myThreshold.imagepanel = new ImagePanel(raster);
                           myThreshold.showing();
                           firstThreshold = false;
                       }
                       if(cbxTypeLastIdx != 3){
                           pnlimageMain.remove(pnlip);
                           pnlimageMain.add(myThreshold.geoleanrpnlip,BorderLayout.CENTER);
                       }
                       setImagePanel(myThreshold.myThresh.getImageObject());
                       checkUI();
                       cbxTypeLastIdx = 3;
                   }else if(cbxtype.getSelectedIndex()==4){
                       cltype.show(pnltypes,cbxItems[4]);
                       clbtns.show(pnlbtns,cbxItems[4]);
                       if(cbxTypeLastIdx == 3){
                           pnlimageMain.remove(myThreshold.geoleanrpnlip);
                           pnlimageMain.add(pnlip,BorderLayout.CENTER);
                           taCounts.setText(voidText);
                       }
                       setImagePanel(paintMask);
                       checkUI();
                       cbxTypeLastIdx = 4;
                       
                   }
                   }
               }
        });
       
       myImageCalculator.btnMaskApply.setAction(new AbstractAction("Apply Mask"){
           public void actionPerformed(ActionEvent e){
               if(myImageCalculator._imageList.getSelectedIndices().length > 1){
                   myImageCalculator._imageList.setSelectedIndex(myImageCalculator._imageList.getSelectedIndex());
                   logger.error("Only one mask can be applied each time.");
               }else{
                   applyMask(myImageCalculator._imageListModel.getMask(myImageCalculator._imageList.getSelectedIndex()));
               }
               }
       });
       
       myImageCalculator.btnSave.setAction(new AbstractAction("Save Mask"){
           public void actionPerformed(ActionEvent e){
               if(myImageCalculator._imageList.getSelectedIndices().length > 1){
                   myImageCalculator._imageList.setSelectedIndex(myImageCalculator._imageList.getSelectedIndex());
                   logger.error("Only one mask can be saved each time.");
               }else{
                   saveImage(myImageCalculator._imageListModel.getMask(myImageCalculator._imageList.getSelectedIndex()));
               }
           }
       });
       
       }
    
   public void setImagePanel(ImageObject imgobj){
       if(cbxTypeLastIdx == 3){
          myThreshold.ip.setImageObject(imgobj); 
       }else{
           ip.setImageObject(imgobj);
       }
   }
   
   public void setRaster(ImageObject imgobj){
       try {
        
        proj = (Projection)imgobj.getProperty(ImageObject.GEOINFO);
        if(proj==null){
            logger.info("The raster does not contain geoinformation!");
        }
        raster = (ImageObject)imgobj.clone();
        isRasterLoaded = true;
        setImagePanel(raster);
        mask2 = new ImageObjectByte(raster.getNumRows(),raster.getNumCols(),1);
        mask2.setProperty(ImageObject.GEOINFO,proj);
        tableMask = raster;
        shapeMask = raster;
        categoricalMask = raster;
        paintMask = raster;
        checkUI();
    } catch (CloneNotSupportedException e) {
        e.printStackTrace();
    }
       
   }
   
   private  void addMarker(int x, int y) {
        boolean isMaskOut = false;
        if(cbxtype.getSelectedIndex() == 0){
            isMaskOut = isShapeMaskOut;
        }else if(cbxtype.getSelectedIndex() == 2){
            isMaskOut = isCategoricalMaskOut;
        }
        Point pt = ip.getImageLocation(new Point(x, y));
        if(isCV){
            int red = mask.getInt(pt.y, pt.x, 0);
            int green = mask.getInt(pt.y, pt.x, 1);
            int blue = mask.getInt(pt.y, pt.x, 2);
            if(checkLabel(red,green,blue) || isMaskOut ){
                return;
            }
        }else{
        int boundary = mask.getInt(pt.y, pt.x, 0);
        if ((boundary == 0) || checkBoundary(boundary)|| isMaskOut) {
            return;
        }
        }
        ImageMarker mark = new ImageMarker(pt.x, pt.y, 15, 15, ImageMarker.CROSS);
        mark.setColor(Color.red);
        mark.setVisible(true);
        ip.addAnnotationImage(mark);
        boundaries.add(pt);
        checkUI();
    }
   
   private boolean addToList(ImageObject imgobj,String name){
       if(imgobj != null){
           if(editName.isSelected()){
               String name2 = myImageCalculator._tfRename.getText();
               if(name2 == null){
                   logger.error("No name entered.");
                   myImageCalculator._imageListModel.addMask(imgobj,name);
                   return true;
               }else{
                   myImageCalculator._imageListModel.addMask(imgobj,name2);
                   return true;
               }
               
           }else{
               myImageCalculator._imageListModel.addMask(imgobj,name);
               return true;
           }
       }else{
           System.err.println("This mask is null"); 
           return false;
       }
   }
   
   public boolean checkBoundary(int boundary) {
       for(Point bpt : boundaries) {
           if (boundary == mask.getInt(bpt.y, bpt.x, 0)) {
               return true;
           }
       }
       return false;
   }
   
   public boolean checkLabel(int red, int green, int blue){
       for(Point bpt : boundaries){
           if (isCV && 
               red == mask.getInt( bpt.y, bpt.x, 0) && 
               green == mask.getInt( bpt.y, bpt.x, 1) && 
               blue == mask.getInt( bpt.y, bpt.x, 2)){
               return true;
           }
        }
       return false;
   }
   
   public ImageObject createmask(ImageObject targetImage, ImageObject actionImage,boolean include, boolean isCategorical) {
       if(include){
           //targetImage = new ImageObjectByte(actionImage.getNum)
           targetImage.setImageObjectValue(0);
           for(int idx=0, row=0; row<actionImage.getNumRows(); row++) {
               for(int col=0; col<actionImage.getNumCols(); col++, idx++) {
                  if(isCategorical){
                      int red = actionImage.getInt(row,col,0);
                      int green = actionImage.getInt(row,col,1);
                      int blue = actionImage.getInt(row,col,2);
                      if(!checkLabel(red,green,blue)){
                          continue;
                      }
                      }else{
                          if (!checkBoundary(actionImage.getInt(idx))) {
                              continue;
                              }
                          }
                  targetImage.set(row,col,0,255);
                  }
           }
       }else{
           targetImage.setImageObjectValue(255);
           for(int idx=0, row=0; row<actionImage.getNumRows(); row++) {
               for(int col=0; col<actionImage.getNumCols(); col++, idx++) {
                   if(isCategorical){
                      int red = actionImage.getInt(row,col,0);
                      int green = actionImage.getInt(row,col,1);
                      int blue = actionImage.getInt(row,col,2);
                      if(!checkLabel(red,green,blue)){
                          continue;
                      }
                      }else{
                          if (!checkBoundary(actionImage.getInt(idx)) && actionImage.getInt(idx)!=0) {
                              continue;
                              }
                          }
                   targetImage.set(row,col,0,0);
                 }
           }
       }
       return targetImage;
   }
   
   private void applyMask(ImageObject mask){
       try {
       if(mask == null){
           System.err.println("The mask to apply is null");
           return;
       }

       ImageObject img;
        img = (ImageObject)raster.clone();
       ImageObject curMask = (ImageObject)mask.clone();
       int type = img.getType();
       if(type == ImageObject.TYPE_BYTE){
           int white = 255;
           int black = 0;
           for(int i=0;i<img.getNumRows();i++){
               for(int j=0;j<img.getNumCols();j++){
                   for(int k=0;k<img.getNumBands();k++){
                       if(curMask.getInt(i,j,0)==0){
                           img.set(i,j,k,isCV?white:black);
                       }
                    }
               }
           }
       }else if(type==ImageObject.TYPE_SHORT || type==img.TYPE_USHORT){
           int white = Short.MAX_VALUE;
           int black = Short.MIN_VALUE;
           for(int i=0;i<img.getNumRows();i++){
               for(int j=0;j<img.getNumCols();j++){
                   for(int k=0;k<img.getNumBands();k++){
                       if(curMask.getInt(i,j,0)==0){
                           img.set(i,j,k,isCV?white:black);
                       }
                    }
               }
           }
       }else if(type==ImageObject.TYPE_INT){
           int white = Integer.MAX_VALUE;
           int black = Integer.MIN_VALUE;
           for(int i=0;i<img.getNumRows();i++){
               for(int j=0;j<img.getNumCols();j++){
                   for(int k=0;k<img.getNumBands();k++){
                       if(curMask.getInt(i,j,0)==0){
                           img.set(i,j,k,isCV?white:black);
                       }
                    }
               }
           }
       }else if(type==ImageObject.TYPE_LONG){
           long white = Long.MAX_VALUE;
           long black = Long.MIN_VALUE;
           for(int i=0;i<img.getNumRows();i++){
               for(int j=0;j<img.getNumCols();j++){
                   for(int k=0;k<img.getNumBands();k++){
                       if(curMask.getInt(i,j,0)==0){
                           img.set(i,j,k,isCV?white:black);
                       }
                    }
               }
           }
       }else if(type==ImageObject.TYPE_FLOAT){
           float white = Float.MAX_VALUE;
           float black = Float.MIN_VALUE;
           for(int i=0;i<img.getNumRows();i++){
               for(int j=0;j<img.getNumCols();j++){
                   for(int k=0;k<img.getNumBands();k++){
                       if(curMask.getInt(i,j,0)==0){
                           img.set(i,j,k,isCV?white:black);
                       }
                    }
               }
           }
       }else if(type==ImageObject.TYPE_DOUBLE){
           double white = Double.MAX_VALUE;
           double black = Double.MIN_VALUE;
           for(int i=0;i<img.getNumRows();i++){
               for(int j=0;j<img.getNumCols();j++){
                   for(int k=0;k<img.getNumBands();k++){
                       if(curMask.getInt(i,j,0)==0){
                           img.set(i,j,k,isCV?white:black);
                       }
                    }
               }
           }
       }
       finalraster = img;
       setImagePanel(img);
       } catch (CloneNotSupportedException e) {
           e.printStackTrace();
       }
   }
   
   
   
   private void showTable(boolean show){
       try{
       JFrame frame = new JFrame("Table");
       JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
       
       boolean multipleSheet = false;
       if(vectorfileIn.endsWith(".xls")){
           Workbook w = Workbook.getWorkbook(new File(vectorfileIn));
           Sheet[] sht = w.getSheets();
           if(sht.length > 1){
               multipleSheet = true;
           }
       }
       if(show){
       if(!multipleSheet){
           JPanel pnlTable = new JPanel();
           JTable table = new JTable(myCreateMask.table);
           pnlTable.setLayout(new BorderLayout());
           pnlTable.add(new JScrollPane(table),BorderLayout.CENTER);
           frame.add(pnlTable,BorderLayout.CENTER);
       }else{
           Workbook w = Workbook.getWorkbook(new File(vectorfileIn));
           Sheet[] sht = w.getSheets();
           TableModel[] tm = ExcelTable.excelToTable(vectorfileIn,header.isSelected());
           JTabbedPane tptables = new JTabbedPane();
           for(int i=0;i<tm.length;i++){
               JPanel pnl = new JPanel(new BorderLayout());
               JTable jt = new JTable(tm[i]);
               pnl.add(new JScrollPane(jt),BorderLayout.CENTER);
               tptables.addTab(sht[i].getName(),pnl);
               tptables.setTabPlacement(JTabbedPane.BOTTOM);
           }
           frame.add(tptables,BorderLayout.CENTER);
       }
       
       frame.add(pnlBtns,BorderLayout.SOUTH);
       frame.pack();
       frame.setVisible(true);
       }else{
           System.out.println("No table available to save");
       }
       }catch(Exception e){
           
       }
   }
   
   //to draw a rectangle
   private void paint(ImageObject imgobj,Point a, Point b, boolean include){
       if(firstPaint){
           if(include){
               paintMask.setImageObjectValue(0);
           }else{
               paintMask.setImageObjectValue(255);
           }
       }
       Point pta = ip.getImageLocation(a);
       Point ptb = ip.getImageLocation(b);
       int finalColor = 255;
       if(!include){
           finalColor = 0;
       }
       for(int i=pta.x;i<=ptb.x;i++){
           for(int j=pta.y;j<=ptb.y;j++){
               imgobj.setInt(i,j,0,finalColor);
           }
       }
   }
   
   //to draw a circle
   private void paint(ImageObject imgobj,Point center,double radius, boolean include){
       if(firstPaint){
           if(include){
               paintMask.setImageObjectValue(0);
           }else{
               paintMask.setImageObjectValue(255);
           }
       }
       Point pt = ip.getImageLocation(center);
       int finalColor = 255;
       if(!include){
           finalColor = 0;
       } 
       Projection proj = (Projection)imgobj.getProperty(ImageObject.GEOINFO);
        double scaleX = proj.getScaleX();
        double scaleY = proj.getScaleY();
        int rangeX,rangeY;
        if (radius > 0) {
            rangeX = (int)((radius-radius%scaleX)/scaleX+1);
            rangeY = (int)((radius-radius%scaleY)/scaleY+1);
            for(int i=pt.x-rangeX;i<=pt.x+rangeX;i++){
                for(int j=pt.y-rangeY;j<=pt.y+rangeY;j++){
                        if ((i>=0)&&(j<imgobj.getNumRows())&&(j>=0)&&(j<imgobj.getNumCols())){
                            double r = Math.hypot(Math.abs(i-pt.x)*scaleX,Math.abs(j-pt.y)*scaleY);
                            if(r*r<=radius*radius){
                                mask.set(i,j,0,finalColor);
                            }
                        }
                    }
                }
                
            } else {
                mask.set(pt.x,pt.y,0,finalColor);
            }
        
   }
   
   private void reset() {
       include.setSelected(true);
       if(cbxtype.getSelectedIndex() == 0){
           if ((boundaries.size() != 0) || (boundaries.size()==0 && isShapeMaskOut)) {
               boundaries.clear();
               ip.removeAllAnnotations();
               setImagePanel(mask);
               mask2.setImageObjectValue(0);
               isShapeMaskOut = false;
               finalraster = raster;
               checkUI();
           }
       }else if(cbxtype.getSelectedIndex() == 2){
           if ((boundaries.size() != 0) || (boundaries.size()==0 && isCategoricalMaskOut)) {
               boundaries.clear();
               ip.removeAllAnnotations();
               setImagePanel(raster_Categorical);
               mask2.setImageObjectValue(0);
               isCategoricalMaskOut = false;
               finalraster = raster;
               checkUI();
           }
       }else if(cbxtype.getSelectedIndex() == 4){
           
       }
   }
   
   private void readColName(int variables){
       cbxpara1.removeAll();
       cbxpara2.removeAll();
       cbxpara3.removeAll();
        for(int i=0;i<table.getColumnCount();i++){
            cbxpara1.addItem(table.getColumnName(i));
            cbxpara2.addItem(table.getColumnName(i));
            cbxpara3.addItem(table.getColumnName(i));
        }
   }
   public void loadTableFile(){
           JFileChooser jfc = new JFileChooser() {
               public boolean accept(File f) {
                   String filename = f.getName().toLowerCase();
                   return f.isDirectory() || filename.endsWith(".csv") || filename.endsWith(".xls"); 
               }
           };
           int result = jfc.showOpenDialog(CreateMaskDialog.this);
           if (result != JFileChooser.APPROVE_OPTION) 
               return;
           String filename = jfc.getSelectedFile().getAbsolutePath();
           try {
               if(filename!=null){
                   vectorfileIn = filename;
                   if(filename.endsWith(".csv")){
                        isCSV = true;
                        isTableLoaded = true;
                        myCreateMask.table = new CSVTable().readTable(vectorfileIn,header.isSelected());
                        table = myCreateMask.table;
                        readColName(prjVariableCounts);
                        
                    }
                    else if(filename.endsWith(".xls")) {
                        isXls = true;
                        isTableLoaded = true;
                        myCreateMask.table = ExcelTable.excelToTable(filename,sheet,header.isSelected());
                        table = myCreateMask.table;
                        readColName(prjVariableCounts);
                        
                    }else{
                        System.out.println("Only CSV and Excel files are supported!");
                   }
                   }
               } catch (Exception exc) {
               logger.error("Error loading vector file.", exc);
           }
       checkUI();
   }
   
   public void loadShapeFile(){
       JFileChooser jfc = new JFileChooser() {
           public boolean accept(File f) {
               String filename = f.getName().toLowerCase();
               return f.isDirectory() || filename.endsWith(".shp"); 
           }
       };
       int result = jfc.showOpenDialog(CreateMaskDialog.this);
       if (result != JFileChooser.APPROVE_OPTION) 
           return;
       String filename = jfc.getSelectedFile().getAbsolutePath();
       try {
           if(filename.endsWith(".shp")){
               vectorfileIn = filename;
                    ImageObject imgobj = raster;
                    mask = myCreateMask.createMask(imgobj,vectorfileIn);
                    setImagePanel(mask);
                    isShp = true;
                    isVectorLoaded = true;
                    isShapeLoaded = true;
                }else{
                    System.out.println("Only shape files are supported!");
               }
           } catch (Exception exc) {
           logger.error("Error loading shape file.", exc);
       }
   }
   
   private void loadCategoricalFile(){

       FileChooser fc = new FileChooser();
       fc.setTitle("Load Categorical Raster");
       try {
           String filename = fc.showOpenDialog();
           if (filename != null) {
               rasterfileIn = filename;
               raster_Categorical = ImageLoader.readImage(filename);
               proj = (Projection)raster_Categorical.getProperty(ImageObject.GEOINFO);
               if(proj==null)
                   System.out.println("Warning : The raster does not contain geoinformation!");
               if(raster.getNumRows() == raster_Categorical.getNumRows() && 
                   raster.getNumCols() == raster_Categorical.getNumCols()){
                   mask2 = new ImageObjectByte(raster.getNumRows(),raster.getNumCols(),1);
                   mask2.setProperty(ImageObject.GEOINFO,proj);
                   isCategoricalLoaded = true;
                   //isRasterLoaded = true;
                   setImagePanel(raster_Categorical);
                   mask = raster_Categorical;
                   checkUI();
               }else{
                   System.err.println("The newly loaded categorical image has different row counts or col counts from the original raster.");
                   return;
               }
           }
       } catch (IOException exc) {
           logger.error("Error loading categorical image ", exc);
       }
   
   }
   
   public void loadThresholdFile(){

       FileChooser fc = new FileChooser();
       fc.setTitle("Load Threshold Raster");
       try {
           String filename = fc.showOpenDialog();
           if (filename != null) {
               raster_Threshold = ImageLoader.readImage(filename);
               proj = (Projection)raster_Threshold.getProperty(ImageObject.GEOINFO);
               if(proj==null)
                   System.out.println("Warning : The raster does not contain geoinformation!");
               if(raster.getNumRows() == raster_Threshold.getNumRows() && 
                       raster.getNumCols() == raster_Threshold.getNumCols()){
                   mask2 = new ImageObjectByte(raster.getNumRows(),raster.getNumCols(),1);
                   mask2.setProperty(ImageObject.GEOINFO,proj);
                   isThreshImageLoaded = true;
                   myThreshold.geolearn = true;
                   myThreshold.ip = new ImagePanel(raster_Threshold);
                   myThreshold.showing();
                   setImagePanel(raster_Threshold);
                   checkUI();
               }else{
                   System.err.println("The newly loaded threshold image has different row counts or col counts from the original raster.");
                   return;
               }
           }
       } catch (IOException exc) {
           logger.error("Error loading threshold image", exc);
       }
   
   }
   
   public void create(String type){
       if(type.equalsIgnoreCase("Table")){

           try{
                   if(proj.getScaleX()==proj.getScaleY()){
                       radius = Integer.parseInt(tfradius.getText());
                   }else{
                       System.out.println("The image has different scales in X,Y directions.");
                       reset();
                       return;
                   }
               sheet = Integer.parseInt(tfsheet.getText())-1;
               ImageObject imgobj = raster;
               mask2 = CreateMask.createMask(imgobj,vectorfileIn,radius,cbxpara1.getSelectedIndex(),cbxpara2.getSelectedIndex(),include.isSelected(),sheet,header.isSelected());
               setImagePanel(mask2);
               tableMask = mask2;
               getCounts(mask2);
               isTableMaskOut = true;
               //isMaskOut = true;
               checkUI();
           }catch(Exception exc){
                   logger.error("Error loading CSV/Excel file.", exc);
               }

       }else if(type.equalsIgnoreCase("Shape")){

           try{
           if(isShapeLoaded){
           shapeMask = createmask(mask2,mask,include.isSelected(),isCV);
           setImagePanel(mask2);
           getCounts(mask2);
           //isMaskOut = true;
           isShapeMaskOut = true;
           checkUI();
           }
           }catch(Exception exc){
                   logger.error("Error loading vector file.", exc);
               }

       }
       else if(type.equalsIgnoreCase("Categorical")){

           try{
           createmask(mask2,mask,include.isSelected(),isCV);
           setImagePanel(mask2);
           getCounts(mask2);
           categoricalMask = mask2;
           isCategoricalMaskOut = true;
           //isMaskOut = true;
           checkUI();
           }catch(Exception exc){
                   logger.error("Error loading categorical image.", exc);
               }

       }
       else if(type.equalsIgnoreCase("Paint")){
    
       }
   }
   
   public void createTableMask(){

       try{
               if(proj.getScaleX()==proj.getScaleY()){
                   radius = Integer.parseInt(tfradius.getText());
               }else{
                   System.out.println("The image has different scales in X,Y directions.");
                   reset();
                   return;
               }
           sheet = Integer.parseInt(tfsheet.getText())-1;
           ImageObject imgobj = raster;
           mask2 = CreateMask.createMask(imgobj,vectorfileIn,radius,cbxpara1.getSelectedIndex(),cbxpara2.getSelectedIndex(),include.isSelected(),sheet,header.isSelected());
           setImagePanel(mask2);
           tableMask = mask2;
           getCounts(mask2);
           //isMaskOut = true;
           isTableMaskOut = true;
       }catch(Exception exc){
               logger.error("Error loading CSV/Excel file.", exc);
           }
   }
   
   public void saveImage(ImageObject imgobj){
       if(imgobj!= null){
           FileChooser fc = new FileChooser();
           fc.setTitle("Save Resulting Image");
           try {
               String filename = fc.showSaveDialog();
               if (filename != null) {
                   ImageLoader.writeImage(filename.concat(".tif"),imgobj);
               }
               checkUI();
           } catch (IOException exc) {
               logger.error("Error saving result.", exc);
           }
       }else{
           System.err.println("The image to save is null");
       }
   }
   
   private void checkUI() {
       if(cbxtype.getSelectedIndex() == 0){
           
           if(isShapeLoaded){
               btncreate_Shape.setEnabled(true);
           }else{
               btncreate_Shape.setEnabled(false);
           }
           if(isShapeMaskOut){
               btnapplyMask_Shape.setEnabled(true);
               btnsave_Shape.setEnabled(true);
               btnaddToList_Shape.setEnabled(true);
           }else{
               btnapplyMask_Shape.setEnabled(false);
               btnsave_Shape.setEnabled(false);
               btnaddToList_Shape.setEnabled(false);
           }
           if((boundaries.size() == 0 && !isShapeMaskOut)){
               btnreset_Shape.setEnabled(false);
           }else{
               btnreset_Shape.setEnabled(true);
           }
           
       }
       if(cbxtype.getSelectedIndex() == 1){

           if(isTableLoaded){
               btnshowTable_Table.setEnabled(true);
               btncreate_Table.setEnabled(true);
               tfradius.setEnabled(true);
               if(isXls){
                   tfsheet.setEnabled(true);
               }else{
                   tfsheet.setEnabled(false);
               }
           }else{
               btnshowTable_Table.setEnabled(false);
               btncreate_Table.setEnabled(false);
               tfradius.setEnabled(false);
               tfsheet.setEnabled(false);
           }
           if(isTableMaskOut){
               btnapplyMask_Table.setEnabled(true);
               btnsave_Table.setEnabled(true);
               btnaddToList_Table.setEnabled(true);
           }else{
               btnapplyMask_Table.setEnabled(false);
               btnsave_Table.setEnabled(false);
               btnaddToList_Table.setEnabled(false);
           }
           
       }
       if(cbxtype.getSelectedIndex() == 2){
           if(isCategoricalLoaded){
               btncreate_Categorical.setEnabled(true);
           }else{
               btncreate_Categorical.setEnabled(false);
           }
           if(isCategoricalMaskOut){
               btnapplyMask_Categorical.setEnabled(true);
               btnsave_Categorical.setEnabled(true);
               btnaddToList_Categorical.setEnabled(true);
           }else{
               btnapplyMask_Categorical.setEnabled(false);
               btnsave_Categorical.setEnabled(false);
               btnaddToList_Categorical.setEnabled(false);
           }
           if((boundaries.size() == 0 && !isCategoricalMaskOut)){
               btnreset_Categorical.setEnabled(false);
           }else{
               btnreset_Categorical.setEnabled(true);
           }
       }
       if(cbxtype.getSelectedIndex() == 4){
           if(isPaintMaskOut){
               btnapplyMask_Paint.setEnabled(true);
               btnsave_Paint.setEnabled(true);
               btnaddToList_Paint.setEnabled(true);
               btnreset_Paint.setEnabled(true);
           }else{
               btnapplyMask_Paint.setEnabled(false);
               btnsave_Paint.setEnabled(false);
               btnaddToList_Paint.setEnabled(false);
               btnreset_Paint.setEnabled(false);
           }
       }
   }
   
   private void getCounts(ImageObject imgobj){
       int whiteCounts = 0;
       int blackCounts = imgobj.getSize();
       for(int i=0;i<imgobj.getNumRows();i++){
           for(int j=0;j<imgobj.getNumCols();j++){
               if(imgobj.getInt(i,j,0)==255){
                   blackCounts--;
                   whiteCounts++;
                   }
           }
       }
       String s = " Invalid Pixels     =     " + blackCounts + "     (Color : black, Value : 0)" + "\n   Valid Pixels     =     " + whiteCounts +  "      (Color : white, Value : 255)" +"\n   Total Pixels     =     " + (whiteCounts+blackCounts);
       taCounts.setText(s);
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
	    JMenu geo = new JMenu("Geo");

	    JMenuItem createMask = new JMenuItem("Create Mask");
	    createMask.addActionListener(new AbstractAction() {
	      public void actionPerformed(ActionEvent ae) {
	    	  if (!isVisible()) {
                  Window win = SwingUtilities.getWindowAncestor(imagepanel);
                  setLocationRelativeTo(win);
                  setVisible(true);
              }
              toFront();
	      }
	    });

	    geo.add(createMask);
	    return new JMenuItem[] {geo};
	  }

   public void imageUpdated(ImageUpdateEvent event) {
	   if (isVisible() && event.getId() == ImageUpdateEvent.NEW_IMAGE) {
		   showing();
	   }
   }

   public URL getHelp(String menu) {
       return getClass().getResource("help/CreateMask.html");
   }	  
 
   public void showing() {
       raster = this.imagepanel.getImageObject();
       this.ip.setImageObject(raster);
       this.myThreshold.imagepanel.setImageObject(raster);	   
   }
}
