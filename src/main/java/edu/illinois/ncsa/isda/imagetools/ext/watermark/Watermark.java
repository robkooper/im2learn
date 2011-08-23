package edu.illinois.ncsa.isda.imagetools.ext.watermark;
//WaterMark Class
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;


public class Watermark 
{
	private float alpha;
	private int x;
	private int y;
	private Font font;
	private String text;
	private Color textColor;
	
	//----------------------------------------
	public Watermark()
	{
		this(.5f, 0, 20, 20, "", Color.BLACK);
	}
	
	public Watermark(float alpha, int x, int y, int textSize, String text, Color textColor)
	{
		this.alpha=alpha;
		this.x = x;
		this.y=y;
		this.text = text;
		this.textColor= textColor;
		this.font = new Font("Arial",Font.PLAIN,textSize);
	}
	public float getAlpha() {
		return alpha;
	}
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Color getTextColor() {
		return textColor;
	}
	
	// TODO create a color that has alpha in it
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}
	public Font getFont() {
		return font;
	}
	public void setFont(Font font) {
		this.font = font;
	}
	public void setFontSize(int textSize) {
		this.font = new Font("Arial",Font.PLAIN,textSize);;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
 	public ImageObject apply(ImageObject image) throws ImageException
 	{

		
 		int numRow = image.getNumRows();
 		int numCol = image.getNumCols();
 		int numBand = image.getNumBands();
		BufferedImage img = new BufferedImage(numCol, numRow,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		// fill background white
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, numCol, numRow);

		// draw header in red.
		g.setColor(Color.BLACK);
		g.setFont(font);
		g.drawString(text, x, y);
		ImageObject wmImage = ImageObject.getImageObject(img);
 		for(int i=0; i<numRow; i++)
 		{
 			for(int j=0; j<numCol; j++)
 			{
 				for(int k = 0; k<numBand;k++)
 				{
 					if((wmImage.getInt(i,j,0) != 255)) {
 						image.set(i, j, k, image.getDouble(i,j,k)*(1-alpha)+wmImage.getDouble(i,j,0)*alpha);
 					}
 				}
 			}
 		}
 		return image;
 	}	

 	public ImageObject apply(ImageObject image, int rb, int gb, int bb) throws ImageException
 	{

		
 		int numRow = image.getNumRows();
 		int numCol = image.getNumCols();
		BufferedImage img = new BufferedImage(numCol, numRow,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		// fill background white
		
		if(textColor.getRed() == 255 && textColor.getGreen() == 255 && textColor.getBlue() == 255){
			g.setColor(Color.RED);
			System.out.println("Color it red");
		} else {
			g.setColor(Color.WHITE);
			System.out.println("color is white");
		}
		g.fillRect(0, 0, numCol, numRow);

		// draw header in red.
		g.setColor(textColor);
		g.setFont(font);
		g.drawString(text, x, y);
		ImageObject wmImage = ImageObject.getImageObject(img);
		
		if(textColor.getRed() == 255 && textColor.getGreen() == 255 && textColor.getBlue() == 255){
			for(int i=0; i<numRow; i++){
				for(int j=0; j<numCol; j++){
					if(wmImage.getInt(i, j, 0) != 255 || wmImage.getInt(i, j, 1) != 0 || wmImage.getInt(i, j, 2) != 0){
						image.set(i, j, rb, image.getDouble(i,j,rb)*(1-alpha)+wmImage.getDouble(i,j,0)*alpha);
						image.set(i, j, gb, image.getDouble(i,j,gb)*(1-alpha)+wmImage.getDouble(i,j,1)*alpha);
						image.set(i, j, bb, image.getDouble(i,j,bb)*(1-alpha)+wmImage.getDouble(i,j,2)*alpha);
					}
				}
			}
		} else {
			for(int i=0; i<numRow; i++)
			{
				for(int j=0; j<numCol; j++)
				{
					if((wmImage.getInt(i,j,0) != 255) || (wmImage.getInt(i,j,1) != 255) || (wmImage.getInt(i,j,2) != 255)) {
						image.set(i, j, rb, image.getDouble(i,j,rb)*(1-alpha)+wmImage.getDouble(i,j,0)*alpha);
						image.set(i, j, gb, image.getDouble(i,j,gb)*(1-alpha)+wmImage.getDouble(i,j,1)*alpha);
						image.set(i, j, bb, image.getDouble(i,j,bb)*(1-alpha)+wmImage.getDouble(i,j,2)*alpha);
					}
				}	 				
			}
		}
 		return image;
 	}	

}