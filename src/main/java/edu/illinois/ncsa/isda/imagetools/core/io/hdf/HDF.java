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
package edu.illinois.ncsa.isda.imagetools.core.io.hdf;

import ncsa.hdf.object.*;
import ncsa.hdf.object.h4.H4File;
import ncsa.hdf.object.h5.H5File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * This class will encapsulate all HDF methods. All methods are static and can
 * be used by other classes.
 */
public class HDF {
    static private Log logger = LogFactory.getLog(HDF.class);
    static public String propext = ".properties";

    // -----------------------------------------------------------------------
    // HDF wrapper methods.
    // These are convience methods that can be used by other classes as well
    // to interact with HDF files.
    // -----------------------------------------------------------------------

    /**
     * Checks to see if the given filename is a HDF file.
     *
     * @param filename to check for HDF file.
     * @return true if the file is an HDF file, false otherwise
     */
    static public boolean isHDF(String filename) {
        try {
            return getFileFormat(filename) != null;
        } catch (Throwable thr) {
            //logger.debug("No HDF support. Are HDF libraries in path?");
            // No HDF found most likely.
            return false;
        }
    }

    /**
   * Return true if the HDFEOS version attribute exists on the top-level group.
   */
   static public boolean isEOS(String filename) throws IOException {
    // composite file (maybe), split into 2 filenames
    String names[] = filename.split("#", 2);

    try {
      // Get the top-level group.  The EOS metadata are attributes of this group.
      HObject group = (HObject) HDF.openFile(names[0] + "#/", false, false);

      // Get the attributes of the top-level group.
      // This is a hash table where the key is the name of the property,
      // the value is the property.
      // The keys will be :
      //      CoreMetadata.0
      //      StructMetadata.0
      //      ArchiveMetadata.0
      //      HDFEOSVersion      
      HashMap props = HDF.readAttributes(null, group, null);
      logger.debug("Deciding HDF EOS......");
      if (props != null) {
          if(props.get("HDFEOSVersion") != null)
          {
              logger.debug("Is HDF EOS");
            return true;
          }
      }
    }
    catch(Throwable thr) {
      logger.error("error checking for EOS.", thr);
      return false;
    }
    logger.debug("Not HDF EOS");
    return false;
  }


    /**
     * Checks the file and see if it is an HDF file. Will return the correct
     * fileformat for the file.
     *
     * @param filename to check.
     * @return fileformat of the file.
     */
    static public FileFormat getFileFormat(String filename) {
        File file = new File(filename);
        filename = file.getName();
        if (file.exists()) {
            for (Enumeration e = FileFormat.getFileFormatKeys(); e.hasMoreElements();) {
                Object key = e.nextElement();
                FileFormat ff = FileFormat.getFileFormat(key.toString());
                if (ff == null) {
                    logger.info("No " + key + " support?");
                } else if (ff.isThisType(file.getAbsolutePath())) {
                    return ff;
                }
            }
            return null;
        } else {
            if (filename.endsWith(".h5")) {
                return FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
            }
            if (filename.endsWith(".h4") || filename.endsWith(".hdf")) {
                return FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF4);
            }
            return null;
        }
    }

    static public Object readData(String filename) throws IOException {
        Object hdfobj = openFile(filename, false, true);
        Object result = readData(hdfobj);
        closeFile(hdfobj);
        return result;
    }

    static public Object readData(Object hdfobj) throws IOException {
        if (hdfobj instanceof ScalarDS) {
            ScalarDS scalards = (ScalarDS) hdfobj;
            try {
                scalards.init();
                long[] sdims = scalards.getDims();
                long[] select = scalards.getSelectedDims();
                System.arraycopy(sdims, 0, select, 0, sdims.length);
                Object data = scalards.read();
                int alen = select.length;

                // now split the array correctly
                if (alen == 1) {
                    return data;
                } else {
                    int[] dims = new int[alen];
                    for (int i = 0; i < alen; i++) {
                        dims[i] = (int) select[i];
                    }
                    Class clazz = data.getClass();
                    while (clazz.isArray()) {
                        clazz = clazz.getComponentType();
                    }
                    Object arr = Array.newInstance(clazz, dims);
                    hdfToArray(data, 0, arr, dims, 0);
                    return arr;
                }
            } catch (Exception exc) {
                throw(new IOException("Could not read data."));
            }
        } else {
            throw(new IOException("Error reading data."));
        }
    }

    static private int hdfToArray(Object src, int start, Object dst, int[] dim, int idx) {
        if (idx == dim.length - 1) {
            System.arraycopy(src, start, dst, 0, dim[idx]);
            return start + dim[idx];
        } else {
            for (int i = 0; i < dim[idx]; i++) {
                start = hdfToArray(src, start, Array.get(dst, i), dim, idx + 1);
            }
            return start;
        }
    }

    /**
     * Attributes can be a maximum size of 64K. To overcome this limitation the
     * properties of an image are stored in a directory called
     * &lt;filename&gt;.properties with a file for each property. If the file
     * has attribute serialized set to true then the object was a java object
     * and will be deserialized.
     *
     * @param properties hashmap into which to store the properties, if null is
     *                   passed in a new hashmap will be created.
     * @param filename   the filename to which .properties will be added to find
     *                   the directory.
     * @return a hashmap with all the properties read.
     * @throws Exception if an error occured reading properties, or
     *                   deserializing an object.
     */
    static public HashMap readProperties(HashMap properties, String filename) throws Exception {
        Object hdfobj = openFile(filename + propext, false, true);
        HashMap props = readProperties(properties, hdfobj);
        closeFile(hdfobj);
        return props;
    }

    static public HashMap readProperties(HashMap properties, Object hdfobj) throws Exception {
        if (hdfobj == null) {
            return properties;
        }
        if (properties == null) {
            properties = new HashMap();
        }

        if (hdfobj instanceof Group) {
            Group group = (Group) hdfobj;
            List files = group.getMemberList();
            if (files != null) {
                for (Iterator filelist = files.iterator(); filelist.hasNext();) {
                    Object obj = filelist.next();
                    if (obj instanceof ScalarDS) {
                        ScalarDS prop = (ScalarDS) obj;
                        prop.init();

                        // read the data
                        Object data = prop.read();

                        // is the object serialized
                        List attrList = prop.getMetadata();
                        boolean serialized = false;
                        if (attrList != null) {
                            for (Iterator iter = attrList.iterator(); iter.hasNext();) {
                                Attribute attr = (Attribute) iter.next();
                                if (attr.getName().equals("serialized")) {
                                    byte[] val = (byte[]) attr.getValue();
                                    serialized = (val[0] != 0);
                                }
                            }
                        }

                        // arrays of strings are serialized. problem is that upon reading
                        // there is no easy distinction between String and String[1].
                        // TODO can we find method to write array of strings?
                        if (data.getClass() == String[].class) {
                            data = Array.get(data, 0);
                        }

                        if (serialized) {
                            // Deserialize from a byte array
                            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream((byte[]) data));
                            data = in.readObject();
                            in.close();
                        }

                        // add the data to the image
                        properties.put(prop.getName(), data);
                    }
                }
            }
        }
        return properties;
    }

    static public HashMap readAttributes(HashMap properties, String filename, Object[] ignore) throws IOException {
        Object hdfobj = (HObject) openFile(filename, false, true);
        HashMap props = readAttributes(properties, hdfobj, ignore);
        closeFile(hdfobj);
        return props;
    }

    /**
     * Read the attributes that are associated with an HDF object and return
     * those attributes as a hashmap. Each key in the hashmap is the name of the
     * attribute and the value is the value of the attribute. If the value is an
     * array it will be transformed back into an array of the right dimensions
     * based on the rank. Strings will be returned as a string.
     *
     * @param properties hashmap into which to store the properties, if null is
     *                   passed in a new hashmap will be created.
     * @param hdfobj     the HDF object whose attributes to be read.
     * @param ignore     list of properties to be ignored.
     * @return the hashmap with the properties read.
     */
    static public HashMap readAttributes(HashMap properties, Object hdfobj, Object[] ignore) {
        if (!(hdfobj instanceof HObject)) {
            return properties;
        }
        HObject hobj = (HObject) hdfobj;

        // retrieve the attributes from the object
        List attrList = null;
        try {
            attrList = hobj.getMetadata();
        } catch (Exception exc) {
            //logger.debug("Error retrieving attributes, or no metadata.", exc);
            attrList = null;
        }

        // if no attributes, return
        if (attrList == null) {
            return properties;
        }

        // make sure there are properties
        if (properties == null) {
            properties = new HashMap();
        }

        // read the attribute into memory
        boolean hdf4 = (hobj.getFileFormat() instanceof H4File);
        for (Iterator iter = attrList.iterator(); iter.hasNext();) {
            Attribute attr = (Attribute) iter.next();
            Object val = attr.getValue();

            if (val.getClass().isArray()) {
                // Special case for H4 files
                if (hdf4 && (val instanceof String[]) && (attr.getRank() == 1)) {
                    val = ((String[]) val)[0].split("\0");
                    attr.getDataDims()[0] = ((String[]) val).length;
                }

                // reconstruct array
                int[] arrd = new int[attr.getRank()];
                int[] loc = new int[attr.getRank()];
                for (int i = 0; i < arrd.length; i++) {
                    arrd[i] = (int) attr.getDataDims()[i];
                }
                loc[arrd.length - 1] = 1;
                for (int j = arrd.length - 2; j >= 0; j--) {
                    loc[j] = arrd[j + 1] * loc[j + 1];
                }

                Class type = val.getClass().getComponentType();

                if (!type.isPrimitive() && (attr.getRank() == 1) && (attr.getDataDims()[0] == 1)) {
                    //logger.debug(attr.getName() + " = " + Array.get(val, 0));
                    if (!checkIgnore(ignore, attr.getName())) {
                        properties.put(attr.getName(), Array.get(val, 0));
                    }
                } else {
                    Object arr = Array.newInstance(val.getClass().getComponentType(), arrd);

                    for (int i = 0; i < Array.getLength(val); i++) {
                        Object obj = arr;
                        int x = i;
                        for (int j = 0; j < loc.length - 1; j++) {
                            obj = Array.get(obj, x / loc[j]);
                            x = x % loc[j];
                        }
                        Array.set(obj, x, Array.get(val, i));
                    }

                    //logger.debug(attr.getName() + " = " + arr);

                    if (attr.getName().endsWith("_SERIALIZED")) {
                        try {
                            byte[] bytes = (byte[]) val;

                            // Deserialize from a byte array
                            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
                            Object obj = in.readObject();
                            in.close();

                            String key = attr.getName().substring(0, attr.getName().length() - "_SERIALIZED".length());
                            properties.put(key, obj);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!checkIgnore(ignore, attr.getName())) {
                            properties.put(attr.getName(), arr);
                        }
                    }
                }
            } else {
                //logger.debug(attr.getName() + " = " + val);
                if (!checkIgnore(ignore, attr.getName())) {
                    properties.put(attr.getName(), val);
                }
            }
        }
        return properties;
    }

    static private boolean checkIgnore(Object[] ignore, Object o) {
        if (ignore == null) {
            return false;
        }
        for (int i = 0; i < ignore.length; i++) {
            if (ignore[i].equals(o)) {
                return true;
            }
        }
        return false;
    }

    static public void writeData(String filename, Object data) throws IOException {
        writeData(filename, data, false, null, 0);
    }

    /**
     * Write an array of values to a HDF file. This code is used to normally
     * write an array from an image to a HDF file, but can be used by other code
     * to write an arbitrary array.
     *
     * @param filename the name of the HDF file and object inside the file.
     * @param data     an array of primitives to be written.
     * @param image    only set this to true if there data is a 3D array of type
     *                 byte. It is safe to always set this to false.
     * @param dims     the dimension of the array passed in. If this is null the
     *                 number of dimensions will be calculated.
     * @param ncomp    number of components in the array.
     * @throws java.io.IOException thrown if an error occured writing the data.
     */
    static public void writeData(String filename, Object data, boolean image, long[] dims, int ncomp) throws IOException {
        Group parent = null;
        boolean serialized = false;

        // check for valid filename
        String[] parts = splitFileName(filename);
        String file, path;
        int idx1 = parts[1].lastIndexOf(Group.separator);
        if (idx1 == -1) {
            file = parts[1];
            path = parts[0] + "#/";
        } else {
            file = parts[1].substring(idx1 + 1);
            path = parts[0] + "#" + parts[1].substring(0, idx1 + 1);
        }

        // create directories/files if need be and open file
        Object obj = openFile(filename, true, false);

        // existing file
        if (obj != null) {
            if (obj instanceof Dataset) {
                Dataset dataset = (Dataset) obj;

                if (dataset.getFileFormat() instanceof H4File) {
                    throw(new IOException("Can not overwrite files in H4File"));
                }

                // ok delete it
                dataset = (Dataset) openFile(filename, true, true);
                try {
                    dataset.getFileFormat().delete(dataset);
                } catch (Exception exc) {
                    logger.error(exc);
                }
                try {
                    dataset.getFileFormat().close();
                } catch (Exception exc) {
                    logger.error(exc);
                }

                // delete the property set if it exists
                Group group = (Group) openFile(filename + propext, true, true);
                if (group != null) {
                    try {
                        group.getFileFormat().delete(group);
                    } catch (Exception exc) {
                        logger.error(exc);
                    }
                    try {
                        group.getFileFormat().close();
                    } catch (Exception exc) {
                        logger.error(exc);
                    }
                }

            } else {
                throw(new IOException("Invalid filename (" + filename + ")."));
            }
        }

        obj = openFile(path, true, true);
        if (obj instanceof Group) {
            parent = (Group) obj;
        } else {
            throw(new IOException("Invalid filename (" + filename + ")."));
        }

        FileFormat hdffile = parent.getFileFormat();

        // construct data type
        Datatype datatype = null;
        try {
            // data has to be an array, so we can safely ask for component type
            // if it is not an array we will serialize it.
            Class type = data.getClass();
            while (type.isArray()) {
                type = type.getComponentType();
            }
            if (type == Byte.TYPE) {
                datatype = hdffile.createDatatype(Datatype.CLASS_INTEGER, 1, Datatype.NATIVE, Datatype.SIGN_NONE);
            } else if (type == Short.TYPE) {
                datatype = hdffile.createDatatype(Datatype.CLASS_INTEGER, 2, Datatype.NATIVE, Datatype.SIGN_NONE);
            } else if (type == Integer.TYPE) {
                datatype = hdffile.createDatatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.SIGN_NONE);
            } else if (type == Long.TYPE) {
                datatype = hdffile.createDatatype(Datatype.CLASS_INTEGER, 8, Datatype.NATIVE, Datatype.SIGN_NONE);
            } else if (type == Float.TYPE) {
                datatype = hdffile.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, Datatype.SIGN_NONE);
            } else if (type == Double.TYPE) {
                datatype = hdffile.createDatatype(Datatype.CLASS_FLOAT, 8, Datatype.NATIVE, Datatype.SIGN_NONE);
            } else if (data.getClass() == String.class) {
                int len = ((String) data).length();
                data = ((String) data).getBytes();
                if (hdffile instanceof H5File) {
                    datatype = hdffile.createDatatype(Datatype.CLASS_STRING, len, Datatype.NATIVE, Datatype.SIGN_NONE);
                    dims = new long[]{1};
                } else {
                    datatype = hdffile.createDatatype(Datatype.CLASS_STRING, 1, Datatype.NATIVE, Datatype.SIGN_NONE);
                    dims = new long[]{len};
                }
            } else {
                // serialize the object
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos);
                out.writeObject(data);
                out.close();
                byte[] buf = bos.toByteArray();
                data = buf;
                dims = new long[]{buf.length};
                serialized = true;

                datatype = hdffile.createDatatype(Datatype.CLASS_INTEGER, 1, Datatype.NATIVE, Datatype.SIGN_NONE);
            }
        } catch (Exception exc) {
            logger.error(exc);
            try {
                hdffile.close();
            } catch (Exception exc2) {
                logger.error(exc2);
            }
            throw(new IOException("Can't save this data type."));
        }

        // data now has to be an array. (string is already taken care off).
        // count the dimension of the array.
        if (dims == null) {
            if (data.getClass().isArray()) {
                String[] count = data.getClass().getName().split("\\[");
                dims = new long[count.length - 1];
                int idx = 0;
                Object tmpdata = data;
                while (idx < count.length - 1) {
                    dims[idx++] = Array.getLength(tmpdata);
                    tmpdata = Array.get(tmpdata, 0);
                }
                ncomp = (int) dims[dims.length - 1];
            } else {
                dims = new long[]{1};
                ncomp = 1;
            }
        }

        // TODO if can save image in H4 see if need swap col/row.
        //if (hdffile instanceof H4File) {
        //    long tmp = dims[0];
        //    dims[0] = dims[0];
        //    dims[1] = tmp;
        //}

        // not doing any chunking or compressing, if added, chance these.
        long[] maxdims = null;
        long[] chunks = null;
        int gzip = 0;

        // the dataset we are saving.
        Dataset dataset = null;

        // BUG for some reason HDF does not write H4File to right path with createImage
        // BUG can write anything but BYTE? FLOAT/DOUBLE is broken.
        try {
            if ((hdffile instanceof H5File) && image) {
                int interlace = ScalarDS.INTERLACE_PIXEL;
                dataset = hdffile.createImage(file, parent, datatype, dims, maxdims, chunks, gzip, ncomp, interlace, data);
                dataset.write();
            } else {
                dataset = hdffile.createScalarDS(file, parent, datatype, dims, maxdims, chunks, gzip, data);
                dataset.write();
            }

            datatype = hdffile.createDatatype(Datatype.CLASS_INTEGER, 1, Datatype.NATIVE, Datatype.SIGN_NONE);
            Attribute attr = new Attribute("serialized", datatype, new long[]{1});
            attr.setValue(new byte[]{(serialized) ? (byte) 1 : (byte) 0});
            dataset.writeMetadata(attr);

            hdffile.close();
        } catch (Exception exc) {
            logger.debug("Error writing data.", exc);
            try {
                hdffile.close();
            } catch (Exception exc2) {
                logger.error("Error closing file.", exc2);
            }
            throw(new IOException("Error saving data."));
        }
    }

    // write the properties. the properties can only be written up to 16K
    // so they are all written to their own file, and pointers are saved
    // in the image object. The properties will be written in a file
    // that is <imagename>.properties/<property_name>
    static public void writeProperties(String filename, HashMap properties) throws IOException {
        if ((properties != null) && !properties.keySet().isEmpty()) {
            for (Iterator keys = properties.keySet().iterator(); keys.hasNext();) {
                Object key = keys.next();
                Object val = properties.get(key);

                if (!key.toString().startsWith("_") && (val != null)) {
                    writeData(filename + propext + "/" + key.toString(), val, false, null, 0);
                }
            }
        }
    }

    /**
     * List all the files in a group. This function will return a vector with
     * all the files that are in a group. This will return any subgroups found
     * in the group if showgroup is set to true. The subgroups will end with /.
     * If recurse is set to true it will recurse through any subgroup and return
     * those results as well. Since any subgroup ending with .properties is
     * considered a special group, these will not be listed.
     *
     * @param filename  the filename including group to list.
     * @param showgroup should subgroups be listed as well.
     * @param recurse   should any subgroups be listed as well.
     * @return a vector with all filenames to objects found.
     * @throws IOException if the filename does not point to a group or an error
     *                     occured reading the group, or subgroups.
     */
    static public Vector listGroup(String filename, boolean showgroup, boolean recurse) throws IOException {
        if (filename.indexOf('#') == -1) {
            filename += "#/";
        } else if (!filename.endsWith("/")) {
            filename += "/";
        }

        Object obj = openFile(filename, false, false);
        if (!(obj instanceof Group)) {
            throw(new IOException("filename is not a hdf group, directory."));
        }

        List list = ((Group) obj).getMemberList();
        Vector result = new Vector();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            HObject hobj = (HObject) iter.next();
            String name = filename + hobj.getName();
            if (hobj instanceof Group) {
                if (!name.endsWith(".properties")) {
                    name += "/";
                    if (showgroup) {
                        result.add(name);
                    }
                    if (recurse) {
                        result.addAll(listGroup(name, showgroup, recurse));
                    }
                }
            } else {
                result.add(name);
            }
        }

        return result;
    }

    /**
     * Open a composite file and return a pointer to the object. If necessary
     * this function will extract the file to a temperary directory and return a
     * pointer to this temporary file. The file itself consists of the parent
     * file followed by a # and the path inside the parent file.
     *
     * @param filename to the file to open
     * @param create   true if the file should be created if it does not exist
     * @param extract  is true if the user wants to use this file later, this
     *                 means the archive won't be closed and the code is
     *                 responsible for calling hdffile.close later.
     * @throws IOException if the file could not be found.
     */
    static public Object openFile(String filename, boolean create, boolean extract) throws IOException {
        // composite file (maybe), split into 2 filenames
        String[] parts;
        if (filename.indexOf("#") != -1) {
            parts = splitFileName(filename);
        } else {
            parts = new String[]{filename};
        }

        // try to determine the parent file
        FileFormat ff = getFileFormat(parts[0]);
        if (ff == null) {
            throw(new IOException("Error opening HDF FILE"));
        }

        // open the HDF file
        FileFormat hdffile = null;
        try {
            // BUG: Opening a file for READ first does not allow for write.
            hdffile = ff.open(parts[0], FileFormat.WRITE);
        } catch (Exception exc) {
            logger.error("Error opening HDF FILE", exc);
            throw(new IOException(exc.toString()));
        }

        // create if needed.
        if (!hdffile.exists()) {
            if (create) {
                try {
                    hdffile = hdffile.create(parts[0]);
                } catch (Exception exc) {
                    logger.error("Error opening HDF FILE", exc);
                    throw(new IOException(exc.toString()));
                }
            } else {
                throw(new IOException("Error opening HDF FILE"));
            }
        }

        // now really open the HDF file
        try {
            hdffile.open();
        } catch (Exception exc) {
            logger.error(exc);
            throw(new IOException("Error opening file."));
        }

        Object result = hdffile;
        if (parts.length > 1) {
            result = openFileHDF(hdffile.getRootNode(), parts[1].split("/"), 0, create);
        }
        if (!extract) {
            try {
                hdffile.close();
            } catch (Exception exc) {
                logger.error(exc);
            }
        }
        return result;
    }

    static public void closeFile(Object hdfobj) throws IOException {
        if (hdfobj == null) {
            return;
        } else if (hdfobj instanceof FileFormat) {
            try {
                ((FileFormat) hdfobj).close();
            } catch (Exception exc) {
                throw(new IOException("Error closing file."));
            }
            return;
        } else if (hdfobj instanceof HObject) {
            try {
                ((HObject) hdfobj).getFileFormat().close();
            } catch (Exception exc) {
                throw(new IOException("Error closing file."));
            }
            return;
        } else {
            throw(new IOException("Error closing file."));
        }
    }

    /**
     * Find a file inside a HDF file
     *
     * @param node   the curent subdirectory searched
     * @param path   full path in chunks to file that is being looked for
     * @param depth  current depth in path
     * @param create the path if it does not exist?
     * @return pointer to file object
     */
    static private Object openFileHDF(TreeNode node, String[] path, int depth, boolean create) {
        // just in case
        if (node == null) {
            return null;
        }

        // if we reached the end of the path return the current node.
        if (depth >= path.length) {
            return ((DefaultMutableTreeNode) node).getUserObject();
        }

        // skip any empty path parts
        if (path[depth].equals("")) {
            return openFileHDF(node, path, depth + 1, create);
        }

        // search the child nodes for a match.
        for (Enumeration e = node.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement();
            HObject hobj = (HObject) child.getUserObject();

            if (hobj.getName().equals(path[depth])) {
                return openFileHDF(child, path, depth + 1, create);
            }
        }

        if (create) {
            Group group = (Group) ((DefaultMutableTreeNode) node).getUserObject();
            FileFormat ff = group.getFileFormat();

            for (int i = depth; i < path.length - 1; i++) {
                try {
                    group = ff.createGroup(path[i], group);
                } catch (Exception exc) {
                    logger.error("Error creating group.", exc);
                    return null;
                }
            }
        }

        return null;
    }

    static public String[] splitFileName(String filename) throws IOException {
        if (filename == null) {
            throw(new IOException("null filename specified."));
        }
        String[] parts = filename.split("#", 2);
        if (parts.length != 2) {
            throw(new IOException("no # found in filename."));
        }
        if (parts[0].length() == 0) {
            throw(new IOException("no HDF file specified."));
        }
        if (parts[1].length() == 0) {
            throw(new IOException("no object inside HDF file specified."));
        }
        return parts;
    }

    //    /**
    //     * returns the maximum length of the array of strings.
    //     *
    //     * @param obj the array of strings.
    //     * @param max length found sofar.
    //     * @return the maximum length found.
    //     */
    //    private int maxString(Object obj, int max) {
    //        if (obj.getClass().isArray()) {
    //            int len = Array.getLength(obj);
    //            for (int i = 0; i < len; i++) {
    //                max = maxString(Array.get(obj, i), max);
    //            }
    //            return max;
    //        } else {
    //            String str = (String) obj;
    //            return (str.length() > max ? str.length() : max);
    //        }
    //    }
    //
    //    /**
    //     * Convert an array of Strings to an array of bytes.
    //     *
    //     * @param obj array of strings
    //     * @param arr that will contain the strings.
    //     */
    //    private void byteString(Object obj, Object arr) {
    //        if (obj.getClass().isArray()) {
    //            int len = Array.getLength(obj);
    //            for (int i = 0; i < len; i++) {
    //                byteString(Array.get(obj, i), Array.get(arr, i));
    //            }
    //        } else {
    //            String str = (String) obj;
    //            int len = Array.getLength(arr);
    //            System.arraycopy(str.getBytes(), 0, arr, 0, str.length());
    //            for (int i = str.length(); i < len - 1; i++) {
    //                Array.setByte(arr, i, (byte) ' ');
    //            }
    //            Array.setByte(arr, len - 1, (byte) 0);
    //        }
    //    }
}
