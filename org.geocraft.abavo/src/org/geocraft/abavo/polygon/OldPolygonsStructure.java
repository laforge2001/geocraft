/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


import java.io.DataInputStream;
import java.io.IOException;


public class OldPolygonsStructure {

  public char[] colorfile = null;

  OldPolygonStructure[] p = new OldPolygonStructure[64];

  int pordcnt = 0;

  int[] pord = new int[64];

  int fill;

  int sym_lock;

  float pnf;

  int[] pad = new int[15];

  public void read(final DataInputStream dstream) throws IOException {
    // Read 4-bits for old char *, but ignore them.
    dstream.readInt();
    colorfile = null;

    for (int i = 0; i < 64; i++) {
      p[i] = new OldPolygonStructure();
      p[i].read(dstream);
    }
    pordcnt = dstream.readInt();
    for (int i = 0; i < 64; i++) {
      pord[i] = dstream.readInt();
    }
    fill = dstream.readInt();
    sym_lock = dstream.readInt();
    pnf = dstream.readFloat();
    for (int i = 0; i < 15; i++) {
      pad[i] = dstream.readInt();
    }
  }
}
