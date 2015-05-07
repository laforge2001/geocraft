/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.fault;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;


/**
 * Defines the model of properties used to renderer a <code>FaultInterpretation</code> entity
 * in the 3D viewer.
 */
public final class FaultRendererModel extends Model {

  /** The key constant for the segment visibility property. */
  public static final String SHOW_SEGMENTS = "Show Segments";

  /** The key constant for the segment color property. */
  public static final String SEGMENT_COLOR = "Segment Color";

  /** The key constant for the segment width property. */
  public static final String SEGMENT_WIDTH = "Segment Width";

  /** The key constant for the triangulation visibility property. */
  public static final String SHOW_TRIANGULATION = "Show Triangulation";

  /** The key constant for the triangle color property. */
  public static final String TRIANGLE_COLOR = "Triangle Color";

  /** The segment visibility property. */
  private final BooleanProperty _showSegments;

  /** The segment color property. */
  private final ColorProperty _segmentColor;

  /** The segment width property. */
  private final IntegerProperty _segmentWidth;

  /** The triangulation visibility property. */
  private final BooleanProperty _showTriangulation;

  /** The triangle color property. */
  private final ColorProperty _triangleColor;

  /**
   * Constructs a renderer model with default settings.
   */
  public FaultRendererModel() {
    _showSegments = addBooleanProperty(SHOW_SEGMENTS, true);
    _segmentColor = addColorProperty(SEGMENT_COLOR, new RGB(255, 0, 0));
    _segmentWidth = addIntegerProperty(SEGMENT_WIDTH, 1);
    _showTriangulation = addBooleanProperty(SHOW_TRIANGULATION, true);
    _triangleColor = addColorProperty(TRIANGLE_COLOR, new RGB(255, 0, 0));
  }

  /**
   * Constructs a renderer model, copied from another.
   * 
   * @param model the model from which to copy properties.
   */
  public FaultRendererModel(final FaultRendererModel model) {
    this();
    updateFrom(model);
  }

  /**
   * Gets the segment visibility flag.
   * 
   * @return <i>true</i> to show segments; <i>false</i> to hide.
   */
  public boolean getShowSegments() {
    return _showSegments.get();
  }

  /**
   * Sets the segment visibility flag.
   * 
   * @param showSegments <i>true</i> to show segments; <i>false</i> to hide.
   */
  public void setShowSegments(final boolean showSegments) {
    _showSegments.set(showSegments);
  }

  /**
   * Gets the color for rendering segments.
   * 
   * @return the segment color.
   */
  public RGB getSegmentColor() {
    return _segmentColor.get();
  }

  /**
   * Sets the color for rendering segments.
   * 
   * @param segmentColor the segment color.
   */
  public void setSegmentColor(final RGB segmentColor) {
    _segmentColor.set(segmentColor);
  }

  /**
   * Gets the width for rendering segments.
   * 
   * @return the segment width.
   */
  public int getSegmentWidth() {
    return _segmentWidth.get();
  }

  /**
   * Sets the width for rendering segments.
   * 
   * @param segmentWidth the segment width.
   */
  public void setSegmentWidth(final int segmentWidth) {
    _segmentWidth.set(segmentWidth);
  }

  /**
   * Gets the triangulation visibility flag.
   * 
   * @return <i>true</i> to show triangulation; <i>false</i> to hide.
   */
  public boolean getShowTriangulation() {
    return _showTriangulation.get();
  }

  /**
   * Sets the triangulation visibility flag.
   * 
   * @param showTriangulation <i>true</i> to show triangulation; <i>false</i> to hide.
   */
  public void setShowTriangulation(final boolean showTriangulation) {
    _showTriangulation.set(showTriangulation);
  }

  /**
   * Gets the color for rendering triangles.
   * 
   * @return the triangle color.
   */
  public RGB getTriangleColor() {
    return _triangleColor.get();
  }

  /**
   * Sets the color for rendering triangles.
   * 
   * @param triangleColor the triangle color.
   */
  public void setTriangleColor(final RGB triangleColor) {
    _triangleColor.set(triangleColor);
  }

  public void validate(final IValidation results) {
    // Validate the segment color is non-null.
    if (_segmentColor.isNull()) {
      results.error(SEGMENT_COLOR, "No segment color specified.");
    }

    // Validate the segment width is positive.
    final int segmentWidth = _segmentWidth.get();
    if (segmentWidth < 1) {
      results.error(SEGMENT_WIDTH, "Invalid segment width: " + _segmentWidth);
    }

    // Validate the triangle color is non-null.
    if (_triangleColor.isNull()) {
      results.error(TRIANGLE_COLOR, "No triangle color specified.");
    }
  }
}
