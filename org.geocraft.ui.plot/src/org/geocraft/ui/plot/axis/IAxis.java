/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.axis;


import java.beans.PropertyChangeListener;

import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.label.ILabel;


/**
 * Interface for a plot axis.
 */
public interface IAxis {

  /**
   * Gets the axis label.
   * 
   * @return the axis label.
   */
  ILabel getLabel();

  /**
   * Gets the axis unit of measurement.
   * 
   * @return the axis unit of measurement.
   */
  Unit getUnit();

  /**
   * Sets the axis unit of measurement.
   * 
   * @param unit the axis unit of measurement.
   */
  void setUnit(Unit unit);

  /**
   * Gets the axis direction.
   * 
   * @return the axis direction.
   */
  AxisDirection getDirection();

  /**
   * Sets the axis direction.
   * 
   * @param direction the axis direction.
   */
  void setDirection(AxisDirection direction);

  /**
   * Gets the axis scale.
   * 
   * @return the axis scale.
   */
  AxisScale getScale();

  /**
   * Sets the axis scale.
   * 
   * @param direction the axis scale.
   */
  void setScale(AxisScale scale);

  /**
   * Gets the axis orientation.
   * 
   * @return the axis orientation.
   */
  Orientation getOrientation();

  /**
   * Gets the default axis range.
   * 
   * @return the default axis range.
   */
  //AxisRange getDefaultRange();
  /**
   * Gets the default axis start.
   * 
   * @return the default axis start.
   */
  double getDefaultStart();

  /**
   * Gets the default axis end.
   * 
   * @return the default axis end.
   */
  double getDefaultEnd();

  /**
   * Gets the viewable axis range.
   * 
   * @return the viewable axis range.
   */
  //AxisRange getViewableRange();
  /**
   * Gets the viewable axis start.
   * 
   * @return the viewable axis start.
   */
  double getViewableStart();

  /**
   * Gets the viewable axis end.
   * 
   * @return the viewable axis end.
   */
  double getViewableEnd();

  /**
   * Sets the default axis range.
   * 
   * @param range the default axis range.
   */
  void setDefaultRange(double start, double end);

  /**
   * Returns a flag indicating of the axis scale is editable (i.e. can switch between linear and log).
   * 
   * @return <i>true</i> if editable; <i>false</i> if not.
   */
  boolean isScaleEditable();

  /**
   * Sets the viewable axis range.
   * 
   * @param range the viewable axis range.
   */
  void setViewableRange(double start, double end);

  void addPropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  void dispose();
}
