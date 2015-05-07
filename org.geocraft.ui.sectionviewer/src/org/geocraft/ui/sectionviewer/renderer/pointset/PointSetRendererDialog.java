/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.pointset;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.PointSet;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


/**
 * Defines the dialog for editing the display settings of a <code>PointSet<code> entity.
 * These settings include point style and size, as well as which property to use for
 * the point color.
 */
public class PointSetRendererDialog extends PreferencesModelDialog implements IPointSetRendererConstants {

  /** The point set renderer. */
  private final PointSetRenderer _renderer;

  /** The color bar editor. */
  private ColorBarEditor _colorBarEditor;

  public PointSetRendererDialog(final Shell shell, final String title, final PointSetRenderer renderer) {
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
    final PointSet pointSet = (PointSet) _renderer.getRenderedObjects()[0];

    FormSection section = form.addSection("Point Attributes");

    ComboField comboField1 = section.addComboField(COLOR_ATTRIBUTE, getAttributeNames(pointSet));
    section.addComboField(POINT_STYLE, PointStyle.values());
    section.addTextField(POINT_SIZE);
    section.addColorField(POINT_COLOR);

    section = form.addSection("Threshold Attributes");

    section.addComboField(THRESHOLD_ATTRIBUTE, getAttributeNames(pointSet));
    section.addTextField(THRESHOLD_MIN_VALUE);
    section.addTextField(THRESHOLD_MAX_VALUE);

    section = form.addSection("Connection Attributes");

    section.addComboField(CONNECTION_ATTRIBUTE, getAttributeNames(pointSet));
    section.addComboField(LINE_STYLE, LineStyle.values());
    section.addTextField(LINE_WIDTH);
    section.addColorField(LINE_COLOR);

    // Create a color bar editor below the parameter form.
    Composite container = form.createComposite("Attribute Color Bar");
    container.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));

    final ColorBar colorBar = ((PointSetRendererModel) _model).getColorBar();
    _colorBarEditor = new ColorBarEditor(container, colorBar);

    // Need to manually listen to the attribute combo, so that
    // the start and end values can be updated in the color bar.
    final Combo combo = comboField1.getCombo();

    combo.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        String attribute = combo.getItem(combo.getSelectionIndex());
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int i = 0; i < pointSet.getNumPoints(); i++) {
          float value = pointSet.getAttribute(attribute).getFloat(i);
          min = Math.min(min, value);
          max = Math.max(max, value);
        }
        colorBar.setStartValue(min);
        colorBar.setEndValue(max);
        Labels labels = new Labels(min, max, 10);
        colorBar.setStepValue(labels.getIncrement());
        _colorBarEditor.updated();
      }

    });
    FormToolkit toolkit = _managedForm.getToolkit();
    toolkit.adapt(container);
    _colorBarEditor.adapt(toolkit);
  }

  public String[] getAttributeNames(final PointSet pointSet) {
    String[] attributeNames = pointSet.getAttributeNames();
    String[] results = new String[1 + attributeNames.length];
    results[0] = NO_ATTRIBUTE;
    System.arraycopy(attributeNames, 0, results, 1, attributeNames.length);
    return results;
  }

  @Override
  protected IModel createModel() {
    return new PointSetRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    // Apply the settings to the renderer.
    ColorBar colorBar = ((PointSetRendererModel) _model).getColorBar();
    colorBar.setStartValue(_colorBarEditor.getStartValue());
    colorBar.setEndValue(_colorBarEditor.getEndValue());
    _renderer.updateSettings((PointSetRendererModel) _model, colorBar);
  }

  @Override
  public boolean close() {
    if (_colorBarEditor != null) {
      _colorBarEditor.dispose();
    }
    return super.close();
  }

  @Override
  protected void updatePreferences() {
    PointSetRendererModel model = (PointSetRendererModel) _model;

    // Update the default renderer settings to the preferences.
    IPreferenceStore preferences = PointSetRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(POINT_SIZE, model.getPointSize());
    preferences.setValue(POINT_STYLE, model.getPointStyle().getName());
    PreferenceConverter.setValue(preferences, POINT_COLOR, model.getPointColor());
    preferences.setValue(LINE_WIDTH, model.getLineWidth());
    preferences.setValue(LINE_STYLE, model.getLineStyle().getName());
    PreferenceConverter.setValue(preferences, LINE_COLOR, model.getLineColor());
  }
}
