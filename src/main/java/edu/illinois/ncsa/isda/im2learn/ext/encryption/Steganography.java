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
package edu.illinois.ncsa.isda.im2learn.ext.encryption;

import java.io.IOException;
import java.util.Random;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;

/**
 *
 */
public class Steganography  {
    private ImageObject imgOrig;
    private ImageObject imgSecret;

    static private final int TEXT  = 0x9abc;
    static private final int IMAGE = 0xaf53;
    int gap = 1;
    boolean distribute = false;
    
    public Steganography() {
        this(null);
    }

    public Steganography(ImageObject imgobj) {
        this.imgOrig = imgobj;
        this.imgSecret = null;
    }

    public ImageObject getOriginalImage() {
        return imgOrig;
    }

    public void setOriginalImage(ImageObject imgobj) {
        this.imgOrig = imgobj;
        this.imgSecret = null;
    }

    public ImageObject getSecretImage() {
        return imgSecret;
    }

    public void setSecretImage(ImageObject imgobj) {
        this.imgSecret = imgobj;
    }


    public void setDistribute(boolean dist) {
    	distribute = dist;
    	if(!distribute) 
    		gap = 1;
    }
    
    public boolean getDistribute() {
    	return distribute;
    }
    
    public Object decryptObject(int bits, int offset) throws IOException {
        if (imgSecret == null) {
            throw(new IOException("need at least an image."));
        }
        checkBits(bits);

        int[] header = new int[1];
        readInt(header, offset, bits);

        switch (header[0]) {
            case TEXT:
                return decryptText(bits, offset);

            case IMAGE:
                return decryptImage(bits, offset);

            default:
                throw(new IOException("no message, or message is lost."));
        }
    }

    public void fillGarbage(int bits, int offset, int len) throws IOException {
        writeGarbage(len, offset, bits);
    }

    // ------------------------------------------------------------------------
    // set/get text from the image
    // ------------------------------------------------------------------------
    public String decryptText(int bits, int offset) throws IOException {
        if (imgSecret == null) {
            throw(new IOException("need at least an image."));
        }
        checkBits(bits);

        int[] header = new int[2];
        int idx = readInt(header, offset, bits);

        if (header[0] != TEXT) {
            throw(new IOException("no message, or message is lost."));
        }

        byte[] data = new byte[header[1]];
        idx = readByte(data, idx, bits);

        return new String(data);
    }

    public void encryptText(String message, int bits, int offset) throws IOException {
        if (imgOrig == null) {
            throw(new IOException("need at least an image."));
        }
        try {
            imgSecret = (ImageObject) imgOrig.clone();
        } catch (CloneNotSupportedException exc) {
            throw(new IOException("Could not clone image."));
        }

        byte[] data = message.getBytes();
        int[] header = new int[]{TEXT, data.length};

        if (bits <= 0) {
            bits = calcBits(data, header.length * 32);
        }
        checkBits(bits);

        int idx = writeInt(header, offset, bits);
        idx = writeByte(data, idx, bits);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public ImageObject decryptImage(int bits, int offset) throws IOException {
        if (imgSecret == null) {
            throw(new IOException("need at least an image."));
        }
        checkBits(bits);

        int[] header = new int[5];
        int idx = readInt(header, offset, bits);

        if (header[0] != IMAGE) {
            throw(new IOException("no message, or message is lost."));
        }

      int bytes = (int) Math.ceil(header[1] * 8 / bits);
        
     	if(distribute) {
	        gap = (int)((double)(imgSecret.getSize()-offset)/(double)bytes);
	        System.out.println("reading Gap = "+ gap);
      	}
      	
        byte[] data = new byte[header[1]];
        idx = readByte(data, idx, bits);

        int w = header[2];
        int h = header[3];
        int d = header[4];

        try {
            ImageObject image = ImageObject.createImage(h, w, d, ImageObject.TYPE_BYTE);
            System.arraycopy(data, 0, image.getData(), 0, data.length);
            return image;
        } catch (ImageException exc) {
            throw(new IOException("Error creating image."));
        }
    }

    public void encryptImage(ImageObject image, int bits, int offset) throws IOException {
        if (imgOrig == null) {
            throw(new IOException("need at least an image."));
        }
        if (image.getType() != ImageObject.TYPE_BYTE) {
            throw(new IOException("only can encrypt byte images."));
        }
        try {
            imgSecret = (ImageObject) imgOrig.clone();
        } catch (CloneNotSupportedException exc) {
            throw(new IOException("Could not clone image."));
        }

        byte[] data = (byte[]) image.getData();
        int[] header = new int[]{IMAGE, data.length, image.getNumCols(),
                                image.getNumRows(), image.getNumBands()};

        if (bits <= 0) {
            bits = calcBits(data, header.length * 32);
        }
        checkBits(bits);
        
        int idx = writeInt(header, offset, bits);
        
        
        if(distribute) {
        	int bytes = (int) Math.ceil(data.length * 8 / bits);
        	gap = (int)((double)(imgSecret.getSize()-idx)/(double)bytes);
        	System.out.println("Optimal Gap = "+ gap);
        }
        
        idx = writeByte(data, idx, bits);
    }

    private int calcBits(byte[] data, int extrabits) {
        int nobits = extrabits + (data.length * 8);
        return (int)Math.ceil(nobits / imgOrig.getSize());
    }

    private void checkBits(int bits) throws IOException {
        if (bits < 1) {
            throw(new IOException("Need at least 1 bit."));
        }
        switch (imgSecret.getType()) {
            case ImageObject.TYPE_BYTE:
                if (bits > 8)
                    throw(new IOException("Maximum for byte image is 8 bits."));
                break;

            case ImageObject.TYPE_SHORT:
            case ImageObject.TYPE_USHORT:
                if (bits > 16)
                    throw(new IOException("Maximum for short image is 16 bits."));
                break;

            case ImageObject.TYPE_INT:
                if (bits > 32)
                    throw(new IOException("Maximum for int image is 32 bits."));
                break;

            case ImageObject.TYPE_LONG:
                if (bits > 64)
                    throw(new IOException("Maximum for long image is 64 bits."));
                break;

            default:
                throw(new IOException("Image type is not supported."));
        }
    }

    // ------------------------------------------------------------------------
    // convert int to bits and write/read to image
    // ------------------------------------------------------------------------

    private int writeInt(int[] data, int idx, int bits) throws IOException {
        int nobits = data.length * 32;
        int bytes = (int) Math.ceil(nobits / bits);

        if (idx + bytes > imgSecret.getSize()) {
            throw(new IOException("Not enough space in image."));
        }

        long mask = ~((1 << bits) - 1);

        long msg = 0;
        int val = 0;
        for (int m = 0, i = 0; i < nobits; i++) {
            if (i % 32 == 0) {
                val = data[m];
                m++;
            }
            if ((i != 0) && (i % bits == 0)) {
                imgSecret.setLong(idx, (imgSecret.getLong(idx) & mask) | msg);
                msg = 0;
                idx++;
            }

            msg = msg << 1;
            if ((val & 0x80000000) != 0) {
                msg++;
            }
            val = val << 1;
        }
        if ((nobits % bits) != 0) {
            msg = msg << (bits - (nobits % bits));
        }
        imgSecret.setLong(idx, (imgSecret.getLong(idx) & mask) | msg);
        idx++;

        return idx;
    }

    private int readInt(int[] data, int idx, int bits) throws IOException {
        int nobits = data.length * 32;
        int bytes = (int) Math.ceil(nobits / bits);

        if (idx + bytes > imgSecret.getSize()) {
            throw(new IOException("Not enough space in image."));
        }

        long mask = (1 << (bits - 1));

        int msg = 0;
        long val = 0;
        int m = 0;
        for (int i = 0; i < nobits; i++) {
            if ((i != 0) && (i % 32 == 0)) {
                data[m] = msg;
                msg = 0;
                m++;
            }
            if (i % bits == 0) {
                val = imgSecret.getLong(idx);
                idx++;
            }

            msg = msg << 1;
            if ((val & mask) != 0) {
                msg++;
            }
            val = val << 1;
        }
        data[m] = msg;

        return idx;
    }

    // ------------------------------------------------------------------------
    // convert byte to bits and write/read to image
    // ------------------------------------------------------------------------

    private int writeByte(byte[] data, int idx, int bits) throws IOException {
        int nobits = data.length * 8;
        int bytes = (int) Math.ceil(nobits / bits);

        if (idx + bytes > imgSecret.getSize()) {
           throw(new IOException("Not enough space in image."));
        }

        long mask = ~((1 << bits) - 1);

        long msg = 0;
        byte val = 0;
        for (int m = 0, i = 0; i < nobits; i++) {
            if (i % 8 == 0) {
                val = data[m];
                m++;
            }
            if ((i != 0) && (i % bits == 0)) {
            	try{
            	imgSecret.setLong(idx, (imgSecret.getLong(idx) & mask) | msg);
            	}
            	catch(Exception ee) {
            		System.out.println("Out of range "+idx);
            		
            	}
            	
                msg = 0;
              //  idx++;
                idx += gap;
            }

            msg = msg << 1;
            if ((val & 0x80) != 0) {
                msg++;
            }
            val = (byte) (val << 1);
        }
        if ((nobits % bits) != 0) {
            msg = msg << (bits - (nobits % bits));
        }
        imgSecret.setLong(idx, (imgSecret.getLong(idx) & mask) | msg);
        idx++;

        return idx;
    }

    private int readByte(byte[] data, int idx, int bits) throws IOException {
        int nobits = data.length * 8;
        int bytes = (int) Math.ceil(nobits / bits);

        if (idx + bytes > imgSecret.getSize()) {
            throw(new IOException("Not enough space in image."));
        }

        long mask = (1 << (bits - 1));

        byte msg = 0;
        int m = 0;
        long val = imgSecret.getLong(idx);
        for (int i = 0; i < nobits; i++) {
            if ((i != 0) && (i % 8 == 0)) {
                data[m] = msg;
                msg = 0;
                m++;
            }
            if (i % bits == 0) {
            	try{
                val = imgSecret.getLong(idx);
            	}catch(Exception ee){}
                //idx++;
                idx += gap;
                
            }

            msg = (byte) (msg << 1);
            if ((val & mask) != 0) {
                msg++;
            }
            val = val << 1;
        }
        data[m] = msg;

        return idx;
    }

    // ------------------------------------------------------------------------
    // fill image with garbage, removing secret or blurring secret area
    // ------------------------------------------------------------------------

    private int writeGarbage(int len, int idx, int bits) throws IOException {
        long used = (1 << bits) - 1;
        long mask = ~used;

        if ((len != Integer.MAX_VALUE) && (idx + len > imgSecret.getSize())) {
            throw(new IOException("Not enough space in image."));
        }

        Random random = new Random( );
        for(int i=0; i<len && idx<imgSecret.getSize(); i++, idx++) {
            long tmp = imgSecret.getLong(idx) & mask;
            tmp += random.nextInt((int)used);
            imgSecret.setLong(idx, tmp);
        }

        return idx;
    }
}
