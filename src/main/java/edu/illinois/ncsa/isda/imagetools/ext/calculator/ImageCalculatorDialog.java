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
/*
 * Created on Jun 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.illinois.ncsa.isda.imagetools.ext.calculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.SimpleFilter;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileException;
import edu.illinois.ncsa.isda.imagetools.ext.geo.MaskListModel;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.imagetools.ext.misc.PlotComponent;
import edu.illinois.ncsa.isda.imagetools.ext.segment.ColorModels;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.Histogram;



/**
 * 
 * <p>
 * The class ImCalculator provides a tool for performing algebraic and boolean
 * operations with images. The algebraic operations are image addition,
 * subtraction, inversion, multiplication, division and average. The boolean
 * operations include AND, OR and XOR.
 * </p>
 * 
 * <img src="help/calc2.jpg">
 * <p>
 * <B>Description:</B> The methods supported by ImageCalculator class operate on
 * images at the level of each image pixel. For example, two color images with
 * red, green and blue bands will be added by summing spatially corresponding
 * pixel values in each band. While the number of bands and data type must be
 * consistent in both input images, the image size can vary. The result image
 * will have the spatial dimension based on the minimum of input image
 * dimensions. If the result of any algebraic operation leads to a value that is
 * outside the bounds of the input data type, then all resulting values are
 * scaled so that the output image contains data of the same data type as the
 * input images, unless they are not of the same type. In this case it will
 * create the output of the type of the image that is bigger, with double being
 * the biggest and byte being the smallest.
 * </p>
 * <p>
 * The ImageCalculator performs the following algebraic and boolean operations:
 * </p>
 * <p>
 * Addition
 * </p>
 * <p>
 * Subtraction
 * </p>
 * <p>
 * Inversion
 * </p>
 * <p>
 * Multiplication
 * </p>
 * <p>
 * Division
 * </p>
 * <p>
 * Average
 * </p>
 * <p>
 * Boolean AND
 * </p>
 * <p>
 * Boolean OR
 * </p>
 * <p>
 * Boolean XOR
 * </p>
 * <p>
 * 
 * 
 * <B> Setup:</B> In order to execute any operation, two images have to be
 * loaded and thus set as the two operands for the chosen calculation. The image
 * loaded into the frame, by using "LoadIntoFrame", is the first operand
 * (obviously this only matters for Subtract, Divide and Inverse). Thus the
 * FrameImage is the image which the operation is performed on. Loading an image
 * using "Load Image" will make it the second operand, or as I refer to it the
 * ActionImage, which seemed logical because it is the one performing the
 * operation. Therefore if you Subtract/Divide you will Sub/Div the ActionImage
 * from the FrameImage. Inversion is performed on the FrameImage.
 * </p>
 * <p>
 * 
 * <B>Run:</B> In order to run the tool two images will have to be loaded. A
 * FrameImage ("LoadIntoFrame") and a ActionImage ("Load Image"). Following the
 * desired operation shoud be chosen from the dropdown menu (Add, Subtract, etc)
 * and by pressing the "Execute" Button the calculation will take place and
 * deliver a result which will pop up in a new frame. If one should close the
 * ResultFrame or the ActionImage frame there are buttons two buttons
 * ("Show Image", "Show Result") which will make the image in question appear in
 * a new frame. The ResultImage can be put into the mainframe by utilizing
 * "ResultIntoFrame" and thus it will become the FrameImage which means that
 * operations are now performed on that image.
 * </p>
 * <p>
 * The summary of the additional buttons is provided next:
 * </p>
 * <p>
 * Load Image - Loads the second operand and shows the image.
 * </p>
 * <p>
 * LoadIntoFrame - Loads the first operand and displays it in the mainframe.
 * </p>
 * <p>
 * Load Many - Loads more than two images, they are not displayed, only
 * "Average Many" can be performed.
 * </p>
 * <p>
 * Execute - The chosen action from the ComboBox will be performed with the
 * images.
 * </p>
 * <p>
 * Show Result - displays the result image in a new frame
 * </p>
 * <p>
 * Save Result - saves the result image.
 * </p>
 * <p>
 * 
 * <B> Example ADD:</B>
 * </p>
 * <img src="help/image1.jpg"></br>
 * 
 * <img src="help/image2.jpg"></br>
 * 
 * <img src="help/result.jpg">
 * <p>
 * <B>
 * 
 * @author Peter Ferak and Peter Bajcsy
 * 
 */
public class ImageCalculatorDialog extends Im2LearnFrame implements ActionListener, ListSelectionListener, Im2LearnMenu {

    public JComboBox              _comboAction        = new JComboBox(new String[] { "Add", "Subtract", "Average", "Multiply", "Divide", "OR", "XOR", "AND", "Inverse", "ApplyMask" });
    private final JButton         _cboxExecute        = new JButton("Execute");
    public JComboBox			  _colorModels		  = new JComboBox(ColorModels.getColorModelList());
    public String				  cSpace			  = "RGB";

    private final JButton         _cboxExchangeActRes = new JButton("ResultToFrame");
    private final JButton         _cboxSaveResult     = new JButton("Save Result");
    public JButton                btnSave             = new JButton("Save");
    public JButton                _cboxDone           = new JButton("Done");

    private ImageObject           _action             = null;
    private ImageFrame            frmImage            = null;
    private ImageObject           _result             = null;
    private ImageFrame            frmResult           = null;
    private ImageObject           _target             = null;

    public MaskListModel          _imageListModel     = new MaskListModel();
    public JList                  _imageList          = new JList(_imageListModel);
    private final JPanel          _pnlImageList       = new JPanel(new BorderLayout());
    public JButton                _cboxLoad           = new JButton("Load");
    private final JButton         _cboxRemove         = new JButton("Remove");
    private final JButton         _cboxSetTarget      = new JButton("Set Target");
    private final JButton         _cboxSetAction      = new JButton("Set Action");
    private final JButton         _cboxResultToList   = new JButton("ResultToList");
    private final JButton         _cboxRename         = new JButton("Rename");
    private final JButton		  _cboxHist			  = new JButton("Generate Histogram over Mask");
    public JTextField             _tfRename           = new JTextField();

    private boolean               targetLoaded        = false;
    private boolean               actionLoaded        = false;
    private boolean               isResultOut         = false;
    public boolean                plugin              = false;
    private final ImageCalculator imagecalculate      = new ImageCalculator();

    public JPanel                 pnl                 = new JPanel(new GridBagLayout());
    public JButton                btnMaskApply        = new JButton("Apply Mask");
    

    private ImagePanel            imagepanel;
    private static Log            logger              = LogFactory.getLog(ImageCalculatorDialog.class);

    public ImageCalculatorDialog() {
        super("Image Calculator");

        createUI();
        checkUI();
    }

    public ImageCalculatorDialog(boolean plugin) {
        if (plugin) {
            this.plugin = true;
            ImageCalculator imagecalculate = new ImageCalculator();
            // this.imagecalculate.invert = true;
            createUI();
            checkUI();
        }
    }

    @Override
    public void closing() {
        if (frmImage != null) {
            frmImage.setImageObject(null);
            frmImage.setVisible(false);
        }
        _action = null;

        if (frmResult != null) {
            frmResult.setImageObject(null);
            frmResult.setVisible(false);
            frmResult = null;
        }
        _result = null;
    }

    public void createUI() {
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints gbConstraints = new GridBagConstraints();

        // getContentPane().setLayout();

        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.weightx = 1.0;
        gbConstraints.weighty = 1.0;
        gbConstraints.insets = new Insets(2, 2, 2, 2);

        _pnlImageList.add(new JScrollPane(_imageList), BorderLayout.CENTER);
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        gbConstraints.gridwidth = 2;
        gbConstraints.gridheight = 5;
        pnl.add(_pnlImageList, gbConstraints);
        _imageList.addListSelectionListener(this);

        gbConstraints.gridx = 0;
        gbConstraints.gridy = 5;
        gbConstraints.weighty = 0.0;
        gbConstraints.gridwidth = 1;
        gbConstraints.gridheight = 1;
        pnl.add(_cboxLoad, gbConstraints);
        _cboxLoad.addActionListener(this);

        gbConstraints.gridx = 1;
        pnl.add(_cboxRemove, gbConstraints);
        _cboxRemove.addActionListener(this);

        gbConstraints.gridx = 0;
        gbConstraints.gridy++;
        pnl.add(_tfRename, gbConstraints);

        gbConstraints.gridx = 1;
        pnl.add(_cboxRename, gbConstraints);
        _cboxRename.addActionListener(this);

        gbConstraints.gridx = 0;
        gbConstraints.gridy++;
        pnl.add(_cboxSetTarget, gbConstraints);
        _cboxSetTarget.addActionListener(this);
        _cboxSetTarget.setVisible(false);

        gbConstraints.gridx = 1;
        pnl.add(_cboxSetAction, gbConstraints);
        _cboxSetAction.addActionListener(this);
        _cboxSetAction.setVisible(false);

        gbConstraints.gridx = 0;
        gbConstraints.gridy++;
        pnl.add(_comboAction, gbConstraints);
        _comboAction.addActionListener(this);

        gbConstraints.gridx = 1;
        pnl.add(_cboxExecute, gbConstraints);
        _cboxExecute.addActionListener(this);

        gbConstraints.gridx = 0;
        gbConstraints.gridy++;
        pnl.add(_cboxResultToList, gbConstraints);
        _cboxResultToList.addActionListener(this);
        
        gbConstraints.gridx = 1;
        if (plugin) {
            pnl.add(btnMaskApply, gbConstraints);
        } else {
            pnl.add(_cboxExchangeActRes, gbConstraints);
            _cboxExchangeActRes.addActionListener(this);
        }

        gbConstraints.gridx = 0;
        gbConstraints.gridy++;
        if (plugin) {
            pnl.add(btnSave, gbConstraints);
        } else {
            pnl.add(_cboxSaveResult, gbConstraints);
            _cboxSaveResult.addActionListener(this);
        }

        gbConstraints.gridx = 1;
        pnl.add(_cboxDone, gbConstraints);
        _cboxDone.addActionListener(this);
        
        gbConstraints.gridx = 0;
        gbConstraints.gridy++;
        pnl.add(_colorModels, gbConstraints);
        _colorModels.addActionListener(this);
        
        gbConstraints.gridx = 0;
        gbConstraints.gridy++;
        pnl.add(_cboxHist, gbConstraints);
        _cboxHist.addActionListener(this);

        getContentPane().add(pnl, BorderLayout.CENTER);
        pack();
        frmImage = new ImageFrame("Image");
    }

    public void actionPerformed(ActionEvent e) {
        Object cbox = e.getSource();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (cbox.equals(_cboxExecute)) {
            executeAction();
        } else if (cbox.equals(_comboAction)) {
            actionType();
        } else if (cbox.equals(_cboxLoad)) {
            load();
        } else if (cbox.equals(_cboxRemove)) {
            remove();
        } else if (cbox.equals(_cboxRename)) {
            rename();
        } else if (cbox.equals(_cboxSetTarget)) {
            setTarget();
        } else if (cbox.equals(_cboxSetAction)) {
            setAction();
        } else if (cbox.equals(_cboxResultToList)) {
            resultToList();
        } else if (cbox.equals(_cboxExchangeActRes)) {
            exchangeActRes();
        } else if (cbox.equals(_cboxSaveResult)) {
            saveResult();
        } else if (cbox.equals(_cboxDone)) {
            setVisible(false);
        } else if (cbox.equals(_cboxHist)) {
        	int[] operands = _imageList.getSelectedIndices();
        	ImageObject img = _imageListModel.getMask(operands[0]);
            ImageObject img2 = null;
            if(!cSpace.equals("RGB")){
                ColorModels converter = new ColorModels("RGB",cSpace);
                int[] arr = {0, 1, 2};
                if(img.getNumBands() >= 3){
	                try {
						converter.convert(img.extractBand(arr));
						img2 = converter.getConvertedIm();
					} catch (ImageException e1) {
						e1.printStackTrace();
					}
        		}
            } else {
            	img2 = img;
            }
            ImageObject mask = _imageListModel.getMask(operands[1]);
        	imagecalculate.plotHistMask(img2,mask);
        } else if (cbox.equals(_colorModels)) {
        	String newSpace = (String)_colorModels.getSelectedItem();
        	cSpace = newSpace;
        }

        setCursor(Cursor.getDefaultCursor());
    }


    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        if (_imageList.getSelectedIndex() == -1) {
            frmImage.setImageObject(null);
            frmImage.setVisible(false);
        } else {
            frmImage.setImageObject(_imageListModel.getMask(_imageList.getSelectedIndex()));
            frmImage.setVisible(true);
        }
        checkUI();
    }

    private void actionType() {
        if ((_comboAction.getSelectedItem() == "Subtract") || (_comboAction.getSelectedItem() == "Multiply") || (_comboAction.getSelectedItem() == "Divide")
                || (_comboAction.getSelectedItem() == "XOR")) {

            targetLoaded = false;
            actionLoaded = false;
            _target = null;
            _action = null;
            _cboxSetTarget.setVisible(true);
            _cboxSetTarget.setEnabled(true);
            _cboxSetAction.setVisible(true);
            _cboxSetAction.setEnabled(true);
        } else {
            _cboxSetTarget.setVisible(false);
            _cboxSetAction.setVisible(false);
        }
        checkUI();
    }

    private void executeAction() {

        // add
        if (_comboAction.getSelectedItem().equals("Add")) {

            try {
                int[] toAdd = _imageList.getSelectedIndices();
                ImageObject[] images = new ImageObject[toAdd.length];
                for (int i = 0; i < toAdd.length; i++) {
                    images[i] = _imageListModel.getMask(toAdd[i]);
                }
                _result = imagecalculate.add(images);
            } catch (ImageException e) {
                logger.error("ERROR loading images.");
            }

            // subtract
        } else if (_comboAction.getSelectedItem().equals("Subtract")) {

            try {
                _result = imagecalculate.subtract(_target, _action);
            } catch (ImageException e) {
                e.printStackTrace();
            }

            // average
        } else if (_comboAction.getSelectedItem().equals("Average")) {

            try {
                int[] toAverage = _imageList.getSelectedIndices();
                ImageObject[] images = new ImageObject[toAverage.length];
                for (int i = 0; i < toAverage.length; i++) {
                    images[i] = _imageListModel.getMask(toAverage[i]);
                }
                _result = imagecalculate.average(images);
            } catch (ImageException e) {
                logger.error("ERROR loading images.");
            }

            // multiply
        } else if (_comboAction.getSelectedItem().equals("Multiply")) {

            try {
                _result = imagecalculate.multiply(_target, _action);
            } catch (ImageException e) {

                e.printStackTrace();
            }

            // divide
        } else if (_comboAction.getSelectedItem().equals("Divide")) {

            try {
                _result = imagecalculate.divide(_target, _action);
            } catch (ImageException e) {

                e.printStackTrace();
            }

            // or
        } else if (_comboAction.getSelectedItem().equals("OR")) {

            try {
                int[] toAdd = _imageList.getSelectedIndices();
                ImageObject[] images = new ImageObject[toAdd.length];
                for (int i = 0; i < toAdd.length; i++) {
                    images[i] = _imageListModel.getMask(toAdd[i]);
                }
                _result = imagecalculate.or(images);
            } catch (ImageException e) {
                logger.error("ERROR loading images.");
            }

            // xor
        } else if (_comboAction.getSelectedItem().equals("XOR")) {

            try {
                _result = imagecalculate.xor(_target, _action);
            } catch (ImageException e) {

                e.printStackTrace();
            }

            // and
        } else if (_comboAction.getSelectedItem().equals("AND")) {

            try {
                int[] toAdd = _imageList.getSelectedIndices();
                ImageObject[] images = new ImageObject[toAdd.length];
                for (int i = 0; i < toAdd.length; i++) {
                    images[i] = _imageListModel.getMask(toAdd[i]);
                }
                _result = imagecalculate.and(images);
            } catch (ImageException e) {
                logger.error("ERROR loading images.");
            }

            // inverse
        } else if (_comboAction.getSelectedItem().equals("Inverse")) {

            ImageObject image = _imageListModel.getMask(_imageList.getSelectedIndex());
            try {
                _result = imagecalculate.invert(image);
            } catch (ImageException e) {
                e.printStackTrace();
            }
            // applyMask
        } else if (_comboAction.getSelectedItem().equals("ApplyMask")){
            try {
            	int[] operands = _imageList.getSelectedIndices();
                ImageObject img = _imageListModel.getMask(operands[0]);
                ImageObject img2 = null;
                if(!cSpace.equals("RGB")){
	                ColorModels converter = new ColorModels("RGB",cSpace);
	                int[] arr = {0,1,2};
	                if(img.getNumBands() >= 3){
		                try {
							converter.convert(img.extractBand(arr));
							img2 = converter.getConvertedIm();
						} catch (ImageException e1) {
							e1.printStackTrace();
						}
	                }
                } else {
                	img2 = img;
                }
                ImageObject mask = _imageListModel.getMask(operands[1]);
                int numBands = img2.getNumBands();
                double[] statistics = new double[numBands*2];
                _result = imagecalculate.applyMask(img2, mask, statistics);
                // Display Statistics
                JFrame frame = new JFrame("Statistics");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                Container content = frame.getContentPane();
                content.setBackground(Color.white);
                JLabel label = new JLabel("Average");
                label.setVerticalAlignment(JLabel.TOP);
                String aAverage = Double.toString(statistics[0]);
                String bAverage = Double.toString(statistics[1]);
                String cAverage = Double.toString(statistics[2]);
                String aStd = Double.toString(statistics[numBands]);
                String bStd = Double.toString(statistics[numBands+1]);
                String cStd = Double.toString(statistics[numBands+2]);
                label.setText("<html><b>Averages:</b><br>"+"A: "+aAverage+"<br>B: "+bAverage+"<br>C: "+cAverage
                		+"<br><br><b>Standard Deviations:</b>"+"<br>A: "+aStd+"<br>B: "+bStd+"<br>C "+cStd+"</html>");
                final String text = "Averages:\n"+"A: "+aAverage+"\nB: "+bAverage+"\nC: "+cAverage
        		+"\n\nStandard Deviations:"+"\nA: "+aStd+"\nB: "+bStd+"\nC "+cStd+"";
                label.setVerticalTextPosition(JLabel.TOP);
                label.setHorizontalTextPosition(JLabel.LEFT);
                frame.getContentPane().add(label);
                frame.pack();
                frame.setSize(300, 200);
                frame.setVisible(true);
                JMenuBar menuBar = new JMenuBar();
                JMenu fileMenu = new JMenu("File");
                JMenuItem saveStatistics = new JMenuItem(new AbstractAction("Save"){
        			public void actionPerformed(ActionEvent e){
        		        JFileChooser fc = new JFileChooser(FileChooser.getInitialDirectory());
        		        fc.setDialogTitle("Save Masked Image Statistics");
        		        String[] extens = {"txt"};
        		        fc.setFileFilter(new SimpleFilter(extens, null));
        		        try {
        		            int result = fc.showSaveDialog(null);
        			        String filename = fc.getSelectedFile().getAbsolutePath();
        			        if(filename.lastIndexOf('.')!=-1){ // Force extension to be .txt
        			        	filename = filename.substring(0, filename.lastIndexOf('.'));
        			        }
        			        filename = filename + ".txt";
        		            if (filename != null) {
        						// Save the file
        		            	Writer writer = null;
        		                try
        		                {
        		                    File file = new File(filename);
        		                    writer = new BufferedWriter(new FileWriter(file));
        		                    writer.write(text);
        		                } catch (FileNotFoundException e1)
        		                {
        		                    e1.printStackTrace();
        		                } catch (IOException e1)
        		                {
        		                    e1.printStackTrace();
        		                } finally
        		                {
        		                    writer.close();
        		                }
      
        		            }
        				} catch (IOException e1) {
        					// TODO Auto-generated catch block
        					e1.printStackTrace();
        				}
        			}
        		});
                fileMenu.add(saveStatistics);
                menuBar.add(fileMenu);
                frame.setJMenuBar(menuBar);
            } catch (ImageException e) {
                logger.error("ERROR loading images.");
            }
        }

        // show the resultImage in a new frame
        if (_result == null) {
            if (frmResult != null) {
                frmResult.setImageObject(null);
                frmResult.setVisible(false);
                frmResult = null;
            }
        } else {
            if (frmResult == null) {
                frmResult = new ImageFrame("Result Image");
            }
            isResultOut = true;
            frmResult.setImageObject(_result);
            frmResult.setVisible(true);
        }
        checkUI();

    }

    private void remove() {
        int index = _imageList.getSelectedIndex();
        int[] toRemove = _imageList.getSelectedIndices();
        _imageListModel.removeMasks(toRemove);
        int size = _imageListModel.masks.size();
        if (size == 0) {
            _cboxRemove.setEnabled(false);
            frmImage.setVisible(false);
        } else {
            if (index == size) {
                index--;
            }
            _imageList.setSelectedIndex(index);
            _imageList.ensureIndexIsVisible(index);
        }
        checkUI();
    }

    private void rename() {
        String name = _tfRename.getText();
        if ((name != null) && (name.length() > 0)) {
            _imageListModel.rename(_imageList.getSelectedIndices(), name);
        } else {
            logger.error("On name is entered in textfield.");
        }
    }

    private void setTarget() {
        int[] selected = _imageList.getSelectedIndices();
        if (selected.length > 1) {
            logger.error("Multiple images are selected, but only one is allowed to load as target image.");
            return;
        } else {
            _target = _imageListModel.getMask(_imageList.getSelectedIndex());
            targetLoaded = true;
            checkUI();
        }
    }

    private void setAction() {
        int[] selected = _imageList.getSelectedIndices();
        if (selected.length > 1) {
            logger.error("Multiple images are selected, but only one is allowed to load as target image.");
            return;
        } else {
            _action = _imageListModel.getMask(_imageList.getSelectedIndex());
            actionLoaded = true;
            checkUI();
        }
    }

    private void resultToList() {
        if (_result != null) {
            _imageListModel.addMask(_result, "ResultingImage");
        } else {
            logger.error("This mask is null");
        }
    }

    /**
     * Triggers a Open File dialog. Loads the actionImage (second operand),
     * which is then displayed in a new frame.
     */
    private void load() {

        FileChooser fc = new FileChooser();
        fc.setTitle("Load Image:");
        try {
            String[] filenames = fc.showMultiOpenDialog();
            if (filenames != null) {
                _imageListModel.addMask(filenames);
                checkUI();
            }
        } catch (Exception exc) {
            System.err.println("Error loading mask");
        }
    }

    /**
     * Puts the result image into the MainFrame, thus the result image becomes
     * the frameImage (first operand).
     */
    private void exchangeActRes() {
        logger.info("The Result is transfered into MainFrame, thus is now the FrameImage");

        if (frmResult == null) {
            logger.error("No existent result image");
        } else {
            imagepanel.setImageObject(_result);
        }
    }
    
    /**
     * Triggers a Save Image dialog Saves the result image.
     */
    private void saveResult() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Resulting Image");
        try {
            String filename = fc.showSaveDialog();
            if (filename != null) {
                ImageLoader.writeImage(filename, _result);
            }
        } catch (IOException exc) {
            logger.error("Error saving result.", exc);
        }
    }

    public void checkUI() {
    	
    	_cboxHist.setEnabled(false);
    	_colorModels.setEnabled(false);
    	
        if ((_imageListModel.masks.size() > 0) && ((_imageList.getSelectedIndices().length) >= 1)) {
            _cboxRemove.setEnabled(true);
            _cboxRename.setEnabled(true);
            _comboAction.setEnabled(true);
            btnSave.setEnabled(true);
            btnMaskApply.setEnabled(true);

        } else {
            _cboxRemove.setEnabled(false);
            _cboxRename.setEnabled(false);
            _comboAction.setEnabled(false);
            btnSave.setEnabled(false);
            btnMaskApply.setEnabled(false);
        }

        if (targetLoaded) {
            _cboxSetTarget.setEnabled(false);
        } else {
            _cboxSetTarget.setEnabled(true);
        }

        if (actionLoaded) {
            _cboxSetAction.setEnabled(false);
        } else {
            _cboxSetAction.setEnabled(true);
        }

        if (isResultOut) {
            _cboxResultToList.setEnabled(true);
            _cboxExchangeActRes.setEnabled(true);
            _cboxSaveResult.setEnabled(true);
        } else {
            _cboxResultToList.setEnabled(false);
            _cboxExchangeActRes.setEnabled(false);
            _cboxSaveResult.setEnabled(false);
        }
        
        if (_comboAction.getSelectedItem() == "ApplyMask"){
        	_cboxHist.setEnabled(true);
        	_colorModels.setEnabled(true);
        }
        if ((_comboAction.getSelectedItem() == "Subtract") || (_comboAction.getSelectedItem() == "Multiply") || (_comboAction.getSelectedItem() == "Divide")
                || (_comboAction.getSelectedItem() == "XOR")) {

            if (targetLoaded && actionLoaded) {
                _cboxExecute.setEnabled(true);
            } else {
                _cboxExecute.setEnabled(false);
            }
        } else {
            if ((_imageListModel.masks.size() > 0) && ((_imageList.getSelectedIndices().length) >= 1)) {
                _cboxExecute.setEnabled(true);
            } else {
                _cboxExecute.setEnabled(false);
            }
        }
    }

    /**
     * @return the result image.
     */
    public ImageObject getResult() {
        return _result;
    }

    public void setProjection(Projection prj) {
        this._imageListModel.setProjection(prj);
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
        JMenu tools = new JMenu("Tools");

        JMenuItem calculator = new JMenuItem(new AbstractAction("Image Calculator") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                }
                toFront();
            }
        });
        tools.add(calculator);

        return new JMenuItem[] { tools };
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    public URL getHelp(String menu) {
        URL url = null;

        if (url == null) {
            String file = menu.toLowerCase().replaceAll("[\\s\\?\\*]", "") + ".html";
            url = this.getClass().getResource("help/" + file);
        }

        return url;
    }
}
