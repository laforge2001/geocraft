package org.geocraft.ui.mapviewer.renderer.fault;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
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


public class FaultRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IFaultRendererConstants {

  public static final String ID = "org.geocraft.ui.mapviewer.fault";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(FaultRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public FaultRendererPreferencePage() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for fault plotting in the map viewer.");
  }

  public static void setDefaults() {

    // Initialize the fault pick preferences.
    PreferencesUtil.getService(ID).setDefault(SEGMENTS_VISIBLE, true);
    PreferencesUtil.getService(ID).setDefault(SEGMENTS_LINE_STYLE, LineStyle.SOLID.getName());
    PreferencesUtil.getService(ID).setDefault(SEGMENTS_LINE_WIDTH, 2);

    // Initialize the fault bore preferences.
    PreferencesUtil.getService(ID).setDefault(TRIANGLES_VISIBLE, true);
    PreferencesUtil.getService(ID).setDefault(TRIANGLES_LINE_STYLE, LineStyle.SOLID.getName());
    PreferencesUtil.getService(ID).setDefault(TRIANGLES_LINE_WIDTH, 2);
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    Group faultPicksGroup = createGroup(parent, "Fault Picks");

    BooleanFieldEditor faultPicksVisibleField = new BooleanFieldEditor(SEGMENTS_VISIBLE, SEGMENTS_VISIBLE,
        faultPicksGroup);
    addField(faultPicksVisibleField);

    LineStyle[] lineStyles = LineStyle.values();
    String[][] entryNamesAndValues = new String[lineStyles.length][2];
    for (int i = 0; i < lineStyles.length; i++) {
      entryNamesAndValues[i][0] = lineStyles[i].getName();
      entryNamesAndValues[i][1] = lineStyles[i].getName();
    }
    ComboFieldEditor faultPicksLineStyleField = new ComboFieldEditor(SEGMENTS_LINE_STYLE, SEGMENTS_LINE_STYLE,
        entryNamesAndValues, faultPicksGroup);
    addField(faultPicksLineStyleField);

    IntegerFieldEditor faultPicksLineWidthField = new IntegerFieldEditor(SEGMENTS_LINE_WIDTH, SEGMENTS_LINE_WIDTH,
        faultPicksGroup);
    addField(faultPicksLineWidthField);

    Group faultTrianglesGroup = createGroup(parent, "Fault Triangles");

    BooleanFieldEditor faultTrianglesVisibleField = new BooleanFieldEditor(TRIANGLES_VISIBLE, TRIANGLES_VISIBLE,
        faultTrianglesGroup);
    addField(faultTrianglesVisibleField);

    ComboFieldEditor faultTriangleLineStyleField = new ComboFieldEditor(TRIANGLES_LINE_STYLE, TRIANGLES_LINE_STYLE,
        entryNamesAndValues, faultTrianglesGroup);
    addField(faultTriangleLineStyleField);

    IntegerFieldEditor faultTrianglesLineWidthField = new IntegerFieldEditor(TRIANGLES_LINE_WIDTH,
        TRIANGLES_LINE_WIDTH, faultTrianglesGroup);
    addField(faultTrianglesLineWidthField);

    setDefaults();

    faultPicksVisibleField.load();
    faultPicksLineStyleField.load();
    faultPicksLineWidthField.load();
    faultTrianglesVisibleField.load();
    faultTriangleLineStyleField.load();
    faultTrianglesLineWidthField.load();
  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    boolean picksVisible = localStore.getBoolean(SEGMENTS_VISIBLE);
    prefState.put(SEGMENTS_VISIBLE, Boolean.toString(picksVisible));

    String picksLineStyle = localStore.getString(SEGMENTS_LINE_STYLE);
    prefState.put(SEGMENTS_LINE_STYLE, picksLineStyle);

    int picksLineWidth = localStore.getInt(SEGMENTS_LINE_WIDTH);
    prefState.put(SEGMENTS_LINE_WIDTH, Integer.toString(picksLineWidth));

    boolean trianglesVisible = localStore.getBoolean(TRIANGLES_VISIBLE);
    prefState.put(TRIANGLES_VISIBLE, Boolean.toString(trianglesVisible));

    String trianglesLineStyle = localStore.getString(TRIANGLES_LINE_STYLE);
    prefState.put(TRIANGLES_LINE_STYLE, trianglesLineStyle);

    int trianglesLineWidth = localStore.getInt(TRIANGLES_LINE_WIDTH);
    prefState.put(TRIANGLES_LINE_WIDTH, Integer.toString(trianglesLineWidth));

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
      boolean picksVisible = Boolean.parseBoolean(prefs.get(SEGMENTS_VISIBLE));
      localStore.setValue(SEGMENTS_VISIBLE, picksVisible);
    } catch (Exception nfe) {
      // Leave as default.
    }

    try {
      localStore.setValue(SEGMENTS_LINE_STYLE, prefs.get(SEGMENTS_LINE_STYLE));
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      int picksLineWidth = Integer.parseInt(prefs.get(SEGMENTS_LINE_WIDTH));
      localStore.setValue(SEGMENTS_LINE_WIDTH, picksLineWidth);
    } catch (Exception nfe) {
      // Leave as default.
    }

    try {
      boolean trianglesVisible = Boolean.parseBoolean(prefs.get(TRIANGLES_VISIBLE));
      localStore.setValue(TRIANGLES_VISIBLE, trianglesVisible);
    } catch (Exception nfe) {
      // Leave as default.
    }

    try {
      localStore.setValue(TRIANGLES_LINE_STYLE, prefs.get(TRIANGLES_LINE_STYLE));
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      int trianglesLineWidth = Integer.parseInt(prefs.get(TRIANGLES_LINE_WIDTH));
      localStore.setValue(TRIANGLES_LINE_WIDTH, trianglesLineWidth);
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
