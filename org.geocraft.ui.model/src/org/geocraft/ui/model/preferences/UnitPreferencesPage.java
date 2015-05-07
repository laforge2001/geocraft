package org.geocraft.ui.model.preferences;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.model.preferences.CoordinateSystemService;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.preferences.IGeocraftPreferencePage;


/**
 * The page for displaying/editing the current unit preferences.
 */
public class UnitPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, IGeocraftPreferencePage {

  public static final String KEY = "org.geocraft.core.model";

  private final IPreferenceStore _store = new ScopedPreferenceStore(new InstanceScope(), KEY);

  /** The seismic datum elevation field editor. */
  private IntegerFieldEditor _seismicElevationField;

  public static final String SEISMIC_DATUM_ELEVATION = "seismicDatumElevation";

  public UnitPreferencesPage() {
    super("Unit Preferences");
  }

  @Override
  public void init(IWorkbench workbench) {
    PreferencesUtil.getService(KEY).setDefault(UnitPreferences.HORIZONTAL_UNIT, Unit.METER.toString());
    PreferencesUtil.getService(KEY).setDefault(UnitPreferences.DEPTH_UNIT, Unit.METER.toString());
    PreferencesUtil.getService(KEY).setDefault(UnitPreferences.TIME_UNIT, Unit.MILLISECONDS.toString());
  }

  public static String[] getUnitNames(final int index) {
    Domain domain = ApplicationPreferences._unitDomains[index];
    List<String> units = Unit.getUnitNamesByDomain(domain, false);
    // This is a temporary fix to limit unit choices to most common.
    if (domain == Domain.TIME) {
      // For now, limit time to milliseconds.
      units.clear();
      units.add(Unit.MILLISECONDS.getName());
      // units.add(Unit.SECOND.getName());
    } else if (domain == Domain.DISTANCE) {
      units.clear();
      units.add(Unit.METER.getName());
      units.add(Unit.FOOT.getName());
      units.add(Unit.US_SURVEY_FOOT.getName());
    }
    String[] unitsArray = new String[units.size()];

    for (int i = 0; i < unitsArray.length; ++i) {
      unitsArray[i] = units.get(i);
    }

    return unitsArray;
  }

  private Group createGroup(final Composite parent, final String title) {
    Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setText(title);
    GridLayout layout = new GridLayout(2, true);
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buttonGroup;
  }

  private void addLabels(Composite parent, int numColumns, String title, String value) {
    Label control = new Label(parent, SWT.LEFT_TO_RIGHT);
    control.setText(title);
    GridData gd = new GridData(SWT.RIGHT, SWT.FILL, true, true);
    gd.horizontalSpan = 1;
    control.setLayoutData(gd);
    control = new Label(parent, SWT.LEFT_TO_RIGHT);
    control.setText(value);
    gd = new GridData();
    gd.horizontalSpan = 1;
    gd.horizontalAlignment = GridData.FILL;
    control.setLayoutData(gd);
    control.setFont(parent.getFont());
  }

  private String getPreferenceString(String key) {
    String value = _store.getString(key);
    if (value.isEmpty()) {
      return _store.getDefaultString(key);
    }
    return value;
  }

  private void setPreferenceString(String key, String value) {
    _store.setValue(key, value);
  }

  @Override
  protected Control createContents(final Composite parent) {
    Composite mainPanel = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.verticalSpacing = 10;
    mainPanel.setLayout(layout);
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    mainPanel.setLayoutData(data);

    Group unitsPanel = createGroup(mainPanel, "Units");

    String horizontalDistanceUnit = getPreferenceString(UnitPreferences.HORIZONTAL_UNIT);
    String verticalDistanceUnit = getPreferenceString(UnitPreferences.DEPTH_UNIT);
    String timeUnit = getPreferenceString(UnitPreferences.TIME_UNIT);
    addLabels(unitsPanel, 2, "X,Y Unit: ", horizontalDistanceUnit);
    addLabels(unitsPanel, 2, "Depth Unit: ", verticalDistanceUnit);
    addLabels(unitsPanel, 2, "Time Unit: ", timeUnit);

    Group valuesPanel = createGroup(mainPanel, "Other");
    _seismicElevationField = new IntegerFieldEditor(SEISMIC_DATUM_ELEVATION, "Seismic Datum Elevation", valuesPanel);
    _seismicElevationField.setEmptyStringAllowed(false);
    _seismicElevationField.setPage(this);
    _seismicElevationField.setStringValue((int) ApplicationPreferences.getInstance().getSeismicDatumElevation() + "");

    return mainPanel;
  }

  @Override
  public boolean performOk() {
    _seismicElevationField.store();
    try {
      ((ScopedPreferenceStore) _store).save();
    } catch (IOException ex) {
      MessageDialog.openError(getShell(), "Unit Preferences Error", ex.getMessage());
    }
    UnitPreferences.getInstance().updateUnitPreferences();
    ApplicationPreferences.getInstance().setSeismicDatumElevation(_seismicElevationField.getIntValue());
    ApplicationPreferences.getInstance().updateAppPreferences();
    return super.performOk();
  }

  /**
   * The composite for displaying/editing the coordinate system.
   */
  class CoordinateSystemPanel extends Composite {

    /** The values combo box. */
    private final ComboViewer _coordinateSysComboViewer;

    /** The domain. */
    private final Domain _domain;

    /**
     * The constructor.
     * @param domain the domain of coordinate systems
     */
    public CoordinateSystemPanel(final Composite parent, final Domain domain) {
      super(parent, SWT.LEFT);
      _domain = domain;
      Label coordinateLabel = new Label(this, SWT.NONE);
      coordinateLabel.setText(domain.getTitle() + ":");
      setLayout(new RowLayout(SWT.HORIZONTAL));
      RowData data = new RowData();
      data.width = 120;
      coordinateLabel.setLayoutData(data);

      _coordinateSysComboViewer = new ComboViewer(this, SWT.READ_ONLY);
      updateCoordinateSystemList();
      _coordinateSysComboViewer.getCombo().select(0);

      //Coordinate System not implemented currently so disabling list
      _coordinateSysComboViewer.getCombo().setEnabled(false);
    }

    /**
     *  This must be re-populated whenever the preferences are shown since plugins maybe have added
     *  new coordinate systems to the list.
     */
    public void updateCoordinateSystemList() {
      _coordinateSysComboViewer.getCombo().removeAll();
      for (CoordinateSystem coordinate : CoordinateSystemService.getInstance().getCoordinateSystems(_domain)) {
        _coordinateSysComboViewer.add(coordinate);
      }
    }

    /**
     * Return the domain.
     * @return the domain
     */
    public Domain getDomain() {
      return _domain;
    }

    /**
     * Set the selected coordinate system.
     * @param name the coordinate system
     */
    public void setCoordinateSystem(final CoordinateSystem system) {
      if (system != null) {
        _coordinateSysComboViewer.setSelection(new StructuredSelection(system));
      }
    }

    /**
     * Return the selected coordinate system.
     * @return the selected coordinate system
     */
    public CoordinateSystem getSelectedSystem() {
      return (CoordinateSystem) ((StructuredSelection) _coordinateSysComboViewer.getSelection()).getFirstElement();
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#getPreferenceState()
   */
  @Override
  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();

    String horizontalDistanceUnit = getPreferenceString(UnitPreferences.HORIZONTAL_UNIT);
    prefState.put(UnitPreferences.HORIZONTAL_UNIT, horizontalDistanceUnit);

    String verticalDistanceUnit = getPreferenceString(UnitPreferences.DEPTH_UNIT);
    prefState.put(UnitPreferences.DEPTH_UNIT, verticalDistanceUnit);

    String timeUnit = getPreferenceString(UnitPreferences.TIME_UNIT);
    prefState.put(UnitPreferences.TIME_UNIT, timeUnit);

    int sde = (int) ApplicationPreferences.getInstance().getSeismicDatumElevation();
    prefState.put(SEISMIC_DATUM_ELEVATION, Integer.toString(sde));

    return prefState;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#setPreferenceState(java.util.Map)
   */
  @Override
  public void setPreferenceState(Map<String, String> prefs) {
    String horizontalDistanceUnit = prefs.get(UnitPreferences.HORIZONTAL_UNIT);
    setPreferenceString(UnitPreferences.HORIZONTAL_UNIT, horizontalDistanceUnit);

    String verticalDistanceUnit = prefs.get(UnitPreferences.DEPTH_UNIT);
    setPreferenceString(UnitPreferences.DEPTH_UNIT, verticalDistanceUnit);

    String timeUnit = prefs.get(UnitPreferences.TIME_UNIT);
    setPreferenceString(UnitPreferences.TIME_UNIT, timeUnit);

    try {
      int sde = Integer.parseInt(prefs.get(SEISMIC_DATUM_ELEVATION));
      ApplicationPreferences.getInstance().setSeismicDatumElevation(sde);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }
  }
}
