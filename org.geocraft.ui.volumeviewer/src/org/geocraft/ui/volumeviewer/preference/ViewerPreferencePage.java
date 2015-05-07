package org.geocraft.ui.volumeviewer.preference;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.internal.ui.volumeviewer.widget.FocusRods.ShowMode;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;

import com.ardor3d.math.ColorRGBA;


/**
 * Preference page for the 3d viewer.
 */
public class ViewerPreferencePage extends AbstractViewerPreferencePage {

  public static PropertyStore VIEWER_PREFERENCE_STORE = PropertyStoreFactory.getStore(AbstractViewerPreferencePage.ID);

  private static final RGB DEFAULT_SELECTION_COLOR = new RGB(0, 255, 255);

  /** Current center values. */
  private static final String[][] CURRENT_CENTER = new String[][] {
      { ShowMode.ALWAYS.getName(), ShowMode.ALWAYS.getId() }, { ShowMode.NEVER.getName(), ShowMode.NEVER.getId() },
      { ShowMode.ON_INTERACT.getName(), ShowMode.ON_INTERACT.getId() } };

  /** Projection mode values. */
  private static final String[][] PROJECTION_MODE = new String[][] { { "Orthographic", "parallel" },
      { "Perspective", "perspective" } };

  /** Depth bits values. */
  private static final String[][] DEPTH_BITS = new String[][] { { "8", "8" }, { "16", "16" }, { "24", "24" },
      { "32", "32" } };

  /** The preferences store. */
  private final PropertyStore _store = (PropertyStore) getStore();

  /** Current center radio group. */
  private RadioGroupFieldEditor _currentCenter;

  /** Projection mode radio group. */
  private RadioGroupFieldEditor _projectionMode;

  /** Depth bits radio group. */
  private RadioGroupFieldEditor _depthBits;

  /** Selection color editor. */
  private ColorFieldEditor _selectionColor;

  /** Show labels editor. */
  private BooleanFieldEditor _showLabels;

  /** Labels text size editor. */
  private IntegerFieldEditor _labelsTextSize;

  /** Current center preference key. */
  public static final String CURRENT_CENTER_KEY = "currentCenter";

  /** Projection mode preference key. */
  public static final String PROJECTION_MODE_KEY = "projectionMode";

  /** Depth bits preference key. */
  public static final String DEPTH_BITS_KEY = "depthBits";

  /** Selection color preference key. */
  public static final String SELECTION_COLOR_KEY = "selectionColor";

  /** Show labels preference key. */
  public static final String SHOW_LABELS_KEY = "showLabels";

  /** Labels text size preference key. */
  public static final String LABELS_TEXT_BASE_SIZE = "labelsSize";

  static {
    setDefaults();
  }

  public ViewerPreferencePage() {
    // parameterless constructor needed because it is called by the preferences APIs
    setDefaults();
  }

  public ViewerPreferencePage(final String title) {
    super(title);
    setDefaults();
  }

  public ViewerPreferencePage(final String title, final ImageDescriptor image) {
    super(title, image);
    setDefaults();
  }

  public static void setDefaults() {
    PreferencesUtil.getService(ID).setDefault(CURRENT_CENTER_KEY, ShowMode.ON_INTERACT.getId());
    PreferencesUtil.getService(ID).setDefault(PROJECTION_MODE_KEY, "perspective");
    PreferencesUtil.getService(ID).setDefault(DEPTH_BITS_KEY, 24);
    PreferencesUtil.getService(ID).setDefault(SELECTION_COLOR_KEY, DEFAULT_SELECTION_COLOR);
    PreferencesUtil.getService(ID).setDefault(SHOW_LABELS_KEY, true);
    PreferencesUtil.getService(ID).setDefault(LABELS_TEXT_BASE_SIZE, Math.round(SceneText.getBaseFontScale() * 100));
  }

  @Override
  protected Control createContents(final Composite parent) {
    final Composite subParent = (Composite) super.createContents(parent);
    final Composite mainPanel = super.createMainPanel(subParent);

    _currentCenter = new RadioGroupFieldEditor(CURRENT_CENTER_KEY, "Current center display", 3, CURRENT_CENTER,
        mainPanel, true);
    _currentCenter.setPage(this);
    _currentCenter.setPreferenceStore(_store);
    _currentCenter.load();
    addField(_currentCenter);

    _projectionMode = new RadioGroupFieldEditor(PROJECTION_MODE_KEY, "Projection mode", 2, PROJECTION_MODE, mainPanel,
        true);
    _projectionMode.setPage(this);
    _projectionMode.setPreferenceStore(_store);
    _projectionMode.load();
    addField(_projectionMode);

    _depthBits = new RadioGroupFieldEditor(DEPTH_BITS_KEY, "Color accuracy (depth bits)", 4, DEPTH_BITS, mainPanel,
        true);
    _depthBits.setPage(this);
    _depthBits.setPreferenceStore(_store);
    _depthBits.load();
    addField(_depthBits);

    final Group selGroup = createGroup(mainPanel, "Selection outline");
    _selectionColor = new ColorFieldEditor(SELECTION_COLOR_KEY, "Color", selGroup);
    _selectionColor.setPage(this);
    _selectionColor.setPreferenceStore(_store);
    _selectionColor.load();
    setFieldEditorLayout(_selectionColor.getLabelControl(selGroup), _selectionColor.getColorSelector().getButton(), 60);
    addField(_selectionColor);

    final Group labelsGroup = createGroup(mainPanel, "Labels");
    _showLabels = new BooleanFieldEditor(SHOW_LABELS_KEY, "Display labels", labelsGroup);
    _showLabels.setPage(this);
    _showLabels.setPreferenceStore(_store);
    _showLabels.load();
    addField(_showLabels);
    new Label(labelsGroup, SWT.NONE);

    _labelsTextSize = new IntegerFieldEditor(LABELS_TEXT_BASE_SIZE, "Labels text base size", labelsGroup);
    _labelsTextSize.setValidRange(10, 100);
    _labelsTextSize.setPage(this);
    _labelsTextSize.setPreferenceStore(_store);
    _labelsTextSize.load();
    addField(_labelsTextSize);
    setFieldEditorLayout(_labelsTextSize.getLabelControl(labelsGroup), _labelsTextSize.getTextControl(labelsGroup), 60);

    return mainPanel;
  }

  @Override
  public void init(@SuppressWarnings("unused") final IWorkbench workbench) {
    _store.setDefault(CURRENT_CENTER_KEY, ShowMode.ON_INTERACT.getId());
    _store.setDefault(PROJECTION_MODE_KEY, "perspective");
    _store.setDefault(DEPTH_BITS_KEY, 24);
    _store.setDefault(SHOW_LABELS_KEY, true);
    _store.setDefault(LABELS_TEXT_BASE_SIZE, Math.round(SceneText.getBaseFontScale() * 100));

    PreferenceConverter.setDefault(_store, SELECTION_COLOR_KEY, VolumeViewerHelper.colorRGBAToRGB(ColorRGBA.CYAN
        .asMutable()));
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#getPreferenceState()
   */
  @Override
  public Map<String, String> getPreferenceState() {
    final HashMap<String, String> prefState = new HashMap<String, String>();
    final IPreferenceStore localStore = VIEWER_PREFERENCE_STORE.getLocalStore();

    final String centerDisplayMode = localStore.getString(CURRENT_CENTER_KEY);
    prefState.put(CURRENT_CENTER_KEY, centerDisplayMode);

    final String projectionMode = localStore.getString(PROJECTION_MODE_KEY);
    prefState.put(PROJECTION_MODE_KEY, projectionMode);

    final String colorAccuracy = localStore.getString(DEPTH_BITS_KEY);
    prefState.put(DEPTH_BITS_KEY, colorAccuracy);

    final RGB selectionColor = PreferenceConverter.getColor(localStore, SELECTION_COLOR_KEY);
    prefState.put(SELECTION_COLOR_KEY, selectionColor.toString());

    final boolean showLabels = localStore.getBoolean(SHOW_LABELS_KEY);
    prefState.put(SHOW_LABELS_KEY, Boolean.toString(showLabels));

    final int textBaseSize = localStore.getInt(LABELS_TEXT_BASE_SIZE);
    prefState.put(LABELS_TEXT_BASE_SIZE, Integer.toString(textBaseSize));

    return prefState;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#setPreferenceState(java.util.Map)
   */
  @Override
  public void setPreferenceState(final Map<String, String> prefs) {
    final IPreferenceStore localStore = VIEWER_PREFERENCE_STORE.getLocalStore();

    localStore.setValue(CURRENT_CENTER_KEY, prefs.get(CURRENT_CENTER_KEY));

    localStore.setValue(PROJECTION_MODE_KEY, prefs.get(PROJECTION_MODE_KEY));

    localStore.setValue(DEPTH_BITS_KEY, prefs.get(DEPTH_BITS_KEY));

    final RGB selectionColor = PropertyStore.rgbValue(prefs.get(SELECTION_COLOR_KEY));
    PreferenceConverter.setValue(localStore, SELECTION_COLOR_KEY, selectionColor);

    try {
      final boolean showLabels = Boolean.parseBoolean(prefs.get(SHOW_LABELS_KEY));
      localStore.setValue(SHOW_LABELS_KEY, showLabels);
    } catch (final NumberFormatException nfe) {
      //leave value as default
    }

    try {
      final int textBaseSize = Integer.parseInt(prefs.get(LABELS_TEXT_BASE_SIZE));
      localStore.setValue(LABELS_TEXT_BASE_SIZE, textBaseSize);
    } catch (final NumberFormatException nfe) {
      //leave value as default
    }
  }
}
