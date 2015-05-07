/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.component;


import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.plot.ICornerRenderer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.sectionviewer.ISectionViewer;


public class TraceAxisCornerRenderer implements ICornerRenderer {

  private final ISectionViewer _sectionViewer;

  private final AxisPlacement _placement;

  public TraceAxisCornerRenderer(final ISectionViewer sectionViewer, final AxisPlacement placement) {
    _sectionViewer = sectionViewer;
    _placement = placement;
  }

  public void render(final GC graphics, final Rectangle rectangle, final TextProperties textProperties) {
    // If the trace section currently in the viewer is null, then simply fill the rectangle
    // with the background color and then return.
    TraceSection section = _sectionViewer.getTraceSection();
    if (section == null) {
      graphics.fillRectangle(rectangle);
      return;
    }

    IPlot plot = _sectionViewer.getPlot();
    IModelSpace modelSpace = plot.getActiveModelSpace();
    ModelSpaceBounds bounds = modelSpace.getViewableBounds();
    IModelSpaceCanvas canvas = plot.getModelSpaceCanvas();
    Point2D.Double pixelMin = new Point2D.Double(0, 0);
    canvas.transformModelToPixel(modelSpace, bounds.getStartX(), 0, pixelMin);
    Point2D.Double pixelMax = new Point2D.Double(0, 0);
    canvas.transformModelToPixel(modelSpace, bounds.getEndX(), 0, pixelMax);

    graphics.setFont(textProperties.getFont());
    FontMetrics metrics = graphics.getFontMetrics();
    TraceAxisKey[] keys = section.getTraceAxisKeys();
    int numKeys = keys.length;
    int startIndex = 0;
    if (section.is2D()) {
      startIndex = 1;
    }

    for (int i = startIndex; i < keys.length; i++) {
      int px = 0;
      String label = keys[i].toString();
      if (_placement.equals(AxisPlacement.LEFT)) {
        label = keys[i].toString() + " -> ";
        int textWidth = metrics.getAverageCharWidth() * label.length();
        px = rectangle.width - textWidth;
      } else if (_placement.equals(AxisPlacement.RIGHT)) {
        label = " <- " + keys[i].toString();
      }
      int py0 = rectangle.height - numKeys * 2 - numKeys * metrics.getHeight() - 2;
      int py = py0 + i * 2 + i * metrics.getHeight();
      graphics.drawText(label, px, py);
    }
  }

}
