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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import sun.awt.image.URLImageSource;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Code specific for the Sony NC-RZ30 camera. This class implements the
 * functions from the abstract Camera class. The Sony camera will always return
 * the image as if the camera is mounted on the ceiling. This code will take
 * care of flipping the image around.
 */
public class SNCRZ30 extends Camera {
    private String hostname = null;
    private int port = 80;

    private boolean deskmounted;
    private int serialnumber;
    private boolean ntsc;
    private String model;
    private float version;
    private boolean oldversion;
    private int imageSize;
    private String username;
    private String password;

    private String[] sizeN = new String[]{"763x480 (auto)", "763x480 (frame)", "763x480 (field)",
                                          "640x480 (auto)", "640x480 (frame)", "640x480 (field)",
                                          "320x240", "160x120", "Special"};
    private String[] sizeP = new String[]{"736x544 (auto)", "736x544 (frame)", "736x544 (field)",
                                          "640x480 (auto)", "640x480 (frame)", "640x480 (field)",
                                          "320x240", "160x120", "Special"};


    private static Log logger = LogFactory.getLog(SNCRZ30.class);

    public SNCRZ30() {
        this(null, 80);
    }

    public SNCRZ30(String hostname) {
        this(hostname, 80);
    }

    public SNCRZ30(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

        Authenticator.setDefault(new SNCRZ30Authenticator());

        initialize();
    }

    // ------------------------------------------------------------------------

    class SNCRZ30Authenticator extends Authenticator {
        protected PasswordAuthentication getPasswordAuthentication() {
            if ((username == null) || (password == null)) {
                return null;
            }
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Set the hostname of the camera. This determines which network camera the
     * class is connected to.
     *
     * @return hostname of the camera.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname of the camera the class is connected to.
     *
     * @param hostname the hostname of the camera.
     */
    public void setHostname(String hostname) {
        if ((this.hostname != null) && this.hostname.equals(hostname)) {
            return;
        }
        this.hostname = hostname;
        initialize();
    }

    /**
     * The port the camera is sending the images on. This is the port on which
     * the camera runs the web-server.
     *
     * @return port number of the camerea.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port to which the class will connect. This is the port on which
     * the camera runs the web-server.
     *
     * @param port the port to which to connect.
     */
    public void setPort(int port) {
        if (this.port == port) {
            return;
        }
        this.port = port;
        initialize();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ------------------------------------------------------------------------
    public boolean isDeskmounted() {
        return deskmounted;
    }

    public int getSerialnumber() {
        return serialnumber;
    }

    public String getModel() {
        return model;
    }

    public float getVersion() {
        return version;
    }

    public boolean isNtsc() {
        return ntsc;
    }

    public String[] getImageSizes() {
        return ntsc ? sizeN : sizeP;
    }

    public String getImageSize() {
        return ntsc ? sizeN[imageSize] : sizeP[imageSize];
    }

    public void setImageSize(int imageSize) {
        if (this.imageSize == imageSize) {
            return;
        }

        String script = oldversion ? "jpeg.cgi" : "camera.cgi";
        try {
            URL url = new URL("http://" + hostname + ":" + port + "/command/" + script + "?ImageSize=" + imageSize);
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            // map variables to properties
            Map map = connection.getHeaderFields();
            for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
                Object key = iter.next();
                Object val = map.get(key);

                //logger.debug(key + " = " + val);
            }
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            this.imageSize = imageSize;
        } catch (IOException exc) {
            logger.error("Could not set imagesize.", exc);
        }
    }

    public void setImageSize(String size) {
        String[] camera = getImageSizes();
        for (int idx = 0; idx < camera.length; idx++) {
            if (camera[idx].startsWith(size)) {
                setImageSize(idx);
                return;
            }
        }
    }

    public boolean isOldversion() {
        return oldversion;
    }

    // ------------------------------------------------------------------------

    private void initialize() {
        Properties prop = new Properties();
        try {
            URL url = new URL("http://" + hostname + ":" + port + "/command/inquiry.cgi?inq=sysinfo");
            URLConnection connection = url.openConnection();
            //connection.setDoOutput(true);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String[] params = in.readLine().split("&");
            in.close();

            for (int i = 0; i < params.length; i++) {
                String[] var = params[i].split("=", 2);
                String key = URLDecoder.decode(var[0], "UTF8");
                String val = URLDecoder.decode(var[1], "UTF8");
                prop.setProperty(key, val);
            }
        } catch (IOException exc) {
            logger.info("Could not read sysinfo properties.");
        }

        try {
            URL url = new URL("http://" + hostname + ":" + port + "/command/inquiry.cgi?inq=camera");
            URLConnection connection = url.openConnection();
            //connection.setDoOutput(true);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String[] params = in.readLine().split("&");
            in.close();

            for (int i = 0; i < params.length; i++) {
                String[] var = params[i].split("=", 2);
                String key = URLDecoder.decode(var[0], "UTF8");
                String val = URLDecoder.decode(var[1], "UTF8");
                prop.setProperty(key, val);
            }
        } catch (IOException exc) {
            logger.info("Could not read camera properties.");
        }

        // now really parse the data
        try {
            serialnumber = Integer.parseInt(prop.getProperty("Serial", "0"));
        } catch (NumberFormatException exc) {
            logger.info("Invalid serial number.");
        }
        deskmounted = prop.getProperty("Mount", "desktop").equals("desktop");
        model = prop.getProperty("ModelName", "SNC-RZ30N");
        ntsc = model.endsWith("N");
        try {
            version = Float.parseFloat(prop.getProperty("SoftVersion", "1.05"));
        } catch (NumberFormatException exc) {
            logger.info("Invalid version number.");
        }
        oldversion = (version < 1.10);
        try {
            imageSize = Integer.parseInt(prop.getProperty("ImageSize", "0"));
        } catch (NumberFormatException exc) {
            logger.info("Invalid image size.");
        }
    }

    // ------------------------------------------------------------------------

    /**
     * The name of the camera, this will return the camera model.
     *
     * @return the model number of the camera.
     */
    public String getName
            () {
        return model;
    }

    /**
     * Closes any connections opened with the camera.
     *
     * @throws IOException if anything goes wrong.
     */
    public void close() throws IOException {
    }

    /**
     * Return a single frame from the camera.
     *
     * @return an image captured by the camera.
     * @throws IOException if anything goes wrong capturing the image.
     */
    public ImageObject getFrame() throws IOException {
        if (hostname == null) {
            throw(new IOException("No hostname specified."));
        }
        URL url = new URL("http://" + hostname + ":" + port + "/oneshotimage.jpg");
        SonyImageGrabber grabber = new SonyImageGrabber(url);
        return grabber.getFrame();
    }

    /**
     * Show the dialog in which the user can control the camera.
     */
    public void showOptionsDialog() {
        SNCRZ30Options options = new SNCRZ30Options(this);
        options.show();
    }

    /**
     * Return the model of the camera and the hostname of the camera.
     *
     * @return model and hostname of the camera.
     */
    public String toString() {
        return getName() + " (" + hostname + ":" + port + ")";
    }

    // ------------------------------------------------------------------------

    /**
     * Helper class, responsible for parsing the result coming back from the
     * Sony camera. The images from the sony seem to be returned flipped around,
     * this class will unflip those images.
     */
    class SonyImageGrabber implements ImageConsumer {
        private ImageObject imgobj = null;
        private int id = 0;

        public SonyImageGrabber(URL url) throws IOException {
            URLConnection conn = url.openConnection();
            conn.connect();
            Object obj = conn.getContent();
            if (obj instanceof URLImageSource) {
                synchronized (this) {
                    URLImageSource is = (URLImageSource) obj;
                    is.startProduction(this);

                    // wait for image
                    try {
                        this.wait();
                    } catch (InterruptedException exc) {
                        throw(new IOException("Interrupted"));
                    }

                    // map variables to properties
                    Map map = conn.getHeaderFields();
                    for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
                        Object key = iter.next();
                        Object val = map.get(key);

                        //logger.debug(key + " = " + val);
                        if (key != null) {
                            imgobj.setProperty(key.toString(), val);
                        } else {
                            imgobj.setProperty("null_" + id, val);
                            id++;
                        }
                    }

                    // add some bonus info
                    imgobj.setProperty("hostname", url.getHost());
                    imgobj.setProperty("port", "" + url.getPort());
                }
            } else {
                throw(new IOException("Expected image returned."));
            }
        }

        public ImageObject getFrame() {
            return imgobj;
        }

        public void imageComplete(int status) {
            //logger.debug("imageComplete : " + status);
            synchronized (this) {
                this.notify();
            }
        }

        public void setHints(int hintflags) {
            //logger.debug("setHints : " + hintflags);
        }

        public void setDimensions(int width, int height) {
            //logger.debug("setDimensions : " + width + ", " + height);
            imgobj = new ImageObjectByte(height, width, 3);
        }

        public void setPixels(int x, int y, int w, int h, ColorModel model, byte pixels[], int off, int scansize) {
            //logger.debug("setPixels byte");
            int imgw = imgobj.getNumCols();
            int imgb = imgobj.getNumBands();

            int i = off;
            int stepi = scansize - w;

            int j = ((y * imgw) + x) * imgb;
            int stepj = (imgw - w) * imgb;

            // if the camera is deskmounted, the image needs to be flipped.
            if (deskmounted) {
                j = imgobj.getSize() - j - imgb;
                stepj = -stepj;
                imgb = -imgb;
            }

            int r, c;
            for (r = 0; r < h; r++, j += stepj, i += stepi) {
                for (c = 0; c < w; c++, i++, j += imgb) {
                    imgobj.set(j + 0, model.getRed(pixels[i]));
                    imgobj.set(j + 1, model.getGreen(pixels[i]));
                    imgobj.set(j + 2, model.getBlue(pixels[i]));
                }
            }
        }

        public void setPixels(int x, int y, int w, int h, ColorModel model, int pixels[], int off, int scansize) {
            //logger.debug("setPixels int");
            int imgw = imgobj.getNumCols();
            int imgb = imgobj.getNumBands();

            int i = off;
            int stepi = scansize - w;

            int j = ((y * imgw) + x) * imgb;
            int stepj = (imgw - w) * imgb;

            // if the camera is deskmounted, the image needs to be flipped.
            if (deskmounted) {
                j = imgobj.getSize() - j - imgb;
                stepj = -stepj;
                imgb = -imgb;
            }

            int r, c;
            for (r = 0; r < h; r++, j += stepj, i += stepi) {
                for (c = 0; c < w; c++, i++, j += imgb) {
                    imgobj.set(j + 0, model.getRed(pixels[i]));
                    imgobj.set(j + 1, model.getGreen(pixels[i]));
                    imgobj.set(j + 2, model.getBlue(pixels[i]));
                }
            }
        }

        public void setColorModel(ColorModel model) {
            //logger.debug("setColorModel : " + model);
        }

        public void setProperties(Hashtable props) {
            //logger.debug("Hashtable : ");
            for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
                Object key = iter.next();
                Object val = props.get(key);

                //logger.debug(key + " = " + val);
                if (key != null) {
                    imgobj.setProperty(key.toString(), val);
                } else {
                    imgobj.setProperty("null_" + id, val);
                    id++;
                }
            }
        }
    }
}
