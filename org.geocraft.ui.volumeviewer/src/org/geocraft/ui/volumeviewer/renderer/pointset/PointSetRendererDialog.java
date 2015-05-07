/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.pointset;


import java.sql.Timestamp;
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
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.PointSetAttribute;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.form2.field.ComboField;


/**
 * Defines the dialog for editing the display settings of a <code>PointSet<code> entity.
 * These settings include point style and size, as well as which attribute to use for
 * the point color.
 */
public class PointSetRendererDialog extends PreferencesModelDialog implements IPointSetRendererConstants {

  /** The point set renderer. */
  private final PointSetRenderer _renderer;

  /** The color bar editor. */
  private ColorBarEditor _colorBarEditor;

  private ComboField _sizeAttributeField;

  private ComboField _colorAttributeField;

  private ComboField _thresholdAttributeField;

  public PointSetRendererDialog(final Shell shell, final String title, final PointSetRenderer renderer, final PointSet pointSet) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    final IModelForm form = forms[0];
    final PointSet pointSet = (PointSet) _renderer.getRenderedObjects()[0];
    final String[] attributeNames = getContinuousAttributeNames(pointSet);

    final FormSection section = form.addSection("Point Attributes", false);

    //section.addTextField(DECIMATION);

    section.addComboField(POINT_STYLE, new PointStyle[] { PointStyle.SPHERE });
    section.addTextField(POINT_SIZE);
    section.addCheckboxField(SIZE_BY_ATTRIBUTE);
    _sizeAttributeField = section.addComboField(SIZE_ATTRIBUTE, attributeNames);
    section.addTextField(SIZE_ATTRIBUTE_MIN);
    section.addTextField(SIZE_ATTRIBUTE_MAX);
    section.addTextField(POINT_SIZE_MIN);
    section.addTextField(POINT_SIZE_MAX);
    section.addColorField(POINT_COLOR);
    section.addCheckboxField(COLOR_BY_ATTRIBUTE);
    _colorAttributeField = section.addComboField(COLOR_ATTRIBUTE, attributeNames);

    //section = form.addSection("Threshold Attributes", false);
    //
    section.addCheckboxField(THRESHOLD_BY_ATTRIBUTE);
    _thresholdAttributeField = section.addComboField(IPointSetRendererConstants.THRESHOLD_ATTRIBUTE, attributeNames);
    section.addTextField(THRESHOLD_ATTRIBUTE_MIN);
    section.addTextField(THRESHOLD_ATTRIBUTE_MAX);

    // Create a color bar editor below the parameter form.
    final Composite container = form.createComposite("Attribute Color Bar", false);
    container.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));

    final ColorBar colorBar = ((PointSetRendererModel) _model).getColorBar();
    _colorBarEditor = new ColorBarEditor(container, colorBar);

    // Need to manually listen to the attribute combo, so that
    // the point size min,max values can be updated.
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

    // Need to manually listen to the attribute combo, so that
    // the threshold min,max values can be updated.
    final Combo thresholdAttributeCombo = _thresholdAttributeField.getCombo();

    thresholdAttributeCombo.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        thresholdAttributeSelected(pointSet, thresholdAttributeCombo);
      }

    });

    //    final Combo thresholdCombo = thresholdField.getCombo();
    //
    //    thresholdCombo.addListener(SWT.Selection, new Listener() {
    //
    //      @Override
    //      public void handleEvent(final Event event) {
    //        final PointSetRendererModel model = (PointSetRendererModel) _model;
    //        if (model == null) {
    //          return;
    //        }
    //        final int index = thresholdCombo.getSelectionIndex();
    //        float min = Float.MAX_VALUE;
    //        float max = -Float.MAX_VALUE;
    //        switch (index) {
    //          case 0: // None.
    //            break;
    //          case 1: // Z attribute.
    //            for (int i = 0; i < pointSet.getNumPoints(); i++) {
    //              final float value = (float) pointSet.getZ(i);
    //              min = Math.min(min, value);
    //              max = Math.max(max, value);
    //            }
    //            model.setThresholdMinValue(min);
    //            model.setThresholdMaxValue(max);
    //            break;
    //          case 2: // Other attribute.
    //            final String attribute = thresholdCombo.getItem(index);
    //            for (int i = 0; i < pointSet.getNumPoints(); i++) {
    //              final float value = pointSet.getAttribute(attribute, i);
    //              min = Math.min(min, value);
    //              max = Math.max(max, value);
    //            }
    //            model.setThresholdMinValue(min);
    //            model.setThresholdMaxValue(max);
    //            break;
    //        }
    //
    //      }
    //
    //    });

    sizeAttributeSelected(pointSet, sizeAttributeCombo);
    colorAttributeSelected(pointSet, colorBar, colorAttributeCombo);
    thresholdAttributeSelected(pointSet, thresholdAttributeCombo);

    final FormToolkit toolkit = _managedForm.getToolkit();
    toolkit.adapt(container);
    _colorBarEditor.adapt(toolkit);
  }

  /**
   * @param pointSet
   * @param sizeCombo
   */
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
        final String sizeAttrName = sizeCombo.getItem(index);
        final PointSetAttribute sizeAttr = pointSet.getAttribute(sizeAttrName);
        switch (sizeAttr.getType()) {
          case SHORT:
          case INTEGER:
          case LONG:
          case FLOAT:
          case DOUBLE:
            for (int i = 0; i < pointSet.getNumPoints(); i++) {
              final float value = sizeAttr.getFloat(i);
              min = Math.min(min, value);
              max = Math.max(max, value);
            }
            model.setSizeAttributeMin(min);
            model.setSizeAttributeMax(max);
            break;
          case STRING:
          case TIMESTAMP:
            break;
        }
        break;
    }
  }

  /**
   * @param pointSet
   * @param colorBar
   * @param colorCombo
   */
  private void colorAttributeSelected(final PointSet pointSet, final ColorBar colorBar, final Combo colorCombo) {
    final int index = colorCombo.getSelectionIndex();
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
        colorBar.setStartValue(min);
        colorBar.setEndValue(max);
        final Labels zLabels = new Labels(min, max, 10);
        colorBar.setStepValue(zLabels.getIncrement());
        _colorBarEditor.updated();
        break;
      default: // Other attribute.
        final String colorAttrName = colorCombo.getItem(index);
        final PointSetAttribute colorAttr = pointSet.getAttribute(colorAttrName);
        switch (colorAttr.getType()) {
          case SHORT:
          case INTEGER:
          case LONG:
          case FLOAT:
          case DOUBLE:
            for (int i = 0; i < pointSet.getNumPoints(); i++) {
              final float value = colorAttr.getFloat(i);
              min = Math.min(min, value);
              max = Math.max(max, value);
            }
            colorBar.setStartValue(min);
            colorBar.setEndValue(max);
            final Labels labels = new Labels(min, max, 10);
            colorBar.setStepValue(labels.getIncrement());
            _colorBarEditor.updated();
            break;
          case STRING:
            break;
          case TIMESTAMP:
            for (int i = 0; i < pointSet.getNumPoints(); i++) {
              final Timestamp value = colorAttr.getTimestamp(i);
              min = Math.min(min, value.getTime());
              max = Math.max(max, value.getTime());
            }
            colorBar.setStartValue(min);
            colorBar.setEndValue(max);
            final Labels labels2 = new Labels(min, max, 10);
            colorBar.setStepValue(labels2.getIncrement());
            _colorBarEditor.updated();
            break;
        }
        break;
    }
  }

  private void thresholdAttributeSelected(final PointSet pointSet, final Combo thresholdCombo) {
    final PointSetRendererModel model = (PointSetRendererModel) _model;
    final int index = thresholdCombo.getSelectionIndex();
    if (index == -1) {
      return;
    }
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    switch (index) {
      case 0: // Z attribute.
        for (int i = 0; i < pointSet.getNumPoints(); i++) {
          final double value = (float) pointSet.getZ(i);
          min = Math.min(min, value);
          max = Math.max(max, value);
        }
        model.setThresholdAttributeMin(min);
        model.setThresholdAttributeMax(max);
        break;
      default: // Other attribute.
        final String thresholdAttrName = thresholdCombo.getItem(index);
        final PointSetAttribute thresholdAttr = pointSet.getAttribute(thresholdAttrName);
        switch (thresholdAttr.getType()) {
          case SHORT:
          case INTEGER:
          case LONG:
          case FLOAT:
          case DOUBLE:
            for (int i = 0; i < pointSet.getNumPoints(); i++) {
              final double value = thresholdAttr.getFloat(i);
              min = Math.min(min, value);
              max = Math.max(max, value);
            }
            model.setThresholdAttributeMin(min);
            model.setThresholdAttributeMax(max);
            break;
          case STRING:
            break;
          case TIMESTAMP:
            for (int i = 0; i < pointSet.getNumPoints(); i++) {
              final Timestamp value = thresholdAttr.getTimestamp(i);
              min = Math.min(min, value.getTime());
              max = Math.max(max, value.getTime());
            }
            model.setThresholdAttributeMin(min);
            model.setThresholdAttributeMax(max);
            break;
        }
        break;
    }
  }

  @Override
  public void modelFormUpdated(final String key) {
    super.modelFormUpdated(key);
    final PointSetRendererModel model = (PointSetRendererModel) _model;
    final PointSet pointSet = (PointSet) _renderer.getRenderedObjects()[0];
    if (key == COLOR_BY_ATTRIBUTE) {
      final boolean colorByAttribute = model.getColorByAttribute();
      setFieldEnabled(POINT_COLOR, !colorByAttribute);
      setFieldEnabled(COLOR_ATTRIBUTE, colorByAttribute);
      _colorBarEditor.setEnabled(colorByAttribute);
    } else if (key == COLOR_ATTRIBUTE) {
      colorAttributeSelected(pointSet, _colorBarEditor.getColorBar(), _colorAttributeField.getCombo());
    } else if (key == SIZE_BY_ATTRIBUTE) {
      final boolean sizeByAttribute = model.getSizeByAttribute();
      setFieldEnabled(POINT_SIZE, !sizeByAttribute);
      setFieldEnabled(SIZE_ATTRIBUTE, sizeByAttribute);
      setFieldEnabled(SIZE_ATTRIBUTE_MIN, sizeByAttribute);
      setFieldEnabled(SIZE_ATTRIBUTE_MAX, sizeByAttribute);
      setFieldEnabled(POINT_SIZE_MIN, sizeByAttribute);
      setFieldEnabled(POINT_SIZE_MAX, sizeByAttribute);
    } else if (key == SIZE_ATTRIBUTE) {
      sizeAttributeSelected(pointSet, _sizeAttributeField.getCombo());
    } else if (key == THRESHOLD_BY_ATTRIBUTE) {
      final boolean thresholdByAttribute = model.getThresholdByAttribute();
      setFieldEnabled(THRESHOLD_ATTRIBUTE, thresholdByAttribute);
      setFieldEnabled(THRESHOLD_ATTRIBUTE_MIN, thresholdByAttribute);
      setFieldEnabled(THRESHOLD_ATTRIBUTE_MAX, thresholdByAttribute);
    } else if (key == THRESHOLD_ATTRIBUTE) {
      thresholdAttributeSelected(pointSet, _thresholdAttributeField.getCombo());
    }
  }

  public String[] getContinuousAttributeNames(final PointSet pointSet) {
    final String[] attributeNames = pointSet.getAttributeNames();
    final List<String> results = new ArrayList<String>();
    results.add(Z_ATTRIBUTE);
    for (final String attributeName : attributeNames) {
      final PointSetAttribute attr = pointSet.getAttribute(attributeName);
      switch (attr.getType()) {
        case SHORT:
        case INTEGER:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case TIMESTAMP:
          results.add(attributeName);
          break;
        case STRING:
          break;
      }
    }
    return results.toArray(new String[0]);
  }

  @Override
  protected IModel createModel() {
    return new PointSetRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    // Apply the settings to the renderer.
    final ColorBar colorBar = ((PointSetRendererModel) _model).getColorBar();
    colorBar.setStartValue(_colorBarEditor.getStartValue());
    colorBar.setEndValue(_colorBarEditor.getEndValue());
    _renderer.updateRendererModel((PointSetRendererModel) _model);
    //_renderer.updateSettings((PointSetRendererModel) _model, colorBar);
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
    final PointSetRendererModel model = (PointSetRendererModel) _model;

    // Update the default renderer settings to the preferences.
    final IPreferenceStore preferences = PointSetRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(DECIMATION, 1);
    preferences.setValue(POINT_SIZE, model.getPointSize());
    preferences.setValue(SIZE_BY_ATTRIBUTE, model.getSizeByAttribute());
    preferences.setValue(POINT_SIZE_MIN, model.getPointSizeMin());
    preferences.setValue(POINT_SIZE_MAX, model.getPointSizeMax());
    preferences.setValue(POINT_STYLE, model.getPointStyle().getName());
    PreferenceConverter.setValue(preferences, POINT_COLOR, model.getPointColor());
    preferences.setValue(COLOR_BY_ATTRIBUTE, model.getColorByAttribute());
    preferences.setValue(THRESHOLD_BY_ATTRIBUTE, model.getThresholdByAttribute());
    //preferences.setValue(COLOR_MAP, colorMap);
  }
}