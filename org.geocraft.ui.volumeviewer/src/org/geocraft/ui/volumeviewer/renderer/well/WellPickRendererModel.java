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
 * The model of display properties for rendering a well pick in 3D view.
 */
public class WellPickRendererModel extends Model implements IWellRendererConstants {

  /** The well pick rendering radius. */
  private final IntegerProperty _size;

  /** The well pick rendering color. */
  private final ColorProperty _color;

  /**
   * Constructs a well pick renderer model with default properties.
   */
  public WellPickRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    final IPreferenceStore preferences = WellRendererPreferencePage.PREFERENCE_STORE;

    final int pickRadius = preferences.getInt(WELL_PICK_RADIUS);
    final RGB pickColor = PreferenceConverter.getColor(preferences, WELL_PICK_COLOR);

    _size = addIntegerProperty(WELL_PICK_RADIUS, pickRadius);
    _color = addColorProperty(WELL_PICK_COLOR, pickColor);
  }

  /**
   * Constructs a well pick renderer model, copied from another.
   * 
   * @param model the renderer model to copy.
   */
  public WellPickRendererModel(final WellPickRendererModel model) {
    this();
    updateFrom(model);
  }

  /**
   * Gets the radius to render the well pick.
   * 
   * @return the well pick radius.
   */
  public int getPickRadius() {
    return _size.get();
  }

  /**
   * Gets the color to render the well pick.
   * 
   * @return the well pick color.
   */
  public RGB getPickColor() {
    return _color.get();
  }

  public void validate(final IValidation results) {
    // Validate the well pick radius is positive.
    final int size = _size.get();
    if (size <= 0) {
      results.error(WELL_PICK_RADIUS, "Invalid pick radius: " + size);
    } else if (size == 0) {
      results.warning(WELL_PICK_RADIUS, "Zero pick radius specified.");
    }

    // Validate the well pick color is non-null.
    if (_color.isNull()) {
      results.error(WELL_PICK_COLOR, "No pick color specified.");
    }
  }

}
