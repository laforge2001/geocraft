/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocraft.core.common.io.TextFile;
import org.geocraft.core.model.datatypes.Country;
import org.geocraft.core.model.datatypes.Unit;


/**
 * This class is used to read LAS-formatted files containing wells and log curves.
 */
public final class LasReader {

  /**
   * Enumeration of the available delimiters that specify
   * how the data records are separated in an LAS file.
   */
  enum Delimiter {
    SPACE, // One or more spaces.
    COMMA, // Supports missing values.
    TAB, // I think this means one tab only?
    INVALID, // Something was specified but it was not recognized.  
    UNSPECIFIED, // Defaults to SPACE delimited. 
  }

  /** Column of the "Mnemonic" in the header. */
  public static final int MNEM = 0;

  /** Column of the "Unit" in the header. */
  public static final int UNIT = 1;

  /** Column of the "Value" in the header. */
  public static final int VALUE = 2;

  /** Column of the "Description" in the header. */
  public static final int DESC = 3;

  /** Column of the for "Format" in the header. */
  public static final int FORMAT = 4;

  /** Column of the "Association" in the header. */
  public static final int ASSOC = 5;

  /** Default to assuming the data is space delimited. */
  private Delimiter _delimiter = Delimiter.SPACE;

  private String _wellName;

  private String _versionOfLAS = "";

  /** If true each measurement is on multiple lines in the file. */
  private boolean _wrapped = false;

  /** The raw log data. */
  private float[][] _logs;

  /** The null data value e.g. -999.25. */
  private float _nullValue;

  /** The description of the curve units of the logs in the LAS file. */
  private final Map<String, String> _unitList = new LinkedHashMap<String, String>();

  /** The comments for each of the logs. */
  private final Map<String, String> _comments = new HashMap<String, String>();

  /** Starting depth of log records. */
  private float _start = Float.NaN;

  /** Stopping depth of log records. */
  private float _stop = Float.NaN;

  /** Step between records. */
  private float _step = Float.NaN;

  /** Operating oil company name. */
  private String _company = "";

  /** Name of the oil field. */
  private String _field = "";

  /** Name of the well's location. */
  private String _location = "";

  /** The name of the service company that drilled the well. */
  private String _serviceCompany = "";

  /** Country of this well. */
  private Country _country;

  /** Date the well was drilled. */
  private String _serviceDate = "";

  /** Required if there are lat/long or x/y coordinates. */
  private String _geodeticDatum = "";

  /** Latitude of the well. */
  private double _latitude = 0.0;

  /** Longitude of the well. */
  private double _longitude = 0.0;

  /** X coordinate of the well. */
  private double _x = 0.0;

  /** Y coordinate of the well. */
  private double _y = 0.0;

  /** Horizontal coordinate system. */
  private String _horizontalCoordinateSystem = "";

  /** US state. */
  private String _state = "";

  /** US county. */
  private String _county = "";

  /** US unique id. */
  private String _api = "";

  /** Canadian province. */
  private String _province = "";

  /** Canadian unique well id. */
  private String _uwi = "";

  /** Canadian license number. */
  private String _license = "";

  public LasReader(final String directory, final String fileName) {
    String filePath = directory + File.separator + fileName;
    File file = new File(filePath);
    if (!file.exists() || !file.canRead() || file.isDirectory()) {
      throw new RuntimeException("Cannot acces the LAS file: " + filePath);
    }

    TextFile tf = new TextFile(filePath);
    load(tf);
    tf = null;
  }

  public LasReader(final List<String> tf) {
    load(tf);
  }

  /**
   * Loads the contents of an LAS file that has been parsed into a collection of strings.
   * 
   * @param recordList the collection of records.
   */
  private void load(final List<String> recordList) {
    List<String> validRecords = new ArrayList<String>();

    // Validate and store each of the records that are
    // greater than zero in length. 
    for (String record : recordList) {
      if (record.trim().length() > 0) {
        validRecords.add(record.trim());
      }
    }

    String[] records = validRecords.toArray(new String[validRecords.size()]);
    validRecords = null;

    // Parse the header records.
    parseHeader(records);

    // Parse the data records.
    parseLasData(records);
  }

  /**
   * Returns the LAS version number contained in the LAS file.
   * 
   * @return the LAS version number.
   */
  public String getVersionOfLAS() {
    return _versionOfLAS;
  }

  /**
   * Returns the well name contained in the LAS file.
   * 
   * @return the well name.
   */
  public String getWellName() {
    return _wellName;
  }

  /**
   * Returns the company contained in the LAS file.
   * 
   * @return the company.
   */
  public String getCompany() {
    return _company;
  }

  /**
   * Returns the field name contained in the LAS file.
   * 
   * @return the field name.
   */
  public String getField() {
    return _field;
  }

  /**
   * Returns the location contained in the LAS file.
   * 
   * @return the location.
   */
  public String getLocation() {
    return _location;
  }

  /**
   * Returns the service company contained in the LAS file.
   * 
   * @return the service company.
   */
  public String getServiceCompany() {
    return _serviceCompany;
  }

  /**
   * Returns the country contained in the LAS file.
   * 
   * @return the country.
   */
  public Country getCountry() {
    return _country;
  }

  /**
   * Returns the service date contained in the LAS file.
   * 
   * @return the service date.
   */
  public String getServiceDate() {
    return _serviceDate;
  }

  /**
   * Returns the geodetic datum contained in the LAS file.
   * 
   * @return the geodetic datum.
   */
  public String getGeodeticDatum() {
    return _geodeticDatum;
  }

  /**
   * Returns the latitude of the well contained in the LAS file.
   * 
   * @return the latitude of the well.
   */
  public double getLatitude() {
    return _latitude;
  }

  /**
   * Returns the longitude of the well contained in the LAS file.
   * 
   * @return the longitude of the well.
   */
  public double getLongitude() {
    return _longitude;
  }

  /**
   * Returns the x coordinate of the well contained in the LAS file.
   * 
   * @return the x coordinate of the well.
   */
  public double getXCoordinate() {
    return _x;
  }

  /**
   * Returns the y coordinate of the well contained in the LAS file.
   * 
   * @return the y coordinate of the well.
   */
  public double getYCoordinate() {
    return _y;
  }

  /**
   * Returns the horizontal coordinate system of the well contained in the LAS file.
   * 
   * @return the horizontal coordinate system of the well.
   */
  public String getHorizontalCoordinateSystem() {
    return _horizontalCoordinateSystem;
  }

  /**
   * Returns the US state contained in the LAS file.
   * 
   * @return the US state.
   */
  public String getState() {
    return _state;
  }

  /**
   * Returns the US county contained in the LAS file.
   * 
   * @return the US county.
   */
  public String getCounty() {
    return _county;
  }

  /**
   * Returns the US unique ID (i.e. API) contained in the LAS file.
   * 
   * @return the US unique ID.
   */
  public String getApi() {
    return _api;
  }

  /**
   * Returns the Canadian province contained in the LAS file.
   * 
   * @return the Canadian provice.
   */
  public String getProvince() {
    return _province;
  }

  /**
   * Returns the Canadian unique ID (i.e. UWI) contained in the LAS file.
   * 
   * @return the Canadian unique ID.
   */
  public String getUwi() {
    return _uwi;
  }

  /**
   * Returns the Canadian licence number contained in the LAS file.
   * 
   * @return the Canadian license number.
   */
  public String getLicense() {
    return _license;
  }

  /**
   * Returns an array of the raw log data contained in the LAS file.
   * 
   * @return the raw log data.
   */
  public float[][] getRawData() {
    return _logs;
  }

  /**
   * Returns an array of the data ranges (start, end, delta) contained in the LAS file.
   * 
   * @return the data ranges.
   */
  public float[] getDataRange() {
    return new float[] { _start, _stop, _step };
  }

  /**
   * Returns the <i>null</i> data value contained in the LAS file.
   * 
   * @return the <i>null</i> data value.
   */
  public float getNullValue() {
    return _nullValue;
  }

  /**
   * Returns an array of the column names contained in the LAS file.
   * 
   * @return the column names.
   */
  public String[] getColumnNames() {
    return _unitList.keySet().toArray(new String[_unitList.size()]);
  }

  /**
   * Returns a collection of the units contained in the LAS file, mapped by a mnemonic key.
   * 
   * @return a collection of the units.
   */
  public Map<String, String> getUnitList() {
    return _unitList;
  }

  /**
   * Gets a unit by the specified mnemonic key.
   * 
   * @param mnem the mnemonic.
   * @return the unit; or <code>UNDEFINED</code> if no match found.
   */
  public Unit getUnits(final String mnem) {

    // Try to find the LAS unit by name;
    Unit unit = Unit.UNDEFINED;
    boolean unitFound = false;
    try {
      unit = Unit.lookupByName(_unitList.get(mnem));
      unitFound = true;
    } catch (IllegalArgumentException e) {
      unitFound = false;
    }

    // Otherwise try to find it by symbol.
    if (!unitFound) {
      try {
        unit = Unit.lookupBySymbol(_unitList.get(mnem));
        unitFound = true;
      } catch (IllegalArgumentException e) {
        unitFound = false;
      }
    }

    return unit;
  }

  /**
   * Returns a collection of the comments contained in the LAS file.
   * 
   * @return the comments.
   */
  public Map<String, String> getCommentsMap() {
    return _comments;
  }

  /**
   * Compute the expected number of samples based upon
   * the stated start, stop and step values. 
   * 
   * There can be some inaccuracy in the computation
   * if the step value is a float with truncated 
   * precision. 
   * 
   * @return expected number of data samples
   */
  public int getExpectedNumberOfSamples() {
    return 1 + Math.round((_stop - _start) / _step);
  }

  /**
   * Gets the delimiter contained in the LAS file.
   * 
   * @return the delimiter.
   */
  public Delimiter getDelimiter() {
    return _delimiter;
  }

  /** 
   * Sets the delimiter used in the LAS file.
   * 
   * @param delimiter the delimiter.
   */
  public void setDelimiter(final Delimiter delimiter) {
    _delimiter = delimiter;
  }

  /** 
   * Parse the header records.
   */
  public void parseHeader(final String[] records) {

    // Loop thru each of the records.
    for (int i = 0; i < records.length; i++) {
      if (records[i].startsWith("~V")) {
        parseVersionBlock(records, i);
      } else if (records[i].startsWith("~W")) {
        parseWellInformationBlock(records, i);
      } else if (records[i].startsWith("~C")) {
        parseCurveInformationBlock(records, i);
      } else if (records[i].startsWith("~P")) {
        // TODO Parse parameters and constants
        continue;
      } else if (records[i].startsWith("~O")) {
        // TODO Parse other information
        continue;
      } else if (records[i].startsWith("~A")) {
        // we are done parsing the headers
        // and are ready to read the data now. 
        break;
      }
    }
  }

  /**
   * Parse the version block of the LAS file records.
   * 
   * @param records the array of records.
   * @param start the index of the starting record for this block.
   */
  private void parseVersionBlock(final String[] records, final int start) {

    int index = start + 1;
    while (!records[index++].startsWith("~")) {
      if (!records[index].startsWith("#")) {

        String[] data = splitHeaderRecord(records[index]);

        // Check for the LAS version number.
        if (data[MNEM].equalsIgnoreCase("VERS")) {
          _versionOfLAS = data[VALUE];
          // Check for the "wrapped" flag.
        } else if (data[MNEM].equalsIgnoreCase("WRAP")) {
          if (data[VALUE].equalsIgnoreCase("YES")) {
            _wrapped = true;
          } else {
            _wrapped = false;
          }
        } else if (data[MNEM].equals("DLM")) {
          if (data[VALUE].equalsIgnoreCase("COMMA")) {
            _delimiter = Delimiter.COMMA;
          } else if (data[VALUE].equalsIgnoreCase("TAB")) {
            _delimiter = Delimiter.TAB;
          } else if (data[VALUE].equalsIgnoreCase("SPACE")) {
            _delimiter = Delimiter.SPACE;
          } else {
            // Something was specified but we don't recognize it.
            _delimiter = Delimiter.INVALID;
          }
        }
      }
    }
  }

  /**
   * Parse the well information block of the LAS records.
   * 
   * @param records the array of records.
   * @param start the index of the starting record for this block.
   */
  private void parseWellInformationBlock(final String[] records, final int startIndex) {
    int index = startIndex + 1;

    String record = "";
    while (!record.startsWith("~")) {
      record = records[index++];
      if (!record.startsWith("#")) {

        String[] cols = splitHeaderRecord(record);

        String value = cols[VALUE].trim();

        // Information about the depth range of the well etc... 

        if (record.startsWith("STRT")) {
          _start = Float.parseFloat(value);
        } else if (record.startsWith("STOP")) {
          _stop = Float.parseFloat(value);
        } else if (record.startsWith("STEP")) {
          _step = Float.parseFloat(value);
        } else if (record.startsWith("NULL")) {
          _nullValue = Float.parseFloat(value);
        } else if (record.startsWith("WELL")) {
          _wellName = value;

          // Business information about the well...

        } else if (record.startsWith("COMP")) {
          _company = value;
        } else if (record.startsWith("FLD")) {
          _field = value;
        } else if (record.startsWith("LOC")) {
          _location = value;
        } else if (record.startsWith("SRVC")) {
          _serviceCompany = value;
        } else if (record.startsWith("CTRY")) {
          _country = Country.lookupByCode(value);
        } else if (record.startsWith("DATE")) {
          _serviceDate = value;

          // Geographic location of well...

        } else if (record.startsWith("GDAT")) {
          _geodeticDatum = value;
        } else if (record.startsWith("LAT")) {
          // TODO we ought to handle degrees minutes and seconds here. 
          _latitude = Double.parseDouble(value);
        } else if (record.startsWith("LONG")) {
          _longitude = Double.parseDouble(value);
        } else if (record.startsWith("X")) {
          _x = Double.parseDouble(value);
        } else if (record.startsWith("Y")) {
          _y = Double.parseDouble(value);
        } else if (record.startsWith("HZCS")) {
          _horizontalCoordinateSystem = value;

          // US state, county and API information...

        } else if (record.startsWith("STAT")) {
          _state = value;
        } else if (record.startsWith("CNTY")) {
          _county = value;
        } else if (record.startsWith("API")) {
          _api = value;

          // Canadian province, license and UWI information...

        } else if (record.startsWith("PROV")) {
          _province = value;
        } else if (record.startsWith("LIC")) {
          _license = value;
        } else if (record.startsWith("UWI")) {
          _uwi = value;
        }

      }
    }
  }

  /**
   * Parse the curve information block of the LAS records.
   * 
   * @param records the array of records.
   * @param start the index of the starting record for this block.
   */
  private void parseCurveInformationBlock(final String[] records, final int startIndex) {
    int index = startIndex + 1;
    while (!records[index].startsWith("~")) {
      if (!records[index].startsWith("#")) {
        parseUnitRecord(records[index]);
      }
      index++;
    }
  }

  /** 
   * Parse a single record in the 'curve information block'.
   * <p>
   * For example:
   * <br>
   * DTCO .US/F                 :  Filled log.
   * 
   * @param record the curve information block record to parse.
   */
  private void parseUnitRecord(final String record) {
    String[] data = splitHeaderRecord(record);

    // Get the mnemonic of the curve.
    String mnem = createUniqueMnemonic(data[MNEM].trim(), _unitList);

    // Add the curve unit to the units collection, mapped by the curve mnemonic.
    _unitList.put(mnem, data[UNIT].trim());
    // Add the curve comments to the comments collection, mapped by the curve mnemonic.
    _comments.put(mnem, data[VALUE].trim());
  }

  /**
   * Returns a unique curve mnemonic, in the event a duplicate mnemonic exists in the LAS records.
   * 
   * @param mnem the mnemonic to check.
   * @return a unique mnemonic (the original mnemonic of not duplicated).
   */
  public static String createUniqueMnemonic(final String mnem, final Map<String, String> unitList) {
    String result = mnem;
    if (unitList.containsKey(mnem)) {
      int suffix = 1;
      while (unitList.containsKey(mnem + "_" + suffix)) {
        suffix++;
      }
      result = mnem + "_" + suffix;
    }
    return result;
  }

  /**
   * Parse the LAS well log data records.
   */
  private void parseLasData(final String[] records) {

    List<String[]> foo = null;

    if (_wrapped) {
      // Parse the records from the "wrapped" format.
      for (int i = 0; i < records.length; i++) {
        if (records[i].startsWith("~A")) {
          foo = parseWrappedDataRecords(records, getColumnNames().length, i + 1, _delimiter, _nullValue);
          break;
        }
      }
    } else {
      // Parse the records from the "non-wrapped" format.
      for (int i = 0; i < records.length; i++) {
        if (records[i].startsWith("~A")) {
          foo = parseNonWrappedDataRecords(records, getColumnNames().length, i + 1, _delimiter, _nullValue);
        }
      }
    }

    // Convert the list of data records to log data arrays.
    _logs = parseRecordListArray(foo, getColumnNames().length);
  }

  /**
   * Parses an array of "non-wrapped" data records.
   * 
   * @param records the array of records to parse.
   * @param numTraces the number of curves in the record.
   * @param firstDataRecord the index of the first record to parse.
   * @return the collection of parsed records.
   */
  private static List<String[]> parseNonWrappedDataRecords(final String[] records, final int numTraces,
      final int firstDataRecord, final Delimiter delimiter, final float nullValue) {

    List<String[]> list = new ArrayList<String[]>();
    String[] data = null;

    for (int i = firstDataRecord; i < records.length; i++) {
      if (delimiter == Delimiter.SPACE || delimiter == Delimiter.UNSPECIFIED || delimiter == Delimiter.INVALID) {
        data = parseDataRecordsBySpaces(records[i], numTraces);
      } else if (delimiter == Delimiter.TAB) {
        data = parseDataRecordsByRegex(records[i], numTraces, "\\t", nullValue);
      } else if (delimiter == Delimiter.COMMA) {
        data = parseDataRecordsByRegex(records[i], numTraces, ",", nullValue);
      }
      list.add(data);
    }

    return list;
  }

  public static float[][] parseRecordListArray(final List<String[]> data, final int numTraces) {

    float[][] logs = new float[numTraces][data.size()];

    for (int i = 0; i < data.size(); i++) {

      String[] record = data.get(i);

      for (int col = 0; col < numTraces; col++) {
        try {
          float value = Float.parseFloat(record[col]);
          logs[col][i] = value;
        } catch (NumberFormatException ex) {
          // could not parse the value in this column. 
        }
      }
    }

    return logs;
  }

  /**
   * Parses an array of "wrapped" data records.
   * <p>
   * Note: We could make this faster and use less memory but it would be more 
   * complex. Make it work first. 
   * 
   * @param records the array of records to parse.
   * @param numTraces the number of curves in the record.
   * @param firstDataRecord the index of the first record to parse.
   * @return the collection of parsed records.
   */
  public static List<String[]> parseWrappedDataRecords(final String[] records, final int numTraces,
      final int firstDataRecord, final Delimiter delimiter, final float nullValue) {

    List<String[]> result = new ArrayList<String[]>();

    int i = firstDataRecord;

    while (i < records.length) {

      String[] record = new String[numTraces];

      // The index value is on it's own line.
      record[0] = records[i++].trim();

      String[] data = null;

      int col = 1;
      while (col < numTraces) {
        if (delimiter == Delimiter.SPACE || delimiter == Delimiter.UNSPECIFIED || delimiter == Delimiter.INVALID) {
          data = parseDataRecordsBySpaces(records[i++], numTraces);
        } else if (delimiter == Delimiter.TAB) {
          data = parseDataRecordsByRegex(records[i++], numTraces, "\\t", nullValue);
        } else if (delimiter == Delimiter.COMMA) {
          data = parseDataRecordsByRegex(records[i++], numTraces, ",", nullValue);
        } else {
          throw new IllegalStateException("Delimiter not defined.");
        }

        System.arraycopy(data, 0, record, col, data.length);
        col += data.length;
      }
      result.add(record);
    }
    return result;
  }

  /**
   * Parses a space-delimited record containing curve data.
   * 
   * @param record the record to parse.
   * @param numTraces the number of curves in the record.
   * @return the array of curve values.
   */
  public static String[] parseDataRecordsBySpaces(final String record, final int numTraces) {
    String regex = "\\s+";
    return record.trim().split(regex);
  }

  /**
   * Parses a regex-delimited record containing curve data.
   * 
   * @param record the record to parse.
   * @param numTraces the number of curves in the record.
   * @param delimiter the regex delimiter.
   * @return the array of curve values.
   */
  public static String[] parseDataRecordsByRegex(final String record, final int numTraces, final String delimiter,
      final float nullValue) {
    String[] result = new String[numTraces];
    String remainder = record;
    int end = 0;
    for (int i = 0; i < numTraces; i++) {
      end = remainder.indexOf(delimiter);
      if (end == -1) {
        result[i] = remainder;
      } else {
        result[i] = remainder.substring(0, end);
      }
      if (result[i].length() == 0) {
        result[i] = "" + nullValue;
      }
      remainder = remainder.substring(end + 1);
    }
    return result;
  }

  /**
   * Splits a header record.
   * <p>
   * For example:<br>
   * STRT .F                7800:  START DEPTH
   * <br>
   * AT10 .OHMM                 :  Loaded
   * <p>
   * I chose not to use String.split() because it is
   * easier to do error handling if you explicitly 
   * break up the record using the period and colon
   * location. 
   * 
   * @param record the record to split.
   * @return the array of record columns.
   */
  public static String[] splitHeaderRecord(final String record) {

    int periodIndex = record.indexOf(".");
    int colonIndex = record.indexOf(":");

    // If the . or : are missing then give up on this record.
    if (periodIndex == -1 || colonIndex == -1) {
      return new String[] { "", "", "", "", "" };
    }

    // Extract the mnemonic.
    String name = record.substring(0, periodIndex).trim();

    // Extract the unit.
    String remains = record.substring(periodIndex + 1); // don't trim yet in case unit is blank
    int endOfUnit = remains.indexOf(" ");
    String unit = remains.substring(0, endOfUnit);

    // Extract the type or code.
    remains = remains.substring(endOfUnit).trim();
    colonIndex = remains.indexOf(":");
    String code = remains.substring(0, colonIndex).trim();

    // Extract the description.
    String description = remains.substring(colonIndex + 1).trim();

    return new String[] { name, unit, code, description };
  }

}
