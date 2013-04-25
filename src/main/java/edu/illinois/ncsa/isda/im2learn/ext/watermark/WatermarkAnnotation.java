package edu.illinois.ncsa.isda.im2learn.ext.watermark;
//WaterMark Class
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;

import edu.illinois.ncsa.isda.im2learn.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectionDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;


public class WatermarkAnnotation implements ImageAnnotation
{
	private Watermark wm;
	
	//----------------------------------------
	public WatermarkAnnotation(Watermark wm)
	{
		this.wm = wm;
	}
	public void paint(java.awt.Graphics2D g, ImagePanel imagepanel)
	{
		if (wm.getText().equals("")) {
			return;
		}
		Color oldcolor = g.getColor();
		Font oldfont = g.getFont();
		g.setFont(wm.getFont());
		int red = wm.getTextColor().getRed();
		int green = wm.getTextColor().getGreen();
		int blue = wm.getTextColor().getBlue();
		g.setColor(new Color((float)red/255, (float)green/255, (float)blue/255, (float)wm.getAlpha()));
		g.drawString(wm.getText(), wm.getX(), wm.getY());
		g.setFont(oldfont);
		g.setColor(oldcolor);
	}
}