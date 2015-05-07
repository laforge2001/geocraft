/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciigrid;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.StringTokenizer;

import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.BufferedReaderFactory;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.service.ServiceProvider;


/**
 * The class for reading data from Ascii grid files.
 */
public abstract class AbstractAsciiHorizonReader implements AsciiFileConstants {

  // Number of lines in file
  int _numOfLines = 0;

  /** The mapper model. */
  protected AsciiHorizonMapperModel _model;

  /**
   * The default constructor.
   * @param model the model of mapper parameters used to read a ModSpec grid file.
   */
  public AbstractAsciiHorizonReader(final AsciiHorizonMapperModel model) {
    _model = model;
  }

  public int getNumOfLines() {
    return _numOfLines;
  }

  public float[][] readAsciiHorizon(final String filePath, final int startingLineNum, final int numOfHorizons,
      final int[] columnNums) throws IOException {

    // Deterimine the # of lines in the file
    int numOfLines = getNumOfLines(filePath, 0);

    // Create the results array based on the # of lines of file
    float[][] results = new float[numOfHorizons + 2][numOfLines];

    // Create a buffered reader.
    BufferedReader reader = BufferedReaderFactory.getBufferedReader(filePath);

    int currentLineNum = 1;
    int dataIndex = 0;
    int xColNum = columnNums[0];
    int yColNum = columnNums[1];

    boolean endFlag = false;
    String readLine = "";
    while (!endFlag) {
      // Read the next line
      try {
        // Create a buffered reader.
        readLine = reader.readLine();
      } catch (IOException ex) {
        throw new IOException("Unable to read the Ascii file!  " + ex);
      }

      if (readLine != null) {
        StringTokenizer tokenizer = new StringTokenizer(readLine);
        if (currentLineNum >= startingLineNum && !readLine.startsWith("#")) {
          int currentColNum = 1;
          while (tokenizer.hasMoreTokens()) {
            boolean tokenFound = false;
            if (currentColNum == xColNum) {
              float xVal = Float.parseFloat(tokenizer.nextToken());
              results[0][dataIndex] = xVal;
              tokenFound = true;
            }
            if (currentColNum == yColNum && !tokenFound) {
              float yVal = Float.parseFloat(tokenizer.nextToken());
              results[1][dataIndex] = yVal;
              tokenFound = true;
            }

            for (int i1 = 0; i1 < numOfHorizons && !tokenFound; i1++) {
              int colIndex = i1 + 2;
              int horColNum = columnNums[colIndex];
              if (currentColNum == horColNum) {
                float horVal = Float.parseFloat(tokenizer.nextToken());
                results[i1 + 2][dataIndex] = horVal;
                tokenFound = true;
              }
              if (tokenFound) {
                break;
              }
            }
            // Skip column if token not found
            if (!tokenFound) {
              tokenizer.nextToken();
            }
            currentColNum++;
          }
          dataIndex++;
        }
        currentLineNum++;
      } else {
        endFlag = true;
      }
    }

    // Close the input file.
    try {
      reader.close();
    } catch (IOException ex) {
      throw new IOException("Unable to close the Ascii file!  " + ex);
    }

    // return the results
    return results;
  }

  public int getNumOfLines(final String filePath, int maxLine) throws IOException {

    // Create a buffered reader.
    BufferedReader reader = BufferedReaderFactory.getBufferedReader(filePath);

    int numOfLines = 0;
    boolean endFlag = false;
    String readLine = "";
    while (!endFlag) {
      // Read the next line
      try {
        readLine = reader.readLine();
      } catch (IOException ex) {
        throw new IOException("Unable to read the Ascii file!  " + ex);
      }

      if (readLine != null) {
        // Stop reading if maximum line has been reached
        if (maxLine > 0 && numOfLines >= maxLine) {
          endFlag = true;
          // Don't count the lines with comments          
        } else if (!readLine.startsWith("#")) {
          numOfLines++;
        }
        // Assume we are at the end of the file
      } else {
        endFlag = true;
        _numOfLines = numOfLines;
      }
    }

    // Close the input file.
    try {
      reader.close();
    } catch (IOException ex) {
      throw new IOException("Unable to close the Ascii file! " + ex);
    }

    return numOfLines;
  }

  public abstract void setUnitPreferences();

  public abstract CoordinateSystem getCoordinateSystem(Domain zDomain);

  public GridGeometry3d updateGridGeometry(final Grid3d grid, final double xOrigin, final double yOrigin,
      final double dx, final double dy, final int nx, final int ny, final double angle, final float nullValue) throws IOException {
    setUnitPreferences();
    boolean switchOrientation = false;
    int numRows = 0;
    int numCols = 0;
    double colSpacing = 0;
    double rowSpacing = 0;
    double x0 = xOrigin;
    double y0 = yOrigin;

    if (_model.getOrientation() == GridOrientation.X_IS_COLUMN) {
      numRows = ny;
      numCols = nx;
      colSpacing = dx;
      rowSpacing = dy;
    } else if (_model.getOrientation() == GridOrientation.Y_IS_COLUMN) {
      numRows = nx;
      numCols = ny;
      colSpacing = dy;
      rowSpacing = dx;
      switchOrientation = true;
    }

    // Update the model file
    _model.setXorigin(xOrigin);
    _model.setYorigin(yOrigin);
    _model.setColSpacing(colSpacing);
    _model.setRowSpacing(rowSpacing);
    _model.setNumOfRows(numRows);
    _model.setNumOfColumns(numCols);
    _model.setPrimaryAngle(angle);
    _model.setNullValue(nullValue);

    // determine corner points of grid
    Point3d[] points = new Point3d[4];
    double costerm1 = Math.cos(Math.toRadians(angle));
    double sinterm1 = Math.sin(Math.toRadians(angle));
    double costerm2 = Math.cos(Math.toRadians(angle + 90));
    double sinterm2 = Math.sin(Math.toRadians(angle + 90));
    if (switchOrientation) {
      costerm2 = Math.cos(Math.toRadians(angle));
      sinterm2 = Math.sin(Math.toRadians(angle));
      costerm1 = Math.cos(Math.toRadians(angle + 90));
      sinterm1 = Math.sin(Math.toRadians(angle + 90));
    }
    int[] rows = { 0, 0, numRows - 1, numRows - 1 };
    int[] cols = { 0, numCols - 1, numCols - 1, 0 };
    for (int i = 0; i < 4; i++) {
      double x = x0 + cols[i] * colSpacing * costerm1 + rows[i] * rowSpacing * costerm2;
      double y = y0 + cols[i] * colSpacing * sinterm1 + rows[i] * rowSpacing * sinterm2;
      points[i] = new Point3d(x, y, 0);
    }

    Domain zDomain = Domain.TIME;
    // Check the specified store data units.
    if (_model.getDataUnits() == Unit.UNDEFINED) {
      _model.setDataUnits(Unit.MILLISECONDS);
      zDomain = Domain.TIME;
    } else if (_model.getDataUnits().getDomain() == Domain.TIME) {
      zDomain = Domain.TIME;
    } else if (_model.getDataUnits().getDomain() == Domain.DISTANCE) {
      zDomain = Domain.DISTANCE;
    }
    CoordinateSystem coordSys = getCoordinateSystem(zDomain);
    CornerPointsSeries cornerPoints = CornerPointsSeries.createDirect(points, coordSys);

    // Update the grid.
    GridGeometry3d geometry = new GridGeometry3d("Grid " + numRows + "x" + numCols, numRows, numCols, cornerPoints);
    grid.setGeometry(geometry);
    grid.setZDomain(zDomain);
    grid.setDatumElevation(0);
    return geometry;
  }

  public abstract Unit getDataUnits(Domain domain);

  /**
   * Updates the grid with the information read from the ModSpec grid file.
   * @param grid the grid to update.
   * @param filePath the ModSpec grid file path.
   * @param nx the x-size of the ModSpec grid.
   * @param ny the y-size of the ModSpec grid.
   * @param znon the null value of the ModSpec grid.
   * @param data the data array of the ModSpec grid.
   */
  public void updateGrid(final Grid3d grid, final String filePath, final int nx, final int ny, final double nullValue,
      final double[][] data) {
    boolean isZProp = false;
    Domain domain = Domain.TIME;
    float znon = (float) nullValue;

    // Check the specified data unit.
    if (_model.getDataUnits() == null) {
      throw new RuntimeException("Invalid ModSpec grid data unit: " + _model.getDataUnits() + ".");
    }

    // If data unit is of domain time or depth, flag it as z-property.
    domain = _model.getDataUnits().getDomain();
    if (domain == Domain.TIME) {
      isZProp = true;
    } else if (domain == Domain.DISTANCE) {
      isZProp = true;
    }

    Unit appDataUnits = getDataUnits(domain);

    // If not a z property (time or depth), so use the storage data unit.
    if (!isZProp) {
      appDataUnits = _model.getDataUnits();
    }

    // Set the onset type.
    grid.setOnsetType(_model.getOnsetType());

    int numRows = ny;
    int numCols = nx;
    float[][] values = new float[numRows][numCols];

    // Convert the values from the storage data unit to the application data unit.
    int index = 0;
    int didx = 1;
    int didy = nx;
    if (_model.getOrientation() == GridOrientation.Y_IS_COLUMN) {
      didx = ny;
      didy = 1;
      values = new float[nx][ny];
    }
    for (int j = 0; j < ny; j++) {
      index = j * didy;
      for (int i = 0; i < nx; i++) {
        int row = index / numCols;
        int col = index % numCols;
        if (MathUtil.isEqual(data[j][i], znon)) {
          values[row][col] = znon;
          index += didx;
        } else {
          try {
            values[row][col] = (float) Unit.convert(data[j][i], _model.getDataUnits(), appDataUnits);
            index += didx;
          } catch (Exception e) {
            values[row][col] = znon;
            index += didx;
          }
        }
      }
    }

    // Update the grid with the actual data.
    grid.setValues(values, znon, appDataUnits);

    File f = new File(filePath);
    Timestamp lastModifiedDate = new Timestamp(f.lastModified());
    grid.setLastModifiedDate(lastModifiedDate);
    grid.setDirty(false);
  }

  public abstract void setDisplayColor(Grid3d grid);

  public void read(final Grid3d grid, final String filePath) throws IOException {

    int numOfHorizons = _model.getNumOfHorizons();
    int[] colNumbers = new int[numOfHorizons + 2];
    colNumbers[0] = _model.getXcolumnNum();
    colNumbers[1] = _model.getYcolumnNum();
    for (int i1 = 0; i1 < numOfHorizons; i1++) {
      int horNum = i1 + 1;
      int horCol = _model.getH1ColumnNum();
      if (horNum == 2) {
        horCol = _model.getH2ColumnNum();
      } else if (horNum == 3) {
        horCol = _model.getH3ColumnNum();
      } else if (horNum == 4) {
        horCol = _model.getH4ColumnNum();
      } else if (horNum == 5) {
        horCol = _model.getH5ColumnNum();
      }
      colNumbers[i1 + 2] = horCol;
    }

    // read the file
    int startingLineNum = _model.getStartingLineNum();

    // Read the file
    float[][] results = readAsciiHorizon(filePath, startingLineNum, numOfHorizons, colNumbers);

    // Determine the number of lines in file
    int numOfLines = getNumOfLines();

    setDisplayColor(grid);

    // Update the grid geometry.
    double x0 = _model.getXorigin();
    double y0 = _model.getYorigin();
    double dx = _model.getColSpacing();
    double dy = _model.getRowSpacing();
    int nX = _model.getNumOfColumns();
    int nY = _model.getNumOfRows();
    double angle = _model.getPrimaryAngle();
    float nullValue = _model.getNullValue();
    try {
      updateGridGeometry(grid, x0, y0, dx, dy, nX, nY, angle, nullValue);
    } catch (IOException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.getMessage(), ex);
    }

    double[][] gridVals = new double[nY][nX];
    // initialize grid Values to null
    for (int i1 = 0; i1 < nX; i1++) {
      for (int i2 = 0; i2 < nY; i2++) {
        gridVals[i1][i2] = nullValue;
      }
    }

    // Save each value read from the file
    for (int i4 = 0; i4 < numOfLines; i4++) {

      // Transform x & y value to a row & column
      float xVal = results[0][i4];
      float yVal = results[1][i4];
      double[] rowcol = grid.getGeometry().transformXYToRowCol(xVal, yVal, true);
      int row = (int) Math.round(rowcol[0]);
      int col = (int) Math.round(rowcol[1]);

      // save value into the row & column
      if (row >= 0 && row < nY && col >= 0 && col < nX) {
        gridVals[row][col] = results[2][i4];
      }
    }

    // Update the grid.
    String gridPath = _model.getDirectory() + File.separator + _model.getFileName();
    float znon = nullValue;
    updateGrid(grid, gridPath, nX, nY, znon, gridVals);
  }

}
