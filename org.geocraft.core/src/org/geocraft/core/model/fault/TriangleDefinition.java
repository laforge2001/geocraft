/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.fault;


public class TriangleDefinition {

  /** Index of the first vertex. */
  int _vertex1;

  /** Index of the second vertex. */
  int _vertex2;

  /** Index of the third vertex. */
  int _vertex3;

  public TriangleDefinition(final int vertex1, final int vertex2, final int vertex3) {
    _vertex1 = vertex1;
    _vertex2 = vertex2;
    _vertex3 = vertex3;
  }

  public int getVertex1() {
    return _vertex1;
  }

  public int getVertex2() {
    return _vertex2;
  }

  public int getVertex3() {
    return _vertex3;
  }
}
