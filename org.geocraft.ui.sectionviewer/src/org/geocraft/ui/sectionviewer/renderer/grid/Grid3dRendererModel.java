/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.grid;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.defs.LineStyle;


/**
 * Defines the model of display parameters to use when rendering
 * a <code>Grid3d</code> entity in the section viewer.
 */
public class Grid3dRendererModel extends Model implements IGridRendererConstants {

  /** The style of the grid intersection lines. */
  private final EnumProperty<LineStyle> _lineStyle;

  /** The width of the grid intersection lines. */
  private final IntegerProperty _lineWidth;

  /** The color of the grid intersection lines. */
  private final ColorProperty _lineColor;

  /**
   * The default constructor.
   */
  public Grid3dRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    IPreferenceStore preferences = GridRendererPreferencePage.PREFERENCE_STORE;

    String lineStyleStr = preferences.getString(LINE_STYLE);
    LineStyle lineStyle = LineStyle.lookup(lineStyleStr);
    int lineWidth = preferences.getInt(LINE_WIDTH);

    _lineStyle = addEnumProperty(LINE_STYLE, LineStyle.class, lineStyle);
    _lineWidth = addIntegerProperty(LINE_WIDTH, lineWidth);
    _lineColor = addColorProperty(LINE_COLOR, new RGB(255, 0, 0));
  }

  /**
   * The copy constructor.
   * @param model the grid renderer model to copy.
   */
  public Grid3dRendererModel(final Grid3dRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(final IValidation results) {
    if (_lineWidth.get() < 0) {
      results.error(LINE_WIDTH, "Line size must be >= 0");
    }
    if (_lineStyle.isNull()) {
      results.error(LINE_STYLE, "Line style not specified.");
    }
    if (_lineColor.isNull()) {
      results.error(LINE_COLOR, "Line color not specified.");
    }
  }

  /**
   * Returns the line style.
   */
  public LineStyle getLineStyle() {
    return _lineStyle.get();
  }

  /**
   * Returns the line width (in pixels).
   */
  public int getLineWidth() {
    return _lineWidth.get();
  }

  /**
   * Returns the line color.
   */
  public RGB getLineColor() {
    return _lineColor.get();
  }

  /**
   * Sets the line color.
   */
  public void setLineColor(final RGB color) {
    _lineColor.set(color);
  }
}
