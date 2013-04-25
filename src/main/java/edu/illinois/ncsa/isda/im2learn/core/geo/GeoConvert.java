package edu.illinois.ncsa.isda.im2learn.core.geo;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;

public class GeoConvert {
    static public ImageObject toGeoGraphic(ImageObject imgobj) throws ImageException {
        if (imgobj.getProperty(ImageObject.GEOINFO) == null) {
            throw (new ImageException("No projection associated with image."));
        }

        Projection proj = ProjectionConvert.getNewProjection(imgobj.getProperty(ImageObject.GEOINFO));

        // find min max lat lon
        BoundingBox bb = GeoUtilities.getEarthBounds(imgobj, proj);

        // create geographic projection
        Projection proj2 = Projection.getProjection(ProjectionType.Geographic, proj.getGeographicCoordinateSystem());
        proj2.setTiePoint(proj.getTiePoint());

        // create the image
        RasterPoint ul = proj2.earthToRaster(new GeodeticPoint(bb.getMaxY(), bb.getMinX()));
        RasterPoint lr = proj2.earthToRaster(new GeodeticPoint(bb.getMinY(), bb.getMaxX()));

        int w = (int) (ul.getX() - lr.getX());
        int h = (int) (ul.getY() - lr.getY());
        int b = imgobj.getNumBands();

        ImageObject result = ImageObject.createImage(h, w, b, imgobj.getType());
        result.setProperty(ImageObject.GEOINFO, proj2);
        result.setInvalidData(imgobj.getInvalidData());

        // copy the pixels
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                GeodeticPoint gp = proj.rasterToEarth(new RasterPoint(y, x));
                RasterPoint rp = proj2.earthToRaster(gp);
                for (int z = 0; z < b; z++) {
                    result.set(y, x, z, imgobj.getDouble((int) rp.getRow(), (int) rp.getCol(), z));
                }
            }
        }

        // done
        return result;
    }
}
