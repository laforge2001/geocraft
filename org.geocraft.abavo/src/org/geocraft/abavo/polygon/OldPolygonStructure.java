/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


import java.io.DataInputStream;
import java.io.IOException;


public class OldPolygonStructure {

  public int defined;

  public float value;

  public int ptcnt;

  public float[] pts = null;

  public void read(final DataInputStream dstream) throws IOException {
    defined = dstream.readInt();
    value = dstream.readFloat();
    ptcnt = dstream.readInt();

    // Read 4-bytes, but ignore them for the old void *.
    dstream.readInt();
    pts = null;
  }
}
