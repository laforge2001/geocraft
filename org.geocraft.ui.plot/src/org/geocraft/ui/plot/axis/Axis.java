/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.axis;


import org.geocraft.core.common.model.AbstractBean;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.label.ILabel;
import org.geocraft.ui.plot.label.Label;


/**
 * Simple implementation of the plot axis interface.
 */
public class Axis extends AbstractBean implements IAxis {

  public static final String LABEL = "label";

  public static final String LABEL_TEXT = "labelText";

  public static final String LABEL_ORIENTATION = "labelOrientation";

  public static final String LABEL_ALIGNMENT = "labelAlignment";

  public static final String LABEL_VISIBILITY = "labelVisible";

  public static final String AXIS_UNIT = "unit";

  public static final String AXIS_ORIENTATION = "orientation";

  public static final String AXIS_DIRECTION = "direction";

  public static final String AXIS_SCALE = "scale";

  public static final String DEFAULT_START = "defaultStart";

  public static final String DEFAULT_END = "defaultEnd";

  public static final String VIEWABLE_START = "viewableStart";

  public static final String VIEWABLE_END = "viewableEnd";

  /** The axis label. */
  private final ILabel _label;

  /** The axis unit of measurement. */
  private Unit _axisUnit;

  /** The axis orientation. */
  private Orientation _axisOrientation;

  /** The direction of the axis. */
  private AxisDirection _axisDirection;

  /** The scale of the axis. */
  private AxisScale _axisScale;

  /** The default start of the axis. */
  private double _defaultStart;

  /** The default start of the axis. */
  private double _defaultEnd;

  /** The viewable start of the axis. */
  private double _viewableStart;

  /** The viewable end of the axis. */
  private double _viewableEnd;

  /** The flag indicating of the axis scale is editable. */
  private boolean _isScaleEditable;

  /**
   * The minimal constructor.
   * 
   * @param label axis label.
   * @param unit the axis unit of measurement.
   * @param range the default and viewable range.
   * @param orientation the axis orientation.
   */
  public Axis(final ILabel label, final Unit unit, final AxisRange range, final Orientation orientation) {
    this(label, unit, range, range, orientation, getDefaultDirection(orientation), AxisScale.LINEAR, false);
  }

  /**
   * The minimal constructor.
   * 
   * @param label axis label.
   * @param unit the axis unit of measurement.
   * @param range the default and viewable range.
   * @param orientation the axis orientation.
   */
  public Axis(final ILabel label, final Unit unit, final AxisRange range, final Orientation orientation, final boolean isScaleEditable) {
    this(label, unit, range, range, orientation, getDefaultDirection(orientation), AxisScale.LINEAR, isScaleEditable);
  }

  /**
   * The intermediate constructor.
   * 
   * @param label axis label.
   * @param unit the axis unit of measurement.
   * @param range the default and viewable range.
   * @param orientation the axis orientation.
   * @param direction the axis direction;
   */
  public Axis(final ILabel label, final Unit unit, final AxisRange range, final Orientation orientation, final AxisDirection direction) {
    this(label, unit, range, range, orientation, getCorrectDirection(orientation, direction), AxisScale.LINEAR, false);
  }

  /**
   * The intermediate constructor.
   * 
   * @param label axis label.
   * @param unit the axis unit of measurement.
   * @param range the default and viewable range.
   * @param orientation the axis orientation.
   * @param direction the axis direction;
   */
  public Axis(final ILabel label, final Unit unit, final AxisRange range, final Orientation orientation, final AxisDirection direction, final boolean isScaleEditable) {
    this(label, unit, range, range, orientation, getCorrectDirection(orientation, direction), AxisScale.LINEAR,
        isScaleEditable);
  }

  /**
   * The copy constructor.
   * @param axis the axis to copy.
   */
  public Axis(final IAxis axis) {
    _label = new Label(axis.getLabel());
    setUnit(axis.getUnit());
    setOrientation(axis.getOrientation());
    setDirection(getCorrectDirection(axis.getOrientation(), axis.getDirection()));
    setScale(axis.getScale());
    setDefaultStart(axis.getDefaultStart());
    setDefaultEnd(axis.getDefaultEnd());
    setViewableStart(axis.getViewableStart());
    setViewableEnd(axis.getViewableEnd());
    _isScaleEditable = axis.isScaleEditable();
  }

  /**
   * The full constructor.
   * 
   * @param label the axis label.
   * @param unit the axis unit of measurement.
   * @param defaultRange the default range.
   * @param viewableRange the viewable range.
   * @param orientation the axis orientation.
   * @param direction the axis direction;
   * @param scale the axis scale.
   */
  public Axis(final ILabel label, final Unit unit, final AxisRange defaultRange, final AxisRange viewableRange, final Orientation orientation, final AxisDirection direction, final AxisScale scale, final boolean isScaleEditable) {
    _label = label;
    setUnit(unit);
    setOrientation(orientation);
    setDirection(direction);
    setScale(scale);
    setDefaultStart(defaultRange.getStart());
    setDefaultEnd(defaultRange.getEnd());
    setViewableStart(viewableRange.getStart());
    setViewableEnd(viewableRange.getEnd());
    _isScaleEditable = isScaleEditable;
  }

  public ILabel getLabel() {
    return _label;
  }

  public String getLabelText() {
    return _label.getText();
  }

  public void setLabelText(final String text) {
    _label.setText(text);
  }

  public Orientation getLabelOrientation() {
    return _label.getOrientation();
  }

  public void setLabelOrientation(final Orientation orientation) {
    _label.setOrientation(orientation);
  }

  public Alignment getLabelAlignment() {
    return _label.getAlignment();
  }

  public void setLabelAlignment(final Alignment alignment) {
    _label.setAlignment(alignment);
  }

  public boolean isLabelVisible() {
    return _label.isVisible();
  }

  public boolean getLabelVisible() {
    return _label.isVisible();
  }

  public void setLabelVisible(final boolean visible) {
    _label.setVisible(visible);
  }

  public Unit getUnit() {
    return _axisUnit;
  }

  public void setUnit(final Unit unit) {
    firePropertyChange(AXIS_UNIT, _axisUnit, _axisUnit = unit);
  }

  public Orientation getOrientation() {
    return _axisOrientation;
  }

  public void setOrientation(final Orientation orientation) {
    firePropertyChange(AXIS_ORIENTATION, _axisOrientation, _axisOrientation = orientation);
  }

  public AxisDirection getDirection() {
    return _axisDirection;
  }

  public void setDirection(final AxisDirection direction) {
    firePropertyChange(AXIS_DIRECTION, _axisDirection, _axisDirection = direction);
  }

  public AxisScale getScale() {
    return _axisScale;
  }

  public void setScale(final AxisScale scale) {
    firePropertyChange(AXIS_SCALE, _axisScale, _axisScale = scale);
  }

  public double getDefaultStart() {
    return _defaultStart;
  }

  public void setDefaultStart(final double start) {
    firePropertyChange(DEFAULT_START, _defaultStart, _defaultStart = start);
  }

  public double getDefaultEnd() {
    return _defaultEnd;
  }

  public void setDefaultEnd(final double end) {
    firePropertyChange(DEFAULT_END, _defaultEnd, _defaultEnd = end);
  }

  public double getViewableStart() {
    return _viewableStart;
  }

  public void setViewableStart(final double start) {
    firePropertyChange(VIEWABLE_START, _viewableStart, _viewableStart = start);
  }

  public double getViewableEnd() {
    return _viewableEnd;
  }

  public void setViewableEnd(final double end) {
    firePropertyChange(VIEWABLE_END, _viewableEnd, _viewableEnd = end);
  }

  //  public AxisRange getDefaultRange() {
  //    return new AxisRange(getDefaultStart(), getDefaultEnd());
  //  }
  //
  //  public AxisRange getViewableRange() {
  //    return new AxisRange(getViewableStart(), getViewableEnd());
  //  }

  public void setDefaultRange(final double start, final double end) {
    setDefaultStart(start);
    setDefaultEnd(end);
  }

  public void setViewableRange(final double start, final double end) {
    setViewableStart(start);
    setViewableEnd(end);
  }

  public boolean isScaleEditable() {
    return _isScaleEditable;
  }

  public void dispose() {
    // No action.
  }

  private static AxisDirection getDefaultDirection(final Orientation orientation) {
    if (orientation.equals(Orientation.HORIZONTAL)) {
      return AxisDirection.LEFT_TO_RIGHT;
    }
    return AxisDirection.BOTTOM_TO_TOP;
  }

  private static AxisDirection getCorrectDirection(final Orientation orientation, final AxisDirection direction) {
    if (orientation.equals(Orientation.HORIZONTAL)) {
      if (direction.equals(AxisDirection.TOP_TO_BOTTOM)) {
        return AxisDirection.RIGHT_TO_LEFT;
      } else if (direction.equals(AxisDirection.BOTTOM_TO_TOP)) {
        return AxisDirection.LEFT_TO_RIGHT;
      }
    } else if (orientation.equals(Orientation.VERTICAL)) {
      if (direction.equals(AxisDirection.LEFT_TO_RIGHT)) {
        return AxisDirection.BOTTOM_TO_TOP;
      } else if (direction.equals(AxisDirection.RIGHT_TO_LEFT)) {
        return AxisDirection.TOP_TO_BOTTOM;
      }
    }
    return direction;
  }
}
