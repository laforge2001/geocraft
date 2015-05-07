/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.fault;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.form2.field.TextField;
import org.geocraft.ui.plot.defs.LineStyle;


/**
 * Settings dialog for the section view fault renderer.
 */
public class FaultRendererDialog extends PreferencesModelDialog implements IFaultRendererConstants {

  /** The fault renderer. */
  private final FaultRenderer _renderer;

  public FaultRendererDialog(final Shell shell, final String title, final FaultRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    IModelForm form = forms[0];
    FormSection section = form.addSection("Pick Segments Attributes", false);
    section.addCheckboxField(SEGMENTS_VISIBLE);
    section.addComboField(SEGMENTS_LINE_STYLE, LineStyle.values());
    section.addTextField(SEGMENTS_LINE_WIDTH);
    TextField toleranceField = section.addTextField(DISTANCE_TOLERANCE);
    Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    toleranceField.setLabel(DISTANCE_TOLERANCE + " (" + xyUnit.getSymbol() + ")");

    section = form.addSection("Triangle Attributes", false);
    section.addCheckboxField(TRIANGLES_VISIBLE);
    section.addComboField(TRIANGLES_LINE_STYLE, LineStyle.values());
    section.addTextField(TRIANGLES_LINE_WIDTH);
  }

  @Override
  protected IModel createModel() {
    return new FaultRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    // Apply the settings to the renderer.
    _renderer.updateSettings((FaultRendererModel) _model);
  }

  @Override
  protected void updatePreferences() {
    FaultRendererModel model = (FaultRendererModel) _model;

    // Update the default renderer settings to the preferences.
    IPreferenceStore preferences = FaultRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(DISTANCE_TOLERANCE, model.getDisplayTolerance());
    preferences.setValue(SEGMENTS_VISIBLE, model.getSegmentsVisible());
    preferences.setValue(SEGMENTS_LINE_WIDTH, model.getSegmentsLineWidth());
    preferences.setValue(SEGMENTS_LINE_STYLE, model.getSegmentsLineStyle().getName());
    preferences.setValue(TRIANGLES_VISIBLE, model.getTrianglesVisible());
    preferences.setValue(TRIANGLES_LINE_WIDTH, model.getTrianglesLineWidth());
    preferences.setValue(TRIANGLES_LINE_STYLE, model.getTrianglesLineStyle().getName());
  }
}
