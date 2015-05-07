package org.geocraft.ui.volumeviewer.renderer.grid;


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
import org.geocraft.ui.volumeviewer.renderer.pointset.PointStyle;


public class Grid3dRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IGridRendererConstants {

  public static final String ID = "org.geocraft.ui.volumeviewer.grid";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(Grid3dRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public Grid3dRendererPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for grid plotting in the 3D viewer.");
  }

  public static void setDefaults() {
    final GeocraftPreferenceService service = PreferencesUtil.getService(ID);

    // Initialize the point preferences.
    service.setDefault(TRANSPARENCY, 0);
    service.setDefault(SMOOTHING_METHOD, SmoothingMethod.NONE.toString());
    service.setDefault(SHOW_MESH, false);
    service.setDefault(PERCENTILE, 0);
    service.setDefault(COLOR_MAP, SpectrumColorMap.COLOR_MAP_NAME);
  }

  @Override
  protected void createFieldEditors() {
    final Composite parent = getFieldEditorParent();

    final Group generalGroup = createGroup(parent, "General");

    final PointStyle[] pointStyles = PointStyle.values();
    final String[][] pointStyleOptions = new String[pointStyles.length][2];
    for (int i = 0; i < pointStyles.length; i++) {
      pointStyleOptions[i][0] = pointStyles[i].getName();
      pointStyleOptions[i][1] = pointStyles[i].getName();
    }

    final IntegerFieldEditor transparencyField = new IntegerFieldEditor(TRANSPARENCY, TRANSPARENCY, generalGroup);
    addField(transparencyField);

    final SmoothingMethod[] smoothMethods = SmoothingMethod.values();
    final String[][] smoothOptions = new String[smoothMethods.length][2];
    for (int i = 0; i < smoothMethods.length; i++) {
      smoothOptions[i][0] = smoothMethods[i].toString();
      smoothOptions[i][1] = smoothMethods[i].toString();
    }
    final ComboFieldEditor smoothImageField = new ComboFieldEditor(SMOOTHING_METHOD, SMOOTHING_METHOD, smoothOptions,
        generalGroup);
    addField(smoothImageField);

    final BooleanFieldEditor showMeshField = new BooleanFieldEditor(SHOW_MESH, SHOW_MESH, generalGroup);
    addField(showMeshField);

    final Group colorGroup = createGroup(parent, "Colors");

    final IntegerFieldEditor percentileField = new IntegerFieldEditor(PERCENTILE, PERCENTILE, colorGroup);
    addField(percentileField);

    final ColorMapDescription[] colorMapDescs = ServiceProvider.getColorMapService().getAll();
    final String[][] colorMapOptions = new String[colorMapDescs.length][2];
    for (int i = 0; i < colorMapDescs.length; i++) {
      colorMapOptions[i][0] = colorMapDescs[i].getName();
      colorMapOptions[i][1] = colorMapDescs[i].getName();
    }
    final ComboFieldEditor colorMapField = new ComboFieldEditor(COLOR_MAP, COLOR_MAP, colorMapOptions, colorGroup);
    addField(colorMapField);

    setDefaults();

    transparencyField.load();
    smoothImageField.load();
    showMeshField.load();
    percentileField.load();
    colorMapField.load();
  }

  public Map<String, String> getPreferenceState() {
    final HashMap<String, String> prefState = new HashMap<String, String>();
    final IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    final String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    final int transparency = localStore.getInt(TRANSPARENCY);
    prefState.put(TRANSPARENCY, Integer.toString(transparency));

    final String smoothMethod = localStore.getString(SMOOTHING_METHOD);
    prefState.put(SMOOTHING_METHOD, smoothMethod);

    final boolean showMesh = localStore.getBoolean(SHOW_MESH);
    prefState.put(SHOW_MESH, Boolean.toString(showMesh));

    final int percentile = localStore.getInt(PERCENTILE);
    prefState.put(PERCENTILE, Integer.toString(percentile));

    final String colorMap = localStore.getString(COLOR_MAP);
    prefState.put(COLOR_MAP, colorMap);

    return prefState;
  }

  public void setPreferenceState(final Map<String, String> prefs) {
    final IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    try {
      PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs.get(PropertyStore.USE_PROJECT_SETTINGS));
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final int transparency = Integer.parseInt(prefs.get(TRANSPARENCY));
      localStore.setValue(TRANSPARENCY, transparency);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final String smoothMethod = prefs.get(SMOOTHING_METHOD);
      localStore.setValue(SMOOTHING_METHOD, smoothMethod);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final boolean showMesh = Boolean.parseBoolean(prefs.get(SHOW_MESH));
      localStore.setValue(SHOW_MESH, showMesh);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final int percentile = Integer.parseInt(prefs.get(PERCENTILE));
      localStore.setValue(PERCENTILE, percentile);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      localStore.setValue(COLOR_MAP, prefs.get(COLOR_MAP));
    } catch (final Exception ex) {
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
    final Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setText(title);
    final GridLayout layout = new GridLayout();
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buttonGroup;
  }
}
