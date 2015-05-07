package org.geocraft.ui.mapviewer.renderer.pointset;


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
import org.geocraft.ui.plot.defs.PointStyle;


public class PointSetRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IPointSetRendererConstants {

  public static final String ID = "org.geocraft.ui.mapviewer.pointset";

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
    setDescription("Preferences for pointset plotting in the map viewer.");
  }

  public static void setDefaults() {
    GeocraftPreferenceService service = PreferencesUtil.getService(ID);

    // Initialize the point preferences.
    service.setDefault(POINT_STYLE, PointStyle.CROSS.getName());
    service.setDefault(POINT_SIZE, 2);
    service.setDefault(SIZE_BY_ATTRIBUTE, false);
    service.setDefault(POINT_SIZE_MIN, 1);
    service.setDefault(POINT_SIZE_MAX, 20);
    service.setDefault(POINT_COLOR, new RGB(255, 255, 0));
    service.setDefault(COLOR_BY_ATTRIBUTE, false);
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

    ComboFieldEditor pointStyleField = new ComboFieldEditor(POINT_STYLE, POINT_STYLE, pointStyleOptions, generalGroup);
    addField(pointStyleField);

    IntegerFieldEditor pointSizeField = new IntegerFieldEditor(POINT_SIZE, POINT_SIZE, generalGroup);
    addField(pointSizeField);

    BooleanFieldEditor sizeByAttrField = new BooleanFieldEditor(SIZE_BY_ATTRIBUTE, SIZE_BY_ATTRIBUTE, generalGroup);
    addField(sizeByAttrField);

    IntegerFieldEditor pointSizeMinField = new IntegerFieldEditor(POINT_SIZE_MIN, POINT_SIZE_MIN, generalGroup);
    addField(pointSizeMinField);

    IntegerFieldEditor pointSizeMaxField = new IntegerFieldEditor(SIZE_BY_ATTRIBUTE, SIZE_BY_ATTRIBUTE, generalGroup);
    addField(pointSizeMaxField);

    ColorFieldEditor pointColorField = new ColorFieldEditor(POINT_COLOR, POINT_COLOR, generalGroup);
    addField(pointColorField);

    BooleanFieldEditor colorByAttrField = new BooleanFieldEditor(COLOR_BY_ATTRIBUTE, COLOR_BY_ATTRIBUTE, generalGroup);
    addField(colorByAttrField);

    ColorMapDescription[] colorMapDescs = ServiceProvider.getColorMapService().getAll();
    String[][] colorMapOptions = new String[colorMapDescs.length][2];
    for (int i = 0; i < colorMapDescs.length; i++) {
      colorMapOptions[i][0] = colorMapDescs[i].getName();
      colorMapOptions[i][1] = colorMapDescs[i].getName();
    }
    ComboFieldEditor colorMapField = new ComboFieldEditor(COLOR_MAP, COLOR_MAP, colorMapOptions, generalGroup);
    addField(colorMapField);

    setDefaults();

    pointStyleField.load();
    pointSizeField.load();
    sizeByAttrField.load();
    pointSizeMinField.load();
    pointSizeMaxField.load();
    pointColorField.load();
    colorByAttrField.load();
    colorMapField.load();
  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    String pointStyle = localStore.getString(POINT_STYLE);
    prefState.put(POINT_STYLE, pointStyle);

    int pointSize = localStore.getInt(POINT_SIZE);
    prefState.put(POINT_SIZE, Integer.toString(pointSize));

    boolean sizeByAttr = localStore.getBoolean(SIZE_BY_ATTRIBUTE);
    prefState.put(SIZE_BY_ATTRIBUTE, Boolean.toString(sizeByAttr));

    int pointSizeMin = localStore.getInt(POINT_SIZE_MIN);
    prefState.put(POINT_SIZE_MIN, Integer.toString(pointSizeMin));

    int pointSizeMax = localStore.getInt(POINT_SIZE_MAX);
    prefState.put(POINT_SIZE_MAX, Integer.toString(pointSizeMax));

    RGB pointColor = PreferenceConverter.getColor(localStore, POINT_COLOR);
    prefState.put(POINT_COLOR, pointColor.toString());

    boolean colorByAttr = localStore.getBoolean(COLOR_BY_ATTRIBUTE);
    prefState.put(COLOR_BY_ATTRIBUTE, Boolean.toString(colorByAttr));

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
      localStore.setValue(POINT_STYLE, prefs.get(POINT_STYLE));
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      int pointSize = Integer.parseInt(prefs.get(POINT_SIZE));
      localStore.setValue(POINT_SIZE, pointSize);
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      boolean sizeByAttr = Boolean.parseBoolean(prefs.get(SIZE_BY_ATTRIBUTE));
      localStore.setValue(SIZE_BY_ATTRIBUTE, sizeByAttr);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      int pointSizeMin = Integer.parseInt(prefs.get(POINT_SIZE_MIN));
      localStore.setValue(POINT_SIZE_MIN, pointSizeMin);
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      int pointSizeMax = Integer.parseInt(prefs.get(POINT_SIZE_MAX));
      localStore.setValue(POINT_SIZE_MAX, pointSizeMax);
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      RGB pointColor = PropertyStore.rgbValue(prefs.get(POINT_COLOR));
      PreferenceConverter.setValue(localStore, POINT_COLOR, pointColor);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      boolean colorByAttr = Boolean.parseBoolean(prefs.get(COLOR_BY_ATTRIBUTE));
      localStore.setValue(COLOR_BY_ATTRIBUTE, colorByAttr);
    } catch (Exception ex) {
      // Leave value as default.
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
