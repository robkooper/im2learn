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
package edu.illinois.ncsa.isda.im2learn.main;

import javax.swing.UIManager;


import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.im2learn.ext.calculator.ImageCalculatorDialog;
import edu.illinois.ncsa.isda.im2learn.ext.camera.CameraDialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.ChangeTypeDialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.GrayScaleDialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.PCADialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.RegistrationDialog;
import edu.illinois.ncsa.isda.im2learn.ext.geo.AlignDialog;
import edu.illinois.ncsa.isda.im2learn.ext.geo.GeoCoordValDialog;
import edu.illinois.ncsa.isda.im2learn.ext.geo.GeoDEMFeatureDialog;
import edu.illinois.ncsa.isda.im2learn.ext.geo.MosaicDialog;
import edu.illinois.ncsa.isda.im2learn.ext.geo.VectorDialog;
import edu.illinois.ncsa.isda.im2learn.ext.hyperspectral.CalibrateImageDialog;
import edu.illinois.ncsa.isda.im2learn.ext.hyperspectral.ConvertRGBDialog;
import edu.illinois.ncsa.isda.im2learn.ext.hyperspectral.HyperSpectralFusionDialog;
import edu.illinois.ncsa.isda.im2learn.ext.hyperspectral.IlluminateImageDialog;
import edu.illinois.ncsa.isda.im2learn.ext.info.CoordValDialog;
import edu.illinois.ncsa.isda.im2learn.ext.info.DebugDialog;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.AnnotationDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.CropDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.GammaDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectionDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.UseTotalsDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.BoundBoxDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.HSVThresholdDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.Seg2DSuperDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ThresholdDialog;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.HistogramDialog;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.ImageCompareDialog;
import edu.illinois.ncsa.isda.im2learn.ext.texture.GaborFilterBankDialog;
import edu.illinois.ncsa.isda.im2learn.ext.texture.IsTextureDialog;
import edu.illinois.ncsa.isda.im2learn.ext.vis.FakeRGBColorDialog;
import edu.illinois.ncsa.isda.im2learn.ext.vis.ImageExtractDialog;
import edu.illinois.ncsa.isda.im2learn.ext.vis.PlayBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.vis.PseudoImageDialog;

/**
 * This is the NCSA version of Im2Learn. All menus will be enabled.
 */
public class Im2LearnNCSA extends Im2LearnMainFrame {
    /**
     * Default constructor shows the Im2Learn Image
     */
    public Im2LearnNCSA() {
        this((String) null);
    }

    /**
     * Create the MainFrame with all the menus
     */
    public Im2LearnNCSA(String filename) {
        super(filename);
    }

    /**
     * Create the MainFrame with all the menus
     */
    public Im2LearnNCSA(ImageObject image) {
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

        // zoom
        addMenu(new ZoomDialog());

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

        // hyperspectral functions
        addMenu(new ConvertRGBDialog());
        addMenu(new CalibrateImageDialog());
        addMenu(new IlluminateImageDialog());
        addMenu(new HyperSpectralFusionDialog());

        // image comparison
        addMenu(new ImageCompareDialog());
        addMenu(new HistogramDialog());
        addMenu(new ImageCalculatorDialog());
        addMenu(new Seg2DSuperDialog());
        addMenu(new ThresholdDialog());
        addMenu(new BoundBoxDialog());
        addMenu(new HSVThresholdDialog());

        // image registration
        addMenu(new RegistrationDialog());

        // debug
        addMenu(new DebugDialog());

        // geo stuff
        addMenu(new GeoCoordValDialog(this));
        addMenu(new GeoDEMFeatureDialog());
        addMenu(new MosaicDialog());
        addMenu(new AlignDialog());
        addMenu(new VectorDialog());

        // camera capture
        addMenu(new CameraDialog());

        // Image Extract
        addMenu(new ImageExtractDialog());
        addMenu(new IsTextureDialog());
        addMenu(new GaborFilterBankDialog());
    }

    /**
     * Start of Im2Learn. Make sure to start the logger.
     * 
     * @param args
     *            for Im2Learn
     */
    static public void main(String[] args) {

        // change L&F to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LogFactory.getLog("").error("Unable to load native look and feel");
        }

        // start Im2Learn
        if (args.length > 0) {
            new Im2LearnNCSA(args[0]);
        } else {
            new Im2LearnNCSA((String) null);
        }
    }
}
