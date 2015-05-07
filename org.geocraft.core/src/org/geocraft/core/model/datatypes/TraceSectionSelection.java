/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection.SectionType;


public class TraceSectionSelection {

  private SectionType _sectionType = null;

  private SeismicSurvey2d _survey2d = null;

  private int _lineNumber2d = 0;

  private SeismicSurvey3d _survey3d = null;

  private Point3d[] _points3d = new Point3d[0];

  public TraceSectionSelection(final SectionType sectionType, final SeismicSurvey3d survey3d, final Point3d[] points3d) {
    _sectionType = sectionType;
    _survey3d = survey3d;
    _points3d = new Point3d[points3d.length];
    System.arraycopy(points3d, 0, _points3d, 0, points3d.length);
  }

  public TraceSectionSelection(final SectionType sectionType, final SeismicSurvey2d survey2d, final int lineNumber2d) {
    _sectionType = sectionType;
    _survey2d = survey2d;
    _lineNumber2d = lineNumber2d;
  }

  public SectionType getSectionType() {
    return _sectionType;
  }

  public boolean is2D() {
    return _survey2d != null;
  }

  public boolean is3D() {
    return _survey3d != null;
  }

  public SeismicSurvey2d getSurvey2d() {
    return _survey2d;
  }

  public int getLineNumber2d() {
    return _lineNumber2d;
  }

  public SeismicSurvey3d getSurvey3d() {
    return _survey3d;
  }

  public Point3d[] getPoints3d() {
    Point3d[] points = new Point3d[_points3d.length];
    System.arraycopy(_points3d, 0, points, 0, points.length);
    return points;
  }
}
