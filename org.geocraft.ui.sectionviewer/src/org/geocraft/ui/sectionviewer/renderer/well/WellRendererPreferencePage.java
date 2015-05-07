/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.well;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
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


public class WellRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IWellRendererConstants {

  public static final String ID = "org.geocraft.ui.sectionviewer.well";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(WellRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public WellRendererPreferencePage() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for well plotting in the section viewer.");
  }

  public static void setDefaults() {
    // Initialize the general preferences.
    PreferencesUtil.getService(ID).setDefault(DISTANCE_TOLERANCE, 1000);

    // Initialize the well bore preferences.
    PreferencesUtil.getService(ID).setDefault(SHOW_BORE_LABELS, true);
    PreferencesUtil.getService(ID).setDefault(BORE_LINE_WIDTH, 1);
    PreferencesUtil.getService(ID).setDefault(BORE_LINE_COLOR, new RGB(0, 255, 0));

    // Initialize the well pick preferences.
    PreferencesUtil.getService(ID).setDefault(SHOW_PICKS, true);
    PreferencesUtil.getService(ID).setDefault(PICK_SYMBOL_COLOR, new RGB(255, 0, 255));
    PreferencesUtil.getService(ID).setDefault(SHOW_PICK_LABELS, true);
    PreferencesUtil.getService(ID).setDefault(PICK_LABEL_COLOR, new RGB(255, 255, 0));

    // Initialize the well log preferences.
    PreferencesUtil.getService(ID).setDefault(SHOW_LOGS, false);
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    Group generalGroup = createGroup(parent, "General");

    IntegerFieldEditor distanceToleranceField = new IntegerFieldEditor(DISTANCE_TOLERANCE, "Distance Tolerance",
        generalGroup);
    addField(distanceToleranceField);

    Group wellBoreGroup = createGroup(parent, "Well Bores");

    BooleanFieldEditor wellBoresLabelsField = new BooleanFieldEditor(SHOW_BORE_LABELS, "Show Labels", wellBoreGroup);
    addField(wellBoresLabelsField);

    IntegerFieldEditor wellBoresLineWidthField = new IntegerFieldEditor(BORE_LINE_WIDTH, "Line Width", wellBoreGroup);
    addField(wellBoresLineWidthField);

    ColorFieldEditor wellBoresLineColorField = new ColorFieldEditor(BORE_LINE_COLOR, "Line Color", wellBoreGroup);
    addField(wellBoresLineColorField);

    Group wellPicksGroup = createGroup(parent, "Well Picks");

    BooleanFieldEditor wellPicksVisibleField = new BooleanFieldEditor(SHOW_PICKS, "Show Picks", wellPicksGroup);
    addField(wellPicksVisibleField);

    ColorFieldEditor wellPicksSymbolsColorField = new ColorFieldEditor(PICK_SYMBOL_COLOR, "Symbol Color",
        wellPicksGroup);
    addField(wellPicksSymbolsColorField);

    BooleanFieldEditor wellPicksLabelsVisibleField = new BooleanFieldEditor(SHOW_PICK_LABELS, "Show Labels",
        wellPicksGroup);
    addField(wellPicksLabelsVisibleField);

    ColorFieldEditor wellPicksLabelsColorField = new ColorFieldEditor(PICK_LABEL_COLOR, "Label Color", wellPicksGroup);
    addField(wellPicksLabelsColorField);

    Group wellLogsGroup = createGroup(parent, "Well Logs");

    BooleanFieldEditor wellLogsVisibleField = new BooleanFieldEditor(SHOW_LOGS, "Show Logs", wellLogsGroup);
    addField(wellLogsVisibleField);

    setDefaults();

    distanceToleranceField.load();
    wellBoresLabelsField.load();
    wellBoresLineWidthField.load();
    wellBoresLineColorField.load();
    wellPicksVisibleField.load();
    wellPicksSymbolsColorField.load();
    wellPicksLabelsVisibleField.load();
    wellPicksLabelsColorField.load();
    wellLogsVisibleField.load();
  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    int distanceTolerance = localStore.getInt(DISTANCE_TOLERANCE);
    prefState.put(DISTANCE_TOLERANCE, Integer.toString(distanceTolerance));

    boolean showBoreLabels = localStore.getBoolean(SHOW_BORE_LABELS);
    prefState.put(SHOW_BORE_LABELS, Boolean.toString(showBoreLabels));

    int boreLineWidth = localStore.getInt(BORE_LINE_WIDTH);
    prefState.put(BORE_LINE_WIDTH, Integer.toString(boreLineWidth));

    RGB boreLineColor = PreferenceConverter.getColor(localStore, BORE_LINE_COLOR);
    prefState.put(BORE_LINE_COLOR, boreLineColor.toString());

    boolean showPicks = localStore.getBoolean(SHOW_PICKS);
    prefState.put(SHOW_PICKS, Boolean.toString(showPicks));

    RGB pickSymbolColor = PreferenceConverter.getColor(localStore, PICK_SYMBOL_COLOR);
    prefState.put(PICK_SYMBOL_COLOR, pickSymbolColor.toString());

    boolean showPickLabels = localStore.getBoolean(SHOW_PICK_LABELS);
    prefState.put(SHOW_PICK_LABELS, Boolean.toString(showPickLabels));

    RGB pickLabelColor = PreferenceConverter.getColor(localStore, PICK_LABEL_COLOR);
    prefState.put(PICK_LABEL_COLOR, pickLabelColor.toString());

    boolean showLogs = localStore.getBoolean(SHOW_LOGS);
    prefState.put(SHOW_LOGS, Boolean.toString(showLogs));

    return prefState;
  }

  public void setPreferenceState(final Map<String, String> prefs) {

    try {
      PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs.get(PropertyStore.USE_PROJECT_SETTINGS));
    } catch (Exception ex) {
      // Leave as default.
    }

    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    try {
      int distanceTolerance = Integer.parseInt(prefs.get(DISTANCE_TOLERANCE));
      localStore.setValue(DISTANCE_TOLERANCE, distanceTolerance);
    } catch (Exception nfe) {
      // Leave value as default.
    }

    try {
      boolean showBoreLabels = Boolean.parseBoolean(prefs.get(SHOW_BORE_LABELS));
      localStore.setValue(SHOW_BORE_LABELS, showBoreLabels);
    } catch (Exception nfe) {
      // Leave value as default.
    }

    try {
      int boreLineWidth = Integer.parseInt(prefs.get(BORE_LINE_WIDTH));
      localStore.setValue(BORE_LINE_WIDTH, boreLineWidth);
    } catch (Exception nfe) {
      // Leave value as default.
    }

    try {
      RGB boreLineColor = PropertyStore.rgbValue(prefs.get(BORE_LINE_COLOR));
      PreferenceConverter.setValue(localStore, BORE_LINE_COLOR, boreLineColor);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      boolean showPicks = Boolean.parseBoolean(prefs.get(SHOW_PICKS));
      localStore.setValue(SHOW_PICKS, showPicks);
    } catch (Exception nfe) {
      // Leave value as default.
    }

    try {
      RGB pickSymbolColor = PropertyStore.rgbValue(prefs.get(PICK_SYMBOL_COLOR));
      PreferenceConverter.setValue(localStore, PICK_SYMBOL_COLOR, pickSymbolColor);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      boolean showPickLabels = Boolean.parseBoolean(prefs.get(SHOW_PICK_LABELS));
      localStore.setValue(SHOW_PICK_LABELS, showPickLabels);
    } catch (Exception nfe) {
      // Leave value as default.
    }

    try {
      RGB pickLabelColor = PropertyStore.rgbValue(prefs.get(PICK_LABEL_COLOR));
      PreferenceConverter.setValue(localStore, PICK_LABEL_COLOR, pickLabelColor);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      boolean showLogs = Boolean.parseBoolean(prefs.get(SHOW_LOGS));
      localStore.setValue(SHOW_LOGS, showLogs);
    } catch (Exception nfe) {
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
