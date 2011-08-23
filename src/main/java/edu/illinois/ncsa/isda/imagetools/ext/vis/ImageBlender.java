package edu.illinois.ncsa.isda.imagetools.ext.vis;

import javax.swing.UIManager;


import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.segment.ColorModels;


public class ImageBlender {

  public ImageBlender() {
  }
  
  static public ImageObject HSVBlending(ImageObject imA, ImageObject imB, double weightA) {
	  ColorModels col = new ColorModels();
	  col.convertRGB2HSV(imA);
	  ImageObject imAH = col.getConvertedIm();
	  
	  col.convertRGB2HSV(imB);
	  ImageObject imBH = col.getConvertedIm();
	  
	  ImageObject bImH = null;
	  try {
		bImH = ImageObject.createImage(Math.max(imA.getNumRows(), imB.getNumRows()), 
				  									 Math.max(imA.getNumCols(), imB.getNumCols()),
				  									 3, ImageObject.TYPE_FLOAT);

		for(int j=0; j<bImH.getNumRows(); j++) {
			for(int i=0; i<bImH.getNumCols(); i++) {
				for(int k=0; k<3; k++) {
					if(i >= imAH.getNumCols() || j >= imAH.getNumRows()) {
						bImH.set(j,i,k, imBH.getFloat(j, i, k));
					} 
					else if(i >= imBH.getNumCols() || j >= imBH.getNumRows()) {
						bImH.set(j,i,k, imAH.getFloat(j, i, k));
					}
					else {
						try{
							if(k == 0) {
								if(Math.abs(imAH.getFloat(j, i, 0) - imBH.getFloat(j, i, 0)) > 180) {
									bImH.set(j,i,k, imAH.getFloat(j, i, k)*weightA + imBH.getFloat(j, i, k)*(1-weightA));
								}
								else {
									
								}
							} else {
								bImH.set(j,i,k, imAH.getFloat(j, i, k)*weightA + imBH.getFloat(j, i, k)*(1-weightA));
								//System.out.println(imAH.getFloat(j, i, k)*weightA + imBH.getFloat(j, i, k)*(1-weightA));
							}
						}
						catch(Exception e){
					//		System.out.println(i+" "+j);
						}
					}
				}
				
				
			}
		}
		
	  } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		

	 // return bImH;
	  
	  col.convertHSV2RGB(bImH);
			  
	  return col.getConvertedIm();
  }
  
  static public ImageObject RGBBlending(ImageObject imA, ImageObject imB, double weightA) {
	  	  
	  ImageObject bIm = null;
	  try {
		bIm = ImageObject.createImage(Math.max(imA.getNumRows(), imB.getNumRows()), 
				  									 Math.max(imA.getNumCols(), imB.getNumCols()),
				  									 3, ImageObject.TYPE_BYTE);

		for(int j=0; j<bIm.getNumRows(); j++) {
			for(int i=0; i<bIm.getNumCols(); i++) {
				for(int k=0; k<3; k++) {
					if(i >= imA.getNumCols() || j >= imA.getNumRows()) {
						bIm.set(j,i,k, imB.getFloat(j, i, k));
					} 
					else if(i >= imB.getNumCols() || j >= imB.getNumRows()) {
						bIm.set(j,i,k, imA.getFloat(j, i, k));
					}
					else {
						try{
							bIm.set(j,i,k, imA.getFloat(j, i, k)*weightA + imB.getFloat(j, i, k)*(1-weightA));
							//System.out.println(imAH.getFloat(j, i, k)*weightA + imBH.getFloat(j, i, k)*(1-weightA));
						}
						catch(Exception e){
					//		System.out.println(i+" "+j);
						}
					}
				}
				
				
			}
		}
		
	  } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		

	  return bIm;
  }
  
  public static void main(String[] args) {
	   try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception ex) {
          LogFactory.getLog("").error("Unable to load native look and feel");
      }
      FileChooser fc = new FileChooser();
   
      try{
    	  ImageObject a = ImageLoader.readImage(fc.showOpenDialog());
    	  ImageObject b = ImageLoader.readImage(fc.showOpenDialog());
//    	  new MI2Learn(a);
//          new MI2Learn(b);
  //        new MI2Learn(HSVBlending(a,b,0.8));
       //   new MI2Learn(RGBBlending(a,b,0.2));
      }
      catch(Exception e) {
    	  e.printStackTrace();
      }
      
      
      	
      
  }
}

