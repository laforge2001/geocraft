/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.utilities.areaofinterest;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.geocraft.core.model.aoi.MapPolygon;
import org.geocraft.core.model.datatypes.Point3d;


/**
 * Extract contours from a bitmap and store internally as a collection of
 * outer boundary (INCLUSION), inner boundary (EXCLUSION) in preparation
 * for defining an MapPolygonAOI.
 * @author pixtojl
 *
 */
public class ContourGenerator {

  // The boolean data to contour
  BitMap _bitMap = null;

  BitMap _dialateBitMap = null;

  /** record of what cells have been visited **/
  BitMap _visited1 = null;

  /** as above, but need two flags for cases 6 & 9 since 6 and 9 cells are traversed twice **/
  BitMap _visited2 = null;

  // boundary distance (valid range (0,1))
  float _boundaryDistance = 0.5f;

  /** row values for each contour generated **/
  List<double[]> _rowValues = new ArrayList<double[]>();

  /** column values for each contour generated **/
  List<double[]> _columnValues = new ArrayList<double[]>();

  /** contour type Outer Boundary (INCLUSIVE) or Inner Boundary (EXCLUSIVE) **/
  List<MapPolygon.Type> _types = new ArrayList<MapPolygon.Type>();

  /**
   * Create a contour generator.  A bitmap will need to be assigned to it
   * before calling generateContours.
   */
  public ContourGenerator() {
    // noop
  }

  /**
   * Create a contour generator and pass it a bitmap
   * @param bitMap bitmap of data to contour
   */
  public ContourGenerator(BitMap bitMap) {
    setBitMap(bitMap);
  }

  /**
   * Set the bitmap for the contour.
   * @param bitMap
   */
  public void setBitMap(BitMap bitMap) {
    _bitMap = bitMap;
    _visited1 = new BitMap(bitMap.numRows() + 1, bitMap.numColumns() + 1);
    _visited2 = new BitMap(bitMap.numRows() + 1, bitMap.numColumns() + 1);
  }

  /** 
   * return the number of generated contours
   * @return
   */
  public int numGeneratedContours() {
    return _rowValues.size();
  }

  /**
   * return the row values for the i'th generated contour
   * @param index contour index
   * @return
   */
  public double[] getRowValues(int index) {
    return _rowValues.get(index);
  }

  /**
   * return the column values for the i'th generated contour 
   * @param index contour index
   * @return
   */
  public double[] getColumnValues(int index) {
    return _columnValues.get(index);
  }

  /**
   * return the type of contour
   * @param index contour index
   * @return
   */
  public MapPolygon.Type getType(int index) {
    return _types.get(index);
  }

  /**
   * Set the boundary distance, measured in cell units
   * Must be greater than zero and less than one.
   * A setting of 0.5 will divide zeros from ones evenly
   * A setting of, say, 0.1 will bring the boundary closer to the
   * grid intersections.  
   * @param boundaryDistance distance from border node to contour
   */
  public void setBoundaryDistance(float boundaryDistance) {
    if (boundaryDistance >= 0 && boundaryDistance < 1) {
      _boundaryDistance = boundaryDistance;
    } else {
      throw new IllegalArgumentException("Boundary distance must be > 0 and < 1");
    }
  }

  /**
   * Compute the case number for the cell comprised of four corners
   * (For the specified bitMap)
   * @param row
   * @param col
   * @return
   */
  private int calcCase(int row, int col) {

    int c = 0;
    if (_bitMap.get(row, col)) {
      c += 1;
    }
    if (_bitMap.get(row, col + 1)) {
      c += 2;
    }
    if (_bitMap.get(row + 1, col)) {
      c += 4;
    }
    if (_bitMap.get(row + 1, col + 1)) {
      c += 8;
    }
    return c;
  }

  /**
   * Generate contours using the current BitMap.  Contour data is held in this 
   * class and can be extracted when this method returns.
   */
  public void generateContours() {

    _visited1.clear();
    _visited2.clear();
    _rowValues.clear();
    _columnValues.clear();
    _types.clear();

    // Yes, iteration starts at -1.  We have to iterate the cells, not the nodes where 
    // the data lies.
    for (int row = -1; row < _bitMap.numRows(); row++) {
      for (int col = -1; col < _bitMap.numColumns(); col++) {
        int cn = calcCase(row, col);
        switch (cn) {
          case 6:
          case 9:
            // have to check two arrays because this cell is traversed twice and
            // generates two boundary fragments
            if (!_visited1.get(row + 1, col + 1)) {
              generateContourLine(row, col, 1);
            }
            if (!_visited2.get(row + 1, col + 1)) {
              generateContourLine(row, col, 2);
            }
            break;
          case 0:
          case 15:
            // no contours in these cases
            _visited1.set(row + 1, col + 1, true);
            break;
          default:
            if (!_visited1.get(row + 1, col + 1)) {
              generateContourLine(row, col, 1);
            }
        }
      }
    }
  }

  /** what is the next cell row, for each of the 16 cases.  Not used for 0,6,9,15 **/
  final static int nextRow[] = { -99, -1, 0, 0, 0, -1, -99, 0, 1, -99, 1, 1, 0, -1, 0, -99 };

  /** what is the next cell column, for each of the 16 cases.  Not used for 0,6,9,15 **/
  final static int nextCol[] = { -99, 0, 1, 1, -1, 0, -99, 1, 0, -99, 0, 0, -1, 0, -1, -99 };

  /** what row value is to the right as I exit the current cell?  Not used for 0,6,9,15 **/
  final static int emitRowR[] = { -99, 0, 0, 0, 1, 0, -99, 0, 1, -99, 1, 1, 1, 0, 1, -99 };

  /** what col value is to the right as I exit the current cell?  Not used for 0,6,9,15 **/
  final static int emitColR[] = { -99, 0, 1, 1, 0, 0, -99, 1, 1, -99, 1, 1, 0, 0, 0, -99 };

  /** what row value is to the left as I exit the current cell?  Not used for 0,6,9,15 **/
  final static int emitRowL[] = { -99, 0, 1, 1, 0, 0, -99, 1, 1, -99, 1, 1, 0, 0, 0, -99 };

  /** what col value is to the left as I exit the current cell?  Not used for 0,6,9,15 **/
  final static int emitColL[] = { -99, 1, 1, 1, 0, 1, -99, 1, 0, -99, 0, 0, 0, 1, 0, -99 };

  /**
   * Starting at cell with sr,sc at the lower left corner, follow the boundary and generate
   * a contour.  For cases 6 and 9, tell which way to begin.
   * @param sr
   * @param sc
   * @param whichCase69
   * @return
   */
  private List<Point3d> followContour(int sr, int sc, int whichCase69) {

    List<Point3d> points = new ArrayList<Point3d>();

    // previous cell with ll corner at r,c
    int rLast = -999;
    int cLast = -999;

    // last point emitted
    float erLast = -999.0f;
    float ecLast = -999.0f;

    // current cell with ll corner at r,c
    int r = sr;
    int c = sc;

    // current point to emit
    float er;
    float ec;
    boolean breakOut = false;
    while (true) {

      int cn = calcCase(r, c);

      switch (cn) {
        case 6:
          if (rLast == -999 && cLast == -999) {
            // We've entered followContour on this case.  Figure out
            // which contour we should be following and set erLast and
            // ecLast so the switch below will do the right thing.
            cLast = c;
            if (whichCase69 == 1) {
              rLast = r - 1;
            } else {
              rLast = r + 1;
            }
          } else {
            // We're processing a boundary, and maybe this is the last segment
            if (rLast == r - 1) {
              if (_visited1.get(r + 1, c + 1)) {
                breakOut = true;
              }
            } else {
              if (_visited2.get(r + 1, c + 1)) {
                breakOut = true;
              }
            }
          }
          break;
        case 9:
          if (rLast == -999 && cLast == -999) {
            // We've entered followContour on this case.  Figure out
            // which contour we should be following and set erLast and
            // ecLast so the switch below will do the right thing.
            rLast = r;
            if (whichCase69 == 1) {
              cLast = c - 1;
            } else {
              cLast = c + 1;
            }
          } else {
            if (cLast == c - 1) {
              if (_visited1.get(r + 1, c + 1)) {
                breakOut = true;
              }
            } else {
              if (_visited2.get(r + 1, c + 1)) {
                breakOut = true;
              }
            }
          }
          break;
        default:
          if (_visited1.get(r + 1, c + 1)) {
            breakOut = true;
          }
          break;
      }
      if (breakOut) {
        break;
      }

      //System.out.println("rlast,clast = <" + rLast + "," + cLast + ">");
      //System.out.println("<r,c> = <" + r + "," + c + "> Follower case no: " + cn);

      switch (cn) {
        case 0:
        case 15:
          // shouldn't get here
          throw new RuntimeException("case " + cn + " encountered in followContour");
        case 6:
          // checkerboard <0,1> and <1,0> set
          if (rLast == r - 1) {
            // emit R=<1,0> L=<0,0>, mark v1, move left
            er = r + 1 + _boundaryDistance * (0 - 1);
            ec = c; // + _boundaryDistance * (0 - 0);
            _visited1.set(r + 1, c + 1, true);
            cLast = c;
            c -= 1;
          } else {
            // emit R=<0,1> L=<1,1>, mark v2, move right
            er = r + _boundaryDistance * (1 - 0);
            ec = c + 1; // + _boundaryDistance * (1 - 1);
            _visited2.set(r + 1, c + 1, true);
            cLast = c;
            c += 1;
          }
          break;
        case 9:
          // checkerboard <0,0> and <1,1> set
          if (cLast == c - 1) {
            // emit R=<1,1> L=<1,0>, move up, don't mark as visited
            er = r + 1; // + _boundaryDistance * (1 - 1);
            ec = c + 1 + _boundaryDistance * (0 - 1);
            _visited1.set(r + 1, c + 1, true);
            rLast = r;
            r += 1;
          } else {
            // emit R=<0,0> L=<0,1>, move down, don't mark as visited
            er = r; // + _boundaryDistance * (0 - 0);
            ec = c + _boundaryDistance * (1 - 0);
            _visited2.set(r + 1, c + 1, true);
            rLast = r;
            r -= 1;
          }
          break;
        default: {
          // mark point to emit
          er = r + emitRowR[cn] + _boundaryDistance * (emitRowL[cn] - emitRowR[cn]);
          ec = c + emitColR[cn] + _boundaryDistance * (emitColL[cn] - emitColR[cn]);

          // mark visited
          _visited1.set(r + 1, c + 1, true);

          // move
          rLast = r;
          cLast = c;
          r += nextRow[cn];
          c += nextCol[cn];
          break;
        }
      }
      // add Point
      if (ec != ecLast || er != erLast) {
        points.add(new Point3d(er, ec, 0));
        erLast = er;
        ecLast = ec;
      }
    }

    // close polygon if needed
    //if (points.get(0))

    return points;
  }

  /**
   * Return true if c appears on the right of the vector from a to b
   * @param a
   * @param b
   * @param c
   * @return
   */
  boolean rightTurn(Point3d a, Point3d b, Point3d c) {
    return (b.getY() - a.getY()) * (c.getX() - a.getX()) - (b.getX() - a.getX()) * (c.getY() - a.getY()) < 0;
  }

  boolean rightTurn(Point3d v01, Point3d v12) {
    return v01.getY() * v12.getX() - v01.getX() * v12.getY() < 0;
  }

  //float angle(const V3f & other) {
  //  return acos(dot(other) / (length() * other.length()));
  //}

  /**
   * Compute the total turn angle of the point list.  From that, we can determine whether
   * the points are in clockwise order or counter-clockwise order.  clockwise order implies
   * an outer boundary (inclusion), and counter-clockwise order implies a hole (exclusion)
   */
  private double calcTurnAngle(List<Point3d> pts) {
    int n = pts.size();
    double turnAngleSum = 0.0;
    for (int i = 0; i < n; i++) {
      Point3d p0 = pts.get(i);
      Point3d p1 = pts.get((i + 1) % n);
      Point3d p2 = pts.get((i + 2) % n);

      Point3d v01 = new Point3d(p1.getX() - p0.getX(), p1.getY() - p0.getY(), 0);
      Point3d v12 = new Point3d(p2.getX() - p1.getX(), p2.getY() - p1.getY(), 0);
      double lenv01 = Math.sqrt(v01.getX() * v01.getX() + v01.getY() * v01.getY());
      double lenv12 = Math.sqrt(v12.getX() * v12.getX() + v12.getY() * v12.getY());
      double dotV01V12 = v01.getX() * v12.getX() + v01.getY() * v12.getY();
      double angle = Math.acos(dotV01V12 / (lenv01 * lenv12));
      if (rightTurn(p0, p1, p2)) {
        turnAngleSum += angle;
      } else {
        turnAngleSum -= angle;
      }
    }
    //System.out.println("Returning turn angle of " + turnAngleSum);
    return turnAngleSum;
  }

  /**
   * Return true if the point list represents a ring (outer boundary, INCLUSION).
   * Return false if it represents a hole (inner boundary, EXCLUSION)
   * @param pts
   * @return
   */
  private boolean isRing(List<Point3d> pts) {
    // return true if the boundary is a ring
    if (calcTurnAngle(pts) > 0) {
      return true;
    }
    return false;
  }

  /**
   * Generate a contour line starting at cell <row,col>, and pass info for cases 6 and 9
   * to make sure the contour is started off in the right direction.
   * @param row
   * @param col
   * @param sc69
   */
  private void generateContourLine(int row, int col, int sc69) {
    List<Point3d> pts = filterPoints(followContour(row, col, sc69));
    boolean r = isRing(pts);

    double xs[] = new double[pts.size()];
    double ys[] = new double[pts.size()];
    for (int i = 0; i < pts.size(); i++) {
      xs[i] = pts.get(i).getX();
      ys[i] = pts.get(i).getY();
    }
    _rowValues.add(xs);
    _columnValues.add(ys);
    if (r) {
      // outer ring
      _types.add(MapPolygon.Type.INCLUSIVE);
    } else {
      // inner hole
      _types.add(MapPolygon.Type.EXCLUSIVE);
    }

  }

  /**
   * Return true if p0, p1, and p2 are colLinear.  Just compute the determinant, which
   * will evaluate to 2 x the area of the enclosed triangle.  If it is very close to zero
   * return true;
   * @param p0
   * @param p1
   * @param p2
   * @return
   */
  private boolean collinear(Point3d p0, Point3d p1, Point3d p2) {
    // check if 3x3 determinant (which computes area x 2 of enclosed triangle) is near zero
    //    p0.x p0.y 1
    //    p1.x p1.y 1
    //    p2.x p2.y 1
    double areaX2 = p0.getX() * p1.getY() + p1.getX() * p2.getY() + p2.getX() * p0.getY() - p0.getX() * p2.getY()
        - p1.getX() * p0.getY() - p2.getX() * p1.getY();
    return Math.abs(areaX2) < 1e-4;
  }

  /**
   * Remove redundant points from the points list.
   * @param pts
   * @return
   */
  private List<Point3d> filterPoints(List<Point3d> pts) {
    int n = pts.size();
    BitSet bs = new BitSet(n);
    bs.clear();

    if (pts.size() <= 3) {
      return pts;
    }

    // compare consecutive 3 points.  If middle point is
    // unnecessary, remove it
    for (int i = 0; i < n; i++) {
      Point3d p0 = pts.get(i);
      Point3d p1 = pts.get((i + 1) % n);
      Point3d p2 = pts.get((i + 2) % n);

      //System.out.println("Checking P0 " + p0 + " P1 " + p1 + " P2 " + p2);

      if (p0.equals(p1) || p1.equals(p2) || collinear(p0, p1, p2)) {
        pts.remove((i + 1) % n);
        i--;
        n--;
      }
    }
    return pts;
  }

  /**
   * Dialate then erode the Bitmap in case there are row or columns that are null
   */
  public void dialateErode() {

    int nRows2 = _bitMap.numRows() + 2;
    int nCols2 = _bitMap.numColumns() + 2;
    _dialateBitMap = new BitMap(nRows2, nCols2);

    for (int row = -1; row <= _bitMap.numRows(); row++) {
      for (int col = -1; col <= _bitMap.numColumns(); col++) {
        // determine if any of the values in the 3 x 3 area are set
        boolean setFlag = false;
        for (int row2 = row - 1; row2 <= row + 1 && !setFlag; row2++) {
          for (int col2 = col - 1; col2 <= col + 1 && !setFlag; col2++) {
            if (_bitMap.get(row2, col2)) {
              setFlag = true;
            }
          }
        }

        // if any values found then set the current row & column
        // (Note: The dialate bit map contains an extra row & column on each end
        if (setFlag) {
          int newRow = row + 1;
          int newCol = col + 1;
          _dialateBitMap.set(newRow, newCol, true);
        }
      }
    }

    // Now start the eroding
    _bitMap.clear();

    for (int row = 1; row < _dialateBitMap.numRows() - 1; row++) {
      for (int col = 1; col < _dialateBitMap.numColumns() - 1; col++) {
        // determine if all values in 3 x 3 area are set
        boolean setFlag = true;
        for (int row2 = row - 1; row2 <= row + 1 && setFlag; row2++) {
          for (int col2 = col - 1; col2 <= col + 1 && setFlag; col2++) {
            if (!_dialateBitMap.get(row2, col2)) {
              setFlag = false;
            }
          }
        }

        // if all values are set then set the current row and column
        // (Note: The dialate bit map contains an extra row & column on each end)
        if (setFlag) {
          int newRow = row - 1;
          int newCol = col - 1;
          _bitMap.set(newRow, newCol, true);
        }
      }
    }
  }

  ///////// TESTING

  public static void dumpCG(ContourGenerator cg) {
    for (int i = 0; i < cg.numGeneratedContours(); i++) {
      double[] rvs = cg.getRowValues(i);
      double[] cvs = cg.getColumnValues(i);
      MapPolygon.Type t = cg.getType(i);

      if (t == MapPolygon.Type.INCLUSIVE) {
        System.out.println("Outer Boundary (INCLUSIVE)");
      } else {
        System.out.println("Inner Boundary (EXCLUSIVE)");
      }
      for (int p = 0; p < rvs.length; p++) {
        System.out.println("  <" + rvs[p] + "," + cvs[p] + ">");
      }
    }
  }

  public static void main(String[] argv) {

    BitMap bm = new BitMap("/d/geoprog/u/pixtojl/dev/contourGenerator/test");
    ContourGenerator cg = new ContourGenerator(bm);
    System.out.println("bm = \n" + bm);

    System.out.println("BoundaryDistance = 0.1");
    cg.setBoundaryDistance(0.1f);
    cg.generateContours();
    dumpCG(cg);

    System.out.println("BoundaryDistance = 0.5");
    cg.setBoundaryDistance(0.5f);
    cg.generateContours();
    dumpCG(cg);
  }
}
