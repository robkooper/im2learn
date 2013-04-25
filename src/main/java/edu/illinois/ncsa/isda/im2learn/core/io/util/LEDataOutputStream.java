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
package edu.illinois.ncsa.isda.im2learn.core.io.util;

/*
 * LEDataOutputStream.java
  * Very similar to DataOutputStream except it writes little-endian instead of
 * big-endian binary data.
 * We can't extend DataOutputStream directly since it has only final methods.
 * This forces us implement LEDataOutputStream with a DataOutputStream object,
 * and use wrapper methods.
 */


import java.io.*;

public
class LEDataOutputStream implements DataOutput {

   /**
     * constructor
     */
   public LEDataOutputStream(OutputStream out) {
      this.d = new DataOutputStream(out);
      w = new byte[8]; // work array for composing output
   }

   // L I T T L E   E N D I A N   W R I T E R S
   // Little endian methods for multi-byte numeric types.
   // Big-endian do fine for single-byte types and strings.

   /**
     * like DataOutputStream.writeShort.
     * also acts as a writeUnsignedShort
     */
   public final void writeShort(int v) throws IOException
   {
      if(!littleEndianMode){d.writeShort(v);return;}
      w[0] = (byte) v;
      w[1] = (byte)(v >> 8);
      d.write(w, 0, 2);
   }

   /**
    * like DataOutputStream.writeChar.
    * Note the parm is an int even though this as a writeChar
    */
   public final void writeChar(int v) throws IOException
   {
      if(!littleEndianMode){d.writeChar(v);return;}
      // same code as writeShort
      w[0] = (byte) v;
      w[1] = (byte)(v >> 8);
      d.write(w, 0, 2);
   }

   /**
     * like DataOutputStream.writeInt.
     */
   public final void writeInt(int v) throws IOException
   {
      if(!littleEndianMode){d.writeInt(v);return;}
      w[0] = (byte) v;
      w[1] = (byte)(v >> 8);
      w[2] = (byte)(v >> 16);
      w[3] = (byte)(v >> 24);
      d.write(w, 0, 4);
   }

   /**
     * like DataOutputStream.writeLong.
     */
   public final void writeLong(long v) throws IOException
   {
      if(!littleEndianMode){d.writeLong(v);return;}
      w[0] = (byte) v;
      w[1] = (byte)(v >> 8);
      w[2] = (byte)(v >> 16);
      w[3] = (byte)(v >> 24);
      w[4] = (byte)(v >> 32);
      w[5] = (byte)(v >> 40);
      w[6] = (byte)(v >> 48);
      w[7] = (byte)(v >> 56);
      d.write(w, 0, 8);
   }

   /**
     * like DataOutputStream.writeFloat.
     */
   public final void writeFloat(float v) throws IOException
   {
      if(!littleEndianMode){d.writeFloat(v);return;}
      writeInt(Float.floatToIntBits(v));
   }

   /**
     * like DataOutputStream.writeDouble.
     */
   public final void writeDouble(double v) throws IOException
   {
      if(!littleEndianMode){d.writeDouble(v);return;}
      writeLong(Double.doubleToLongBits(v));
   }

   /**
     * like DataOutputStream.writeChars, flip each char.
     */
   public final void writeChars(String s) throws IOException
   {
      if(!littleEndianMode){d.writeChars(s);return;}
      int len = s.length();
      for ( int i = 0 ; i < len ; i++ ) {
         writeChar(s.charAt(i));
      }
   } // end writeChars

   // p u r e l y   w r a p p e r   m e t h o d s
   // We cannot inherit since DataOutputStream is final.

   /* This method writes only one byte, even though it says int */
   public final synchronized void write(int b) throws IOException
   {
      d.write(b);
   }

   public final synchronized void write(byte b[], int off, int len)
   throws IOException
   {
      d.write(b, off, len);
   }

   public void flush() throws IOException
   {
      d.flush();
   }


   /* Only writes one byte */
   public final void writeBoolean(boolean v) throws IOException
   {
      d.writeBoolean(v);
   }

   public final void writeByte(int v) throws IOException
   {
      d.writeByte(v);
   }

   public final void writeBytes(String s) throws IOException
   {
      d.writeBytes(s);
   }

   public final void writeUTF(String str) throws IOException
   {
      d.writeUTF(str);
   }

   public final int size() {
      return d.size();
   }

   public final void write(byte b[]) throws IOException
   {
      d.write(b, 0, b.length);
   }

   public final  void close() throws IOException
   {
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

   protected DataOutputStream d; // to get at high level write methods of DataOutputStream
   byte w[]; // work array for composing output
   protected boolean littleEndianMode = true;
} // end LEDataOutputStream
