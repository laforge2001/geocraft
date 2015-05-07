/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.pointset;


import java.util.ArrayList;
import java.util.List;

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
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.PointSet;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.form2.field.ComboField;
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

  private ComboField _sizeAttributeField;

  private ComboField _colorAttributeField;

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

    List<String> attributeNames = new ArrayList<String>();
    attributeNames.add(Z_ATTRIBUTE);
    for (String attributeName : pointSet.getAttributeNames()) {
      attributeNames.add(attributeName);
    }

    section.addComboField(POINT_STYLE, PointStyle.values());
    section.addTextField(POINT_SIZE);
    section.addCheckboxField(SIZE_BY_ATTRIBUTE);
    _sizeAttributeField = section.addComboField(SIZE_ATTRIBUTE, attributeNames.toArray(new String[0]));
    section.addTextField(SIZE_ATTRIBUTE_MIN);
    section.addTextField(SIZE_ATTRIBUTE_MAX);
    section.addTextField(POINT_SIZE_MIN);
    section.addTextField(POINT_SIZE_MAX);
    section.addColorField(POINT_COLOR);
    section.addCheckboxField(COLOR_BY_ATTRIBUTE);
    _colorAttributeField = section.addComboField(COLOR_ATTRIBUTE, attributeNames.toArray(new String[0]));

    // Create a color bar editor below the parameter form.
    Composite container = form.createComposite("Attribute Color Bar");
    container.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));

    final ColorBar colorBar = ((PointSetRendererModel) _model).getColorBar();
    _colorBarEditor = new ColorBarEditor(container, colorBar);

    // Need to manually listen to the attribute combo, so that
    // the start and end values can be updated in the color bar.
    final Combo sizeAttributeCombo = _sizeAttributeField.getCombo();

    sizeAttributeCombo.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        sizeAttributeSelected(pointSet, sizeAttributeCombo);
      }

    });

    // Need to manually listen to the attribute combo, so that
    // the start and end values can be updated in the color bar.
    final Combo colorAttributeCombo = _colorAttributeField.getCombo();

    colorAttributeCombo.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        colorAttributeSelected(pointSet, colorBar, colorAttributeCombo);
      }

    });

    sizeAttributeSelected(pointSet, sizeAttributeCombo);
    colorAttributeSelected(pointSet, colorBar, colorAttributeCombo);

    FormToolkit toolkit = _managedForm.getToolkit();
    toolkit.adapt(container);
    _colorBarEditor.adapt(toolkit);
  }

  private void sizeAttributeSelected(final PointSet pointSet, final Combo sizeCombo) {
    final PointSetRendererModel model = (PointSetRendererModel) _model;
    final int index = sizeCombo.getSelectionIndex();
    if (index == -1) {
      return;
    }
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    switch (index) {
      case 0: // Z attribute.
        for (int i = 0; i < pointSet.getNumPoints(); i++) {
          final float value = (float) pointSet.getZ(i);
          min = Math.min(min, value);
          max = Math.max(max, value);
        }
        model.setSizeAttributeMin(min);
        model.setSizeAttributeMax(max);
        break;
      default: // Other attribute.
        final String attribute = sizeCombo.getItem(index);
        for (int i = 0; i < pointSet.getNumPoints(); i++) {
          final float value = pointSet.getAttribute(attribute).getFloat(i);
          min = Math.min(min, value);
          max = Math.max(max, value);
        }
        model.setSizeAttributeMin(min);
        model.setSizeAttributeMax(max);
        break;
    }
  }

  /**
  * @param pointSet
  * @param colorBar
  * @param combo
  */
  private void colorAttributeSelected(final PointSet pointSet, final ColorBar colorBar, final Combo combo) {
    int index = combo.getSelectionIndex();
    if (index == -1) {
      return;
    }
    String attribute = combo.getItem(index);
    PointSetRendererModel.updateColorBarRangeBasedOnAttribute(pointSet, colorBar, attribute);
    _colorBarEditor.updated();
  }

  @Override
  public void modelFormUpdated(final String key) {
    super.modelFormUpdated(key);
    PointSetRendererModel model = (PointSetRendererModel) _model;
    PointSet pointSet = (PointSet) _renderer.getRenderedObjects()[0];
    if (key == COLOR_BY_ATTRIBUTE || key == COLOR_ATTRIBUTE) {
      boolean colorByAttribute = model.getColorByAttribute();
      setFieldEnabled(POINT_COLOR, !colorByAttribute);
      setFieldEnabled(COLOR_ATTRIBUTE, colorByAttribute);
      colorAttributeSelected(pointSet, _colorBarEditor.getColorBar(), _colorAttributeField.getCombo());
    } else if (key == SIZE_BY_ATTRIBUTE || key == SIZE_ATTRIBUTE) {
      boolean sizeByAttribute = model.getSizeByAttribute();
      setFieldEnabled(POINT_SIZE, !sizeByAttribute);
      setFieldEnabled(SIZE_ATTRIBUTE, sizeByAttribute);
      setFieldEnabled(SIZE_ATTRIBUTE_MIN, sizeByAttribute);
      setFieldEnabled(SIZE_ATTRIBUTE_MAX, sizeByAttribute);
      setFieldEnabled(POINT_SIZE_MIN, sizeByAttribute);
      setFieldEnabled(POINT_SIZE_MAX, sizeByAttribute);
      sizeAttributeSelected(pointSet, _sizeAttributeField.getCombo());
    }
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
    preferences.setValue(SIZE_BY_ATTRIBUTE, model.getSizeAttribute());
    preferences.setValue(SIZE_ATTRIBUTE_MIN, model.getSizeAttributeMin());
    preferences.setValue(SIZE_ATTRIBUTE_MAX, model.getSizeAttributeMax());
    preferences.setValue(POINT_SIZE_MIN, model.getPointSizeMin());
    preferences.setValue(POINT_SIZE_MAX, model.getPointSizeMax());
    preferences.setValue(POINT_STYLE, model.getPointStyle().getName());
    PreferenceConverter.setValue(preferences, POINT_COLOR, model.getPointColor());
    preferences.setValue(COLOR_BY_ATTRIBUTE, model.getColorByAttribute());
    //preferences.setValue(COLOR_MAP, colorMap);
  }
}
