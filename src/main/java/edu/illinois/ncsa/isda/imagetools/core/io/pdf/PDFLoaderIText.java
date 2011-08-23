/**
 * 
 */
package edu.illinois.ncsa.isda.imagetools.core.io.pdf;

import java.io.FileOutputStream;
import java.io.IOException;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageWriter;


/**
 * @author sclee
 *
 */
public class PDFLoaderIText implements ImageWriter {
	public PDFLoaderIText() {		
	}

	/* (non-Javadoc)
	 * @see ncsa.im2learn.core.io.ImageWriter#canWrite(java.lang.String)
	 */
	public boolean canWrite(String filename) {
		return filename.toLowerCase().endsWith(".pdf");
	}


	/* (non-Javadoc)
	 * @see ncsa.im2learn.core.io.ImageWriter#getDescription()
	 */
	public String getDescription() {
		return "PDF Loader lowagie";
	}


	/* (non-Javadoc)
	 * @see ncsa.im2learn.core.io.ImageWriter#writeExt()
	 */
	public String[] writeExt() {
		// TODO Auto-generated method stub
		return new String[] {".pdf"};
	}


	/* (non-Javadoc)
	 * @see ncsa.im2learn.core.io.ImageWriter#writeImage(java.lang.String, ncsa.im2learn.core.datatype.ImageObject)
	 */
	public void writeImage(String filename, ImageObject imageobject) throws IOException, ImageException {
		FileOutputStream output = new FileOutputStream(filename);
		PDFWriter writer = new PDFWriter(output);
		writer.printFigure(imageobject, null, -1);
		writer.closePDF();
	}

}
