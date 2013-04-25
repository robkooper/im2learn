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
package edu.illinois.ncsa.isda.im2learn.ext.geo;

import java.io.*;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 
 * @author Qi Li, Ryo Kondo
 *
 */
public class CSVTable{
	
    public  TableModel table;
    
    public String[]	headerArray;

    
    public  String CSVFile = null;
    public  String outCSVFile = null;
    
    public static final String PIPE = "\\|";
    public static final String COMMA = ",";
	
    public static void main(String[] args){    	
    	CSVTable csvInstance = new CSVTable();
    	try{
//    		String filePath = "C:/TestFolder/test.txt";
    		String filePath = "C:/TestFolder/test1.txt";
//    		String filePath = "//Sweetums/www/Ryo/SampleData/Crime/FBI/2002/FBI2002ByCityOVer10000.csv";
//    		String filePath = "//Sweetums/www/Ryo/SampleData/Demography/TravelTime/Census2000ByPlace/dc_dec_2000_sf3_u_data2.txt";
    		
//    		csvInstance = new CSVTable(filePath, PIPE, true, 2);
    		csvInstance = new CSVTable(filePath, COMMA, true, 2);
//    		csvObject = new CSVTable("//Sweetums/www/Ryo/SampleData/Crime/FBI2004ByCityOVer10000.csv", 1);
//    		CSVFile = "//Sweetums/www/Ryo/SampleData/Demography/PopulationDensity/Census2000ByPlaceCDL/CensusPopDensityUSbyPlaceCDL1.csv";
//    		csvInstance = new CSVTable(//Sweetums/www/Ryo/SampleData/Demography/PopulationDensity/Census2000ByPlaceCDL/CensusPopDensityUSbyPlaceCDL1.csv", "'");
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	    	
    	csvInstance.print();
    }
    
    public CSVTable(){
    	
    }
    
    //Constructor: Call this constructor with a input CSV file, it will read
    //the file into table
	public CSVTable(String inPutFile)throws Exception{
	    table = readTable(inPutFile);
	    CSVFile = inPutFile;
	    }
	
	public CSVTable(String inputFile, String delim)throws Exception{
		table = readTable(inputFile, delim, true);
		CSVFile = inputFile;
	}
	
	/**
	 * Creates new CSVTable object and invokes readTable method
	 * if "colName" is true, "headerRows" should be at least 1
	 * 
	 * @param inputFile
	 * @param delim
	 * @param colName
	 * @param headerRows
	 * @throws Exception
	 */
	public CSVTable(String inputFile, String delim, boolean colName, int headerRows)throws Exception{
		table = readTable(inputFile, delim, colName, headerRows);
		CSVFile = inputFile;
	}
	
    public  TableModel readTable(String CSVFile,boolean colName){
    	TableModel table = new DefaultTableModel();
    	try {
			table = readTable(CSVFile,",",colName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return table;
    }
    
    public  TableModel readTable(String CSVFile)throws Exception {
    	TableModel table = new DefaultTableModel();
    	table = readTable(CSVFile,",",true);
        return table;
    }
    
    public  TableModel readTablePDL(String pdlFile)throws Exception{
    	TableModel table = new DefaultTableModel();
    	table = readTable(pdlFile, "\\|", true);
    	return table;
    }
	
	
    public  TableModel readTable(String csvFile, String delim, boolean colName) throws Exception {
    	TableModel table = new DefaultTableModel();
    	table = readTable(csvFile, delim, colName, 1);
    	return table;
    }
    
    /**
     * Reads a separated values file and itegrates it into a DefaultTableModel.
     * Skips first "headerRows" rows if colName is true.
     * If "colName" is true, headerRows should be at least 1
     * 
     * @param csvFile
     * @param delim
     * @param colName
     * @param headerRows
     * @return
     * @throws Exception
     */
    public TableModel readTable(String csvFile, String delim, boolean colName, int headerRows) throws Exception {
    	//System.out.println("Start readTable");	
    	
	    FileReader fr = new FileReader(csvFile);
	    BufferedReader reader = new BufferedReader(fr);
	    String line = null;
	    
	    int numRows = headerRows;
	    boolean firstLine;
	    if(colName){
	    	firstLine = true;
	    }else{
	    	firstLine = false;
	    }
	    int numLines = 0;
	    int numColumns = 0;

	    line = reader.readLine();
	    while (line != null && line.length() != 0) {
	      if (firstLine) {
	    	  if(numRows == 1)
	    		  firstLine = false;
	    	  numRows--;
	      }
	      else {
	        numLines++;
	      }
	      
	      int numTokens = line.split(delim).length;
	      if (numColumns < numTokens) {
	    	  numColumns = numTokens;
	      }
	      line = reader.readLine();
	    }

	    // now we know the number of lines.
	    fr = new FileReader(csvFile);
	    reader = new BufferedReader(fr);
	    int curRow = 0;

		TableModel dataset = new DefaultTableModel(numLines,0);
		char defaultname = 'A';
	    if(colName){
	    	firstLine = true;
		    numRows = headerRows;
	    	while ( (line = reader.readLine()) != null && line.length() != 0) {
	    		String[]tokens = null;
	    		if(delim.equals("\\|"))
	    		//	if(line != "")
	    				tokens = lineToArray(line);
	    		else if(delim.equals("\",\"") && line.charAt(0) == '"' && line.charAt(line.length()-1) == '"'){
	    			line = line.substring(1, line.length()-1);
	    			tokens = line.split(delim);
	    		}
	    		else
	    			tokens = line.split(delim);
	            if(firstLine){
	            	if(numRows == 1){
	            		for(int i=0;i<numColumns;i++){
		            		if(i<tokens.length){
		            			((DefaultTableModel)dataset).addColumn(tokens[i]);
		            		}else{
		            			((DefaultTableModel)dataset).addColumn(String.valueOf(defaultname++));
		            		}
		            	}
		            	headerArray = tokens;
//		            	for(int i=0; i<headerArray.length; i++)
//		            		System.out.print(headerArray[i]+" -- ");
		            	firstLine = false;
	            	}
	            	numRows--;
	            }
	            
	            else{
	            	/*
	            	if(curRow==0){
	            		for(int col=0;col<tokens.length;col++){
	            			dataset.setValueAt(col+1,0,col);
	            		}
	            		curRow++;
	            	}*/
	            	int curCol = 0;
	            	for(int i=0;i<tokens.length;i++){
	            	dataset.setValueAt(tokens[i],curRow,curCol);
	            	curCol++;
	            	}
	            	curRow++;
	            }
		    }
	    	
	    }else{
	    	//line = reader.readLine();
	    	//System.out.println(line);
	    	String[] tokens; // = line.split(delim);
	    	//System.out.println(tokens.length);
	    	//for(int i=0;i<tokens.length;i++){
	    	
	    	for(int i=0; i<numColumns; i++){
        		((DefaultTableModel)dataset).addColumn(String.valueOf(defaultname++));
    		}
	    	fr = new FileReader(csvFile);
		    reader = new BufferedReader(fr);
		    line = reader.readLine();
	    	while ( line != null && line.length() != 0) {
	    		if(delim.equals("\\|"))
	    			tokens = lineToArray(line);
	    		else if(delim.equals("\",\"") && line.charAt(0) == '"' && line.charAt(line.length()-1) == '"'){
	    			line = line.substring(1, line.length()-1);
	    			tokens = line.split(delim);
	    		}
	    		else
	    			tokens = line.split(delim);
	    		/*
		    	if(curRow==0){
            		for(int col=0;col<tokens.length;col++){
            			dataset.setValueAt(col+1,0,col);
            		}
            		curRow++;
            	}*/
	            int curCol = 0;
	            for(int i=0;i<tokens.length;i++){
	            	dataset.setValueAt(tokens[i],curRow,curCol);
	            	curCol++;
	            }
	            
	            for(; curCol < numColumns; curCol++) {
	            	dataset.setValueAt("", curRow, curCol);
	            }
	            
	            curRow++;
	            line = reader.readLine();
	            
           }
	    }
	    //System.out.println("End of readTable");
	    for(int i=0; i<dataset.getRowCount(); i++){
	    	for(int j=0; j<dataset.getColumnCount(); j++){
	    		if(dataset.getValueAt(i, j)==null)
	    			dataset.setValueAt("", i, j);
	    	}
	    }
	    return dataset;
    }
    
    /**
     * Takes in a String and parces it into a String[] while taking care of anomolies.
     * 
     * 
     * TODO: Be able to handle a lot more exceptions
     * 
     * @param line
     * @return
     */
	public  String[] lineToArray(String line){
		String[] tempArray1 = null;

		if(line == null)
			return tempArray1;
		
		/*
		This portion takes care of consecutive '|'s at the end of a line.
		Consecutive '|'s are read as empty white space and not considered to be 
		values.  
		*/
		int a=1;
		
		if(line.charAt(line.length()-a) == '|'){
			while(line.length()-(a+1)>= 0 && line.charAt(line.length()-(a+1)) == '|')
				a++;
			String[] tempArray2 = line.split("\\|");
			if(line.length()-(a+1)==-1)
				tempArray1 = new String[a+1];
			else
				tempArray1 = new String[tempArray2.length+a];
			for(int i=0; i<tempArray2.length; i++)
				tempArray1[i] = tempArray2[i];
//			for(int i=0; i<tempArray1.length; i++)
//				if(tempArray1[i]==null)
//					tempArray1[i] = "";
		}else{
			tempArray1 = line.split("\\|");
		}
		return tempArray1;
	}
	
	public  void writeTable(TableModel t, String file, boolean colName) throws Exception {
		FileWriter fw = new FileWriter(file);
	    BufferedWriter bw = new BufferedWriter(fw);
        
	   
	    int numrow = t.getRowCount();
	    int numcol = t.getColumnCount();
	    
        if(colName){
        	for(int i = 0; i < numcol; i++) {
      	      bw.write(t.getColumnName(i));
      	      if(i < numcol-1)
      	        bw.write(",");
      	    }
      	    bw.write("\n");
        }
	    /*
	     * int idx = 0;
	    int isidx = 0;
        for(int i=0;i<t.getColumnCount();i++){
			if(Integer.parseInt(t.getValueAt(0,i).toString())==i+1){
				idx++;
			}
		}
		if(idx==t.getColumnCount()){
			isidx = 1;
		}*/
	    for(int row = 0; row < numrow; row++) {
	      for(int col = 0; col < numcol; col++) {
	    	Object value = new Object();
	    	try{
	    		value = t.getValueAt(row, col);
		        bw.write(value.toString());
	    	}catch(NullPointerException e){
	    		
	    	}
	    	
	        if(col < numcol-1) {
	          bw.write(",");
	        }
	      }
	      bw.write("\n");
	    }
	    
	    bw.flush();
	    bw.close();
	  }
	
     public  void CSVToExcel(String csvfile,boolean colName, String xlsfile) throws RowsExceededException, WriteException, IOException, Exception{
    	 ExcelTable.writeExcel(readTable(csvfile,colName),xlsfile,colName);
     }
     
     public  void ExcelToCSV(String xlsfile,int sheet,boolean colName, String csvfile) throws BiffException, IOException, Exception{
    	 writeTable(ExcelTable.excelToTable(xlsfile,sheet,colName),csvfile,colName);
     }

	public String[] getHeaderArray() {
		return headerArray;
	}

	public void setHeaderArray(String[] headerArray) {
		this.headerArray = headerArray;
	}
	
	public void print(){
    	System.out.println("row is " + this.table.getRowCount());
    	System.out.println("col is " + this.table.getColumnCount());
    	for(int i=0; i < this.table.getRowCount(); i++){
    		for(int j=0; j < (this.table.getColumnCount()); j++){
    			System.out.print(this.table.getValueAt(i, j) + " | ");
    		}
    		System.out.println();
    	}
	}
     
}
/*can not write table
 * the write(value.toString())is always trouble making;
 */
