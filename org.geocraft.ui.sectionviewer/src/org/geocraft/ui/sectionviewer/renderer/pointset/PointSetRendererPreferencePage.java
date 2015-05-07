package org.geocraft.ui.sectionviewer.renderer.pointset;


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
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.GeocraftPreferenceService;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


public class PointSetRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IPointSetRendererConstants {

  public static final String ID = "org.geocraft.ui.sectionviewer.pointset";

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
    setDescription("Preferences for pointset plotting in the section viewer.");
  }

  public static void setDefaults() {

    // Initialize the PointSet preferences. 
    GeocraftPreferenceService service = PreferencesUtil.getService(ID);
    service.setDefault(POINT_STYLE, PointStyle.CROSS.getName());
    service.setDefault(POINT_SIZE, 4);
    service.setDefault(LINE_STYLE, LineStyle.SOLID.getName());
    service.setDefault(LINE_WIDTH, 2);
    service.setDefault(COLOR_MAP, SpectrumColorMap.COLOR_MAP_NAME);
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    Group pointAttrGroup = createGroup(parent, "Point Attributes");

    PointStyle[] pointStyles = PointStyle.values();
    String[][] pointStyleOptions = new String[pointStyles.length][2];
    for (int i = 0; i < pointStyles.length; i++) {
      pointStyleOptions[i][0] = pointStyles[i].getName();
      pointStyleOptions[i][1] = pointStyles[i].getName();
    }
    ComboFieldEditor pointStyleField = new ComboFieldEditor(POINT_STYLE, "Point Style", pointStyleOptions,
        pointAttrGroup);
    addField(pointStyleField);

    IntegerFieldEditor pointSizeField = new IntegerFieldEditor(POINT_SIZE, "Point Size", pointAttrGroup);
    addField(pointSizeField);

    Group connectAttrGroup = createGroup(parent, "Connection Attributes");

    LineStyle[] lineStyles = LineStyle.values();
    String[][] lineStyleOptionss = new String[lineStyles.length][2];
    for (int i = 0; i < lineStyles.length; i++) {
      lineStyleOptionss[i][0] = lineStyles[i].getName();
      lineStyleOptionss[i][1] = lineStyles[i].getName();
    }
    ComboFieldEditor lineStyleField = new ComboFieldEditor(LINE_STYLE, "Line Style", lineStyleOptionss,
        connectAttrGroup);
    addField(lineStyleField);

    IntegerFieldEditor lineWidthField = new IntegerFieldEditor(LINE_WIDTH, "Line Width", connectAttrGroup);
    addField(lineWidthField);

    Group colorMapAttrGroup = createGroup(parent, "Color Bar Attributes");

    ColorMapDescription[] colorMapDescs = ServiceProvider.getColorMapService().getAll();
    String[][] colorMapOptions = new String[colorMapDescs.length][2];
    for (int i = 0; i < colorMapDescs.length; i++) {
      colorMapOptions[i][0] = colorMapDescs[i].getName();
      colorMapOptions[i][1] = colorMapDescs[i].getName();
    }
    ComboFieldEditor colorMapField = new ComboFieldEditor(COLOR_MAP, COLOR_MAP, colorMapOptions, colorMapAttrGroup);
    addField(colorMapField);

    setDefaults();

    pointStyleField.load();
    pointSizeField.load();
    lineStyleField.load();
    lineWidthField.load();
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

    String lineStyle = localStore.getString(LINE_STYLE);
    prefState.put(LINE_STYLE, lineStyle);

    int lineWidth = localStore.getInt(LINE_WIDTH);
    prefState.put(LINE_WIDTH, Integer.toString(lineWidth));

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
      String pointStyle = prefs.get(POINT_STYLE);
      localStore.setValue(POINT_STYLE, pointStyle);
    } catch (Exception nfe) {
      // Leave as default.
    }

    try {
      int pointSize = Integer.parseInt(prefs.get(POINT_SIZE));
      localStore.setValue(POINT_SIZE, pointSize);
    } catch (Exception nfe) {
      // Leave as default.
    }

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
