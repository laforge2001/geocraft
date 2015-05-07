/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.well;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;


public class WellRendererModel extends Model implements IWellRendererConstants {

  private final FloatProperty _displayTolerance;

  private final BooleanProperty _showBoreLabels;

  private final IntegerProperty _boreWidth;

  private final ColorProperty _boreColor;

  private final BooleanProperty _showPicks;

  private final ColorProperty _pickColor;

  private final BooleanProperty _showPickLabels;

  private final ColorProperty _pickLabelColor;

  private final BooleanProperty _showLogs;

  public WellRendererModel() {
    // Initialize the default renderer settings from the preferences.
    IPreferenceStore preferences = WellRendererPreferencePage.PREFERENCE_STORE;

    float displayTolerance = preferences.getInt(DISTANCE_TOLERANCE);
    boolean showWellBoreLabels = preferences.getBoolean(SHOW_BORE_LABELS);
    int wellBoreWidth = preferences.getInt(BORE_LINE_WIDTH);
    RGB wellBoreColor = PreferenceConverter.getColor(preferences, BORE_LINE_COLOR);
    boolean showPicks = preferences.getBoolean(SHOW_PICKS);
    boolean showPickLabels = preferences.getBoolean(SHOW_PICK_LABELS);
    RGB wellPickSymbolColor = PreferenceConverter.getColor(preferences, PICK_SYMBOL_COLOR);
    RGB wellPickLabelColor = PreferenceConverter.getColor(preferences, PICK_LABEL_COLOR);

    _displayTolerance = addFloatProperty(DISTANCE_TOLERANCE, displayTolerance);
    _showBoreLabels = addBooleanProperty(SHOW_BORE_LABELS, showWellBoreLabels);
    _boreWidth = addIntegerProperty(BORE_LINE_WIDTH, wellBoreWidth);
    _boreColor = addColorProperty(BORE_LINE_COLOR, wellBoreColor);
    _showPicks = addBooleanProperty(SHOW_PICKS, showPicks);
    _pickColor = addColorProperty(PICK_SYMBOL_COLOR, wellPickSymbolColor);
    _showPickLabels = addBooleanProperty(SHOW_PICK_LABELS, showPickLabels);
    _pickLabelColor = addColorProperty(PICK_LABEL_COLOR, wellPickLabelColor);
    _showLogs = addBooleanProperty(SHOW_LOGS, false);
  }

  public WellRendererModel(final WellRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(final IValidation results) {
    if (_displayTolerance.get() < 0) {
      results.error(_displayTolerance, "Tolerance cannot be negative.");
    }

    if (_boreWidth.get() < 0) {
      results.error(_boreWidth, "Bore width cannot be negative.");
    }

    if (_boreColor.isNull()) {
      results.error(_boreColor, "Bore color not specified.");
    }

    if (_pickColor.isNull()) {
      results.error(_pickColor, "Pick color not specified.");
    }

    if (_pickLabelColor.isNull()) {
      results.error(_pickLabelColor, "Pick label color not specified.");
    }
  }

  public float getDisplayTolerance() {
    return _displayTolerance.get();
  }

  public boolean getShowBoreLabels() {
    return _showBoreLabels.get();
  }

  public int getBoreWidth() {
    return _boreWidth.get();
  }

  public RGB getBoreColor() {
    return _boreColor.get();
  }

  public boolean getShowPicks() {
    return _showPicks.get();
  }

  public RGB getPickColor() {
    return _pickColor.get();
  }

  public boolean getShowPickLabels() {
    return _showPickLabels.get();
  }

  public RGB getPickLabelColor() {
    return _pickLabelColor.get();
  }

  public boolean getShowLogs() {
    return _showLogs.get();
  }

}
