package org.geocraft.ui.volumeviewer;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
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
import org.geocraft.internal.ui.volumeviewer.widget.FocusRods.ShowMode;
import org.geocraft.ui.volumeviewer.renderer.pointset.PointStyle;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;

import com.ardor3d.math.ColorRGBA;


public class VolumeViewerPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage {

  public static final String DEPTH_BITS = "Depth Bits";

  public static final String CURRENT_CENTER = "Current Center";

  public static final String PROJECTION_MODE = "Projection Mode";

  public static final String SELECTION_COLOR = "Selection Color";

  public static final String SHOW_LABELS = "Show Labels";

  public static final String TEXT_LABELS_BASE_SIZE = "Text Labels Base Size";

  public static final int DEFAULT_DEPTH_BITS = 24;

  public static final ShowMode DEFAULT_CURRENT_CENTER = ShowMode.ON_INTERACT;

  public static final ProjectionMode DEFAULT_PROJECTION_MODE = ProjectionMode.PERSPECTIVE;

  public static final RGB DEFAULT_SELECTION_COLOR = VolumeViewerHelper.colorRGBAToRGB(ColorRGBA.CYAN);

  public static final boolean DEFAULT_SHOW_LABELS = true;

  public static final int DEFAULT_TEXT_LABELS_BASE_SIZE = Math.round(SceneText.getBaseFontScale() * 100);

  public static final String ID = "org.geocraft.ui.volumeviewer";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(VolumeViewerPreferencePage.ID);

  static {
    setDefaults();
  }

  public VolumeViewerPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for the 3D viewer.");
  }

  public static void setDefaults() {
    final GeocraftPreferenceService service = PreferencesUtil.getService(ID);

    // Initialize the point preferences.
    service.setDefault(CURRENT_CENTER, DEFAULT_CURRENT_CENTER.getId());
    service.setDefault(PROJECTION_MODE, DEFAULT_PROJECTION_MODE.getName());
    service.setDefault(DEPTH_BITS, DEFAULT_DEPTH_BITS);
    service.setDefault(SELECTION_COLOR, DEFAULT_SELECTION_COLOR);
    service.setDefault(SHOW_LABELS, DEFAULT_SHOW_LABELS);
    service.setDefault(TEXT_LABELS_BASE_SIZE, DEFAULT_TEXT_LABELS_BASE_SIZE);

  }

  @Override
  protected void createFieldEditors() {
    final Composite parent = getFieldEditorParent();

    final PointStyle[] pointStyles = PointStyle.values();
    final String[][] pointStyleOptions = new String[pointStyles.length][2];
    for (int i = 0; i < pointStyles.length; i++) {
      pointStyleOptions[i][0] = pointStyles[i].getName();
      pointStyleOptions[i][1] = pointStyles[i].getName();
    }

    final String[][] showModeOptions = new String[][] { { ShowMode.ALWAYS.getName(), ShowMode.ALWAYS.getId() },
        { ShowMode.NEVER.getName(), ShowMode.NEVER.getId() },
        { ShowMode.ON_INTERACT.getName(), ShowMode.ON_INTERACT.getId() } };
    final RadioGroupFieldEditor currentCenterField = new RadioGroupFieldEditor(CURRENT_CENTER, CURRENT_CENTER, 3,
        showModeOptions, parent, true);
    addField(currentCenterField);

    final ProjectionMode[] projectionModes = ProjectionMode.values();
    final String[][] projectionModeOptions = new String[projectionModes.length][2];
    for (int i = 0; i < projectionModes.length; i++) {
      projectionModeOptions[i][0] = projectionModes[i].toString();
      projectionModeOptions[i][1] = projectionModes[i].toString();
    }
    final RadioGroupFieldEditor projectionModeField = new RadioGroupFieldEditor(PROJECTION_MODE, PROJECTION_MODE, 2,
        projectionModeOptions, parent, true);
    addField(projectionModeField);

    final String[][] depthBitOptions = new String[][] { { "8", "8" }, { "16", "16" }, { "24", "24" }, { "32", "32" } };
    final RadioGroupFieldEditor depthBitsField = new RadioGroupFieldEditor(DEPTH_BITS, "Color Accuracy (depth bits)",
        4, depthBitOptions, parent, true);
    addField(depthBitsField);

    final Group colorsGroup = createGroup(parent, "Colors");
    System.out.println("RGB: " + DEFAULT_SELECTION_COLOR);
    final ColorFieldEditor selectionColorField = new ColorFieldEditor(SELECTION_COLOR, SELECTION_COLOR, colorsGroup);
    selectionColorField.getColorSelector().setColorValue(DEFAULT_SELECTION_COLOR);
    addField(selectionColorField);

    final Group labelsGroup = createGroup(parent, "Labels");
    final BooleanFieldEditor showLabelsField = new BooleanFieldEditor(SHOW_LABELS, SHOW_LABELS, labelsGroup);
    addField(showLabelsField);

    final IntegerFieldEditor textLabelsSizeField = new IntegerFieldEditor(TEXT_LABELS_BASE_SIZE, TEXT_LABELS_BASE_SIZE,
        labelsGroup);
    addField(textLabelsSizeField);

    setDefaults();

    currentCenterField.load();
    projectionModeField.load();
    depthBitsField.load();
    selectionColorField.load();
    showLabelsField.load();
    textLabelsSizeField.load();
  }

  public Map<String, String> getPreferenceState() {
    final HashMap<String, String> prefState = new HashMap<String, String>();
    final IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    final String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    final String currentCenter = localStore.getString(CURRENT_CENTER);
    prefState.put(CURRENT_CENTER, currentCenter);

    final String projectionMode = localStore.getString(PROJECTION_MODE);
    prefState.put(PROJECTION_MODE, projectionMode);

    final int depthBits = localStore.getInt(DEPTH_BITS);
    prefState.put(DEPTH_BITS, Integer.toString(depthBits));

    final boolean showLabels = localStore.getBoolean(SHOW_LABELS);
    prefState.put(SHOW_LABELS, Boolean.toString(showLabels));

    final int textLabelsBaseSize = localStore.getInt(TEXT_LABELS_BASE_SIZE);
    prefState.put(TEXT_LABELS_BASE_SIZE, Integer.toString(textLabelsBaseSize));

    final RGB selectionColor = PreferenceConverter.getColor(localStore, SELECTION_COLOR);
    prefState.put(SELECTION_COLOR, selectionColor.toString());

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
      final String currentCenter = prefs.get(CURRENT_CENTER);
      localStore.setValue(CURRENT_CENTER, currentCenter);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final String projectionMode = prefs.get(PROJECTION_MODE);
      localStore.setValue(PROJECTION_MODE, projectionMode);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final int depthBits = Integer.parseInt(prefs.get(DEPTH_BITS));
      localStore.setValue(DEPTH_BITS, depthBits);
    } catch (final Exception ex) {
      // Leave value as default.
    }

    try {
      final boolean showLabels = Boolean.parseBoolean(prefs.get(SHOW_LABELS));
      localStore.setValue(SHOW_LABELS, showLabels);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final int textLabelsBaseSize = Integer.parseInt(prefs.get(TEXT_LABELS_BASE_SIZE));
      localStore.setValue(TEXT_LABELS_BASE_SIZE, textLabelsBaseSize);
    } catch (final Exception ex) {
      // Leave as default.
    }

    try {
      final RGB selectionColor = PropertyStore.rgbValue(prefs.get(SELECTION_COLOR));
      PreferenceConverter.setValue(localStore, SELECTION_COLOR, selectionColor);
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
