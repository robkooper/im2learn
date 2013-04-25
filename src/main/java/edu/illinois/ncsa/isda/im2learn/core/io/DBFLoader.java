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
package edu.illinois.ncsa.isda.imagetools.core.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

/**
 * This class allows for reading and writing to a DBF file. DBF is a simple database format developped by Borland. This
 * class allows to read the records out of a DBF file, create a new DBF file, or append records to a DBF file. <p/> When
 * reading a DBF file, simply call open with the filename and READ as the mode. This will open the file, read the field
 * descriptors. You can now use getRecord() to read a single record from the file, or getColumn() to read one or more
 * columns from the file. <p/> To write to a DBF file, first the fields have to be defined. Calling addField() for each
 * field that needs to be added to the DBF file. Next the file can be opened by simply calling open with the filename
 * and WRITE as the mode. After this you can write records by calling writeRecord(). This takes a list of Objects which
 * need to match the types and order specified in addField(). <p/> To append to a DBF file simply call open with the
 * filename and APPEND as the mode. This will open the file, read the field descriptors. You can now append records in
 * the same fashion as write. <p/> After finishing working with the DBF file, call close to release the lock.
 * 
 * @author Rob Kooper
 * @author Peter Groves
 * @author Peter Bajcsy
 * @version 2.0
 */
public class DBFLoader extends AbstractTableModel {
	private Date					lastupdate	= null;
	private int						type		= 0;
	private int						fieldcount	= 0;
	private int						recordcount	= 0;
	private int						fieldsize	= 0;
	private int						recordsize	= 1;
	private long					recordstart	= 0;
	private final ArrayList			fielddesc	= new ArrayList();
	private RandomAccessFile		file		= null;
	private byte[]					record		= null;
	private final byte[]			tmparr		= new byte[4];

	static public final int			READ		= 0;
	static public final int			WRITE		= 1;
	static public final int			APPEND		= 2;

	private static int				HEADER_SIZE	= 32;
	private static int				FIELD_SIZE	= 32;

	private static SimpleDateFormat	sdf			= new SimpleDateFormat("yyyyMMdd");

	public static final byte		STRING		= 'C';
	public static final byte		FLOAT		= 'F';
	public static final byte		INT			= 'N';
	public static final byte		BOOLEAN		= 'L';
	public static final byte		DATE		= 'D';
	public static final byte		UNKNOWN		= 0;

	/**
	 * Default constructor, does nothing.
	 */
	public DBFLoader() {
	}

	/**
	 * Add the field to the list of fields. This will assume length of 255 and in the case of a FLOAT, 8 chars after the
	 * decimal.
	 * 
	 * @param name
	 *            the name of the field.
	 * @param type
	 *            the type of the field.
	 * @throws IllegalArgumentException
	 *             if the field could not be defined, or the file is already opened.
	 */
	public void addField(String name, byte type) throws IllegalArgumentException {
		addField(name, type, 255, 8);
	}

	/**
	 * Add the field to the list of fields. This will craete a new field for the database with the specified variables.
	 * When writing records the data written will be checked with this field and truncated if it is to long, or an
	 * exception is thrown if the type is not correct.
	 * 
	 * @param name
	 *            the name of the field.
	 * @param type
	 *            the type of the field.
	 * @param length
	 *            the lenght of the data in the field.
	 * @param decimals
	 *            the number of chars after the decimal in case of FLOAT.
	 * @throws IllegalArgumentException
	 *             if the field could not be defined, or the file is already opened.
	 */
	public void addField(String name, byte type, int length, int decimals) throws IllegalArgumentException {
		if (file != null) {
			throw (new IllegalArgumentException("file is already opened, can not add more fields."));
		}

		FieldDescriptor fd = new FieldDescriptor(name, type, recordsize, length, decimals);
		fielddesc.add(fd);
		recordsize += fd.getLength();
		fieldcount++;
		fieldsize += 32;

		fireTableStructureChanged();
	}

	/**
	 * Open the file. In case of READ or APPEND read the header of the database and the field descriptors. The class
	 * will now now the number of records, how many fields each record has and what fields each record has. In case of
	 * WRITE, truncate the file, write the header as specified with addField and set the number of records to 0.
	 * 
	 * @param filename
	 *            the name of the dbf file to open.
	 * @param mode
	 *            signals to open for read-only (READ), append to end of the file (APPEND) or write to the file,
	 *            truncating any existing file (WRITE).
	 * @throws IOException
	 *             if an error happens opening or reading the file.
	 */
	public void open(String filename, int mode) throws IOException {
		if ((mode != READ) && (mode != WRITE) && (mode != APPEND)) {
			throw (new IllegalArgumentException("Don't know what to do with the file."));
		}

		if (mode == WRITE) {
			file = new RandomAccessFile(filename, "rws");
			file.setLength(0);

			// write the header
			file.writeByte(3);
			writeNow();
			writeRecordCount();
			recordstart = fieldsize + FIELD_SIZE + 1;
			write((short) recordstart);
			write((short) recordsize);

			// fill rest with 0
			byte[] buf = new byte[20];
			file.write(buf);

			// write the fielddescriptors
			for (int i = 0; i < fieldcount; i++) {
				FieldDescriptor fd = (FieldDescriptor) fielddesc.get(i);
				file.write(fd.toArray());
			}

			// write the extra byte
			file.writeByte(0x0D);

			// write the closing byte
			file.writeByte(0x1A);

		} else {
			byte[] header = new byte[HEADER_SIZE];

			// read the header
			if (mode == APPEND) {
				file = new RandomAccessFile(filename, "rws");
			} else {
				file = new RandomAccessFile(filename, "r");
			}
			if (file.read(header) != HEADER_SIZE) {
				throw (new IOException("Could not read header."));
			}

			// read the fielddescriptors
			fieldsize = getShort(header, 8);
			recordstart = fieldsize;

			// get the type of the dbf file
			type = header[0] & 0xff;

			// get the date of last update
			lastupdate = getDate(header, 1);

			// fieldsize is always 1 record (header) and 1 byte to large.
			fieldsize -= (FIELD_SIZE + 1);

			if (fieldsize % FIELD_SIZE != 0) {
				throw (new IOException("Not enough bytes for field descriptor."));
			}
			fieldcount = fieldsize / FIELD_SIZE;

			// read the fielddescriptors
			byte[] arr = new byte[fieldsize];
			if (file.read(arr) != fieldsize) {
				throw (new IOException("Could not read field descriptors."));
			}

			// read the field descriptors
			int offset = 1;
			fielddesc.clear();
			for (int i = 0, j = 0; i < fieldcount; i++, j += FIELD_SIZE) {
				FieldDescriptor fd = new FieldDescriptor(arr, j);
				fd.addOffset(offset);
				offset += fd.getLength();
				fielddesc.add(fd);
			}

			// get the record information
			recordsize = getShort(header, 10);
			recordcount = getInt(header, 4);

			// some basic checks
			if (file.read(arr, 0, 1) != 1) {
				throw (new IOException("Could not read field descriptors end."));
			}
			if (arr[0] != 0x0D) {
				throw (new IOException("End of field record not found."));
			}

			file.seek(file.length() - 1);
			if (file.read(arr, 0, 1) != 1) {
				throw (new IOException("Could not read record end."));
			}
			if (arr[0] != 0x1A) {
				throw (new IOException("End of records not found."));
			}
		}

		record = new byte[recordsize];
	}

	/**
	 * Close the database file. This will close the file, any access to the file after this call will throw a null
	 * pointer exception.
	 * 
	 * @throws IOException
	 *             if the file could not be closed.
	 */
	public void close() throws IOException {
		file.close();
		file = null;
	}

	/**
	 * Return the date of the last update to the file. In case of WRITE or APPEND this signals the last time a record
	 * was written to the file.
	 * 
	 * @return last write to the DBF file.
	 */
	public Date getLastUpdate() {
		return lastupdate;
	}

	/**
	 * Return the current number of records. This can change if more records are written to disk.
	 * 
	 * @return number of records in the DBF file.
	 * @deprecated use getRowCount
	 */
	@Deprecated
	public int getRecordCount() {
		return recordcount;
	}

	/**
	 * Return the current number of records. This can change if more records are written to disk.
	 * 
	 * @return number of records in the DBF file.
	 */
	public int getRowCount() {
		return recordcount;
	}

	/**
	 * Return the number of fields in the DBF file. Once the file has been opened this is constant.
	 * 
	 * @return number of fields in the DBF file.
	 * @deprecated use getColumnCount
	 */
	@Deprecated
	public int getFieldCount() {
		return fieldcount;
	}

	/**
	 * Return the number of fields in the DBF file. Once the file has been opened this is constant.
	 * 
	 * @return number of fields in the DBF file.
	 */
	public int getColumnCount() {
		return fieldcount;
	}

	/**
	 * Returns the name of the field. Each field in the DBF file has a name, this will return the name of the field
	 * specified by the index.
	 * 
	 * @param idx
	 *            the index of the field whose name to return.
	 * @return name of the field.
	 * @throws IllegalArgumentException
	 *             if the field does not exist.
	 * @deprecated use getColumnName
	 */
	@Deprecated
	public String getFieldName(int idx) throws IllegalArgumentException {
		return getColumnName(idx);
	}

	/**
	 * Returns the name of the field. Each field in the DBF file has a name, this will return the name of the field
	 * specified by the index.
	 * 
	 * @param idx
	 *            the index of the field whose name to return.
	 * @return name of the field.
	 * @throws IllegalArgumentException
	 *             if the field does not exist.
	 */
	@Override
	public String getColumnName(int columnIndex) {
		if ((columnIndex < 0) || (columnIndex >= fieldcount)) {
			throw (new IllegalArgumentException("Invalid fieldindex."));
		}
		return ((FieldDescriptor) fielddesc.get(columnIndex)).getName();
	}

	/**
	 * Returns the type of the field. Each field in the DBF file has a type, this will return the type of the field
	 * specified by the index.
	 * 
	 * @param idx
	 *            the index of the field whose type to return.
	 * @return type of the field.
	 * @throws IllegalArgumentException
	 *             if the field does not exist.
	 */
	public byte getFieldType(int idx) throws IllegalArgumentException {
		if ((idx < 0) || (idx >= fieldcount)) {
			throw (new IllegalArgumentException("Invalid fieldindex."));
		}
		return ((FieldDescriptor) fielddesc.get(idx)).getType();
	}

	/**
	 * Returns the length of the field. Each field in the DBF files has a length, this will return the length of the
	 * field specified by the index.
	 * 
	 * @param idx
	 *            the index of the field whose length to return.
	 * @return length of the field.
	 * @throws IllegalArgumentException
	 *             if the field does not exist.
	 */
	public int getFieldLength(int idx) throws IllegalArgumentException {
		if ((idx < 0) || (idx >= fieldcount)) {
			throw (new IllegalArgumentException("Invalid fieldindex."));
		}
		return ((FieldDescriptor) fielddesc.get(idx)).getLength();
	}

	/**
	 * Returns the decimal place of the field. Each field in the DBF file has a decimal, this will return the decimal of
	 * the field specified by the index.
	 * 
	 * @param idx
	 *            the index of the field whose decimal to return.
	 * @return decimal of the field.
	 * @throws IllegalArgumentException
	 *             if the field does not exist.
	 */
	public int getFieldDecimals(int idx) throws IllegalArgumentException {
		if ((idx < 0) || (idx >= fieldcount)) {
			throw (new IllegalArgumentException("Invalid fieldindex."));
		}
		return ((FieldDescriptor) fielddesc.get(idx)).getDecimals();
	}

	/**
	 * Reads a record from the file and returns the record. This will go to the correct place in the file and read the
	 * record from the disk. The record will be returned as an array of Objects, each Objects is a field in the record.
	 * 
	 * @param idx
	 *            the index of the record to return.
	 * @return the record as an array of Objects.
	 * @throws IllegalArgumentException
	 *             if the record does not exists.
	 * @throws IOException
	 *             if the record could not be read.
	 */
	public Object[] getRecord(int idx) throws IllegalArgumentException, IOException {
		if ((idx < 0) || (idx >= recordcount)) {
			throw (new IllegalArgumentException("Invalid recordnumber."));
		}

		// move to record
		file.seek(recordstart + idx * recordsize);

		// read record
		if (file.read(record) != recordsize) {
			throw (new IOException("Could not read record."));
		}

		// TODO throw exception if record deleted?
		// if (record[0] == '*') {
		//     throw(new IOException("Record deleted."));
		// }

		Object[] result = new Object[fieldcount];

		// parse record
		for (int j = 0; j < fieldcount; j++) {
			result[j] = ((FieldDescriptor) fielddesc.get(j)).toObject(record);
		}

		return result;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			return getRecord(rowIndex)[columnIndex];
		} catch (IOException exc) {
			return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		try {
			Object[] fields = getRecord(rowIndex);
			fields[columnIndex] = aValue;
			writeRecord(fields, rowIndex);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Write the record to disk. The fields are specified as an array of Objects where each Objects matches a field in
	 * the DBF. Each Object is compared with the type of the field, meaning a STRING field expects a String Object, if
	 * there is no match an IllegalArgumentException is thrown. This will update the lastupdate timestamp and the number
	 * of records in the DBF file.
	 * 
	 * @param fields
	 *            an array of fields that need to be written to the disk.
	 * @throws IOException
	 *             if the record could not be written.
	 * @throws IllegalArgumentException
	 *             if not enough fields where provided or if the type of the Object does not match the type of the
	 *             field.
	 */
	public void writeRecord(Object[] fields, int idx) throws IOException, IllegalArgumentException {
		if ((idx < 0) || (idx >= recordcount)) {
			throw (new IllegalArgumentException("Invalid recordnumber."));
		}
		if (fields.length != fieldcount) {
			throw (new IllegalArgumentException("Not enough fields in record."));
		}

		// move to record
		file.seek(recordstart + idx * recordsize);

		// write record
		Arrays.fill(record, (byte) ' ');

		// convert the fields to bytes
		for (int i = 0; i < fieldcount; i++) {
			FieldDescriptor fd = (FieldDescriptor) fielddesc.get(i);
			fd.fromObject(fields[i], record);
		}

		file.write(record);

		// update last update field
		writeNow();

		fireTableRowsUpdated(idx, idx);
	}

	/**
	 * Write the record to disk. The fields are specified as an array of Objects where each Objects matches a field in
	 * the DBF. Each Object is compared with the type of the field, meaning a STRING field expects a String Object, if
	 * there is no match an IllegalArgumentException is thrown. This will update the lastupdate timestamp and the number
	 * of records in the DBF file.
	 * 
	 * @param fields
	 *            an array of fields that need to be written to the disk.
	 * @throws IOException
	 *             if the record could not be written.
	 * @throws IllegalArgumentException
	 *             if not enough fields where provided or if the type of the Object does not match the type of the
	 *             field.
	 */
	public void writeRecord(Object[] fields) throws IOException, IllegalArgumentException {
		if (fields.length != fieldcount) {
			throw (new IllegalArgumentException("Not enough fields in record."));
		}

		// write record
		file.seek(file.length() - 1);
		Arrays.fill(record, (byte) ' ');

		// convert the fields to bytes
		for (int i = 0; i < fieldcount; i++) {
			FieldDescriptor fd = (FieldDescriptor) fielddesc.get(i);
			fd.fromObject(fields[i], record);
		}

		file.write(record);
		recordcount++;

		// write the closing byte
		file.writeByte(0x1A);

		// update last update field
		writeNow();

		// update record counter
		writeRecordCount();

		fireTableRowsInserted(recordcount, recordcount);
	}

	/**
	 * Write the records to disk. The records are stored as an array, each entry containing an array of fields, where
	 * each field is specified as an Object that will be stored in the DBF. Each Object is compared with the type of the
	 * field, meaning a STRING field expects a String Object, if there is no match an IllegalArgumentException is
	 * thrown. This will update the lastupdate timestamp and the number of records in the DBF file. <p/> When writing
	 * many records at once it is advantage to use this method as compared to the method that only writes one record
	 * since this method will avoid updating the recordcount, lastupdate etc until the last record is written.
	 * 
	 * @param fields
	 *            an array of fields that need to be written to the disk.
	 * @param norecs
	 *            the number of records passed.
	 * @throws IOException
	 *             if the record could not be written.
	 * @throws IllegalArgumentException
	 *             if not enough fields where provided or if the type of the Object does not match the type of the
	 *             field.
	 */
	public void writeRecord(Object[][] fields, int norecs) throws IOException, IllegalArgumentException {
		if ((norecs <= 0) || (norecs > fields.length)) {
			throw (new IllegalArgumentException("Not enough records."));
		}

		file.seek(file.length() - 1);

		for (int i = 0; i < norecs; i++) {
			if (fields[i].length != fieldcount) {
				throw (new IllegalArgumentException("Not enough fields in record."));
			}

			// write record
			Arrays.fill(record, (byte) ' ');

			// convert the fields to bytes
			for (int j = 0; j < fieldcount; j++) {
				FieldDescriptor fd = (FieldDescriptor) fielddesc.get(j);
				fd.fromObject(fields[i][j], record);
			}

			file.write(record);
		}

		int oldcount = recordcount;
		recordcount += norecs;

		// write the closing byte
		file.writeByte(0x1A);

		// update last update field
		writeNow();

		// update record counter
		writeRecordCount();

		fireTableRowsInserted(oldcount + 1, recordcount);
	}

	/**
	 * Return a single array of values from the DBF. This will return an array that contains the values of a specified
	 * field for all records in the DBF file. <p/> In the case of a field being of type INT, FLOAT or BOOLEAN, this will
	 * not return Integer[], Float[] or Boolean[] but will return int[], float[] and boolean[] instead.
	 * 
	 * @param idx
	 *            the index of the field.
	 * @return an array with values, based on the type of the field.
	 * @throws IOException
	 *             if an error occured reading records.
	 * @throws IllegalArgumentException
	 *             if an invalid field is specified.
	 */
	public Object getFieldValues(int idx) throws IOException, IllegalArgumentException {
		return getFieldValues(new int[] { idx })[0];
	}

	/**
	 * Return a set of arrays of values from the DBF. This will return a set of arrays that contains the values of all
	 * specified fields for all records in the DBF file. <p/> In the case of a field being of type INT, FLOAT or
	 * BOOLEAN, this will not return Integer[], Float[] or Boolean[] but will return int[], float[] and boolean[]
	 * instead.
	 * 
	 * @param idx
	 *            the indexes of the fields.
	 * @return an array with values, based on the type of the field.
	 * @throws IOException
	 *             if an error occured reading records.
	 * @throws IllegalArgumentException
	 *             if an invalid field is specified.
	 */
	public Object[] getFieldValues(int[] idx) throws IOException, IllegalArgumentException {
		int count = idx.length;
		Object[] result = new Object[count];
		int i, j;

		for (i = 0; i < count; i++) {
			if ((idx[i] < 0) || (idx[i] >= fieldcount)) {
				throw (new IllegalArgumentException("Invalid field index."));
			}
			switch (((FieldDescriptor) fielddesc.get(idx[i])).getType()) {
			case STRING:
				result[i] = new String[recordcount];
				break;

			case DATE:
				result[i] = new Date[recordcount];
				break;

			case BOOLEAN:
				result[i] = new boolean[recordcount];
				break;

			case INT:
				result[i] = new int[recordcount];
				break;

			case FLOAT:
				result[i] = new float[recordcount];
				break;

			default:
				result[i] = new String[recordcount];
			}
		}

		// read all records and stick in right column
		Object[] record;
		for (i = 0; i < recordcount; i++) {
			record = getRecord(i);
			for (j = 0; j < count; j++) {
				Array.set(result[j], i, record[idx[j]]);
			}
		}

		return result;
	}

	/**
	 * Return the information of this DBF file as a string. This will return a string that is useful for debugging. The
	 * string will contain all the information kept in the head of the DBF file.
	 * 
	 * @return a string with all the information kept in the head of the DBF file.
	 */
	@Override
	public String toString() {
		String result = "";

		switch (type) {
		case 0x03:
			result = "Type        : FoxBase+, FoxPro, dBaseIII+, dBaseIV, no memo\n";
			break;

		case 0x83:
			result = "Type        : FoxBase+, dBaseIII+ with memo\n";
			break;

		case 0xF5:
			result = "Type        : FoxPro with memo\n";
			break;

		case 0x8B:
			result = "Type        : dBaseIV with memo\n";
			break;

		case 0x8E:
			result = "Type        : dBaseIV with SQL Table\n";
			break;

		default:
			result = "Type        : unknown\n";
		}

		result += "Last update : " + getLastUpdate().toString() + "\n";
		result += "No. fields  : " + fieldcount + "\n";
		result += "Field size  : " + fieldsize + "\n";
		result += "No. records : " + recordcount + "\n";
		result += "Record size : " + recordsize + "\n";

		for (int i = 0; i < fieldcount; i++) {
			result += "field " + i + "  : " + (fielddesc.get(i)) + "\n";
		}

		return result;
	}

	/**
	 * Write the last update time (now) to disk.
	 */
	private void writeNow() throws IOException {
		file.seek(1);

		Calendar cal = Calendar.getInstance();
		lastupdate = cal.getTime();
		file.writeByte(cal.get(Calendar.YEAR) - 1900);
		file.writeByte(cal.get(Calendar.MONTH));
		file.writeByte(cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * Write the number of records in the DBF to disk.
	 */
	private void writeRecordCount() throws IOException {
		file.seek(4);
		write(recordcount);
	}

	/**
	 * Write a short to disk.
	 */
	private void write(short v) throws IOException {
		tmparr[0] = (byte) v;
		tmparr[1] = (byte) (v >> 8);
		file.write(tmparr, 0, 2);
	}

	/**
	 * Write an int to disk.
	 */
	private void write(int v) throws IOException {
		tmparr[0] = (byte) v;
		tmparr[1] = (byte) (v >> 8);
		tmparr[2] = (byte) (v >> 16);
		tmparr[3] = (byte) (v >> 24);
		file.write(tmparr, 0, 4);
	}

	/**
	 * Convert the last update date to a real date.
	 */
	private Date getDate(byte[] arr, int idx) {
		Calendar cal = Calendar.getInstance();
		cal.set(1900 + arr[idx], arr[idx + 1], arr[idx + 2], 0, 0, 0);
		return cal.getTime();
	}

	/**
	 * Get a string out of the array.
	 */
	private String getString(byte[] arr, int idx, int len) {
		return new String(arr, idx, len).trim();
	}

	/**
	 * Get a short out of the array.
	 */
	private short getShort(byte[] arr, int idx) {
		return (short) (((arr[idx + 0] & 0xff)) + ((arr[idx + 1] & 0xff) << 8));
	}

	/**
	 * Get an int out of the array.
	 */
	private int getInt(byte[] arr, int idx) {
		return ((arr[idx + 0] & 0xff)) + ((arr[idx + 1] & 0xff) << 8) + ((arr[idx + 2] & 0xff) << 16) + ((arr[idx + 3] & 0xff) << 24);
	}

	/**
	 * Class that is used to contain the field descriptor information. This class can also extract the data of a field
	 * from a record of bytes, convert the data to a properly formatted set of bytes. Finally it can also convert the
	 * fielddescriptor to an array of bytes that can be written to disk.
	 */
	class FieldDescriptor {
		private String			name		= "";
		private byte			type		= STRING;
		private int				offset		= 0;
		private int				length		= 0;
		private int				decimals	= 0;

		private NumberFormat	nf			= null;

		/**
		 * Extract the information of this fielddescriptor from an array.
		 */
		public FieldDescriptor(byte[] arr, int offset) {
			this.name = new String(arr, offset, 10);
			this.offset = getInt(arr, offset + 12);
			this.length = arr[offset + 16] & 0xff;
			this.decimals = arr[offset + 17] & 0xff;
			this.type = arr[offset + 11];

			if ((type == 'N') && (decimals != 0)) {
				type = FLOAT;
			}

			check();
		}

		/**
		 * Set the values of this field descriptor.
		 */
		public FieldDescriptor(String name, byte type, int offset, int length, int decimals) {
			this.name = name;
			this.type = type;
			this.offset = offset;
			this.length = length;
			this.decimals = decimals;

			check();
		}

		/**
		 * Check the values of the field descriptor making sure the descriptor is valid.
		 */
		private void check() {
			if (name.length() > 10) {
				name = name.substring(0, 10);
			}
			name = name.trim();

			switch (this.type) {
			case STRING:
				decimals = 0;
				break;

			case DATE:
				decimals = 0;
				length = 8;
				break;

			case INT:
				nf = NumberFormat.getIntegerInstance(Locale.US);
				nf.setGroupingUsed(false);
				decimals = 0;
				break;

			case FLOAT:
				nf = NumberFormat.getInstance(Locale.US);
				nf.setGroupingUsed(false);
				break;

			case BOOLEAN:
				decimals = 0;
				length = 1;
				break;

			default:
				decimals = 0;
			}
		}

		/**
		 * Returns an array of bytes that describes this field descriptor.
		 */
		public byte[] toArray() {
			byte[] result = new byte[32];

			System.arraycopy(name.getBytes(), 0, result, 0, name.length());
			if (type != UNKNOWN) {
				result[11] = type;
			} else {
				result[11] = STRING;
			}
			result[16] = (byte) length;
			result[17] = (byte) decimals;

			return result;
		}

		/**
		 * Returns the name of this field.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the type of this field.
		 */
		public byte getType() {
			return type;
		}

		/**
		 * Returns the offset into the record of this field. This is the offset from the beginning of the record, not
		 * the offset stored in the DBF file.
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * Adds an additional offset. Used to update the offset value with respect to the total offset in the record.
		 */
		public void addOffset(int offset) {
			this.offset += offset;
		}

		/**
		 * Returns the maximum length of this field.
		 */
		public int getLength() {
			return length;
		}

		/**
		 * Returns the maximum number of values after the decimal in the case of FLOAT.
		 */
		public int getDecimals() {
			return decimals;
		}

		/**
		 * Returns an Object that is decoded from the record, based on this field descriptor.
		 */
		public Object toObject(byte[] record) throws IOException {
			String str = getString(record, offset, length);
			switch (type) {
			case STRING:
				return str;

			case DATE:
				try {
					return sdf.parse(str);
				} catch (ParseException exc) {
					throw (new IOException("Invalid date."));
				}

			case BOOLEAN:
				return Boolean.valueOf(str);

			case INT:
				try {
//                    	System.out.println(str);
					return Integer.valueOf(str);
				} catch (NumberFormatException exc) {
					System.out.print("This is the error ");
					System.out.println(str);
					throw (new IOException("Invalid number."));
				}

			case FLOAT:
				try {
					return Float.valueOf(str);
				} catch (NumberFormatException exc) {
					throw (new IOException("Invalid number [" + str + "]."));
				}

			default:
				return str;
			}
		}

		/**
		 * Converts the obj to a sequence of bytes based on the type of the field descriptor and puts those in the right
		 * location in the record.
		 */
		public void fromObject(Object obj, byte[] record) throws IOException {
			byte[] b;

			switch (type) {
			case STRING:
				if (obj instanceof String) {
					b = ((String) obj).getBytes();
				} else {
					throw (new IllegalArgumentException("Excpected a String."));
				}
				break;

			case DATE:
				if (obj instanceof Date) {
					b = sdf.format((Date) obj).getBytes();
				} else {
					throw (new IllegalArgumentException("Excpected a Date."));
				}
				break;

			case BOOLEAN:
				if (obj instanceof Boolean) {
					if (((Boolean) obj).booleanValue()) {
						b = new byte[] { 'Y' };
					} else {
						b = new byte[] { 'N' };
					}
				} else {
					throw (new IllegalArgumentException("Excpected a BOOLEAN."));
				}
				break;

			case INT:
			case FLOAT:
				if (obj instanceof Number) {
					b = formatNumber((Number) obj);
				} else {
					throw (new IllegalArgumentException("Excpected a Number."));
				}
				break;

			default:
				b = obj.toString().getBytes();
			}
			int len = b.length > length ? length : b.length;
			System.arraycopy(b, 0, record, offset, len);
		}

		/**
		 * Returns a string displaying the field descriptor, useful for debugging.
		 */
		@Override
		public String toString() {
			return name + "[" + type + ":" + length + " (" + decimals + ") @ " + offset + "]";
		}

		/**
		 * Special routine to format a number that can be written to disk. This will make sure the number of chars after
		 * the decimal point is correct.
		 */
		private byte[] formatNumber(Number number) {
			String result = nf.format(number);
			String[] parts = result.split("\\.", 2);
			if ((parts.length == 2) && (parts[1].length() > decimals)) {
				result = parts[0] + "." + parts[1].substring(0, decimals);
			}
			return result.getBytes();
		}
	}
}

//    DBF FILE STRUCTURE
//    ~~~~~~~~~~~~~~~~~~
//
//    BYTES   DESCRIPTION
//    00	FoxBase+, FoxPro, dBaseIII+, dBaseIV, no memo - 0x03
//            FoxBase+, dBaseIII+ with memo - 0x83
//        FoxPro with memo - 0xF5
//        dBaseIV with memo - 0x8B
//        dBaseIV with SQL Table - 0x8E
//
//    01-03   Last update, format YYYYMMDD   **correction: it is YYMMDD**
//    04-07	Number of records in file (32-bit number)
//    08-09	Number of bytes in header (16-bit number)
//    10-11	Number of bytes in record (16-bit number)
//    12-13	Reserved, fill with 0x00
//    14	dBaseIV flag, incomplete transaction
//            Begin Transaction sets it to 0x01
//        End Transaction or RollBack reset it to 0x00
//
//    15      Encryption flag, encrypted 0x01 else 0x00
//            Changing the flag does not encrypt or decrypt the records
//
//    16-27   dBaseIV multi-user environment use
//    28	Production index exists - 0x01 else 0x00
//    29	dBaseIV language driver ID
//    30-31   Reserved fill with 0x00
//    32-n	Field Descriptor array
//    n+1	Header Record Terminator - 0x0D
//
//    FIELD DESCRIPTOR ARRAY TABLE
//    BYTES   DESCRIPTION
//    0-10    Field Name ASCII padded with 0x00
//    11	Field Type Identifier (see table)
//    12-15	Displacement of field in record
//    16	Field length in bytes
//    17	Field decimal places
//    18-19	Reserved
//    20	dBaseIV work area ID
//    21-30	Reserved
//    31 	Field is part of production index - 0x01 else 0x00
//
//    FIELD IDENTIFIER TABLE
//    ASCII   DESCRIPTION
//    C       Character
//    D       Date, format YYYYMMDD
//    F       Floating Point
//    G       General - FoxPro addition
//    L       Logical, T:t,F:f,Y:y,N:n,?-not initialized
//    M       Memo (stored as 10 digits representing the dbt block number)
//    N       Numeric
//    P       Picture - FoxPro addition
//
//    Note all dbf field records begin with a deleted flag field.
//    If record is deleted - 0x2A (asterisk) else 0x20 (space)
//    End of file is marked with 0x1A

