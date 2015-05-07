package org.geocraft.ui.sectionviewer.renderer.grid;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
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
import org.geocraft.ui.plot.defs.LineStyle;


public class GridRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IGridRendererConstants {

  public static final String ID = "org.geocraft.ui.sectionviewer.grid";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(GridRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public GridRendererPreferencePage() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for grid plotting in the section viewer.");
  }

  public static void setDefaults() {

    // Initialize the grid preferences.
    PreferencesUtil.getService(ID).setDefault(LINE_STYLE, LineStyle.SOLID.getName());
    PreferencesUtil.getService(ID).setDefault(LINE_WIDTH, 2);
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    Group generalGroup = createGroup(parent, "General");

    LineStyle[] styles = LineStyle.values();
    String[][] options = new String[styles.length][2];
    for (int i = 0; i < styles.length; i++) {
      options[i][0] = styles[i].getName();
      options[i][1] = styles[i].getName();
    }
    ComboFieldEditor lineStyleField = new ComboFieldEditor(LINE_STYLE, "Line Style", options, generalGroup);
    addField(lineStyleField);

    IntegerFieldEditor lineWidthField = new IntegerFieldEditor(LINE_WIDTH, "Line Width", generalGroup);
    addField(lineWidthField);

    setDefaults();

    lineStyleField.load();
    lineWidthField.load();
  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    String lineStyle = localStore.getString(LINE_STYLE);
    prefState.put(LINE_STYLE, lineStyle);

    int lineWidth = localStore.getInt(LINE_WIDTH);
    prefState.put(LINE_WIDTH, Integer.toString(lineWidth));

    return prefState;
  }

  public void setPreferenceState(final Map<String, String> prefs) {
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs.get(PropertyStore.USE_PROJECT_SETTINGS));

    try {
      String lineStyle = prefs.get(LINE_STYLE);
      localStore.setValue(LINE_STYLE, lineStyle);
    } catch (Exception nfe) {
      // Leave as default.
    }

    try {
      int lineWidth = Integer.parseInt(prefs.get(LINE_WIDTH));
      localStore.setValue(LINE_WIDTH, lineWidth);
    } catch (Exception nfe) {
      // Leave as default.
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
