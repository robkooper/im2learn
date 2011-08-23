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
package edu.illinois.ncsa.isda.imagetools.core.io.pdf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;

/**
 * This class is a wrapper of iText library. It allows to create PDF files
 * automatically.
 * 
 */
public class PDFWriter {
	static private Log	logger	= LogFactory.getLog(PDFWriter.class);
	private Document	document;
	private PdfWriter	writer;
	private int			figureCnt	= 0, tableCnt = 0;
	private String		HTMLFilePath;

	public PDFWriter(OutputStream output) {

		document = new Document();

		try {
			writer = PdfWriter.getInstance(document, output);
			//PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("iTextExample.pdf"));

			document.open();
		} catch (DocumentException de) {
			System.err.println(de.getMessage());
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				closePDF();
			}
		});

	}

	public void closePDF() {
		if ((document != null) && document.isOpen()) {
			//System.out.print("document=" + document + " page number=" + document.getPageNumber());
			document.close();
			document = null;
		}
	}

	/**
	 * This method takes an array of 2D points and connects the points into one
	 * piece-wise linear segment on a page A new page is created at the end of
	 * the drawing.
	 * 
	 * @param points
	 *            is the 2D array of (col,row) coordinates
	 */
	public void strokeLines(int[][] points) {
		if (points == null) {
			System.err.println("ERROR: point array is null");
			return;
		}
		if ((document != null) && document.isOpen()) {

			// draw connected lines
			// the first value is horizontal (col) and the second value is vertical (row)
			PdfContentByte cb = writer.getDirectContent();
			cb.setLineWidth(0f);
			cb.moveTo(points[0][0], points[0][1]);
			int numPoints = points.length;

			//System.out.println("INFO: numPoints=" + numPoints);

			for (int i = 1; i < numPoints; i++) {
				cb.lineTo(points[i][0], points[i][1]);
			}
			cb.stroke();
			document.newPage();
		} else {
			System.err.println("ERROR: document is null or not open");
		}

	}

	public boolean Txt2PDF(AbstractDocument doc) {
		try {
			String buf = "";
			char c;
			for (int i = 0; i < doc.getLength(); i++) {
				c = doc.getText(i, 1).charAt(0);
				buf += c;
				if (c == '\n') {
					printString(buf);
					buf = "";
				}
			}
			printString(buf);

			closePDF();
			return true;
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean HTML2PDF(String filePath, HTMLDocument doc) {
		return HTML2PDF(filePath, doc, null);
	}

	public static void LaunchPDFFile(String filename) {
		try {

			Runtime rt = Runtime.getRuntime();
			rt.exec("cmd /c call \"" + filename + "\"");

//			String cmd = "cmd \"call \""+filename+"\"\"";
//			ProcessBuilder builder = new ProcessBuilder(cmd);
//			final Process process = builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean HTML2PDF(String filePath, HTMLDocument htmlDoc, String secretKey) {
		/*
		 * NOTE: THIS IS NOT A GENERAL HTML2PDF CONVERTER. Currently, it
		 * supports simple text, headings, table, and img (inside table). You
		 * are welcomed to add more features here.
		 */

		HTMLFilePath = filePath;
		try {

//			 Parse
			ElementIterator iterator = new ElementIterator(htmlDoc);
			Element element;
			while ((element = iterator.next()) != null) {
				AttributeSet attributes = element.getAttributes();
				Object name = attributes.getAttribute(StyleConstants.NameAttribute);
//				Object italic = attributes.getAttribute(StyleConstants.Italic);
//				Object bold = attributes.getAttribute(StyleConstants.Bold);
				if ((name instanceof HTML.Tag) && ((name != HTML.Tag.CONTENT))) {

					StringBuffer text = new StringBuffer();
					int count = element.getElementCount();
					for (int i = 0; i < count; i++) {
						Element child = element.getElement(i);
						AttributeSet childAttributes = child.getAttributes();
						if (childAttributes.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.CONTENT) {
							int startOffset = child.getStartOffset();
							int endOffset = child.getEndOffset();
							int length = endOffset - startOffset;
							text.append(htmlDoc.getText(startOffset, length));
						}
					}

					if (name == HTML.Tag.H1) {
						this.printTitle(text.toString() + "\n");
					} else if (name == HTML.Tag.H2) {
						this.printHeading(text.toString(), 20);
					} else if (name == HTML.Tag.H3) {
						this.printHeading(text.toString(), 16);
					} else if (name == HTML.Tag.BR) {
						this.printString("\n");
					} else if (name == HTML.Tag.IMPLIED) {
						this.printString(text.toString());
					} else if ((name == HTML.Tag.TABLE) || (name == HTML.Tag.TR)) {
						try {
							convertTable(iterator, htmlDoc, secretKey);
						} catch (java.security.InvalidKeyException e) {
							JOptionPane.showMessageDialog(null, "Invalid Key");
							return false;
						}
					} else {
					}

					//	System.out.println(name + ": " + text.toString());
				}
			}

			closePDF();

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	void convertTable(ElementIterator iterator, HTMLDocument htmlDoc, String secretKey) throws Exception {
		String caption = null;
		Element element;
		ArrayList row = new ArrayList();
		ArrayList column = null;
		ArrayList img = new ArrayList();
		boolean isImage = false;

		while ((element = iterator.next()) != null) {
			AttributeSet attributes = element.getAttributes();
			Object name = attributes.getAttribute(StyleConstants.NameAttribute);
//			Object italic = attributes.getAttribute(StyleConstants.Italic);
//			Object bold = attributes.getAttribute(StyleConstants.Bold);

			if ((name instanceof HTML.Tag)
					&& ((name == HTML.Tag.CAPTION) || (name == HTML.Tag.IMPLIED) || (name == HTML.Tag.TR) || (name == HTML.Tag.TD) || (name == HTML.Tag.CONTENT) || (name == HTML.Tag.IMG)
							|| (name == HTML.Tag.TABLE) || (name == HTML.Tag.P))) {

				StringBuffer text = new StringBuffer();
				int count = element.getElementCount();
				for (int i = 0; i < count; i++) {
					Element child = element.getElement(i);
					AttributeSet childAttributes = child.getAttributes();
					if (childAttributes.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.CONTENT) {
						int startOffset = child.getStartOffset();
						int endOffset = child.getEndOffset();
						int length = endOffset - startOffset;
						text.append(htmlDoc.getText(startOffset, length));
					}
				}

				if (name == HTML.Tag.CAPTION) {
					caption = getNext(iterator, htmlDoc);
				}
				if (name == HTML.Tag.IMG) {
					isImage = true;
					Object filename = attributes.getAttribute(HTML.Attribute.SRC);
					try {
						img.add(ImageLoader.readImage(HTMLFilePath + (String) filename));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (name == HTML.Tag.TR) {
					if (!isImage) {
						if (column != null) {
							row.add(column);
						}
						column = new ArrayList();
					}
				} else if (name == HTML.Tag.TD) {
					if (!isImage) {
						column.add(getNext(iterator, htmlDoc));
					}
				}

				//System.out.println(name + ": " + text.toString());
			} else {
				row.add(column);
				iterator.previous();
				break;
			}
		}

		String[] caps = caption.split(":");
		caption = "";
		for (int i = 1; i < caps.length; i++) {
			caption += caps[i];
		}

		if (isImage) {
			ImageObject[] data = new ImageObject[img.size()];

			for (int i = 0; i < img.size(); i++) {
				data[i] = (ImageObject) img.get(i);
			}

			this.printFigures(data, caption);
		} else {
			Object o = row.get(0);
			ArrayList<String> names = (ArrayList<String>) o;
			Object[] cNames = names.toArray();

			Object[][] data = new Object[row.size() - 1][cNames.length];
			for (int i = 1; i < row.size(); i++) {
				o = row.get(i);
				names = (ArrayList<String>) o;
				data[i - 1] = names.toArray();
			}

			//System.out.println(secretKey);

			if (secretKey != null) {

				for (int j = 0; j < cNames.length; j++) {
					try {
						cNames[j] = Crypto.decrypt((String) cNames[j], secretKey, "AES");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				for (int j = 0; j < data.length; j++) {
					for (int i = 0; i < data[0].length; i++) {
						try {
							data[j][i] = Crypto.decrypt((String) data[j][i], secretKey, "AES");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							//		System.out.println(data[j][i]);
						}
					}
					//System.out.println();
				}
			}

			JTable table = new JTable(data, cNames);

			this.printTable(table, caption);
		}
	}

	public String getNext(ElementIterator iterator, HTMLDocument htmlDoc) throws BadLocationException {

		Element element = iterator.next();

		AttributeSet attributes = element.getAttributes();
		Object name = attributes.getAttribute(StyleConstants.NameAttribute);
		Object italic = attributes.getAttribute(StyleConstants.Italic);
		Object bold = attributes.getAttribute(StyleConstants.Bold);

		StringBuffer text = new StringBuffer();
		int count = element.getElementCount();
		for (int i = 0; i < count; i++) {
			Element child = element.getElement(i);
			AttributeSet childAttributes = child.getAttributes();
			if (childAttributes.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.CONTENT) {
				int startOffset = child.getStartOffset();
				int endOffset = child.getEndOffset();
				int length = endOffset - startOffset;
				text.append(htmlDoc.getText(startOffset, length));
			}
		}

		return text.toString();
	}

	public boolean printString(String text) {
		return printString(text, false, false, 12);
	}

	public boolean printString(String text, boolean bold, boolean italic, int size) {
		try {
			Font f = new Font(Font.TIMES_ROMAN, size, Font.NORMAL);
			;

			if (bold) {
				f.setStyle(Font.BOLD);
			}
			if (italic) {
				f.setStyle(Font.ITALIC);
			}
			if (bold && italic) {
				f.setStyle(Font.BOLDITALIC);
			}

			Paragraph p = new Paragraph(text, f);

			//document.add(new Paragraph(text));
			document.add(p);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean printParagraph(Paragraph p) {
		try {
			document.add(p);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public boolean printHeading(String text, int size) {
		try {
			Paragraph p = new Paragraph(text, new Font(Font.TIMES_ROMAN, size, Font.BOLD));
			document.add(p);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public void printTable(JTable table, String caption) {
		try {
			tableCnt++;

			Paragraph p = new Paragraph("Table " + tableCnt + ":" + caption + "\n", new Font(Font.HELVETICA, Font.DEFAULTSIZE, Font.BOLD));
			p.setAlignment(Paragraph.ALIGN_CENTER);
			document.add(p);

			PdfPTable ptable = new PdfPTable(table.getColumnCount());
			for (int j = 0; j < table.getColumnCount(); j++) {
				ptable.addCell(new Paragraph(table.getColumnName(j), new Font(Font.COURIER, 9, Font.BOLD)));
			}

			Object obj = null;
			for (int i = 0; i < table.getRowCount(); i++) {
				for (int j = 0; j < table.getColumnCount(); j++) {
					obj = table.getValueAt(i, j);

					if (obj == null) {
						ptable.addCell("");
					} else {
						if (obj instanceof Date) {
							ptable.addCell(new Paragraph(((Date) obj).toString(), new Font(Font.COURIER, 9, Font.NORMAL)));
						} else {
							ptable.addCell(new Paragraph(obj.toString(), new Font(Font.COURIER, 9, Font.NORMAL)));
						}
					}
				}
			}

			document.add(ptable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean printTitle(String text) {
		try {
			Paragraph p = new Paragraph(text, new Font(Font.TIMES_ROMAN, 36, Font.BOLD));
			p.setAlignment(Paragraph.ALIGN_CENTER);
			document.add(p);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public void printFigure(ImageObject im, String caption) {
		printFigures(new ImageObject[] { im }, caption, -1);
	}

	public void printFigures(ImageObject[] im, String caption) {
		printFigures(im, caption, -1);
	}

	public void printFigure(ImageObject im, String caption, int width) {
		printFigures(new ImageObject[] { im }, caption, width);
	}

	public void printFigures(ImageObject[] im, String caption, int width) {

		figureCnt++;
		for (int i = 0; i < im.length; i++) {

			java.awt.Image jim = im[i].makeBufferedImage();

			try {

				Image pdfIm = Image.getInstance(jim, null);
				pdfIm.setAlignment(Image.ALIGN_CENTER);
				if ((im[i].getNumCols() > width) && (width > 0)) {
					pdfIm.scalePercent(100 * (float) width / im[i].getNumCols());

				}
				document.add(pdfIm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (caption != null) {
			Paragraph p = new Paragraph("Figure " + figureCnt + ":" + caption, new Font(Font.HELVETICA, Font.DEFAULTSIZE, Font.BOLD));
			p.setAlignment(Paragraph.ALIGN_CENTER);
			try {
				document.add(p);

			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	static public void main(String[] args) {

		FileChooser file = new FileChooser();
		String filename = null;
		try {
			filename = file.showSaveDialog();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (filename == null) {
			return;
		}

		PDFWriter pdf = null;
		try {
			pdf = new PDFWriter(new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pdf.printHeading("1. Section 1", 20);

		pdf.printString("Test image");

		try {
			filename = file.showOpenDialog();
			ImageObject inputIm = ImageLoader.readImage(filename);

			pdf.printFigure(inputIm, "Figure");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pdf.closePDF();

	}
}
