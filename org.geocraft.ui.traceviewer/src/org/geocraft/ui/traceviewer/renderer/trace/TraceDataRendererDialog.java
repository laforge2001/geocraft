/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.traceviewer.renderer.trace;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.common.math.AGC;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.ColorField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.SpinnerField;
import org.geocraft.ui.sectionviewer.InterpolationMethod;
import org.geocraft.ui.sectionviewer.NormalizationMethod;
import org.geocraft.ui.sectionviewer.renderer.seismic.ISeismicDatasetRendererConstants;
import org.geocraft.ui.sectionviewer.renderer.seismic.SeismicDatasetRendererPreferencePage;


public class TraceDataRendererDialog extends PreferencesModelDialog implements ISeismicDatasetRendererConstants,
    ColorMapListener {

  /** The associated trace renderer. */
  protected final TraceDataRenderer _renderer;

  /** The color bar editor. */
  protected ColorBarEditor _overlayColorBarEditor;

  protected ColorBarEditor _underlayColorBarEditor;

  public TraceDataRendererDialog(final Shell shell, final String title, final TraceDataRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected int getNumForms() {
    return 3;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    IModelForm form = forms[0];
    form.setTitle("Rendering");

    FormSection generalSection = form.addSection("General", false);

    ComboField interpolationField = generalSection.addComboField(INTERPOLATION_METHOD, InterpolationMethod.values());
    interpolationField.setTooltip("Select the interpolation method used in the vertical direction.");

    ComboField normalizationField = generalSection.addComboField(NORMALIZATION_METHOD, NormalizationMethod.values());
    normalizationField.setTooltip("Select the normalization method used for coloring traces.");

    generalSection.addTextField(TRACE_EXAGGERATION);

    generalSection.addSpinnerField(TRACE_CLIPPING, 1, 10, 0, 1);

    SpinnerField percentileField = generalSection.addSpinnerField(PERCENTILE, 0, 50, 0, 1);
    percentileField.setTooltip("Reduces the min/max range of data. Used to eliminate spikes, etc.");

    SpinnerField transparencyField = generalSection.addSpinnerField(TRANSPARENCY, 0, 100, 0, 1);
    transparencyField
        .setTooltip("Select the level of transparency for the traces (0=fully opaque, 100=fully transparent).");

    CheckboxField polarityField = generalSection.addCheckboxField(REVERSE_POLARITY);
    polarityField.setTooltip("Switches the displayed polarity of the trace samples.");

    FormSection renderSection = form.addSection("Styles", false);

    CheckboxField wiggleField = renderSection.addCheckboxField(WIGGLE_TRACE);
    wiggleField.setTooltip("Displays a wiggle rendering of the traces.");

    CheckboxField posColorFillField = renderSection.addCheckboxField(POSITIVE_COLOR_FILL);
    posColorFillField.setTooltip("Displays a solid color fill rendering for the positive lobes of the traces.");

    CheckboxField negColorFillField = renderSection.addCheckboxField(NEGATIVE_COLOR_FILL);
    negColorFillField.setTooltip("Displays a solid color fill rendering for the negative lobes of the traces.");

    CheckboxField posDensityFillField = renderSection.addCheckboxField(POSITIVE_DENSITY_FILL);
    posDensityFillField.setTooltip("Displays a color density fill rendering for the positive lobes of the traces.");

    CheckboxField negDensityFillField = renderSection.addCheckboxField(NEGATIVE_DENSITY_FILL);
    negDensityFillField.setTooltip("Displays a color density fill rendering for the negative lobes of the traces.");

    CheckboxField varDensityField = renderSection.addCheckboxField(VARIABLE_DENSITY);
    varDensityField.setTooltip("Displays a variable color density rendering of the traces.");

    FormSection colorsSection = form.addSection("Colors", false);

    ColorField wiggleColorField = colorsSection.addColorField(COLOR_WIGGLE);
    wiggleColorField.setTooltip("Select the color to use for wiggle rendering of the traces.");

    ColorField posFillColorField = colorsSection.addColorField(COLOR_POSITIVE_FILL);
    posFillColorField.setTooltip("Select the color to use for positive color fill rendering of the traces.");

    ColorField negFillColorField = colorsSection.addColorField(COLOR_NEGATIVE_FILL);
    negFillColorField.setTooltip("Select the color to use for negative color fill rendering of the traces.");

    form = forms[1];
    form.setTitle("Color Bar");

    //FormSection colorbarSection = form.addSection("Color Bar", false);

    // Create a color bar editor below the parameter form.
    //Composite container = new Composite(colorbarSection.getComposite(), SWT.NONE);
    Composite container = form.createComposite("Color Bar", false);
    container.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));

    _overlayColorBarEditor = new ColorBarEditor(container, ((TraceDataRendererModel) _model).getColorBar(), 384);

    FormToolkit toolkit = _managedForm.getToolkit();
    toolkit.adapt(container);
    _overlayColorBarEditor.adapt(toolkit);

    form = forms[2];
    form.setTitle("AGC and Geometric Gain");

    FormSection agcSection = form.addSection("AGC");
    agcSection.addCheckboxField(AGC_APPLY);
    agcSection.addComboField(AGC_TYPE, AGC.Type.values());
    agcSection.addTextField(AGC_WINDOW_LENGTH);

    FormSection gainSection = form.addSection("Geometric Gain");
    gainSection.addCheckboxField(GEOMETRIC_GAIN_APPLY);
    gainSection.addTextField(GEOMETRIC_GAIN_T0);
    gainSection.addTextField(GEOMETRIC_GAIN_N);
    gainSection.addTextField(GEOMETRIC_GAIN_TMAX);
  }

  @Override
  public void propertyChanged(final String key) {
    TraceDataRendererModel model = (TraceDataRendererModel) _model;
    if (key.equals(NORMALIZATION_METHOD)) {
      NormalizationMethod normalization = model.getNormalizationMethod();
      boolean showPercentile = normalization.equals(NormalizationMethod.BY_MAXIMUM);
      setFieldEnabled(PERCENTILE, showPercentile);
    }
    super.propertyChanged(key);
  }

  /**
   * Set the current display settings.
   */
  @Override
  protected void applySettings() {
    TraceDataRendererModel model = (TraceDataRendererModel) _model;

    double normalizationMin = 0;
    double normalizationMax = 0;
    NormalizationMethod overlayNormalization = model.getNormalizationMethod();
    if (overlayNormalization.equals(NormalizationMethod.BY_LIMITS)) {
      normalizationMin = _overlayColorBarEditor.getStartValue();
      normalizationMax = _overlayColorBarEditor.getEndValue();
    } else if (overlayNormalization.equals(NormalizationMethod.BY_MAXIMUM)) {
      normalizationMin = _renderer.getDataMinimum();
      normalizationMax = _renderer.getDataMaximum();
      double minmax = Math.max(Math.abs(normalizationMin), Math.abs(normalizationMax));
      normalizationMin = -minmax;
      normalizationMax = minmax;
    } else if (overlayNormalization.equals(NormalizationMethod.BY_AVERAGE)) {
      normalizationMax = _renderer.getDataAverage();
      normalizationMin = -normalizationMax;
    }
    model.getColorBar().setStartValue(normalizationMin);
    model.getColorBar().setEndValue(normalizationMax);
    _overlayColorBarEditor.setColorBar(model.getColorBar());

    _renderer.updateRendererModel(model);
  }

  @Override
  protected IModel createModel() {
    TraceDataRendererModel model = new TraceDataRendererModel(_renderer.getSettingsModel());
    model.getColorBar().addColorMapListener(this);
    return model;
  }

  @Override
  public boolean close() {
    if (_overlayColorBarEditor != null) {
      _overlayColorBarEditor.dispose();
    }
    if (_underlayColorBarEditor != null) {
      _underlayColorBarEditor.dispose();
    }
    return super.close();
  }

  public void colorsChanged(final ColorMapEvent event) {
    _renderer.colorsChanged(event);
  }

  @Override
  protected void updatePreferences() {
    TraceDataRendererModel model = (TraceDataRendererModel) _model;

    // Update the default renderer settings to the preferences.
    IPreferenceStore preferences = SeismicDatasetRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(REVERSE_POLARITY, model.getReversePolarity());
    preferences.setValue(INTERPOLATION_METHOD, model.getInterpolationMethod().getName());
    preferences.setValue(NORMALIZATION_METHOD, model.getNormalizationMethod().getName());
    preferences.setValue(TRACE_EXAGGERATION, model.getTraceExaggeration());
    preferences.setValue(TRACE_CLIPPING, model.getTraceClipping());
    preferences.setValue(TRANSPARENCY, model.getTransparency());
    preferences.setValue(PERCENTILE, model.getPercentile());
    preferences.setValue(WIGGLE_TRACE, model.getWiggleTrace());
    preferences.setValue(POSITIVE_COLOR_FILL, model.getPositiveColorFill());
    preferences.setValue(NEGATIVE_COLOR_FILL, model.getNegativeColorFill());
    preferences.setValue(POSITIVE_DENSITY_FILL, model.getPositiveDensityFill());
    preferences.setValue(NEGATIVE_DENSITY_FILL, model.getNegativeDensityFill());
    preferences.setValue(VARIABLE_DENSITY, model.getVariableDensity());
    PreferenceConverter.setValue(preferences, COLOR_NULL, model.getColorNull());
    PreferenceConverter.setValue(preferences, COLOR_WIGGLE, model.getColorWiggle());
    PreferenceConverter.setValue(preferences, COLOR_POSITIVE_FILL, model.getColorPositiveFill());
    PreferenceConverter.setValue(preferences, COLOR_NEGATIVE_FILL, model.getColorNegativeFill());
    //preferences.setValue(COLOR_MAP, colorMap);

    preferences.setValue(ISeismicDatasetRendererConstants.AGC_APPLY, model.getAgcApply());
    preferences.setValue(AGC_TYPE, model.getAgcType().getName());
    preferences.setValue(AGC_WINDOW_LENGTH, model.getAgcWindowLength());

    preferences.setValue(ISeismicDatasetRendererConstants.GEOMETRIC_GAIN_APPLY, model.getGeometricGainApply());
    preferences.setValue(ISeismicDatasetRendererConstants.GEOMETRIC_GAIN_T0, model.getGeometricGainT0());
    preferences.setValue(ISeismicDatasetRendererConstants.GEOMETRIC_GAIN_TMAX, model.getGeometricGainTMax());
    preferences.setValue(ISeismicDatasetRendererConstants.GEOMETRIC_GAIN_N, model.getGeometricGainN());
  }
}
