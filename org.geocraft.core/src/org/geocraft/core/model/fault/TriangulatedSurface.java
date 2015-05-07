/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.fault;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.base.ValueObject;
import org.geocraft.core.model.datatypes.Point3d;


public class TriangulatedSurface extends ValueObject {

  /** The list of surface triangle vertices. */
  List<Point3d> _vertices = new ArrayList<Point3d>();

  /** The list of surface triangles. */
  List<TriangleDefinition> _triangles = new ArrayList<TriangleDefinition>();

  public TriangulatedSurface(final String displayName, final List<Point3d> vertices, final List<TriangleDefinition> triangles) {
    super(displayName);
    setVertices(vertices);
    setTriangles(triangles);
  }

  public Point3d[] getVertices() {
    return _vertices.toArray(new Point3d[0]);
  }

  public TriangleDefinition[] getTriangles() {
    return _triangles.toArray(new TriangleDefinition[0]);
  }

  public void setVertices(final List<Point3d> vertices) {
    _vertices = vertices;
  }

  public void setTriangles(final List<TriangleDefinition> triangles) {
    _triangles = triangles;
  }

  public int getNumVertices() {
    return _vertices.size();
  }

  public int getNumTriangles() {
    return _triangles.size();
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

}
