/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


public class NoOpPixelRenderStrategy implements IPixelRenderStrategy {

  public void renderPixel(final int index, final int index2, final int colorIndex, final int alpha,
      final int imageWidth, final int imageHeight, final byte[] imageColors) {
    // Does nothing.
  }

  public void renderPixels(final int index0, final int index1, final int index, final int colorIndex, final int alpha,
      final int imageWidth, final int imageHeight, final byte[] imageColors) {
    // Does nothing.
  }

}
