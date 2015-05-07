package org.geocraft.ui.volumeviewer.renderer.pointset;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
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
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.GeocraftPreferenceService;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.core.service.ServiceProvider;


public class PointSetRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IPointSetRendererConstants {

  public static final String ID = "org.geocraft.ui.volumeviewer.pointset";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(PointSetRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public PointSetRendererPreferencePage() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for pointset plotting in the 3D viewer.");
  }

  public static void setDefaults() {
    final GeocraftPreferenceService service = PreferencesUtil.getService(ID);

    // Initialize the point preferences.
    service.setDefault(POINT_STYLE, PointStyle.SPHERE.getName());
    service.setDefault(POINT_SIZE, 10);
    service.setDefault(SIZE_BY_ATTRIBUTE, false);
    service.setDefault(POINT_SIZE_MIN, 5);
    service.setDefault(POINT_SIZE_MAX, 25);
    service.setDefault(POINT_COLOR, new RGB(255, 255, 0));
    service.setDefault(COLOR_BY_ATTRIBUTE, false);
    service.setDefault(COLOR_MAP, SpectrumColorMap.COLOR_MAP_NAME);
    service.setDefault(THRESHOLD_BY_ATTRIBUTE, false);
  }

  @Override
  protected void createFieldEditors() {
    final Composite parent = getFieldEditorParent();

    final Group generalGroup = createGroup(parent, "General");

    final IntegerFieldEditor decimationField = new IntegerFieldEditor(DECIMATION, DECIMATION, generalGroup);
    addField(decimationField);

    final PointStyle[] pointStyles = PointStyle.values();
    final String[][] pointStyleOptions = new String[pointStyles.length][2];
    for (int i = 0; i < pointStyles.length; i++) {
      pointStyleOptions[i][0] = pointStyles[i].getName();
      pointStyleOptions[i][1] = pointStyles[i].getName();
    }
    final ComboFieldEditor pointStyleField = new ComboFieldEditor(POINT_STYLE, POINT_STYLE, pointStyleOptions,
        generalGroup);
    addField(pointStyleField);

    final IntegerFieldEditor pointSizeField = new IntegerFieldEditor(POINT_SIZE, POINT_SIZE, generalGroup);
    addField(pointSizeField);

    final BooleanFieldEditor sizeByAttrField = new BooleanFieldEditor(SIZE_BY_ATTRIBUTE, SIZE_BY_ATTRIBUTE,
        generalGroup);
    addField(sizeByAttrField);

    final IntegerFieldEditor pointSizeMinField = new IntegerFieldEditor(POINT_SIZE_MIN, POINT_SIZE_MIN, generalGroup);
    addField(pointSizeMinField);

    final IntegerFieldEditor pointSizeMaxField = new IntegerFieldEditor(SIZE_BY_ATTRIBUTE, SIZE_BY_ATTRIBUTE,
        generalGroup);
    addField(pointSizeMaxField);

    final ColorFieldEditor pointColorField = new ColorFieldEditor(POINT_COLOR, POINT_COLOR, generalGroup);
    addField(pointColorField);

    final BooleanFieldEditor colorByAttrField = new BooleanFieldEditor(COLOR_BY_ATTRIBUTE, COLOR_BY_ATTRIBUTE,
        generalGroup);
    addField(colorByAttrField);

    final ColorMapDescription[] colorMapDescs = ServiceProvider.getColorMapService().getAll();
    final String[][] colorMapOptions = new String[colorMapDescs.length][2];
    for (int i = 0; i < colorMapDescs.length; i++) {
      colorMapOptions[i][0] = colorMapDescs[i].getName();
      colorMapOptions[i][1] = colorMapDescs[i].getName();
    }
    final ComboFieldEditor colorMapField = new ComboFieldEditor(COLOR_MAP, COLOR_MAP, colorMapOptions, generalGroup);
    addField(colorMapField);

    final BooleanFieldEditor thresholdByAttrField = new BooleanFieldEditor(THRESHOLD_BY_ATTRIBUTE,
        THRESHOLD_BY_ATTRIBUTE, generalGroup);
    addField(thresholdByAttrField);

    setDefaults();

    decimationField.load();
    pointStyleField.load();
    pointSizeField.load();
    sizeByAttrField.load();
    pointSizeMinField.load();
    pointSizeMaxField.load();
    pointColorField.load();
    colorByAttrField.load();
    colorMapField.load();
    thresholdByAttrField.load();
  }

  public Map<String, String> getPreferenceState() {
    final HashMap<String, String> prefState = new HashMap<String, String>();
    final IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    final String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    final int decimation = Math.max(1, localStore.getInt(DECIMATION));
    prefState.put(DECIMATION, Integer.toString(decimation));

    final String pointStyle = localStore.getString(POINT_STYLE);
    prefState.put(POINT_STYLE, pointStyle);

    final int pointSize = localStore.getInt(POINT_SIZE);
    prefState.put(POINT_SIZE, Integer.toString(pointSize));

    final boolean sizeByAttr = localStore.getBoolean(SIZE_BY_ATTRIBUTE);
    prefState.put(SIZE_BY_ATTRIBUTE, Boolean.toString(sizeByAttr));

    final int pointSizeMin = localStore.getInt(POINT_SIZE_MIN);
    prefState.put(POINT_SIZE_MIN, Integer.toString(pointSizeMin));

    final int pointSizeMax = localStore.getInt(POINT_SIZE_MAX);
    prefState.put(POINT_SIZE_MAX, Integer.toString(pointSizeMax));

    final boolean colorByAttr = localStore.getBoolean(COLOR_BY_ATTRIBUTE);
    prefState.put(COLOR_BY_ATTRIBUTE, Boolean.toString(colorByAttr));

    final RGB pointColor = PreferenceConverter.getColor(localStore, POINT_COLOR);
    prefState.put(POINT_COLOR, pointColor.toString());

    final String colorMap = localStore.getString(COLOR_MAP);
    prefState.put(COLOR_MAP, colorMap);

    final boolean thresholdByAttr = localStore.getBoolean(THRESHOLD_BY_ATTRIBUTE);
    prefState.put(THRESHOLD_BY_ATTRIBUTE, Boolean.toString(thresholdByAttr));

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
      final int decimation = Integer.parseInt(prefs.get(DECIMATION));
      localStore.setValue(DECIMATION, decimation);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      localStore.setValue(POINT_STYLE, prefs.get(POINT_STYLE));
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final int pointSize = Integer.parseInt(prefs.get(POINT_SIZE));
      localStore.setValue(POINT_SIZE, pointSize);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final boolean sizeByAttr = Boolean.parseBoolean(prefs.get(SIZE_BY_ATTRIBUTE));
      localStore.setValue(SIZE_BY_ATTRIBUTE, sizeByAttr);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final int pointSizeMin = Integer.parseInt(prefs.get(POINT_SIZE_MIN));
      localStore.setValue(POINT_SIZE_MIN, pointSizeMin);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final int pointSizeMax = Integer.parseInt(prefs.get(POINT_SIZE_MAX));
      localStore.setValue(POINT_SIZE_MAX, pointSizeMax);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final RGB pointColor = PropertyStore.rgbValue(prefs.get(POINT_COLOR));
      PreferenceConverter.setValue(localStore, POINT_COLOR, pointColor);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final boolean colorByAttr = Boolean.parseBoolean(prefs.get(COLOR_BY_ATTRIBUTE));
      localStore.setValue(COLOR_BY_ATTRIBUTE, colorByAttr);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      localStore.setValue(COLOR_MAP, prefs.get(COLOR_MAP));
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final boolean thresholdByAttr = Boolean.parseBoolean(prefs.get(THRESHOLD_BY_ATTRIBUTE));
      localStore.setValue(THRESHOLD_BY_ATTRIBUTE, thresholdByAttr);
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
