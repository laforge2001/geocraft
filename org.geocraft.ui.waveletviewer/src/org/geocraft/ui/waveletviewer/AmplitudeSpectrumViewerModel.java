/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved.
 */
package org.geocraft.ui.waveletviewer;


import java.util.Map;

import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.axis.AxisRange;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.listener.IModelSpaceListener;
import org.geocraft.ui.plot.model.IModelSpace;


/**
 * The model of aplitude spectrum viewer display properties.
 * @author hansegj
 *
 */
public class AmplitudeSpectrumViewerModel extends Model implements IModelSpaceListener {

  /** The property key for the X axis range of the viewable bounds */
  public static final String XAXIS_RANGE = "X Axis Range";

  /** The property key for the Y axis range of the viewable bounds */
  public static final String YAXIS_RANGE = "Y Axis Range";

  /** The property key for the X axis default range of the viewable bounds */
  public static final String XAXIS_DEFAULT_RANGE = "X Axis Default Range";

  /** The property key for the Y axis default range of the viewable bounds */
  public static final String YAXIS_DEFAULT_RANGE = "Y Axis Default Range";

  /** The property key for the viewer's TraceSection */
  public static final String TRACE_SECTION = "Trace Section";

  /** The X axis range of the viewable bounds */
  private final StringProperty _xaxisRange;

  private AxisRange _xAxisRange;

  /** The Y axis range of the vieweable bounds */
  private final StringProperty _yaxisRange;

  private AxisRange _yAxisRange;

  /** The default X axis range of the viewable bounds */
  private final StringProperty _xaxisDefaultRange;

  private AxisRange _xAxisDefaultRange;

  /** The default Y axis range of the vieweable bounds */
  private final StringProperty _yaxisDefaultRange;

  private AxisRange _yAxisDefaultRange;

  IModelSpace _modelSpace;

  public AmplitudeSpectrumViewerModel(final IModelSpace mspace) {
    _xaxisRange = addStringProperty(XAXIS_RANGE, "");
    _xAxisRange = new AxisRange(1.0, 1.0);
    _yaxisRange = addStringProperty(YAXIS_RANGE, "");
    _yAxisRange = new AxisRange(1.0, 1.0);
    _xaxisDefaultRange = addStringProperty(XAXIS_DEFAULT_RANGE, "");
    _xAxisDefaultRange = new AxisRange(1.0, 1.0);
    _yaxisDefaultRange = addStringProperty(YAXIS_DEFAULT_RANGE, "");
    _yAxisDefaultRange = new AxisRange(1.0, 1.0);
    _modelSpace = mspace;
    _modelSpace.addListener(this);
  }

  public AxisRange getXAxisRange() {
    return _xAxisRange;
  }

  /**
   * Set the X axis range of the viewable bounds
   * @param xrange The X axis range
   */
  public void setXAxisRange(final AxisRange xrange) {
    _xAxisRange = xrange;
    _xaxisRange.set(xrange.toString());
  }

  public AxisRange getYAxisRange() {
    return _yAxisRange;
  }

  /**
   * Set the Y axis range of the viewable bounds
   * @param yrange The Y axis range
   */
  public void setYAxisRange(final AxisRange yrange) {
    _yAxisRange = yrange;
    _yaxisRange.set(yrange.toString());
  }

  public AxisRange getXAxisDefaultRange() {
    return _xAxisDefaultRange;
  }

  /**
   * Set the X axis default range of the viewable bounds
   * @param xrange The X axis default range
   */
  public void setXAxisDefaultRange(final AxisRange xrange) {
    _xAxisDefaultRange = xrange;
    _xaxisDefaultRange.set(xrange.toString());
  }

  public AxisRange getYAxisDefaultRange() {
    return _yAxisDefaultRange;
  }

  /**
   * Set the Y axis default range of the viewable bounds
   * @param yrange The Y axis default range
   */
  public void setYAxisDefaultRange(final AxisRange yrange) {
    _yAxisDefaultRange = yrange;
    _yaxisDefaultRange.set(yrange.toString());
  }

  /**
   * Parse an axis range of the form "start, end"
   * @param range String representation of the axis range
   * @return An AxisRange
   */
  private AxisRange parseAxisRange(final String range) {
    String[] bounds = range.split(",");
    float start = 1.0f, end = 1.0f;
    try {
      start = Float.parseFloat(bounds[0]);
      end = Float.parseFloat(bounds[1]);
    } catch (NumberFormatException nfe) {
      //
    }
    return new AxisRange(start, end);
  }

  @Override
  public void unpickle(final Map<String, String> parms) {
    super.unpickle(parms);

    for (String key : parms.keySet()) {
      if (key.equals(XAXIS_RANGE)) {
        _xAxisRange = parseAxisRange(parms.get(key));
      } else if (key.equals(YAXIS_RANGE)) {
        _yAxisRange = parseAxisRange(parms.get(key));
      } else if (key.equals(XAXIS_DEFAULT_RANGE)) {
        _xAxisDefaultRange = parseAxisRange(parms.get(key));
      } else if (key.equals(YAXIS_DEFAULT_RANGE)) {
        _yAxisDefaultRange = parseAxisRange(parms.get(key));
      }
    }
    //reset the model space
    _modelSpace.setViewableBounds(_xAxisRange.getStart(), _xAxisRange.getEnd(), _yAxisRange.getStart(), _yAxisRange
        .getEnd());
    _modelSpace.setDefaultBounds(_xAxisDefaultRange.getStart(), _xAxisDefaultRange.getEnd(), _yAxisDefaultRange
        .getStart(), _yAxisDefaultRange.getEnd());
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.IModel#validate(org.geocraft.core.model.validation.IValidation)
   */
  @Override
  public void validate(final IValidation results) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.plot.listener.IModelSpaceListener#modelSpaceUpdated(org.geocraft.ui.plot.event.ModelSpaceEvent)
   */
  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent modelEvent) {
    setXAxisRange(modelEvent.getModelSpace().getViewableBounds().getRangeX());
    setYAxisRange(modelEvent.getModelSpace().getViewableBounds().getRangeY());
    setXAxisDefaultRange(modelEvent.getModelSpace().getDefaultBounds().getRangeX());
    setYAxisDefaultRange(modelEvent.getModelSpace().getDefaultBounds().getRangeY());
  }
}
