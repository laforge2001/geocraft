/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.preferences;


import java.util.HashMap;
import java.util.Map;

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
import org.geocraft.internal.abavo.PreferencesConstants;


/**
 * The page for setting preferences for the ABAVO crossplot related to the ellipse regions.
 */
public class CrossplotPreferencePage3 extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage {

  public static PropertyStore CROSSPLOT_PREFERENCE_STORE3 = PropertyStoreFactory.getStore(CrossplotPreferencePage3.ID);

  public static final String ID = "org.geocraft.abavo.crossplot3";

  static {
    setDefaults();
  }

  public CrossplotPreferencePage3() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for the ABAVO crossplot ellipses.");
  }

  /**
   * Sets the default values for the crossplot preferences.
   */
  public static void setDefaults() {
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.MINIMUM_ELLIPSE_COLOR, new RGB(255, 0, 0));
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.MAXIMUM_ELLIPSE_COLOR, new RGB(0, 255, 0));
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.SELECTION_ELLIPSE_COLOR, new RGB(0, 0, 255));
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.MINIMUM_ELLIPSE_LINE_WIDTH, 2);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.MAXIMUM_ELLIPSE_LINE_WIDTH, 2);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.SELECTION_ELLIPSE_LINE_WIDTH, 2);
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();
    setDefaults();

    Group group = createGroup(parent, "Background Ellipse");

    ColorFieldEditor minimumLineColor = new ColorFieldEditor(PreferencesConstants.MINIMUM_ELLIPSE_COLOR, "Color:",
        group);
    addField(minimumLineColor);

    IntegerFieldEditor minimumLineWidth = new IntegerFieldEditor(PreferencesConstants.MINIMUM_ELLIPSE_LINE_WIDTH,
        "Line Width:", group);
    addField(minimumLineWidth);

    group = createGroup(parent, "Maximum Ellipse");

    ColorFieldEditor maximumLineColor = new ColorFieldEditor(PreferencesConstants.MAXIMUM_ELLIPSE_COLOR, "Color:",
        group);
    addField(maximumLineColor);

    IntegerFieldEditor maximumLineWidth = new IntegerFieldEditor(PreferencesConstants.MAXIMUM_ELLIPSE_LINE_WIDTH,
        "Line Width:", group);
    addField(maximumLineWidth);

    group = createGroup(parent, "Selection Ellipse");

    ColorFieldEditor selectionLineColor = new ColorFieldEditor(PreferencesConstants.SELECTION_ELLIPSE_COLOR, "Color:",
        group);
    addField(selectionLineColor);

    IntegerFieldEditor selectionLineWidth = new IntegerFieldEditor(PreferencesConstants.SELECTION_ELLIPSE_LINE_WIDTH,
        "Line Width:", group);
    addField(selectionLineWidth);

    // Set the preference defaults.
    setDefaults();

    // Update the editor widgets from the preferences.
    minimumLineColor.load();
    minimumLineWidth.load();
    maximumLineColor.load();
    maximumLineWidth.load();
    selectionLineColor.load();
    selectionLineWidth.load();

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
    IPreferenceStore localStore = CROSSPLOT_PREFERENCE_STORE3.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    RGB minEclipseColor = PreferenceConverter.getColor(localStore, PreferencesConstants.MINIMUM_ELLIPSE_COLOR);
    prefState.put(PreferencesConstants.MINIMUM_ELLIPSE_COLOR, minEclipseColor.toString());

    RGB maxEclipseColor = PreferenceConverter.getColor(localStore, PreferencesConstants.MAXIMUM_ELLIPSE_COLOR);
    prefState.put(PreferencesConstants.MAXIMUM_ELLIPSE_COLOR, maxEclipseColor.toString());

    RGB selectionEclipseColor = PreferenceConverter.getColor(localStore, PreferencesConstants.SELECTION_ELLIPSE_COLOR);
    prefState.put(PreferencesConstants.SELECTION_ELLIPSE_COLOR, selectionEclipseColor.toString());

    int minEclipseLineWidth = localStore.getInt(PreferencesConstants.MINIMUM_ELLIPSE_LINE_WIDTH);
    prefState.put(PreferencesConstants.MINIMUM_ELLIPSE_LINE_WIDTH, Integer.toString(minEclipseLineWidth));

    int maxEclipseLineWidth = localStore.getInt(PreferencesConstants.MAXIMUM_ELLIPSE_LINE_WIDTH);
    prefState.put(PreferencesConstants.MAXIMUM_ELLIPSE_LINE_WIDTH, Integer.toString(maxEclipseLineWidth));

    int selectionEclipseLineWidth = localStore.getInt(PreferencesConstants.SELECTION_ELLIPSE_LINE_WIDTH);
    prefState.put(PreferencesConstants.SELECTION_ELLIPSE_LINE_WIDTH, Integer.toString(selectionEclipseLineWidth));

    return prefState;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#setPreferenceState(java.util.Map)
   */
  @Override
  public void setPreferenceState(Map<String, String> prefs) {
    IPreferenceStore localStore = CROSSPLOT_PREFERENCE_STORE3.getLocalStore();

    CROSSPLOT_PREFERENCE_STORE3.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs
        .get(PropertyStore.USE_PROJECT_SETTINGS));

    RGB minEclipseColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.MINIMUM_ELLIPSE_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.MINIMUM_ELLIPSE_COLOR, minEclipseColor);

    RGB maxEclipseColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.MAXIMUM_ELLIPSE_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.MAXIMUM_ELLIPSE_COLOR, maxEclipseColor);

    RGB selectionEclipseColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.SELECTION_ELLIPSE_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.SELECTION_ELLIPSE_COLOR, selectionEclipseColor);

    try {
      int minEclipseLineWidth = Integer.parseInt(prefs.get(PreferencesConstants.MINIMUM_ELLIPSE_LINE_WIDTH));
      localStore.setValue(PreferencesConstants.MINIMUM_ELLIPSE_LINE_WIDTH, minEclipseLineWidth);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      int maxEclipseLineWidth = Integer.parseInt(prefs.get(PreferencesConstants.MAXIMUM_ELLIPSE_LINE_WIDTH));
      localStore.setValue(PreferencesConstants.MAXIMUM_ELLIPSE_LINE_WIDTH, maxEclipseLineWidth);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    try {
      int selectionEclipseLineWidth = Integer.parseInt(prefs.get(PreferencesConstants.SELECTION_ELLIPSE_LINE_WIDTH));
      localStore.setValue(PreferencesConstants.SELECTION_ELLIPSE_LINE_WIDTH, selectionEclipseLineWidth);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }
  }
}