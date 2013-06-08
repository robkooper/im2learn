package edu.illinois.ncsa.isda.im2learn.core.geo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

public class PRJLoader {
	public static Projection getProjection(File file) throws GeoException, IOException {
		String input = "";
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			input += line;
		}
		return getProjection(input);
	}

	public static Projection getProjection(String input) throws GeoException {
		String[] temp1 = parseRow(input);
		if (temp1[0].equalsIgnoreCase("PROJCS")) {
			return getProjection(temp1);
		} else if (temp1[0].equalsIgnoreCase("GEOGCS")) {
			GeoGraphicCoordinateSystem geogcs = getGeoGraphicCoordinateSystem(temp1);
			Projection projection = Projection.getProjection(Projection.ProjectionType.Geographic, geogcs);
			return projection;
		}
		throw new GeoException("Could not parse input");
	}

	private static Projection getProjection(String[] input) throws GeoException {
		String name = null;
		Projection projection = null;
		Projection.ProjectionType type = null;
		GeoGraphicCoordinateSystem geoCS = null;
		Map<String, String> parameters = new Hashtable<String, String>();
		TiePoint tiePoint = null;
		LinearUnit unit = null;

		name = removeQuotes(input[1]);
		for (int i = 0; i < input.length; i++) {
			String[] array = parseBracket(input[i]);
			if (array[0].equalsIgnoreCase("GEOGCS")) {
				geoCS = getGeoGraphicCoordinateSystem(parseRow(input[i]));
			} else if (array[0].equalsIgnoreCase("PROJECTION")) {
				type = getProjectionType(parseRow(input[i]));
			} else if (array[0].equalsIgnoreCase("PARAMETER")) {
				String[] array1 = parseRow(input[i]);
				parameters.put(removeQuotes(array1[1]), array1[2]);
			}
			// add in new item for tiepoint
			else if (array[0].equalsIgnoreCase("UNIT")) {
				unit = getLinearUnit(parseRow(input[i]));
			}
		}
		projection = Projection.getProjection(name, type, geoCS, parameters, tiePoint, unit);
		return projection;
	}

	private static GeoGraphicCoordinateSystem getGeoGraphicCoordinateSystem(String[] input) {
		GeoGraphicCoordinateSystem ggcs = null;
		String name = null;
		Datum datum = null;
		PrimeMeridian primeMeridian = null;
		AngularUnit unit = null;

		name = removeQuotes(input[1]);
		for (int i = 0; i < input.length; i++) {
			String[] array = parseBracket(input[i]);
			if (array[0].equalsIgnoreCase("DATUM")) {
				datum = getDatum(parseRow(input[i]));
			} else if (array[0].equalsIgnoreCase("PRIMEM")) {
				primeMeridian = getPrimeMeridian(parseRow(input[i]));
			} else if (array[0].equalsIgnoreCase("UNIT")) {
				unit = getAngularUnit(parseRow(input[i]));
			}
		}

		ggcs = new GeoGraphicCoordinateSystem(name, datum, primeMeridian, unit);

		return ggcs;
	}

	private static Datum getDatum(String[] input) {
		Datum datum = Datum.getDatum(removeQuotes(input[1]));
		if (datum == null) {
			String name;
			Ellipsoid ellipsoid = null;

			name = removeQuotes(input[1]);
			for (int i = 0; i < input.length; i++) {
				if (parseBracket(input[i])[0].equalsIgnoreCase("SPHEROID")) {
					ellipsoid = getSpheroid(parseRow(input[i]));
				}
			}
			datum = new Datum(name, ellipsoid);
		}
		return datum;
	}

	private static Ellipsoid getSpheroid(String[] input) {
		Ellipsoid spheroid = new Ellipsoid(removeQuotes(input[1]), Double.parseDouble(input[2]),
				Double.parseDouble(input[3]));
		return spheroid;
	}

	private static PrimeMeridian getPrimeMeridian(String[] input) {
		PrimeMeridian primeMeridian = new PrimeMeridian(removeQuotes(input[1]), Double.parseDouble(input[2]));
		return primeMeridian;
	}

	private static AngularUnit getAngularUnit(String[] input) {
		AngularUnit unit = new AngularUnit(removeQuotes(input[1]), Double.parseDouble(input[2]));
		return unit;
	}

	private static LinearUnit getLinearUnit(String[] input) {
		LinearUnit unit = new LinearUnit(removeQuotes(input[1]), Double.parseDouble(input[2]));
		return unit;
	}

	private static Projection.ProjectionType getProjectionType(String[] input) {
		return Projection.getProjectionType(removeQuotes(input[1]));
	}

	private static String removeQuotes(String input) {
		if ((input.charAt(0) == '"') && (input.charAt(input.length() - 1) == '"')) {
			return input.substring(1, input.length() - 1);
		} else {
			return input;
		}
	}

	private static String[][] parsePROJCS(String input) {
		String[][] array = new String[4][];
		array[0] = parseRow(input);
		array[1] = parseRow(array[0][2]);
		array[2] = parseRow(array[1][2]);
		array[3] = parseRow(array[2][2]);
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.print(array[i][j] + " - ");
			}
			System.out.println();
		}
		return array;
	}

	private static String[][] parseGEOGCS(String input) {
		String[][] array = new String[3][];
		array[0] = parseRow(input);
		array[1] = parseRow(array[0][2]);
		array[2] = parseRow(array[1][2]);
		return array;
	}

	private static String[] parseRow(String input) {
		String[] output = null;
		String[] temp1 = parseBracket(input);
		String[] temp2 = parseComma(temp1[1]);
		output = new String[temp2.length + 1];
		output[0] = temp1[0];
		for (int i = 1; i < output.length; i++) {
			output[i] = temp2[i - 1];
		}
		return output;
	}

	/**
	 * Reads an input string and outputs a 2 length String array. The substring before the first square bracket goes
	 * into cell 0. The substring within square brackets goes in to cell 2.
	 * 
	 * @param input
	 * @return
	 */
	private static String[] parseBracket(String input) {
		String[] output = null;
		int startIndex = 0;
		boolean startTagged = false;
		int endIndex = 0;
		for (int i = 0; i < input.length(); i++) {
			if ((input.charAt(i) == '[') && !startTagged) {
				startIndex = i;
				startTagged = true;
			}
			if (input.charAt(i) == ']') {
				endIndex = i;
			}
		}
		if ((startIndex == 0) && (endIndex == 0)) {
			String[] noBrackets = new String[1];
			noBrackets[0] = input;
			return noBrackets;
		}
		output = new String[2];
		output[0] = input.substring(0, startIndex);
		output[1] = input.substring(startIndex + 1, endIndex);
		return output;
	}

	private static String[] parseComma(String input) {
		int numCommas = commaCount(input);
		int startInd = 0;
		int endInd = 0;
		int indexCount = 0;
		String[] output = new String[numCommas + 1];
		int bracketCount = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '[') {
				bracketCount++;
			} else if (input.charAt(i) == ']') {
				bracketCount--;
			} else if ((bracketCount == 0) && (input.charAt(i) == ',')) {
				endInd = i;
				output[indexCount] = input.substring(startInd, endInd);
				startInd = i + 1;
				indexCount++;
			}
		}
		output[indexCount] = input.substring(startInd);
		return output;
	}

	/**
	 * Counts the number of commas in this input string. It does not count the commas within brackets.
	 * 
	 * @param input
	 * @return
	 */
	private static int commaCount(String input) {
		int bracketCount = 0;
		int count = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '[') {
				bracketCount++;
			}
			if (input.charAt(i) == ']') {
				bracketCount--;
			}
			if ((bracketCount == 0) && (input.charAt(i) == ',')) {
				count++;
			}
		}
		return count;
	}
}
