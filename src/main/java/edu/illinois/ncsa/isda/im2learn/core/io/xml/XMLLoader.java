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
package edu.illinois.ncsa.isda.imagetools.core.io.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageWriter;

/**
 * XML format for saved file <image width="xxx" height="xxx" bands="xxxx"
 * type="xxxx"> <raster> <CDATA with raster base64 RGBRGBRGB encoded />
 * </raster> <properties> <property key="xxxx"> <CDATA value base64 encoded />
 * </property> ..... </properties> </image>
 * 
 * @author kooper
 * 
 */
public class XMLLoader implements ImageReader, ImageWriter {

    public boolean canRead(String filename, byte[] hdr) {
        return ImageLoader.getExtention(filename).equals("xml");
    }

    public String getDescription() {
        return "XML File";
    }

    public String[] readExt() {
        return new String[] { "xml" };
    }

    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException, ImageException {
        return readImage(filename, subarea, sampling, false);
    }

    public ImageObject readImageHeader(String filename) throws IOException, ImageException {
        return readImage(filename, null, 1, true);
    }

    private ImageObject readImage(String filename, SubArea subarea, int sampling, boolean headeronly) throws IOException, ImageException {
        try {
            // create the reader
            XMLReader reader = XMLReaderFactory.createXMLReader();

            // setup our custom handler
            ImageHandler handler = new ImageHandler(subarea, sampling, headeronly);
            reader.setContentHandler(handler);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

            // open the file and parse it
            FileInputStream fis = new FileInputStream(filename);
            reader.parse(new InputSource(fis));
            fis.close();

            // return the ImageObject
            return handler.getImageObject();

        } catch (SAXException exc) {
            throw (new ImageException(exc));
        }
    }

    public boolean canWrite(String filename) {
        return "xml".equalsIgnoreCase(ImageLoader.getExtention(filename));
    }

    public String[] writeExt() {
        return new String[] { "xml" };
    }

    public void writeImage(String filename, ImageObject imageobject) throws IOException, ImageException {
        PrintWriter out = new PrintWriter(filename);

        // write the header
        out.format("<image cols=\"%d\" rows=\"%d\" bands=\"%d\" type=\"%s\">%n", imageobject.getNumCols(), imageobject.getNumRows(), imageobject.getNumBands(), imageobject.getTypeString());

        // write the raster
        out.println("  <raster>");
        StringBuffer sb = new StringBuffer("   ");
        switch (imageobject.getType()) {
        case ImageObject.TYPE_BYTE:
        case ImageObject.TYPE_USHORT:
        case ImageObject.TYPE_SHORT:
        case ImageObject.TYPE_INT:
        case ImageObject.TYPE_LONG:
            for (int i = 0; i < imageobject.getSize(); i++) {
                if (sb.length() >= 70) {
                    out.println(sb.toString());
                    sb.setLength(3);
                }
                sb.append(" ");
                sb.append(imageobject.getLong(i));
                ImageLoader.fireProgress(i, imageobject.getSize());
            }
            if (sb.length() > 3) {
                out.println(sb.toString());
            }
            break;
        default:
            for (int i = 0; i < imageobject.getSize(); i++) {
                if (sb.length() >= 70) {
                    out.println(sb.toString());
                    sb.setLength(3);
                }
                sb.append(" ");
                sb.append(imageobject.getDouble(i));
                ImageLoader.fireProgress(i, imageobject.getSize());
            }
            if (sb.length() > 3) {
                out.println(sb.toString());
            }
        }
        out.println("  </raster>");

        // write the properties
        out.println("  <properties>");
        for (Entry<String, Object> entry : imageobject.getProperties().entrySet()) {
            if (!entry.getKey().startsWith("_")) {
                if (entry.getValue() instanceof String) {
                    dump(out, entry.getKey(), (String) entry.getValue());
                } else {
                    dump(out, entry.getKey(), entry.getValue());
                }
            }
        }
        out.println("  </properties>");

        // done
        out.println("</image>");
        out.close();
    }

    private void dump(PrintWriter out, String key, String val) throws IOException {
        out.format("    <property key=\"%s\" type=\"string\">%n", key);
        out.format("      <![CDATA[%s]]>%n", val);
        out.format("    </property>%n");
    }

    private void dump(PrintWriter out, String key, Object val) throws IOException {
        out.format("    <property key=\"%s\" type=\"serialized\">%n", key);
        out.format("      <![CDATA[%s]]>%n", encode(val));
        out.format("    </property>%n");
    }

    private String encode(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        return DatatypeConverter.printBase64Binary(bos.toByteArray());
    }

    private Object decode(String data) throws IOException, ClassNotFoundException {
        byte[] arr = DatatypeConverter.parseBase64Binary(data);
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    public int getImageCount(String filename) {
        return 1;
    }

    public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException {
        return readImage(filename, subarea, sampling);
    }

    public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
        return readImageHeader(filename);
    }

    enum ParseState {
        NONE, IMAGE, RASTER, PROPERTIES, PROPERTY
    };

    class ImageHandler extends DefaultHandler implements LexicalHandler {
        private final SubArea subarea;
        private final int     sampling;
        private final boolean headeronly;
        private ImageObject   imgobj;
        private int           rows, cols, bands;
        private int           row, col, band;
        private int           index;
        private StringBuffer  data  = null;
        private String        type  = null;
        private String        key   = null;
        private ParseState    state = ParseState.NONE;

        public ImageHandler(SubArea subarea, int sampling, boolean headeronly) {
            this.subarea = subarea;
            this.sampling = sampling;
            this.headeronly = headeronly;
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException {
            if ((state == ParseState.NONE) && localName.equalsIgnoreCase("image")) {
                cols = Integer.parseInt(atts.getValue("cols"));
                rows = Integer.parseInt(atts.getValue("rows"));
                bands = Integer.parseInt(atts.getValue("bands"));
                String type = atts.getValue("type");
                try {
                    imgobj = ImageObject.createImage((int) Math.ceil((double) rows / sampling), (int) Math.ceil((double) cols / sampling), bands, type, headeronly);
                } catch (ImageException exc) {
                    throw (new SAXException(exc));
                }
                state = ParseState.IMAGE;
            } else if ((state == ParseState.IMAGE) && localName.equalsIgnoreCase("raster")) {
                data = new StringBuffer();
                row = 0;
                col = 0;
                band = 0;
                index = 0;
                state = ParseState.RASTER;
            } else if ((state == ParseState.IMAGE) && localName.equalsIgnoreCase("properties")) {
                state = ParseState.PROPERTIES;
            } else if ((state == ParseState.PROPERTIES) && localName.equalsIgnoreCase("property")) {
                key = atts.getValue("key");
                type = atts.getValue("type");
                state = ParseState.PROPERTY;
            } else {
                throw (new SAXException("Invalid state :" + state + " " + localName));
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
            if ((state == ParseState.IMAGE) && localName.equalsIgnoreCase("image")) {
                state = ParseState.NONE;
            } else if ((state == ParseState.RASTER) && localName.equalsIgnoreCase("raster")) {
                state = ParseState.IMAGE;
            } else if ((state == ParseState.PROPERTIES) && localName.equalsIgnoreCase("properties")) {
                state = ParseState.IMAGE;
            } else if ((state == ParseState.PROPERTY) && localName.equalsIgnoreCase("property")) {
                state = ParseState.PROPERTIES;
            } else {
                throw (new SAXException("Invalid state :" + state + " " + localName));
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (data == null) {
                return;
            }
            switch (state) {
            case RASTER:
                data.append(ch, start, length);
                String ds = data.toString();
                String[] vals = ds.trim().split("\\s+");
                data.setLength(0);
                length = vals.length;
                if (!ds.endsWith(" ") && !ds.endsWith("\n")) {
                    data.append(vals[length - 1]);
                    length--;
                }
                for (int i = 0; i < length; i++) {
                    try {
                        double d = Double.parseDouble(vals[i].trim());
                        boolean ok = true;
                        if ((subarea != null) && ((col < subarea.getMinX()) || (col > subarea.getMaxX()) || (row < subarea.getMinY()) || (row > subarea.getMaxY()))) {
                            ok = false;
                        }
                        if ((sampling != 1) && (((col % sampling) != 0) || ((row % sampling) != 0))) {
                            ok = false;
                        }
                        if (ok) {
                            ImageLoader.fireProgress(index, imgobj.getSize());
                            imgobj.set(index++, d);
                        }
                        band++;
                        if (band >= bands) {
                            band = 0;
                            col++;
                            if (col >= cols) {
                                col = 0;
                                row++;
                            }
                        }
                    } catch (Exception exc) {
                        throw (new SAXException(exc));
                    }
                }
                break;
            case PROPERTY:
                data.append(ch, start, length);
                break;
            }
        }

        public ImageObject getImageObject() throws ImageException {
            return imgobj;
        }

        public void comment(char[] ch, int start, int length) throws SAXException {
        }

        public void startCDATA() throws SAXException {
            data = new StringBuffer();
        }

        public void endCDATA() throws SAXException {
            if (data == null) {
                return;
            }
            String str = data.toString();
            switch (state) {
            case PROPERTY:
                Object val;
                if (type.equals("string")) {
                    val = str;
                } else if (type.equals("serialized")) {
                    try {
                        val = decode(str);
                    } catch (ClassNotFoundException exc) {
                        throw (new SAXException(exc));
                    } catch (IOException exc) {
                        throw (new SAXException(exc));
                    } finally {
                        data = null;
                    }
                } else {
                    throw (new SAXException("Can't decode " + type));
                }
                imgobj.setProperty(key, val);
                break;
            default:
                throw (new SAXException("no cdata known for state=" + state));
            }
            data = null;
        }

        public void startDTD(String name, String publicId, String systemId) throws SAXException {
        }

        public void endDTD() throws SAXException {
        }

        public void startEntity(String name) throws SAXException {
        }

        public void endEntity(String name) throws SAXException {
        }
    }
}
