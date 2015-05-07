package org.geocraft.io.las;


import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.geocraft.core.common.util.Utilities;
import org.geocraft.io.las.LasReader.Delimiter;


/**
 * Unit test for the various aspects of reading an LAS file.
 */
public class LasReaderTestCase extends TestCase {

  /**
   * Unit test for checking the existence and readability of an LAS file.
   */
  public void testFile() {
    String path = Utilities.getPath("org.geocraft.io.las.test");
    File file = new File(path + "data" + File.separator + "test.las");
    assertTrue(file.exists());
    assertTrue(file.canRead());
  }

  /**
   * Unit test for parsing the header block of an LAS file.
   */
  public void testHeaderParser() {
    String missingCol3 = "CALI .IN                   :  Loaded";
    String[] result1 = LasReader.splitHeaderRecord(missingCol3);
    System.out.println(Arrays.toString(result1));
    String missingCol2 = "NULL .              -999.25:  NULL VALUE";
    String[] result2 = LasReader.splitHeaderRecord(missingCol2);
    System.out.println(Arrays.toString(result2));
    String allCols = "STRT .F                7800:  START DEPTH";
    String[] result3 = LasReader.splitHeaderRecord(allCols);
    System.out.println(Arrays.toString(result3));
    assertEquals("CALI", result1[0]);
    assertEquals("IN", result1[1]);
    assertEquals("", result1[2]);
    assertEquals("Loaded", result1[3]);
    assertEquals("NULL", result2[0]);
    assertEquals("", result2[1]);
    assertEquals("-999.25", result2[2]);
    assertEquals("NULL VALUE", result2[3]);
    assertEquals("STRT", result3[0]);
    assertEquals("F", result3[1]);
    assertEquals("7800", result3[2]);
    assertEquals("START DEPTH", result3[3]);
  }

  /**
   * Unit test for parsing an LAS record header with multiple substrings in the value column.
   */
  public void testHeaderParserMultiValue() {
    String header = "COMP .       ANY OIL COMPANY INC.             : COMPANY";
    String[] result = LasReader.splitHeaderRecord(header);
    assertEquals("COMP", result[0]);
    assertEquals("", result[1]);
    assertEquals("ANY OIL COMPANY INC.", result[2]);
    assertEquals("COMPANY", result[3]);
  }

  /**
   * Unit test for parsing LAS data records.
   */
  public void testParsingOfDataRecords() {
    String[] names = new String[] { "DEPTH", "AT10", "AT30", "AT90", "CALI", "DTCO", "DTSM", "DT_ED", "GR", "NPHI",
        "RHOB" };
    String spaces = "  7800.0000         2.4826         1.7344         1.5953        12.9770       108.0620       238.7940       108.0620        62.0883         0.4490         2.2932";
    String tabs = "7800.0000\t2.4826\t1.7344\t1.5953\t12.9770\t108.0620\t238.7940\t108.0620\t62.0883\t0.4490\t2.2932";
    String commas = "7800.0000,2.4826,1.7344,1.5953,12.9770,108.0620,238.7940,108.0620,62.0883,0.4490,2.2932";
    // String mixed = "7800.0000,2.4826,1.7344,\t1.5953,12.9770,108.0620,238.7940,108.0620,62.0883,0.4490,2.2932\t ";
    // String missing = "7800.0000,2.4826,1.7344,1.5953,12.9770,,,,,, ";
    //String truncated = "7800.0000,2.4826,1.7344";
    String[] res = LasReader.parseDataRecordsBySpaces(spaces, names.length);
    assertEquals("7800.0000", res[0]);
    assertEquals("2.2932", res[10]);
    res = LasReader.parseDataRecordsByRegex(tabs, names.length, "\u0009", -999.25f);
    assertEquals("2.4826", res[1]);
    assertEquals("2.2932", res[10]);
    res = LasReader.parseDataRecordsByRegex(commas, names.length, ",", -999.25f);
    assertEquals("7800.0000", res[0]);
    assertEquals("2.2932", res[10]);
    // reader.parseDataRecord(0, mixed, names, logs4);
    // reader.parseDataRecord(0, missing, names, logs5);
    // try {
    // float[][] logs6 = new float[names.length][1];
    // reader.parseDataRecordComma(truncated, names);
    // fail();
    // } catch (Exception ex) {
    // // should fail.
    // }
  }

  /**
   * Unit test for loading an LAS file.
   */
  public void testLoad() {
    LasReader reader = new LasReader(Utilities.getPath("org.geocraft.io.las.test") + "data", "test.las");
    assertEquals(-999.25f, reader.getNullValue());
    assertEquals(7800f, reader.getDataRange()[0]);
    assertEquals(8368f, reader.getDataRange()[1]);
    assertEquals(0.25f, reader.getDataRange()[2]);
    String[] columns = reader.getColumnNames();
    String[] names = new String[] { "DEPTH", "AT10", "AT30", "AT90", "CALI", "DTCO", "DTSM", "DT_ED", "GR", "NPHI",
        "RHOB" };
    assertEquals(names.length, columns.length);
    for (int i = 0; i < columns.length; i++) {
      assertEquals(names[i], columns[i]);
    }
    String[] units = new String[] { "F", "OHMM", "OHMM", "OHMM", "IN", "US/F", "US/F", "US/F", "GAPI", "V/V", "G/C3" };
    Map<String, String> unitMap = reader.getUnitList();
    for (int i = 0; i < columns.length; i++) {
      assertTrue(unitMap.containsKey(names[i]));
    }
    int numRecs = (int) (1 + (8368f - 7800f) / 0.25);
    float[][] data = reader.getRawData();
    assertEquals(numRecs, data[0].length);
    assertEquals(units.length, data.length);
    assertEquals(7800f, data[0][0]);
    assertEquals(8368f, data[0][numRecs - 1]);
    assertEquals(2.3152f, data[units.length - 1][numRecs - 1]);
    // // sanity check total number of points
    // WellBore[] bores = well.getWellBores();
    // assertEquals(1, bores.length);
    //
    // WellLogTrace[] traces = bores[0].getWellLogTraces();
    // assertEquals(11, traces.length);
    //
    // for (WellLogTrace trace : traces) {
    // System.out.println(trace);
    // }
  }

  /**
   * Unit test for parsing the "wrapped" LAS data format.
   */
  public void testParseWrappedLine() {
    String[] records = { "1900.0000",
        "1900.0000  -1877.0000       1.7132       1.7321       1.7532       1.7914       1.7831       1.7830",
        "-0.0085     110.3195     110.3290     105.8072     367.3773     370.9969",
        "110.3213     110.4587     110.4589     835.3944       4.4855      69.9999",
        "70.2882      12.2870       0.0030     209.6898    -999.2500       4.8809",
        "-999.2500       2.4041       2.4032       2.4039       2.4089       2.4089",
        "0.5914       0.6545       0.0069", "1901.0000",
        "1900.0000  -1877.0000       1.7132       1.7321       1.7532       1.7914       1.7831       1.7830",
        "-0.0085     110.3195     110.3290     105.8072     367.3773     370.9969",
        "110.3213     110.4587     110.4589     835.3944       4.4855      69.9999",
        "70.2882      12.2870       0.0030     209.6898    -999.2500       4.8809",
        "-999.2500       2.4041       2.4032       2.4039       2.4089       2.4089",
        "0.5914       0.6545       1.0069" };
    List<String[]> result = LasReader.parseWrappedDataRecords(records, 36, 0, Delimiter.SPACE, -999.25f);
    String[] record = result.get(0);
    assertEquals("1900.0000", record[0]);
    assertEquals("0.0069", record[record.length - 1]);

    record = result.get(1);
    assertEquals("1901.0000", record[0]);
    assertEquals("1.0069", record[record.length - 1]);
  }

  /**
   * Unit test for creation of unique mnemonics.
   */
  public void testUniqueMnem() {
    Map<String, String> unitList = new HashMap<String, String>();
    assertEquals("foo", LasReader.createUniqueMnemonic("foo", unitList));
    unitList.put("foo", "foo");
    assertEquals("foo_1", LasReader.createUniqueMnemonic("foo", unitList));
    unitList.put("foo_1", "foo");
    assertEquals("foo_2", LasReader.createUniqueMnemonic("foo", unitList));
  }
}
