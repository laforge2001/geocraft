/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciipointset;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.service.ServiceProvider;


public class AsciiPointSetMapper extends AbstractMapper<PointSet> {

  protected AsciiPointSetMapperModel _model;

  public AsciiPointSetMapper(final AsciiPointSetMapperModel model) {
    _model = model;
  }

  @Override
  public AsciiPointSetMapper factory(final MapperModel mapperModel) {
    return new AsciiPointSetMapper((AsciiPointSetMapperModel) mapperModel);
  }

  @Override
  protected void createInStore(final PointSet pointSet) throws IOException {
    throw new UnsupportedOperationException("Creation of this format not yet supported.");
  }

  @Override
  protected void deleteFromStore(final PointSet pointSet) throws IOException {
    throw new UnsupportedOperationException("Deletion of this format not yet supported.");
  }

  @Override
  protected AsciiPointSetMapperModel getInternalModel() {
    return _model;
  }

  @Override
  protected void readFromStore(final PointSet pointSet) throws IOException {
    readFromStore(pointSet, new NullProgressMonitor());
  }

  @Override
  protected void readFromStore(final PointSet pointSet, final IProgressMonitor monitor) throws IOException {
    String filePath = _model.getDirectory() + File.separator + _model.getFileName();
    Domain domain = _model.getZUnit().getDomain();

    monitor.beginTask("Loading Ascii PointSet: " + pointSet.getDisplayName(), 100);
    monitor.worked(1);
    monitor.subTask("Reading file...");

    File file = new File(filePath);
    if (!file.exists() || !file.canRead() || file.isDirectory()) {
      throw new RuntimeException("Cannot read pointset file: " + filePath);
    }

    // TODO: Need to design this such that we don't have to read the file
    // twice.
    int numLines = getNumberOfLines(file, monitor);

    monitor.worked(9);

    BufferedReader reader = createBufferedReader(file);
    loadPoints(pointSet, domain, reader, numLines, monitor);
    reader.close();

    reader = createBufferedReader(file);
    reader.close();

    pointSet.setLastModifiedDate(new Timestamp(file.lastModified()));
    pointSet.setDirty(false);
  }

  @Override
  protected void updateInStore(final PointSet pointSet) throws IOException {
    throw new UnsupportedOperationException("Update of this format not yet supported.");
  }

  @Override
  public AsciiPointSetMapperModel getModel() {
    return new AsciiPointSetMapperModel(_model);
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

  protected void loadPoints(final PointSet pointSet, final Domain domain, final BufferedReader reader,
      final int numLines, final IProgressMonitor monitor) throws IOException {

    AsciiPointSetMapperModel mapper = _model;
    if (mapper == null) {
      throw new RuntimeException("The task for loading the Ascii file has not been initialized.");
    }

    List<Point3d> points = new ArrayList<Point3d>();

    int startingLineNum = mapper.getStartingLineNum();
    int xColNumber = mapper.getXcolumnNum();
    int yColNumber = mapper.getYcolumnNum();
    int zColNumber = mapper.getZcolumnNum();

    int prevWorked = 0;
    for (int i = 0; i < numLines; i++) {
      int currentLineNum = i + 1;
      String line = reader.readLine();
      if (line != null) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        if (currentLineNum >= startingLineNum && !line.startsWith("#")) {
          int currentColNum = 1;
          int numTokensFound = 0;
          double xVal = 0;
          double yVal = 0;
          double zVal = 0;
          while (tokenizer.hasMoreTokens()) {
            boolean tokenFound = false;
            if (currentColNum == xColNumber) {
              xVal = Double.parseDouble(tokenizer.nextToken());
              tokenFound = true;
              numTokensFound++;
            }
            if (currentColNum == yColNumber && !tokenFound) {
              yVal = Double.parseDouble(tokenizer.nextToken());
              tokenFound = true;
              numTokensFound++;
            }
            if (currentColNum == zColNumber && !tokenFound) {
              zVal = Double.parseDouble(tokenizer.nextToken());
              tokenFound = true;
              numTokensFound++;
            }

            // Skip column if token not found
            if (!tokenFound) {
              tokenizer.nextToken();
            }
            currentColNum++;
          }
          if (numTokensFound == 3) {
            Point3d pt = new Point3d(xVal, yVal, zVal);
            pointSet.addPoint(pt);
          } else {
            ServiceProvider.getLoggingService().getLogger(getClass())
                .error("Could not parse record #" + currentLineNum);
            System.out.println("Could not parse record #" + currentLineNum);
          }
        }
      } else {
        ServiceProvider.getLoggingService().getLogger(getClass()).error("Could not parse record #" + currentLineNum);
        System.out.println("Could not parse record #" + currentLineNum);
      }

      if (i % 1000 == 0) {
        int currentWorked = 45 * i / numLines;
        int worked = currentWorked - prevWorked;
        prevWorked = currentWorked;
        monitor.worked(worked);
        monitor.subTask("Reading points...");
      }
    }
    pointSet.setDirty(false);
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "ASCII File PointSet";
  }

  public String getDatastore() {
    return "ASCII File";
  }

}
