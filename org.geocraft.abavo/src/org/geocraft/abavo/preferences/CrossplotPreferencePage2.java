/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.preferences;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.internal.abavo.PreferencesConstants;
import org.geocraft.ui.plot.defs.PointStyle;


/**
 * The page for setting preferences for the ABAVO crossplot related to the data series.
 */
public class CrossplotPreferencePage2 extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage {

  public static PropertyStore CROSSPLOT_PREFERENCE_STORE2 = PropertyStoreFactory.getStore(CrossplotPreferencePage2.ID);

  /** The preference page ID. */
  public static final String ID = "org.geocraft.abavo.crossplot2";

  /** The available options for the data series point styles. */
  private static final PointStyle[] POINT_STYLES = { PointStyle.CIRCLE, PointStyle.SQUARE, PointStyle.TRIANGLE,
      PointStyle.DIAMOND, PointStyle.CROSS, PointStyle.FILLED_CIRCLE, PointStyle.FILLED_SQUARE,
      PointStyle.FILLED_TRIANGLE, PointStyle.FILLED_DIAMOND, PointStyle.X };

  private static final int[] POINT_COLORS = { SWT.COLOR_BLUE, SWT.COLOR_RED, SWT.COLOR_GREEN, SWT.COLOR_CYAN,
      SWT.COLOR_MAGENTA };

  static {
    setDefaults();
  }

  public CrossplotPreferencePage2() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for the ABAVO crossplot data series.");
  }

  /**
   * Sets the default values for the crossplot preferences.
   */
  public static void setDefaults() {
    for (int i = 0; i < IABavoCrossplot.MAX_SERIES; i++) {
      int id = i + 1;
      Color color = Display.getCurrent().getSystemColor(POINT_COLORS[i % 5]);
      PreferencesUtil.getService(ID).setDefault(PreferencesConstants.DATA_SERIES_COLOR + id, color.getRGB());
      PreferencesUtil.getService(ID).setDefault(PreferencesConstants.DATA_SERIES_SIZE + id, 3);
      PreferencesUtil.getService(ID).setDefault(PreferencesConstants.DATA_SERIES_SYMBOL + id,
          POINT_STYLES[i % 10].getName());
    }
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();
    setDefaults();
    for (int i = 0; i < IABavoCrossplot.MAX_SERIES; i++) {
      int id = i + 1;

      Group group = createGroup(parent, "Data Series #" + id);

      ColorFieldEditor seriesColor = new ColorFieldEditor(PreferencesConstants.DATA_SERIES_COLOR + id, "Color:", group);
      addField(seriesColor);

      IntegerFieldEditor seriesSize = new IntegerFieldEditor(PreferencesConstants.DATA_SERIES_SIZE + id, "Size:", group);
      addField(seriesSize);

      PointStyle[] styles = PointStyle.values();
      String[][] entryNamesAndValues = new String[styles.length][2];
      for (int j = 0; j < styles.length; j++) {
        entryNamesAndValues[j][0] = styles[j].getName();
        entryNamesAndValues[j][1] = styles[j].getName();
      }
      ComboFieldEditor seriesSymbol = new ComboFieldEditor(PreferencesConstants.DATA_SERIES_SYMBOL + id, "Symbol:",
          entryNamesAndValues, group);
      addField(seriesSymbol);

      // Set the preference defaults.
      setDefaults();

      // Update the editor widgets from the preferences.
      seriesColor.load();
      seriesSize.load();
      seriesSymbol.load();
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

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#getPreferenceState()
   */
  @Override
  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = CROSSPLOT_PREFERENCE_STORE2.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    for (int i = 0; i < IABavoCrossplot.MAX_SERIES; i++) {
      int id = i + 1;

      RGB color = PreferenceConverter.getColor(localStore, PreferencesConstants.DATA_SERIES_COLOR + id);
      prefState.put(PreferencesConstants.DATA_SERIES_COLOR + id, color.toString());

      int size = localStore.getInt(PreferencesConstants.DATA_SERIES_SIZE + id);
      prefState.put(PreferencesConstants.DATA_SERIES_SIZE + id, Integer.toString(size));

      String symbol = localStore.getString(PreferencesConstants.DATA_SERIES_SYMBOL + id);
      prefState.put(PreferencesConstants.DATA_SERIES_SYMBOL + id, symbol);
    }

    return prefState;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#setPreferenceState(java.util.Map)
   */
  @Override
  public void setPreferenceState(Map<String, String> prefs) {
    IPreferenceStore localStore = CROSSPLOT_PREFERENCE_STORE2.getLocalStore();

    CROSSPLOT_PREFERENCE_STORE2.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs
        .get(PropertyStore.USE_PROJECT_SETTINGS));

    for (int i = 0; i < IABavoCrossplot.MAX_SERIES; i++) {
      int id = i + 1;

      RGB color = PropertyStore.rgbValue(prefs.get(PreferencesConstants.DATA_SERIES_COLOR + id));
      PreferenceConverter.setValue(localStore, PreferencesConstants.DATA_SERIES_COLOR + id, color);

      try {
        int size = Integer.parseInt(prefs.get(PreferencesConstants.DATA_SERIES_SIZE + id));
        localStore.setValue(PreferencesConstants.DATA_SERIES_SIZE + id, size);
      } catch (NumberFormatException nfe) {
        //leave value as default
      }

      localStore.setValue(PreferencesConstants.DATA_SERIES_SYMBOL + id, prefs
          .get(PreferencesConstants.DATA_SERIES_SYMBOL + id));
    }
  }
}
