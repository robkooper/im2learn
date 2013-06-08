package edu.illinois.ncsa.isda.im2learn.ext.texture;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
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
import edu.illinois.ncsa.isda.im2learn.ext.conversion.SpFilters;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;


public class IsTextureDialog extends Im2LearnFrame implements ActionListener, KeyListener, Im2LearnMenu {
    private IsTexture    _isTexture;
    // private ImageFrame _imgFrame = null;
    protected ImagePanel imagepanel;
    private ImageObject  _oldImage           = null;

    public JPanel        pnl;
    public JPanel        pnlField;
    // public JPanel pnlBoxes;
    public JPanel        pnlSlider;
    public JPanel        pnlBtns;

    private JButton      _doBtn              = new JButton("AssignLabels");
    private JButton      _showBtn            = new JButton("Show");
    private JButton      _cancelBtn          = new JButton("Done");
    private JButton      _saveBtn            = new JButton("Save");

    private JLabel       _labelRow           = new JLabel("Kernel-Row");
    private JLabel       _labelCol           = new JLabel("Kernel-Col");
    private JLabel       _labelMin           = new JLabel("Thresh-Lower");
    private JLabel       _labelMax           = new JLabel("Thresh-Upper");
    private JLabel       _labelSlider        = new JLabel("Texture bands", JLabel.CENTER);
    private JLabel       _labelBands         = new JLabel("One                        Most                        All");

    public JPanel        pnlKernelRow        = new JPanel(new CardLayout());
    public JPanel        pnlKernelCol        = new JPanel(new CardLayout());
    public JPanel        pnlThreshValueLower = new JPanel(new CardLayout());
    public JPanel        pnlThreshValueUpper = new JPanel(new CardLayout());
    private JTextField   _fieldRow           = new JTextField("7", 4);
    // private FlyValidator _validatorRow = new FlyValidator();

    private JTextField   _fieldCol           = new JTextField("7", 4);
    // private FlyValidator _validatorCol = new FlyValidator();

    private JTextField   _fieldMin           = new JTextField("0.1", 4);
    private JTextField   _fieldMax           = new JTextField("0.2", 4);

    private JCheckBox    _densityBox         = new JCheckBox("Extrema Density", true);
    private JCheckBox    _maskBox            = new JCheckBox("Texture Mask", true);
    private JCheckBox    _segmentedBox       = new JCheckBox("Segmented Image", true);

    private JSlider      _bandSlider         = new JSlider(0, 2, 0);

    private ImageFrame   _densityFrame       = null;
    private ImageFrame   _maskFrame          = null;
    private ImageFrame   _segmentedFrame     = null;

    private ImageObject  _densityImage       = null;
    private ImageObject  _maskImage          = null;
    private ImageObject  _segmentedImage     = null;

    private int          _kernelRow          = 0;
    private int          _kernelCol          = 0;
    private int          _sliderPosition     = 0;
    private double       _lowerThreshold     = -1;
    private double       _upperThreshold     = -1;

    private String       _title              = "Is Texture";

    private boolean      _isModal            = false;                                                                   // true;

    private Dialog       myd                 = null;

    public IsTextureDialog() {
        super("IsTexture");
        /*
         * if (_imPanel != null) _oldImage = _imPanel.getImageObject();
         * 
         * if (_oldImage == null) { System.out.println("Error: no image to
         * process !"); return; } _oldImage = _imPanel.getImageObject();
         * _densityFrame = new ImageFrame("Extrema Density"); _maskFrame = new
         * ImageFrame("Texture Mask"); _segmentedFrame = new
         * ImageFrame("Segmented Input");
         * 
         */
        imagepanel = null;
        createUI();
        // showing();
    }

    public void createUI() {

        pnlKernelRow.add(_labelRow, "Kernel-Row");

        pnlKernelCol.add(_labelCol, "Kernel-Col");

        pnlThreshValueLower.add(_labelMin, "Lower Threshold");
        // pnlThreshValueLower.add(JLabelThreshValue,"Value(Point)");

        pnlThreshValueUpper.add(_labelMax, "Upper Threshold");
        // pnlThreshValueUpper.add(JLabelThreshValue1,"Value(VectNormal)");

        pnlSlider = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(4, 4, 4, 4);
        c.gridx = 0;
        c.gridy = 0;
        pnlSlider.add(pnlKernelRow, c);
        c.gridx = 1;
        c.gridy = 0;
        pnlSlider.add(pnlKernelCol, c);
        c.gridx = 0;
        c.gridy = 1;
        pnlSlider.add(_fieldRow, c);
        c.gridx = 1;
        c.gridy = 1;
        pnlSlider.add(_fieldCol, c);
        c.gridx = 0;
        c.gridy = 2;
        pnlSlider.add(pnlThreshValueLower, c);
        c.gridx = 1;
        c.gridy = 2;
        pnlSlider.add(pnlThreshValueUpper, c);
        c.gridx = 0;
        c.gridy = 3;
        pnlSlider.add(_fieldMin, c);
        c.gridx = 1;
        c.gridy = 3;
        pnlSlider.add(_fieldMax, c);
        c.gridx = 0;
        c.gridy = 4;
        pnlSlider.add(_densityBox, c);
        c.gridx = 1;
        c.gridy = 4;
        pnlSlider.add(_maskBox, c);
        c.gridx = 2;
        c.gridy = 4;
        pnlSlider.add(_segmentedBox, c);
        c.gridx = 1;
        c.gridy = 5;
        pnlSlider.add(_labelSlider, c);
        c.gridx = 1;
        c.gridy = 6;
        pnlSlider.add(_bandSlider, c);
        c.gridx = 1;
        c.gridy = 7;
        pnlSlider.add(_labelBands, c);

        // pnlField = new JPanel();
        // pnlField.setLayout(new GridLayout(1,1) );
        // pnlField.add(fieldThreshVector);

        pnl = new JPanel();
        BoxLayout boxLayoutPnl = new BoxLayout(pnl, BoxLayout.Y_AXIS);
        pnl.setLayout(boxLayoutPnl);
        // pnl.add(pnlType);
        pnl.add(pnlSlider);
        // pnl.add(pnlField);

        pnlBtns = new JPanel(new FlowLayout());
        pnlBtns.add(_doBtn);
        pnlBtns.add(_showBtn);
        pnlBtns.add(_cancelBtn);
        pnlBtns.add(_saveBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pnl, BorderLayout.CENTER);
        getContentPane().add(pnlBtns, BorderLayout.SOUTH);

        pack();

        // JLabelThreshBand.setVisible(false);
        // JLabelThreshBand1.setVisible(false);
        // cbxThreshBand.setVisible(false);
        // cbxThreshBand1.setVisible(false);
        // lblThreshMode.setVisible(true);
        // cbxThreshMode.setVisible(true);

        // fieldThreshValue1.setEnabled(false);
        // slider1.setEnabled(false);

        _densityBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                _densityBox.setEnabled(_densityBox.isSelected());
            }
        });

        _maskBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                _maskBox.setEnabled(_maskBox.isSelected());
            }
        });

        _segmentedBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                _segmentedBox.setEnabled(_segmentedBox.isSelected());
            }
        });

        _bandSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                JSlider src = (JSlider) ce.getSource();
                if (!src.getValueIsAdjusting()) {
                    _sliderPosition = _bandSlider.getValue();
                }
            }
        });

        _fieldRow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String s1 = _fieldRow.getText();
                int temp;
                try {
                    temp = Integer.parseInt(s1);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Enter a number ! ");
                    return;
                }
                _kernelRow = temp;
            }
        });

        _fieldCol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String s1 = _fieldCol.getText();
                int temp;
                try {
                    temp = Integer.parseInt(s1);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Enter a number ! ");
                    return;
                }
                _kernelCol = temp;
            }
        });

        _fieldMin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String s1 = _fieldMin.getText();
                double temp;
                try {
                    temp = Double.parseDouble(s1);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Enter a number ! ");
                    return;
                }
                _lowerThreshold = temp;
            }
        });

        _fieldMax.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String s1 = _fieldMax.getText();
                double temp;
                try {
                    temp = Double.parseDouble(s1);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Enter a number ! ");
                    return;
                }
                _upperThreshold = temp;
            }
        });

        _doBtn.setAction(new AbstractAction("Assign Labels") {
            public void actionPerformed(ActionEvent e) {
                try {
                    doActivated(e);
                } catch (ImageException exc) {
                    System.out.println("ImageException thrown!");
                    return;

                }
            }
        });

        _showBtn.setAction(new AbstractAction("Show") {
            public void actionPerformed(ActionEvent e) {
                try {
                    showActivated(e);
                } catch (ImageException exc) {
                    System.out.println("ImageException thrown!");
                    return;

                }
            }
        });

        _cancelBtn.setAction(new AbstractAction("Done") {
            public void actionPerformed(ActionEvent e) {
                cancelActivated(e);
            }
        });

        _saveBtn.setAction(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                saveActivated(e);
            }
        });

        pack();
        setSize(getPreferredSize());

        // forgeolearn:
        // geolearntaCounts.setEditable(false);
        // geoleanrpnlip.add(new JScrollPane(ip), BorderLayout.CENTER);
        // geoleanrpnlip.add(geolearntaCounts, BorderLayout.SOUTH);
    }

    /*
     * public IsTextureDialog(ImageFrame myFrame, ImageObject image) {
     * IsTextureDialog(myFrame, _title, _isModal); }
     * 
     * public IsTextureDialog(ImageFrame myFrame, ImageObject image, String
     * Title) { IsTextureDialog(myFrame, Title, _isModal); }
     * 
     * public void IsTextureDialog(ImageFrame myFrame, String title, boolean
     * isModal) { _imgFrame = myFrame; myd = new Dialog(myFrame, title,
     * isModal); JPanel labelPanel = new JPanel(); JPanel editPanel = new
     * JPanel(); JPanel checkBoxPanel = new JPanel(); JPanel buttonPanel = new
     * JPanel(); JPanel sliderPanel = new JPanel();
     * 
     * labelPanel.setLayout(new GridLayout(1, 4)); labelPanel.add(_labelRow);
     * labelPanel.add(_labelCol); labelPanel.add(_labelMin);
     * labelPanel.add(_labelMax);
     * 
     * editPanel.setLayout(new GridLayout(1, 4)); _fieldRow.setEditable(true);
     * editPanel.add(_fieldRow); _fieldCol.setEditable(true);
     * editPanel.add(_fieldCol); editPanel.add(_fieldMin);
     * editPanel.add(_fieldMax);
     * 
     * checkBoxPanel.setLayout(new GridLayout(1, 3));
     * checkBoxPanel.add(_densityBox); checkBoxPanel.add(_maskBox);
     * checkBoxPanel.add(_segmentedBox);
     * 
     * buttonPanel.add(_doBtn); buttonPanel.add(_showBtn);
     * buttonPanel.add(_saveBtn); buttonPanel.add(_cancelBtn);
     * 
     * sliderPanel.setLayout(new GridLayout(2, 1));
     * sliderPanel.add(_labelSlider); sliderPanel.add(_bandSlider); // Turn on
     * labels at major tick marks. _bandSlider.setMajorTickSpacing(1);
     * _bandSlider.setMinorTickSpacing(1); _bandSlider.setPaintTicks(false);
     * _bandSlider.setPaintLabels(true); // Create the label table
     * 
     * Hashtable labelTable = new Hashtable(); labelTable.put(new Integer(0),
     * new JLabel("One")); labelTable.put(new Integer(1), new JLabel("Most"));
     * labelTable.put(new Integer(2), new JLabel("All"));
     * _bandSlider.setLabelTable(labelTable);
     * 
     * myd.setLayout(new GridLayout(5, 1)); myd.add(labelPanel);
     * myd.add(editPanel); myd.add(checkBoxPanel); myd.add(sliderPanel);
     * myd.add(buttonPanel); // tab key implementation
     * _fieldRow.setNextFocusableComponent(_fieldCol);
     * _fieldCol.setNextFocusableComponent(_fieldMin);
     * _fieldMin.setNextFocusableComponent(_fieldMax);
     * _fieldMax.setNextFocusableComponent(_fieldRow);
     * 
     * _labelRow.setToolTipText("Number of rows in filter kernel");
     * _labelCol.setToolTipText("Number of columns in fitler kerne");
     * _labelMin.setToolTipText("Threshold lower bound for extrema density
     * (values 0 to 1)"); _labelMax.setToolTipText("Threshold upper bound for
     * extrema density (values 0 to 1)");
     * 
     * _fieldRow.setToolTipText("Number of rows in filter kernel");
     * _fieldCol.setToolTipText("Number of columns in fitler kernel");
     * _fieldMin.setToolTipText("Threshold lower bound for extrema density
     * (values 0 to 1)"); _fieldMax.setToolTipText("Threshold upper bound for
     * extrema density (values 0 to 1)");
     * 
     * _densityBox.setToolTipText("Pressing Show button will show extrema
     * density image"); _maskBox.setToolTipText("Pressing Show button will show
     * texture mask image"); _segmentedBox.setToolTipText("Pressing Show button
     * will show the input image overlayed with the texture mask");
     * 
     * _labelSlider.setToolTipText("Select how many bands in a pixel must be
     * texture bands in order for the whole pixel to be considered as texture
     * pixel"); _bandSlider.setToolTipText("Select how many bands in a pixel
     * must be texture bands in order for the whole pixel to be considered as
     * texture pixel");
     * 
     * _doBtn.setToolTipText("Performs computations on input image");
     * _showBtn.setToolTipText("Shows any images selected in the check boxes
     * above"); _saveBtn.setToolTipText("Lets you save the 3 created images (You
     * must press Do or Show first)"); _cancelBtn.setToolTipText("Cancels this
     * action and closes this dialog box along with any displayed images");
     * 
     * _doBtn.addActionListener(this); _showBtn.addActionListener(this);
     * _saveBtn.addActionListener(this); _cancelBtn.addActionListener(this);
     * 
     * myd.addWindowListener(new IsTextureDialogWindowListener());
     * 
     * pack();
     * 
     * _saveBtn.setEnabled(false);
     * 
     * myd.setBounds(200, 200, 400, 300); myd.show(); }
     */
    public void showing() {

        if (imagepanel.getImageObject() == null) {
            System.err.println("Error: no image to process !");
            return;
        } else {
            _oldImage = imagepanel.getImageObject();

            _densityFrame = new ImageFrame("Extrema Density");
            _maskFrame = new ImageFrame("Texture Mask");
            _segmentedFrame = new ImageFrame("Segmented Input");
        }
        imagepanel.setAutozoom(true);
        // ip.setAutozoom(true);
    }

    public void actionPerformed(ActionEvent event) {
        // sanity check
        if (event.getSource().getClass().toString().equalsIgnoreCase("class javax.swing.JButton")) {
            JButton btn = (JButton) event.getSource();
            if (btn == _doBtn) {
                try {
                    doActivated(event);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            } else if (btn == _showBtn) {
                try {
                    showActivated(event);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            } else if (btn == _cancelBtn) {
                cancelActivated(event);
            } else {
                if (btn == _saveBtn)
                    saveActivated(event);
            }
        }
    }

    public void doActivated(ActionEvent event) throws ImageException {
        ImageObject image = imagepanel.getImageObject();
        if (_isTexture == null)
            _isTexture = new IsTexture();
        // ImageObject image2 = imgConvert(image);

        int kernelR = Integer.parseInt(_fieldRow.getText());
        int kernelC = Integer.parseInt(_fieldCol.getText());
        double lowerThresh = Double.parseDouble(_fieldMin.getText());
        double upperThresh = Double.parseDouble(_fieldMax.getText());
        int sliderPos = _bandSlider.getValue();

        _isTexture.setKernelHeight(kernelR);
        _isTexture.setKernelWidth(kernelC);
        _isTexture.setLowerThreshold(lowerThresh);
        _isTexture.setUpperThreshold(upperThresh);
        _isTexture.setTextureBandCountMin(sliderPos * (image.getNumBands() - 1) / 2);

        _segmentedImage = null;
        _densityImage = null;
        _maskImage = null;

        _densityImage = densityImage(image);
        _maskImage = maskImage(_densityImage);
        // go over input data image and mask out non-texture pixels
        _segmentedImage = _isTexture.suppressNontexture(image, _maskImage);

        if (_segmentedImage == null) {
            System.out.println("ERROR: detect Texture failed");
            return;
        }

        _kernelRow = kernelR;
        _kernelCol = kernelC;
        _lowerThreshold = lowerThresh;
        _upperThreshold = upperThresh;
        _sliderPosition = sliderPos;

        ImageObject temp;

        if (_densityBox.isSelected() && _densityImage != null) {
            Point p = imagepanel.getLocation();
            p.translate(imagepanel.getWidth(), 0);
            _densityFrame = refreshFrame(_densityFrame, p, _densityImage, "Extrema Density");
            _densityFrame.hide();
        }

        if (_maskBox.isSelected() && _maskImage != null) {
            temp = ImageObject.createImage(_maskImage.getNumRows(), _maskImage.getNumCols(), _maskImage.getNumBands(), 0);
            for (int i = 0; i < _maskImage.getSize(); i++)
                temp.set(i, 255 * _maskImage.getDouble(i));
            ImageObject textureMaskByte = temp.convert(0, false);
            Point p = imagepanel.getLocation();
            p.translate(imagepanel.getWidth(), 20);
            _maskFrame = refreshFrame(_maskFrame, p, textureMaskByte, "Texture Mask");
            _maskFrame.hide();
        }

        if (_segmentedBox.isSelected() && _segmentedImage != null) {
            Point p = imagepanel.getLocation();
            p.translate(imagepanel.getWidth(), 40);
            _segmentedFrame = refreshFrame(_segmentedFrame, p, _segmentedImage, "Segmented Input");
            _segmentedFrame.hide();
        }

        _saveBtn.setEnabled(true);
    }

    public void showActivated(ActionEvent event) throws ImageException {
        ImageObject image = imagepanel.getImageObject();
        ImageObject temp;
        // ImageObject image2 = imgConvert(image);

        /*
         * SelectTools st = new SelectTools(); st.EnableAllTools(); image =
         * smoothImage(image); Point loc = new Point(30, 30); ImageFrame myFrame =
         * new ImageFrame(image, st, loc, "smoothed");
         */

        int kernelR = Integer.parseInt(_fieldRow.getText());
        int kernelC = Integer.parseInt(_fieldCol.getText());
        double lowerThresh = Double.parseDouble(_fieldMin.getText());
        double upperThresh = Double.parseDouble(_fieldMax.getText());
        int sliderPos = _bandSlider.getValue();

        if (_isTexture == null)
            _isTexture = new IsTexture();

        _isTexture.setKernelHeight(kernelR);
        _isTexture.setKernelWidth(kernelC);
        _isTexture.setLowerThreshold(lowerThresh);
        _isTexture.setUpperThreshold(upperThresh);
        _isTexture.setTextureBandCountMin(sliderPos * (image.getNumBands() - 1) / 2);

        if (imagepanel.getImageObject() != image || kernelR != _kernelRow || kernelC != _kernelCol) {
            _densityImage = densityImage(image);
            _maskImage = maskImage(_densityImage);
            // go over input data image and mask out non-texture pixels
            _segmentedImage = _isTexture.suppressNontexture(image, _maskImage);
        } else if (upperThresh != _upperThreshold || lowerThresh != _lowerThreshold || sliderPos != _sliderPosition) {
            _maskImage = maskImage(_densityImage);
            // go over input data image and mask out non-texture pixels
            _segmentedImage = _isTexture.suppressNontexture(image, _maskImage);
        }

        _kernelRow = kernelR;
        _kernelCol = kernelC;
        _lowerThreshold = lowerThresh;
        _upperThreshold = upperThresh;
        _sliderPosition = sliderPos;
        _oldImage = image;

        if (_densityBox.isSelected() && _densityImage != null) {
            Point p = imagepanel.getLocation();
            p.translate(imagepanel.getWidth(), 0);
            _densityFrame = refreshFrame(_densityFrame, p, _densityImage, "Extrema Density");
            _densityFrame.show();
        }

        if (_maskBox.isSelected() && _maskImage != null) {
            temp = ImageObject.createImage(_maskImage.getNumRows(), _maskImage.getNumCols(), _maskImage.getNumBands(), "DOUBLE");
            // multiply pixel values by 255 to make image more visible (binary
            // image of 0 and 255 instead of 0 and 1)
            for (int i = 0; i < _maskImage.getSize(); i++)
                temp.set(i, 255 * _maskImage.getDouble(i));
            ImageObject textureMaskByte = temp.convert(0, false);

            Point p = imagepanel.getLocation();
            p.translate(imagepanel.getWidth(), 20);
            _maskFrame = refreshFrame(_maskFrame, p, textureMaskByte, "Texture Mask");
            _maskFrame.show();
        }

        if (_segmentedBox.isSelected() && _segmentedImage != null) {
            Point p = imagepanel.getLocation();
            p.translate(imagepanel.getWidth(), 40);
            _segmentedFrame = refreshFrame(_segmentedFrame, p, _segmentedImage, "Segmented Input");
            _segmentedFrame.show();
        }

        _saveBtn.setEnabled(true);
    }

    public void saveActivated(ActionEvent event) {
        FileChooser fc;

        if (_densityFrame != null) {
            _densityFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            fc = new FileChooser();
            fc.setTitle("Save Resulting Image");
            try {
                String filename = fc.showSaveDialog();
                if (filename != null) {
                    ImageLoader.writeImage(filename, _densityFrame.getImageObject());
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }

        if (_maskFrame != null) {
            _maskFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            fc = new FileChooser();
            fc.setTitle("Save Resulting Image");
            try {
                String filename = fc.showSaveDialog();
                if (filename != null) {
                    ImageLoader.writeImage(filename, _maskFrame.getImageObject());
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }

        if (_segmentedFrame != null) {
            _segmentedFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            fc = new FileChooser();
            fc.setTitle("Save Resulting Image");
            try {
                String filename = fc.showSaveDialog();
                if (filename != null) {
                    ImageLoader.writeImage(filename, _densityFrame.getImageObject());
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    public void cancelActivated(ActionEvent event) {
        if (_densityFrame != null)
            _densityFrame.dispose();
        if (_maskFrame != null)
            _maskFrame.dispose();
        if (_segmentedFrame != null)
            _segmentedFrame.dispose();
        // myd.dispose();
        setVisible(false);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {

            // updateEvent(false);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    private ImageFrame refreshFrame(ImageFrame frame, Point location, ImageObject image, String title) {
        try {
            if (frame == null) {
                frame = new ImageFrame(title);
                frame.setImageObject(image);
                // frame.mbar.setVisible(false);
                frame.getImagePanel().setPreferredSize(new Dimension(frame.getImagePanel().getWidth(), frame.getImagePanel().getHeight()));
            } else {
                ImagePanel imageObjPanel = frame.getImagePanel();
                imageObjPanel.setImageObject(image);
                imageObjPanel.setSize(new Dimension(imageObjPanel.getWidth(), imageObjPanel.getHeight()));
                // frame.updateImage(image, "description", null);
            }
        } catch (Exception e) {
            return null;
        }
        return frame;
    }

    private ImageObject densityImage(ImageObject inImage) throws ImageException {
        ImageObject temp = _isTexture.scanImage(inImage);
        // detect pixels that are min-min or max-max
        _isTexture.detectExtrema(temp);
        // go through and find extrema pixel counts
        return _isTexture.LowPassOut(temp);
    }

    private ImageObject maskImage(ImageObject inImage) throws ImageException {
        ImageObject temp = inImage;
        // threshold the image
        temp = _isTexture.thresholdDensityImage(temp);
        return _isTexture.toOneBandTextureMask(temp);
    }

    private ImageObject smoothImage(ImageObject inImage) {
        SpFilters filter = new SpFilters();
        filter.setKernel(5, 5);
        filter.setImage(inImage);
        if (filter.filter(5))
            return filter.getResult();
        else
            return null;
    }

    // converts RGB image to grayscale image
    private ImageObject imgConvert(ImageObject inImage) throws ImageException {
        if (inImage.getNumBands() != 3) {
            System.out.println("ERROR: Image must have 3 bands");
            return null;
        }

        ImageObject outImage = ImageObject.createImage(inImage.getNumRows(), inImage.getNumCols(), 1, "BYTE");
        double k;
        int a1, a2, a3;
        for (int i = 0; i < inImage.getNumRows() * inImage.getNumCols(); i++) {
            a1 = inImage.getInt(3 * i) & 0xff;
            a2 = inImage.getInt(3 * i + 1) & 0xff;
            a3 = inImage.getInt(3 * i + 2) & 0xff;
            // outImage.image[i] = (byte)Math.sqrt(a1*a1 + a2*a2 + a3*a3);
            outImage.set(i, (byte) (0.3 * a1 + 0.59 * a2 + 0.11 * a3));
        }
        return outImage;
    }

    private ImageObject normalizeImage(ImageObject inImage) throws ImageException {
        double min = 1e4;
        double max = 0;

        for (int i = 0; i < inImage.getSize(); i++) {
            if (inImage.getDouble(i) < min)
                min = inImage.getDouble(i);
            if (inImage.getDouble(i) > max)
                max = inImage.getDouble(i);
        }

        double range = max - min;
        ImageObject outImage = ImageObject.createImage(inImage.getNumRows(), inImage.getNumCols(), inImage.getNumBands(), "BYTE");
        for (int i = 0; i < outImage.getSize(); i++)
            outImage.set(i, (byte) ((inImage.getDouble(i) - min) * 255 / range));

        return outImage;
    }

    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu tools = new JMenu("Tools");
        JMenu tex = new JMenu("Texture");
        JMenuItem texture = new JMenuItem(new AbstractAction("IsTexture") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                }
                toFront();
            }
        });
        tex.add(texture);
        tools.add(tex);
        return new JMenuItem[] { tools };
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            setVisible(false);
        }
    }

    public URL getHelp(String menu) {

        return getClass().getResource("help/texture.htm");
    }

}

class IsTextureDialogWindowListener extends WindowAdapter {
    public void windowClosing(WindowEvent event) {
        Window window = (Window) event.getSource();
        window.dispose();
    }

}