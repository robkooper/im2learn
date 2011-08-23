/**
 * File: LegendTablesModel.java
 * Programmer: Scott Deuser
 * Date Started: August 12, 2008
 * Last Updated: August 15, 2008
 * Description - This class is used to show a legend for images.
 * Intended Use - Class is used in conjuction with LegendDialog.java
 *                this class is needed so that the JTable set up in 
 *                LegendDialog.java can display colors.
 */

package edu.uiuc.ncsa.isda.clustering;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

class LegendTableModel extends AbstractTableModel {
    private String[]   columnNames;
    private Object[][] data;

    /**
     * constructor
     */
    public LegendTableModel() {
        this(null);
    }

    /**
     * constructor
     * 
     * @param labelHashMap
     *            - labels for the legend
     */
    public LegendTableModel(HashMap<Color, Double[][]> labelHashMap) {
        setData(labelHashMap);
    } //end MyTableModel()

    /**
     * sets all the data for the labels for the legend
     * 
     * @param labelHashMap
     */
    public void setData(HashMap<Color, Double[][]> labelHashMap) {
        data = null;
        int row = 0;
        int numColumns = 1;
        int numBands = 0;
        int numRows = 0;
        if (labelHashMap != null) {
            for (Entry<Color, Double[][]> entry : labelHashMap.entrySet()) {
                if (data == null) {
                    numRows = labelHashMap.size();
                    numBands = entry.getValue().length;
                    numColumns = 1 + 3 * numBands;
                    data = new Object[numRows][numColumns];
                }

                // color
                this.data[row][0] = entry.getKey();

                // center
                for (int i = 0; i < numBands; i++) {
                    this.data[row][i + 1] = entry.getValue()[i][2];
                }

                // min max
                for (int i = 0; i < numBands; i++) {
                    this.data[row][(i * 2) + numBands + 1] = entry.getValue()[i][0];
                    this.data[row][(i * 2) + numBands + 2] = entry.getValue()[i][1];
                }

                // next row
                row++;
            }
        } else {
            data = new Object[numRows][numColumns];
        }

        // columns
        columnNames = new String[numColumns];
        columnNames[0] = "Colors";
        int col = 1;
        for (int i = 0; i < numBands; i++) {
            this.columnNames[col++] = "Center " + i;
        }

        for (int i = 0; i < numBands; i++) {
            this.columnNames[col++] = "Min " + i;
            this.columnNames[col++] = "Max " + i;
        }
    }

    /**
     * @return - the number of columns
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * @return - the number of rows
     */
    public int getRowCount() {
        return data.length;
    }

    /**
     * gets the column name of the index passed in
     * 
     * @param col
     *            - index of column selected
     * @return - column name of column selected
     * 
     */
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * gets the value at the index passed in
     * 
     * @param row
     *            - index of row selected
     * @param col
     *            - index of column selected
     * @return - the value at that column and row
     */
    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    /**
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the color column
     * would not be able to display color
     * 
     * @param c
     *            - index of column selected
     * @return - value at that column
     */
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
} //end MyTableModel{}