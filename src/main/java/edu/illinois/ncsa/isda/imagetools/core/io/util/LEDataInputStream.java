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
package edu.illinois.ncsa.isda.imagetools.core.io.util;

/*
 * LEDataInputStream.java
 *
 * Very similar to DataInputStream except it reads little-endian instead of
 * big-endian binary data.
 * We can't extend DataInputStream directly since it has only final methods.
 * This forces us implement LEDataInputStream with a DataInputStream object,
 * and use wrapper methods.
 */

import java.io.*;

public class LEDataInputStream implements DataInput {

   /**
    * constructor
    */
   public LEDataInputStream(InputStream in) {
      this.in = in;
      this.d =  new DataInputStream(in);
      w = new byte[8];
   }

   // L I T T L E   E N D I A N   R E A D E R S
   // Little endian methods for multi-byte numeric types.
   // Big-endian do fine for single-byte types and strings.
   /**
     * like DataInputStream.readShort except little endian.
     */
   public final short readShort() throws IOException
   {
      if(!littleEndianMode){return d.readShort();}
      d.readFully(w, 0, 2);
      return (short)(
                    (w[1]&0xff) << 8 |
                    (w[0]&0xff));
   }

   /**
     * like DataInputStream.readUnsignedShort except little endian.
     * Note, returns int even though it reads a short.
     */
   public final int readUnsignedShort() throws IOException
   {
      if(!littleEndianMode){return d.readUnsignedShort();}
      d.readFully(w, 0, 2);
      return (
             (w[1]&0xff) << 8 |
             (w[0]&0xff));
   }

   /**
     * like DataInputStream.readChar except little endian.
     */
   public final char readChar() throws IOException
   {
      if(!littleEndianMode){return d.readChar();}
      d.readFully(w, 0, 2);
      return (char) (
                    (w[1]&0xff) << 8 |
                    (w[0]&0xff));
   }

   /**
     * like DataInputStream.readInt except little endian.
     */
   public final int readInt() throws IOException
   {
      if(!littleEndianMode){return d.readInt();}
      d.readFully(w, 0, 4);
      return
      (w[3])      << 24 |
      (w[2]&0xff) << 16 |
      (w[1]&0xff) <<  8 |
      (w[0]&0xff);
   }

   /**
     * like DataInputStream.readLong except little endian.
     */
   public final long readLong() throws IOException
   {
      if(!littleEndianMode){return d.readLong();}
      d.readFully(w, 0, 8);
      return
      (long)(w[7])      << 56 |  /* long cast needed or shift done modulo 32 */
      (long)(w[6]&0xff) << 48 |
      (long)(w[5]&0xff) << 40 |
      (long)(w[4]&0xff) << 32 |
      (long)(w[3]&0xff) << 24 |
      (long)(w[2]&0xff) << 16 |
      (long)(w[1]&0xff) <<  8 |
      (long)(w[0]&0xff);
   }

   /**
     * like DataInputStream.readFloat except little endian.
     */
   public final float readFloat() throws IOException
   {
      if(!littleEndianMode){return d.readFloat();}
      return Float.intBitsToFloat(readInt());
   }

   /**
     * like DataInputStream.readDouble except little endian.
     */
   public final double readDouble() throws IOException
   {
       if(!littleEndianMode){return d.readDouble();}
      return Double.longBitsToDouble(readLong());
   }

   // p u r e l y   w r a p p e r   m e t h o d s
   // We can't simply inherit since dataInputStream is final.

   /* Watch out, may return fewer bytes than requested. */
   public final int read(byte b[], int off, int len) throws IOException
   {
      // For efficiency, we avoid one layer of wrapper
      return in.read(b, off, len);
   }

   public final void readFully(byte b[]) throws IOException
   {
      d.readFully(b, 0, b.length);
   }

   public final void readFully(byte b[], int off, int len) throws IOException
   {
      d.readFully(b, off, len);
   }

   public final int skipBytes(int n) throws IOException
   {
      return d.skipBytes(n);
   }

   /* only reads one byte */
   public final boolean readBoolean() throws IOException
   {
      return d.readBoolean();
   }

   public final byte readByte() throws IOException
   {
      return d.readByte();
   }

   // note: returns an int, even though says Byte.
   public final int readUnsignedByte() throws IOException
   {
      return d.readUnsignedByte();
   }

   public final String readLine() throws IOException
   // Try changing this according to the documentation:
   /* This method does not properly convert bytes to characters. As of JDK 1.1, the preferred way to read lines of text is via the BufferedReader.readLine() method. Programs that use the DataInputStream class to read lines can be converted to use the BufferedReader class by replacing code of the form:
   *  DataInputStream d = new DataInputStream(in);
   *  with:
   *  BufferedReader d = new BufferedReader(new InputStreamReader(in));
   */
   {
      return d.readLine();
   }

   public final String readUTF() throws IOException
   {
      return d.readUTF();
   }

   // Note. This is a STATIC method!
   public final static String readUTF(DataInput in) throws IOException
   {
      return DataInputStream.readUTF(in);
   }

   public final  void close() throws IOException   {
      d.close();
   }


   public final void setLittleEndianMode(boolean flag){
        littleEndianMode = flag;
    }

    public final boolean getLittleEndianMode(){
        return littleEndianMode;
    }

    public final boolean isLittleEndianMode(){
        return littleEndianMode;
    }
   // i n s t a n c e   v a r i a b l e s

   protected DataInputStream d; // to get at high level readFully methods of DataInputStream
   protected InputStream in;    // to get at the low-level read methods of InputStream
   byte w[]; // work array for buffering input
   protected boolean littleEndianMode = true;

} // end class LEDataInputStream
