/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.utilities.areaofinterest;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.geocraft.core.model.grid.Grid3d;


/**
 * Class representing a bitmap.  Return false if trying to access
 * an out-of-bounds value instead of throwing an exception
 * @author pixtojl
 *
 */
public class BitMap {

  BitSet _bits;

  public int _nrows;

  public int _ncols;

  // Flag used to determine if a null row is between two valid rows 
  public boolean _nullRowFound;

  // Flag used to determine if a null column is betweeen two valid columns
  public boolean _nullColFound;

  BitMap(int nrows, int ncols) {
    _bits = new BitSet(nrows * ncols);
    _nrows = nrows;
    _ncols = ncols;
    _bits.clear();
  }

  BitMap(final String filename) {
    read(filename);
  }

  BitMap(final Grid3d inputGrid) {
    setGrid(inputGrid);
  }

  public void read(final String filename) {
    List<String> lines = new ArrayList<String>();
    try {
      FileInputStream fis = new FileInputStream(filename);
      DataInputStream dis = new DataInputStream(fis);
      BufferedReader br = new BufferedReader(new InputStreamReader(dis));
      String s;
      int numRows = 0;
      int numCols = 0;
      while ((s = br.readLine()) != null) {
        if (s.length() > numCols) {
          numCols = s.length();
        }
        numRows++;
        lines.add(s);
      }

      _bits = new BitSet(numRows * numCols);
      _nrows = numRows;
      _ncols = numCols;

      // Save rows or columns that may be all null
      boolean[] nullRows = new boolean[numRows];
      boolean[] nullCols = new boolean[numCols];

      // initialize the first and last rows that are valid
      int validMinRow = -1;
      int validMaxRow = numRows;

      clear();
      int row = numRows - 1;
      for (String bs : lines) {
        boolean nullRowFlag = true;
        for (int col = 0; col < bs.length(); col++) {
          if (bs.charAt(col) == '1') {
            set(row, col, true);
            nullRowFlag = false;
          }
        }
        nullRows[row] = nullRowFlag;

        // Determine a minimum and maximum row that is valid
        if (!nullRowFlag) {
          validMinRow = row;
          if (validMaxRow == numRows) {
            validMaxRow = row;
          }
        }
        row--;
      }

      // Look for a null row between two valid rows
      boolean nullRowFound = false;
      if (validMinRow > 0) {
        int currentRow = validMinRow + 1;
        while (!nullRowFound && currentRow < validMaxRow) {
          if (nullRows[currentRow]) {
            nullRowFound = true;
          }
          row++;
        }
      }

      // initialize the first and last columns that are valid
      int validMinCol = -1;
      int validMaxCol = numCols;

      // look for columns that are completely null
      for (int col = 0; col < numCols; col++) {
        boolean nullColFlag = true;
        for (row = 0; row < numRows && nullColFlag; row++) {
          if (get(row, col)) {
            nullColFlag = false;
          }
        }
        nullCols[col] = nullColFlag;

        // Determine a minimum and maximum column that is valid
        if (!nullColFlag) {
          if (validMinCol < 0) {
            validMinCol = col;
          }
          validMaxCol = col;
        }
      }

      // Look for a null column between two valid columns
      boolean nullColFound = false;
      if (validMinCol > 0) {
        int currentCol = validMinCol + 1;
        while (!nullColFound && currentCol < validMaxCol) {
          if (nullCols[currentCol]) {
            nullColFound = true;
          }
          currentCol++;
        }
      }

      // Save whether we found a null row or column
      _nullRowFound = nullRowFound;
      _nullColFound = nullColFound;

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Set the bitmap based on the current grid
  public void setGrid(final Grid3d inputGrid) {
    int numRows = inputGrid.getNumRows();
    int numCols = inputGrid.getNumColumns();

    _bits = new BitSet(numRows * numCols);
    _nrows = numRows;
    _ncols = numCols;

    boolean[] nullRows = new boolean[numRows];
    boolean[] nullCols = new boolean[numCols];

    int validMinRow = -1;
    int validMaxRow = numRows;
    clear();

    // Set bit map
    // (Also look for rows that are completely null)
    for (int row = 0; row < numRows; row++) {
      boolean nullRowFlag = true;
      for (int col = 0; col < numCols; col++) {
        if (!inputGrid.isNull(row, col)) {
          set(row, col, true);
          nullRowFlag = false;
        }
      }
      nullRows[row] = nullRowFlag;

      // Determine a minimum and maximum row that is valid
      if (!nullRowFlag) {
        if (validMinRow < 0) {
          validMinRow = row;
        }
        validMaxRow = row;
      }
    }

    // Look for a null row between two valid rows
    boolean nullRowFound = false;
    if (validMinRow > 0) {
      int currentRow = validMinRow + 1;
      while (!nullRowFound && currentRow < validMaxRow) {
        if (nullRows[currentRow]) {
          nullRowFound = true;
        }
        currentRow++;
      }
    }

    int validMinCol = -1;
    int validMaxCol = numCols;

    // look for columns that are completely null
    for (int col = 0; col < numCols; col++) {
      boolean nullColFlag = true;
      for (int row = 0; row < numRows && nullColFlag; row++) {
        if (!inputGrid.isNull(row, col)) {
          nullColFlag = false;
        }
      }
      nullCols[col] = nullColFlag;

      // Determine a minimum and maximum column that is valid
      if (!nullColFlag) {
        if (validMinCol < 0) {
          validMinCol = col;
        }
        validMaxCol = col;
      }
    }

    // Look for a null column between two valid columns
    boolean nullColFound = false;
    if (validMinCol > 0) {
      int currentCol = validMinCol + 1;
      while (!nullColFound && currentCol < validMaxCol) {
        if (nullCols[currentCol]) {
          nullColFound = true;
        }
        currentCol++;
      }
    }

    // Save whether we found a null row or column
    _nullRowFound = nullRowFound;
    _nullColFound = nullColFound;
  }

  public void clear() {
    _bits.clear();
  }

  public int numRows() {
    return _nrows;
  }

  public int numColumns() {
    return _ncols;
  }

  public void set(int r, int c, boolean v) {
    if (r >= 0 && r < _nrows && c >= 0 && c < _ncols) {
      _bits.set(_nrows * c + r, v);
    } else {
      throw new ArrayIndexOutOfBoundsException("r = " + r + " c = " + c);
    }
  }

  public boolean get(int r, int c) {
    if (r < 0 || r >= _nrows || c < 0 || c >= _ncols) {
      return false;
    }
    return _bits.get(_nrows * c + r);
  }

  // return whether bitmap had a null row
  // (This row must be between rows with valid data)
  public boolean nullRowFound() {
    return _nullRowFound;
  }

  // return whether bitmap had a null column
  // (This column must be between rows with valid data)
  public boolean nullColFound() {
    return _nullColFound;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int r = _nrows - 1; r >= 0; r--) {
      for (int c = 0; c < _ncols; c++) {
        if (get(r, c) == true) {
          sb.append("1");
        } else {
          sb.append("0");
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
