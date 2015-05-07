package org.geocraft.ui.viewer.preferences;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.core.common.preferences.OverlayPage;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.ui.internal.viewer.ServiceComponent;


/**
 * Preferences page for the plot layout settings.
 */
public class PlotPreferencePage extends OverlayPage implements IWorkbenchPreferencePage, IGeocraftPreferencePage {

  static {
    setDefaults();
  }

  public static final String ID = ServiceComponent.PLUGIN_ID;

  public static PropertyStore PLOT_PREFERENCE_STORE = PropertyStoreFactory.getStore(ID);

  public static final String CREATE_PLOTS_IN = "createPlotsIn";

  public static final String ACTIVE_WINDOW = "activeWindow";

  public static final String SEPARATE_WINDOW = "separateWindow";

  /** The plot create choices. */
  private static final String[][] PLOT_CREATE_CHOICES = new String[][] { { "Active window", ACTIVE_WINDOW },
      { "A separate window", SEPARATE_WINDOW } };

  /** The preferences store. */
  private final PropertyStore _store = PropertyStoreFactory.getStore(ID);

  /** The plot create field editor. */
  private RadioGroupFieldEditor _plotCreate;

  /** The plot layout field editor. */
  private RadioGroupFieldEditor _plotLayout;

  public PlotPreferencePage() {
    // empty constructor needed because it is called by the preferences APIs
    setDefaults();
  }

  public PlotPreferencePage(final String title) {
    super(title);
    setDefaults();
  }

  public PlotPreferencePage(final String title, final ImageDescriptor image) {
    super(title, image);
    setDefaults();
  }

  public static void setDefaults() {
    PreferencesUtil.getService(ID).setDefault(CREATE_PLOTS_IN, ACTIVE_WINDOW);
  }

  @Override
  protected Control createContents(final Composite parent) {
    Composite subParent = (Composite) super.createContents(parent);
    Composite mainPanel = new Composite(subParent, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    mainPanel.setLayout(layout);
    GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
    data.horizontalAlignment = SWT.FILL;
    mainPanel.setLayoutData(data);

    _plotCreate = new RadioGroupFieldEditor("plotCreate", "Create plots in", 1, PLOT_CREATE_CHOICES, mainPanel);
    _plotCreate.setPage(this);
    _plotCreate.setPreferenceStore(_store);
    _plotCreate.load();
    addField(_plotCreate);

    return mainPanel;
  }

  @Override
  @SuppressWarnings("unused")
  public void init(final IWorkbench workbench) {
    // does nothing for now
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.common.preferences.OverlayPage#getPageId()
   */
  @Override
  protected String getPageId() {
    return ID;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#getPreferenceState()
   */
  @Override
  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = PLOT_PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    String createPlotsIn = localStore.getString(CREATE_PLOTS_IN);
    prefState.put(CREATE_PLOTS_IN, createPlotsIn);

    return prefState;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#setPreferenceState(java.util.Map)
   */
  @Override
  public void setPreferenceState(Map<String, String> prefs) {
    IPreferenceStore localStore = PLOT_PREFERENCE_STORE.getLocalStore();

    PLOT_PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs.get(PropertyStore.USE_PROJECT_SETTINGS));

    localStore.setValue(CREATE_PLOTS_IN, prefs.get(CREATE_PLOTS_IN));
  }
}
