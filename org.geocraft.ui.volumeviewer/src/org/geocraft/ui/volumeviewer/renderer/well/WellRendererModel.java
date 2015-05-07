/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.well;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;


/**
 * The model of display properties for rendering a well in 3D view.
 */
public class WellRendererModel extends Model implements IWellRendererConstants {

  /** The well bore rendering radius. */
  private final IntegerProperty _size;

  /** The well bore rendering color. */
  private final ColorProperty _color;

  /**
   * Constructs a well renderer model with default properties.
   */
  public WellRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    final IPreferenceStore preferences = WellRendererPreferencePage.PREFERENCE_STORE;

    final int boreRadius = preferences.getInt(WELL_BORE_RADIUS);
    final RGB boreColor = PreferenceConverter.getColor(preferences, WELL_BORE_COLOR);

    _size = addIntegerProperty(WELL_BORE_RADIUS, boreRadius);
    _color = addColorProperty(WELL_BORE_COLOR, boreColor);
  }

  /**
   * Constructs a well renderer model, copied from another.
   * 
   * @param model the renderer model to copy.
   */
  public WellRendererModel(final WellRendererModel model) {
    this();
    updateFrom(model);
  }

  /**
   * Gets the radius to render the well bore.
   * 
   * @return the well bore radius.
   */
  public int getBoreRadius() {
    return _size.get();
  }

  /**
   * Gets the color to render the well bore.
   * 
   * @return the well bore color.
   */
  public RGB getBoreColor() {
    return _color.get();
  }

  public void validate(final IValidation results) {
    // Validate the well bore radius is positive.
    final int size = _size.get();
    if (size <= 0) {
      results.error(WELL_BORE_RADIUS, "Invalid bore radius: " + size);
    } else if (size == 0) {
      results.warning(WELL_BORE_RADIUS, "Zero bore radius specified.");
    }

    // Validate the well bore color is non-null.
    if (_color.isNull()) {
      results.error(WELL_BORE_COLOR, "No bore color specified.");
    }
  }
}
