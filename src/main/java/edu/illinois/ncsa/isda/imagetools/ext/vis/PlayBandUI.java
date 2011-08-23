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
package edu.illinois.ncsa.isda.imagetools.ext.vis;


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageComponent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class will display the bands in the image as a sequence of frames. The
 * user can decide what speed the video needs to play back. The user can stop
 * and pause the video, as well make it loop.
 */
public class PlayBandUI extends JPanel implements Runnable {
    private ImageComponent video;
    private Thread videothread;
    private JCheckBox loop;
    private JCheckBox total;
    private long sleep = 1000;
    private String[] fpsoptions = new String[]{"1", "10", "100", "500", "1000"};
    private JComboBox fps;
    private JSlider frame;
    private JTextField counter;

    private JButton play;
    private JButton stop;
    private JToggleButton pause;

    /**
     * Constructor, will create the interface.
     */
    public PlayBandUI() {
        super(new BorderLayout());

        // video frame
        video = new ImageComponent(null);
        video.setAutozoom(true);

        // slider to control frame and counter
        JPanel slider = new JPanel(new BorderLayout());

        frame = new JSlider(0, 0, 0);
        frame.setSnapToTicks(true);
        frame.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int x = frame.getValue();
                video.setGrayBand(x);
                counter.setText(x + "");
            }
        });
        slider.add(frame, BorderLayout.CENTER);

        counter = new JTextField(4);
        counter.setHorizontalAlignment(JTextField.RIGHT);
        counter.setEditable(false);
        slider.add(counter, BorderLayout.EAST);

        // create UI for controlling the player
        JPanel buttons = getButtons();

        // loop, speed etc controls.
        JPanel misc = new JPanel(new FlowLayout());

        JLabel lbl = new JLabel("Delay between frames : ");
        misc.add(lbl);

        fps = new JComboBox(fpsoptions);
        fps.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = fps.getSelectedItem().toString();
                sleep = Long.parseLong(s);
                if (videothread != null) {
                    videothread.interrupt();
                }
            }
        });
        fps.setSelectedItem("" + sleep);
        misc.add(fps);

        loop = new JCheckBox("Loop?");
        misc.add(loop);

        total = new JCheckBox(new AbstractAction("Totals?") {
            public void actionPerformed(ActionEvent e) {
                video.setUseTotals(total.isSelected());
            }
        });
        misc.add(total);

        // create the UI
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(slider, BorderLayout.NORTH);
        panel.add(misc, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        add(video, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
    }

    public JPanel getButtons() {
        JPanel buttons = new JPanel(new FlowLayout());
        play = new JButton(new AbstractAction(" Play ") {
            public void actionPerformed(ActionEvent e) {
                startvideo();
            }
        });
        play.setEnabled(true);
        buttons.add(play);

        pause = new JToggleButton(new AbstractAction(" Pause ") {
            public void actionPerformed(ActionEvent e) {
                if (videothread != null) {
                    videothread.interrupt();
                }
            }
        });
        pause.setEnabled(false);
        buttons.add(pause);

        stop = new JButton(new AbstractAction(" Stop ") {
            public void actionPerformed(ActionEvent e) {
                stopvideo();
            }
        });
        stop.setEnabled(false);
        buttons.add(stop);

        return buttons;
    }

    public void setImageObject(ImageObject image) {
        // stop the current video
        stopvideo();

        // set image to null
        video.setImageObject(image);

        // fix display to grayscale
        video.setGrayBand(0);
        video.setGrayScale(true);

        // start at frame 0
        frame.setValue(0);
        if (image == null) {
            frame.setMaximum(0);
        } else {
            frame.setMaximum(image.getNumBands() - 1);
        }
    }

    /**
     * Start the video thread, reset buttons.
     */
    public void startvideo() {
        if (videothread != null) {
            return;
        }

        // start the video thread
        videothread = new Thread(this);
        videothread.setName("Im2Learn - PlayBand");
        videothread.start();

        // set the buttons
        play.setEnabled(false);
        pause.setSelected(false);
        pause.setEnabled(true);
        stop.setEnabled(true);
    }

    /**
     * Stop the video thread, reset buttons.
     */
    public void stopvideo() {
        if (videothread == null) {
            return;
        }

        // wait for video thread to die
        Thread tmp = videothread;
        videothread = null;
        tmp.interrupt();
        if (tmp != Thread.currentThread()) {
            try {
                tmp.join();
            } catch (InterruptedException e) {
            }
        }

        // set the buttons
        play.setEnabled(true);
        pause.setSelected(false);
        pause.setEnabled(false);
        stop.setEnabled(false);
    }

    /**
     * Video thread, will loop through all bands.
     */
    public void run() {
        int band;

        while (videothread == Thread.currentThread()) {
            if (pause.isSelected()) {
                // sleep long time
                try {
                    Thread.sleep(500);
                } catch (InterruptedException exc) {
                }

            } else {
                band = frame.getValue() + 1;
                if (band > frame.getMaximum()) {
                    band = 0;
                    if (!loop.isSelected()) {
                        stopvideo();
                        return;
                    }
                }
                video.setGrayBand(band);
                frame.setValue(band);

                // sleep till next frame
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException exc) {
                }
            }
        }
    }
}
