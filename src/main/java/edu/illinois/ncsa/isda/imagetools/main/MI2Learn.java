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
package edu.illinois.ncsa.isda.imagetools.main;




import javax.swing.*;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.imagetools.ext.calculator.ImageCalculatorDialog;
import edu.illinois.ncsa.isda.imagetools.ext.camera.CameraDialog;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.ChangeTypeDialog;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.GrayScaleDialog;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.PCADialog;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.RegistrationDialog;
import edu.illinois.ncsa.isda.imagetools.ext.geo.AlignDialog;
import edu.illinois.ncsa.isda.imagetools.ext.geo.GeoCoordValDialog;
import edu.illinois.ncsa.isda.imagetools.ext.geo.GeoDEMFeatureDialog;
import edu.illinois.ncsa.isda.imagetools.ext.geo.MosaicDialog;
import edu.illinois.ncsa.isda.imagetools.ext.geo.VectorDialog;
import edu.illinois.ncsa.isda.imagetools.ext.hyperspectral.CalibrateImageDialog;
import edu.illinois.ncsa.isda.imagetools.ext.hyperspectral.ConvertRGBDialog;
import edu.illinois.ncsa.isda.imagetools.ext.hyperspectral.HyperSpectralFusionDialog;
import edu.illinois.ncsa.isda.imagetools.ext.hyperspectral.IlluminateImageDialog;
import edu.illinois.ncsa.isda.imagetools.ext.info.*;
import edu.illinois.ncsa.isda.imagetools.ext.panel.*;
import edu.illinois.ncsa.isda.imagetools.ext.segment.BoundBoxDialog;
import edu.illinois.ncsa.isda.imagetools.ext.segment.HSVThresholdDialog;
import edu.illinois.ncsa.isda.imagetools.ext.segment.Seg2DBallDialog;
import edu.illinois.ncsa.isda.imagetools.ext.segment.Seg2DDialog;
import edu.illinois.ncsa.isda.imagetools.ext.segment.Seg2DSuperDialog;
import edu.illinois.ncsa.isda.imagetools.ext.segment.ThresholdDialog;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.HistogramDialog;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.ImageCompareDialog;
import edu.illinois.ncsa.isda.imagetools.ext.vis.FakeRGBColorDialog;
import edu.illinois.ncsa.isda.imagetools.ext.vis.ImageExtractDialog;
import edu.illinois.ncsa.isda.imagetools.ext.vis.MagWindow;
import edu.illinois.ncsa.isda.imagetools.ext.vis.PlayBandDialog;
import edu.illinois.ncsa.isda.imagetools.ext.vis.PseudoImageDialog;

/**
 * This is the NCSA version of Im2Learn. All menus will be enabled.
 */
public class MI2Learn extends Im2LearnMainFrame {
    /**
     * Default constructor shows the Im2Learn Image
     */
    public MI2Learn() {
        super();
    }

    /**
     * Create the MainFrame with all the menus
     */
    public MI2Learn(String filename) {
        super(filename);
    }

    /**
     * Create the MainFrame with the menus
     */
    public MI2Learn(ImageObject image) {
        super(image);
    }
    
    public void addMenus() {
        // allow selection
       getImagePanel().setSelectionAllowed(true);

        // information dialogs
        addMenu(new InfoDialog());
        addMenu(new PlayBandDialog());
        addMenu(new PseudoImageDialog());
        addMenu(new CoordValDialog());

        addMenu(new ImageExtractDialog());
        
        // zoom
       addMenu(new ZoomDialog());
       addMenu(new MagWindow());
       
        // selection dialogs
        addMenu(new SelectionDialog());
        addMenu(new CropDialog());

        // markers
        addMenu(new AnnotationDialog());

        // color change
        addMenu(new GammaDialog());
        addMenu(new SelectBandDialog());
        addMenu(new UseTotalsDialog());
        addMenu(new GrayScaleDialog());
        addMenu(new FakeRGBColorDialog());

        // change image type
        addMenu(new ChangeTypeDialog());

        // perform PCA
        addMenu(new PCADialog());

   
        // image comparison
        addMenu(new ImageCompareDialog());
        addMenu(new HistogramDialog());
        addMenu(new ImageCalculatorDialog());
        addMenu(new Seg2DSuperDialog());
        addMenu(new Seg2DBallDialog());
        addMenu(new Seg2DDialog());
        addMenu(new ThresholdDialog());
        addMenu(new BoundBoxDialog());
        addMenu(new HSVThresholdDialog());
        
        // image registration
        addMenu(new RegistrationDialog());

        // debug
        addMenu(new DebugDialog());

        
      
    }

    /**
     * Start of Im2Learn. Make sure to start the logger.
     *
     * @param args for Im2Learn
     */
    static public void main(String[] args) {

        // change L&F to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }

        // start Im2Learn
        if (args.length > 0) {
            new MI2Learn(args[0]);
        } else {
            new MI2Learn();
        }
    }
}
