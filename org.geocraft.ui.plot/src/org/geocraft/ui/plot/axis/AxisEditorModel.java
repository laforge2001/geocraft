/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.axis;


import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.Orientation;


public class AxisEditorModel extends Model {

  private StringProperty _labelText;

  private EnumProperty<Unit> _axisUnit;

  private EnumProperty<AxisScale> _axisScale;

  private EnumProperty<Orientation> _axisOrientation;

  private EnumProperty<AxisDirection> _axisDirection;

  private DoubleProperty _viewableStart;

  private DoubleProperty _viewableEnd;

  public AxisEditorModel(final IAxis axis) {
    super();
    _labelText = addStringProperty(Axis.LABEL_TEXT, axis.getLabel().getText());
    _axisUnit = addEnumProperty(Axis.AXIS_UNIT, Unit.class, axis.getUnit());
    _axisScale = addEnumProperty(Axis.AXIS_SCALE, AxisScale.class, axis.getScale());
    _axisOrientation = addEnumProperty(Axis.AXIS_ORIENTATION, Orientation.class, axis.getOrientation());
    _axisDirection = addEnumProperty(Axis.AXIS_DIRECTION, AxisDirection.class, axis.getDirection());
    _viewableStart = addDoubleProperty(Axis.VIEWABLE_START, axis.getViewableStart());
    _viewableEnd = addDoubleProperty(Axis.VIEWABLE_END, axis.getViewableEnd());
  }

  public void validate(final IValidation results) {
    if (_axisUnit == null) {
      results.error(Axis.AXIS_UNIT, "Invalid axis unit: " + _axisUnit);
    }
    if (_axisScale == null) {
      results.error(Axis.AXIS_SCALE, "Invalid axis scale: " + _axisScale);
    } else {
      if (_axisScale.get().equals(AxisScale.LOG)) {
        if (_viewableStart.get() <= 0) {
          results.error(_viewableStart, "Start value must be positive for log scale.");
        }
        if (_viewableEnd.get() <= 0) {
          results.error(_viewableEnd, "End value must be positive for log scale.");
        }
      }
    }
    if (_axisOrientation == null) {
      results.error(Axis.AXIS_ORIENTATION, "Invalid axis orientation: " + _axisOrientation);
    }
    if (_axisDirection == null) {
      results.error(Axis.AXIS_DIRECTION, "Invalid axis direction: " + _axisDirection);
    }
  }

  public String getLabelText() {
    return _labelText.get();
  }

  public Unit getAxisUnit() {
    return _axisUnit.get();
  }

  public AxisScale getAxisScale() {
    return _axisScale.get();
  }

  public Orientation getAxisOrientation() {
    return _axisOrientation.get();
  }

  public AxisDirection getAxisDirection() {
    return _axisDirection.get();
  }

  public double getViewableStart() {
    return _viewableStart.get();
  }

  public double getViewableEnd() {
    return _viewableEnd.get();
  }

}
