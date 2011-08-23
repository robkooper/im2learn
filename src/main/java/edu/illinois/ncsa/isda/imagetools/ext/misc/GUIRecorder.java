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
/**
 * 
 */
package edu.illinois.ncsa.isda.imagetools.ext.misc;


import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.StringTokenizer;

import javax.swing.*;

import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;


/**
 * @author sclee
 *
 */
public class GUIRecorder extends JFrame implements ActionListener, MouseMotionListener, MouseListener, KeyListener {

	protected JFrame _GUIFrame;
	protected JButton bt_start, bt_stop, bt_play, bt_save, bt_load, bt_exit;
	protected JTextField tf_input;
	protected Point _frameLocation;
	protected Dimension _frameSize;
	protected Dimension _screenSize;
	
	protected eventArray _logger = new eventArray();
	protected Robot robot;
	
	int _drag_button = 0;
	protected int _x, _y;
	protected long _eventId = 0;
	
	protected boolean _recording = false, _stop = false;
	
	
	protected boolean _updated = false;//, _eventCall = false;
	Thread player;
	long time = 0;
	long timeout = 50000;
	//long lastTime;
	
	
	public GUIRecorder(JFrame GUIFrame) {
		_screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		createControl();
		
		setFrame(GUIFrame);
//		_GUIFrame = GUIFrame;
//		_addListener(_GUIFrame);
		
		//_GUIFrame.addKeyListener(this);
		_add(_GUIFrame);

	
	}
		
	public GUIRecorder() {
		_screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		createControl();
//				
//		_GUIFrame = _controlF;
//		addListener(_GUIFrame);
	
	}
	
	/*
	
	void startTimer() {
		lastTime = System.currentTimeMillis();
		timer = new Thread(new Runnable() {
            public void run() {
            	for (;;) {
            		
            		time = System.currentTimeMillis() - lastTime;
            		if(time > timeout) {
            			tf_input.setText(_eventId+": DELAY");

            			_logger.logger.add(new event());
            			_eventId++;
            			lastTime = System.currentTimeMillis();
            		}
            	}
            }
        });
		timer.start();
	}
	
	
	void stopTimer() {
		timer.interrupt();
		timer = null;
	}
	
	*/
	public void setFrame(JFrame f) {
		_GUIFrame = f;
		_addListener(_GUIFrame);
		
		
		_GUIFrame.addComponentListener(new ComponentListener() {
			public void componentMoved(ComponentEvent arg0) {
				if(_recording) {
					event e = _logger.logger.get(0);
					if(e.evtType == -1 &&
						e.button == 999) {
							
						Point loc = _GUIFrame.getLocationOnScreen();
						
						if(loc.x == e.absx && loc.y == e.absy) 
							return;
						JOptionPane.showConfirmDialog(null,"Window Movement Detected.\nWindow is not allowed to be moved during recording events.\nPress OK to Continue",
								"Window Move", JOptionPane.PLAIN_MESSAGE);
						_GUIFrame.setLocation(e.absx,e.absy);
						
					}
					else {
						System.err.println("Cannot recover");
						return;
					}
				}
			}
			public void componentHidden(ComponentEvent arg0) {}
			public void componentResized(ComponentEvent arg0) {}
			public void componentShown(ComponentEvent arg0) {}}); 

		addWindowListener(new WindowListener() {

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowClosed(WindowEvent arg0) {
				_GUIFrame.dispose();
			}

			public void windowClosing(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowOpened(WindowEvent arg0) {
			}
           
        });
		
		
	}
	
	
	
	protected void _addListener(Container c) {
		if(c == null)
			return;
		if(c.getComponentCount() == 0)
			return;
		
		Component comp;
		for(int i=0; i<c.getComponentCount(); i++) {
			
			comp = c.getComponent(i); 
			_add(comp);

			if(comp instanceof JMenuBar) {
				addMenuBarListner((JMenuBar)comp);
			} else if(comp instanceof ImagePanel) {
				addPopupMenuListener(((ImagePanel)comp).getPopupMenu());
			} else if(comp instanceof JMenu) { 
				addMenuListener((JMenu)comp);
			} else {			
				_addListener((Container)comp);
			}
		}
		
		return;
	}
	
	
	protected void addMenuBarListner(JMenuBar mb) {
		mb.addMouseListener(this);
		mb.addKeyListener(this);
		mb.addMouseMotionListener(this);
		
		JMenu menu;
		for(int i=0; i<mb.getMenuCount(); i++) {
			menu = mb.getMenu(i);
			_add(menu);
			addMenuListener(menu);
		}
	}
	
	protected void addMenuListener(JMenu menu) {
		JComponent mi = null;
		for(int j=0; j<menu.getMenuComponentCount(); j++) {
			mi = (JComponent)menu.getMenuComponent(j);
			
			if(mi instanceof JMenu) {
				mi.addMouseListener(this);
				mi.addKeyListener(this);
				mi.addMouseMotionListener(this);
				addMenuListener((JMenu)mi);
			}
			else { //if(mi instanceof JMenuItem) {
				mi.addMouseListener(this);
				mi.addKeyListener(this);
				mi.addMouseMotionListener(this);
			}
			
		}

	}

	protected void addPopupMenuListener(JPopupMenu menu) {
		JComponent mi = null;
		for(int j=0; j<menu.getComponentCount(); j++) {
			mi = (JComponent)menu.getComponent(j);
			if(mi instanceof JMenu) {
				mi.addMouseListener(this);
				mi.addMouseMotionListener(this);
				mi.addKeyListener(this);
				addMenuListener((JMenu)mi);
			}
			else { //if(mi instanceof JMenuItem) {
				mi.addMouseListener(this);
				mi.addMouseMotionListener(this);
				mi.addKeyListener(this);
			}
		
		}
	}
	
	
	protected void _add(JMenu m) {
		m.addMouseListener(this);
		m.addMouseMotionListener(this);
		m.addKeyListener(this);
		for(int i=0; i<m.getComponentCount(); i++) {
			_add(m.getMenuComponent(i));
		}
	}
	
	protected void _add(Component c) {
		c.addMouseListener(this);
		c.addMouseMotionListener(this);
		c.addKeyListener(this);
	}
	
	
	
	
	
	
	protected void createControl() {
		
		
		
		
		bt_start = new JButton("Start");
		bt_stop = new JButton("Stop");
		bt_play = new JButton("Play");
		bt_save = new JButton("Save");
		bt_load = new JButton("Load");
		bt_exit = new JButton("Exit");
		
		
		tf_input = new JTextField();
				
		bt_start.addActionListener(this);
		bt_stop.addActionListener(this);
		bt_play.addActionListener(this);
		bt_save.addActionListener(this);
		bt_load.addActionListener(this);
		bt_exit.addActionListener(this);
		
		JPanel info = new JPanel(new GridLayout(2,1));
		JPanel button = new JPanel();
		info.add(new JLabel("GUI Recoder: press 'Start' to record, 'Stop' to finish, and 'Play' to play (Press " +
				"'F12' to stop)"));
		info.add(tf_input);
		button.add(bt_start);
		button.add(bt_stop);
		button.add(bt_play);
		button.add(bt_load);
		button.add(bt_save);
		button.add(bt_exit);
		
		this.setTitle("GUI Recorder");
		add(info,BorderLayout.NORTH);
		add(button,BorderLayout.SOUTH);
		pack();			
		//_controlF.setLocation(_screenSize.width-_controlF.getWidth(), _screenSize.height- _controlF.getHeight());
		//setSize(new Dimension(_screenSize.width,getHeight()));
		setLocation(0, _screenSize.height- getHeight()-28);
		setAlwaysOnTop(true);
		setVisible(true);
		
	}

	
	
	
	protected void printEvents() {
		event e;
		for (int i=0; i<_logger.logger.size(); i++) {
			e = _logger.logger.get(i);
			System.out.println(i+"   "+e.evtType+"\t"+e.absx+"\t"+
					e.absy+"\t"+e.button);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == bt_start) {
			_updated = true;
			_logger = null;
			_logger = new eventArray();
			_recording = true;
			_GUIFrame.setResizable(false);

			setTitle("GUI Recorder*");
		//	startTimer();
			_frameLocation = _GUIFrame.getLocationOnScreen();
			_frameSize = _GUIFrame.getSize();
			tf_input.setText("Window Dimension: ["+ (int)_frameLocation.getX()+","+(int)_frameLocation.getY()+","+(int)_frameSize.getWidth()+","+(int)_frameSize.getHeight()+"]");
			_logger.logger.add(new event(event.META, (int)_frameLocation.getX(), (int)_frameLocation.getY(), 999));
			_logger.logger.add(new event(event.META, (int)_frameSize.getWidth(), (int)_frameSize.getHeight(), 888));
			this._eventId++;
		}
		else if(e.getSource() == bt_stop) {
			_recording = false;
			_GUIFrame.setResizable(true);
			//	stopTimer();
		}
		else if(e.getSource() == bt_save) {
			JFileChooser file = new JFileChooser();
		//	file.addSaveFilter(new String[] {"log"},"Log file for GUIRecorder");
			try{
				file.showSaveDialog(this);
				String filename = file.getSelectedFile().getAbsolutePath(); 
				if(filename != null) {
					_logger.save(filename);
					_updated = false;
					setTitle("GUI Recorder");
				}
			}
			catch(Exception ee) {
				ee.printStackTrace();
			}

		}
		else if(e.getSource() == bt_load) {
			JFileChooser file = new JFileChooser();
//			file.clearOpenFilter();
//			file.addOpenFilter(new String[] {"log"},"Log file for GUIRecorder");
			try{
				file.showOpenDialog(this);
				
			//	String filename = file.showOpenDialog();
				String filename = file.getSelectedFile().getAbsolutePath();	

//				String filename = 
	//				"C:\\Documents and Settings\\sclee\\My Documents\\data\\NARA\\sim02.log";
				
				if(filename != null)
					_logger.load(filename);
				
			}
			catch(Exception ee) {
				//ee.printStackTrace();
			}
			
			
		}
		else if(e.getSource() == bt_play) {
			//printEvents();
			playEvents();
		}
		else if(e.getSource() == bt_exit) {
			if(_updated) {
				int res = JOptionPane.showConfirmDialog(this, "Record is not saved. Save to click Yes.",
						"Save", JOptionPane.YES_NO_CANCEL_OPTION);
				if(res == JOptionPane.YES_OPTION) {
					FileChooser file = new FileChooser();
					file.clearSaveFilter();
					file.addSaveFilter(new String[] {"log"},"Log file for GUIRecorder");
					try{
						_logger.save(file.showSaveDialog());
					}
					catch(Exception ee) {
						ee.printStackTrace();
					}
					_updated = false;
				}
				else if(res == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			
			this.dispose();

		}	
		
	}

	

	
	
	protected void playEvents() {
		System.gc();
		player = new Thread(new Runnable() {
			public void run() {
				boolean init = true;
				
				_stop = false;
				try{
					robot = new Robot();
					robot.setAutoWaitForIdle(true);
					robot.setAutoDelay(10);
				}
				catch(Exception e) {}
				
				Object[] eArr = _logger.logger.toArray();
				event e, e0;
						
				if(_GUIFrame.getState() != JFrame.NORMAL 				   
				) {
					int ans = JOptionPane.showConfirmDialog(null,"The Window is not shown now. Show the window?",
							"Window Show", JOptionPane.YES_NO_OPTION);
					if(ans == JOptionPane.YES_OPTION) {
						_GUIFrame.setState(JFrame.NORMAL);
						JOptionPane.showConfirmDialog(null,"Press OK to Continue",
								"Window Show", JOptionPane.PLAIN_MESSAGE);
					}
				}
				
				
				for(int i=0; i<eArr.length; i++) {
					if(_stop) {
						break;
					}
					e = (event)eArr[i];
					
					if(i > 0) {
						e0 = (event)eArr[i-1];
						if(e.absx != e0.absx ||
								e.absy != e0.absy ||
								e.button != e0.button ||
								e.evtType != e0.evtType) {}
						else {
							continue;	
						}
					}
					switch (e.evtType) {
					case event.META:
						if(e.button == 999) {
							Point loc = _GUIFrame.getLocationOnScreen();
							if(loc.getX() != e.absx ||
							   loc.getY() != e.absy) {
								int ans = JOptionPane.showConfirmDialog(null,"The Window location does not match. Move the window?",
										"Window Move", JOptionPane.YES_NO_OPTION);
								if(ans == JOptionPane.YES_OPTION) {
									_GUIFrame.setLocation(e.absx, e.absy);
									JOptionPane.showConfirmDialog(null,"Press OK to Continue",
											"Window Move", JOptionPane.PLAIN_MESSAGE);
								}
								else {
									_stop = true;
								/*	ans = JOptionPane.showConfirmDialog(null,"The Application might not work proprely. Continue?",
											"Window Move", JOptionPane.YES_NO_OPTION);
									if(ans == JOptionPane.NO_OPTION) {
										_stop = true;
									}
									else {
										JOptionPane.showConfirmDialog(null,"Press OK to Continue",
												"Window Move", JOptionPane.PLAIN_MESSAGE);
									}*/
								}
							}
						}
						
						if(e.button == 888) {
							Dimension size = _GUIFrame.getSize();
							if(size.getWidth() != e.absx ||
							   size.getHeight() != e.absy) {
								int ans = JOptionPane.showConfirmDialog(null,"The Window Size does not match. Adjust the window size?",
										"Window Adjust", JOptionPane.YES_NO_OPTION);
								if(ans == JOptionPane.YES_OPTION) {
									_GUIFrame.setPreferredSize(new Dimension(e.absx, e.absy));
									_GUIFrame.pack();
									JOptionPane.showConfirmDialog(null,"Press OK to Continue",
											"Window Adjust", JOptionPane.PLAIN_MESSAGE);
								}
								else {
									_stop = true;
									/*ans = JOptionPane.showConfirmDialog(null,"The Application might not work proprely. Continue?",
											"Window Adjust", JOptionPane.YES_NO_OPTION);
									if(ans == JOptionPane.NO_OPTION) {
										_stop = true;
									}
									else {
										JOptionPane.showConfirmDialog(null,"Press OK to Continue",
												"Window Adjust", JOptionPane.PLAIN_MESSAGE);
									}*/
								}
							}
						}
						
						break;
					case event.NONE:
					//	_drag_button = 0;
						//tf_input.setText(i+": DELAY ("+timeout+"ms)");
						try{
						//	robot.delay((int)timeout);
						}
						catch(Exception ee) {
						}
						break;
					case event.MOVE:
						_GUIFrame.getComponentAt(e.absx, e.absy);
						_drag_button = 0;
						try{
							robot.mouseMove(e.absx, e.absy);
							tf_input.setText(i+": MOVE\t["+e.absx+","+e.absy+"]"+e.button);
						}
						catch(Exception ee){}
						break;
					case event.DRAG:
						if(_drag_button == 0) {
							switch(e.button) {
							case MouseEvent.BUTTON1: 
								robot.mousePress(InputEvent.BUTTON1_MASK);
								break;
							case MouseEvent.BUTTON2:
								robot.mousePress(InputEvent.BUTTON2_MASK);
								break;
							case MouseEvent.BUTTON3: 
								robot.mousePress(InputEvent.BUTTON3_MASK);
								break;
							}
						}
						tf_input.setText(i+": DRAG\t["+e.absx+","+e.absy+"]"+e.button);
						robot.mouseMove(e.absx, e.absy);
						robot.waitForIdle();
						break;
					case event.PRESS:
						_drag_button = 0;
//						_eventCall = false;
						
						switch(e.button) {
						case MouseEvent.BUTTON1: 
							robot.mousePress(InputEvent.BUTTON1_MASK);
							break;
						case MouseEvent.BUTTON2:
							robot.mousePress(InputEvent.BUTTON2_MASK);
							break;
						case MouseEvent.BUTTON3: 
							robot.mousePress(InputEvent.BUTTON3_MASK);
							break;
						}
						
						if(e.absx < _frameLocation.x ||
						   e.absx > _frameLocation.x + _frameSize.width ||
						   e.absx > _screenSize.width ||
						   e.absy < _frameLocation.y ||
						   e.absy > _frameLocation.y + _frameSize.height ||
						   e.absy > _screenSize.height) {

							_stop = true;
							JOptionPane.showConfirmDialog(null,"The window is not shown correctly.\n Emergency Stop.",
									"Window Show", JOptionPane.PLAIN_MESSAGE);

						}
						
						if(init && !_GUIFrame.isFocused()) {
							_stop = true;
							JOptionPane.showConfirmDialog(null,"The window is not shown correctly. Try again after hiding other windows",
										"Window Show", JOptionPane.PLAIN_MESSAGE);
						}
						init = false;
//						if(_eventCall == false)
//							player.stop();
						tf_input.setText(i+": PRESS\t["+e.absx+","+e.absy+"]"+e.button);
					
						break;
					case event.RELEASE:
						_drag_button = 0;
						switch(e.button) {
						case MouseEvent.BUTTON1: 
							robot.mouseRelease(InputEvent.BUTTON1_MASK);
							break;
						case MouseEvent.BUTTON2: 
							robot.mouseRelease(InputEvent.BUTTON2_MASK);
							break;
						case MouseEvent.BUTTON3: 
							robot.mouseRelease(InputEvent.BUTTON3_MASK);
							break;
						}
						robot.delay(100);
						tf_input.setText(i+": RELEASE\t["+e.absx+","+e.absy+"]"+e.button);
											
						break;
					case event.KEY_PRESSED:
						robot.keyPress(e.button);
						tf_input.setText(i+": KEY PRESSED\t["+e.absx+","+e.absy+"]"+e.button);
						System.out.println(tf_input.getText());
						break;
				
					case event.KEY_RELEASED:
						robot.keyRelease(e.button);
						tf_input.setText(i+": KEY RELEASED\t["+e.absx+","+e.absy+"]"+e.button);
						System.out.println(tf_input.getText());
						break;
					}

					
//					robot.delay(10);
				//	robot.waitForIdle();
				}

			}		
	        });
		player.start();
	}
	
	
	public void mouseMoved(MouseEvent e) {
		//System.out.println("MOVE "+e.getComponent());
		if(_recording) {
			try{
				Point p = e.getComponent().getLocationOnScreen();
				_x = e.getX() + (int)p.getX(); 
				_y = e.getY() + (int)p.getY();
			}
			catch(Exception ee) {
			//	ee.printStackTrace();
			}
						
			tf_input.setText(_eventId+": Move\t"+e.getX()+'\t'+e.getY()+"\t ["+
					_x+","+_y+"]");

			_logger.logger.add(new event(event.MOVE, _x, _y, -1));
			_eventId++;
		}
	//	lastTime = System.currentTimeMillis();				
	}

	public void mousePressed(MouseEvent e) {
		//System.out.println("PRESS "+e.getComponent());
		//_eventCall = true;
		if(_recording) {
			
			try{
				Point p = e.getComponent().getLocationOnScreen();
				_x = e.getX() + (int)p.getX(); 
				_y = e.getY() + (int)p.getY();
			}
			catch(Exception ee) {
			//	ee.printStackTrace();
			}
					
			_drag_button = e.getButton();
			
			tf_input.setText(_eventId+": Pressed\t"+e.getX()+'\t'+e.getY()+'\t'+e.getButton()+"\t ["+
					_x+","+_y+"]");
			_logger.logger.add(new event(event.PRESS, _x, _y, _drag_button));
			_eventId++;
			
			// temporary fix for JAVA bug ID 6222765
			Component c = e.getComponent();
			if(e.getButton() == 1 &&
					c instanceof JMenuItem && (
					((JMenuItem)c).getText().startsWith("Open") ||
					((JMenuItem)c).getText().startsWith("Exit") ||
					((JMenuItem)c).getText().startsWith("About") 
					)
			) {
				mouseReleased(e);
			}
		}
		//lastTime = System.currentTimeMillis();
	}

	public void mouseReleased(MouseEvent e) {
		//System.out.println(e);
		if(_recording) {
			
			if(_drag_button < 0) {
				return;
			}
			
			try{
				Point p = e.getComponent().getLocationOnScreen();
				_x = e.getX() + (int)p.getX(); 
				_y = e.getY() + (int)p.getY();
			}
			catch(Exception ee) {
			//	ee.printStackTrace();
			}
		
			
			_drag_button = -1;
			
			tf_input.setText(_eventId+": Released\t"+e.getX()+'\t'+e.getY()+"\t ["+
					_x+","+_y+"]");
			
			_logger.logger.add(new event(event.RELEASE, _x, _y, e.getButton()));
			_eventId++;
		}
		//lastTime = System.currentTimeMillis();
	}

	public void mouseDragged(MouseEvent e) {
		// System.out.println("DRAG "+e.getComponent());
		if(_recording) {
			try{
				Point p = e.getComponent().getLocationOnScreen();
				_x = e.getX() + (int)p.getX(); 
				_y = e.getY() + (int)p.getY();
			}
			catch(Exception ee) {
				//ee.printStackTrace();
			}
		
			_drag_button = e.getButton();
			
			tf_input.setText(_eventId+": Drag\t"+e.getX()+'\t'+e.getY()+'\t'+_drag_button+"\t ["+
					_x+","+_y+"]");
			_logger.logger.add(new event(event.DRAG, _x, _y, _drag_button));
			_eventId++;
			//lastTime = System.currentTimeMillis();
		}
				

	}

	
	
	
	public void mouseEntered(MouseEvent e) {
	//	System.out.println("ENTER"+e.getComponent());
	}
	public void mouseExited(MouseEvent e) {
//		System.out.println("EXIT"+e.getComponent());
	}
	public void mouseClicked(MouseEvent e) {
		if(_drag_button >= 0) {
			mouseReleased(e);
		}
		//System.out.println("CLICK"+e.getComponent());
		time = 0;
	}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_F12) {
			if(!_stop && player != null && player.isAlive()) {
				_stop = true;
				player.interrupt();
				JOptionPane.showMessageDialog(this,"Simulation is stopped.");
			}
		}
		
		if(_recording) {
			_drag_button = e.getKeyCode();
			tf_input.setText("KEY PRESSED: "+String.valueOf(e.getKeyChar())+"("+_drag_button+")"+" ["+_x+","+_y+"]");
			_logger.logger.add(new event(event.KEY_PRESSED, _x, _y, _drag_button));
			_eventId++;
		}
		
			
	}
	public void keyReleased(KeyEvent e) {
		if(_recording) {
			_drag_button = e.getKeyCode();
			tf_input.setText("KEY RELEASED: "+String.valueOf(e.getKeyChar())+"("+_drag_button+")"+" ["+_x+","+_y+"]");
			_logger.logger.add(new event(event.KEY_RELEASED, _x, _y, _drag_button));
			_eventId++;
		}
	}

	public void keyTyped(KeyEvent e) {
		
		//	lastTime = System.currentTimeMillis();
	}
	
	protected class event{
		int evtType;
		int absx, absy;
		int button;
		long timestamp;

		protected static final int META = -1, NONE = 0, MOVE=1, DRAG=2, PRESS=3, RELEASE=4, KEY_PRESSED = 5, KEY_RELEASED = 6;
		
		event() {
			evtType = NONE;
			absx = 0;
			absy = 0;
			button = 0;
			timestamp = System.currentTimeMillis();
		}
		
		event(int evt, int x, int y, int b) {
			evtType = evt;
			absx = x;
			absy = y;
			button = b;
			timestamp = System.currentTimeMillis();
		}
	
		event(int evt, int x, int y, int b, long t) {
			evtType = evt;
			absx = x;
			absy = y;
			button = b;
			timestamp = t;
		}
		
	}
	

	public class eventArray {
		public ArrayList<event> logger;
		public eventArray() {
			logger = new ArrayList<event>();			
		}
		public boolean save(String filename) {
			char t = '\t';
			try{
				String buffer ="";
				String entry="";
				event e;
				for(int i=0; i<logger.size(); i++) {
					e = logger.get(i);
					entry = String.valueOf(i)+t+
					e.evtType+t+e.absx+t+ e.absy+t+e.button+t+e.timestamp+'\n';
					buffer += entry;
				}
				
				writeString(filename,buffer,false);
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;	
			}
		}
		
		public boolean load(String filename) {
			logger = new ArrayList<event>();
			try{
				if(filename == null)
					return false;
				
				FileInputStream fin = new FileInputStream(filename);
				DataInputStream din = new DataInputStream(fin);

				BufferedReader d = new BufferedReader(new InputStreamReader(din));
				String in; 
				int e,x,y,b;
				long t;
				String[] tokens;
				
				while((in = d.readLine()) != null) {
					tokens = in.split("\t");
					if(tokens.length != 6)
						continue;
					e = Integer.parseInt(tokens[1]);
					x = Integer.parseInt(tokens[2]);
					y = Integer.parseInt(tokens[3]);
					b = Integer.parseInt(tokens[4]);
					t = Long.parseLong(tokens[5]);
					logger.add(new event(e,x,y,b,t));
				}

				if(fin != null)
					fin.close();
				return true;

			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
				
		}
				  
		boolean writeString(String filename, String data, boolean append) {
			try{
				if(filename == null)
					return false;
		
				FileWriter out = new FileWriter(filename,append);
				out.write(data);
				out.write("\n");
				if(out != null)
					out.close();

				out = null;
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		
	}

	
}
