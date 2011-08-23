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
package edu.illinois.ncsa.isda.imagetools.ext.camera;


import javax.media.*;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.swing.*;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;


/**
 */
public class JMFCamera extends Camera implements ControllerListener {
    private JLabel lblNoVideo;
    private JPanel pnlPreview;
    private Dimension dimPreview;
    private CaptureDeviceInfo device;
    private VideoFormat videoformat;
    private boolean live;
    private Player player;

    private JMFCameraOptions options;

    public JMFCamera() throws IOException {
        dimPreview = new Dimension(320, 240);

        lblNoVideo = new JLabel("No Video", JLabel.CENTER);
        lblNoVideo.setPreferredSize(dimPreview);

        pnlPreview = new JPanel(new BorderLayout());
        pnlPreview.add(lblNoVideo);

        device = null;
        videoformat = null;
        live = false;
        player = null;

        // select the first device and size
        String[] dev = getVideoDevices();
        if ((dev == null) || (dev.length == 0)) {
            throw(new IOException("No device found."));
        }
        setVideoDevice(dev[0]);

        Dimension[] dim = getVideoSizes();
        if ((dim == null) || (dim.length == 0)) {
            throw(new IOException("No video size found."));
        }
        setVideoSize(dim[0]);

        // create options
        options = new JMFCameraOptions(this);
    }

    public void close() throws IOException {
        setEnabled(false);
    }

    public ImageObject getFrame() throws IOException {
        boolean enabled = isEnabled();
        setEnabled(true);
        ImageObject frame = snapshot();
        setEnabled(enabled);
        return frame;
    }

    public void showOptionsDialog() {
        options.show();
    }

    public String getName() {
        return "JMF";
    }

    public String toString() {
        return getName() + " (" + device.getName() + ")";
    }

    public String[] getVideoDevices() {
        ArrayList tmparr = new ArrayList();

        Vector devices = CaptureDeviceManager.getDeviceList(new RGBFormat());
        if (devices != null && devices.size() > 0) {
            CaptureDeviceInfo cdi;
            int len = devices.size();
            for (int i = 0; i < len; i++) {
                cdi = (CaptureDeviceInfo) devices.elementAt(i);
                tmparr.add(cdi.getName());
            }
        }

        String[] result = new String[tmparr.size()];
        tmparr.toArray(result);
        return result;
    }

    public void setVideoDevice(String name) throws IOException {
        // if same device ignore
        if ((device != null) && device.getName().equals(name)) {
            return;
        }

        // stop capture
        if (live) {
            stopPreview();
        }

        // get the new device
        device = CaptureDeviceManager.getDevice(name);
        if (device == null) {
            throw(new IOException("No such device."));
        }

        // start capture
        if (live) {
            startPreview();
        }
    }

    public Dimension[] getVideoSizes() throws IOException {
        if (device == null) {
            throw(new IOException("No capture device selected."));
        }

        ArrayList tmparr = new ArrayList();

        Format[] cfmt = device.getFormats();
        for (int i = 0; i < cfmt.length; i++) {
            if (cfmt[i] instanceof RGBFormat) {
                VideoFormat vf = (VideoFormat) cfmt[i];
                tmparr.add(vf.getSize());
            }
        }

        Dimension[] result = new Dimension[tmparr.size()];
        tmparr.toArray(result);
        return result;
    }

    public void setVideoSize(Dimension size) throws IOException {
        // if the same size ignore
        if ((videoformat != null) && videoformat.getSize().equals(size)) {
            return;
        }

        // stop capture
        if (live) {
            stopPreview();
        }

        Format[] cfmt = device.getFormats();
        videoformat = null;
        // Find the format that the user has requested (if available)
        for (int i = 0; i < cfmt.length; i++) {
            if (cfmt[i] instanceof RGBFormat) {
                videoformat = (RGBFormat) cfmt[i];
                Dimension d = videoformat.getSize();

                if (size.equals(d))
                    break;

                videoformat = null;
            }
        }

        if (videoformat == null) {
            throw (new IOException("No such size for this device."));
        }

        // start capture
        if (live) {
            startPreview();
        }
    }

    public void setEnabled(boolean live) throws IOException {
        if (this.live == live) {
            return;
        }

        if (device == null) {
            throw(new IOException("No capture device selected."));
        }
        if (videoformat == null) {
            throw(new IOException("No videoformat selected."));
        }

        if (live) {
            startPreview();
            try {
                Thread.sleep(500);
            } catch (InterruptedException exc) {}
        } else {
            stopPreview();
        }
        this.live = live;
    }

    public boolean isEnabled() {
        return live;
    }

    public JPanel getPreview() {
        return pnlPreview;
    }

    public ImageObject snapshot() throws IOException {
        if (!live) {
            throw(new IOException("Not enabled."));
        }
        FrameGrabbingControl control;

        control = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
        if (control == null) {
            throw(new IOException("Could not get get FrameGrabbingControl"));
        }

        Buffer buffer;
        do {
            buffer = control.grabFrame();
        } while ((buffer == null) || (buffer.getLength() == 0));

        if (!(buffer.getFormat() instanceof RGBFormat)) {
            throw(new IOException("Format should be RGB how can this be?"));
        }
        RGBFormat format = (RGBFormat) buffer.getFormat();

        if (!format.getDataType().isArray()) {
            throw(new IOException("Data should be an array."));
        }

        int w = format.getSize().width;
        int h = format.getSize().height;
        int b = format.getPixelStride();

        ImageObject result;

        if (format.getDataType().getComponentType().equals(Byte.TYPE)) {
            result = new ImageObjectByte(h, w, b);
        } else {
            throw(new IOException("Can not convert this type of data."));
        }

        result.setData(buffer.getData());
        int[] rgb = new int[]{format.getRedMask() - 1,
                              format.getGreenMask() - 1,
                              format.getBlueMask() - 1};
        result.setProperty(ImageObject.DEFAULT_RGB, rgb);

        if (format.getFlipped() != 0) {
            return flip(result);
        }
        return result;
    }

    private ImageObject flip(ImageObject src) throws IOException {
        ImageObject dst;
        try {
            dst = (ImageObject) src.clone();
        } catch (CloneNotSupportedException exc) {
            throw(new IOException(exc.toString()));
        }

        int r, rl, idx1, idx2, len;
        rl = src.getNumRows() - 1;
        len = src.getNumCols() * src.getNumBands();
        Object srcdata = src.getData();
        Object dstdata = dst.getData();
        for (idx1 = 0, idx2 = rl * len, r = 0; r <= rl; r++, idx1 += len, idx2 -= len) {
            System.arraycopy(srcdata, idx1, dstdata, idx2, len);
        }

        return dst;
    }

    private void stopPreview() {
        if (player != null) {
            player.close();
        }
    }

    private void startPreview() throws IOException {
        /*  To use this device we need a MediaLocator  */
        MediaLocator loc = device.getLocator();

        if (loc == null)
            throw new IOException("Unable to get MediaLocator for device");

        DataSource formattedSource = null;

        /*  Now create a dataSource for this device and set the format to
         *  the one chosen by the user.
         */
        try {
            formattedSource = Manager.createDataSource(loc);
        } catch (NoDataSourceException exc) {
            throw new IOException(exc.toString());
        }

        /*  Setting the format is rather complicated.  Firstly we need to get
         *  the format controls from the dataSource we just created.  In order
         *  to do this we need a reference to an object implementing the
         *  CaptureDevice interface (which DataSource objects can).
         */
        if (!(formattedSource instanceof CaptureDevice))
            throw new IOException("DataSource not a CaptureDevice");

        FormatControl[] fmtControls =
                ((CaptureDevice) formattedSource).getFormatControls();

        if (fmtControls == null || fmtControls.length == 0)
            throw new IOException("No FormatControl available");

        Format setFormat = null;

        /*  Now we need to loop through the available FormatControls and try
         *  to set the format to the one we want.  According to the documentation
         *  even though this may appear to work, it may fail later on.  Since
         *  we know that the format is supported we hope that this won't happen
         */
        for (int i = 0; i < fmtControls.length; i++) {
            if (fmtControls[i] == null)
                continue;

            if ((setFormat = fmtControls[i].setFormat(videoformat)) != null)
                break;
        }

        /*  Throw an exception if we couldn't set the format  */
        if (setFormat == null)
            throw new IOException("Failed to set camera format");

        /*  Connect to the DataSource  */
        try {
            formattedSource.connect();
        } catch (IOException ioe) {
            throw new IOException("Unable to connect to DataSource");
        }

        // create a player that is connected to the datasource
        try {
            player = Manager.createPlayer(formattedSource);
            player.addControllerListener(this);
            player.start();
        } catch (NoPlayerException e) {
            throw(new IOException(e.toString()));
        }
    }

    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof RealizeCompleteEvent) {
            if (player != null) {
                Component preview = player.getVisualComponent();
                preview.setBounds(0, 0, dimPreview.width, dimPreview.height);
                pnlPreview.removeAll();
                pnlPreview.add(preview);
                pnlPreview.invalidate();
            }
        } else if (event instanceof ControllerClosedEvent) {
            if (player != null) {
                player = null;
                pnlPreview.removeAll();
                pnlPreview.add(lblNoVideo);
                pnlPreview.invalidate();
            }
        }
    }
}
