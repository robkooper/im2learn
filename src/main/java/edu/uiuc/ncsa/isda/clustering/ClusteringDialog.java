/**
 * File: ClusteringDialog.java
 * Programmer: Scott Deuser, Kelby Lanning
 * Date Started: June 17, 2008
 * Last Updated: November 3, 2008
 * Description - This class will set up a GUI that will allow the user to perform k-means 
 *               and ISODATA clustering on images. When showing it will take in an image 
 *               and use that image as its data set.
 * Intended Use - Intended to be used with Isodata.java, and Kmeans.java
 */

package edu.uiuc.ncsa.isda.clustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.ColorBar;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.display.VerticalLayout;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImEnhance;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;


public class ClusteringDialog extends Im2LearnFrame implements ActionListener, Im2LearnMenu {

    private ImagePanel                 imagePanel               = null;                             // the
                                                                                                     // image
                                                                                                     // Panel
                                                                                                     // that
                                                                                                     // this
                                                                                                     // dialog
                                                                                                     // is
                                                                                                     // associated
                                                                                                     // with
                                                                                                     // .

    // private members associated with the control panel
    private final JLabel               labelAlgorithm           = new JLabel("Algorithm");
    private final JComboBox            comboBoxAlgorithm        = new JComboBox();
    private final String               algorithmIsodata         = "Isodata";
    private final String               algorithmKMeans          = "KMeans";
    private String                     algorithm                = algorithmIsodata;

    private final JButton              buttonFindSeeds          = new JButton("FindSeeds");
    private final JButton              buttonShowSeeds          = new JButton("ShowSeeds");
    private final JButton              buttonSaveSeeds          = new JButton("SaveSeeds");
    private final JLabel               labelFindSeeds           = new JLabel("SeedsType");
    private final JComboBox            comboBoxFindSeeds        = new JComboBox();
    private final String               FirstNPoints             = "First N Points";
    private final String               LoadFromFile             = "Load From File";
    private final String               Random                   = "Random";
    private final String               SeperatedBy2StDev        = "Separated by 2*StDev";
    private String                     methodFindSeeds          = FirstNPoints;

    private final JLabel               labelNumberClusters      = new JLabel("NumberClusters");
    private final JLabel               labelMoveCentroidsThresh = new JLabel("MoveCentroidsThresh");
    private final JLabel               labelMaxIterations       = new JLabel("MaxIterations");
    private final JLabel               labelMinSampPerCluster   = new JLabel("MinSampPerCluster");
    private final JLabel               labelLump                = new JLabel("Lump");
    private final JLabel               labelStDev               = new JLabel("StDev");
    private final JLabel               labelSubSampRow          = new JLabel("SubSampRow");
    private final JLabel               labelSubSampCol          = new JLabel("SubSampCol");

    private final JTextField           fieldNumberClusters      = new JTextField("10", 4);
    private final JTextField           fieldMoveCentroidsThresh = new JTextField("0.01", 4);
    private final JTextField           fieldMaxIterations       = new JTextField("20", 4);
    private final JTextField           fieldMinSampPerCluster   = new JTextField("20", 4);
    private final JTextField           fieldLump                = new JTextField("21", 4);
    private final JTextField           fieldStDev               = new JTextField("1.0", 4);
    private final JTextField           fieldSubSampRow          = new JTextField("1", 4);
    private final JTextField           fieldSubSampCol          = new JTextField("1", 4);

    // following items are used to normalize data before clustering
    // and un-normalize the data after the clustering
    private final JCheckBox            checkBoxNormalize        = new JCheckBox("Normalize values");
    private boolean                    valuesNormalized         = false;
    private final ArrayList<Double>    multipliers              = new ArrayList<Double>();
    private final ArrayList<Double>    minimums                 = new ArrayList<Double>();
    private ArrayList<Cluster>         listOfClusters;                                              // make
                                                                                                     // hash
                                                                                                     // Map
                                                                                                     // based
                                                                                                     // on
                                                                                                     // this

    private final JButton              buttonCluster            = new JButton("Cluster");
    private final JButton              buttonClusterMask        = new JButton("ClusterMask");
    private final JButton              buttonStats              = new JButton("Stats");
    private final JButton              buttonShowCentroids      = new JButton("ShowCentroids");
    private final JButton              buttonSaveCentroids      = new JButton("SaveCentroids");
    private final JButton              buttonShowStats          = new JButton("ShowStats");

    private final JLabel               labelResults             = new JLabel("Results");
    private final JTextArea            fieldResults             = new JTextArea(10, 45);
    private String                     resultsString            = new String();                     // check
                                                                                                     // ,
                                                                                                     // might
                                                                                                     // be
                                                                                                     // able
                                                                                                     // to
                                                                                                     // get
                                                                                                     // rid
                                                                                                     // of
                                                                                                     // this
                                                                                                     // at
                                                                                                     // the
                                                                                                     // end
                                                                                                     // .

    private final JLabel               labelEnhanceLabels       = new JLabel("Labels");
    private final JComboBox            comboBoxEnhanceLabels    = new JComboBox();
    private final String               choiceImEnhance          = "ImEnhance";
    private final String               choiceColorBar           = "ColorBar";
    private final String               choiceOriginalLabels     = "Original Labels";
    private String                     Labels                   = this.choiceImEnhance;

    private final JButton              buttonShowLabels         = new JButton("ShowLabels");
    private final JButton              buttonSaveLabels         = new JButton("SaveLabels");

    private final JButton              buttonClearResults       = new JButton("Clear Results");
    private final JButton              buttonShowHashMap        = new JButton("Hash Map");
    private final JButton              buttonDone               = new JButton("Done");

    private Isodata                    isodata                  = null;                             // performs
                                                                                                     // isodata
                                                                                                     // clustering
                                                                                                     // on
                                                                                                     // image
    private Kmeans                     kmeans                   = null;                             // performs
                                                                                                     // kmeans
                                                                                                     // clustering
                                                                                                     // on
                                                                                                     // image
    private ImageObject                imgObject                = null;                             // image
                                                                                                     // to
                                                                                                     // be
                                                                                                     // clustered
    private ArrayList<Point>           currentSeeds             = null;

    // the following five things were thrown in last week to make the legend.
    // Original design did not take this into consideration so that is why this
    // may seem
    // messy or unnecessary
    private LegendDialog               legend                   = null;                             // an
                                                                                                     // instance
                                                                                                     // of
                                                                                                     // the
                                                                                                     // legend
                                                                                                     // dialog
    private ImageObject                labels                   = null;                             // results
                                                                                                     // of
                                                                                                     // the
                                                                                                     // last
                                                                                                     // clustering
                                                                                                     // run
    private int                        numClustersFound         = 0;                                // number
                                                                                                     // of
                                                                                                     // clusters
                                                                                                     // found
                                                                                                     // in
                                                                                                     // the
                                                                                                     // last
                                                                                                     // clustering
                                                                                                     // run
    private int                        numDimensions;                                               // number
                                                                                                     // of
                                                                                                     // dimensions
                                                                                                     // of
                                                                                                     // the
                                                                                                     // data
                                                                                                     // set
    private boolean                    hasBeenShown             = false;                            // keeps
                                                                                                     // track
                                                                                                     // if
                                                                                                     // the
                                                                                                     // dialog
                                                                                                     // has
                                                                                                     // been
                                                                                                     // shown
                                                                                                     // yet
    private HashMap<Color, Double[][]> labelHashMap             = null;

    private int[]                      sizes;                                                       // keeps
                                                                                                     // number
                                                                                                     // of
                                                                                                     // points
                                                                                                     // in
                                                                                                     // each
                                                                                                     // cluster
    private ArrayList<Double>          centroidDist;                                                // keeps
                                                                                                     // the
                                                                                                     // distance
                                                                                                     // between
                                                                                                     // centroids
    private double[]                   averageDist;                                                 // keeps
                                                                                                     // the
                                                                                                     // average
                                                                                                     // Euclidean
                                                                                                     // distance
                                                                                                     // for
                                                                                                     // each
                                                                                                     // cluster
    private double[][]                 StDevVec;                                                    // keeps
                                                                                                     // the
                                                                                                     // Standard
                                                                                                     // Deviation
                                                                                                     // Vector
                                                                                                     // for
                                                                                                     // each
                                                                                                     // cluster
    private double[]                   StDevVecMax;                                                 // keeps
                                                                                                     // the
                                                                                                     // max
                                                                                                     // value
                                                                                                     // in
                                                                                                     // the
                                                                                                     // Standard
                                                                                                     // Deviation
                                                                                                     // Vector
                                                                                                     // for
                                                                                                     // each
                                                                                                     // cluster

    /**
     * constructor -Enables all buttons on the GUI that have been implemented
     */
    public ClusteringDialog() {

        this.setLayout(new BorderLayout(0, 10));
        this.setTitle("Clustering 1.0");

        this.legend = new LegendDialog();

        populateChoice();
        setNorthPanel();
        setCenterPanel();
        setSouthPanel();

        // initially the algorithm method is the isodata algorithm, so turn on
        // all components
        // not all buttons and other functionality is finished
        // turn off the buttons that are not implemented
        this.disableAllComponents();
        this.enableAllComponents(); // only enables buttons that have been
                                    // implemented.

        addListenersToControlPanel();
        pack();

    } // end ClusteringDialog()

    /**
     * @return - true if the dialog has been shown and false otherwise
     */
    public boolean getHasBeenShown() {
        return this.hasBeenShown;
    } // end getHasBeenShown()

    /**
     * @return - the number of clusters that were found in the last clustering
     *         run
     */
    public int getNumberOfClustersFound() {
        return this.numClustersFound;
    } // end getNumberOfClustersFound()

    /**
     * @return - the number of dimensions that are in the data set
     */
    public int getNumDimensions() {
        return this.numDimensions;
    } // end getNumDimensionsFound()

    /**
     * @return - the hash map that was made from the labels imageObject
     */
    public HashMap<Color, Double[][]> getHashMap() {
        return this.labelHashMap;
    } // getHashMap()

    /**
     * Clustering Dialog is now showing, so make the GUI visible and set up the
     * algorithms
     */
    @Override
    public void showing() {
        this.hasBeenShown = true;

        this.isodata = new Isodata();
        this.kmeans = new Kmeans();
        this.algorithm = algorithmIsodata;
        this.imgObject = this.imagePanel.getImageObject();

        // this.numDimensions = this.isodata.getNumberOfDimensions();

        comboBoxAlgorithm.setSelectedIndex(0);
        this.pack();
        super.showing();
    } // end showing()

    /**
     * Clustering Dialog is closing. Get rid of pointers and other objects
     * taking up space. Make Dialog not visible.
     */
    @Override
    public void closing() {

        this.fieldResults.setText("");
        this.checkBoxNormalize.setSelected(false);

        // get rid of pointers not being used
        this.isodata = null;
        this.kmeans = null;
        this.imgObject = null;
        this.currentSeeds = null;

        this.setVisible(false);

        super.closing();
    } // end closing()

    /**
     * adds the choices to the drop-down "Seeds Type" choice adds the choices to
     * the drop-down "Enhance Label" choice
     */
    private void populateChoice() {
        this.comboBoxAlgorithm.addItem(this.algorithmIsodata);
        this.comboBoxAlgorithm.addItem(this.algorithmKMeans);

        this.comboBoxFindSeeds.addItem(this.FirstNPoints);
        this.comboBoxFindSeeds.addItem(this.LoadFromFile);
        this.comboBoxFindSeeds.addItem(this.Random);
        if (algorithm.equals(algorithmIsodata)) {
            this.comboBoxFindSeeds.addItem(this.SeperatedBy2StDev);
        }

        this.comboBoxEnhanceLabels.addItem(this.choiceImEnhance);
        this.comboBoxEnhanceLabels.addItem(this.choiceColorBar);
        this.comboBoxEnhanceLabels.addItem(this.choiceOriginalLabels);
    } // end populateChoice()

    /**
     * Sets up the north panel on "this" frame
     */
    private void setNorthPanel() {
        JPanel northPanel = new JPanel(new VerticalLayout(5, VerticalLayout.BOTH));
        northPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        // top of the north panel
        JPanel algorithmPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        algorithmPanel.add(this.labelAlgorithm);
        algorithmPanel.add(this.comboBoxAlgorithm);

        // 2nd row of north panel
        JPanel completeSeedsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JPanel seedsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        seedsPanel.add(this.labelFindSeeds);
        seedsPanel.add(this.comboBoxFindSeeds);
        completeSeedsPanel.add(seedsPanel);

        JPanel seedsButtonPanel = new JPanel(new GridLayout(1, 3));
        seedsButtonPanel.add(this.buttonFindSeeds);
        seedsButtonPanel.add(this.buttonShowSeeds);
        seedsButtonPanel.add(this.buttonSaveSeeds);
        completeSeedsPanel.add(seedsButtonPanel);

        // 3rd row of north panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 5, 0));
        inputPanel.add(this.labelNumberClusters);
        inputPanel.add(this.labelMoveCentroidsThresh);
        inputPanel.add(this.labelMaxIterations);
        inputPanel.add(this.fieldNumberClusters);
        inputPanel.add(this.fieldMoveCentroidsThresh);
        inputPanel.add(this.fieldMaxIterations);

        // 4th row of north panel
        JPanel isodataInputPanel = new JPanel(new GridLayout(2, 3, 5, 0));
        isodataInputPanel.add(this.labelMinSampPerCluster);
        isodataInputPanel.add(this.labelLump);
        isodataInputPanel.add(this.labelStDev);
        isodataInputPanel.add(this.fieldMinSampPerCluster);
        isodataInputPanel.add(this.fieldLump);
        isodataInputPanel.add(this.fieldStDev);
        isodataInputPanel.setBorder(BorderFactory.createTitledBorder("Isodata"));

        // 5th row of north panel
        JPanel fifthRowPanel = new JPanel(new BorderLayout(10, 0));
        JPanel subSampPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel inputSampPanel = new JPanel(new GridLayout(2, 2, 5, 0));
        inputSampPanel.add(this.labelSubSampRow);
        inputSampPanel.add(this.labelSubSampCol);
        inputSampPanel.add(this.fieldSubSampRow);
        inputSampPanel.add(this.fieldSubSampCol);
        subSampPanel.add(inputSampPanel);
        subSampPanel.setBorder(BorderFactory.createTitledBorder("Sampling"));
        JPanel normalizePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.checkBoxNormalize.setSelected(false);
        normalizePanel.add(this.checkBoxNormalize);
        fifthRowPanel.add(subSampPanel, BorderLayout.CENTER);
        fifthRowPanel.add(normalizePanel, BorderLayout.EAST);

        // 6th row of north panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 3));
        buttonsPanel.add(this.buttonCluster);
        buttonsPanel.add(this.buttonClusterMask);
        buttonsPanel.add(this.buttonStats);
        buttonsPanel.add(this.buttonShowCentroids);
        buttonsPanel.add(this.buttonSaveCentroids);
        buttonsPanel.add(this.buttonShowStats);
        buttonPanel.add(buttonsPanel);

        northPanel.add(algorithmPanel);
        northPanel.add(completeSeedsPanel);
        northPanel.add(inputPanel);
        northPanel.add(isodataInputPanel);
        northPanel.add(fifthRowPanel);
        northPanel.add(buttonPanel);

        this.add(northPanel, BorderLayout.NORTH);
    } // end setNorthPanel()

    /**
     * Sets up the center panel on "this" frame.
     */
    private void setCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(this.labelResults, BorderLayout.NORTH);
        centerPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        // check to see what the default is on
        // this------------------------------????
        this.fieldResults.setEditable(false);
        JScrollPane resultsScrollPane = new JScrollPane(this.fieldResults);
        centerPanel.setPreferredSize(new Dimension(0, 200));
        centerPanel.add(resultsScrollPane, BorderLayout.CENTER);
        this.add(centerPanel, BorderLayout.CENTER);
    } // end setCenterPanel()

    /**
     * Sets up the south panel on "this" frame.
     */
    private void setSouthPanel() {
        JPanel southPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        southPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        JPanel southGridPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel enhancedLabelsPanel = new JPanel(new GridLayout(2, 1));
        enhancedLabelsPanel.add(this.labelEnhanceLabels);
        enhancedLabelsPanel.add(this.comboBoxEnhanceLabels);
        JPanel labelsGrid = new JPanel(new GridLayout(2, 1));
        labelsGrid.add(this.buttonShowLabels);
        labelsGrid.add(this.buttonSaveLabels);
        // labelsGrid.setSize(10, 10);

        JPanel clearPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        clearPanel.add(this.buttonClearResults);
        clearPanel.add(this.buttonShowHashMap);

        JPanel donePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        donePanel.add(this.buttonDone);

        southGridPanel.add(enhancedLabelsPanel);
        southGridPanel.add(labelsGrid);
        southGridPanel.add(clearPanel);

        southPanel.add(southGridPanel);
        southPanel.add(donePanel);
        this.add(southPanel, BorderLayout.SOUTH);
    } // end setSouthPanel()

    // //////////////////////////////////////////add all listeners to the panel
    // (top)////////////////////////////////
    /**
     * Adds all listeners to each panel.
     */
    private void addListenersToControlPanel() {
        // north panel of the control panel
        this.comboBoxAlgorithm.addItemListener(new AlgorithmListener());
        this.comboBoxFindSeeds.addItemListener(new FindSeedsMethodListener());
        this.buttonFindSeeds.addActionListener(this);
        this.buttonShowSeeds.addActionListener(this);
        this.buttonSaveSeeds.addActionListener(this);

        // center panel of the control panel
        this.buttonCluster.addActionListener(this);
        this.buttonShowCentroids.addActionListener(this);
        this.buttonSaveCentroids.addActionListener(this);
        this.buttonShowStats.addActionListener(this);
        this.buttonStats.addActionListener(this);
        this.buttonClusterMask.addActionListener(this);

        // south panel of the control panel
        this.comboBoxEnhanceLabels.addItemListener(new EnhancedLabelsListener());
        this.buttonShowLabels.addActionListener(this);
        this.buttonSaveLabels.addActionListener(this);
        this.buttonClearResults.addActionListener(this);
        this.buttonShowHashMap.addActionListener(this);
        this.buttonDone.addActionListener(this);

    } // end addListeners()

    // ///////////////////////////////////////add all listeners to the panel
    // (bottom)////////////////////////////////

    // ///////////////////////////////////////determine who invoked a listener
    // (top)/////////////////////////////////
    /**
     * Listens for an action event. When an action event is performed, the event
     * that invoked the action is determined and the appropriate function is
     * called.
     */
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof JButton) {
            JButton box = (JButton) event.getSource();
            if (box == this.buttonFindSeeds) {
                findSeedsActivated(event);
            } else if (box == this.buttonShowSeeds) {
                showSeedsActivated(event);
            } else if (box == this.buttonSaveSeeds) {
                saveSeedsActivated(event);
            } else if (box == this.buttonCluster) {
                clusterActivated(event);
            } else if (box == this.buttonShowCentroids) {
                showCentroidsActivated(event);
            } else if (box == this.buttonSaveCentroids) {
                saveCentroidsActivated(event);
            } else if (box == this.buttonShowStats) {
                showStatsIsActivated(event);
            } else if (box == this.buttonStats) {
                statsIsActivated(event);
            } else if (box == this.buttonClusterMask) {
                clusterMaskIsActivated(event);
            } else if (box == this.buttonShowLabels) {
                showLabelsActivated(event);
            } else if (box == this.buttonSaveLabels) {
                saveLabelsActivated(event);
            } else if (box == this.buttonClearResults) {
                clearResultsActivated(event);
            } else if (box == this.buttonShowHashMap) {
                showHashMapTable();
            } else if (box == this.buttonDone) {
                doneActivated(event);
            }

        } else {
            System.err.println("No function has been made yet to handle this action\n" + "IsodataDialog.actionPerformed");
        }

    } // end actionPerformed()

    // ///////////////////////////////////////determine who invoked a listener
    // (bottom)//////////////////////////////

    // ///////////////////////////////////////implements what happens when
    // action occurs (top)///////////////////////

    private class AlgorithmListener implements ItemListener {

        /**
         * Changes the available options when the algorithm changes.
         * 
         * @param event
         *            - when user changes the dropdown value of the algorithm
         */
        public void itemStateChanged(ItemEvent event) {
            algorithm = (String) comboBoxAlgorithm.getSelectedItem();
            // change the options on the GUI to make it appropriate for the
            // selected algorithm.
            if (algorithm.equals(algorithmIsodata)) {
                enableIsodataComponents();
                if (comboBoxFindSeeds.getItemCount() < 4) {
                    comboBoxFindSeeds.addItem(SeperatedBy2StDev);
                }
            } else if (algorithm.equals(algorithmKMeans)) {
                disableIsodataComponents();
                comboBoxFindSeeds.removeItem(SeperatedBy2StDev);
            } else {
                System.err.println("There should not be a way to get to AlgorithmListener.itemStateChanged if\n" + " algorithm is not 0 or 1");
            }
        } // end itemStateChanged()
    } // end class AlgorithmListener{}

    /**
     * Sets which method of finding seeds to use.
     */
    private class FindSeedsMethodListener implements ItemListener {

        /**
         * Changes the current type of seeds to be found
         * 
         * @param event
         *            -when user changes the dropdown value of "SeedsType"
         */
        public void itemStateChanged(ItemEvent event) {
            methodFindSeeds = (String) comboBoxFindSeeds.getSelectedItem();
        } // end itemStateChanged()
    } // end class FindSeedsListener{}

    /**
     * Sets what kind of image should be produced when the labels are to be
     * displayed.
     */
    private class EnhancedLabelsListener implements ItemListener {

        public void itemStateChanged(ItemEvent even) {
            Labels = (String) comboBoxEnhanceLabels.getSelectedItem();
        } // end itemStateChanged()
    } // end class FindSeedsListener{}

    /**
     * Determines which method should be used to find the seeds and then calls
     * the appropriate method to find the seeds.
     * 
     * @param event
     *            - the user clicking the "Find Seeds" button
     */
    private void findSeedsActivated(ActionEvent event) {
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        this.resultsString = "Finding Seeds...";
        this.fieldResults.append(this.resultsString + "\n");

        int temp;
        // grab the number of seeds needed to be found
        try {
            if (algorithm.equals(algorithmIsodata)) {
                temp = Integer.parseInt(this.fieldNumberClusters.getText());
                if (!this.isodata.setNumCluseters(temp)) {
                    this.fieldNumberClusters.setText("10");
                }
            } else {
                temp = Integer.parseInt(this.fieldNumberClusters.getText());
                if (!this.kmeans.setNumCluseters(temp)) {
                    this.fieldNumberClusters.setText("10");
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("A valid parameter for numberof clusters was not selected");
        }

        try {
            // determine which method to use.
            if (this.methodFindSeeds.equals(this.FirstNPoints)) {
                if (algorithm.equals(algorithmIsodata)) {
                    if (this.isodata.setOfPoints == null) {
                        this.isodata.makePoints(this.imgObject, true);
                    }
                    this.currentSeeds = this.isodata.firstNPoints();
                } else {
                    if (this.isodata.setOfPoints == null) {
                        this.kmeans.makePoints(this.imgObject, true);
                    }
                    this.currentSeeds = this.kmeans.firstNPoints();
                }
            } else if (this.methodFindSeeds.equals(this.LoadFromFile)) {
                if (algorithm.equals(algorithmIsodata)) {
                    this.currentSeeds = this.isodata.loadUserDefinedCenters(extractPointsFromFile());
                } else {
                    this.currentSeeds = this.kmeans.loadUserDefinedCenters(extractPointsFromFile());
                }

                // System.out.println(
                // "Getting seeds from a file has not been implemented yet");

            } else if (this.methodFindSeeds.equals(this.Random)) {
                if (algorithm.equals(algorithmIsodata)) {
                    if (this.isodata.setOfPoints == null) {
                        this.isodata.makePoints(this.imgObject, true);
                    }
                    this.currentSeeds = this.isodata.random();
                } else {
                    if (this.isodata.setOfPoints == null) {
                        this.kmeans.makePoints(this.imgObject, true);
                    }
                    this.currentSeeds = this.kmeans.random();
                }
            } else if (this.methodFindSeeds.equals(this.SeperatedBy2StDev)) {
                // grab the current StDev in the text box
                if (algorithm.equals(algorithmIsodata)) {
                    if (this.isodata.setOfPoints == null) {
                        this.isodata.makePoints(this.imgObject, true);
                    }
                    this.isodata.setThetaS(Double.parseDouble(this.fieldStDev.getText()));
                    this.currentSeeds = this.isodata.twoTimesStDev();
                } else {
                    System.err.println("Finding seeds by 2*StDev should not be an option for kmeans");
                }

            } else {
                System.err.println("A valid method was not selected");
            }
        } catch (Exception exception) {
            System.err.println("Error finding seeds; IsodataDialog.findSeedsActivated()");
        }
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        this.resultsString = "Seeds Found";
        this.fieldResults.append(this.resultsString + "\n");
    } // end findSeedsActivated()

    /**
     * prints the cluster centers to the text area in the GUI
     * 
     * @param event
     *            - the user clicking the show seeds button
     */
    private void showSeedsActivated(ActionEvent event) {
        this.fieldResults.append("Seeds:\n");
        if (this.currentSeeds == null) {
            this.fieldResults.append("No seeds have been made." + "\n");
        } else {
            this.displayPointsToResultsField(this.currentSeeds);
        }
    } // end showSeedsActivated()

    /**
     * Saves the seeds that were found in a csv file The points will be stored
     * one line at a time
     * 
     * @param event
     *            - the user clicking the save seeds button
     */
    private void saveSeedsActivated(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Seeds");
        try {
            // grab the seeds and make sure they exist
            if ((this.currentSeeds == null) || (this.currentSeeds.size() == 0)) {
                this.fieldResults.append("There were no seeds to save.");
            } else {
                String fileName = fc.showSaveDialog();
                if (fileName != null) {
                    // make sure it is a .csv file
                    if (!fileName.endsWith("csv")) {
                        fileName.concat(".csv");
                    }
                    // Create file
                    BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
                    out.write("Header: Saved Seeds\nEach line contains one point\n");
                    this.printPointsToStream(out, this.currentSeeds);
                    // Close the output stream
                    out.close();
                }
            }
        } catch (Exception exc) {
            System.err.println("Error saving the seeds");
            System.err.println(exc.getMessage());
        }
    } // end saveSeedsActivated()

    /**
     * Updates the results field and starts the clustering.
     * 
     * @param event
     *            - the event that someone wants the algorithm to cluster
     */
    private void clusterActivated(ActionEvent event) {
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        // how to make strings stay in result field
        this.resultsString = "Grabbing parameters...";
        this.fieldResults.append(this.resultsString + "\n");
        this.grabParametersFromPanel();

        if (this.checkBoxNormalize.isSelected()) {
            normalizeValues();
        } else {
            this.valuesNormalized = false;
        }

        // make points here. Before points can be made, must know if user wants
        // the
        // values normalized. Can't be sure until they click "cluster"
        if (this.algorithm.equals(this.algorithmIsodata)) {
            this.isodata.makePoints(this.imgObject, true);
        } else {
            this.kmeans.makePoints(this.imgObject, true);
        }

        this.resultsString = "Clustering...";
        this.fieldResults.append(this.resultsString + "\n");

        // thread should turn buttons and other functionality back on when done.
        this.disableAllComponents();

        ArrayList<Cluster> clusters = null;

        ClusteringThreadWithLabels clusterThread = new ClusteringThreadWithLabels();
        clusterThread.start();
    } // end clusterActivated()

    /**
     * Grabs the necessary parameters from the control panel to run the isodata
     * algorithm
     */
    private void grabParametersFromPanel() {

        int tempInt;
        double tempD;

        try {
            if (this.algorithm.equals(this.algorithmIsodata)) {
                tempInt = Integer.parseInt(this.fieldNumberClusters.getText());
                if (!this.isodata.setNumCluseters(Integer.parseInt(this.fieldNumberClusters.getText()))) {
                    this.fieldNumberClusters.setText("10");
                }
                if (!this.isodata.setThetaN(Integer.parseInt(this.fieldMinSampPerCluster.getText()))) {
                    this.fieldMinSampPerCluster.setText("20");
                }
                if (!this.isodata.setThetaS(Double.parseDouble(this.fieldStDev.getText()))) {
                    this.fieldStDev.setText("1.0");
                }
                if (!this.isodata.setThetaC(Integer.parseInt(this.fieldLump.getText()))) {
                    this.fieldLump.setText("21");
                }
                if (!this.isodata.setCentersTolerance(Double.parseDouble(this.fieldMoveCentroidsThresh.getText()))) {
                    this.fieldMoveCentroidsThresh.setText("0.01");
                }
                if (!this.isodata.setMaxIterations(Integer.parseInt(this.fieldMaxIterations.getText()))) {
                    this.fieldMaxIterations.setText("10");
                }
            } else if (this.algorithm.equals(this.algorithmKMeans)) {
                if (!this.kmeans.setNumCluseters(Integer.parseInt(this.fieldNumberClusters.getText()))) {
                    this.fieldNumberClusters.setText("10");
                }
                if (!this.kmeans.setCentersTolerance(Double.parseDouble(this.fieldMoveCentroidsThresh.getText()))) {
                    this.fieldMoveCentroidsThresh.setText("0.01");
                }
                if (!this.kmeans.setMaxIterations(Integer.parseInt(this.fieldMaxIterations.getText()))) {
                    this.fieldMaxIterations.setText("10");
                }
            } else {
                System.err.println("The value for algorithm should only be 0 or 1; ClusteringDialog.grabParametersFromPanel");
            }
        } catch (NumberFormatException e) {
            System.err.println("A valid parameter was not selected");
        }
    } // end grabParametersFromPanel()

    /**
     * Shows the centroids after a clustering has been run.
     * 
     * @param event
     *            - user clicking the "ShowCentroids" button.
     */
    private void showCentroidsActivated(ActionEvent event) {
        this.fieldResults.append("Centroids:\n");
        ArrayList<Point> centers = null;

        if (this.algorithm.equals(this.algorithmIsodata)) {
            centers = this.isodata.getClusterCenters();
        } else {
            centers = this.kmeans.getClusterCenters();
        }

        if ((centers == null) || (centers.size() == 0)) {
            this.fieldResults.append("No centers have been found.\n");
        } else {
            this.displayPointsToResultsField(centers);
        }

    } // end showCentroidsActivated()

    /**
     * Takes the current cluster centers and saves them to the specified place
     * 
     * @param event
     *            - the user clicking the save centroids button
     */
    private void saveCentroidsActivated(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Centroids");
        try {
            // grab the seeds and make sure they exist
            ArrayList<Point> seeds = null;
            if (this.algorithm.equals(this.algorithmIsodata)) {
                seeds = this.isodata.getClusterCenters();
            } else {
                seeds = this.kmeans.getClusterCenters();
            }

            if ((seeds == null) || (seeds.size() == 0)) {
                this.fieldResults.append("There were no centroids to save.");
            } else {
                String fileName = fc.showSaveDialog();
                if (fileName != null) {
                    // make sure it is a .csv file
                    if (!fileName.endsWith("csv")) {
                        fileName.concat(".csv");
                    }
                    // Create file
                    BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
                    out.write("Header: Saved Centroids\nEach line contains one point\n");
                    this.printPointsToStream(out, seeds);
                    // Close the output stream
                    out.close();
                }
            }
        } catch (Exception exc) {
            System.err.println("Error saving the seeds");
            System.err.println(exc.getMessage());
        }
    } // end saveCentroidsActivated()

    /**
     * When the user clicks the "Show Labels" button, a new frame is created
     * that displays the labels ImageObject.
     * 
     * @param event
     *            - the user clicking "Show Labels" button.
     */
    private void showLabelsActivated(ActionEvent event) {
        ImageFrame frame = new ImageFrame("labels");
        // determine which method to use when displaying the labels
        if (this.Labels.equals(this.choiceImEnhance)) {
            makeLegend();
        } else if (this.Labels.equals(this.choiceColorBar)) {
            try {
                if (this.algorithm.equals(this.algorithmIsodata)) {
                    ColorBar colorBar = new ColorBar(0, this.isodata.getNumClusters());
                    frame.setImageObject(colorBar.getColorImage(this.isodata.getLabels(), 0));
                } else {
                    ColorBar colorBar = new ColorBar(0, this.kmeans.getNumClusters());
                    frame.setImageObject(colorBar.getColorImage(this.kmeans.getLabels(), 0));
                }
            } catch (Exception e) {
                System.err.println("Error setting up the labels image; ClusteringDialog.showLabelsActivated");
            }

            frame.addMenu(new InfoDialog());
            frame.setVisible(true);
        }
    } // end showLabelsActivated()

    /**
     * Shows the statistics after a clustering has been run and they have been
     * computed.
     * 
     * @param event
     *            - the user clicking the "ShowStats" button.
     */
    private void showStatsIsActivated(ActionEvent event) {
        System.out.println("showStatsIsActivated()");

        int i, j, k, l, index = 0;
        String results = "";
        // print number of points in each cluster
        if (this.sizes != null) {
            this.fieldResults.append("Number of Points Inside Each Cluster:\n");
            for (i = 0; i < this.listOfClusters.size(); i++) {
                j = i + 1;
                this.fieldResults.append("Cluster " + j + " = " + this.sizes[i] + "\n");
            }
            this.fieldResults.append("\n");
        } else {
            this.fieldResults.append("No cluster sizes found.\n");
        }

        // print average Euclidean distances
        if (this.averageDist != null) {
            this.fieldResults.append("Average Euclidean distance for each cluster: \n");
            for (i = 0; i < this.averageDist.length; i++) {
                j = i + 1;
                this.fieldResults.append("Cluster " + j + " = " + this.averageDist[i] + "\n");
            }
            this.fieldResults.append("\n");
        } else {
            this.fieldResults.append("No average Euclidean distances found.\n");
        }

        // print Standard Deviation Vectors
        if (this.StDevVec != null) {
            this.fieldResults.append("Standard Deviation Vectors:\n");
            for (i = 0; i < this.StDevVec.length; i++) {
                results = "[" + (this.StDevVec[i][0]);
                for (j = 1; j < this.StDevVec[0].length; j++) {
                    results = results.concat(", " + this.StDevVec[i][j]);
                }
                k = i + 1;
                this.fieldResults.append("Cluster " + k + " = " + results + "]\n");
            }
            this.fieldResults.append("\n");
        } else {
            this.fieldResults.append("No Standard Deviation Vectors found.\n");
        }

        // print max vector of Standard Deviation Vectors
        if (this.StDevVecMax != null) {
            this.fieldResults.append("Max Standard Deviation Vector: \n");
            results = "[" + (this.StDevVecMax[0]);
            for (i = 1; i < this.StDevVecMax.length; i++) {
                results = results.concat(", " + this.StDevVecMax[i]);
            }
            this.fieldResults.append(results + "]\n\n");
        } else {
            this.fieldResults.append("No max Standard Deviation Vector found.\n");
        }

        // print mutual centroids distance for all pairs of cluster centroids
        if (this.centroidDist != null) {
            this.fieldResults.append("Mutual centroids distance for all pairs of cluster centroids: \n");
            for (i = 0; i < this.listOfClusters.size(); i++) {
                for (j = i + 1; j < this.listOfClusters.size(); j++) {
                    k = i + 1;
                    l = j + 1;
                    this.fieldResults.append("[" + k + ", " + l + "] = " + this.centroidDist.get(index) + "\n");
                    index++;
                }
            }
        } else {
            this.fieldResults.append("No centroids distances found.\n");
        }
    }

    /**
     * Computes the statistics after a clustering has been run.
     * 
     * @param event
     *            - the user clicking the "Stats" button.
     * @throws Exception
     *             - thrown while compututing average distance from cluster
     *             center, finding the standard deviation vectors, or computing
     *             the cetroids distances
     */
    private void statsIsActivated(ActionEvent event) {
        System.out.println("statsIsActivated()");
        this.fieldResults.append("Compiling Stats...\n");
        int i = 0;
        // find number of points in each cluster
        this.sizes = new int[this.listOfClusters.size()];
        for (i = 0; i < this.listOfClusters.size(); i++) {
            this.sizes[i] = this.listOfClusters.get(i).getNumberOfPointsInCluster();
        }

        ArrayList<Point> centers = new ArrayList<Point>(this.listOfClusters.size());
        for (i = 0; i < this.listOfClusters.size(); i++) {
            centers.add(i, this.listOfClusters.get(i).getClusterCenter());
        }
        // find average Euclidean distance from the cluster center
        // find Standard Deviation Vector for each cluster
        // find max Standard Deviation Vector
        // find mutual centroid distances
        try {
            if (this.algorithm.equals(this.algorithmIsodata)) {
                this.averageDist = this.isodata.ComputeAverageDistanceFromClusterCenter(this.listOfClusters, centers);
                this.StDevVec = this.isodata.FindStDevCluster(this.listOfClusters, centers);
                this.StDevVecMax = this.isodata.FindStDevClusterMax(this.StDevVec);
                this.centroidDist = this.isodata.ComputeCentroidsDist(centers);
            } else {
                this.averageDist = this.kmeans.ComputeAverageDistanceFromClusterCenter(this.listOfClusters, centers);
                this.StDevVec = this.kmeans.FindStDevCluster(this.listOfClusters, centers);
                this.StDevVecMax = this.kmeans.FindStDevClusterMax(this.StDevVec);
                this.centroidDist = this.kmeans.ComputeCentroidsDist(centers);
            }
        } catch (Exception e1) {
            System.out.println("Error computing statistics.");
            e1.printStackTrace();
        }

        this.fieldResults.append("Compiling Stats Completed Successfully\n");

    }

    /**
     * Runs clustering on all pixels not in "background."
     * 
     * @param event
     *            - the user clicking the "ClusterMask" button.
     */
    private void clusterMaskIsActivated(ActionEvent event) {
        System.out.println("clusterMaskIsActivated()");
        this.fieldResults.append("ClusterMask is activated.\n");
    }

    /**
     * Create an enhanced image and return it. Based on the algorithm selected
     * return an enhanced image with the labels.
     * 
     * @return enhanced label image.
     */
    private ImageObject getEnhancedImage() {
        ImEnhance enhance = new ImEnhance();

        if (this.algorithm.equals(this.algorithmIsodata)) {

            // EnhanceLabelsOut will try to figure out the number of clusters on
            // its own if you pass in zero for the number of labels
            return enhance.EnhanceLabelsOut(this.isodata.getLabels(), this.isodata.getClusterList().size() + 1, true);
            // for the number of labels: number of clusters plus one for invalid
            // data

        } else {
            return enhance.EnhanceLabelsOut(this.kmeans.getLabels(), this.kmeans.getClusterList().size() + 1, true);
        }

    }

    /**
     * This method uses the legendDialog data member to create a frame, not only
     * with the labels image in it, but also a legend describing the values in
     * the image
     */
    private void makeLegend() {
        // As of the writing of this method, the hash map is created with every
        // run and showing the labels is not
        // an option until after the first clustering execution, so the hash map
        // and labels ImageObject
        // should both be initialized.
        this.legend.setEnhanceImg(getEnhancedImage());
        this.legend.setLabelHashMap(this.labelHashMap);
        this.legend.setVisible(true);
    } // end makeLegend()

    /**
     * Saves the Labels ImageObject out to disk.
     * 
     * @param event
     *            - the "Save Labels" button was clicked.
     */
    private void saveLabelsActivated(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Labels");
        try {
            String fileName = fc.showSaveDialog();
            if (fileName != null) {
                ColorBar colorBar = new ColorBar(0, this.isodata.getNumClusters());

                if (this.Labels.equals(this.choiceColorBar)) {
                    if (this.algorithm.equals(this.algorithmIsodata)) {
                        ImageLoader.writeImage(fileName, colorBar.getColorImage(this.isodata.getLabels(), 0));
                    } else {
                        ImageLoader.writeImage(fileName, colorBar.getColorImage(this.kmeans.getLabels(), 0));
                    }
                } else if (this.Labels.equals(this.choiceOriginalLabels)) {
                    if (this.algorithm.equals(this.algorithmIsodata)) {
                        ImageLoader.writeImage(fileName, this.isodata.getLabels());
                    } else {
                        ImageLoader.writeImage(fileName, this.kmeans.getLabels());
                    }
                } else {
                    // Image image =
                    // this.legend.getEnhanceImg().toBufferedImage(null, true,
                    // null, false, redband, greenband, blueband, false, 0,
                    // null, 255, 1.0);
                    ImageObject newPic;
                    newPic = ImageObject.intToByte(getEnhancedImage());
                    // ImageObject.getImageObject(this.legend.getEnhanceImg().
                    // getImage());

                    ImageLoader.writeImage(fileName, newPic);
                }
            }
        } catch (Exception exc) {
            System.err.println("Error saving the image");
        }
    } // end saveLabelsActivated()

    /**
     * Clears the contents of the results field.
     * 
     * @param event
     *            - the user clicking on the clear results button.
     */
    private void clearResultsActivated(ActionEvent event) {
        this.fieldResults.setText("");
    } // end clearResultsActivated()

    /**
     * Shuts off the control panel when the "Done" button is clicked
     * 
     * @param event
     *            - the "Done" button was clicked
     */
    private void doneActivated(ActionEvent event) {
        this.setVisible(false);
    } // end doneActivated()

    // ///////////////////////////////////////implements what happens when
    // action occurs (bottom)////////////////////

    // //////////////////////////////////////other
    // methods//////////////////////////////////////////////////////////

    /**
     * Opens a open dialog and the selected file is read in. The file needs to
     * be a comma separated file with one line for a header and each point on a
     * newline.
     */
    private ArrayList<Point> extractPointsFromFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Load Seeds");
        String fileName = "";
        try {
            fileName = fc.showOpenDialog();
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
            System.err.println(ioExc.getMessage());
        }

        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        ArrayList<Point> points = new ArrayList<Point>();
        // can't be sure how many dimensions a point might be, so use dynamic
        // list
        double[] ordinates = null;
        String line = null;
        String[] splitString = null;
        // skip the first line of the file (header)
        try {
            input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        try {
            // read a line. Use string. split to split string by commas
            line = input.readLine();
            while (line != null) {
                splitString = line.split(",");
                ordinates = new double[splitString.length];
                for (int i = 0; i < splitString.length; i++) {
                    ordinates[i] = Double.parseDouble(splitString[i]);
                }

                points.add(new Point(ordinates));
                line = input.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return points;
    } // end extractPointsFromFile()

    /**
     * Disables all components.
     */
    private void disableAllComponents() {
        // they are listed in pretty much the order that they appear on the
        // panel
        this.comboBoxAlgorithm.setEnabled(false);
        this.buttonFindSeeds.setEnabled(false);
        this.buttonSaveSeeds.setEnabled(false);
        this.buttonShowSeeds.setEnabled(false);
        this.comboBoxFindSeeds.setEnabled(false);
        this.fieldNumberClusters.setEnabled(false);
        this.fieldNumberClusters.setEnabled(false);
        this.fieldMoveCentroidsThresh.setEnabled(false);
        this.fieldMaxIterations.setEnabled(false);
        this.disableIsodataComponents();
        this.fieldSubSampRow.setEnabled(false);
        this.fieldSubSampCol.setEnabled(false);
        this.buttonCluster.setEnabled(false);
        this.buttonClusterMask.setEnabled(false);
        this.buttonStats.setEnabled(false);
        this.buttonShowCentroids.setEnabled(false);
        this.buttonSaveCentroids.setEnabled(false);
        this.buttonShowStats.setEnabled(false);
        this.comboBoxEnhanceLabels.setEnabled(false);
        this.buttonShowLabels.setEnabled(false);
        this.buttonSaveLabels.setEnabled(false);
        this.buttonClearResults.setEnabled(false);
        this.buttonShowHashMap.setEnabled(false);
        this.buttonDone.setEnabled(false);
    } // end disableAllButtons()

    /**
     * disables the components that are specific to the isodata algorithm
     */
    private void disableIsodataComponents() {
        this.labelMinSampPerCluster.setEnabled(false);
        this.labelLump.setEnabled(false);
        this.labelStDev.setEnabled(false);
        this.fieldMinSampPerCluster.setEnabled(false);
        this.fieldLump.setEnabled(false);
        this.fieldStDev.setEnabled(false);
    } // end disableIsodataButtons()

    /**
     * Enables all components.
     */
    private void enableAllComponents() {
        // they are listed in pretty much the order that they appear on the
        // panel
        this.comboBoxAlgorithm.setEnabled(true);
        this.buttonFindSeeds.setEnabled(true);
        this.buttonSaveSeeds.setEnabled(true);
        this.buttonShowSeeds.setEnabled(true);
        this.comboBoxFindSeeds.setEnabled(true);
        this.fieldNumberClusters.setEnabled(true);
        this.fieldNumberClusters.setEnabled(true);
        this.fieldMoveCentroidsThresh.setEnabled(true);
        this.fieldMaxIterations.setEnabled(true);
        this.enableIsodataComponents();
        this.fieldSubSampRow.setEnabled(true); // not implemented yet
        this.fieldSubSampCol.setEnabled(true); // not implemented yet
        this.buttonCluster.setEnabled(true);
        this.buttonClusterMask.setEnabled(true);
        this.buttonStats.setEnabled(true);
        this.buttonShowCentroids.setEnabled(true);
        this.buttonSaveCentroids.setEnabled(true);
        this.buttonShowStats.setEnabled(true);
        this.comboBoxEnhanceLabels.setEnabled(true);
        if (this.labels != null) {
            this.buttonShowLabels.setEnabled(true);
            this.buttonSaveLabels.setEnabled(true);
        }
        this.buttonClearResults.setEnabled(true);
        if (this.labelHashMap != null) {
            this.buttonShowHashMap.setEnabled(true);
        }
        this.buttonDone.setEnabled(true);
    } // end enableAllButtons()

    /**
     *Enables the components that are specific to the isodata algorithm
     */
    private void enableIsodataComponents() {
        this.labelMinSampPerCluster.setEnabled(true);
        this.labelLump.setEnabled(true);
        this.labelStDev.setEnabled(true);
        this.fieldMinSampPerCluster.setEnabled(true);
        this.fieldLump.setEnabled(true);
        this.fieldStDev.setEnabled(true);
    } // end enableIsodataButtons()

    /**
     * Displays the points in the list to the results field in the GUI.
     * 
     * @param listOfPoints
     *            - the list of points to display.
     */
    private void displayPointsToResultsField(ArrayList<Point> listOfPoints) {
        for (int i = 0; i < listOfPoints.size(); i++) {
            this.fieldResults.append((i + 1) + ": {");
            try {
                for (int j = 0; j < listOfPoints.get(i).getNumberOfDimensions(); j++) {
                    if ((j + 1) < listOfPoints.get(i).getNumberOfDimensions()) {
                        this.fieldResults.append(listOfPoints.get(i).getOrdinate(j) + ", ");
                    } else {
                        this.fieldResults.append(listOfPoints.get(i).getOrdinate(j) + "}\n");
                    }
                }
            } catch (Exception exception) {
                System.err.println("Error getting ordinates; IsodataDialog.showSeedsActivated");
            }
        }
    } // end dispalyPointsToResultsField()

    /**
     * Prints the points in the array list out to the specified stream. One
     * point on each line
     * 
     * @param out
     *            - the stream to print to
     * @param points
     *            - the list of points to print
     */
    private void printPointsToStream(BufferedWriter out, ArrayList<Point> points) {
        try {
            for (int i = 0; i < points.size(); i++) {
                for (int j = 0; j < points.get(i).getNumberOfDimensions() - 1; j++) {
                    out.write(Double.toString(points.get(i).getOrdinate(j)) + ", ");
                }

                // print out the last ordinate for the particular point
                out.write(Double.toString(points.get(i).getOrdinate(points.get(i).getNumberOfDimensions() - 1)) + "\n");
            }
        } catch (Exception exc) {
            System.err.println("Error in printPointsToStream\n" + exc.getMessage());
        }
    } // end printPointsToStream()

    /**
     * Take the images from the list and normalizes the values so that all of
     * the values are between 0 and 1
     * 
     * @param listOfImgs
     *            - the images to normalize
     */
    private void normalizeValues() {

        // TODO don't normalize the raw data
        double min;
        double max;
        double denominator;
        double newValue = 0;

        // go through each band and normalize it
        for (int band = 0; band < this.imgObject.getNumBands(); band++) {
            min = this.imgObject.getMin(band);
            max = this.imgObject.getMax(band);
            denominator = max - min;

            // go through every point in the image for this band and normalize
            // the values
            for (int i = 0 + band; i < this.imgObject.getSize(); i += this.imgObject.getNumBands()) {
                if (this.imgObject.getDouble(i) != this.imgObject.getInvalidData()) {
                    newValue = (this.imgObject.getDouble(i) - min) / denominator;
                    if (newValue < 0.0) {
                        newValue = 0.0;
                    }
                    if (newValue > 1.0) {
                        newValue = 1.0;
                    }
                    this.imgObject.set(i, newValue);
                }
            }

            // normalize the seeds
            if (this.currentSeeds != null) {
                for (int j = 0; j < this.currentSeeds.size(); j++) {
                    try {
                        //System.out.println(this.currentSeeds.get(j).getOrdinate
                        // (band));
                        newValue = (this.currentSeeds.get(j).getOrdinate(band) - min) / denominator;
                        this.currentSeeds.get(j).setOneOrdinate(newValue, band);
                        //System.out.println(this.currentSeeds.get(j).getOrdinate
                        // (band));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            // used to un-normalize the values
            this.multipliers.add(denominator);
            this.minimums.add(min);
        }
        this.valuesNormalized = true;
    } // end normalizeValues()

    /**
     * if a data set was normalized and then clustered, this method should be
     * called to unNormalize the data. unNormalize the data from
     * ClusteringDialog.listOfClusters, which should be set after each
     * clustering execution also, must unNormalize the imageObject makeHashMap
     * uses both of these and they both must be unNormalized
     */
    private void unNormalize() {
        double newValue = 0;

        // unnormalize seeds
        if (this.currentSeeds != null) {
            for (int j = 0; j < this.currentSeeds.size(); j++) {
                for (int k = 0; k < this.currentSeeds.get(j).getNumberOfDimensions(); k++) {
                    try {
                        //System.out.println(this.currentSeeds.get(j).getOrdinate
                        // (k));
                        newValue = (this.currentSeeds.get(j).getOrdinate(k) * this.multipliers.get(k)) + this.minimums.get(k);
                        this.currentSeeds.get(j).setOneOrdinate(newValue, k);
                        //System.out.println(this.currentSeeds.get(j).getOrdinate
                        // (k));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        // loop through each cluster
        for (int i = 0; i < listOfClusters.size(); i++) {

            // loop through each band in the cluster (unnormalize cluster center)
            try {
                for (int band = 0; band < listOfClusters.get(i).getClusterCenter().getNumberOfDimensions(); band++) {
                    newValue = (listOfClusters.get(i).getClusterCenter().getOrdinate(band) * this.multipliers.get(band)) + this.minimums.get(band);
                    listOfClusters.get(i).getClusterCenter().setOneOrdinate(newValue, band);

                }
            } catch (Exception exc) {
                System.err.println(exc.getMessage());
                exc.printStackTrace();
            }

            // loop through each point in a cluster
            for (int j = 0; j < listOfClusters.get(i).getNumberOfPointsInCluster(); j++) {

                // loop through each band in the cluster
                try {

                    for (int band = 0; band < listOfClusters.get(i).getPoint(j).getNumberOfDimensions(); band++) {
                        // if
                        // (listOfClusters.get(i).getPoint(j).getOrdinate(band)
                        // != this.imgObject.getInvalidData()) {
                        newValue = (listOfClusters.get(i).getPoint(j).getOrdinate(band) * this.multipliers.get(band)) + this.minimums.get(band);
                        listOfClusters.get(i).getPoint(j).setOneOrdinate(newValue, band);
                        // }
                    }
                } catch (Exception exc) {
                    System.err.println(exc.getMessage());
                    exc.printStackTrace();
                }
            }
        } // end loop through clusters

        // now loop through the imageObject
        for (int i = 0; i < this.imgObject.getSize(); i++) {
            if (this.imgObject.getDouble(i) != this.imgObject.getInvalidData()) {
                newValue = this.imgObject.getDouble(i) * this.multipliers.get(i % this.imgObject.getNumBands()) + this.minimums.get(i % this.imgObject.getNumBands());
                this.imgObject.set(i, newValue);
            }
        }
    } // end unNormalize()

    // ////////////////////////////////////threads
    // below////////////////////////////////////////////

    /**
     * used to run the clustering algorithms. This way the GUI can still print
     * to the results area while the algorithms are running
     */
    private class ClusteringThreadWithLabels extends Thread {
        // ClusteringThreadWithLabels(ArrayList<Cluster> clusters) {
        //             
        // }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            try {
                if (algorithm.equals(algorithmIsodata)) {
                    listOfClusters = isodata.runIsodataAlgorithm();
                    enableAllComponents();
                } else if (algorithm.equals(algorithmKMeans)) {
                    listOfClusters = kmeans.runAlgorithm();
                    enableAllComponents();
                    disableIsodataComponents();
                } else {
                    System.err.println("Algorithm should only be 0 or 1; ClusteringDialog.java ClusteringThreadWithLabels");
                }
            } catch (Exception exception) {
                System.err.println("Error in thread IsodataDialog.clusterActivated");
                System.err.println(exception.getMessage());
                exception.printStackTrace();
            }
            long finish = System.currentTimeMillis();
            resultsString = "Done Clustering";
            fieldResults.append(resultsString + "\n");
            fieldResults.append("Duration of clustering: " + (finish - start) + " milliseconds\n");

            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            if (algorithm.equals(algorithmKMeans)) {
                disableIsodataComponents();
                labels = kmeans.getLabels();
                numClustersFound = listOfClusters.size();
                if (valuesNormalized) {
                    unNormalize();
                }
            } else {
                labels = isodata.getLabels();
                numClustersFound = listOfClusters.size();
                if (valuesNormalized) {
                    unNormalize();
                }
            }

            // thrown in last week of internship, used to make legend
            makeHashMapTable();

            enableAllComponents();
        } // end run()
    } // end class ResultsThread

    // ////////////////////////////////////////////threads
    // above///////////////////////////

    /**
     * Sets local imagePanel to that passed to it
     * 
     * @param imagePanel
     *            panel to be set
     */
    public void setImagePanel(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    } // end setImagePanel()

    /**
     * Returns a list of menu entries that will be appended to the panel popup
     * menu.
     * 
     * @return list of menuitems.
     */
    public JMenuItem[] getPanelMenuItems() {
        return null;
    } // end getPanelMenuItems()

    /**
     * Returns a list of menu entries that will be added to the
     * ncsa.im2learn.main menu bar. If the menuitem is a menu it will be only
     * added to the menu if it does not already exist. If it exists the menu
     * entries are appended to then end of the existing menu.
     * 
     * @return list of ncsa.im2learn.main menu items
     */
    public JMenuItem[] getMainMenuItems() {
        JMenu tools = new JMenu("Tools");
        JMenuItem menu = new JMenuItem(new AbstractAction("Clustering") {
            public void actionPerformed(ActionEvent e) {
                setLocationRelativeTo(getOwner());
                setVisible(true);
            }
        });
        tools.add(menu);
        return new JMenuItem[] { tools };
    } // end getMainMenuItems()

    /**
     * Return a URL which describes the topic. The URL can specify an html page
     * that is stored with the class file. To support loading html pages from a
     * jar file it is best to use this.getClass().getResource("html file"). This
     * will return a URL that is either to the html file on the filesystem or a
     * URL to the html file inside a jar file.
     * 
     * @param topic
     *            the helptopic of which to return help.
     * 
     * @return the url to the webpage with the help text.
     */
    public URL getHelp(String topic) {
        return null;
    }

    /**
     * Resets fields when there is a new image
     * 
     * @param event
     *            - the image has been updated
     */
    public void imageUpdated(ImageUpdateEvent event) {
        if (isVisible()) {
            switch (event.getId()) {
            case ImageUpdateEvent.NEW_IMAGE:
                // new image, so clean up stuff in dialog
                this.fieldResults.setText("");
                this.isodata = null;
                this.kmeans = null;
                this.imgObject = null;
                this.currentSeeds = null;
                showing();
                break;
            }
        }
    } // end imageUpdated()

    /**
     * Makes the labels' hashMapTable Note that for a particular band: 0 is the
     * index for the minimum, 1 is for the maximum and, 2 is for the center
     * 
     * @return - the HashMapTable of labels
     */
    private HashMap<Color, Double[][]> makeHashMapTable() {
        this.labelHashMap = new HashMap<Color, Double[][]>();

        Point pt = null;
        Point center = null;
        double[] ordinates = new double[this.imgObject.getNumBands()];
        boolean inValidPoint = false;

        double[] value = new double[this.imgObject.getNumBands()];
        Double[][] tempHashValue = null;

        ImageObject enhancedImg = getEnhancedImage();

        // go through all the pixels
        for (int row = 0; row < enhancedImg.getNumRows(); row++) {
            for (int col = 0; col < enhancedImg.getNumCols(); col++) {
                Color color = new Color(enhancedImg.getInt(row, col, 0), true);

                // used for associating the center with a particular cluster
                // color
                for (int i = 0; i < this.imgObject.getNumBands(); i++) {
                    ordinates[i] = this.imgObject.getDouble(row, col, i);
                    if (ordinates[i] == this.imgObject.getInvalidData()) {
                        inValidPoint = true;
                    }
                }
                pt = new Point(ordinates);

                if (!inValidPoint) {

                    // get the values at each of the bands
                    for (int band = 0; band < this.imgObject.getNumBands(); band++) {
                        value[band] = this.imgObject.getDouble(row, col, band);
                    }

                    // check to see if the key is already in the hash map
                    if (this.labelHashMap.get(color) != null) {
                        tempHashValue = this.labelHashMap.get(color);
                        // loop through each band and check to see if the value
                        // is a maximum or a minimum
                        for (int band = 0; band < this.imgObject.getNumBands(); band++) {
                            // check for a minimum
                            if (value[band] < tempHashValue[band][0]) {
                                tempHashValue[band][0] = value[band];
                            } else if (value[band] > tempHashValue[band][1]) {
                                tempHashValue[band][1] = value[band];
                            }
                        }
                    }
                    // key is not in the hash map
                    else {
                        center = findCluster(pt);
                        if (center == null) {
                            System.err.println("NO CENTER FOR : " + pt);
                        } else {
                            tempHashValue = new Double[this.imgObject.getNumBands()][3];
                            for (int band = 0; band < tempHashValue.length; band++) {
                                tempHashValue[band][0] = value[band];
                                tempHashValue[band][1] = value[band];
                                try {

                                    tempHashValue[band][2] = center.getOrdinate(band);

                                } catch (Exception exc) {
                                    exc.printStackTrace();
                                    System.err.println(exc.getMessage());
                                }
                            } // end for
                            this.labelHashMap.put(color, tempHashValue);
                        }
                    } // end else
                } // end if(!inValidPoint)

                inValidPoint = false;

            } // end col for()
        } // end row for()


        return this.labelHashMap;
    } // end makeHashMapTable()

    /**
     * Given a particular point, this method will find which cluster the point
     * belongs to. Once it has the cluster, it will return the center
     * 
     * @param pt
     *            - is the point that you want to find its cluster
     * @return - cluster center of the cluster the point was in
     */
    private Point findCluster(Point pt) {
        Cluster cluster = null;
        Point center = null;
        boolean notFound = true;
        int clusterNumber = 0;
        int index = 0;
        int i, j = 0;
        int numBands = this.listOfClusters.get(0).getClusterCenter().getOrdinates().size();

        // cycle through every point by looking at one cluster at a time
        while (notFound && (clusterNumber < this.numClustersFound)) {
            cluster = this.listOfClusters.get(clusterNumber);

            while (notFound && (index < cluster.getNumberOfPointsInCluster())) {
                try {

                	
                    for (i = 0, j = 0; i < numBands; i++) {
                        if (Math.abs(pt.getOrdinate(i) - (cluster.getPoint(index)).getOrdinate(i)) < (1e-6)) {
                            j++;
                        }
                    }

                    if (j == numBands) {
                        center = cluster.getClusterCenter();
                        notFound = false;
                    }


                    // if (pt.equals(cluster.getPoint(index))) {
                    // center = cluster.getClusterCenter();
                    // notFound = false;
                    // }

                } catch (Exception exc) {
                    exc.printStackTrace();
                    System.err.println(exc.getMessage());
                }
                index++;
            }

            index = 0;
            if (notFound) {
                clusterNumber++;
            }
        }

        // couldn't find the point
        if (clusterNumber >= this.numClustersFound) {
            return null;
        }

        return center;
    } // end findCluster()

    /**
     * Calls the appropriate method to make the hash map. Goes through the hash
     * map and prints out the color followed by the minimum and maximum values
     * it has for each of its bands.
     */
    public void showHashMapTable() {
        HashMap<Color, Double[][]> hmap = makeHashMapTable();

        boolean makeALog = false;
        if (makeALog) {
            PrintStream log = null;
            String fileName = getFileNameFromUser();
            try {
                log = new PrintStream(new FileOutputStream(fileName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }

            log.print("Cluster Index | red   | green | blue  ");

            int numDimensions = 0;
            if (this.algorithm.equals(this.algorithmIsodata)) {
                numDimensions = this.isodata.clusterList.get(0).getClusterCenter().getNumberOfDimensions();
            } else {
                numDimensions = this.kmeans.clusterList.get(0).getClusterCenter().getNumberOfDimensions();
            }

            for (int i = 0; i < numDimensions; i++) {
                log.print("| Band" + i + " min | Band" + i + " max ");
            }
            log.println();

            Iterator<Color> iter = hmap.keySet().iterator();
            Color key;
            Double[][] values;
            int clusterIndex = 1;

            while (iter.hasNext()) {
                key = iter.next();
                values = hmap.get(key);

                log.print(clusterIndex + "          | " + key.getRed() + " | " + key.getGreen() + " | " + key.getBlue());
                clusterIndex++;
                for (int i = 0; i < values.length; i++) {
                    log.print(" | " + values[i][0] + " | " + values[i][1]);
                }
                log.println();
            } // end while()
            log.close();

        } // end if(makeALog)

        else {
            System.out.print("Cluster Index | red   | green | blue  ");

            int numDimensions = 0;
            if (this.algorithm.equals(this.algorithmIsodata)) {
                numDimensions = this.isodata.clusterList.get(0).getClusterCenter().getNumberOfDimensions();
            } else {
                numDimensions = this.kmeans.clusterList.get(0).getClusterCenter().getNumberOfDimensions();
            }

            for (int i = 0; i < numDimensions; i++) {
                System.out.print("| Band" + i + " min | Band" + i + " max ");
            }
            System.out.println();

            Iterator<Color> iter = hmap.keySet().iterator();
            Color key;
            Double[][] values;
            int clusterIndex = 1;

            while (iter.hasNext()) {
                key = iter.next();
                values = hmap.get(key);

                System.out.print(clusterIndex + "          | " + key.getRed() + " | " + key.getGreen() + " | " + key.getBlue());
                clusterIndex++;
                for (int i = 0; i < values.length; i++) {
                    System.out.print(" | " + values[i][0] + " | " + values[i][1]);
                }
                System.out.println();
            } // end while()
        }

    } // end showHashMapTable()

    /**
     * Opens up a frame for the user to enter in the fileName to store the hash
     * map into
     */
    private String getFileNameFromUser() {

        // use JFileChooser
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getAbsolutePath();
        }
        return null;
    } // end getFileNameFromUser()

    /**
     * Created so that the GUI using the clusteringDialog can have access to the
     * labels ImageObject.
     * 
     * @return - the labels ImageObject from the algorithm that produced one.
     */
    public ImageObject getLabelsImage() {
        return this.labels;
    } // end getLabelsImage()

} // end class ClusteringDialog{}
