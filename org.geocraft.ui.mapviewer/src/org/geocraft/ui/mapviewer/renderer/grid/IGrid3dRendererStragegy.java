/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.grid;


import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


public interface IGrid3dRendererStragegy {

  public void render(final IModelSpaceCanvas canvas, final IModelSpace modelSpace, final Grid3d grid,
      final int drawPixelMinX, final int drawPixelMaxX, final int drawPixelMinY, final int drawPixelMaxY,
      final int imageWidth, final int numBytesPerPixel, final byte[] imagePixels, final byte[] imageAlphas);
}
