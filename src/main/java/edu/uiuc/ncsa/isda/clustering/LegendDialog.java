/**
 * File: LegendDialog.java
 * Programmer: Scott Deuser
 * Date Started: August 12, 2008
 * Last Updated: August 15, 2008
 * Description - This class is used to show a legend for images.
 * Intended Use - Class designed with the intent that results from clustering would
 *                be used to set the private data members of this class. Intended to be used
 *                by ClusteringDialog.java
 */

package edu.uiuc.ncsa.isda.clustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;


public class LegendDialog extends Im2LearnFrame {
    //needed to send to the table model
    private ImagePanel       labelsImagePanel;
    private LegendTableModel legendModel;
    private JTable           legendTable;

    /**
     * constructor
     */
    public LegendDialog() {
        this(null, null);
    } //end LegendDialor()

    /**
     * constructor
     * 
     * @param labelHashMap
     *            - labels for the legend
     * @param enhanceImg
     *            - image for the legend
     */
    public LegendDialog(HashMap<Color, Double[][]> labelHashMap,
            ImageObject enhanceImg) {
        super("Legend");

        createUI();
        setLabelHashMap(labelHashMap);
        setEnhanceImg(enhanceImg);
    } //end LegendDialog

    /**
     * set the labels for the legend
     * 
     * @param labelHashMap
     *            - labels for the legend
     */
    public void setLabelHashMap(HashMap<Color, Double[][]> labelHashMap) {
        legendModel.setData(labelHashMap);
        legendModel.fireTableStructureChanged();
    }

    /**
     * set the image for the legend
     * 
     * @param enhanceImg
     *            -image for the legend
     */
    public void setEnhanceImg(ImageObject enhanceImg) {
        labelsImagePanel.setImageObject(enhanceImg);
    }

    /**
     * Takes the private data members and creates a legend. Displays the legend
     * in "this" and sets "this" to visible
     */
    private void createUI() {
        //this.removeAll();

        setLayout(new BorderLayout(10, 0));
        setBackground(Color.white);

        labelsImagePanel = new ImagePanel();
        labelsImagePanel.setAutozoom(true);
        //        labelsImagePanel.setOpaque(true);
        //        labelsImagePanel.setBackground(new Color(0, 255, 0));

        //panel for the legend
        JScrollPane legendPanel = makeLegendPanel();

        add(labelsImagePanel, BorderLayout.CENTER);
        add(legendPanel, BorderLayout.EAST);

        this.setPreferredSize(new Dimension(1000, 700));

        this.pack();
    } //end createAndShowLegend()

    /**
     * sets up the legend puts it in a panel, and then returns that panel
     * 
     * @return - the JPanel that has a legend in it.
     */
    private JScrollPane makeLegendPanel() {
        legendModel = new LegendTableModel();
        legendTable = new JTable(legendModel);

        JScrollPane legendScrollPane = new JScrollPane(legendTable);
        legendTable.setFillsViewportHeight(true);

        legendTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
        legendTable.setDefaultEditor(Color.class, new ColorEditor());

        return legendScrollPane;
    } //end makeLegendPanel()

} //end class LegendDialog{}
