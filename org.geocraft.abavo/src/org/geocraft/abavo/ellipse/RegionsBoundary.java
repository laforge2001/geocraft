/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


public enum RegionsBoundary {
  P1toP2("Class I (Top) and Class II (Top)"), P2toP3("Class II (Top) and Class III (Top)"), P3toP4("Class III (Top) and Class IV (Top)"), P4toNULL("Class IV (Top) and NULL"), NULLtoN1(
      "NULL and Class I (Base)"), N1toN2("Class I (Base) and Class II (Base)"), N2toN3("Class II (Base) and Class III (Base)"), N3toN4("Class III (Base) and Class IV (Base)"), N4toNULL(
      "Class IV (Base) and NULL"), NULLtoP1("NULL and Class I (Top)");

  private String _name;

  RegionsBoundary(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
