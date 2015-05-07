/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.internal;


import java.beans.PropertyChangeEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.geocraft.ui.plot.ICornerCanvas;
import org.geocraft.ui.plot.ICornerRenderer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.PlotComposite;
import org.geocraft.ui.plot.defs.CornerPlacement;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;


public class CornerCanvas extends PlotCanvas implements ICornerCanvas {

  /** The corner placement. */
  private final CornerPlacement _placement;

  private ICornerRenderer _renderer;

  /**
   * Constructs a corner canvas (Northwest, Northeast, SouthWest or Southeast) for a plot.
   * @parent the parent plot composite.
   * @parent the associated pot.
   * @parent the corner placement (Northwest, Northeast, SouthWest or Southeast).
   */
  public CornerCanvas(final PlotComposite parent, final IPlot plot, final CornerPlacement placement, final CanvasLayoutModel layoutModel) {
    super(parent, plot, SWT.NONE);
    _placement = placement;
    _renderer = new DefaultCornerRenderer();

    GridData constraints = new GridData();
    constraints.grabExcessHorizontalSpace = false;
    constraints.grabExcessVerticalSpace = false;
    constraints.horizontalSpan = 2;
    constraints.verticalSpan = 2;
    constraints.horizontalAlignment = SWT.FILL;
    constraints.verticalAlignment = SWT.FILL;
    if (placement.equals(CornerPlacement.TOP_LEFT)) {
      constraints.heightHint = layoutModel.getTopAxisHeight() + layoutModel.getTopLabelHeight();
      constraints.widthHint = layoutModel.getLeftAxisWidth() + layoutModel.getLeftLabelWidth();
    } else if (placement.equals(CornerPlacement.TOP_RIGHT)) {
      constraints.heightHint = layoutModel.getTopAxisHeight() + layoutModel.getTopLabelHeight();
      constraints.widthHint = layoutModel.getRightAxisWidth() + layoutModel.getRightLabelWidth();
    } else if (placement.equals(CornerPlacement.BOTTOM_LEFT)) {
      constraints.heightHint = layoutModel.getBottomAxisHeight() + layoutModel.getBottomLabelHeight();
      constraints.widthHint = layoutModel.getLeftAxisWidth() + layoutModel.getLeftLabelWidth();
    } else if (placement.equals(CornerPlacement.BOTTOM_RIGHT)) {
      constraints.heightHint = layoutModel.getBottomAxisHeight() + layoutModel.getBottomLabelHeight();
      constraints.widthHint = layoutModel.getRightAxisWidth() + layoutModel.getRightLabelWidth();
    } else {
      throw new IllegalArgumentException("Invalid corner placement: " + placement);
    }
    setLayoutData(constraints);
  }

  public CornerPlacement getPlacement() {
    return _placement;
  }

  public void canvasLayoutUpdated(final CanvasLayoutModel layout) {
    // No action is needed here, since the corner canvases simple resize according
    // to the other plot canvases.
  }

  public void paintControl(final PaintEvent event) {
    // Fill in the entire corner canvas with the background color.
    GC gc = event.gc;
    Point size = getSize();
    gc.setBackground(getBackground());
    Rectangle rect = new Rectangle(0, 0, size.x, size.y);
    gc.fillRectangle(rect);
    _renderer.render(gc, rect, _textProperties);
  }

  public void setRenderer(ICornerRenderer renderer) {
    _renderer = renderer;
  }

  public void propertyChange(final PropertyChangeEvent evt) {
    // TODO Auto-generated method stub

  }
}
