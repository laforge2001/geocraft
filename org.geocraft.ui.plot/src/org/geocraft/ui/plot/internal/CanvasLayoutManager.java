/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.internal;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.IModelListener;
import org.geocraft.ui.plot.IAxisLabelCanvas;
import org.geocraft.ui.plot.IAxisRangeCanvas;
import org.geocraft.ui.plot.ICornerCanvas;
import org.geocraft.ui.plot.ITitleCanvas;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.defs.CornerPlacement;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;


public class CanvasLayoutManager implements IModelListener {

  private final CanvasLayoutModel _model;

  private final Composite _plotComposite;

  private final ITitleCanvas _titleCanvas;

  private final IAxisLabelCanvas[] _axisLabelCanvases;

  private final IAxisRangeCanvas[] _axisRangeCanvases;

  private final ICornerCanvas[] _cornerCanvases;

  public CanvasLayoutManager(final CanvasLayoutModel model, final Composite plotComposite, final ITitleCanvas titleCanvas, final IAxisLabelCanvas[] axisLabelCanvases, final IAxisRangeCanvas[] axisRangeCanvases, final ICornerCanvas[] cornerCanvases) {
    _model = model;
    _plotComposite = plotComposite;
    _titleCanvas = titleCanvas;
    _axisLabelCanvases = axisLabelCanvases;
    _axisRangeCanvases = axisRangeCanvases;
    _cornerCanvases = cornerCanvases;
  }

  public void propertyChanged(String key) {
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = false;
    gridData.grabExcessVerticalSpace = false;
    gridData.horizontalSpan = 5;
    gridData.verticalSpan = 1;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.verticalAlignment = SWT.FILL;
    gridData.heightHint = _model.getTitleHeight();
    _titleCanvas.getComposite().setLayoutData(gridData);
    _titleCanvas.getLabel().setVisible(_model.getTitleVisible());

    for (IAxisLabelCanvas canvas : _axisLabelCanvases) {
      Composite composite = canvas.getComposite();
      AxisPlacement placement = canvas.getPlacement();
      int size = getAxisLabelSize(placement);
      if (placement.equals(AxisPlacement.TOP) || placement.equals(AxisPlacement.BOTTOM)) {
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.grabExcessVerticalSpace = false;
        gridData1.horizontalSpan = 1;
        gridData1.verticalSpan = 1;
        gridData1.horizontalAlignment = SWT.FILL;
        gridData1.verticalAlignment = SWT.FILL;
        gridData1.heightHint = size;
        composite.setLayoutData(gridData1);
      } else if (placement.equals(AxisPlacement.LEFT) || placement.equals(AxisPlacement.RIGHT)) {
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = false;
        gridData2.grabExcessVerticalSpace = true;
        gridData2.horizontalSpan = 1;
        gridData2.verticalSpan = 1;
        gridData2.horizontalAlignment = SWT.FILL;
        gridData2.verticalAlignment = SWT.FILL;
        gridData2.widthHint = size;
        composite.setLayoutData(gridData2);
      }
    }

    for (IAxisRangeCanvas canvas : _axisRangeCanvases) {
      Composite composite = canvas.getComposite();
      AxisPlacement placement = canvas.getPlacement();
      int size = getAxisRangeSize(placement);
      if (placement.equals(AxisPlacement.TOP) || placement.equals(AxisPlacement.BOTTOM)) {
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.grabExcessVerticalSpace = false;
        gridData1.horizontalSpan = 1;
        gridData1.verticalSpan = 1;
        gridData1.horizontalAlignment = SWT.FILL;
        gridData1.verticalAlignment = SWT.FILL;
        gridData1.heightHint = size;
        composite.setLayoutData(gridData1);
      } else if (placement.equals(AxisPlacement.LEFT) || placement.equals(AxisPlacement.RIGHT)) {
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = false;
        gridData2.grabExcessVerticalSpace = true;
        gridData2.horizontalSpan = 1;
        gridData2.verticalSpan = 1;
        gridData2.horizontalAlignment = SWT.FILL;
        gridData2.verticalAlignment = SWT.FILL;
        gridData2.widthHint = size;
        composite.setLayoutData(gridData2);
      }
    }

    for (ICornerCanvas canvas : _cornerCanvases) {
      Composite composite = canvas.getComposite();
      CornerPlacement placement = canvas.getPlacement();
      int width = getCornerWidth(placement);
      int height = getCornerHeight(placement);
      GridData gridData3 = new GridData();
      gridData3.grabExcessHorizontalSpace = false;
      gridData3.grabExcessVerticalSpace = false;
      gridData3.horizontalSpan = 2;
      gridData3.verticalSpan = 2;
      gridData3.horizontalAlignment = SWT.FILL;
      gridData3.verticalAlignment = SWT.FILL;
      gridData3.widthHint = width;
      gridData3.heightHint = height;
      composite.setLayoutData(gridData3);
    }
    _plotComposite.redraw();
    _plotComposite.update();
  }

  private int getAxisLabelSize(final AxisPlacement placement) {
    if (placement.equals(AxisPlacement.TOP)) {
      return _model.getTopLabelHeight();
    } else if (placement.equals(AxisPlacement.LEFT)) {
      return _model.getLeftLabelWidth();
    } else if (placement.equals(AxisPlacement.RIGHT)) {
      return _model.getRightLabelWidth();
    } else if (placement.equals(AxisPlacement.BOTTOM)) {
      return _model.getBottomLabelHeight();
    }
    return 0;
  }

  private int getAxisRangeSize(final AxisPlacement placement) {
    if (placement.equals(AxisPlacement.TOP)) {
      return _model.getTopAxisHeight();
    } else if (placement.equals(AxisPlacement.LEFT)) {
      return _model.getLeftAxisWidth();
    } else if (placement.equals(AxisPlacement.RIGHT)) {
      return _model.getRightAxisWidth();
    } else if (placement.equals(AxisPlacement.BOTTOM)) {
      return _model.getBottomAxisHeight();
    }
    return 0;
  }

  private int getCornerWidth(final CornerPlacement placement) {
    if (placement.equals(CornerPlacement.TOP_LEFT)) {
      return _model.getLeftLabelWidth() + _model.getLeftAxisWidth();
    } else if (placement.equals(CornerPlacement.TOP_RIGHT)) {
      return _model.getRightLabelWidth() + _model.getRightAxisWidth();
    } else if (placement.equals(CornerPlacement.BOTTOM_LEFT)) {
      return _model.getLeftLabelWidth() + _model.getLeftAxisWidth();
    } else if (placement.equals(CornerPlacement.BOTTOM_RIGHT)) {
      return _model.getRightLabelWidth() + _model.getRightAxisWidth();
    }
    return 0;
  }

  private int getCornerHeight(final CornerPlacement placement) {
    if (placement.equals(CornerPlacement.TOP_LEFT)) {
      return _model.getTopLabelHeight() + _model.getTopAxisHeight();
    } else if (placement.equals(CornerPlacement.TOP_RIGHT)) {
      return _model.getTopLabelHeight() + _model.getTopAxisHeight();
    } else if (placement.equals(CornerPlacement.BOTTOM_LEFT)) {
      return _model.getBottomLabelHeight() + _model.getBottomAxisHeight();
    } else if (placement.equals(CornerPlacement.BOTTOM_RIGHT)) {
      return _model.getBottomLabelHeight() + _model.getBottomAxisHeight();
    }
    return 0;
  }
}
