package edu.illinois.ncsa.isda.im2learn.core.datatype;

public class PDFFont {

	private int size;
	private String face;

	public PDFFont() {

	}

	public PDFFont(int size, String Face) {
		this.face = Face;
		this.size = size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setFace(String face) {
		this.face = face;
	}

	public int getSize() {
		return size;
	}

	public String getFace() {
		return face;
	}

}
