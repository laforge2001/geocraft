/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.fault;


import org.eclipse.jface.preference.IPreferenceStore;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.defs.LineStyle;


/**
 * Defines the model of display parameters to use when rendering
 * a <code>FaultSurface</code> entity in the section viewer.
 */
public class FaultRendererModel extends Model implements IFaultRendererConstants {

  private final FloatProperty _distanceTolerance;

  private final BooleanProperty _segmentsVisible;

  private final EnumProperty<LineStyle> _segmentsLineStyle;

  private final IntegerProperty _segmentsLineWidth;

  private final BooleanProperty _trianglesVisible;

  private final EnumProperty<LineStyle> _trianglesLineStyle;

  private final IntegerProperty _trianglesLineWidth;

  /**
   * The default constructor.
   */
  public FaultRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    IPreferenceStore preferences = FaultRendererPreferencePage.PREFERENCE_STORE;

    float tolerance = preferences.getFloat(DISTANCE_TOLERANCE);
    boolean picksVisible = preferences.getBoolean(SEGMENTS_VISIBLE);
    String picksLineStyleStr = preferences.getString(SEGMENTS_LINE_STYLE);
    LineStyle picksLineStyle = LineStyle.lookup(picksLineStyleStr);
    int picksLineWidth = preferences.getInt(SEGMENTS_LINE_WIDTH);
    boolean trianglesVisible = preferences.getBoolean(TRIANGLES_VISIBLE);
    String trianglesLineStyleStr = preferences.getString(TRIANGLES_LINE_STYLE);
    LineStyle trianglesLineStyle = LineStyle.lookup(trianglesLineStyleStr);
    int trianglesLineWidth = preferences.getInt(TRIANGLES_LINE_WIDTH);

    _distanceTolerance = addFloatProperty(DISTANCE_TOLERANCE, tolerance);
    _segmentsVisible = addBooleanProperty(SEGMENTS_VISIBLE, picksVisible);
    _segmentsLineStyle = addEnumProperty(SEGMENTS_LINE_STYLE, LineStyle.class, picksLineStyle);
    _segmentsLineWidth = addIntegerProperty(SEGMENTS_LINE_WIDTH, picksLineWidth);
    _trianglesVisible = addBooleanProperty(TRIANGLES_VISIBLE, trianglesVisible);
    _trianglesLineStyle = addEnumProperty(TRIANGLES_LINE_STYLE, LineStyle.class, trianglesLineStyle);
    _trianglesLineWidth = addIntegerProperty(TRIANGLES_LINE_WIDTH, trianglesLineWidth);
  }

  /**
   * The copy constructor.
   * @param model the fault renderer model to copy.
   */
  public FaultRendererModel(final FaultRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(final IValidation results) {
    // No validation.
  }

  /**
   * Returns the triangle line color.
   */
  public float getDisplayTolerance() {
    return _distanceTolerance.get();
  }

  /**
   * Returns the pick segments visibity.
   */
  public boolean getSegmentsVisible() {
    return _segmentsVisible.get();
  }

  /**
   * Returns the pick segments line style.
   */
  public LineStyle getSegmentsLineStyle() {
    return _segmentsLineStyle.get();
  }

  /**
   * Returns the pick segments line width.
   */
  public int getSegmentsLineWidth() {
    return _segmentsLineWidth.get();
  }

  /**
   * Returns the triangles visiblity.
   */
  public boolean getTrianglesVisible() {
    return _trianglesVisible.get();
  }

  /**
   * Returns the triangles line style.
   */
  public LineStyle getTrianglesLineStyle() {
    return _trianglesLineStyle.get();
  }

  /**
   * Returns the triangles line width.
   */
  public int getTrianglesLineWidth() {
    return _trianglesLineWidth.get();
  }

}
