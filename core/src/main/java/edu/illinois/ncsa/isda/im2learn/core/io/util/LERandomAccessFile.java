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
 * LERandomAccessFile.java
 *
 * Very similar to RandomAccessFile except it reads/writes
 * little-endian instead of big-endian binary data.
 * RandomAccessFile methods are final, so we cannot simply extend it.
 * We have to build a wrapper class.
 */

import java.io.*;

public
class LERandomAccessFile implements DataInput, DataOutput {

    /**
     * constructors
     */
   public LERandomAccessFile(String f, String rw) throws IOException
   {
      r = new RandomAccessFile(f, rw);
      w = new byte[8];
   }

   public LERandomAccessFile(File f, String rw) throws IOException
   {
      r = new RandomAccessFile(f, rw);
      w = new byte[8];
   }

   // L I T T L E   E N D I A N   R E A D E R S
   // Little endian methods for multi-byte numeric types.
   // Big-endian do fine for single-byte types and strings.

   /**
     * like RandomAcessFile.readShort except little endian.
     */
   public final short readShort() throws IOException
   {
      r.readFully(w, 0, 2);
      return (short)(
                    (w[1]&0xff) << 8 |
                    (w[0]&0xff));
   }

   /**
     * like RandomAcessFile.readUnsignedShort except little endian.
     * Note, returns int even though it reads a short.
     */
   public final int readUnsignedShort() throws IOException
   {
      r.readFully(w, 0, 2);
      return (
             (w[1]&0xff) << 8 |
             (w[0]&0xff));
   }

   /**
     * like RandomAcessFile.readChar except little endian.
     */
   public final char readChar() throws IOException
   {
      r.readFully(w, 0, 2);
      return (char) (
                    (w[1]&0xff) << 8 |
                    (w[0]&0xff));
   }

   /**
     * like RandomAcessFile.readInt except little endian.
     */
   public final int readInt() throws IOException
   {
      r.readFully(w, 0, 4);
      return
      (w[3])      << 24 |
      (w[2]&0xff) << 16 |
      (w[1]&0xff) <<  8 |
      (w[0]&0xff);
   }

   /**
     * like RandomAcessFile.readLong except little endian.
     */
   public final long readLong() throws IOException
   {
      r.readFully(w, 0, 8);
      return
      (long)(w[7])      << 56 | /* long cast necessary or shift done modulo 32 */
      (long)(w[6]&0xff) << 48 |
      (long)(w[5]&0xff) << 40 |
      (long)(w[4]&0xff) << 32 |
      (long)(w[3]&0xff) << 24 |
      (long)(w[2]&0xff) << 16 |
      (long)(w[1]&0xff) <<  8 |
      (long)(w[0]&0xff);
   }

   /**
     * like RandomAcessFile.readFloat except little endian.
     */
   public final float readFloat() throws IOException
   {
      return Float.intBitsToFloat(readInt());
   }

   /**
     * like RandomAcessFile.readDouble except little endian.
     */
   public final double readDouble() throws IOException
   {
      return Double.longBitsToDouble(readLong());
   }

   // L I T T L E   E N D I A N   W R I T E R S
   // Little endian methods for multi-byte numeric types.
   // Big-endian do fine for single-byte types and strings.

   /**
     * like RandomAcessFile.writeShort.
     * also acts as a writeUnsignedShort
     */
   public final void writeShort(int v) throws IOException
   {
      w[0] = (byte) v;
      w[1] = (byte)(v >> 8);
      r.write(w, 0, 2);
   }

   /**
    * like RandomAcessFile.writeChar.
    * Note the parm is an int even though this as a writeChar
    */
   public final void writeChar(int v) throws IOException
   {
      // same code as writeShort
      w[0] = (byte) v;
      w[1] = (byte)(v >> 8);
      r.write(w, 0, 2);
   }

   /**
     * like RandomAcessFile.writeInt.
     */
   public final void writeInt(int v) throws IOException
   {
      w[0] = (byte) v;
      w[1] = (byte)(v >> 8);
      w[2] = (byte)(v >> 16);
      w[3] = (byte)(v >> 24);
      r.write(w, 0, 4);
   }

   /**
     * like RandomAcessFile.writeLong.
     */
   public final void writeLong(long v) throws IOException
   {
      w[0] = (byte) v;
      w[1] = (byte)(v >> 8);
      w[2] = (byte)(v >> 16);
      w[3] = (byte)(v >> 24);
      w[4] = (byte)(v >> 32);
      w[5] = (byte)(v >> 40);
      w[6] = (byte)(v >> 48);
      w[7] = (byte)(v >> 56);
      r.write(w, 0, 8);
   }

   /**
     * like RandomAcessFile.writeFloat.
     */
   public final void writeFloat(float v) throws IOException
   {
      writeInt(Float.floatToIntBits(v));
   }

   /**
     * like RandomAcessFile.writeDouble.
     */
   public final void writeDouble(double v) throws IOException
   {
      writeLong(Double.doubleToLongBits(v));
   }

   /**
     * like RandomAcessFile.writeChars, has to flip each char.
     */
   public final void writeChars(String s) throws IOException
   {
      int len = s.length();
      for ( int i = 0 ; i < len ; i++ ) {
         writeChar(s.charAt(i));
      }
   } // end writeChars

   // p u r e l y   w r a p p e r   m e t h o d s

   public final FileDescriptor getFD() throws IOException
   {
      return r.getFD();
   }

   public final long getFilePointer() throws IOException
   {
      return r.getFilePointer();
   }

   public final long length() throws IOException
   {
      return r.length();
   }

   public final int read(byte b[], int off, int len) throws IOException
   {
      return r.read(b, off, len);
   }

   public final int read(byte b[]) throws IOException
   {
      return r.read(b);
   }

   public final int read() throws IOException
   {
      return r.read();
   }

   public final void readFully(byte b[]) throws IOException
   {
      r.readFully(b, 0, b.length);
   }

   public final void readFully(byte b[], int off, int len) throws IOException
   {
      r.readFully(b,off,len);
   }

   public final int skipBytes(int n) throws IOException
   {
      return r.skipBytes(n);
   }

   /* OK, reads only only 1 byte */
   public final boolean readBoolean() throws IOException
   {
      return r.readBoolean();
   }

   public final byte readByte() throws IOException
   {
      return r.readByte();
   }

   // note: returns an int, even though says Byte.
   public final int readUnsignedByte() throws IOException
   {
      return r.readUnsignedByte();
   }

   public final String readLine() throws IOException
   {
      return r.readLine();
   }

   public final String readUTF() throws IOException
   {
      return r.readUTF();
   }

   public final void seek(long pos) throws IOException
   {
      r.seek(pos);
   }

   /* Only writes one byte even though says int */
   public final synchronized void write(int b) throws IOException
   {
      r.write(b);
   }

   public final synchronized void write(byte b[], int off, int len)
   throws IOException
   {
      r.write(b, off, len);
   }

   public final void writeBoolean(boolean v) throws IOException
   {
      r.writeBoolean(v);
   }

   public final void writeByte(int v) throws IOException
   {
      r.writeByte(v);
   }

   public final void writeBytes(String s) throws IOException
   {
      r.writeBytes(s);
   }

   public final void writeUTF(String str) throws IOException
   {
      r.writeUTF(str);
   }

   public final void write(byte b[]) throws IOException
   {
      r.write(b, 0, b.length);
   }

   public final  void close() throws IOException
   {
      r.close();
   }

   // i n s t a n c e   v a r i a b l e s

   protected RandomAccessFile r;
   byte w[]; // work array for buffering input/output

} // end class LERandomAccessFile
