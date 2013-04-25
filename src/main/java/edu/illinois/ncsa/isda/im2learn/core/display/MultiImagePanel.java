package edu.illinois.ncsa.isda.imagetools.core.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;


public class MultiImagePanel extends JPanel implements ImageUpdateListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(MultiImagePanel.class);
	
	private ArrayList<ImagePreview> imagelist;
	private JPanel list;

	private JScrollPane sp;
	private double listw = -1;

	private ImagePanel imagepanel;
	
	public MultiImagePanel(ImagePanel imagepanel) {
		super(new BorderLayout());
		this.imagepanel = imagepanel;
		this.imagepanel.addImageUpdateListener(this);
		
		list = new JPanel(new VerticalLayout());
		
		sp = new JScrollPane(list);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(sp, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, BorderLayout.SOUTH);
		
		buttons.add(new JButton(new AbstractAction("Clear") {
			public void actionPerformed(ActionEvent arg0) {
				MultiImagePanel.this.removeAll();
			}
		}));
		
		imagelist = new ArrayList<ImagePreview>();
		setPreferredSize(new Dimension(210, 300));
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				double w = sp.getViewport().getWidth() - 4;
				if ((w < 1) || (w == listw)) {
					return;
				}
				listw = w;
				for(ImagePreview p : imagelist) {
					try {
						p.reload();
					} catch (IOException e1) {
						log.debug("Could not resize image", e1);
					}
				}
			}
		});
	}
	
	public void imageUpdated(ImageUpdateEvent event) {
		if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
			try {
				ImageObject imgobj = (ImageObject)event.getObject();	
				if ((imgobj == null) || (imgobj.getProperty("_MIP") != null)) {
					return;
				}
				imgobj.setProperty("_MIP", imgobj.hashCode());
				for(ImagePreview p : imagelist) {
					p.setSelected(false);
				}
				ImagePreview ip = new ImagePreview(imgobj);
				imagelist.add(ip);
				list.add(ip);					
			} catch (IOException e) {
				log.warn("Could not add image to list.", e);
			} catch (IllegalArgumentException e) {
				log.warn("Could not add image to list.", e);
			} catch (ImageException e) {
				log.warn("Could not add image to list.", e);
			}
		}
	}
		
	public void removeAll() {
		for(Iterator<ImagePreview> iter=imagelist.iterator(); iter.hasNext(); ) {
			ImagePreview p = iter.next();
			if (!p.isSelected()) {
				iter.remove();
				list.remove(p);
			}
		}
		list.revalidate();
		list.repaint();
	}
	
	private void showImage(ImagePreview ip) {
		for(ImagePreview p : imagelist) {
			if (p == ip) {
				try {
					imagepanel.setImageObject(p.getImageObject());
					p.setSelected(true);
				} catch (IOException e) {
					p.setSelected(false);
					log.warn("Could not select image to panel.", e);
				}
			} else {
				p.setSelected(false);
			}
		}
	}
		
	class ImagePreview extends JPanel {
		private JLabel image;
		private JLabel title;
		private File   cache;
		private BufferedImage bi;
		private String filename;
		private boolean selected;
		
		public ImagePreview(ImageObject imgobj) throws IOException, IllegalArgumentException, ImageException {
			super(new BorderLayout());
			setSelected(true);
			
			// add the click listener
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					MultiImagePanel.this.showImage(ImagePreview.this);
				}				
			});
			
			// create the image
			double scx = 200.0 / imgobj.getNumCols();
			double scy = 200.0 / imgobj.getNumRows();	
			if (scy < scx) {
				scx = scy;
			}
			bi = imgobj.scale(scx, scx, true).makeBufferedImage();

			// label for the image
			image = new JLabel("", JLabel.CENTER);
			add(image, BorderLayout.CENTER);
			
			// add the title
			title = new JLabel("", JLabel.CENTER);
			add(title, BorderLayout.SOUTH);
			
			// cache image
			cache = File.createTempFile("im2learn", ".tmp");
			filename = imgobj.getFileName();
			if ((filename == null) || (filename.length() == 0)) {
				filename = "UNKNOWN";
			}
			save(imgobj);
			
			// setup icon/text
			refresh(imgobj);
		}
		
		public void dispose() {
			removeAll();
			cache.delete();
			bi = null;
			image = null;
			title = null;
		}
		
		public void refresh(ImageObject imgobj) throws IllegalArgumentException, ImageException {
			// calculate the image
			int width = bi.getWidth();
			int height = bi.getHeight();
			double scx = listw / width;
			double scy = listw / height;
//			if ((scx < 1) || (scy < 1)) {
				if (scy < scx) {
					width  *= scy;
					height *= scy;
				} else {
					width  *= scx;
					height *= scx;
				}
//			}
			image.setIcon(new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_FAST)));
			
			// calculate the filename
			int len = filename.length() - 1;
			do {
				len--;
				title.setText(filename.substring(len));
			} while((len > 0) && (title.getPreferredSize().getWidth() < listw));
			if (title.getPreferredSize().getWidth() > listw) {
				title.setText(filename.substring(len+1));
			}
		}
		
		public void reload() throws IOException {
			ImageObject imgobj = getImageObject();
			try {
				refresh(imgobj);
			} catch (IllegalArgumentException e) {
				log.debug("Could not refresh image.", e);
				throw(new IOException(e.toString()));
			} catch (ImageException e) {
				log.debug("Could not refresh image.", e);
				throw(new IOException(e.toString()));
			}
		}
		
		public ImageObject getImageObject() throws IOException {
			try {
				return load();
			} catch (ClassNotFoundException e) {
				log.debug("Could not load image.", e);
				throw(new IOException(e.toString()));
			}
		}
		
		public void setSelected(boolean b) {
			selected = b;
			if (selected) {
				setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			} else {
				setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));				
			}
		}
		
		public boolean isSelected() {
			return selected;
		}
		
		private void save(ImageObject imgobj) throws IOException {
			HashMap<String, Object> props = imgobj.getProperties();
			imgobj.setProperties(null);			
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
			try {
				oos.writeObject(imgobj);
	            oos.writeInt(props.keySet().size());
	            for (String key : props.keySet()) {
	                oos.writeUTF(key);
	                oos.writeObject(props.get(key));
	            }
				oos.close();
			} finally {
				imgobj.setProperties(props);
			}
		}
		
		private ImageObject load() throws IOException, ClassNotFoundException {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
			ImageObject imgobj = (ImageObject)ois.readObject();
            int count = ois.readInt();
            for (int i = 0; i < count; i++) {
                String key = ois.readUTF();
                imgobj.setProperty(key, ois.readObject());
            }
			ois.close();
			return imgobj;
		}
	}
}
