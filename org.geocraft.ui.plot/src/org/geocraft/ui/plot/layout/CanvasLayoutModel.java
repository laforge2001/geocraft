/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.layout;


import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.defs.AxisPlacement;


public class CanvasLayoutModel extends Model {

  public static final String TITLE_VISIBLE = "Show Title?";

  public static final String TITLE_HEIGHT = "Title Height";

  public static final String TOP_AXIS_HEIGHT = "Top Axis Height";

  public static final String LEFT_AXIS_WIDTH = "Left Axis Width";

  public static final String RIGHT_AXIS_WIDTH = "Right Axis Width";

  public static final String BOTTOM_AXIS_HEIGHT = "Bottom Axis Height";

  public static final String TOP_LABEL_HEIGHT = "Top Label Height";

  public static final String LEFT_LABEL_WIDTH = "Left Label Width";

  public static final String RIGHT_LABEL_WIDTH = "Right Label Width";

  public static final String BOTTOM_LABEL_HEIGHT = "Bottom Label Height";

  private BooleanProperty _titleVisible;

  private IntegerProperty _titleHeight;

  private IntegerProperty _topAxisHeight;

  private IntegerProperty _leftAxisWidth;

  private IntegerProperty _rightAxisWidth;

  private IntegerProperty _bottomAxisHeight;

  private IntegerProperty _topLabelHeight;

  private IntegerProperty _leftLabelWidth;

  private IntegerProperty _rightLabelWidth;

  private IntegerProperty _bottomLabelHeight;

  public CanvasLayoutModel() {
    super();
    _titleVisible = addBooleanProperty(TITLE_VISIBLE, true);
    _titleHeight = addIntegerProperty(TITLE_HEIGHT, 20);
    _topLabelHeight = addIntegerProperty(TOP_LABEL_HEIGHT, 20);
    _topAxisHeight = addIntegerProperty(TOP_AXIS_HEIGHT, 20);
    _leftLabelWidth = addIntegerProperty(LEFT_LABEL_WIDTH, 20);
    _leftAxisWidth = addIntegerProperty(LEFT_AXIS_WIDTH, 30);
    _rightLabelWidth = addIntegerProperty(RIGHT_LABEL_WIDTH, 20);
    _rightAxisWidth = addIntegerProperty(RIGHT_AXIS_WIDTH, 30);
    _bottomLabelHeight = addIntegerProperty(BOTTOM_LABEL_HEIGHT, 20);
    _bottomAxisHeight = addIntegerProperty(BOTTOM_AXIS_HEIGHT, 20);
  }

  public CanvasLayoutModel(final CanvasLayoutModel model) {
    this();
    updateFrom(model);
  }

  public void validate(final IValidation results) {
    if (_titleHeight.get() < 0) {
      results.error(CanvasLayoutModel.TITLE_HEIGHT, "Title height cannot be negative.");
    }
    if (_topAxisHeight.get() < 0) {
      results.error(CanvasLayoutModel.TOP_AXIS_HEIGHT, "Top axis height cannot be negative.");
    }
    if (_leftAxisWidth.get() < 0) {
      results.error(CanvasLayoutModel.LEFT_AXIS_WIDTH, "Left axis height cannot be negative.");
    }
    if (_rightAxisWidth.get() < 0) {
      results.error(CanvasLayoutModel.RIGHT_AXIS_WIDTH, "Right axis height cannot be negative.");
    }
    if (_bottomAxisHeight.get() < 0) {
      results.error(CanvasLayoutModel.BOTTOM_AXIS_HEIGHT, "Bottom axis height cannot be negative.");
    }
  }

  public boolean getTitleVisible() {
    return _titleVisible.get();
  }

  public int getTitleHeight() {
    return _titleHeight.get();
  }

  public int getTopAxisHeight() {
    return _topAxisHeight.get();
  }

  public int getLeftAxisWidth() {
    return _leftAxisWidth.get();
  }

  public int getRightAxisWidth() {
    return _rightAxisWidth.get();
  }

  public int getBottomAxisHeight() {
    return _bottomAxisHeight.get();
  }

  public int getTopLabelHeight() {
    return _topLabelHeight.get();
  }

  public int getLeftLabelWidth() {
    return _leftLabelWidth.get();
  }

  public int getRightLabelWidth() {
    return _rightLabelWidth.get();
  }

  public int getBottomLabelHeight() {
    return _bottomLabelHeight.get();
  }

  public void setTitleVisible(final boolean visible) {
    _titleVisible.set(visible);
  }

  public void setTitleHeight(final int height) {
    _titleHeight.set(height);
  }

  public void setTopAxisHeight(final int axisHeight) {
    _topAxisHeight.set(axisHeight);
  }

  public void setLeftAxisWidth(final int axisWidth) {
    _leftAxisWidth.set(axisWidth);
  }

  public void setRightAxisWidth(final int axisWidth) {
    _rightAxisWidth.set(axisWidth);
  }

  public void setBottomAxisHeight(final int axisHeight) {
    _bottomAxisHeight.set(axisHeight);
  }

  public void setTopLabelHeight(final int labelHeight) {
    _topLabelHeight.set(labelHeight);
  }

  public void setLeftLabelWidth(final int labelWidth) {
    _leftLabelWidth.set(labelWidth);
  }

  public void setRightLabelWidth(final int labelWidth) {
    _rightLabelWidth.set(labelWidth);
  }

  public void setBottomLabelHeight(final int labelHeight) {
    _bottomLabelHeight.set(labelHeight);
  }

  public int getAxisWidthOrHeight(final AxisPlacement placement) {
    if (placement.equals(AxisPlacement.TOP)) {
      return getTopAxisHeight();
    } else if (placement.equals(AxisPlacement.LEFT)) {
      return getLeftAxisWidth();
    } else if (placement.equals(AxisPlacement.RIGHT)) {
      return getRightAxisWidth();
    } else if (placement.equals(AxisPlacement.BOTTOM)) {
      return getBottomAxisHeight();
    }
    throw new IllegalArgumentException("Invalid axis placement: " + placement);
  }

  public int getLabelWidthOrHeight(final AxisPlacement placement) {
    if (placement.equals(AxisPlacement.TOP)) {
      return getTopLabelHeight();
    } else if (placement.equals(AxisPlacement.LEFT)) {
      return getLeftLabelWidth();
    } else if (placement.equals(AxisPlacement.RIGHT)) {
      return getRightLabelWidth();
    } else if (placement.equals(AxisPlacement.BOTTOM)) {
      return getBottomLabelHeight();
    }
    throw new IllegalArgumentException("Invalid label placement: " + placement);
  }
}
