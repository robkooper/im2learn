package edu.illinois.ncsa.isda.imagetools.core.io.pdf;
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
 *   Neither the names of University of Illinois/NCSA, nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 * 
 *******************************************************************************/



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;

public class Crypto {


  public static String encrypt(String input, String key, String xform) throws Exception {
	  SecretKey sk =  new SecretKeySpec(Base64.decode(key), xform);
	  byte[] enc = encrypt(input.getBytes(), sk);
	  return Base64.encode(enc);
  }
	
  public static byte[] encrypt(byte[] inpBytes, SecretKey key) throws Exception {
    Cipher cipher = Cipher.getInstance(key.getAlgorithm());
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(inpBytes);
  }

  
  public static String decrypt(String input, String key, String xform) throws Exception {
	  SecretKey sk =  new SecretKeySpec(Base64.decode(key), xform);
	  byte[] dec = decrypt(Base64.decode(input), sk);
	  return new String(dec);
  }
  
  public static byte[] decrypt(byte[] inpBytes, SecretKey key) throws Exception {
    Cipher cipher = Cipher.getInstance(key.getAlgorithm());
    cipher.init(Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(inpBytes);
  }

  
  
  public static String getSecretKeyString(String xform) throws NoSuchAlgorithmException {
	  return Base64.encode(getSecretKey(xform).getEncoded());
  }
  
  public static SecretKey getSecretKey(String xform) throws NoSuchAlgorithmException {
	  // Generate a secret key
	  KeyGenerator kg = KeyGenerator.getInstance(xform);
	  kg.init(128); 
	  return kg.generateKey();
	  
  }
  
  public static boolean encryptFile(File outputFile, File inputFile, String secretKey) {
	  try {
		  
		  SecretKey sk =  new SecretKeySpec(Base64.decode(secretKey), "AES");
		  Cipher ecipher = Cipher.getInstance(sk.getAlgorithm());
		  ecipher.init(Cipher.ENCRYPT_MODE, sk);
		  
		  byte[] buf = new byte[1024];
	 
		  FileInputStream in = new FileInputStream(inputFile);
		  FileOutputStream out = new FileOutputStream(outputFile);
		  CipherOutputStream cout = new CipherOutputStream(out, ecipher);
		  
		  int numByte = 0;
		  while ((numByte = in.read(buf)) >= 0) {
			  cout.write(buf, 0, numByte);
		  }
		  cout.close();
		  
		  return true;
	  } catch (Exception e) {
		  e.printStackTrace();
		  return false;
	  }
  }
  
  public static boolean decryptFile(File outputFile, File inputFile, String secretKey) {
	  try {
		  
		  SecretKey sk =  new SecretKeySpec(Base64.decode(secretKey), "AES");
		  Cipher ecipher = Cipher.getInstance(sk.getAlgorithm());
		  ecipher.init(Cipher.DECRYPT_MODE, sk);
		  
		  byte[] buf = new byte[1024];
	 
		  FileInputStream in = new FileInputStream(inputFile);
		  FileOutputStream out = new FileOutputStream(outputFile);
		  CipherOutputStream cout = new CipherOutputStream(out, ecipher);
		  
		  int numByte = 0;
		  while ((numByte = in.read(buf)) >= 0) {
			  cout.write(buf, 0, numByte);
		  }
		  cout.close();
		  
		  return true;
	  } catch (Exception e) {
		  e.printStackTrace();
		  return false;
	  }
  }
	  
  
  
  public static void main(String[] unused) throws Exception {
	  
	 // String data = "J2EE Security for Servlets, \nEJBs and Web Services";
	  
	  FileChooser fc = new FileChooser();
	  fc.setTitle("Input a file to encrypt");
	  File org = new File(fc.showOpenDialog()); 
	  File enc = new File(org.getAbsolutePath()+".aes");
	  File dec = new File(enc.getAbsolutePath()+".dec");
	  File secretkey = new File(org.getAbsolutePath()+".key");
	
	  for(int i=0; i<6; i++ ) {
	  
		  String xform = "AES";
		  
		  
		  		  
		  System.out.println("Filename = " + org.getName());
	
		  long start = System.nanoTime();
		  
		  String keyString = getSecretKeyString(xform);
		  FileOutputStream out = new FileOutputStream(secretkey);
		  PrintStream pout = new PrintStream(out);
		  pout.print(keyString);
		  pout.close();
		  out.close();
		  
		  double time = (System.nanoTime() - start)/1000000.0;
		  System.out.println("Key String = " + keyString);
		  System.out.println("Secret Key Generation Time = "+time);
		  
		  start = System.nanoTime();
		  encryptFile(enc, org, keyString);
		  time = (System.nanoTime() - start)/1000000.0;
		  System.out.println("Encryption Time = "+time);
		  
		  start = System.nanoTime();
		  decryptFile(dec, enc, keyString);
		  time = (System.nanoTime() - start)/1000000.0;
		  System.out.println("Decryption Time = "+time);
	
		  System.out.println("Original File Size = " + org.length());
		  System.out.println("Encrypted File Size = " + enc.length());
		  System.out.println("Decrypted File Size = " + dec.length());

	  }
	  
	/*  
	  System.out.println("Input = " + data);
	  System.out.println("Key String = " + keyString);
	
	  String encData = encrypt(data, keyString, xform);
	  System.out.println("Encrypted data = " + encData);
	  	  
	  String decData = decrypt(encData, keyString, xform);
	  System.out.println("Decrypted data = " + decData);
	  */
//	  boolean expected = java.util.Arrays.equals(enc.getBytes(), dec.getBytes());
//	  System.out.println("Test " + (expected ? "SUCCEEDED!" : "FAILED!"));
//	
  }
}