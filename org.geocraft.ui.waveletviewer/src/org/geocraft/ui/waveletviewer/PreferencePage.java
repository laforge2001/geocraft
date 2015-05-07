/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.waveletviewer;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;


public class PreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage, IGeocraftPreferencePage {

  static {
    setDefaults();
  }

  public static PropertyStore WAVELET_VIEWER_PREFERENCE_STORE = PropertyStoreFactory.getStore(PreferencePage.ID);

  public static final String ID = "org.geocraft.ui.waveletviewer.preferences";

  public static final String TIME_DOMAIN_SCALING = "scaling";

  public static final String TIME_DOMAIN_SCALE_FACTOR = "scaleFactor";

  public static final String MAXIMUM_FREQUENCY = "maximumFrequency";

  public static final String FREQUENCY_DOMAIN_SCALING = "frequencyDomainScaling";

  public static final String FREQUENCY_DOMAIN_SCALE_FACTOR = "frequencyDomainScaleFactor";

  public static final String SPECTRUM_TYPE = "amplitudeSpectrumType";

  public static final String DECIBEL_RANGE = "dbRange";

  public static final String DISPLAY_PHASE_PLOT = "displayPhasePlot";

  public static final String UNWRAP_PHASE = "unwrapPhase";

  public static final String AMPLITUDE_SPECTRUM_THRESHOLD = "amplitudeSpectrumThreshold";

  public static final String MINIMUM_PHASE = "minimumPhase";

  public static final String MAXIMUM_PHASE = "maximumPhase";

  public static final String ADD_REGRESSION_LINE = "addRegressionLine";

  public static final String MINIMUM_REGRESSION_FREQUENCY = "minimumRegressionFrequency";

  public static final String AUTOMATIC_SCALING = "AUTOMATIC_SCALING";

  public static final String MANUAL_SCALING = "MANUAL_SCALING";

  public static final String AMPLITUDE_SPECTRUM = "AMPLITUDE_SPECTRUM";

  public static final String LINEAR_POWER_SPECTRUM = "LINEAR_POWER_SPECTRUM";

  public static final String DECIBEL_POWER_SPECTRUM = "DECIBEL_POWER_SPECTRUM";

  public PreferencePage() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for wavelet plotting.");
  }

  public static void setDefaults() {
    // Initialize the time domain preferences.
    PreferencesUtil.getService(ID).setDefault(TIME_DOMAIN_SCALING, MANUAL_SCALING);
    PreferencesUtil.getService(ID).setDefault(TIME_DOMAIN_SCALE_FACTOR, 1);

    // Initialize the amplitude spectrum preferences.
    PreferencesUtil.getService(ID).setDefault(MAXIMUM_FREQUENCY, 250);
    PreferencesUtil.getService(ID).setDefault(FREQUENCY_DOMAIN_SCALING, AUTOMATIC_SCALING);
    PreferencesUtil.getService(ID).setDefault(FREQUENCY_DOMAIN_SCALE_FACTOR, 0);
    PreferencesUtil.getService(ID).setDefault(SPECTRUM_TYPE, AMPLITUDE_SPECTRUM);
    PreferencesUtil.getService(ID).setDefault(DECIBEL_RANGE, -50);

    // Initialize the phase spectrum preferences.
    PreferencesUtil.getService(ID).setDefault(DISPLAY_PHASE_PLOT, true);
    PreferencesUtil.getService(ID).setDefault(UNWRAP_PHASE, false);
    PreferencesUtil.getService(ID).setDefault(AMPLITUDE_SPECTRUM_THRESHOLD, 1);
    PreferencesUtil.getService(ID).setDefault(MINIMUM_PHASE, -180);
    PreferencesUtil.getService(ID).setDefault(MAXIMUM_PHASE, 180);
    PreferencesUtil.getService(ID).setDefault(ADD_REGRESSION_LINE, false);
    PreferencesUtil.getService(ID).setDefault(MINIMUM_REGRESSION_FREQUENCY, 0);
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();
    setDefaults();

    Group timeGroup = createGroup(parent, "Time Domain");

    String[][] scalingOptions = new String[2][2];
    scalingOptions[0][0] = "Automatic Scaling";
    scalingOptions[0][1] = AUTOMATIC_SCALING;
    scalingOptions[1][0] = "Manual Scaling";
    scalingOptions[1][1] = MANUAL_SCALING;
    RadioGroupFieldEditor scaling = new RadioGroupFieldEditor(TIME_DOMAIN_SCALING, "Scaling", 1, scalingOptions,
        timeGroup);
    addField(scaling);

    IntegerFieldEditor scaleFactor = new IntegerFieldEditor(TIME_DOMAIN_SCALE_FACTOR, "Scale Factor", timeGroup);
    addField(scaleFactor);

    Group ampGroup = createGroup(parent, "Amplitude Spectrum");

    IntegerFieldEditor maximumFreq = new IntegerFieldEditor(MAXIMUM_FREQUENCY, "Maximum Frequency", ampGroup);
    addField(maximumFreq);

    RadioGroupFieldEditor ampScaling = new RadioGroupFieldEditor(FREQUENCY_DOMAIN_SCALING, "Scaling", 1,
        scalingOptions, ampGroup);
    addField(ampScaling);

    IntegerFieldEditor ampScaleFactor = new IntegerFieldEditor(FREQUENCY_DOMAIN_SCALE_FACTOR, "Scale Factor", ampGroup);
    addField(ampScaleFactor);

    String[][] spectrumOptions = new String[3][2];
    spectrumOptions[0][0] = "Amplitude";
    spectrumOptions[0][1] = AMPLITUDE_SPECTRUM;
    spectrumOptions[1][0] = "Linear Power";
    spectrumOptions[1][1] = LINEAR_POWER_SPECTRUM;
    spectrumOptions[2][0] = "Decibel Power";
    spectrumOptions[2][1] = DECIBEL_POWER_SPECTRUM;
    RadioGroupFieldEditor ampSpectrum = new RadioGroupFieldEditor(SPECTRUM_TYPE, "Spectrum Type", 1, spectrumOptions,
        ampGroup);
    addField(ampSpectrum);

    IntegerFieldEditor dbRange = new IntegerFieldEditor(DECIBEL_RANGE, "dB Range", ampGroup);
    dbRange.setValidRange(-200, 0);
    addField(dbRange);

    Group phaseGroup = createGroup(parent, "Phase Spectrum");
    BooleanFieldEditor displayPhasePlot = new BooleanFieldEditor(DISPLAY_PHASE_PLOT, "Display the Phase Spectrum Plot",
        phaseGroup);
    addField(displayPhasePlot);

    BooleanFieldEditor unwrapPhase = new BooleanFieldEditor(UNWRAP_PHASE, "Unwrap Phase", phaseGroup);
    addField(unwrapPhase);

    IntegerFieldEditor ampSpectrumThreshold = new IntegerFieldEditor(AMPLITUDE_SPECTRUM_THRESHOLD,
        "Amplitude Spectrum Threshold (%)", phaseGroup);
    ampSpectrumThreshold.setValidRange(0, 100);
    addField(ampSpectrumThreshold);

    IntegerFieldEditor minimumPhase = new IntegerFieldEditor(MINIMUM_PHASE, "Minimum Phase", phaseGroup);
    minimumPhase.setValidRange(-360, 360);
    addField(minimumPhase);

    IntegerFieldEditor maximumPhase = new IntegerFieldEditor(MAXIMUM_PHASE, "Maximum Phase", phaseGroup);
    maximumPhase.setValidRange(-360, 360);
    addField(maximumPhase);

    BooleanFieldEditor addRegressionLine = new BooleanFieldEditor(ADD_REGRESSION_LINE, "Add Regression Line",
        phaseGroup);
    addField(addRegressionLine);

    IntegerFieldEditor minRegressionFreq = new IntegerFieldEditor(MINIMUM_REGRESSION_FREQUENCY,
        "Minimum Regression Frequency", phaseGroup);
    minRegressionFreq.setValidRange(0, 1000);
    addField(minRegressionFreq);

    scaling.load();
    scaleFactor.load();
    maximumFreq.load();
    ampScaling.load();
    ampScaleFactor.load();
    ampSpectrum.load();
    dbRange.load();
    displayPhasePlot.load();
    unwrapPhase.load();
    ampSpectrumThreshold.load();
    minimumPhase.load();
    maximumPhase.load();
    addRegressionLine.load();
    minRegressionFreq.load();
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

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#getPreferenceState()
   */
  @Override
  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = WAVELET_VIEWER_PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    String timeScaling = localStore.getString(TIME_DOMAIN_SCALING);
    prefState.put(TIME_DOMAIN_SCALING, timeScaling);

    int timeScaleFactor = localStore.getInt(TIME_DOMAIN_SCALE_FACTOR);
    prefState.put(TIME_DOMAIN_SCALE_FACTOR, Integer.toString(timeScaleFactor));

    int maxFreq = localStore.getInt(MAXIMUM_FREQUENCY);
    prefState.put(MAXIMUM_FREQUENCY, Integer.toString(maxFreq));

    String freqScaling = localStore.getString(FREQUENCY_DOMAIN_SCALING);
    prefState.put(FREQUENCY_DOMAIN_SCALING, freqScaling);

    int freqScaleFactor = localStore.getInt(FREQUENCY_DOMAIN_SCALE_FACTOR);
    prefState.put(FREQUENCY_DOMAIN_SCALE_FACTOR, Integer.toString(freqScaleFactor));

    String specturmType = localStore.getString(SPECTRUM_TYPE);
    prefState.put(SPECTRUM_TYPE, specturmType);

    int dbRange = localStore.getInt(DECIBEL_RANGE);
    prefState.put(DECIBEL_RANGE, Integer.toString(dbRange));

    boolean displayPhasePlot = localStore.getBoolean(DISPLAY_PHASE_PLOT);
    prefState.put(DISPLAY_PHASE_PLOT, Boolean.toString(displayPhasePlot));

    boolean unwrapPhase = localStore.getBoolean(UNWRAP_PHASE);
    prefState.put(UNWRAP_PHASE, Boolean.toString(unwrapPhase));

    int threshold = localStore.getInt(AMPLITUDE_SPECTRUM_THRESHOLD);
    prefState.put(AMPLITUDE_SPECTRUM_THRESHOLD, Integer.toString(threshold));

    int minPhase = localStore.getInt(MINIMUM_PHASE);
    prefState.put(MINIMUM_PHASE, Integer.toString(minPhase));

    int maxPhase = localStore.getInt(MAXIMUM_PHASE);
    prefState.put(MAXIMUM_PHASE, Integer.toString(maxPhase));

    boolean addRegressionLine = localStore.getBoolean(ADD_REGRESSION_LINE);
    prefState.put(ADD_REGRESSION_LINE, Boolean.toString(addRegressionLine));

    int minRegressionFreq = localStore.getInt(MINIMUM_REGRESSION_FREQUENCY);
    prefState.put(MINIMUM_REGRESSION_FREQUENCY, Integer.toString(minRegressionFreq));

    return prefState;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#setPreferenceState(java.util.Map)
   */
  @Override
  public void setPreferenceState(Map<String, String> prefs) {
    IPreferenceStore localStore = WAVELET_VIEWER_PREFERENCE_STORE.getLocalStore();

    WAVELET_VIEWER_PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs
        .get(PropertyStore.USE_PROJECT_SETTINGS));

    localStore.setValue(TIME_DOMAIN_SCALING, prefs.get(TIME_DOMAIN_SCALING));

    try {
      int timeScaleFactor = Integer.parseInt(prefs.get(TIME_DOMAIN_SCALE_FACTOR));
      localStore.setValue(TIME_DOMAIN_SCALE_FACTOR, timeScaleFactor);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      int maxFreq = Integer.parseInt(prefs.get(MAXIMUM_FREQUENCY));
      localStore.setValue(MAXIMUM_FREQUENCY, maxFreq);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    localStore.setValue(FREQUENCY_DOMAIN_SCALING, prefs.get(FREQUENCY_DOMAIN_SCALING));

    try {
      int freqScaleFactor = Integer.parseInt(prefs.get(FREQUENCY_DOMAIN_SCALE_FACTOR));
      localStore.setValue(FREQUENCY_DOMAIN_SCALE_FACTOR, freqScaleFactor);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    localStore.setValue(SPECTRUM_TYPE, prefs.get(SPECTRUM_TYPE));

    try {
      int dbRange = Integer.parseInt(prefs.get(DECIBEL_RANGE));
      localStore.setValue(DECIBEL_RANGE, dbRange);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      boolean displayPhasePlot = Boolean.parseBoolean(prefs.get(DISPLAY_PHASE_PLOT));
      localStore.setValue(DISPLAY_PHASE_PLOT, displayPhasePlot);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      boolean unwrapPhase = Boolean.parseBoolean(prefs.get(UNWRAP_PHASE));
      localStore.setValue(UNWRAP_PHASE, unwrapPhase);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      int threshold = Integer.parseInt(prefs.get(AMPLITUDE_SPECTRUM_THRESHOLD));
      localStore.setValue(AMPLITUDE_SPECTRUM_THRESHOLD, threshold);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      int minPhase = Integer.parseInt(prefs.get(MINIMUM_PHASE));
      localStore.setValue(MINIMUM_PHASE, minPhase);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      int maxPhase = Integer.parseInt(prefs.get(MAXIMUM_PHASE));
      localStore.setValue(MAXIMUM_PHASE, maxPhase);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      boolean addRegressionLine = Boolean.parseBoolean(prefs.get(ADD_REGRESSION_LINE));
      localStore.setValue(ADD_REGRESSION_LINE, addRegressionLine);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      int minRegressionFreq = Integer.parseInt(prefs.get(MINIMUM_REGRESSION_FREQUENCY));
      localStore.setValue(MINIMUM_REGRESSION_FREQUENCY, minRegressionFreq);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }
  }
}
