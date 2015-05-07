package org.geocraft.ui.volumeviewer.renderer.well;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
import org.geocraft.core.common.preferences.GeocraftPreferenceService;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;


public class WellRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IWellRendererConstants {

  public static final String ID = "org.geocraft.ui.volumeviewer.well";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(WellRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public WellRendererPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for well plotting in the 3D viewer.");
  }

  public static void setDefaults() {
    final GeocraftPreferenceService service = PreferencesUtil.getService(ID);

    // Initialize the bore preferences.
    service.setDefault(WELL_BORE_RADIUS, DEFAULT_WELL_BORE_RADIUS);
    service.setDefault(WELL_BORE_COLOR, DEFAULT_WELL_BORE_COLOR);

    // Initialize the pick preferences.
    service.setDefault(WELL_PICK_RADIUS, DEFAULT_WELL_PICK_RADIUS);
    service.setDefault(WELL_PICK_COLOR, DEFAULT_WELL_PICK_COLOR);
  }

  @Override
  protected void createFieldEditors() {
    final Composite parent = getFieldEditorParent();

    final Group boreGroup = createGroup(parent, "Bore");

    final IntegerFieldEditor boreRadiusField = new IntegerFieldEditor(WELL_BORE_RADIUS, WELL_BORE_RADIUS, boreGroup);
    addField(boreRadiusField);

    final ColorFieldEditor boreColorField = new ColorFieldEditor(WELL_BORE_COLOR, WELL_BORE_COLOR, boreGroup);
    addField(boreColorField);

    final Group pickGroup = createGroup(parent, "Picks");

    final IntegerFieldEditor pickRadiusField = new IntegerFieldEditor(WELL_PICK_RADIUS, WELL_PICK_RADIUS, pickGroup);
    addField(pickRadiusField);

    final ColorFieldEditor pickColorField = new ColorFieldEditor(WELL_PICK_COLOR, WELL_PICK_COLOR, pickGroup);
    addField(pickColorField);

    setDefaults();

    boreRadiusField.load();
    boreColorField.load();
    pickRadiusField.load();
    pickColorField.load();
  }

  public Map<String, String> getPreferenceState() {
    final HashMap<String, String> prefState = new HashMap<String, String>();
    final IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    final String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    final int boreRadius = localStore.getInt(WELL_BORE_RADIUS);
    prefState.put(WELL_BORE_RADIUS, Integer.toString(boreRadius));

    final RGB boreColor = PreferenceConverter.getColor(localStore, WELL_BORE_COLOR);
    prefState.put(WELL_BORE_COLOR, boreColor.toString());

    final int pickRadius = localStore.getInt(WELL_PICK_RADIUS);
    prefState.put(WELL_PICK_RADIUS, Integer.toString(pickRadius));

    final RGB pickColor = PreferenceConverter.getColor(localStore, WELL_PICK_COLOR);
    prefState.put(WELL_PICK_COLOR, pickColor.toString());

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
      final int boreRadius = Integer.parseInt(prefs.get(WELL_BORE_RADIUS));
      localStore.setValue(WELL_BORE_RADIUS, boreRadius);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final RGB boreColor = PropertyStore.rgbValue(prefs.get(WELL_BORE_COLOR));
      PreferenceConverter.setValue(localStore, WELL_BORE_COLOR, boreColor);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final int pickRadius = Integer.parseInt(prefs.get(WELL_PICK_RADIUS));
      localStore.setValue(WELL_PICK_RADIUS, pickRadius);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final RGB pickColor = PropertyStore.rgbValue(prefs.get(WELL_PICK_COLOR));
      PreferenceConverter.setValue(localStore, WELL_PICK_COLOR, pickColor);
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
