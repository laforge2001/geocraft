/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.traceviewer.renderer.trace;


import java.util.Arrays;

import org.eclipse.swt.graphics.RGB;


public class PicksData {

  private final String _name;

  private final RGB _color;

  private final float[] _picks;

  public PicksData(final float[] picks, final String name, final RGB color) {
    _name = name;
    _color = color;
    _picks = Arrays.copyOf(picks, picks.length);
  }

  public String getName() {
    return _name;
  }

  public RGB getColor() {
    return _color;
  }

  public float[] getPicks() {
    return _picks;
  }

  public int getNumPicks() {
    return _picks.length;
  }
}
