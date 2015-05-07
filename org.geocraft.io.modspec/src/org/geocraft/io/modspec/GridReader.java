/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.modspec;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;
import org.geocraft.internal.io.modspec.ServiceComponent;


/**
 * The class for reading data from ModSpec grid files.
 * Both the ASCII and binary file formats are currently supported.
 */
public class GridReader implements ModSpecGridConstants {

  private enum ReadType {
    HEADER,
    DATA
  }

  /** The unit preferences. */
  private static final UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  /** The model of mapper parameters. */
  private final GridMapperModel _model;

  /**
   * The default constructor.
   * @param model the model of mapper parameters used to read a ModSpec grid file.
   */
  public GridReader(final GridMapperModel model) {
    _model = model;
  }

  /**
   * Reads a ModSpec grid file into the specified grid.
   * @param grid the grid to read.
   * @param filePath the ModSpec grid file path.
   * @throws IOException thrown on read error.
   */
  public void read(final Grid3d grid, final String filePath, final IProgressMonitor monitor) throws IOException {
    ILogger logger = ServiceProvider.getLoggingService().getLogger(getClass());

    try {
      // Read the header and data.
      GridReaderTask dataTask = new GridReaderTask(grid, filePath);
      dataTask.read(logger, monitor);
    } catch (CoreException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Reads the ASCII format of a ModSpec grid.
   * 
   * Amazingly the points seem to be ordered in a row major format with the origin
   * in the upper left. 
   * 
   * |
   * | |
   * | |
   * | |
   * | v
   * 0-------------
   * 
   * @param grid the grid.
   * @param filePath the grid file path.
   * @throws IOException thrown on read error.
   */
  public void readAsciiFormat(final BufferedReader reader, final Grid3d grid, final String filePath,
      final IProgressMonitor monitor) throws IOException {
    // We cannot use the StreamTokenizer because in 1.5 Java does not support
    // exponential numbers which can occur in the header (body also?).

    // Read the first line, which contains the x,y origin.
    StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
    double x0 = Double.parseDouble(tokenizer.nextToken());
    double y0 = Double.parseDouble(tokenizer.nextToken());

    // Read the second line, which contains the delta x,y values.
    tokenizer = new StringTokenizer(reader.readLine());
    double dx = Double.parseDouble(tokenizer.nextToken());
    double dy = Double.parseDouble(tokenizer.nextToken());

    // Read the third line, which contains the number of rows and columns.
    tokenizer = new StringTokenizer(reader.readLine());
    int nx = Integer.parseInt(tokenizer.nextToken());
    int ny = Integer.parseInt(tokenizer.nextToken());

    // Read the fourth line, which contains the rotation angle and null values.
    tokenizer = new StringTokenizer(reader.readLine());
    double angle = Double.parseDouble(tokenizer.nextToken()); // angle
    float znon = Float.parseFloat(tokenizer.nextToken());

    // If the application x,y units are undefined, then set them
    // to the units of this mapper.
    Unit appUnitXY = UNIT_PREFS.getHorizontalDistanceUnit();
    if (appUnitXY.equals(Unit.UNDEFINED)) {
      UNIT_PREFS.setHorizontalDistanceUnit(_model.getXyUnit());
      appUnitXY = _model.getXyUnit();
    }

    // Convert the x,y origin and the delta x,y values.
    x0 = Unit.convert(x0, _model.getXyUnit(), appUnitXY);
    y0 = Unit.convert(y0, _model.getXyUnit(), appUnitXY);
    dx = Unit.convert(dx, _model.getXyUnit(), appUnitXY);
    dy = Unit.convert(dy, _model.getXyUnit(), appUnitXY);

    // Note: this is not like the row ordered fortran style array!
    // Note: ny = number of rows, nx = number of columns.
    double[][] data = new double[ny][nx];
    // This looks inefficient.
    // There must be a better way to cram the data into the array.
    // The StreamTokenizer looks cleaner but there might be exponential numbers
    // in the data so we can't use it.
    // Dan: maybe consider using java.util.Scanner instead of StringTokenizer
    // Compute the number of lines in the file.
    // (e.g. 265 / 8.0f points per row = 33.125 = 34 records of 8 data points).
    // This presumes the old modspec format that had "8F10.2" per line.
    int ny2 = (int) Math.ceil(ny / 8.0f);
    int numLines = nx * ny2;
    monitor.beginTask("Loading " + grid.getDisplayName(), nx + ny);

    int x = 0;
    int y = ny - 1;
    // Tried to optimize this code by splitting records manually and not using
    // a tokenizer but it made no difference.
    String dataPoint = "";
    for (int record = 0; record < numLines && !monitor.isCanceled(); record++) {
      if (!reader.ready()) {
        // added this code to allow one to read fewer lines with more tokens per line than 8.
        break;
      }
      tokenizer = new StringTokenizer(reader.readLine());
      int numTokens = tokenizer.countTokens(); // by using num_tokens we can read more than 8 tokens per line.
      for (int j = 0; j < numTokens; j++) {
        dataPoint = tokenizer.nextToken();
        data[y][x] = Double.parseDouble(dataPoint);
        // Increment the column number.
        y--;
        // If we are at the top of this column go to next column.
        if (y == -1) {
          y = ny - 1;
          x++;
          break;
        }
      }
      if (record % ny2 == 0) {
        // avoid so many calls on this
        monitor.worked(1);
      }
    }

    // Update the grid geometry.
    updateGridGeometry(grid, x0, y0, dx, dy, nx, ny, angle);

    // Update the grid.
    updateGrid(grid, filePath, nx, ny, znon, data, monitor);
  }

  /**
   * Reads the binary format of a ModSpec grid. The binary format is laid out like this... 
   * 
   * | 
   * | 
   * | 
   * |--------> 
   * 0----------
   * @param grid the grid.
   * @param filePath the grid file path.
   * @throws IOException thrown on read error.
   */
  public void readBinaryFormat(final BufferedReader reader, final Grid3d grid, final String filePath,
      final IProgressMonitor monitor) throws IOException {
    String record = reader.readLine().trim();

    // See if this could be a binary ModSpec grid.
    if (!record.equals("#<CPS_v1 TYPE=MODSPEC_GRID/>")) {
      return;
    }

    // Read and load the parameters from the main header.
    Map<String, String> parameters = new HashMap<String, String>();
    record = reader.readLine().trim();
    while (!record.equals("#</MODSPEC_GRID>")) {
      if (record.indexOf("=") > -1) {
        StringTokenizer tokenizer = new StringTokenizer(record, "#=");
        String name = tokenizer.nextToken().trim();
        String value = tokenizer.nextToken().trim();
        parameters.put(name, value);
      }
      record = reader.readLine().trim();
    }

    // Get the number of rows and columns.
    int nx = Integer.parseInt(parameters.get("NX"));
    int ny = Integer.parseInt(parameters.get("NY"));
    monitor.beginTask("Loading", ny * 2 + 1);

    // Get the delta x,y values.
    double dx = Double.parseDouble(parameters.get("DX"));
    double dy = Double.parseDouble(parameters.get("DY"));

    // Get the x,y origin.
    double x0 = Double.parseDouble(parameters.get("XORG"));
    double y0 = Double.parseDouble(parameters.get("YORG"));

    // Get the rotation angle and null values.
    double angle = Double.parseDouble(parameters.get("ANGLE"));
    float znon = Float.parseFloat(parameters.get("ZNON"));
    String address = parameters.get("ADDRESS");
    parameters.get("ATTRIBUTE");

    // Read the 'headerStartAddress' 
    Long.parseLong(address.substring(address.indexOf('(') + 1, address.indexOf(',')));
    // Read the 'headerEndAddress'
    long headerEndAddress = Long.parseLong(address.substring(address.indexOf(',') + 2, address.indexOf(')')));

    // If the application x,y units are undefined, then set them
    // to the units of this mapper.
    Unit appUnitXY = UNIT_PREFS.getHorizontalDistanceUnit();
    if (appUnitXY.equals(Unit.UNDEFINED)) {
      UNIT_PREFS.setHorizontalDistanceUnit(_model.getXyUnit());
      appUnitXY = _model.getXyUnit();
    }

    // Convert the x,y origin and the delta x,y values.
    x0 = Unit.convert(x0, _model.getXyUnit(), appUnitXY);
    y0 = Unit.convert(y0, _model.getXyUnit(), appUnitXY);
    dx = Unit.convert(dx, _model.getXyUnit(), appUnitXY);
    dy = Unit.convert(dy, _model.getXyUnit(), appUnitXY);

    // Now use a different approach to access the numeric data.
    RandomAccessFile raf = new RandomAccessFile(filePath, "r");
    FileChannel fc = raf.getChannel();

    // Read the data section address header.
    MappedByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, headerEndAddress, WORDS_IN_DATA_HEADER * LENGTH_OF_INT
        + nx * ny * LENGTH_OF_FLOAT);
    buf.order(ByteOrder.LITTLE_ENDIAN);

    // buf.order (ByteOrder.BIG_ENDIAN); // TODO: Test if this is needed.
    // TODO: None of these 7 variables below are used??
    buf.getInt(); // 'maxpicks'
    buf.getInt(); // 'startAddressAll'
    buf.getInt(); // 'endAddressAll'
    buf.getInt(); // 'startAddressFirstHeader'
    buf.getInt(); // 'startAddressFirstData'
    buf.getInt(); // 'numPicksFirst'

    double[][] data = new double[ny][nx];
    monitor.worked(1);
    // Read the grid data.
    for (int j = 0; j < ny && !monitor.isCanceled(); j++) {
      for (int i = 0; i < nx; i++) {
        data[j][i] = buf.getFloat();
      }
      monitor.worked(1);
    }

    // Update the grid geometry.
    updateGridGeometry(grid, x0, y0, dx, dy, nx, ny, angle);

    // Update the grid.
    updateGrid(grid, filePath, nx, ny, znon, data, monitor);
  }

  /**
   * Creates a buffered reader for the ModSpec grid specified by the file path.
   * @param filePath the file path of the ModSpec grid file.
   * @return the buffered reader.
   * @throws FileNotFoundException
   */
  BufferedReader createBufferedReader(final String filePath) throws FileNotFoundException {
    return new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
  }

  /**
   * Updates the grid with the information read from the ModSpec grid file.
   * @param grid the grid to update.
   * @param filePath the ModSpec grid file path.
   * @param nx the x-size of the ModSpec grid.
   * @param ny the y-size of the ModSpec grid.
   * @param znon the null value of the ModSpec grid.
   * @param data the data array of the ModSpec grid.
   */
  private void updateGrid(final Grid3d grid, final String filePath, final int nx, final int ny, final float znon,
      final double[][] data, final IProgressMonitor monitor) {
    boolean isZProp = false;
    Domain domain = Domain.TIME;

    // Check the specified data unit.
    if (_model.getDataUnit() == null) {
      throw new RuntimeException("Invalid ModSpec grid data unit: " + _model.getDataUnit() + ".");
    }

    // If data unit is of domain time or depth, flag it as z-property.
    domain = _model.getDataUnit().getDomain();
    if (domain == Domain.TIME) {
      isZProp = true;
    } else if (domain == Domain.DISTANCE) {
      isZProp = true;
    }

    // Determine the application data unit.
    Unit appDataUnits = Unit.UNDEFINED;
    if (domain == Domain.TIME) { // If the domain is time, then get the application time units.
      // If the application time units are undefined, set them based on this mapper.
      if (UNIT_PREFS.getTimeUnit() == Unit.UNDEFINED) {
        UNIT_PREFS.setTimeUnit(_model.getDataUnit());
      }
      appDataUnits = UNIT_PREFS.getTimeUnit();
    } else if (domain == Domain.DISTANCE) { // If the domain is depth, then get the application depth units.
      // If the application depth units are undefined, set them based on this mapper.
      if (UNIT_PREFS.getVerticalDistanceUnit() == Unit.UNDEFINED) {
        UNIT_PREFS.setVerticalDistanceUnit(_model.getDataUnit());
      }
      appDataUnits = UNIT_PREFS.getVerticalDistanceUnit();
    }

    // If not a z property (time or depth), so use the storage data unit.
    if (!isZProp) {
      appDataUnits = _model.getDataUnit();
    }

    // Set the onset type.
    grid.setOnsetType(_model.getOnsetType());

    int numRows = grid.getNumRows();
    int numCols = grid.getNumColumns();
    float[][] values = new float[numRows][numCols];

    // Convert the values from the storage data unit to the application data unit.
    int index = 0;
    int didx = 1;
    int didy = nx;
    //      if (_model.getOrientation() == GridOrientation.Y_IS_COLUMN) {
    //        didx = ny;
    //        didy = 1;
    //        values = new float[nx][ny];
    //      }
    for (int j = 0; j < ny && !monitor.isCanceled(); j++) {
      index = j * didy;
      for (int i = 0; i < nx; i++) {
        int row = index / numCols;
        int col = index % numCols;
        if (MathUtil.isEqual(data[j][i], znon)) {
          values[row][col] = znon;
          index += didx;
        } else {
          try {
            values[row][col] = (float) Unit.convert(data[j][i], _model.getDataUnit(), appDataUnits);
            index += didx;
          } catch (Exception e) {
            values[row][col] = znon;
            index += didx;
          }
        }
      }
      monitor.worked(1);
    }

    // Update the grid with the actual data.
    grid.setValues(values, znon, appDataUnits);

    File f = new File(filePath);
    Timestamp lastModifiedDate = new Timestamp(f.lastModified());
    grid.setLastModifiedDate(lastModifiedDate);
    grid.setDirty(false);
  }

  class GridReaderTask {

    private final Grid3d _grid;

    private final String _filePath;

    GridReaderTask(final Grid3d grid, final String filePath) {
      _grid = grid;
      _filePath = filePath;
    }

    public void read(final ILogger logger, final IProgressMonitor monitor) throws CoreException {
      try {
        // Create a buffered reader.
        BufferedReader reader = createBufferedReader(_filePath);
        try {
          if (_model.getFileFormat() == GridFileFormat.ASCII) {
            // Read the grid in ASCII format.
            readAsciiFormat(reader, _grid, _filePath, monitor);
          } else if (_model.getFileFormat() == GridFileFormat.BINARY) {
            // Read the grid in binary format.
            readBinaryFormat(reader, _grid, _filePath, monitor);
          } else {
            throw new CoreException(new Status(IStatus.ERROR, ServiceComponent.PLUGIN_ID,
                "Invalid ModSpec file format: " + _model.getFileFormat() + "."));
          }
        } catch (NumberFormatException ex) {
          // Throw an I/O exception so that the app can handle it.
          throw new CoreException(new Status(IStatus.WARNING, ServiceComponent.PLUGIN_ID,
              "Unable to parse number in ModSpec grid.", ex));
        } finally {
          // Close the buffered reader, if it was opened.
          if (reader != null) {
            reader.close();
          }
        }
      } catch (IOException ex) {
        throw new CoreException(new Status(IStatus.WARNING, ServiceComponent.PLUGIN_ID, "Unable to load ModSpec grid",
            ex));
      }
    }
  }

  private void updateGridGeometry(final Grid3d grid, final double xOrigin, final double yOrigin, final double dx,
      final double dy, final int nx, final int ny, final double angle) throws IOException {
    Unit appUnitsXY = UNIT_PREFS.getHorizontalDistanceUnit();
    if (appUnitsXY == Unit.UNDEFINED) {
      UNIT_PREFS.setHorizontalDistanceUnit(_model.getXyUnit());
      appUnitsXY = _model.getXyUnit();
    }
    //boolean switchOrientation = false;
    int numRows = ny;
    int numCols = nx;
    double colSpacing = dx;
    double rowSpacing = dy;
    double x0 = xOrigin;
    double y0 = yOrigin;

    //switch (_model.getOrientation()) {
    //  case X_IS_COLUMN:
    //        numRows = ny;
    //        numCols = nx;
    //        colSpacing = dx;
    //        rowSpacing = dy;
    //        break;
    //      case Y_IS_COLUMN:
    //        numRows = nx;
    //        numCols = ny;
    //        colSpacing = dy;
    //        rowSpacing = dx;
    //        switchOrientation = true;
    //        break;
    //    }

    Point3d[] points = new Point3d[4];

    // Define normal direction as counter-clockwise.
    double costerm1 = Math.cos(Math.toRadians(angle));
    double sinterm1 = Math.sin(Math.toRadians(angle));
    double costerm2 = Math.cos(Math.toRadians(angle + 90));
    double sinterm2 = Math.sin(Math.toRadians(angle + 90));
    //    if (switchOrientation) {
    //      // If switched, direction is clockwise.
    //      costerm2 = Math.cos(Math.toRadians(angle));
    //      sinterm2 = Math.sin(Math.toRadians(angle));
    //      costerm1 = Math.cos(Math.toRadians(angle + 90));
    //      sinterm1 = Math.sin(Math.toRadians(angle + 90));
    //    }
    int[] rows = { 0, 0, numRows - 1, numRows - 1 };
    int[] cols = { 0, numCols - 1, numCols - 1, 0 };
    for (int i = 0; i < 4; i++) {
      double x = x0 + cols[i] * colSpacing * costerm1 + rows[i] * rowSpacing * costerm2;
      double y = y0 + cols[i] * colSpacing * sinterm1 + rows[i] * rowSpacing * sinterm2;
      points[i] = new Point3d(x, y, 0);
    }
    CoordinateSystem coordSys = null;
    Domain domain = _model.getDataUnit().getDomain();
    Domain zDomain = Domain.TIME;
    // Check the specified store data units.
    if (_model.getDataUnit() == Unit.UNDEFINED) {
      _model.setDataUnit(Unit.MILLISECONDS);
      zDomain = Domain.TIME;
    } else if (domain == Domain.TIME || domain == Domain.DISTANCE) {
      zDomain = domain;
    }
    if (zDomain == Domain.TIME) {
      coordSys = ApplicationPreferences.getInstance().getTimeCoordinateSystem();
    } else if (zDomain == Domain.DISTANCE) {
      coordSys = ApplicationPreferences.getInstance().getDepthCoordinateSystem();
    } else {
      throw new IOException("Invalid domain: " + domain + ".");
    }
    CornerPointsSeries cornerPoints = CornerPointsSeries.create(points, coordSys);

    // Update the grid.
    grid.setGeometry(new GridGeometry3d("Grid " + numRows + "x" + numCols, numRows, numCols, cornerPoints));
    grid.setZDomain(zDomain);
    grid.setDatumElevation(0);
  }
}
