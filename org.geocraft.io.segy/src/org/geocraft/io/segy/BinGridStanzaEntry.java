/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


public enum BinGridStanzaEntry {

  BinGridName("Bin grid name"),
  AlternateIAxisDescription("Alternate I-Axis description"),
  AlternateJAxisDescription("Alternate J-Axis description"),
  BinGridOriginICoordinate("Bin grid origin I coordinate"),
  BinGridOriginJCoordinate("Bin grid origin J coordinate"),
  BinGridOriginEasting("Bin grid origin Easting"),
  BinGridOriginNorthing("Bin grid origin Northing"),
  ScaleFactorOfBinGrid("Scale factor of bin grid"),
  ScaleFactorNodeICoordinate("Scale factor node I coordinate"),
  ScaleFactorNodeJCoordinate("Scale factor node J coordinate"),
  NormalBinWidthOnIAxis("Normal bin width on I axis"),
  NormalBinWidthOnJAxis("Normal bin width on J axis"),
  GridBearingOfBinGridJAxis("Grid bearing of bin grid J axis"),
  GridBearingUnitName("Grid bearing unit name"),
  BinNodeIncrementOnIAxis("Bin node increment on I axis"),
  BinNodeIncrementOnJAxis("Bin node increment on J axis"),
  FirstCheckNodeICoordinate("First check node I coordinate"),
  FirstCheckNodeJCoordinate("First check node J coordinate"),
  FirstCheckNodeEasting("First check node Easting"),
  FirstCheckNodeNorthing("First check node Northing"),
  SecondCheckNodeICoordinate("Second check node I coordinate"),
  SecondCheckNodeJCoordinate("Second check node J coordinate"),
  SecondCheckNodeEasting("Second check node Easting"),
  SecondCheckNodeNorthing("Second check node Northing"),
  ThirdCheckNodeICoordinate("Third check node I coordinate"),
  ThirdCheckNodeJCoordinate("Third check node J coordinate"),
  ThirdCheckNodeEasting("Third check node Easting"),
  ThirdCheckNodeNorthing("Third check node Northing");

  private String _text;

  BinGridStanzaEntry(String text) {
    _text = text;
  }

  @Override
  public String toString() {
    return _text;
  }
}
