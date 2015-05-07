/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.polar;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.GeocraftPreferenceService;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.plot.defs.PointStyle;


public class PolarChartRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IPolarChartRendererConstants {

  public static final String ID = "org.geocraft.ui.chartviewer.renderer.polar.grid";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(PolarChartRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public PolarChartRendererPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for grid plotting on a polar plot.");
  }

  public static void setDefaults() {
    GeocraftPreferenceService service = PreferencesUtil.getService(ID);

    // Initialize the point preferences.
    service.setDefault(TRANSPARENCY, 0);
    service.setDefault(SMOOTH_IMAGE, false);
    service.setDefault(SHADED_RELIEF, false);
    service.setDefault(PERCENTILE, 0);
    service.setDefault(COLOR_MAP, SpectrumColorMap.COLOR_MAP_NAME);
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    Group generalGroup = createGroup(parent, "General");

    PointStyle[] pointStyles = PointStyle.values();
    String[][] pointStyleOptions = new String[pointStyles.length][2];
    for (int i = 0; i < pointStyles.length; i++) {
      pointStyleOptions[i][0] = pointStyles[i].getName();
      pointStyleOptions[i][1] = pointStyles[i].getName();
    }

    IntegerFieldEditor transparencyField = new IntegerFieldEditor(TRANSPARENCY, TRANSPARENCY, generalGroup);
    addField(transparencyField);

    BooleanFieldEditor smoothImageField = new BooleanFieldEditor(SMOOTH_IMAGE, SMOOTH_IMAGE, generalGroup);
    addField(smoothImageField);

    BooleanFieldEditor shadedReliefField = new BooleanFieldEditor(SHADED_RELIEF, SHADED_RELIEF, generalGroup);
    addField(shadedReliefField);

    Group colorGroup = createGroup(parent, "Colors");

    IntegerFieldEditor percentileField = new IntegerFieldEditor(PERCENTILE, PERCENTILE, colorGroup);
    addField(percentileField);

    ColorMapDescription[] colorMapDescs = ServiceProvider.getColorMapService().getAll();
    String[][] colorMapOptions = new String[colorMapDescs.length][2];
    for (int i = 0; i < colorMapDescs.length; i++) {
      colorMapOptions[i][0] = colorMapDescs[i].getName();
      colorMapOptions[i][1] = colorMapDescs[i].getName();
    }
    ComboFieldEditor colorMapField = new ComboFieldEditor(COLOR_MAP, COLOR_MAP, colorMapOptions, colorGroup);
    addField(colorMapField);

    setDefaults();

    transparencyField.load();
    smoothImageField.load();
    shadedReliefField.load();
    percentileField.load();
    colorMapField.load();
  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    int transparency = localStore.getInt(TRANSPARENCY);
    prefState.put(TRANSPARENCY, Integer.toString(transparency));

    boolean smoothImage = localStore.getBoolean(SMOOTH_IMAGE);
    prefState.put(SMOOTH_IMAGE, Boolean.toString(smoothImage));

    boolean shadedRelief = localStore.getBoolean(SHADED_RELIEF);
    prefState.put(SHADED_RELIEF, Boolean.toString(shadedRelief));

    int percentile = localStore.getInt(PERCENTILE);
    prefState.put(PERCENTILE, Integer.toString(percentile));

    String colorMap = localStore.getString(COLOR_MAP);
    prefState.put(COLOR_MAP, colorMap);

    return prefState;
  }

  public void setPreferenceState(final Map<String, String> prefs) {
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    try {
      PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs.get(PropertyStore.USE_PROJECT_SETTINGS));
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      int transparency = Integer.parseInt(prefs.get(TRANSPARENCY));
      localStore.setValue(TRANSPARENCY, transparency);
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      boolean smoothImage = Boolean.parseBoolean(prefs.get(SMOOTH_IMAGE));
      localStore.setValue(SMOOTH_IMAGE, smoothImage);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      boolean shadedRelief = Boolean.parseBoolean(prefs.get(SHADED_RELIEF));
      localStore.setValue(SHADED_RELIEF, shadedRelief);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      int percentile = Integer.parseInt(prefs.get(PERCENTILE));
      localStore.setValue(PERCENTILE, percentile);
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      localStore.setValue(COLOR_MAP, prefs.get(COLOR_MAP));
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
