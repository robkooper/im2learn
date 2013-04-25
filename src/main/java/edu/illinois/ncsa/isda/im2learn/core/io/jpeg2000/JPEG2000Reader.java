package edu.illinois.ncsa.isda.im2learn.core.io.jpeg2000;

import icc.ICCProfileException;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import jj2000.disp.BlkImgDataSrcImageProducer;
import jj2000.j2k.codestream.HeaderInfo;
import jj2000.j2k.codestream.reader.BitstreamReaderAgent;
import jj2000.j2k.codestream.reader.HeaderDecoder;
import jj2000.j2k.decoder.Decoder;
import jj2000.j2k.decoder.DecoderSpecs;
import jj2000.j2k.encoder.Encoder;
import jj2000.j2k.entropy.decoder.EntropyDecoder;
import jj2000.j2k.fileformat.reader.FileFormatReader;
import jj2000.j2k.image.BlkImgDataSrc;
import jj2000.j2k.image.ImgDataConverter;
import jj2000.j2k.image.invcomptransf.InvCompTransf;
import jj2000.j2k.io.BEBufferedRandomAccessFile;
import jj2000.j2k.io.RandomAccessIO;
import jj2000.j2k.quantization.dequantizer.Dequantizer;
import jj2000.j2k.roi.ROIDeScaler;
import jj2000.j2k.util.FacilityManager;
import jj2000.j2k.util.MsgLogger;
import jj2000.j2k.util.ParameterList;
import jj2000.j2k.wavelet.synthesis.InverseWT;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import colorspace.ColorSpace;
import colorspace.ColorSpaceException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectInt;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageWriter;
import edu.illinois.ncsa.isda.im2learn.core.io.pnm.PNMLoader;

public class JPEG2000Reader implements ImageReader, ImageWriter {
    private static Log log = LogFactory.getLog(JPEG2000Reader.class);

    /**
     * Returns true if the filename ends with j2k or jp2.
     * 
     * @param filename
     *            ignored.
     * @param hdr
     *            the first 100 bytes of the file.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr) {
        if ((hdr[0] == 0) && (hdr[1] == 0) && (hdr[2] == 0) && (hdr[3] == 12) && (hdr[4] == 106) && (hdr[5] == 80) && (hdr[6] == 32) && (hdr[7] == 32) && (hdr[8] == 13) && (hdr[9] == 10) && (hdr[10] == -121) && (hdr[11] == 10)) {
            return true;
        }
        String ext = ImageLoader.getExtention(filename);
        if (ext == null) {
            return false;
        }
        return ext.equals("j2k") || ext.equals("jp2");
    }

    public int getImageCount(String filename) {
        return 1;
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the file.
     * 
     * @param filename
     *            of the file to be read
     * @param subarea
     *            of the file to be read, null if full image is to be read.
     * @param sampling
     *            is the subsampling that needs to be done on the image as it is
     *            loaded.
     * @return the file as an imageobject.
     * @throws IOException
     *             if the file could not be read.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
        return readImage(filename, subarea, sampling, false);
    }

    public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException {
        return readImage(filename, subarea, sampling, false);
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the information of the image but not the imagedata itself.
     * 
     * @param filename
     *            of the file to be read
     * @return the file as an imageobject except of the imagedata.
     * @throws IOException
     *             if the file could not be read.
     */
    public ImageObject readImageHeader(String filename) throws IOException {
        return readImage(filename, null, 1, true);
    }

    public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
        return readImage(filename, null, 1, true);
    }

    private ImageObject readImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException {
        RandomAccessIO in = null;

        try {
            // Initialize default parameters
            ParameterList dfl = new ParameterList();
            String[][] param = Decoder.getAllParameters();

            for (int i = param.length - 1; i >= 0; i-- ) {
                if (param[i][3] != null) {
                    dfl.put(param[i][0], param[i][3]);
                }
            }

            // Set the parameters
            ParameterList pl = new ParameterList(dfl);
            pl.put("i", filename);

            in = new BEBufferedRandomAccessFile(pl.getParameter("i"), "r");

            // **** File Format ****
            // If the codestream is wrapped in the jp2 fileformat, Read the
            // file format wrapper
            FileFormatReader ff = new FileFormatReader(in);
            ff.readFileFormat();
            if (ff.JP2FFUsed) {
                in.seek(ff.getFirstCodeStreamPos());
            }

            // +----------------------------+
            // | Instantiate decoding chain |
            // +----------------------------+

            // **** Header decoder ****
            // Instantiate header decoder and read main header
            HeaderInfo hi = new HeaderInfo();
            HeaderDecoder hd = new HeaderDecoder(in, pl, hi);

            int nCompCod = hd.getNumComps();
            int nTiles = hi.siz.getNumTiles();
            DecoderSpecs decSpec = hd.getDecoderSpecs();

            // Report information
            if (log.isInfoEnabled()) {
                String info = nCompCod + " component(s) in codestream, " + nTiles + " tile(s)\n";
                info += "Image dimension: ";
                for (int c = 0; c < nCompCod; c++ ) {
                    info += hi.siz.getCompImgWidth(c) + "x" + hi.siz.getCompImgHeight(c) + " ";
                }

                if (nTiles != 1) {
                    info += "\nNom. Tile dim. (in canvas): " + hi.siz.xtsiz + "x" + hi.siz.ytsiz;
                }
                log.info(info);
            }

            if (pl.getBooleanParameter("cdstr_info")) {
                log.info("Main header:\n" + hi.toStringMainHeader());
            }

            // Get demixed bitdepths
            int[] depth = new int[nCompCod];
            for (int i = 0; i < nCompCod; i++ ) {
                depth[i] = hd.getOriginalBitDepth(i);
            }

            // **** Bit stream reader ****
            BitstreamReaderAgent breader = BitstreamReaderAgent.createInstance(in, hd, pl, decSpec, pl.getBooleanParameter("cdstr_info"), hi);

            // **** Entropy decoder ****
            EntropyDecoder entdec = hd.createEntropyDecoder(breader, pl);

            // **** ROI de-scaler ****
            ROIDeScaler roids = hd.createROIDeScaler(entdec, pl, decSpec);

            // **** Dequantizer ****
            Dequantizer deq = hd.createDequantizer(roids, depth, decSpec);

            // **** Inverse wavelet transform ***
            // full page inverse wavelet transform
            InverseWT invWT = InverseWT.createInstance(deq, decSpec);

            int res = breader.getImgRes();
            invWT.setImgResLevel(res);

            // **** Data converter **** (after inverse transform module)
            ImgDataConverter converter = new ImgDataConverter(invWT, 0);

            // **** Inverse component transformation ****
            InvCompTransf ictransf = new InvCompTransf(converter, decSpec, depth, pl);

            // **** Color space mapping ****
            BlkImgDataSrc color;

            if (ff.JP2FFUsed && pl.getParameter("nocolorspace").equals("off")) {
                try {
                    ColorSpace csMap = new ColorSpace(in, hd, pl);
                    BlkImgDataSrc channels = hd.createChannelDefinitionMapper(ictransf, csMap);
                    BlkImgDataSrc resampled = hd.createResampler(channels, csMap);
                    BlkImgDataSrc palettized = hd.createPalettizedColorSpaceMapper(resampled, csMap);
                    color = hd.createColorSpaceMapper(palettized, csMap);

                    if (csMap.debugging()) {
                        FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + csMap);
                        FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + channels);
                        FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + resampled);
                        FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + palettized);
                        FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + color);
                    }
                } catch (ColorSpaceException cse) {
                    throw (new IOException(cse));
                } catch (ICCProfileException ipe) {
                    throw (new IOException(ipe));
                }
            } else { // Skip colorspace mapping
                color = ictransf;
            }

            // This is the last image in the decoding chain and should be
            // assigned by the last transformation:
            BlkImgDataSrc decodedImage = color;
            if (color == null) {
                decodedImage = ictransf;
            }

            JPEG2000Consumer jc = new JPEG2000Consumer(decodedImage, subarea, sampling, header);
            if (!header) {
                new BlkImgDataSrcImageProducer(decodedImage).startProduction(jc);
            }
            return jc.getImageObject();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    class JPEG2000Consumer implements ImageConsumer {
        private ImageObject imgobj;
        private SubArea     subarea;
        private int         sampling;
        private int         total;

        public JPEG2000Consumer(BlkImgDataSrc decodedImage, SubArea subarea, int sampling, boolean header) {
            this.subarea = subarea;
            this.sampling = sampling;

            total = decodedImage.getImgHeight();

            int w = 0;
            int h = 0;
            if (subarea == null) {
                w = decodedImage.getCompImgWidth(0);
                h = decodedImage.getCompImgHeight(0);
            } else {
                w = subarea.width;
                h = subarea.height;
            }

            if (sampling > 1) {
                w = (int) Math.ceil((double) w / sampling);
                h = (int) Math.ceil((double) h / sampling);
            }

            if (decodedImage.getNumComps() == 1) {
                imgobj = new ImageObjectByte(h, w, 1, header);
            } else {
                imgobj = new ImageObjectInt(h, w, 1, header);
            }
        }

        public ImageObject getImageObject() {
            return imgobj;
        }

        @Override
        public void imageComplete(int status) {
        }

        @Override
        public void setColorModel(ColorModel model) {
        }

        @Override
        public void setDimensions(int width, int height) {
        }

        @Override
        public void setHints(int hintflags) {
        }

        @Override
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
            log.error("setPixels(byte[]) is not implemented.");
        }

        @Override
        public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
            for (int r = y; r < y + h; r++ ) {
                ImageLoader.fireProgress(r, total);

                int dy = r;
                int i = off;
                off += scansize;

                if (subarea != null) {
                    if ((dy < subarea.getMinY()) || (dy >= subarea.getMaxY())) {
                        continue;
                    }
                    dy = dy - subarea.y;
                }
                if (sampling > 1) {
                    if (dy % sampling != 0) {
                        continue;
                    }
                    dy = dy / sampling;
                }
                for (int c = x; c < x + w; c++, i++ ) {
                    int dx = c;

                    if (subarea != null) {
                        if ((dx < subarea.getMinX()) || (dx >= subarea.getMaxX())) {
                            continue;
                        }
                        dx = dx - subarea.x;
                    }
                    if (sampling > 1) {
                        if (dx % sampling != 0) {
                            continue;
                        }
                        dx = dx / sampling;
                    }
                    try {
                        imgobj.set(dy, dx, 0, pixels[i]);
                    } catch (Throwable thr) {
                        thr.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void setProperties(Hashtable<?, ?> props) {
        }
    }

    /**
     * Return a list of extentions this class can read.
     * 
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return new String[] { "j2k", "jp2" };
    }

    /**
     * Return the description of the reader (or writer).
     * 
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "PNM files";
    }

    /**
     * Returns true if the class can write the file.
     * 
     * @param filename
     *            of the file to be written.
     * @return true if the file can be written by this class.
     */
    public boolean canWrite(String filename) {
        String ext = ImageLoader.getExtention(filename);
        if (ext == null) {
            return false;
        }
        return ext.equals("j2k") || ext.equals("jp2");
    }

    /**
     * Return a list of extentions this class can write.
     * 
     * @return a list of extentions that are understood by this class.
     */
    public String[] writeExt() {
        return new String[] { "j2k", "jp2" };
    }

    /**
     * This function will write the imageobject to a file.
     * 
     * @param filename
     *            of the file to be written.
     * @param imageobject
     *            the image image to be written.
     * @throws IOException
     *             if the file could not be written.
     */
    public void writeImage(String filename, ImageObject imageobject) throws IOException {
        File ppm = File.createTempFile("im2learn", ".ppm");

        try {
            // Initialize default parameters
            ParameterList dfl = new ParameterList();
            String[][] param = Encoder.getAllParameters();

            for (int i = param.length - 1; i >= 0; i-- ) {
                if (param[i][3] != null) {
                    dfl.put(param[i][0], param[i][3]);
                }
            }

            // Set the parameters
            ParameterList pl = new ParameterList(dfl);
            // pl.put("debug", "on");
            pl.put("i", ppm.getAbsolutePath());
            pl.put("o", filename);

            // Instantiate the Encoder object
            Encoder enc = new Encoder(pl);
            if (enc.getExitCode() != 0) {
                throw (new IOException("Could not parse parameters."));
            }

            // save the ppm
            new PNMLoader().writeImage(ppm.getAbsolutePath(), imageobject);

            // Run the decoder
            enc.run();
            if (enc.getExitCode() != 0) {
                throw (new IOException("Could not encode image."));
            }

        } finally {
            ppm.delete();
        }
    }
}
