package edu.illinois.ncsa.isda.im2learn.ext.annotation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.illinois.ncsa.isda.im2learn.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;


public class Annotation implements ImageAnnotation, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	enum AnnotationType {
		RECTANGLE, ELLIPSOID, FREEHAND, FREEHAND_CLOSED, ARROW
	}

	private AnnotationType type;
	private boolean visible;
	private ArrayList<Point> points;
	private boolean centerElllipsoid;
	private int arrowhead = 20;
	private int linethickness = 1;
	private Color color  = Color.red;
	private String text = "";
	
	public Annotation() {
		visible = false;
		type = AnnotationType.RECTANGLE;
		points = new ArrayList<Point>();
		centerElllipsoid = true;
	}
	
	public void setType(AnnotationType type) {
		this.type = type;
		clearPoints();
	}
	
	public AnnotationType getType() {
		return type;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void clearPoints() {
		points.clear();
	}
	
	public void addPoint(int x, int y) {
		addPoint(new Point(x, y));
	}
	
	public ArrayList<Point> getPoints() {
		return points;
	}
	
	public void addPoint(Point p) {
		switch(type) {
		case RECTANGLE:
		case ELLIPSOID:
		case ARROW:
			if (points.size() < 2) {
				points.add(p);
			} else {
				points.set(1, p);
			}
			break;
		default:
			points.add(p);
		}
	}
	
	public void reset() {	
		points.clear();
		arrowhead = 20;
		linethickness = 1;
		color  = Color.red;
		text = "";
	}
	
	public void setCenterEllipsoidShown(boolean b) {
		this.centerElllipsoid= b;
	}
	
	public boolean isCenterEllipsoidShown() {
		return centerElllipsoid;
	}
	
	public void setArrowHeadSize(int size) {
		this.arrowhead = size;
	}
	
	public int getArrowHeadSize() {
		return arrowhead;
	}
	
	public void setLineThickness(int t) {
		this.linethickness = t;
	}
	
	public int getLineThickness() {
		return linethickness;
	}

	public void setColor(Color c) {
		this.color = c;
	}

	public Color getColor() {
		return color;
	}
	
	public void setText(String text) {
		this.text  = text;
	}
	
	public String getText() {
		return text;
	}

	public void paint(Graphics2D g, ImagePanel imagepanel) {
		if (!visible) {
			return;			
		}
		
		Color oldcolor = g.getColor();
		Stroke oldstroke = g.getStroke();
		g.setColor(color);
		g.setStroke(new BasicStroke(linethickness));
				
		switch(type) {
		case ARROW:
			if (points.size() > 1) {
				int x1 = points.get(0).x;
				int y1 = points.get(0).y;
				int x2 = points.get(1).x;
				int y2 = points.get(1).y;
				
				// draw the line
				g.drawLine(x1, y1, x2, y2);

				// create the arrow
				int x = (x2 - x1);
				int y = (y2 - y1);
				double l = Math.sqrt(x * x + y * y);
				
				double rx = arrowhead * x / l;
				double ry = arrowhead * y / l;
				
				//g.drawLine(x1, y1, (int)(x1+rx-ry), (int)(y1+ry+rx));
				//g.drawLine(x1, y1, (int)(x1+rx+ry), (int)(y1+ry-rx));
				//g.drawLine((int)(x1+rx-ry), (int)(y1+ry+rx), (int)(x1+rx+ry), (int)(y1+ry-rx));
				g.fillPolygon(new int[]{x1, (int)(x1+rx-ry), (int)(x1+rx+ry)},
						new int[]{y1, (int)(y1+ry+rx), (int)(y1+ry-rx)}, 3);
			}
			break;
		case RECTANGLE:
			if (points.size() > 1) {
				int x1 = points.get(0).x;
				int y1 = points.get(0).y;
				int x2 = points.get(1).x;
				int y2 = points.get(1).y;
				
				int x = (x1 > x2) ? x2 : x1;
				int y = (y1 > y2) ? y2 : y1;
				int w = Math.abs(x1 - x2);
				int h = Math.abs(y1 - y2);
				g.drawRect(x, y, w, h);
			}
			break;
		case ELLIPSOID:
			if (points.size() > 1) {
				int x = points.get(0).x;
				int y = points.get(0).y;
				int w = Math.abs(points.get(1).x - x);
				int h = Math.abs(points.get(1).y - y);
				if (centerElllipsoid) {
					g.drawLine(x-5, y, x+5, y);
					g.drawLine(x, y-5, x, y+5);
				}
				g.drawOval(x - w, y - h, w*2, h*2);
			}
			break;
		case FREEHAND_CLOSED:
			if (points.size() > 1) {
				int i = points.size() - 1;
				g.drawLine(points.get(i).x, points.get(i).y, points.get(0).x, points.get(0).y);
			}
			// fall through
		case FREEHAND:
			for(int i=1; i<points.size(); i++) {
				g.drawLine(points.get(i-1).x, points.get(i-1).y, points.get(i).x, points.get(i).y);
			}
			break;
		}
		
		g.setStroke(oldstroke);
		g.setColor(oldcolor);
	}
	
	public static Annotation buildAnnotationObject(String s) {
		s = s.replaceAll(" ", "");
		Pattern p = Pattern.compile("\\[[^a-zA-Z]*\\]");
		Matcher m = p.matcher(s);
		m.find();
		String coords = m.group();
		s = m.replaceAll("");
		
		p = Pattern.compile("\\[[0-9rgb=,]*\\]");
		m = p.matcher(s);
		m.find();
		String rgb = m.group();
		s = m.replaceAll("");
		
//		System.out.println(coords);
//		System.out.println(rgb);
//		System.out.println(s);
		
		String[] properties = s.split(",");
		
		Annotation retAnn = new Annotation();
		if(properties[0].equalsIgnoreCase("RECTANGLE")) {
			retAnn.setType(AnnotationType.RECTANGLE);
		}
		else if(properties[0].equalsIgnoreCase("ELLIPSOID")) {
			retAnn.setType(AnnotationType.ELLIPSOID);
		}
		else if(properties[0].equalsIgnoreCase("ARROW")) {
			retAnn.setType(AnnotationType.ARROW);
		}
		else if(properties[0].equalsIgnoreCase("FREEHAND")) {
			retAnn.setType(AnnotationType.FREEHAND);
		}
		else if(properties[0].equalsIgnoreCase("FREEHAND_CLOSED")) {
			retAnn.setType(AnnotationType.FREEHAND_CLOSED);
		}
		else {
		}

		//set coordinates
	
		int idx = 0;
		String coord;
		String[] c;
		while(true) {
			p = Pattern.compile("\\([0-9,]*\\)");
			m = p.matcher(coords);
			if(!m.find(idx))
				break;
			coord = m.group();
			
			coord = coord.substring(1, coord.length()-1);
			c = coord.split(",");
			retAnn.addPoint(Integer.parseInt(c[0]), Integer.parseInt(c[1]));
			idx = m.end();
		}
		
		retAnn.setArrowHeadSize(Integer.parseInt(properties[2]));
		retAnn.setLineThickness(Integer.parseInt(properties[3]));
		
		// set color
		rgb = rgb.substring(1, rgb.length()-1);
		String[] rgbs = rgb.split(",");
		Color color = new Color(Integer.parseInt(rgbs[0].split("=")[1]),
							Integer.parseInt(rgbs[1].split("=")[1]),
							Integer.parseInt(rgbs[2].split("=")[1]));
		
		retAnn.setColor(color);
		
		if(properties.length > 5)
			retAnn.setText(properties[5]);
		else {
			retAnn.setText("");
		}
		
		
		return retAnn;
	}
	
	public String toString() {
		String result = type + ", [";
		for(Point p : points) {
			result += "(" + p.x + ", " + p.y + "),";
		}
		result = points.size()>0?result.substring(0,result.length()-1):result;
		return result + "], " + arrowhead +", "+linethickness+", "+color.toString()+", "+text;
	}
}
