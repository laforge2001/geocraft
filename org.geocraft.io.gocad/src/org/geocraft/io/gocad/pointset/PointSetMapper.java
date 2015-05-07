/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.gocad.pointset;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.PointSetAttribute;
import org.geocraft.core.model.PointSetAttribute.Type;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.MultiPoint;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.service.ServiceProvider;


public class PointSetMapper extends AbstractMapper<PointSet> {

  private static final String PROPERTIES = "PROPERTIES";

  private static final String PVRTX = "PVRTX";

  protected PointSetMapperModel _model;

  public PointSetMapper(final PointSetMapperModel model) {
    _model = model;
  }

  @Override
  public PointSetMapper factory(final MapperModel mapperModel) {
    return new PointSetMapper((PointSetMapperModel) mapperModel);
  }

  @Override
  protected void createInStore(final PointSet pointSet) {
    throw new UnsupportedOperationException("Creation of this format not yet supported.");
  }

  @Override
  protected void deleteFromStore(final PointSet pointSet) {
    throw new UnsupportedOperationException("Deletion of this format not yet supported.");
  }

  @Override
  protected PointSetMapperModel getInternalModel() {
    return _model;
  }

  @Override
  protected void readFromStore(final PointSet pointSet) throws IOException {
    readFromStore(pointSet, new NullProgressMonitor());
  }

  @Override
  protected void readFromStore(final PointSet pointSet, final IProgressMonitor monitor) throws IOException {
    String filePath = _model.getDirectory() + File.separator + _model.getFileName();

    monitor.beginTask("Loading GOCAD PointSet: " + pointSet.getDisplayName(), 100);
    monitor.worked(1);
    monitor.subTask("Reading file...");

    File file = new File(filePath);
    if (!file.exists() || !file.canRead() || file.isDirectory()) {
      throw new RuntimeException("Cannot read pointset file: " + filePath);
    }

    // TODO: Need to design this such that we don't have to read the file twice.
    int numLines = getNumberOfLines(file, monitor);

    monitor.worked(9);

    BufferedReader reader = createBufferedReader(file);
    String[] propertyNames = loadPoints(pointSet, reader, numLines, monitor);
    reader.close();

    pointSet.setLastModifiedDate(new Timestamp(file.lastModified()));
    pointSet.setDirty(false);
  }

  @Override
  protected void updateInStore(final PointSet pointSet) {
    throw new UnsupportedOperationException("Update of this format not yet supported.");
  }

  @Override
  public PointSetMapperModel getModel() {
    return new PointSetMapperModel(_model);
  }

  private int getNumberOfLines(final File file, final IProgressMonitor monitor) throws IOException {
    int numLines = 0;
    BufferedReader reader = createBufferedReader(file);
    while (reader.readLine() != null) {
      numLines++;
    }
    reader.close();
    return numLines;
  }

  private BufferedReader createBufferedReader(final File file) throws FileNotFoundException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    return reader;
  }

  protected String[] loadPoints(final PointSet pointSet, final BufferedReader reader, final int numLines,
      final IProgressMonitor monitor) throws IOException {

    List<Point3d> points = new ArrayList<Point3d>();

    List<String> propertyNames = new ArrayList<String>();

    int prevWorked = 0;
    int pointIndex = 0;
    for (int i = 0; i < numLines; i++) {
      String line = reader.readLine();
      if (line.startsWith(PROPERTIES)) {
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(" ");
        String properties = scanner.next();
        while (scanner.hasNext()) {
          pointSet.addAttribute(Type.FLOAT, scanner.next());
        }
      } else if (line.startsWith(PVRTX)) {
        try {
          MultiPoint mpt = parsePoint(line);
          pointSet.addPoint(new Point3d(mpt.getX(), mpt.getY(), mpt.getZ()));
          String[] attrNames = pointSet.getAttributeNames();
          for (int j = 0; j < attrNames.length; j++) {
            PointSetAttribute attr = pointSet.getAttribute(attrNames[j]);
            attr.setFloat(pointIndex, Float.parseFloat(mpt.getAttribute(j)));
          }
          pointIndex++;
        } catch (Exception ex) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error("Could not parse record #" + i);
          System.out.println("Could not parse record #" + i);
        }
      }
      if (i % 1000 == 0) {
        int currentWorked = 45 * i / numLines;
        int worked = currentWorked - prevWorked;
        prevWorked = currentWorked;
        monitor.worked(worked);
        monitor.subTask("Reading points...");
      }
    }

    pointSet.setZUnit(_model.getZUnit());

    return propertyNames.toArray(new String[0]);
  }

  protected MultiPoint parsePoint(final String record) {
    Unit xyUnitApp = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    Unit zUnitApp = UnitPreferences.getInstance().getTimeUnit();
    Unit xyUnit = _model.getXYUnit();
    Unit zUnit = _model.getZUnit();
    Domain zDomain = zUnit.getDomain();
    if (zDomain == Domain.DISTANCE) {
      zUnitApp = UnitPreferences.getInstance().getVerticalDistanceUnit();
    }

    String[] fields = record.split("\\s+");
    int currentFieldNo = 2; //skip the column index
    double x = Double.parseDouble(fields[currentFieldNo++]);
    double y = Double.parseDouble(fields[currentFieldNo++]);
    double z = Double.parseDouble(fields[currentFieldNo++]);
    x = Unit.convert(x, xyUnit, xyUnitApp);
    y = Unit.convert(y, xyUnit, xyUnitApp);
    z = Unit.convert(z, zUnit, zUnitApp);

    ArrayList<String> _attrList = new ArrayList<String>();
    while (currentFieldNo < fields.length) {
      _attrList.add(fields[currentFieldNo++]);
    }

    return new MultiPoint(new double[] { x, y, z }, _attrList);
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "GOCAD PointSet";
  }

  public String getDatastore() {
    return "GOCAD";
  }

}
