/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.seismic;


import org.geocraft.core.common.math.GeometryUtil;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.geometry.LineGeometry;


/**
 * This class describes a 2D seismic survey entity.
 * A 2D seismic survey contains a collection of 2D seismic geometries
 * and has seismic datasets associated with it. The associated seismic
 * datasets may contain seismic traces extending to the full limits of
 * the survey geometries, or may contain subsets.
 */
public class SeismicSurvey2d extends GridGeometry2d {

  /**
   * Constructs an 2D seismic survey with the specified name and geometry.
   * Initially the survey will not have any seismic datasets associated with it.
   * These can be added using the <code>addPostStack2d</code> and the
   * <code>addPostStack2ds</code> methods.
   * @param name the name of the survey.
   * @param mapper the survey mapper.
   * @param seismicLines the array of 2D seismic lines in the survey.
   */
  public SeismicSurvey2d(final String name, final SeismicLine2d[] seismicLines) {
    super(name, seismicLines);
  }

  @Override
  public SeismicLine2d[] getLines() {
    LineGeometry[] lineGeometries = super.getLines();
    SeismicLine2d[] seismicLines = new SeismicLine2d[lineGeometries.length];
    for (int i = 0; i < lineGeometries.length; i++) {
      seismicLines[i] = (SeismicLine2d) lineGeometries[i];
    }
    return seismicLines;
  }

  @Override
  public SeismicLine2d getLine(final int index) {
    return (SeismicLine2d) super.getLine(index);
  }

  /**
   * Checks if the survey contains a line with the given #.
   * 
   * @param lineNumber the line number to search for.
   * @return <i>true</i> if a line with the given # is found; <i>false</i> if not.
   */
  public boolean containsLine(final int lineNumber) {
    for (SeismicLine2d seismicLine : getLines()) {
      if (seismicLine.getNumber() == lineNumber) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the survey contains a line with the given name.
   * 
   * @param lineName the line name to search for.
   * @return <i>true</i> if a line with the given name is found; <i>false</i> if not.
   */
  public boolean containsLine(final String lineName) {
    for (SeismicLine2d seismicLine : getLines()) {
      if (seismicLine.getDisplayName().equals(lineName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the 2D seismic line geometry with the given line name.
   * 
   * @param lineName the line name.
   * @return the 2D seismic line.
   * @throws IllegalArgumentException if no matching line geometry found.
   */
  @Override
  public SeismicLine2d getLineByName(final String lineName) {
    for (SeismicLine2d seismicLine : getLines()) {
      if (seismicLine.getDisplayName().equals(lineName)) {
        return seismicLine;
      }
    }
    throw new IllegalArgumentException("Survey does not contain line with the given name: " + lineName);
  }

  /**
   * Returns the 2D seismic line geometry with the given line number.
   * 
   * @param lineNumber the line number.
   * @return the 2D seismic line.
   * @throws IllegalArgumentException if no matching line geometry found.
   */
  @Override
  public SeismicLine2d getLineByNumber(final int lineNumber) {
    for (SeismicLine2d seismicLine : getLines()) {
      if (seismicLine.getNumber() == lineNumber) {
        return seismicLine;
      }
    }
    throw new IllegalArgumentException("Survey does not contain line with the given number: " + lineNumber);
  }

  public boolean matchesGeometry(final SeismicSurvey2d survey) {
    // First check if the # of lines is the same.
    if (getNumLines() != survey.getNumLines()) {
      return false;
    }
    // Next check each of the lines individually.
    for (SeismicLine2d seismicLine : getLines()) {
      int lineNumber = seismicLine.getNumber();
      if (!survey.containsLine(lineNumber)) {
        return false;
      }
      if (!seismicLine.matchesGeometry(survey.getLineByNumber(lineNumber))) {
        return false;
      }
    }
    // The # of lines and their geometries match, so return true.
    return true;
  }

  @Override
  public String[] getLineNames() {
    String[] lineNames = new String[getNumLines()];
    for (int i = 0; i < lineNames.length; i++) {
      lineNames[i] = getLine(i).getDisplayName();
    }
    return lineNames;
  }

  /**
   * Transforms x,y coordinates to the nearest line,cdp coordinates in the survey.
   * First, a search is performed to find the nearest line to the given x,y coordinates is found.
   * Then, the line is searched to find the nearest CDP location to the given x,y coordinates.
   * Note: The search to find the nearest line is an approximation based on a straight line
   * between the end points of each line. If the survey contains line with significant "bend"
   * or non-linearity, then this transform may not yield the actual closes line.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the line,cdp coordinates.
   */
  public float[] transformXYToLineCDP(final double x, final double y) {
    double minDistance = Double.POSITIVE_INFINITY;
    int lineNumber = -999;
    float cdp = -999;
    for (LineGeometry lineGeometry : getLines()) {
      int numBins = lineGeometry.getNumBins();
      Point3d[] points = lineGeometry.getPoints().getPointsDirect();

      // Approximate the distance using only the first and last points of the line geometry.
      for (int i = 1; i < numBins; i++) {
        double x0 = points[i - 1].getX();
        double y0 = points[i - 1].getY();
        double x1 = points[i].getX();
        double y1 = points[i].getY();
        double distance = GeometryUtil.distancePointToLine(x0, y0, x1, y1, x, y);
        if (Double.isInfinite(minDistance) || distance < minDistance) {
          minDistance = distance;
          lineNumber = lineGeometry.getNumber();
        }
      }
    }

    // On the nearest line, now find the nearest CDP.
    minDistance = Double.POSITIVE_INFINITY;
    SeismicLine2d seismicLine = getLineByNumber(lineNumber);
    int numBins = seismicLine.getNumBins();
    for (int bin = 0; bin < numBins; bin++) {
      double[] xy = seismicLine.transformBinToXY(bin);
      double distance = GeometryUtil.distancePointToPoint(x, y, xy[0], xy[1]);
      if (Double.isInfinite(minDistance) || distance < minDistance) {
        minDistance = distance;
        cdp = seismicLine.transformBinToCdp(bin);
      }
    }

    return new float[] { lineNumber, cdp };
  }
}
