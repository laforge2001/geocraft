/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.modspec;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Formatter;

import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.ArrayUtil;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;


/**
 * The class for writing data to ModSpec grid files.
 * Both the ASCII and binary file formats are currently supported.
 */
public class GridWriter implements ModSpecGridConstants {

  /** The model of mapper parameters. */
  private final GridMapperModel _model;

  /**
   * The default constructor.
   * @param model the model of mapper parameters used to write a ModSpec grid file.
   */
  public GridWriter(final GridMapperModel model) {
    _model = model;
  }

  /**
   * Writes the specified grid property to a ModSpec grid file.
   * @param grid the grid property to write.
   * @param filePath the ModSpec grid file path.
   * @throws IOException thrown on write error.
   */
  public void write(final Grid3d grid, final String filePath) throws IOException {
    // Check which format to write, ASCII or binary.
    if (_model.getFileFormat() == GridFileFormat.BINARY) {
      // Write the grid property in Binary format.
      writeBinaryFormat(grid, filePath);
    } else if (_model.getFileFormat() == GridFileFormat.ASCII) {
      // Write the grid property in ASCII format.
      writeAsciiFormat(grid, filePath);
    } else {
      throw new IOException("Invalid ModSpec file format: " + _model.getFileFormat() + ".");
    }
  }

  /**
   * Writes the ASCII format of a ModSpec grid.
   * @param grid the grid property to write.
   * @param filePath the ModSpec grid file path.
   * @exception IOException thrown on write error.
   */
  public void writeAsciiFormat(final Grid3d grid, final String filePath) throws IOException {
    // Build formatting string for each of the eight fields, each 10 characters wide.
    double maxabsval = ArrayUtil.getMaxCharValue(grid.getValues());
    String fmtstr = " " + MathUtil.computeFormat(maxabsval, 9); // TODO make it read 10!
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath))));
    try {
      boolean switchRowCol = false;
      //      if (_model.getOrientation() == GridOrientation.Y_IS_COLUMN) {
      //        switchRowCol = true;
      //      }
      GridGeometry3d geometry = grid.getGeometry();
      Point3d[] points = geometry.getCornerPoints().getCopyOfPoints();
      for (int i = 0; i < points.length; i++) {
        double x = points[i].getX();
        double y = points[i].getY();
        double z = points[i].getZ();
        x = Unit.convert(x, UnitPreferences.getInstance().getHorizontalDistanceUnit(), _model.getXyUnit());
        y = Unit.convert(y, UnitPreferences.getInstance().getHorizontalDistanceUnit(), _model.getXyUnit());
        points[i] = new Point3d(x, y, z);
      }
      double angle = Math.atan2(points[1].getY() - points[0].getY(), points[1].getX() - points[0].getX());
      double dx01 = points[1].getX() - points[0].getX();
      double dy01 = points[1].getY() - points[0].getY();
      dx01 /= geometry.getNumColumns() - 1;
      dy01 /= geometry.getNumColumns() - 1;
      double colSpacing = Math.sqrt(dx01 * dx01 + dy01 * dy01);
      double dx03 = points[3].getX() - points[0].getX();
      double dy03 = points[3].getY() - points[0].getY();
      dx03 /= geometry.getNumRows() - 1;
      dy03 /= geometry.getNumRows() - 1;
      double rowSpacing = Math.sqrt(dx03 * dx03 + dy03 * dy03);

      // Write the ASCII header.
      if (!switchRowCol) {
        String tmpstr = points[0].getX()
            + " "
            + points[0].getY()
            + " [x0 and y0 origins of the grid. This file will have no more than 8 items per record, so Columns will span records!]\n";
        writer.write(tmpstr);
        tmpstr = colSpacing
            + " "
            + rowSpacing
            + " [dx (column) and dy (row) spacing for grid. -- File is organized so that the first Column in your grid is the first Row in your file...]\n";
        writer.write(tmpstr);
        tmpstr = geometry.getNumColumns() + " " + geometry.getNumRows()
            + " [number of columns, number of rows (in the File, number rows = how many numbers per line.]\n";
        writer.write(tmpstr);
        tmpstr = Math.toDegrees(angle) + " " + grid.getNullValue()
            + " [Rotation angle (from +x axis, cartesian system) and value to be treated as NULL]\n";
        writer.write(tmpstr);
      } else {
        String tmpstr = points[0].getX()
            + " "
            + points[0].getY()
            + " [(rows, columns switched) x0 and y0 origins of the grid. (This format will have no more than 8 items per record, so a row will span records!]\n";
        writer.write(tmpstr);
        tmpstr = rowSpacing
            + " "
            + colSpacing
            + " [dy (row) and dx (column) spacing for grid. -- File is organized so that the first Row in your grid is the first Row in the file.]\n";
        writer.write(tmpstr);
        tmpstr = geometry.getNumRows() + " " + geometry.getNumColumns() + " [number of rows, number of columns]\n";
        writer.write(tmpstr);
        tmpstr = Math.toDegrees(angle)
            - 90
            + " "
            + grid.getNullValue()
            + " Rotation angle (includes switch of rows, columns from default which is that the file has each Row being a Column in your grid!) and value to be treated as NULL]\n";
        writer.write(tmpstr);
      }

      StringBuilder sb = new StringBuilder(81);
      Formatter formatter = new Formatter(sb);
      int count;
      int cell = 0;
      float value = 0;
      Unit dataUnit = grid.getDataUnit();
      Unit dataUnitStorage = _model.getDataUnit();

      // If the domain of the grid data unit does not match the domain of the specified storage data unit, then
      // simply set the data unit to be the same as the storage data unit in order to avoid any conversion errors
      // due to domain differences.
      if (!dataUnit.getDomain().equals(dataUnitStorage.getDomain())) {
        dataUnit = dataUnitStorage;
      }

      float[] values = new float[geometry.getNumRows() * geometry.getNumColumns()];
      for (int row = 0; row < geometry.getNumRows(); row++) {
        for (int col = 0; col < geometry.getNumColumns(); col++) {
          value = grid.getValueAtRowCol(row, col);
          values[cell] = value;
          if (!grid.isNull(value)) {
            values[cell] = Unit.convert(value, dataUnit, dataUnitStorage);
          }
          cell++;
        }
      }

      // Write out grid values, column index varying fastest.
      // Write eight values per line of output.
      int numCols = geometry.getNumColumns();
      int numRows = geometry.getNumRows();
      if (switchRowCol) {
        numCols = geometry.getNumRows();
        numRows = geometry.getNumColumns();
      }
      for (int i = 0; i < numCols; i++) {
        count = 0;
        // Write column values for this row.
        for (int j = numRows - 1; j > -1; j--) {
          // Append next grid value to output string in builder.
          int index = j * numCols + i;
          if (switchRowCol) {
            index = i * numRows + j;
          }
          fmtstr = " " + MathUtil.computeFormat(values[index], 9);
          formatter.format(fmtstr, values[index]);
          count++;
          if (count == 8) {
            // Append a newline, write, and clear string builder.
            formatter.format("%s", "\n"); // TODO make it read 10.
            count = 0;
            writer.write(sb.toString());
            sb.delete(0, 81);
          }
        }
        // When here, writing a row is complete, but a newline is needed if count is not zero.
        if (count > 0) {
          // Append a newline, write, and clear string builder.
          formatter.format("%s", "\n");
          writer.write(sb.toString());
          sb.delete(0, 81);
        }
      }
      writer.flush();
    } finally {
      // Close the writer if it was opened.
      if (writer != null) {
        writer.close();
      }
    }
  }

  /**
   * Writes the binary format of a ModSpec grid.
   * @param grid the grid property to write.
   * @param filePath the ModSpec grid file path.
   * @exception IOException thrown on write error.
   */
  public void writeBinaryFormat(final Grid3d grid, final String filePath) throws IOException {
    // Write the header for for the binary format.
    long dataStartAddr = writeBinaryHeader(grid, filePath);
    // Write the data for the binary format.
    writeBinaryData(grid, filePath, dataStartAddr);
    return;
  }

  /**
   * Writes the header section of the ModSpec grid for the binary format.
   * @param grid the grid property containing the header to write.
   * @param filePath the ModSpec grid file path.
   * @return true if written successfully; false otherwise.
   * @throws IOException thrown on write error.
   */
  public long writeBinaryHeader(final Grid3d grid, final String filePath) throws IOException {
    BufferedWriter writer = null;
    String[] hdrEntries = new String[39];
    // Calculate the row,col spacing.
    GridGeometry3d geometry = grid.getGeometry();
    Point3d[] points = geometry.getCornerPoints().getCopyOfPoints();
    Unit xyUnitApp = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    for (int i = 0; i < points.length; i++) {
      double x = points[i].getX();
      double y = points[i].getY();
      double z = points[i].getZ();
      x = Unit.convert(x, xyUnitApp, _model.getXyUnit());
      y = Unit.convert(y, xyUnitApp, _model.getXyUnit());
      points[i] = new Point3d(x, y, z);
    }
    double angle = Math.atan2(points[1].getY() - points[0].getY(), points[1].getX() - points[0].getX());
    angle *= 180 / Math.PI;
    double dx01 = points[1].getX() - points[0].getX();
    double dy01 = points[1].getY() - points[0].getY();
    dx01 /= geometry.getNumColumns() - 1;
    dy01 /= geometry.getNumColumns() - 1;
    double colSpacing = Math.sqrt(dx01 * dx01 + dy01 * dy01);
    double dx03 = points[3].getX() - points[0].getX();
    double dy03 = points[3].getY() - points[0].getY();
    dx03 /= geometry.getNumRows() - 1;
    dy03 /= geometry.getNumRows() - 1;
    double rowSpacing = Math.sqrt(dx03 * dx03 + dy03 * dy03);
    int nx = 0;
    int ny = 0;
    double dx = 0;
    double dy = 0;
    //if (_model.getOrientation() == GridOrientation.X_IS_COLUMN) {
    nx = geometry.getNumColumns();
    ny = geometry.getNumRows();
    dx = colSpacing;
    dy = rowSpacing;
    //    } else if (_model.getOrientation() == GridOrientation.Y_IS_COLUMN) {
    //      ny = geometry.getNumColumns();
    //      nx = geometry.getNumRows();
    //      dx = rowSpacing;
    //      dy = colSpacing;
    //      angle -= 90;
    //    } else {
    //      throw new IOException("Invalid orientation: " + _model.getOrientation() + ".");
    //    }
    // Set up strings defining the header entries.
    hdrEntries[0] = "#<CPS_v1 TYPE=MODSPEC_GRID/>";
    hdrEntries[1] = "#";
    hdrEntries[2] = "#";
    hdrEntries[3] = "#<HDR_MODSPEC_GRID>";
    hdrEntries[4] = "#VERSION    = 1.0";
    hdrEntries[5] = "#ENDIAN     = " + ByteOrder.nativeOrder();
    hdrEntries[6] = "#XORG       = " + points[0].getX();
    hdrEntries[7] = "#YORG       = " + points[0].getY();
    hdrEntries[8] = "#NX         = " + nx;
    hdrEntries[9] = "#NY         = " + ny;
    hdrEntries[10] = "#DX         = " + dx;
    hdrEntries[11] = "#DY         = " + dy;
    hdrEntries[12] = "#ANGLE      = " + angle;
    hdrEntries[13] = "#ZNON       = " + grid.getNullValue();
    hdrEntries[14] = "#DESCRIPTION = \"\"";
    hdrEntries[15] = "#ATTRIBUTE  = \"\"";
    hdrEntries[16] = "#DATA_FILE  = NONE";
    hdrEntries[17] = "#STORAGE_ORDER = XY";
    hdrEntries[18] = "#ENCODING   = binary";
    hdrEntries[19] = "#NILSTRING  = nil";
    hdrEntries[20] = "#WRAP       = 1";
    hdrEntries[21] = "#NCOLUMNS   = 1";
    hdrEntries[22] = "#FILLOUT    = NO";
    hdrEntries[23] = "#FIELDS     = (grid)";
    hdrEntries[24] = "##DEFAULTS  = (\"\")";
    hdrEntries[25] = "#HDRS       = (0)";
    hdrEntries[26] = "#FIELDTYPES = (\"\")";
    if (xyUnitApp == Unit.METER) {
      hdrEntries[27] = "#UNITS      = (meters)";
    } else if (xyUnitApp == Unit.FOOT) {
      hdrEntries[27] = "#UNITS      = (feet)";
    } else {
      hdrEntries[27] = "#UNITS      = (unknown)";
    }
    hdrEntries[28] = "#VARTYPES   = (F)";
    hdrEntries[29] = "#DELIMITERS = (NO)";
    hdrEntries[30] = "#WIDTHS     = (0)";
    hdrEntries[31] = "#NLINES     = 0";
    hdrEntries[32] = "#NPACKETS   = 0";
    hdrEntries[34] = "#ADDRESS    = (0, xxx)";
    hdrEntries[35] = "#</MODSPEC_GRID>";
    hdrEntries[36] = "#";
    hdrEntries[37] = "#";
    hdrEntries[38] = "#<DTA_MODSPEC_GRID>";
    // Get the offset in bytes to start of the binary data.
    long dataStartAddr = 0;
    // The first four headers are potentially variable in length.
    for (int k = 0; k < 4; k++) {
      Formatter fmt = new Formatter();
      fmt.format("%s\n", hdrEntries[k]);
      hdrEntries[k] = fmt.toString();
      dataStartAddr += hdrEntries[k].length();
    }
    // The next bunch are all 71 characters long.
    for (int k = 4; k < 35; k++) {
      Formatter fmt = new Formatter();
      fmt.format("%-70s\n", hdrEntries[k]);
      hdrEntries[k] = fmt.toString();
      dataStartAddr += hdrEntries[k].length();
    }
    // The last group are again potentially variable in length.
    for (int k = 35; k < 39; k++) {
      Formatter fmt = new Formatter();
      fmt.format("%s\n", hdrEntries[k]);
      hdrEntries[k] = fmt.toString();
      dataStartAddr += hdrEntries[k].length();
    }
    // Now complete the ADDRESS entry.
    Formatter fmt = new Formatter();
    StringBuffer sb = new StringBuffer(hdrEntries[34]);
    fmt.format("%d)", dataStartAddr);
    String tmpStr = fmt.toString();
    int xxxIndex = sb.indexOf("xxx)");
    sb.replace(xxxIndex, xxxIndex + tmpStr.length(), tmpStr);
    hdrEntries[34] = sb.substring(0);
    try {
      // Write the ASCII header.
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath))));
      for (int k = 0; k < 39; k++) {
        writer.write(hdrEntries[k]);
      }
      writer.flush();
    } finally {
      // Close the writer if it was opened.
      if (writer != null) {
        writer.close();
      }
    }
    return dataStartAddr;
  }

  /**
   * Writes the data section of the ModSpec grid for the binary format.
   * @param grid the grid property containing the data to write.
   * @param filePath the ModSpec grid file path.
   * @param dataStartAddr the starting byte address to write the data.
   * @exception IOException thrown on write error.
   */
  public void writeBinaryData(final Grid3d grid, final String filePath, final long dataStartAddr) throws IOException {
    RandomAccessFile raf = null;
    GridGeometry3d geometry = grid.getGeometry();
    int numGridPts = geometry.getNumRows() * geometry.getNumColumns();
    try {
      raf = new RandomAccessFile(filePath, "rw");
      FileChannel fc = raf.getChannel();
      // Read the data section address header.
      long buffersize = WORDS_IN_DATA_HEADER * LENGTH_OF_INT + numGridPts * LENGTH_OF_FLOAT + 1;
      MappedByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, dataStartAddr, buffersize);
      buf.order(ByteOrder.LITTLE_ENDIAN);
      // Write the binary data header.
      buf.putInt(numGridPts);
      buf.putInt(0);
      buf.putInt((int) (dataStartAddr + WORDS_IN_DATA_HEADER * LENGTH_OF_INT + numGridPts * LENGTH_OF_FLOAT) + 1);
      buf.putInt(0);
      buf.putInt((int) (dataStartAddr + (WORDS_IN_DATA_HEADER - 1) * LENGTH_OF_INT));
      buf.putInt(numGridPts);
      // Write the grid data.
      Unit dataUnit = grid.getDataUnit();
      Unit dataUnitStorage = _model.getDataUnit();

      // If the domain of the grid data unit does not match the domain of the specified storage data unit, then
      // simply set the data unit to be the same as the storage data unit in order to avoid any conversion errors
      // due to domain differences.
      if (!dataUnit.getDomain().equals(dataUnitStorage.getDomain())) {
        dataUnit = dataUnitStorage;
      }

      int numRows = geometry.getNumRows();
      int numCols = geometry.getNumColumns();
      float[][] values = grid.getValues();
      for (int row = 0; row < numRows; row++) {
        for (int col = 0; col < numCols; col++) {
          float value = values[row][col];
          if (!grid.isNull(value)) {
            values[row][col] = Unit.convert(values[row][col], dataUnit, dataUnitStorage);
          }
        }
      }
      int nx = 0;
      int ny = 0;
      int didx = 0;
      int didy = 0;
      //     if (_model.getOrientation() == GridOrientation.X_IS_COLUMN) {
      ny = numRows;
      nx = numCols;
      didx = 1;
      didy = nx;
      //      } else if (_model.getOrientation() == GridOrientation.Y_IS_COLUMN) {
      //        nx = numRows;
      //        ny = numCols;
      //        didx = ny;
      //        didy = 1;
      //      } else {
      //        throw new IOException("Invalid orientation: " + _model.getOrientation() + ".");
      //      }
      int index = 0;
      int row;
      int col;
      for (int j = 0; j < ny; j++) {
        index = j * didy;
        for (int i = 0; i < nx; i++) {
          row = index / numCols;
          col = index % numCols;
          buf.putFloat(values[row][col]);
          index += didx;
        }
      }
      // End section containing binary form of grid with a newline.
      // This is NOT putChar('\n') because Java chars are 2 bytes, whereas
      // this binary grid format was designed around C code where a newline
      // is 1 byte. So, put one byte with the newline char cast as a byte.
      buf.put((byte) '\n');
    } finally {
      if (raf != null) {
        raf.close();
      }
    }
    return;
  }
}
