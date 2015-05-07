package org.geocraft.ui.volumeviewer.renderer.seismic;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
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
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.GeocraftPreferenceService;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.core.service.ServiceProvider;


public class SeismicDatasetRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, ISeismicDatasetRendererConstants {

  public static final String ID = "org.geocraft.ui.volumeviewer.seismic";

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
    final GeocraftPreferenceService service = PreferencesUtil.getService(ID);
    service.setDefault(REVERSE_POLARITY, false);
    service.setDefault(INTERPOLATION_METHOD, InterpolationMethod.LINEAR.getName());
    service.setDefault(NORMALIZATION_METHOD, NormalizationMethod.BY_MAXIMUM.getName());
    // service.setDefault(TRANSPARENCY, 0);
    service.setDefault(PERCENTILE, 1);
    service.setDefault(SEISMIC_COLOR_MAP, SeismicColorMap.COLOR_MAP_NAME);
    service.setDefault(VELOCITY_COLOR_MAP, SpectrumColorMap.COLOR_MAP_NAME);
    service.setDefault(OTHER_COLOR_MAP, GrayscaleColorMap.COLOR_MAP_NAME);
    // service.setDefault(AGC_APPLY, false);
    // service.setDefault(AGC_TYPE, AGC.Type.BOXCAR.getName());
    // service.setDefault(AGC_WINDOW_LENGTH, Float.toString(100));
    // service.setDefault(GEOMETRIC_GAIN_APPLY, false);
    // service.setDefault(GEOMETRIC_GAIN_T0, Float.toString(100));
    // service.setDefault(GEOMETRIC_GAIN_TMAX, Float.toString(0));
    // service.setDefault(GEOMETRIC_GAIN_N, Float.toString(2));
  }

  @Override
  protected void createFieldEditors() {
    final Composite parent = getFieldEditorParent();

    final Group generalGroup = createGroup(parent, "General");

    final InterpolationMethod[] interpMethods = InterpolationMethod.values();
    final String[][] interpMethodOptions = new String[interpMethods.length][2];
    for (int i = 0; i < interpMethods.length; i++) {
      interpMethodOptions[i][0] = interpMethods[i].getName();
      interpMethodOptions[i][1] = interpMethods[i].getName();
    }
    final ComboFieldEditor interpolationField = new ComboFieldEditor(INTERPOLATION_METHOD, INTERPOLATION_METHOD,
        interpMethodOptions, generalGroup);
    addField(interpolationField);

    final NormalizationMethod[] normMethods = { NormalizationMethod.BY_LIMITS, NormalizationMethod.BY_MAXIMUM };
    final String[][] normMethodOptions = new String[normMethods.length][2];
    for (int i = 0; i < normMethods.length; i++) {
      normMethodOptions[i][0] = normMethods[i].getName();
      normMethodOptions[i][1] = normMethods[i].getName();
    }
    final ComboFieldEditor normalizationField = new ComboFieldEditor(NORMALIZATION_METHOD, NORMALIZATION_METHOD,
        normMethodOptions, generalGroup);
    addField(normalizationField);

    final ScaleFieldEditor percentileField = new ScaleFieldEditor(PERCENTILE, PERCENTILE, generalGroup, 0, 50, 1, 10);
    addField(percentileField);

    //final ScaleFieldEditor transparencyField = new ScaleFieldEditor(TRANSPARENCY, TRANSPARENCY, generalGroup, 0, 100,
    //     1, 10);
    // addField(transparencyField);

    final BooleanFieldEditor polarityField = new BooleanFieldEditor(REVERSE_POLARITY, REVERSE_POLARITY, generalGroup);
    addField(polarityField);

    final Group colorsGroup = createGroup(parent, "Colors");

    final ColorMapDescription[] colorMapDescs = ServiceProvider.getColorMapService().getAll();
    final String[][] colorMapOptions = new String[colorMapDescs.length][2];
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

    // final Group agcGroup = createGroup(parent, "AGC and Geometric Gain");
    //
    // final BooleanFieldEditor agcApplyField = new BooleanFieldEditor(AGC_APPLY, AGC_APPLY, agcGroup);
    // addField(agcApplyField);

    //final AGC.Type[] agcTypes = AGC.Type.values();
    // final String[][] agcTypeOptions = new String[agcTypes.length][2];
    //for (int i = 0; i < agcTypes.length; i++) {
    //  agcTypeOptions[i][0] = agcTypes[i].getName();
    //  agcTypeOptions[i][1] = agcTypes[i].getName();
    //}
    //final ComboFieldEditor agcTypeField = new ComboFieldEditor(AGC_TYPE, AGC_TYPE, agcTypeOptions, agcGroup);
    //addField(agcTypeField);

    //final StringFieldEditor agcWindowField = new StringFieldEditor(AGC_WINDOW_LENGTH, AGC_WINDOW_LENGTH, agcGroup);
    //addField(agcWindowField);

    //final BooleanFieldEditor gainApplyField = new BooleanFieldEditor(GEOMETRIC_GAIN_APPLY, GEOMETRIC_GAIN_APPLY,
    //    agcGroup);
    // addField(gainApplyField);

    //final StringFieldEditor gainT0Field = new StringFieldEditor(GEOMETRIC_GAIN_T0, GEOMETRIC_GAIN_T0, agcGroup);
    //addField(gainT0Field);

    //final StringFieldEditor gainNField = new StringFieldEditor(GEOMETRIC_GAIN_N, GEOMETRIC_GAIN_N, agcGroup);
    //addField(gainNField);

    // final StringFieldEditor gainTmaxField = new StringFieldEditor(GEOMETRIC_GAIN_TMAX, GEOMETRIC_GAIN_TMAX, agcGroup);
    //addField(gainTmaxField);

    setDefaults();

    interpolationField.load();
    normalizationField.load();
    percentileField.load();
    // transparencyField.load();
    polarityField.load();

    seismicColorMapField.load();
    velocityColorMapField.load();
    otherColorMapField.load();

    //agcApplyField.load();
    //agcTypeField.load();
    //agcWindowField.load();

    //gainApplyField.load();
    //gainT0Field.load();
    //gainNField.load();
    //gainTmaxField.load();

  }

  public Map<String, String> getPreferenceState() {
    final HashMap<String, String> prefs = new HashMap<String, String>();

    final IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    final String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefs.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    final String interpMethod = localStore.getString(INTERPOLATION_METHOD);
    prefs.put(INTERPOLATION_METHOD, interpMethod);

    final String normMethod = localStore.getString(NORMALIZATION_METHOD);
    prefs.put(NORMALIZATION_METHOD, normMethod);

    final int percentile = localStore.getInt(PERCENTILE);
    prefs.put(PERCENTILE, Integer.toString(percentile));

    //final int transparency = localStore.getInt(TRANSPARENCY);
    //prefs.put(TRANSPARENCY, Integer.toString(transparency));

    final boolean reversePolarity = localStore.getBoolean(REVERSE_POLARITY);
    prefs.put(REVERSE_POLARITY, Boolean.toString(reversePolarity));

    final String seismicColorMap = localStore.getString(SEISMIC_COLOR_MAP);
    prefs.put(SEISMIC_COLOR_MAP, seismicColorMap);

    final String velocityColorMap = localStore.getString(VELOCITY_COLOR_MAP);
    prefs.put(VELOCITY_COLOR_MAP, velocityColorMap);

    final String otherColorMap = localStore.getString(OTHER_COLOR_MAP);
    prefs.put(OTHER_COLOR_MAP, otherColorMap);

    //    final boolean agcApply = localStore.getBoolean(AGC_APPLY);
    //    prefs.put(AGC_APPLY, Boolean.toString(agcApply));
    //
    //    final String agcType = localStore.getString(AGC_TYPE);
    //    prefs.put(AGC_TYPE, agcType);
    //
    //    final String agcWindow = localStore.getString(AGC_WINDOW_LENGTH);
    //    prefs.put(AGC_WINDOW_LENGTH, agcWindow);
    //
    //    final boolean gainApply = localStore.getBoolean(GEOMETRIC_GAIN_APPLY);
    //    prefs.put(GEOMETRIC_GAIN_APPLY, Boolean.toString(gainApply));
    //
    //    final String gainT0 = localStore.getString(GEOMETRIC_GAIN_T0);
    //    prefs.put(GEOMETRIC_GAIN_T0, gainT0);
    //
    //    final String gainN = localStore.getString(GEOMETRIC_GAIN_N);
    //    prefs.put(GEOMETRIC_GAIN_N, gainN);
    //
    //    final String gainTmax = localStore.getString(GEOMETRIC_GAIN_TMAX);
    //    prefs.put(GEOMETRIC_GAIN_TMAX, gainTmax);

    return prefs;
  }

  public void setPreferenceState(final Map<String, String> prefs) {

    try {
      PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs.get(PropertyStore.USE_PROJECT_SETTINGS));
    } catch (final Exception ex) {
      // Leave as default.
    }

    final IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    try {
      final String interpMethod = prefs.get(INTERPOLATION_METHOD);
      localStore.setValue(INTERPOLATION_METHOD, interpMethod);
    } catch (final NumberFormatException e) {
      // Leave as default.
    }

    try {
      final String normMethod = prefs.get(NORMALIZATION_METHOD);
      localStore.setValue(NORMALIZATION_METHOD, normMethod);
    } catch (final NumberFormatException e) {
      // Leave as default.
    }

    try {
      final int percentile = Integer.parseInt(prefs.get(PERCENTILE));
      localStore.setValue(PERCENTILE, percentile);
    } catch (final Exception e) {
      // Leave as default.
    }

    //try {
    //   final int transparency = Integer.parseInt(prefs.get(TRANSPARENCY));
    //   localStore.setValue(TRANSPARENCY, transparency);
    // } catch (final Exception e) {
    //   // Leave as default.
    // }

    try {
      final boolean reversePolarity = Boolean.parseBoolean(prefs.get(REVERSE_POLARITY));
      localStore.setValue(REVERSE_POLARITY, reversePolarity);
    } catch (final Exception e) {
      // Leave as default.
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

    //    try {
    //      final boolean agcApply = Boolean.parseBoolean(prefs.get(AGC_APPLY));
    //      localStore.setValue(AGC_APPLY, agcApply);
    //    } catch (final Exception e) {
    //      // Leave as default.
    //    }
    //
    //    try {
    //      final String agcType = prefs.get(AGC_TYPE);
    //      localStore.setValue(AGC_TYPE, agcType);
    //    } catch (final Exception ex) {
    //      // Leave value as default.
    //    }
    //
    //    try {
    //      final String agcWindow = prefs.get(AGC_WINDOW_LENGTH);
    //      localStore.setValue(AGC_WINDOW_LENGTH, agcWindow);
    //    } catch (final Exception ex) {
    //      // Leave value as default.
    //    }
    //
    //    try {
    //      final boolean gainApply = Boolean.parseBoolean(prefs.get(GEOMETRIC_GAIN_APPLY));
    //      localStore.setValue(GEOMETRIC_GAIN_APPLY, gainApply);
    //    } catch (final Exception e) {
    //      // Leave as default.
    //    }
    //
    //    try {
    //      final String gainT0 = prefs.get(GEOMETRIC_GAIN_T0);
    //      localStore.setValue(GEOMETRIC_GAIN_T0, gainT0);
    //    } catch (final Exception ex) {
    //      // Leave value as default.
    //    }
    //
    //    try {
    //      final String gainN = prefs.get(GEOMETRIC_GAIN_N);
    //      localStore.setValue(GEOMETRIC_GAIN_N, gainN);
    //    } catch (final Exception ex) {
    //      // Leave value as default.
    //    }
    //
    //    try {
    //      final String gainTmax = prefs.get(GEOMETRIC_GAIN_TMAX);
    //      localStore.setValue(GEOMETRIC_GAIN_TMAX, gainTmax);
    //    } catch (final Exception ex) {
    //      // Leave value as default.
    //    }
  }

  /**
   * Create a buttons group.
   * 
   * @param parent composite
   * @param title the group title
   */
  protected Group createGroup(final Composite parent, final String title) {
    final Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setText(title);
    final GridLayout layout = new GridLayout();
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buttonGroup;
  }
}
