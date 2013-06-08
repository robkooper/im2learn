package edu.illinois.ncsa.isda.im2learn.ext.texture;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.math.FourierTransform;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.pdf.PDFAnnotationDialog;
import edu.illinois.ncsa.isda.im2learn.ext.texture.GaborFilterBank;


public class GaborFilterBankDialog extends Im2LearnFrame implements ActionListener, Im2LearnMenu {
    private GaborFilterBank _gfb                  = new GaborFilterBank();
    public ImagePanel       imagepanel;
    // private ImageFrame _imgFrame = null;
    private ImageFrame      _filterFrame          = null;
    private ImageFrame      _resultFrame          = null;
    private ImageFrame      _filterBankFrame      = null;
    private ImageFrame		_resultBankFrame	  = null;
    private double[]        _features             = null;
    private double[]        _prevAllValues        = new double[6];
    private double[]        _prevOneValues        = new double[8];

    private JButton         _showAllBtn           = new JButton("Show");
    private JButton         _calculateAllBtn      = new JButton("Calculate");
    private JButton         _cancelBtn            = new JButton("Cancel");
    private JButton         _showOneBtn           = new JButton("Show");
    private JButton         _calculateOneBtn      = new JButton("Calculate");
    private JButton         _saveBtn              = new JButton("Save");

    private JLabel          _labelNumFrequencies  = new JLabel("Frequencies");
    private JLabel          _labelNumOrientations = new JLabel("Orientations");
    private JLabel          _labelType            = new JLabel("Feature Type");

    private JLabel          _labelF               = new JLabel("Frequency");
    private JLabel          _labelOrientation     = new JLabel("Orientation");
    private JLabel          _labelOffset          = new JLabel("Angle Offset");

    private JTextField      _fieldNumFrequencies  = new JTextField("5", 4);
    // private FlyValidator _validatorNumFrequencies = new FlyValidator();

    private JTextField      _fieldNumOrientations = new JTextField("5", 4);
    // private FlyValidator _validatorNumOrientations = new FlyValidator();

    private JTextField      _fieldOffset          = new JTextField("0", 4);

    private JTextField      _fieldF               = new JTextField("4", 4);
    // private FlyValidator _validatorF = new FlyValidator();

    private JTextField      _fieldOrientation     = new JTextField("0", 4);
    // private FlyValidator _validatorOrientation = new FlyValidator();

    private JComboBox       _comboFeature         = new JComboBox();

    private String          _title                = "Gabor Filter";
    private boolean         _isModal              = false;                     // true;
    private static Log logger = LogFactory.getLog(GaborFilterBankDialog.class);
    // private Dialog _myd = null;

    // public GaborFilterBankDialog(ImageFrame myFrame) {
    // GaborFilterBankDialog(myFrame, _title, _isModal);
    // }

    // public GaborFilterBankDialog(ImageFrame myFrame, String title) {
    // GaborFilterBankDialog(myFrame, title, _isModal);
    // }

    public GaborFilterBankDialog() {
        super("Gabor Filter");
        
        /*
         * if (_imPanel != null) _oldImage = _imPanel.getImageObject();
         * 
         * if (_oldImage == null) { System.out.println("Error: no image to
         * process !"); return; } _oldImage = _imPanel.getImageObject();
         * _densityFrame = new ImageFrame("Extrema Density"); _maskFrame = new
         * ImageFrame("Texture Mask"); _segmentedFrame = new
         * ImageFrame("Segmented Input");
         */
        imagepanel = null;
        createUI();
        // showing();
    }

    /*
     * public void GaborFilterBankDialog(ImageFrame myFrame, String title,
     * boolean isModal) { _myd = new Dialog(myFrame, title, isModal); _imgFrame
     * = myFrame;
     * 
     * JPanel bigAllPanel = new JPanel(); JPanel bigOnePanel = new JPanel();
     * JPanel smallAllPanel = new JPanel(); JPanel smallOnePanel = new JPanel();
     * JPanel labelAllPanel = new JPanel(); JPanel labelOnePanel = new JPanel();
     * JPanel editAllPanel = new JPanel(); JPanel editOnePanel = new JPanel();
     * JPanel buttonAllPanel = new JPanel(); JPanel buttonOnePanel = new
     * JPanel(); JPanel buttonPanel = new JPanel();
     * 
     * TitledBorder borderAll = BorderFactory.createTitledBorder("Full filter
     * bank scan"); borderAll.setTitleJustification(TitledBorder.CENTER);
     * bigAllPanel.setBorder(borderAll);
     * 
     * TitledBorder borderOne = BorderFactory.createTitledBorder("Single filter
     * scan"); borderOne.setTitleJustification(TitledBorder.CENTER);
     * bigOnePanel.setBorder(borderOne);
     * 
     * labelAllPanel.setLayout(new GridLayout(1, 4));
     * labelAllPanel.add(_labelNumFrequencies);
     * labelAllPanel.add(_labelNumOrientations);
     * labelAllPanel.add(_labelOffset); labelAllPanel.add(_labelType);
     * 
     * _comboFeature.addItem("Gray"); _comboFeature.addItem("R");
     * _comboFeature.addItem("G"); _comboFeature.addItem("B");
     * _comboFeature.addItem("RGB"); _comboFeature.addItem("Opponent");
     * _comboFeature.addItem("HS Complex");
     * 
     * editAllPanel.setLayout(new GridLayout(1, 4));
     * _fieldNumFrequencies.addKeyListener(_validatorNumFrequencies);
     * editAllPanel.add(_fieldNumFrequencies);
     * _fieldNumOrientations.addKeyListener(_validatorNumOrientations);
     * editAllPanel.add(_fieldNumOrientations); editAllPanel.add(_fieldOffset);
     * editAllPanel.add(_comboFeature);
     * 
     * buttonAllPanel.setLayout(new GridLayout(1, 2));
     * buttonAllPanel.add(_calculateAllBtn); buttonAllPanel.add(_showAllBtn);
     * 
     * smallAllPanel.setLayout(new GridLayout(2, 1));
     * smallAllPanel.add(labelAllPanel); smallAllPanel.add(editAllPanel);
     * 
     * bigAllPanel.setLayout(new GridBagLayout());
     * bigAllPanel.add(smallAllPanel); bigAllPanel.add(buttonAllPanel);
     * 
     * labelOnePanel.setLayout(new GridLayout(1, 2));
     * labelOnePanel.add(_labelF); labelOnePanel.add(_labelOrientation);
     * 
     * editOnePanel.setLayout(new GridLayout(1, 2));
     * _fieldF.addKeyListener(_validatorF); editOnePanel.add(_fieldF);
     * _fieldOrientation.addKeyListener(_validatorOrientation);
     * editOnePanel.add(_fieldOrientation);
     * 
     * buttonOnePanel.setLayout(new GridLayout(1, 2));
     * buttonOnePanel.add(_calculateOneBtn); buttonOnePanel.add(_showOneBtn);
     * 
     * smallOnePanel.setLayout(new GridLayout(2, 1));
     * smallOnePanel.add(labelOnePanel); smallOnePanel.add(editOnePanel);
     * 
     * bigOnePanel.setLayout(new GridBagLayout());
     * bigOnePanel.add(smallOnePanel); bigOnePanel.add(buttonOnePanel);
     * 
     * buttonPanel.add(_saveBtn); buttonPanel.add(_cancelBtn); //
     * _myd.setLayout(new BoxLayout(_myd, BoxLayout.PAGE_AXIS));
     * this.getContentPane().add(bigAllPanel); _myd.add(bigOnePanel);
     * _myd.add(buttonPanel);
     * 
     * _fieldNumFrequencies.addActionListener(this);
     * _fieldNumOrientations.addActionListener(this); //
     * _typeChoice.addActionListener(this); _showAllBtn.addActionListener(this);
     * _calculateAllBtn.addActionListener(this);
     * _showOneBtn.addActionListener(this);
     * _calculateOneBtn.addActionListener(this);
     * _cancelBtn.addActionListener(this); _saveBtn.addActionListener(this); //
     * tab key implementation
     * _fieldNumFrequencies.setNextFocusableComponent(_fieldNumOrientations);
     * _fieldNumOrientations.setNextFocusableComponent(_comboFeature);
     * _comboFeature.setNextFocusableComponent(_fieldNumFrequencies);
     * 
     * _labelNumFrequencies.setToolTipText("Number of central frequencies");
     * _labelNumOrientations.setToolTipText("Number of orientations");
     * _labelType.setToolTipText("Type of features sought");
     * 
     * _fieldNumFrequencies.setToolTipText("Number of central frequencies");
     * _fieldNumOrientations.setToolTipText("Number of orientations");
     * _comboFeature.setToolTipText("Type of features sought");
     * 
     * _myd.addWindowListener(new GaborFilterBankDialogWindowListener());
     * 
     * pack();
     * 
     * _myd.setBounds(200, 200, 580, 220); _myd.show(); }
     */
    public void createUI() {
        // _imgFrame = myFrame;

        JPanel bigAllPanel = new JPanel();
        JPanel bigOnePanel = new JPanel();
        JPanel smallAllPanel = new JPanel();
        JPanel smallOnePanel = new JPanel();
        JPanel labelAllPanel = new JPanel();
        JPanel labelOnePanel = new JPanel();
        JPanel editAllPanel = new JPanel();
        JPanel editOnePanel = new JPanel();
        JPanel buttonAllPanel = new JPanel();
        JPanel buttonOnePanel = new JPanel();
        JPanel buttonPanel = new JPanel();

        TitledBorder borderAll = BorderFactory.createTitledBorder("Full filter bank scan");
        borderAll.setTitleJustification(TitledBorder.CENTER);
        bigAllPanel.setBorder(borderAll);

        TitledBorder borderOne = BorderFactory.createTitledBorder("Single filter scan");
        borderOne.setTitleJustification(TitledBorder.CENTER);
        bigOnePanel.setBorder(borderOne);

        labelAllPanel.setLayout(new GridLayout(1, 4));
        labelAllPanel.add(_labelNumFrequencies);
        labelAllPanel.add(_labelNumOrientations);
        labelAllPanel.add(_labelOffset);
        labelAllPanel.add(_labelType);

        _comboFeature.addItem("Gray");
        _comboFeature.addItem("R");
        _comboFeature.addItem("G");
        _comboFeature.addItem("B");
        _comboFeature.addItem("RGB");
        _comboFeature.addItem("Opponent");
        _comboFeature.addItem("HS Complex");

        editAllPanel.setLayout(new GridLayout(1, 4));
        _fieldNumFrequencies.addActionListener(this);
        editAllPanel.add(_fieldNumFrequencies);
        _fieldNumOrientations.addActionListener(this);
        editAllPanel.add(_fieldNumOrientations);
        editAllPanel.add(_fieldOffset);
        editAllPanel.add(_comboFeature);

        buttonAllPanel.setLayout(new GridLayout(1, 2));
        buttonAllPanel.add(_calculateAllBtn);
        buttonAllPanel.add(_showAllBtn);

        smallAllPanel.setLayout(new GridLayout(2, 1));
        smallAllPanel.add(labelAllPanel);
        smallAllPanel.add(editAllPanel);

        bigAllPanel.setLayout(new GridBagLayout());
        bigAllPanel.add(smallAllPanel);
        bigAllPanel.add(buttonAllPanel);

        labelOnePanel.setLayout(new GridLayout(1, 2));
        labelOnePanel.add(_labelF);
        labelOnePanel.add(_labelOrientation);

        editOnePanel.setLayout(new GridLayout(1, 2));
        _fieldF.addActionListener(this);
        editOnePanel.add(_fieldF);
        _fieldOrientation.addActionListener(this);
        editOnePanel.add(_fieldOrientation);

        buttonOnePanel.setLayout(new GridLayout(1, 2));
        buttonOnePanel.add(_calculateOneBtn);
        buttonOnePanel.add(_showOneBtn);

        smallOnePanel.setLayout(new GridLayout(2, 1));
        smallOnePanel.add(labelOnePanel);
        smallOnePanel.add(editOnePanel);

        bigOnePanel.setLayout(new GridBagLayout());
        bigOnePanel.add(smallOnePanel);
        bigOnePanel.add(buttonOnePanel);

        buttonPanel.add(_saveBtn);
        buttonPanel.add(_cancelBtn);

        // _myd.setLayout(new BoxLayout(_myd, BoxLayout.PAGE_AXIS));
        this.getContentPane().add(bigAllPanel, BorderLayout.NORTH);
        this.getContentPane().add(bigOnePanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        _fieldNumFrequencies.addActionListener(this);
        _fieldNumOrientations.addActionListener(this);
        // _typeChoice.addActionListener(this);
        _showAllBtn.addActionListener(this);
        _calculateAllBtn.addActionListener(this);
        _showOneBtn.addActionListener(this);
        _calculateOneBtn.addActionListener(this);
        _cancelBtn.addActionListener(this);
        _saveBtn.addActionListener(this);

        // tab key implementation
        _fieldNumFrequencies.setNextFocusableComponent(_fieldNumOrientations);
        _fieldNumOrientations.setNextFocusableComponent(_comboFeature);
        _comboFeature.setNextFocusableComponent(_fieldNumFrequencies);

        _labelNumFrequencies.setToolTipText("Number of central frequencies");
        _labelNumOrientations.setToolTipText("Number of orientations");
        _labelType.setToolTipText("Type of features sought");

        _fieldNumFrequencies.setToolTipText("Number of central frequencies");
        _fieldNumOrientations.setToolTipText("Number of orientations");
        _comboFeature.setToolTipText("Type of features sought");

        // _myd.addWindowListener(new GaborFilterBankDialogWindowListener());

        pack();

        // _myd.setBounds(200, 200, 580, 220);
        // _myd.show();
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource().getClass().toString().equalsIgnoreCase("class javax.swing.JButton")) {
            JButton btn = (JButton) event.getSource();
            if (btn == _calculateAllBtn)
                try {
                    calculateAllActivated(event);
                } catch (ImageException e) {
                	logger.warn("Image Creation Error", e);
                }
            else if (btn == _showAllBtn)
                showAllActivated(event);
            else if (btn == _cancelBtn)
                cancelActivated(event);
            if (btn == _calculateOneBtn)
                try {
                    calculateOneActivated(event);
                } catch (ImageException e) {
                	logger.warn("Image Creation Error", e);
                }
            else if (btn == _showOneBtn)
                showOneActivated(event);
            else if (btn == _saveBtn)
                saveActivated(event);
        }
    }

    public void calculateAllActivated(ActionEvent event) throws ImageException{
        ImageObject image = imagepanel.getImageObject();
        if (image == null)
            return;

        int numFreqs, numOrients, rows, cols, type;
        double offset;

        try {
            numFreqs = Integer.parseInt(_fieldNumFrequencies.getText());
        } catch (NumberFormatException e) {
            numFreqs = 1;
        }
        try {
            numOrients = Integer.parseInt(_fieldNumOrientations.getText());
        } catch (NumberFormatException e) {
            numOrients = 3;
        }
        rows = Math.max(image.getNumCols(), image.getNumRows());
        cols = Math.max(image.getNumCols(), image.getNumRows());
        try {
            offset = Double.parseDouble(_fieldOffset.getText());
        } catch (NumberFormatException e) {
            offset = 0;
        }
        type = _comboFeature.getSelectedIndex();

        // sanity check
        if (numFreqs < 1)
            numFreqs = 1;
        if (numOrients < 3)
            numOrients = 3;

        // check if settings changed, if not don't calculate again
        if ((_prevAllValues[0] == numFreqs) && (_prevAllValues[1] == numOrients) && (_prevAllValues[2] == rows) && (_prevAllValues[3] == cols) && (_prevAllValues[4] == offset)
                && (_prevAllValues[5] == type)) {

            return;
        }

        // put current values into previous array
        _prevAllValues[0] = numFreqs;
        _prevAllValues[1] = numOrients;
        _prevAllValues[2] = rows;
        _prevAllValues[3] = cols;
        _prevAllValues[4] = offset;
        _prevAllValues[5] = type;

        _gfb.setNumFrequencies(numFreqs);
        _gfb.setNumOrientations(numOrients);
        _gfb.setRows(rows);
        _gfb.setCols(cols);
        _gfb.setOffset(offset);
        _gfb.setFeatureType(type);
        ImageObject filterBankImg = null;
       
        filterBankImg = ImageObject.createImage(_gfb.getRows(), _gfb.getCols(), 1, "DOUBLE");
       
        _features = _gfb.discreteDecomposition(image, filterBankImg);
        
        if (_features == null)
            logger.warn("Features couldn't be extracted");
        else {
            logger.info("showing filter bank");

            // display filter bank used
            Point loc = imagepanel.getLocation();
            loc.translate(imagepanel.getWidth(), 60);
            _filterBankFrame = new ImageFrame("Gabor Filter Bank");
            _filterBankFrame.setImageObject(filterBankImg);
            // showAllActivated(event);
            int imgRows = image.getNumRows();
			int imgCols = image.getNumCols();
			int newRows = (int) Math.ceil(Math.log(imgRows) / Math.log(2));
			int newCols = (int) Math.ceil(Math.log(imgCols) / Math.log(2));

			int k = newRows;
			newRows = 1;
			for (int i = 0; i < k; i++)
				newRows = newRows * 2;
			k = newCols;
			newCols = 1;
			for (int i = 0; i < k; i++)
				newCols = newCols * 2;

			ImageObject temp = null;
			ImageObject oneBand = null;
			ImageObject imgFiltered = null;
			ImageObject outImage = null;
			
			if (type != 6)
				outImage = ImageObject.createImage(image.getNumRows(), image.getNumCols(),
						image.getNumBands(), "DOUBLE");
			else
				outImage = ImageObject.createImage(image.getNumRows(), image.getNumCols(), 1,
						"DOUBLE");
			
        	
			// loop over all bands in image
			for (int band = 0; band < image.getNumBands(); band++) {
				
				if (type != 6) {
					
					oneBand = ImageObject.createImage(image.getNumRows(), image.getNumCols(), 1,
							image.getType());
					oneBand = image.extractBand(new int[] { band });
				} else
					oneBand = image;
				
				
				temp = FourierTransform.padImageObject(oneBand, newRows,
						newCols);
				
				
				imgFiltered = _gfb.filterImage(temp);
				
				imgFiltered = FourierTransform.toMagnitudePhase(imgFiltered);

				if (type != 6)
					for (int i = 0; i < imgRows; i++)
						for (int j = 0; j < imgCols; j++)
							outImage.set((i * outImage.getNumCols() + j)
									* outImage.getNumBands() + band, imgFiltered.getDouble((i
									* imgFiltered.getNumCols() + j) * 2));
				else {
					for (int i = 0; i < imgRows; i++)
						for (int j = 0; j < imgCols; j++)
							outImage.set(i * outImage.getNumCols() + j, imgFiltered.getDouble((i
									* imgFiltered.getNumCols() + j) * 2));
					band = 2;
				}
			}
			loc = imagepanel.getLocation();
			loc.translate(imagepanel.getWidth(), 60);
			_resultBankFrame = new ImageFrame("Filtered Image");
			_resultBankFrame.setImageObject(outImage);
		}
        
    }

    public void showAllActivated(ActionEvent event) {
    	Point loc= null;
    	if (_filterBankFrame != null){

        // display filter bank used
        loc = imagepanel.getLocation();
        loc.translate(imagepanel.getWidth(), 60);
        _filterBankFrame.show();
        showFeatureValues();
    	}
        if (_resultBankFrame != null){

            // display filter bank used
            loc = imagepanel.getLocation();
            loc.translate(imagepanel.getWidth(), 60);
            _resultBankFrame.show();
            showFeatureValues();
        }
    }

    public void saveActivated(ActionEvent event) {
        FileChooser fc;

        if (_filterFrame != null) {
            _filterFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            fc = new FileChooser();
            fc.setTitle("Save Resulting Image");
            try {
                String filename = fc.showSaveDialog();
                if (filename != null) {
                    ImageLoader.writeImage(filename, _filterFrame.getImageObject());
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }

        if (_resultFrame != null) {
            _resultFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            fc = new FileChooser();
            fc.setTitle("Save Resulting Image");
            try {
                String filename = fc.showSaveDialog();
                if (filename != null) {
                    ImageLoader.writeImage(filename, _resultFrame.getImageObject());
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }

        if (_filterBankFrame != null) {
            _filterBankFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            fc = new FileChooser();
            fc.setTitle("Save Resulting Image");
            try {
                String filename = fc.showSaveDialog();
                if (filename != null) {
                    ImageLoader.writeImage(filename, _filterBankFrame.getImageObject());
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
        if (_resultBankFrame != null) {
            _resultBankFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            fc = new FileChooser();
            fc.setTitle("Save Resulting Image");
            try {
                String filename = fc.showSaveDialog();
                if (filename != null) {
                    ImageLoader.writeImage(filename, _resultBankFrame.getImageObject());
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    public void cancelActivated(ActionEvent event) {
        if (_filterFrame != null)
            _filterFrame.dispose();
        if (_resultFrame != null)
            _resultFrame.dispose();
        if (_filterBankFrame != null)
            _filterBankFrame.dispose();
        if (_resultBankFrame != null)
            _resultBankFrame.dispose();
        setVisible(false);
        // _myd.dispose();
    }

    public void calculateOneActivated(ActionEvent event) throws ImageException {
        Point locShow;
        ImageObject image = imagepanel.getImageObject();
        if (image == null)
        	return;

        int numFreqs, numOrients, rows, cols, type, orientation;
        double offset, F;

        // Setup Gabor Filter
        try {
            numFreqs = Integer.parseInt(_fieldNumFrequencies.getText());
        } catch (NumberFormatException e) {
            numFreqs = 1;
        }
        try {
            numOrients = Integer.parseInt(_fieldNumOrientations.getText());
        } catch (NumberFormatException e) {
            numOrients = 3;
        }
        rows = Math.max(image.getNumCols(), image.getNumRows());
        cols = Math.max(image.getNumCols(), image.getNumRows());
        try {
            offset = Double.parseDouble(_fieldOffset.getText());
        } catch (NumberFormatException e) {
            offset = 0;
        }
        type = _comboFeature.getSelectedIndex();
        try {
            F = Integer.parseInt(_fieldF.getText());
        } catch (NumberFormatException e) {
            F = 0;
        }
        try {
            orientation = Integer.parseInt(_fieldOrientation.getText());
        } catch (NumberFormatException e) {
            orientation = 0;
        }

        // sanity check
        if (numFreqs < 1)
            numFreqs = 1;
        if (numOrients < 3)
            numOrients = 3;
        if (F < 0)
            F = 0;
        if (F >= numFreqs)
            F = numFreqs - 1;

        // check if settings changed, if not don't calculate again
        if ((_prevOneValues[0] == numFreqs) && (_prevOneValues[1] == numOrients) && (_prevOneValues[2] == rows) && (_prevOneValues[3] == cols) && (_prevOneValues[4] == offset)
                && (_prevOneValues[5] == type) && (_prevOneValues[6] == F) && (_prevOneValues[7] == orientation)) {

            return;
        }

        // put current values into previous array
        _prevOneValues[0] = numFreqs;
        _prevOneValues[1] = numOrients;
        _prevOneValues[2] = rows;
        _prevOneValues[3] = cols;
        _prevOneValues[4] = offset;
        _prevOneValues[5] = type;
        _prevOneValues[6] = F;
        _prevOneValues[7] = orientation;

        // if ((type == 0) || (type == 4) || (type == 5)); // scan all bands
        if ((type == 1) || (type == 2) || (type == 3)) { // get R, G, or B
            // band
            if (image.getNumBands() >= type) {
            	
                ImageObject oneBand = ImageObject.createImage(image.getNumRows(), image.getNumCols(), 1, image.getType());
                oneBand= image.extractBand(new int[] { type - 1 });
                image = oneBand;
            	
            	
                
            }
        } else if (type == 6) // HS complex
            if (image.getNumBands() == 3)
                
            	image = _gfb.RGB2ComplexHS(image);
                
            else
                type = 0;

        _gfb.setNumFrequencies(numFreqs);
        _gfb.setNumOrientations(numOrients);
        _gfb.setRows(rows);
        _gfb.setCols(cols);
        _gfb.setOffset(offset);
        _gfb.setFeatureType(type);

        double sampleRate = 1.0 / (5 * Math.pow(2, numFreqs - 1) * Math.sqrt(2));
        double phi = orientation * 2 * Math.PI / numOrients + offset;
        F = Math.pow(2, F) * Math.sqrt(2) * image.getNumCols();
        
        _gfb.makeFrequencyFilter(F, phi, sampleRate);
        // end gabor filter setup
        
        int imgRows = image.getNumRows();
        int imgCols = image.getNumCols();
        int newRows = (int) Math.ceil(Math.log(imgRows) / Math.log(2));
        int newCols = (int) Math.ceil(Math.log(imgCols) / Math.log(2));

        int k = newRows;
        newRows = 1;
        for (int i = 0; i < k; i++)
            newRows = newRows * 2;
        k = newCols;
        newCols = 1;
        for (int i = 0; i < k; i++)
            newCols = newCols * 2;

        ImageObject temp = null;
        ImageObject oneBand = null;
        ImageObject imgFiltered = null;
        ImageObject outImage = null;
        
        if (type != 6)
            outImage = ImageObject.createImage(image.getNumRows(), image.getNumCols(), image.getNumBands(), "DOUBLE");
        else
            outImage = ImageObject.createImage(image.getNumRows(), image.getNumCols(), 1, "DOUBLE");
        
        // loop over all bands in image
        for (int band = 0; band < image.getNumBands(); band++) {
           
        	if (type != 6) {
                
        		oneBand = ImageObject.createImage(image.getNumRows(), image.getNumCols(), 1, image.getType());
                
                
        		oneBand= image.extractBand(new int[] { band});
                
            } else
                oneBand = image;
           
            
            temp = FourierTransform.padImageObject(oneBand, newRows, newCols);
            
            
            imgFiltered = _gfb.filterImage(temp);
            
            imgFiltered = FourierTransform.toMagnitudePhase(imgFiltered);

            if (type != 6)
                for (int i = 0; i < imgRows; i++)
                    for (int j = 0; j < imgCols; j++)
                        outImage.set((i * outImage.getNumCols() + j) * outImage.getNumBands() + band, imgFiltered.getDouble((i * imgFiltered.getNumCols() + j) * 2));
            else {
                for (int i = 0; i < imgRows; i++)
                    for (int j = 0; j < imgCols; j++)
                        outImage.set(i * outImage.getNumCols() + j, imgFiltered.getDouble((i * imgFiltered.getNumCols() + j) * 2));
                band = 2;
            }
        }

        locShow = imagepanel.getLocation();
        locShow.translate(imagepanel.getWidth(), 0);
        _filterFrame = new ImageFrame("Gabor Filter");
        _filterFrame.setImageObject(_gfb.getFilter());
        // _filterFrame.show();

        locShow = imagepanel.getLocation();
        locShow.translate(imagepanel.getWidth(), 40);
        _resultFrame = new ImageFrame("Filtered Image");
        _resultFrame.setImageObject(outImage);
        // _resultFrame.show();

        // showOneActivated(event);
    }

    public void showOneActivated(ActionEvent event) {
        Point locShow;
        if (_filterFrame != null) {
            locShow = imagepanel.getLocation();
            locShow.translate(imagepanel.getWidth(), 0);
            // _filterFrame = refreshFrame(_filterFrame, locShow,
            // _gfb.getFilter(), "Gabor Filter");
            _filterFrame.show();
        }

        if (_resultFrame != null) {
            locShow = imagepanel.getLocation();
            locShow.translate(imagepanel.getWidth(), 40);
            // _resultFrame = refreshFrame(_resultFrame, locShow, outImage,
            // "Filtered Image");
            _resultFrame.show();
        }
    }

    /*
     * private boolean saveFrame(FileDialog dialog, ImageFrame frame, String
     * extension) { String DirName = dialog.getDirectory(); String FileName;
     * 
     * if ((FileName = dialog.getFile()) != null) { FileName = DirName +
     * FileName; // System.out.println("dir + filename=" + FileName ); //
     * determine the suffix and the allowed image format int len =
     * FileName.length(); // System.out.println("Info: FileName length=" + len
     * ); String suf = FileName.substring(len - 4, len);
     * System.out.println("Info: image suffix=" + suf); if
     * (suf.equalsIgnoreCase(extension) == false) FileName = FileName +
     * extension; try { frame.ImageSaveAs(FileName); } catch (Exception e) {
     * System.out.println("Error: IO Exception"); return false; } } else {
     * System.out.println("Info: FileDialog Cancelled"); return false; }
     * 
     * return true; }
     */
/*    private ImageFrame refreshFrame(ImageFrame frame, Point location, ImageObject image, String title) {
        try {
            if (frame == null) {
                frame = new ImageFrame(title);
                frame.setImageObject(image);
                // frame.mbar.setVisible(false);
                frame.getImagePanel().setPreferredSize(new Dimension(frame.getImagePanel().getWidth(), frame.getImagePanel().getHeight()));
            } else {
                ImagePanel imageObjPanel = frame.getImagePanel();
                frame.setVisible(false);
                imageObjPanel.setImageObject(image);
                imageObjPanel.setSize(new Dimension(imageObjPanel.getWidth(), imageObjPanel.getHeight()));
                // frame.updateImage(image, "description", null);
            }
        } catch (Exception e) {
            return null;
        }
        frame.hide();
        return frame;
    }
*/
    /**
     * Shows dialog box with feature values
     * 
     * @return boolean True on success, false if features haven't been
     *         calculated yet
     */
    private boolean showFeatureValues() {
        if (_features == null)
            return false;

        // TODO maybe make text in multiple columns
        System.out.println("Features");
        String text = "";
        if (_gfb.getFeatureType() == _gfb.FEATURE_GRAY) {
            text = "Gray\n";
            for (int i = 0; i < _features.length; i++)
                text = text + _features[i] + "\n";
        } else if (_gfb.getFeatureType() == _gfb.FEATURE_RED) {
            text = "Red\n";
            for (int i = 0; i < _features.length; i++)
                text = text + _features[i] + "\n";
        } else if (_gfb.getFeatureType() == _gfb.FEATURE_GREEN) {
            text = "Green\n";
            for (int i = 0; i < _features.length; i++)
                text = text + _features[i] + "\n";
        } else if (_gfb.getFeatureType() == _gfb.FEATURE_BLUE) {
            text = "Blue\n";
            for (int i = 0; i < _features.length; i++)
                text = text + _features[i] + "\n";
        } else if (_gfb.getFeatureType() == _gfb.FEATURE_RGB) {
            text = "Red                 Green               Blue\n";
            for (int i = 0; i < _features.length-2; i = i + 3)
                text = text + _features[i] + "  " + _features[i + 1] + "  " + _features[i + 2] + "\n";
        } else if (_gfb.getFeatureType() == _gfb.FEATURE_OPPONENT) {
            text = "RG\tRB\tGB\n";
            for (int i = 0; i < _features.length; i = i + 9) {
                text = text + _features[i];
                for (int j = 1; j < 9; j++)
                    text = text + "  " + _features[i + j];
                text = text + "\n";
            }
        } else if (_gfb.getFeatureType() == _gfb.FEATURE_HSCOMPLEX) {
            text = "HS complex\n";
            for (int i = 0; i < _features.length; i++)
                text = text + _features[i] + "\n";
        }

        for (int i = 0; i < _features.length; i++) {
            System.out.println("feature[" + i + "] = " + _features[i]);
        }

        // AboutDialog ad = new AboutDialog(this, "Features", text, 0, 0);
        return true;
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

    class GaborFilterBankDialogWindowListener extends WindowAdapter {
        public void windowClosing(WindowEvent event) {
            Window window = (Window) event.getSource();
            window.dispose();
        }
    }

    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu tools = new JMenu("Tools");

        JMenuItem gabor = new JMenuItem(new AbstractAction("Gabor Filter") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                }
                toFront();
            }
        });

        tools.add(gabor);
        return new JMenuItem[] { tools };
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            setVisible(false);
        }
    }

    public URL getHelp(String menu) {

        return getClass().getResource("help/gaborfilter.htm");
    }

}
