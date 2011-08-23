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
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * This class will the following file format:
 * Microsoft Excel 97-Excel 2003&5.0/95 Workbook (*.xls);
 * The following formats are not supported:
 * Microsoft Excel 2.1/3.0/4.0 Worksheet (*.xls);
 * 
 * @author Qi Li
 *
 */
public class ExcelTable {
	
	public ExcelTable(){};
	
	public static TableModel[] excelToTable(String inFile,boolean colName) throws BiffException, IOException{

		Workbook w;
		TableModel[] tm = null;
		try {
			w = Workbook.getWorkbook(new File(inFile));
			tm = new DefaultTableModel[w.getNumberOfSheets()];
			Sheet[] sht = w.getSheets();
			for(int i=0;i<sht.length;i++){
				tm[i] = creatTable(sht[i],-1,-1,colName);
				}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return tm;
	}

	public static TableModel excelToTable(String inFile,int sheet,boolean colName){
		TableModel[] tm = null;
		try {
			tm = excelToTable(inFile,colName);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return tm[sheet];
		}
	
	public static TableModel[] excelToTable(String inFile){
		
		TableModel[] tm = null;
		boolean colName;
		Workbook w;
		try {
			w = Workbook.getWorkbook(new File(inFile));
			tm = new DefaultTableModel[w.getNumberOfSheets()];
			Sheet[] sht = w.getSheets();
			for(int i=0;i<sht.length;i++){
				Cell firstCell = sht[i].getCell(0,0);
				if(firstCell.getType()==CellType.LABEL)
					colName = true;
				else colName = false;
				tm[i] = creatTable(sht[i],-1,-1,colName);
				}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return tm;
	}
	
	public static TableModel excelToTable(String inFile,int sheet){
		TableModel[] tm = excelToTable(inFile);
		return tm[sheet];
	}
	
	public static TableModel creatTable(Sheet sht, int rows, int cols, boolean colName){
		DefaultTableModel tm;
		if(rows==-1){
			rows = sht.getRows();
		}
		if(cols==-1){
			cols = sht.getColumns();
		}
		if( rows <= 0 || cols <=0 ){
			return null;
		}
		
		tm = new DefaultTableModel(rows-1,0);
		Cell[] cell;
		cell = sht.getRow(0);
		char defaultName = 'A';
		
		if(colName){
			//add cols and set labels for each col
			for(int i=0;i<cols;i++){
				if(i<cell.length && cell[i].getContents().length()>0){
					tm.addColumn(cell[i].getContents());
				}else{
					tm.addColumn(String.valueOf(defaultName++));
				}
			}
			
			boolean firstline = true;
			int curRow = 0;
			for(int i=0;i<rows;i++){
				cell = sht.getRow(i);
					if(firstline){
						//for(int j=0;j<cell.length;j++){
						//	tm.addColumn(cell[j].getContents());
						//}
						firstline = false;
						continue;
					}
					else{
						int curCol = 0;
						for(int j=0;j<cell.length;j++){
							try{
							tm.setValueAt(cell[j].getContents(),curRow,curCol);
							}catch(Exception e){
								
							}
							curCol++;
						}
						curRow++;
					}
				
			}
		}
		else{
			tm = new DefaultTableModel(rows,0);
            //add cols and set labels for each col
			for(int i=0;i<cols;i++){
				tm.addColumn(String.valueOf(defaultName++));
			}
			
			for(int i=0;i<rows;i++){
				cell = sht.getRow(i);
				for(int j=0;j<(cols<cell.length?cols:cell.length);j++){
					tm.setValueAt(cell[j].getContents(),i,j);
				}	
				}
		}
		return (TableModel)tm;
		}
	
	public static void writeExcel(TableModel[] tm,String[] sheetNames,String outFile) throws IOException, RowsExceededException, WriteException{
		int k = 0;
		WritableWorkbook w = Workbook.createWorkbook(new File(outFile));
	    String[] defaultSheetNames;
		if(tm.length>=1){
			if(sheetNames!=null && sheetNames.length<=tm.length){
				defaultSheetNames = sheetNames;
			    }
			else{
				defaultSheetNames = new String[tm.length];
				for(int i=0;i<tm.length;i++){
					defaultSheetNames[i] = "sheet ".concat(((Object)(i+1)).toString());
				}
			}
			for(int idx=0;idx<tm.length;idx++){
				//WritableSheet sht = w.createSheet(sheetNames[idx],idx);
				WritableSheet sht = w.createSheet(defaultSheetNames[idx],idx);
				if(!tm[idx].getColumnName(0).equalsIgnoreCase("A")){
					for(int i=0;i<tm[idx].getColumnCount();i++){
						Label colName = new Label(i,0,tm[idx].getColumnName(i));
						sht.addCell(colName);
					}
					k=1;
				}
				
				for(int i=0;i<tm[idx].getRowCount();i++){
					for(int j=0;j<tm[idx].getColumnCount();j++){
						Object val = tm[idx].getValueAt(i,j);
						Label label = new Label(j,i+k,val.toString());
						sht.addCell(label);
					}
				}
			}
		}
		else{
			System.out.println("No valid input TableModel");
			return;
		}
				
		w.write();
		w.close();
	}
	
	public static void writeExcel(TableModel[] tm,String outFile) throws RowsExceededException, WriteException, IOException{
		writeExcel(tm,null,outFile);
	}
	
	public static void writeExcel(TableModel tm, String sheetName, String outFile, boolean colName) throws RowsExceededException, WriteException, IOException{
		WritableWorkbook w = Workbook.createWorkbook(new File(outFile));
		if(sheetName==null){
			sheetName = "sheet 1";
		}
		WritableSheet sht = w.createSheet(sheetName,0);
		if(colName){
			for(int i=0;i<tm.getColumnCount();i++){
				Label label = new Label(i,0,tm.getColumnName(i));
				sht.addCell(label);
			}
			for(int i=0;i<tm.getRowCount();i++){
				for(int j=0;j<tm.getColumnCount();j++){
					try{
						Object val = tm.getValueAt(i,j);
						Label label = new Label(j,i+1,val.toString());
						sht.addCell(label);
					}catch(NullPointerException e){
						
					}
				}
			}
		}else{
			for(int i=0;i<tm.getRowCount();i++){
				for(int j=0;j<tm.getColumnCount();j++){
					try{
						Object val = tm.getValueAt(i,j);
						Label label = new Label(j,i,val.toString());
						sht.addCell(label);
					}catch(NullPointerException e){
						
					}
				}
			}
		}
		
				w.write();
				w.close();
	}
	
	public static void writeExcel(TableModel tm, String outFile, boolean colName) throws RowsExceededException, WriteException, IOException{
		writeExcel(tm,null,outFile,colName);
	}
	
	public static void writeExcel(String csvFile,String sheetNames,String outFile,boolean colName) throws Exception{
		if(csvFile.endsWith(".csv")){
			TableModel tm = new CSVTable(csvFile).table;
			writeExcel(tm,sheetNames,outFile,colName);
		}
		else{
			System.out.println("Input file is not CSV format!");
			return;
		}
	}
	
	public static void writeExcel(String csvFile,String outFile, boolean colName) throws Exception{
			writeExcel(csvFile,null,outFile,colName);
	}
	
	public static void main(String[] args){
		String xls = "C:/Documents and Settings/qili2006/My Documents/TEST/5586100.xls";
		//String xls = "C:/Documents and Settings/qili2006/My Documents/TEST/nasa.xls";
		try {
			TableModel[] ts = ExcelTable.excelToTable(xls, true);
			JFrame f = new JFrame();
			JTabbedPane tab = new JTabbedPane();
			for(int i=0;i<ts.length;i++){
				JPanel p = new JPanel(new BorderLayout());
				p.add(new JScrollPane(new JTable(ts[i])),BorderLayout.CENTER);
				tab.addTab(Integer.toString(i), p);
			}
			f.add(tab,BorderLayout.CENTER);
			f.pack();
			f.setVisible(true);
			
			
			
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
