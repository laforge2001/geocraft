/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.sectionviewer;


import java.beans.PropertyChangeEvent;

import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.PlotShape;


/**
 * The basic implementation of a plot trace.
 * TODO: Finish this class!
 */
public class PlotTrace extends PlotShape implements IPlotTrace {

  /** The trace number. */
  protected int _traceNo;

  /** The original trace entity. */
  protected Trace _traceOriginal;

  /** The display trace entity. */
  protected Trace _trace;

  /** The trace data minimum. */
  protected float _dataMin;

  /** The trace data maximum. */
  protected float _dataMax;

  /** The trace data average. */
  protected float _dataAvg;

  /** The flag indicating if statistics has been computed for the trace. */
  protected boolean _statisticsComputed;

  /**
   * Constructs a seisplot trace.
   * @param traceNo the trace no.
   * @param trace the actual trace value object.
   */
  public PlotTrace(final int traceNo, final Trace trace) {
    super(ShapeType.TRACE, "");
    _traceNo = traceNo;
    _traceOriginal = trace;
    _trace = new Trace(trace);
    _statisticsComputed = false;
    setRenderLevel(RenderLevel.IMAGE_UNDER_GRID);
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  public int getTraceNo() {
    return _traceNo;
  }

  public Trace getOriginalTrace() {
    return _traceOriginal;
  }

  public Trace getTrace() {
    return _trace;
  }

  public float getDataMinimum() {
    computeDataStats();
    return _dataMin;
  }

  public float getDataMaximum() {
    computeDataStats();
    return _dataMax;
  }

  public float getDataAverage() {
    computeDataStats();
    return _dataAvg;
  }

  public Rectangle getBounds(final IModelSpaceCanvas canvas) {
    return null; // TODO:
  }

  //  @SuppressWarnings("unused")
  //  public void pointPropertiesUpdated(final PlotPropertiesEvent event) {
  //    updated();
  //  }

  /**
   * TODO move into Trace?
   */
  protected void computeDataStats() {
    if (_statisticsComputed) {
      return;
    }
    _dataMin = Float.NaN;
    _dataMax = Float.NaN;
    _dataAvg = Float.NaN;
    double dataSum = 0;
    float[] data = _trace.getDataReference();
    for (int i = 0; i < _trace.getNumSamples(); i++) {
      float v = data[i];
      dataSum += Math.abs(v);
      if (Double.isNaN(_dataMin) || Double.isNaN(_dataMax)) {
        _dataMin = v;
        _dataMax = v;
      } else {
        _dataMin = Math.min(v, _dataMin);
        _dataMax = Math.max(v, _dataMax);
      }
    }
    _dataAvg = (float) (dataSum / _trace.getNumSamples());
    _statisticsComputed = true;
  }

  public void recomputeDataStats() {
    _statisticsComputed = false;
    computeDataStats();
  }

  public Rectangle getRectangle(final IModelSpaceCanvas canvas) {
    // TODO Auto-generated method stub
    return null;
  }

  public void propertyChange(final PropertyChangeEvent evt) {
    // TODO Auto-generated method stub
  }

}
