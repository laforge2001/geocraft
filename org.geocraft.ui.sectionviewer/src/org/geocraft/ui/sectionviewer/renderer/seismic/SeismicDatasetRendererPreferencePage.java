package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.GrayscaleColorMap;
import org.geocraft.core.color.map.SeismicColorMap;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.math.AGC;
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.GeocraftPreferenceService;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.sectionviewer.InterpolationMethod;
import org.geocraft.ui.sectionviewer.NormalizationMethod;


public class SeismicDatasetRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, ISeismicDatasetRendererConstants {

  public static final String ID = "org.geocraft.ui.sectionviewer.seismic";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(SeismicDatasetRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public SeismicDatasetRendererPreferencePage() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for seismic plotting in the 3D viewer.");
  }

  public static void setDefaults() {

    // Initialize the seismic dataset preferences. 
    GeocraftPreferenceService service = PreferencesUtil.getService(ID);
    service.setDefault(REVERSE_POLARITY, false);
    service.setDefault(INTERPOLATION_METHOD, InterpolationMethod.LINEAR.getName());
    service.setDefault(NORMALIZATION_METHOD, NormalizationMethod.BY_MAXIMUM.getName());
    service.setDefault(TRACE_EXAGGERATION, "1.0");
    service.setDefault(TRACE_CLIPPING, 4);
    service.setDefault(TRANSPARENCY, 0);
    service.setDefault(PERCENTILE, 1);
    service.setDefault(WIGGLE_TRACE, false);
    service.setDefault(POSITIVE_COLOR_FILL, false);
    service.setDefault(NEGATIVE_COLOR_FILL, false);
    service.setDefault(POSITIVE_DENSITY_FILL, false);
    service.setDefault(NEGATIVE_DENSITY_FILL, false);
    service.setDefault(VARIABLE_DENSITY, true);
    service.setDefault(COLOR_NULL, new RGB(255, 255, 255));
    service.setDefault(COLOR_WIGGLE, new RGB(0, 0, 0));
    service.setDefault(COLOR_POSITIVE_FILL, new RGB(0, 0, 255));
    service.setDefault(COLOR_NEGATIVE_FILL, new RGB(255, 0, 0));
    service.setDefault(SEISMIC_COLOR_MAP, SeismicColorMap.COLOR_MAP_NAME);
    service.setDefault(VELOCITY_COLOR_MAP, SpectrumColorMap.COLOR_MAP_NAME);
    service.setDefault(OTHER_COLOR_MAP, GrayscaleColorMap.COLOR_MAP_NAME);
    service.setDefault(AGC_APPLY, false);
    service.setDefault(AGC_TYPE, AGC.Type.BOXCAR.getName());
    service.setDefault(AGC_WINDOW_LENGTH, Float.toString(100));
    service.setDefault(GEOMETRIC_GAIN_APPLY, false);
    service.setDefault(GEOMETRIC_GAIN_T0, Float.toString(100));
    service.setDefault(GEOMETRIC_GAIN_TMAX, Float.toString(0));
    service.setDefault(GEOMETRIC_GAIN_N, Float.toString(2));
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    final Group generalGroup = createGroup(parent, "General");

    InterpolationMethod[] interpMethods = InterpolationMethod.values();
    String[][] interpMethodOptions = new String[interpMethods.length][2];
    for (int i = 0; i < interpMethods.length; i++) {
      interpMethodOptions[i][0] = interpMethods[i].getName();
      interpMethodOptions[i][1] = interpMethods[i].getName();
    }
    ComboFieldEditor interpolationField = new ComboFieldEditor(INTERPOLATION_METHOD, INTERPOLATION_METHOD,
        interpMethodOptions, generalGroup);
    addField(interpolationField);

    NormalizationMethod[] normMethods = NormalizationMethod.values();
    String[][] normMethodOptions = new String[normMethods.length][2];
    for (int i = 0; i < normMethods.length; i++) {
      normMethodOptions[i][0] = normMethods[i].getName();
      normMethodOptions[i][1] = normMethods[i].getName();
    }
    ComboFieldEditor normalizationField = new ComboFieldEditor(NORMALIZATION_METHOD, NORMALIZATION_METHOD,
        normMethodOptions, generalGroup);
    addField(normalizationField);

    final StringFieldEditor traceExField = new StringFieldEditor(TRACE_EXAGGERATION, TRACE_EXAGGERATION, generalGroup);
    addField(traceExField);

    ScaleFieldEditor traceClipField = new ScaleFieldEditor(TRACE_CLIPPING, TRACE_CLIPPING, generalGroup, 0, 10, 1, 1);
    addField(traceClipField);

    ScaleFieldEditor percentileField = new ScaleFieldEditor(PERCENTILE, PERCENTILE, generalGroup, 0, 50, 1, 10);
    addField(percentileField);

    ScaleFieldEditor transparencyField = new ScaleFieldEditor(TRANSPARENCY, TRANSPARENCY, generalGroup, 0, 100, 1, 10);
    addField(transparencyField);

    BooleanFieldEditor polarityField = new BooleanFieldEditor(REVERSE_POLARITY, REVERSE_POLARITY, generalGroup);
    addField(polarityField);

    Group stylesGroup = createGroup(parent, "Styles");

    BooleanFieldEditor wiggleField = new BooleanFieldEditor(WIGGLE_TRACE, WIGGLE_TRACE, stylesGroup);
    addField(wiggleField);

    BooleanFieldEditor posColorField = new BooleanFieldEditor(POSITIVE_COLOR_FILL, POSITIVE_COLOR_FILL, stylesGroup);
    addField(posColorField);

    BooleanFieldEditor negColorField = new BooleanFieldEditor(NEGATIVE_COLOR_FILL, NEGATIVE_COLOR_FILL, stylesGroup);
    addField(negColorField);

    BooleanFieldEditor posDensityField = new BooleanFieldEditor(POSITIVE_DENSITY_FILL, POSITIVE_DENSITY_FILL,
        stylesGroup);
    addField(posDensityField);

    BooleanFieldEditor negDensityField = new BooleanFieldEditor(NEGATIVE_DENSITY_FILL, NEGATIVE_DENSITY_FILL,
        stylesGroup);
    addField(negDensityField);

    BooleanFieldEditor varDensityField = new BooleanFieldEditor(VARIABLE_DENSITY, VARIABLE_DENSITY, stylesGroup);
    addField(varDensityField);

    Group colorsGroup = createGroup(parent, "Colors");

    ColorFieldEditor wiggleColorField = new ColorFieldEditor(COLOR_WIGGLE, COLOR_WIGGLE, colorsGroup);
    addField(wiggleColorField);

    ColorFieldEditor posFillColorField = new ColorFieldEditor(COLOR_POSITIVE_FILL, COLOR_POSITIVE_FILL, colorsGroup);
    addField(posFillColorField);

    ColorFieldEditor negFillColorField = new ColorFieldEditor(COLOR_NEGATIVE_FILL, COLOR_NEGATIVE_FILL, colorsGroup);
    addField(negFillColorField);

    ColorFieldEditor nullColorField = new ColorFieldEditor(COLOR_NULL, COLOR_NULL, colorsGroup);
    addField(nullColorField);

    ColorMapDescription[] colorMapDescs = ServiceProvider.getColorMapService().getAll();
    String[][] colorMapOptions = new String[colorMapDescs.length][2];
    for (int i = 0; i < colorMapDescs.length; i++) {
      colorMapOptions[i][0] = colorMapDescs[i].getName();
      colorMapOptions[i][1] = colorMapDescs[i].getName();
    }
    final ComboFieldEditor seismicColorMapField = new ComboFieldEditor(SEISMIC_COLOR_MAP, SEISMIC_COLOR_MAP,
        colorMapOptions, colorsGroup);
    addField(seismicColorMapField);

    final ComboFieldEditor velocityColorMapField = new ComboFieldEditor(VELOCITY_COLOR_MAP, VELOCITY_COLOR_MAP,
        colorMapOptions, colorsGroup);
    addField(velocityColorMapField);

    final ComboFieldEditor otherColorMapField = new ComboFieldEditor(OTHER_COLOR_MAP, OTHER_COLOR_MAP, colorMapOptions,
        colorsGroup);
    addField(otherColorMapField);

    Group agcGroup = createGroup(parent, "AGC and Geometric Gain");

    BooleanFieldEditor agcApplyField = new BooleanFieldEditor(AGC_APPLY, AGC_APPLY, agcGroup);
    addField(agcApplyField);

    AGC.Type[] agcTypes = AGC.Type.values();
    String[][] agcTypeOptions = new String[agcTypes.length][2];
    for (int i = 0; i < agcTypes.length; i++) {
      agcTypeOptions[i][0] = agcTypes[i].getName();
      agcTypeOptions[i][1] = agcTypes[i].getName();
    }
    ComboFieldEditor agcTypeField = new ComboFieldEditor(AGC_TYPE, AGC_TYPE, agcTypeOptions, agcGroup);
    addField(agcTypeField);

    StringFieldEditor agcWindowField = new StringFieldEditor(AGC_WINDOW_LENGTH, AGC_WINDOW_LENGTH, agcGroup);
    addField(agcWindowField);

    BooleanFieldEditor gainApplyField = new BooleanFieldEditor(GEOMETRIC_GAIN_APPLY, GEOMETRIC_GAIN_APPLY, agcGroup);
    addField(gainApplyField);

    StringFieldEditor gainT0Field = new StringFieldEditor(GEOMETRIC_GAIN_T0, GEOMETRIC_GAIN_T0, agcGroup);
    addField(gainT0Field);

    StringFieldEditor gainNField = new StringFieldEditor(GEOMETRIC_GAIN_N, GEOMETRIC_GAIN_N, agcGroup);
    addField(gainNField);

    StringFieldEditor gainTmaxField = new StringFieldEditor(GEOMETRIC_GAIN_TMAX, GEOMETRIC_GAIN_TMAX, agcGroup);
    addField(gainTmaxField);

    setDefaults();

    interpolationField.load();
    normalizationField.load();
    traceExField.load();
    traceClipField.load();
    percentileField.load();
    transparencyField.load();
    polarityField.load();

    wiggleField.load();
    posColorField.load();
    negColorField.load();
    posDensityField.load();
    negDensityField.load();
    varDensityField.load();

    wiggleColorField.load();
    posColorField.load();
    negColorField.load();
    nullColorField.load();
    seismicColorMapField.load();
    velocityColorMapField.load();
    otherColorMapField.load();

    agcApplyField.load();
    agcTypeField.load();
    agcWindowField.load();

    gainApplyField.load();
    gainT0Field.load();
    gainNField.load();
    gainTmaxField.load();

  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefs = new HashMap<String, String>();

    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefs.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    String interpMethod = localStore.getString(INTERPOLATION_METHOD);
    prefs.put(INTERPOLATION_METHOD, interpMethod);

    String normMethod = localStore.getString(NORMALIZATION_METHOD);
    prefs.put(NORMALIZATION_METHOD, normMethod);

    float traceEx = localStore.getFloat(TRACE_EXAGGERATION);
    prefs.put(TRACE_EXAGGERATION, Float.toString(traceEx));

    int traceClip = localStore.getInt(TRACE_CLIPPING);
    prefs.put(TRACE_CLIPPING, Integer.toString(traceClip));

    int percentile = localStore.getInt(PERCENTILE);
    prefs.put(PERCENTILE, Integer.toString(percentile));

    int transparency = localStore.getInt(TRANSPARENCY);
    prefs.put(TRANSPARENCY, Integer.toString(transparency));

    boolean reversePolarity = localStore.getBoolean(REVERSE_POLARITY);
    prefs.put(REVERSE_POLARITY, Boolean.toString(reversePolarity));

    boolean wiggle = localStore.getBoolean(WIGGLE_TRACE);
    prefs.put(WIGGLE_TRACE, Boolean.toString(wiggle));

    boolean posColorFill = localStore.getBoolean(POSITIVE_COLOR_FILL);
    prefs.put(POSITIVE_COLOR_FILL, Boolean.toString(posColorFill));

    boolean negColorFill = localStore.getBoolean(NEGATIVE_COLOR_FILL);
    prefs.put(NEGATIVE_COLOR_FILL, Boolean.toString(negColorFill));

    boolean posDensityFill = localStore.getBoolean(POSITIVE_DENSITY_FILL);
    prefs.put(POSITIVE_DENSITY_FILL, Boolean.toString(posDensityFill));

    boolean negDensityFill = localStore.getBoolean(NEGATIVE_DENSITY_FILL);
    prefs.put(NEGATIVE_DENSITY_FILL, Boolean.toString(negDensityFill));

    boolean varDensity = localStore.getBoolean(VARIABLE_DENSITY);
    prefs.put(VARIABLE_DENSITY, Boolean.toString(varDensity));

    RGB wiggleColor = PreferenceConverter.getColor(localStore, COLOR_WIGGLE);
    prefs.put(COLOR_WIGGLE, wiggleColor.toString());

    RGB posFillColor = PreferenceConverter.getColor(localStore, COLOR_POSITIVE_FILL);
    prefs.put(COLOR_POSITIVE_FILL, posFillColor.toString());

    RGB negFillColor = PreferenceConverter.getColor(localStore, COLOR_NEGATIVE_FILL);
    prefs.put(COLOR_NEGATIVE_FILL, negFillColor.toString());

    RGB colorNull = PreferenceConverter.getColor(localStore, COLOR_NULL);
    prefs.put(COLOR_NULL, colorNull.toString());

    final String seismicColorMap = localStore.getString(SEISMIC_COLOR_MAP);
    prefs.put(SEISMIC_COLOR_MAP, seismicColorMap);

    final String velocityColorMap = localStore.getString(VELOCITY_COLOR_MAP);
    prefs.put(VELOCITY_COLOR_MAP, velocityColorMap);

    final String otherColorMap = localStore.getString(OTHER_COLOR_MAP);
    prefs.put(OTHER_COLOR_MAP, otherColorMap);

    boolean agcApply = localStore.getBoolean(AGC_APPLY);
    prefs.put(AGC_APPLY, Boolean.toString(agcApply));

    String agcType = localStore.getString(AGC_TYPE);
    prefs.put(AGC_TYPE, agcType);

    String agcWindow = localStore.getString(AGC_WINDOW_LENGTH);
    prefs.put(AGC_WINDOW_LENGTH, agcWindow);

    boolean gainApply = localStore.getBoolean(GEOMETRIC_GAIN_APPLY);
    prefs.put(GEOMETRIC_GAIN_APPLY, Boolean.toString(gainApply));

    String gainT0 = localStore.getString(GEOMETRIC_GAIN_T0);
    prefs.put(GEOMETRIC_GAIN_T0, gainT0);

    String gainN = localStore.getString(GEOMETRIC_GAIN_N);
    prefs.put(GEOMETRIC_GAIN_N, gainN);

    String gainTmax = localStore.getString(GEOMETRIC_GAIN_TMAX);
    prefs.put(GEOMETRIC_GAIN_TMAX, gainTmax);

    return prefs;
  }

  public void setPreferenceState(final Map<String, String> prefs) {

    try {
      PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs.get(PropertyStore.USE_PROJECT_SETTINGS));
    } catch (Exception ex) {
      // Leave as default.
    }

    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    try {
      String interpMethod = prefs.get(INTERPOLATION_METHOD);
      localStore.setValue(INTERPOLATION_METHOD, interpMethod);
    } catch (NumberFormatException e) {
      // Leave as default.
    }

    try {
      String normMethod = prefs.get(NORMALIZATION_METHOD);
      localStore.setValue(NORMALIZATION_METHOD, normMethod);
    } catch (NumberFormatException e) {
      // Leave as default.
    }

    try {
      float traceEx = Float.parseFloat(prefs.get(TRACE_EXAGGERATION));
      localStore.setValue(TRACE_EXAGGERATION, traceEx);
    } catch (NumberFormatException e) {
      // Leave as default.
    }

    try {
      int traceClip = Integer.parseInt(prefs.get(TRACE_CLIPPING));
      localStore.setValue(TRACE_CLIPPING, traceClip);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      int percentile = Integer.parseInt(prefs.get(PERCENTILE));
      localStore.setValue(PERCENTILE, percentile);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      int transparency = Integer.parseInt(prefs.get(TRANSPARENCY));
      localStore.setValue(TRANSPARENCY, transparency);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      boolean reversePolarity = Boolean.parseBoolean(prefs.get(REVERSE_POLARITY));
      localStore.setValue(REVERSE_POLARITY, reversePolarity);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      boolean wiggleTrace = Boolean.parseBoolean(prefs.get(WIGGLE_TRACE));
      localStore.setValue(WIGGLE_TRACE, wiggleTrace);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      boolean posColorFill = Boolean.parseBoolean(prefs.get(POSITIVE_COLOR_FILL));
      localStore.setValue(POSITIVE_COLOR_FILL, posColorFill);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      boolean negColorFill = Boolean.parseBoolean(prefs.get(NEGATIVE_COLOR_FILL));
      localStore.setValue(NEGATIVE_COLOR_FILL, negColorFill);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      boolean posDensityFill = Boolean.parseBoolean(prefs.get(POSITIVE_DENSITY_FILL));
      localStore.setValue(POSITIVE_DENSITY_FILL, posDensityFill);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      boolean negDensityFill = Boolean.parseBoolean(prefs.get(NEGATIVE_DENSITY_FILL));
      localStore.setValue(NEGATIVE_DENSITY_FILL, negDensityFill);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      boolean varDensity = Boolean.parseBoolean(prefs.get(VARIABLE_DENSITY));
      localStore.setValue(VARIABLE_DENSITY, varDensity);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      RGB wiggleColor = PropertyStore.rgbValue(prefs.get(COLOR_WIGGLE));
      PreferenceConverter.setValue(localStore, COLOR_WIGGLE, wiggleColor);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      RGB colorPosFill = PropertyStore.rgbValue(prefs.get(COLOR_POSITIVE_FILL));
      PreferenceConverter.setValue(localStore, COLOR_POSITIVE_FILL, colorPosFill);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      RGB colorNegFill = PropertyStore.rgbValue(prefs.get(COLOR_NEGATIVE_FILL));
      PreferenceConverter.setValue(localStore, COLOR_NEGATIVE_FILL, colorNegFill);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      RGB colorNull = PropertyStore.rgbValue(prefs.get(COLOR_NULL));
      PreferenceConverter.setValue(localStore, COLOR_NULL, colorNull);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      final String seismicColorMap = prefs.get(SEISMIC_COLOR_MAP);
      localStore.setValue(SEISMIC_COLOR_MAP, seismicColorMap);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final String velocityColorMap = prefs.get(VELOCITY_COLOR_MAP);
      localStore.setValue(VELOCITY_COLOR_MAP, velocityColorMap);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final String otherColorMap = prefs.get(OTHER_COLOR_MAP);
      localStore.setValue(OTHER_COLOR_MAP, otherColorMap);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      boolean agcApply = Boolean.parseBoolean(prefs.get(AGC_APPLY));
      localStore.setValue(AGC_APPLY, agcApply);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      String agcType = prefs.get(AGC_TYPE);
      localStore.setValue(AGC_TYPE, agcType);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      String agcWindow = prefs.get(AGC_WINDOW_LENGTH);
      localStore.setValue(AGC_WINDOW_LENGTH, agcWindow);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      boolean gainApply = Boolean.parseBoolean(prefs.get(GEOMETRIC_GAIN_APPLY));
      localStore.setValue(GEOMETRIC_GAIN_APPLY, gainApply);
    } catch (Exception e) {
      // Leave as default.
    }

    try {
      String gainT0 = prefs.get(GEOMETRIC_GAIN_T0);
      localStore.setValue(GEOMETRIC_GAIN_T0, gainT0);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      String gainN = prefs.get(GEOMETRIC_GAIN_N);
      localStore.setValue(GEOMETRIC_GAIN_N, gainN);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      String gainTmax = prefs.get(GEOMETRIC_GAIN_TMAX);
      localStore.setValue(GEOMETRIC_GAIN_TMAX, gainTmax);
    } catch (Exception ex) {
      // Leave value as default.
    }
  }

  /**
   * Create a buttons group.
   * 
   * @param parent composite
   * @param title the group title
   */
  protected Group createGroup(final Composite parent, final String title) {
    Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setText(title);
    GridLayout layout = new GridLayout();
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buttonGroup;
  }
}
