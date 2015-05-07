/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.io.las.WellMapperModel.DepthType;


/**
 * This class is used to write LAS-formatted files containing wells and log curves.
 */
public class LasWriter {

  private LasWriter() {
    // Private constructor to prevent instantiation.
  }

  /**
   * Writes an LAS file.
   */
  public static void write(final Well well, final WellMapperModel mapperModel) throws IOException, FileNotFoundException {
    WellBore wellBore = well.getWellBore();

    // Create the print writer.
    String filePath = mapperModel.getDirectory() + File.separator + mapperModel.getFileName();
    File file = createFile(filePath);
    PrintWriter pw = new PrintWriter(file);

    // Initialize the collection of log columns.
    Map<String, WellLogTrace> colVals = new LinkedHashMap<String, WellLogTrace>();

    // Write the version, well and curve info blocks.
    printVersionBlock(pw);
    printWellInfoBlock(pw, wellBore, mapperModel);
    printCurveInfoBlock(pw, wellBore, mapperModel, colVals);

    // Write the log data columns.
    printDataColumns(pw, colVals);

    // Flush and close the print writer.
    pw.flush();
    pw.close();
  }

  /**
   * Writes the version block of an LAS file.
   */
  private static void printVersionBlock(final PrintWriter pw) {
    pw.println("~Version Information Block");
    pw.println("VERS .                 3.00:  CWLS LOG ASCII STANDARD - VERSION 3.00");
    pw.println("WRAP .                   NO:  One line per depth step");
    pw.println("DLM  .                SPACE:  DELIMITER CHARACTER (SPACE TAB OR COMMA)");
    pw.println();
  }

  /**
   * Writes the well info block of an LAS file.
   */
  private static void printWellInfoBlock(final PrintWriter pw, final WellBore wellBore,
      final WellMapperModel mapperModel) {
    Well well = wellBore.getWell();
    pw.println("~Well");
    pw.println("#MNEM.UNIT        DATA        Description");
    pw.println("#---------- ----------------  -----------");

    double begDepth = Float.NaN;
    double endDepth = Float.NaN;
    double step = Float.NaN;

    float[] depths = new float[0];
    if (DepthType.TVD.equals(mapperModel.getDepthType())) {
      depths = wellBore.getTrueVerticalDepths();
    } else {
      depths = wellBore.getMeasuredDepths();
    }
    float[] rangeOld = MathUtil.computeRange(depths, Float.MAX_VALUE);

    //need to convert the range to workspace units
    //    float[] range = convertToWorkspaceUnits(rangeOld, _mapperModel.getDepthUnit());
    //    begDepth = range[0];
    //    endDepth = range[1];
    begDepth = rangeOld[0];
    endDepth = rangeOld[1];
    step = (endDepth - begDepth) / (depths.length * 1.000000 - 1.0);

    float[] dataRange = new float[] { (float) begDepth, (float) endDepth, (float) step };
    WellLogTrace[] traces = well.getWellLogTraces();
    float nullValue = Float.NaN;
    if (traces.length > 0) {
      nullValue = traces[0].getNullValue();
    }

    String companyName = well.getCurrentOperator();
    String wellName = well.getDisplayName();
    String fieldName = well.getField();
    String location = well.getLocation().toString();
    String serviceCo = well.getDataSource();
    String country = well.getCountry() != null ? well.getCountry() : "";
    String serviceDate = well.getSpudDate() != null ? well.getSpudDate().toString() : new Timestamp(System
        .currentTimeMillis()).toString();
    double x = well.getLocation().getX();
    double y = well.getLocation().getY();
    String geoDatum = well.getLocation().getSystem().getDatum();
    String hzcs = well.getLocation().getSystem().getProjection();

    Unit zUnitPref = UnitPreferences.getInstance().getVerticalDistanceUnit();
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "STRT", zUnitPref.getSymbol(), dataRange[0], "START DEPTH");
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "STOP", zUnitPref.getSymbol(), dataRange[1], "STOP DEPTH");
    pw.printf("%-5s.%-5s %25.4f:  %-12s\n", "STEP", zUnitPref.getSymbol(), dataRange[2], "STEP");

    if (nullValue != Float.NaN && !Float.isInfinite(nullValue)) {
      pw.printf("%-5s.%-5s %25s:  %-12s\n", "NULL", "", nullValue, "NULL VALUE");
    }
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "COMP", "", companyName, "Company");
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "WELL", "", wellName, "Well");
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "FLD", "", fieldName, "Field");

    if (!location.contains("NaN")) {
      pw.printf("%-5s.%-5s %25s:  %-12s\n", "LOC", "", location, "Location");
    }

    pw.printf("%-5s.%-5s %25s:  %-12s\n", "SRVC", "", serviceCo, "Service Company");
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "CTRY", "", country, "Country");
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "DATE", "", serviceDate, "Service Date");

    if (Double.compare(Double.NaN, x) != 0) {
      pw.printf("%-5s.%-5s %25s:  %-12s\n", "X", "", x, "X");
    }
    if (Double.compare(Double.NaN, y) != 0) {
      pw.printf("%-5s.%-5s %25s:  %-12s\n", "Y", "", y, "Y");
    }
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "GDAT", "", geoDatum, "Geodetic Datum");
    pw.printf("%-5s.%-5s %25s:  %-12s\n", "HZCS", "", hzcs, "Horizontal Coord System");

    final String CANADA = "ca";
    final String USA = "us";

    // if the country code isn't "ca" or "us" then the 
    // following sections are optional
    if (country != null) {
      if (country.equals(CANADA)) {
        String province = well.getStateOrProvince();
        String uwi = well.getIdentifier();
        String licenseNum = well.getIdentifier();

        pw.printf("%-5s.%-5s %25s:  %-12s\n", "PROV", "", province, "Province");
        pw.printf("%-5s.%-5s %25s:  %-12s\n", "UWI", "", uwi, "UNIQUE WELL ID");
        pw.printf("%-5s.%-5s %25s:  %-12s\n", "LIC", "", licenseNum, "License Number");
      } else if (country.equals(USA)) {
        String state = well.getStateOrProvince();
        String county = well.getCounty();
        String api = well.getIdentifier();

        pw.printf("%-5s.%-5s %25s:  %-12s\n", "STAT", "", state, "State");
        pw.printf("%-5s.%-5s %25s:  %-12s\n", "CNTY", "", county, "County");
        pw.printf("%-5s.%-5s %25s:  %-12s\n", "API", "", api, "API NUMBER");

      }
    }

    pw.println();
  }

  /**
   * Writes the curve info block of an LAS file.
   */
  private static void printCurveInfoBlock(final PrintWriter pw, final WellBore wellBore,
      final WellMapperModel mapperModel, final Map<String, WellLogTrace> colVals) {

    addTracesToMap(wellBore, colVals);

    pw.println("~Curve");
    pw.println("#MNEM.UNIT             LOG CODES                   CURVE DESCRIPTION");
    pw.println("#----------------     -----------              -------------------------");

    //WellMapperModel mapperModel = (WellMapperModel) wellBore.getWell().getMapper().getModel();
    //LasReader reader = new LasReader(mapperModel.getDirectory(), mapperModel.getFileName());

    //create a copy of the data columns map
    Map<String, WellLogTrace> copyMap = new LinkedHashMap<String, WellLogTrace>();
    copyMap.putAll(colVals);

    //retrieve the depth column
    WellLogTrace depthTrace = getDepthTrace(copyMap);

    if (depthTrace != null) {

      //remove it from the map
      copyMap.remove(depthTrace.getDisplayName());

      //print out the depth column info
      Unit dataUnit = depthTrace.getDataUnit();
      //String unitString = dataUnit.equals(Unit.UNDEFINED) ? reader.getUnitList().get(depthTrace.getDisplayName())
      //    : dataUnit.getSymbol();
      String unitString = dataUnit.getSymbol();
      pw.printf("%-8s.%-8s %15s:  %-12s\n", depthTrace.getDisplayName(), unitString, depthTrace.getComment(), "");
    }

    //print the rest, retrieving the original units stored in the original LAS file
    for (Entry<String, WellLogTrace> s : copyMap.entrySet()) {
      WellLogTrace logTrace = s.getValue();
      Unit dataUnit = logTrace.getDataUnit();
      //String unitString = dataUnit.equals(Unit.UNDEFINED) ? reader.getUnitList().get(logTrace.getDisplayName())
      //    : dataUnit.getSymbol();
      String unitString = dataUnit.getSymbol();
      pw.printf("%-8s.%-8s %15s:  %-12s\n", logTrace.getDisplayName(), unitString, logTrace.getComment(), "");
    }

    pw.println();

  }

  /**
   * Writes the data columns of an LAS file.
   */
  private static void printDataColumns(final PrintWriter pw, final Map<String, WellLogTrace> colVals) {
    pw.println("~ASCII");

    //preserves order maintained in LinkedHashMap - ie. Depth column is still first
    WellLogTrace[] traces = colVals.values().toArray(new WellLogTrace[0]);

    float[][] data = new float[traces.length][traces[0].getTraceData().length];

    for (int col = 0; col < colVals.size(); col++) {
      float[] traceData = traces[col].getTraceData();
      for (int j = 0; j < traceData.length; ++j) {
        data[col][j] = traceData[j];
      }
    }

    for (int i = 0; i < data[0].length; ++i) {
      for (int j = 0; j < data.length; ++j) {
        pw.printf("%12.4f ", data[j][i]);
      }
      pw.println();
    }
  }

  private static void convertToWorkspaceUnits(final WellLogTrace depthTrace) {

    Unit workspaceUnit = UnitPreferences.getInstance().getVerticalDistanceUnit();

    if (!depthTrace.getDataUnit().equals(workspaceUnit)) {

      float[] convertedVals = convertToWorkspaceUnits(depthTrace.getTraceData(), depthTrace.getDataUnit());

      depthTrace.setTraceData(convertedVals, workspaceUnit, depthTrace.getNullValue());
    }
  }

  /**
   * @param data
   */
  private static float[] convertToWorkspaceUnits(final float[] data, final Unit origUnit) {
    float[] oldVals = data;
    for (int i = 0; i < oldVals.length; ++i) {
      oldVals[i] = Unit.convert(oldVals[i], origUnit, UnitPreferences.getInstance().getVerticalDistanceUnit());
    }
    return oldVals;
  }

  /**
   * Returns a flag indicating if a well log trace is a "Depth" trace.
   * 
   * @param trace the well log trace to check.
   * @return <i>true</i> if "Depth" trace; <i>false</i> if not.
   */
  private static boolean isDepthTrace(final WellLogTrace trace) {
    return "DEPT".equals(trace.getDisplayName()) || "DEPTH".equals(trace.getDisplayName());
  }

  /**
   * Gets the "Depth" log trace from a collection.
   * 
   * @return returns the depth column; or <i>null</i> if none found.
   */
  private static WellLogTrace getDepthTrace(final Map<String, WellLogTrace> colVals) {
    if (colVals.containsKey("DEPT")) {
      return colVals.get("DEPT");
    }
    return colVals.get("DEPTH");
  }

  /**
   * 
   */
  private static void addTracesToMap(final WellBore wellBore, final Map<String, WellLogTrace> colVals) {
    WellLogTrace[] traces = wellBore.getWell().getWellLogTraces();

    // Find the depth trace and add it first.
    for (WellLogTrace t : traces) {
      if (isDepthTrace(t)) {

        // Exports using workspace preferences units
        //        convertToWorkspaceUnits(t);
        colVals.put(t.getDisplayName(), t);
      }
    }

    // Add the rest.
    for (WellLogTrace t : traces) {
      if (!isDepthTrace(t)) {
        colVals.put(t.getDisplayName(), t);
      }
    }
  }

  /**
   * Creates a file with the given path.
   * 
   * @param path the file path.
   * @return the newly created file.
   * @throws IOException thrown if an error occurred creating the file.
   */
  private static File createFile(final String path) throws IOException {
    File file = new File(path);
    if (!file.exists()) {
      file.createNewFile();
    }
    return file;
  }
}
