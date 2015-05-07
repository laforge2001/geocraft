package org.geocraft.product.workspace;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.util.GeoCraftVersion;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.ui.model.preferences.UnitPreferencesPage;


/**
 * A dialog that prompts for a directory to use as a workspace.
 */
public class ChooseWorkspaceDialog extends TitleAreaDialog {

  private static final String DIALOG_SETTINGS_SECTION = "ChooseWorkspaceDialogSettings"; //$NON-NLS-1$

  private final ChooseWorkspaceData _launchData;

  private Combo _text;

  //  private boolean suppressAskAgain = false;

  private boolean _centerOnMonitor = false;

  private Group _unitsPanel;

  private Label _timeUnitLabel;

  private Combo _timeUnitCombo;

  private Label _horizontalUnitLabel;

  private Combo _horizontalUnitCombo;

  private Label _verticalUnitLabel;

  private Combo _verticalUnitCombo;

  private Button _restoreCheckbox;

  private Group _sessionPanel;

  private boolean _isUnitsEnabled = true;

  static final String META_DATA = ".metadata";

  static final String PLUGINS = ".plugins";

  static final String CORE = "org.eclipse.core.runtime";

  static final String SETTINGS = ".settings";

  static final String UNIT_PREFS = "org.geocraft.core.model.prefs";

  static final String UNIT_PATH = META_DATA + File.separatorChar + PLUGINS + File.separatorChar + CORE
      + File.separatorChar + SETTINGS;

  //string representing path to Unit preferences in a workspace
  static final String PATH_TO_UNITS = UNIT_PATH + File.separatorChar + UNIT_PREFS;

  /** Property file containing user preferences, in particular, the path of the last active session. */
  static final String USER_PREFS = ".userPreferences.properties";

  static final String LAST_ACTIVE_SESSION_PROP = "lastActiveSession";

  static final String RESTORE_ON_LAUNCH_PROP = "restoreOnLaunch";

  /**
   * Create a modal dialog on the arugment shell, using and updating the
   * argument data object.
   * @param parentShell the parent shell for this dialog
   * @param launchData the launch data from past launches
   * 
   * @param suppressAskAgain
   *            true means the dialog will not have a "don't ask again" button
   * @param centerOnMonitor indicates whether the dialog should be centered on 
   * the monitor or according to it's parent if there is one
   */
  public ChooseWorkspaceDialog(final Shell parentShell, final ChooseWorkspaceData launchData,
      final boolean suppressAskAgain, final boolean centerOnMonitor) {
    super(parentShell);
    this._launchData = launchData;
    this._centerOnMonitor = centerOnMonitor;
  }

  /**
   * Show the dialog to the user (if needed). When this method finishes,
   * #getSelection will return the workspace that should be used (whether it
   * was just selected by the user or some previous default has been used.
   * The parameter can be used to override the users preference.  For example,
   * this is important in cases where the default selection is already in use
   * and the user is forced to choose a different one.
   * 
   * @param force
   *            true if the dialog should be opened regardless of the value of
   *            the show dialog checkbox
   */
  public void prompt(final boolean force) {
    if (force || _launchData.getShowDialog()) {
      open();

      // 70576: make sure dialog gets dismissed on ESC too
      if (getReturnCode() == CANCEL) {
        _launchData.workspaceSelected(null);
      }

      return;
    }

    String[] recent = _launchData.getRecentWorkspaces();

    // If the selection dialog was not used then the workspace to use is either the
    // most recent selection or the initialDefault (if there is no history).
    String workspace = null;
    if (recent != null && recent.length > 0) {
      workspace = recent[0];
    }
    if (workspace == null || workspace.length() == 0) {
      workspace = _launchData.getInitialDefault();
    }
    _launchData.workspaceSelected(TextProcessor.deprocess(workspace));
  }

  private String getTimeUnit() {
    if (_timeUnitCombo != null) {
      return _timeUnitCombo.getText();
    }
    return null;
  }

  private String getHorizUnit() {
    if (_horizontalUnitCombo != null) {
      return _horizontalUnitCombo.getText();
    }
    return null;
  }

  private String getVertUnit() {
    if (_verticalUnitCombo != null) {
      return _verticalUnitCombo.getText();
    }
    return null;
  }

  /**
   * Creates and returns the contents of the upper part of this dialog (above
   * the button bar).
   * <p>
   * The <code>Dialog</code> implementation of this framework method creates
   * and returns a new <code>Composite</code> with no margins and spacing.
   * </p>
   *
   * @param parent the parent composite to contain the dialog area
   * @return the dialog area control
   */
  @Override
  protected Control createDialogArea(final Composite parent) {
    String productName = getWindowTitle();

    Composite composite = (Composite) super.createDialogArea(parent);
    setTitle("Choose or Create Workspace");
    setMessage("A workspace is a location where all your project-specific settings are stored." + '\n'
        + "Select a previous workspace or create a new workspace by entering its path.");

    // bug 59934: load title image for sizing, but set it non-visible so the
    //            white background is displayed
    if (getTitleImageLabel() != null) {
      getTitleImageLabel().setVisible(false);
    }

    createWorkspaceBrowseRow(composite);
    createUnitsPanel(composite);
    addSpacer(composite);
    createSessionPanel(composite);

    // look for the eclipse.gcj property.  
    // If true, then we dont need any warning messages.
    // someone is asserting that we're okay on GCJ
    boolean gcj = Boolean.getBoolean("eclipse.gcj"); //$NON-NLS-1$
    String vmName = System.getProperty("java.vm.name");//$NON-NLS-1$
    if (!gcj && vmName != null && vmName.indexOf("libgcj") != -1) { //$NON-NLS-1$
      composite.getDisplay().asyncExec(new Runnable() {

        public void run() {
          // set this via an async - if we set it directly the dialog
          // will
          // be huge. See bug 223532
          setMessage("Running on an unsupported virtual machine", IMessageProvider.WARNING);
        }
      });
    }

    Dialog.applyDialogFont(composite);
    checkUnitPrefs(getWorkspaceLocation());
    return composite;
  }

  /**
   * @param parent
   */
  private void createUnitsPanel(Composite parent) {
    String[] timeUnits = UnitPreferencesPage.getUnitNames(0);
    String[] horizUnits = UnitPreferencesPage.getUnitNames(1);
    String[] vertUnits = UnitPreferencesPage.getUnitNames(2);
    _unitsPanel = createUnitsGroup(parent, "Units");

    List<String> options = new ArrayList<String>();

    _horizontalUnitLabel = new Label(_unitsPanel, SWT.NONE);
    _horizontalUnitLabel.setText("X,Y Unit");

    _horizontalUnitCombo = new Combo(_unitsPanel, SWT.READ_ONLY);
    options.clear();
    for (String horizUnit : horizUnits) {
      options.add(horizUnit);
    }
    _horizontalUnitCombo.setItems(options.toArray(new String[0]));
    _horizontalUnitCombo.select(0);

    _verticalUnitLabel = new Label(_unitsPanel, SWT.NONE);
    _verticalUnitLabel.setText("Depth Unit");

    _verticalUnitCombo = new Combo(_unitsPanel, SWT.READ_ONLY);
    options.clear();
    for (String vertUnit : vertUnits) {
      options.add(vertUnit);
    }
    _verticalUnitCombo.setItems(options.toArray(new String[0]));
    _verticalUnitCombo.select(0);

    _timeUnitLabel = new Label(_unitsPanel, SWT.NONE);
    _timeUnitLabel.setText("Time Units");

    _timeUnitCombo = new Combo(_unitsPanel, SWT.READ_ONLY);
    options.clear();
    for (String timeUnit : timeUnits) {
      options.add(timeUnit);
    }
    _timeUnitCombo.setItems(options.toArray(new String[0]));
    _timeUnitCombo.select(0);
  }

  /**
   * Get the last active session preference for the specified workspace
   * @return Path of last active session if one exists; otherwise, the empty string
   */
  private String getLastActiveSessionPref() {
    String workspaceDir = getWorkspaceLocation();
    Properties userPrefs = new Properties();
    String sessionPath = "";
    try {
      FileInputStream fis = new FileInputStream(workspaceDir + File.separatorChar + USER_PREFS);
      userPrefs.load(fis);
      String prop = userPrefs.getProperty(LAST_ACTIVE_SESSION_PROP);
      if (prop != null) {
        sessionPath = prop;
      }
    } catch (FileNotFoundException e) {
      //no .userPreferences.properties file
    } catch (IOException e) {
      //cannot read .userPreferences.properties file
    }

    return sessionPath;
  }

  /**
   * Get the last setting preference for the "restore on launch" check box for the specified workspace
   * @return Last setting if one exists; otherwise, false
   */
  private boolean getRestoreOnLaunchPref() {
    String workspaceDir = getWorkspaceLocation();
    Properties userPrefs = new Properties();
    boolean restoreOnLaunch = false;
    try {
      FileInputStream fis = new FileInputStream(workspaceDir + File.separatorChar + USER_PREFS);
      userPrefs.load(fis);
      String prop = userPrefs.getProperty(RESTORE_ON_LAUNCH_PROP);
      if (prop != null) {
        restoreOnLaunch = prop.equals("true") ? true : false;
      }
    } catch (FileNotFoundException e) {
      //no .userPreferences.properties file
    } catch (IOException e) {
      //cannot read .userPreferences.properties file
    }
    return restoreOnLaunch;
  }

  private void createSessionPanel(Composite parent) {
    _sessionPanel = createSessionGroup(parent, "Session");

    Label msg1 = new Label(_sessionPanel, SWT.NONE);
    msg1.setText("Upon launch, optionally restore the state of the workspace's\n"
        + "last active session, i.e., the last session saved or restored.");

    //Get the last active session, if any
    boolean isLastActiveSession = true;
    String sessionFile = "N/A";
    String filler = "                    ";
    String sessionPath = getLastActiveSessionPref();

    int idx = sessionPath.lastIndexOf(File.separatorChar);
    if (idx != -1) {
      sessionFile = sessionPath.substring(idx + 1);
    } else {
      isLastActiveSession = false;
    }

    //set "restore on launch" check box based on last setting
    boolean isRestoreOnLaunch = getRestoreOnLaunchPref();

    _restoreCheckbox = new Button(_sessionPanel, SWT.CHECK);
    _restoreCheckbox.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        saveRestoreOnLaunch();
      }

    });
    _restoreCheckbox.setText("Restore last active session: " + sessionFile + filler);
    _restoreCheckbox.setEnabled(isLastActiveSession);
    _restoreCheckbox.setSelection(isLastActiveSession && isRestoreOnLaunch);
  }

  private void addSpacer(Composite parent) {
    Label spacer = new Label(parent, SWT.NONE);
    spacer.setText("");
  }

  /**
   * Returns the title that the dialog (or splash) should have.
   * 
   * @return the window title
   * @since 3.4
   */
  public static String getWindowTitle() {
    String productName = null;
    IProduct product = Platform.getProduct();
    if (product != null) {
      productName = product.getName() + " " + GeoCraftVersion.getCurrentVersion();
    }
    if (productName == null) {
      productName = "Eclipse";
    }
    return productName;
  }

  /**
   * Configures the given shell in preparation for opening this window
   * in it.
   * <p>
   * The default implementation of this framework method
   * sets the shell's image and gives it a grid layout. 
   * Subclasses may extend or reimplement.
   * </p>
   * 
   * @param shell the shell
   */
  @Override
  protected void configureShell(final Shell shell) {
    super.configureShell(shell);
    shell.setText("Geocraft Workspace Chooser");
  }

  /**
   * Notifies that the ok button of this dialog has been pressed.
   * <p>
   * The <code>Dialog</code> implementation of this framework method sets
   * this dialog's return code to <code>Window.OK</code>
   * and closes the dialog. Subclasses may override.
   * </p>
   */
  @Override
  protected void okPressed() {
    String loc = TextProcessor.deprocess(getWorkspaceLocation());
    _launchData.workspaceSelected(loc);
    saveUnits(loc);
    super.okPressed();

  }

  /**
   * does file I/O to persist unit selections into the preferences location
   */
  private void saveUnits(String loc) {
    if (_isUnitsEnabled || !_isUnitsEnabled) {
      //      File unitPrefs = new File(loc.trim() + File.separatorChar + PATH_TO_UNITS);

      try {

        File unitPath = new File(loc.trim() + File.separatorChar + UNIT_PATH);
        if (!unitPath.exists()) {
          //use apache to make the directory structure
          FileUtils.forceMkdir(unitPath);
        }

        File unitPrefs = new File(loc.trim() + File.separatorChar + PATH_TO_UNITS);

        Properties prop = new Properties();

        //if this preferences file exists for some reason, then load properties data
        if (!unitPrefs.createNewFile()) {
          FileInputStream is = new FileInputStream(unitPrefs);
          prop.load(is);
          is.close();
        }

        //set the unit properties from the selections
        prop.setProperty(UnitPreferences.HORIZONTAL_UNIT, getHorizUnit());
        prop.setProperty(UnitPreferences.DEPTH_UNIT, getVertUnit());
        prop.setProperty(UnitPreferences.TIME_UNIT, getTimeUnit());

        //write the properties out to the preferences file
        FileOutputStream os = new FileOutputStream(unitPrefs);
        prop.store(os, null);
        os.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * Get the workspace location from the widget.
   * @return String
   */
  protected String getWorkspaceLocation() {
    return _text.getText();
  }

  /**
   * Notifies that the cancel button of this dialog has been pressed.
   * <p>
   * The <code>Dialog</code> implementation of this framework method sets
   * this dialog's return code to <code>Window.CANCEL</code>
   * and closes the dialog. Subclasses may override if desired.
   * </p>
   */
  @Override
  protected void cancelPressed() {
    _launchData.workspaceSelected(null);
    super.cancelPressed();
  }

  private Group createSessionGroup(final Composite parent, final String title) {
    Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setText(title);
    buttonGroup.setFont(new Font(null, "Tahoma", 10, SWT.BOLD));
    GridLayout layout = new GridLayout(1, true);
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
    return buttonGroup;
  }

  private Group createUnitsGroup(final Composite parent, final String title) {
    Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setFont(new Font(null, "Tahoma", 10, SWT.BOLD));
    buttonGroup.setText(title);
    GridLayout layout = new GridLayout(2, true);
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buttonGroup;
  }

  private void checkUnitPrefs(String baseLocation) {
    File unitPrefs = new File(baseLocation.trim() + File.separatorChar + PATH_TO_UNITS);
    if (unitPrefs.exists()) {
      Properties prop = new Properties();
      try {
        FileInputStream is = new FileInputStream(unitPrefs);
        prop.load(is);
        is.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      disableUnitFields(prop);
    } else {
      enableUnitFields();
    }
  }

  /**
   * Update the check box to restore the last active session for 
   * the selected workspace
   */
  private void updateRestoreOnLaunch() {
    //Get the last active session, if any
    boolean isLastActiveSession = true;
    String sessionFile = "N/A";
    String sessionPath = getLastActiveSessionPref();

    int idx = sessionPath.lastIndexOf(File.separatorChar);
    if (idx != -1) {
      sessionFile = sessionPath.substring(idx + 1);
    } else {
      isLastActiveSession = false;
    }

    _restoreCheckbox.setText("Restore last active session: " + sessionFile);
    _restoreCheckbox.setEnabled(isLastActiveSession);
    _restoreCheckbox.setSelection(getRestoreOnLaunchPref());
  }

  /**
   * Save the state of the check box to restore the last active session on launch
   */
  private void saveRestoreOnLaunch() {
    //if the check box is not enabled, then there is nothing to save
    if (!_restoreCheckbox.isEnabled()) {
      return;
    }

    String workspaceDir = getWorkspaceLocation();
    Properties userPrefs = new Properties();
    String prefsPath = workspaceDir + File.separatorChar + USER_PREFS;
    try {
      //Check if the workspace has a user preference property file.
      //If so, read it.
      File prefs = new File(prefsPath);
      if (prefs.exists() && prefs.isFile()) {
        FileInputStream fis = new FileInputStream(prefsPath);
        userPrefs.load(fis);
      }
      userPrefs.setProperty(RESTORE_ON_LAUNCH_PROP, _restoreCheckbox.getSelection() ? "true" : "false");
      FileOutputStream fos = new FileOutputStream(prefsPath);
      userPrefs.store(fos, null);
    } catch (FileNotFoundException e) {
      //no .userPreferences.properties file
    } catch (IOException e) {
      //cannot read/write .userPreferences.properties file
    }
  }

  /**
   * Enables unit selection fields if the workspace selected is new
   */
  private void enableUnitFields() {
    _horizontalUnitCombo.setEnabled(true);
    _horizontalUnitCombo.select(0);
    _horizontalUnitLabel.setEnabled(true);

    _verticalUnitCombo.setEnabled(true);
    _verticalUnitCombo.select(0);
    _verticalUnitLabel.setEnabled(true);

    _timeUnitCombo.setEnabled(true);
    _timeUnitCombo.select(0);
    _timeUnitLabel.setEnabled(true);

    _isUnitsEnabled = true;

  }

  /**
   * Disables unit selection fields if the selected workspace already exists
   */
  private void disableUnitFields(Properties prop) {
    _horizontalUnitCombo.setEnabled(false);
    _horizontalUnitCombo.select(findUnit(prop, _horizontalUnitCombo, UnitPreferences.HORIZONTAL_UNIT,
        UnitPreferences.PREFERRED_HORIZONTAL_DISTANCE_UNIT_DEPRECATED));
    _horizontalUnitLabel.setEnabled(false);

    _verticalUnitCombo.setEnabled(false);
    _verticalUnitCombo.select(findUnit(prop, _verticalUnitCombo, UnitPreferences.DEPTH_UNIT,
        UnitPreferences.PREFERRED_DEPTH_UNIT_DEPRECATED));
    _verticalUnitLabel.setEnabled(false);

    _timeUnitCombo.setEnabled(false);
    _timeUnitCombo.select(findUnit(prop, _timeUnitCombo, UnitPreferences.TIME_UNIT,
        UnitPreferences.PREFERRED_TIME_UNIT_DEPRECATED));
    _timeUnitLabel.setEnabled(false);

    _isUnitsEnabled = false;

  }

  private int findUnit(Properties prop, Combo combo, String unitKey, String unitKeyDeprecated) {
    String unitName = prop.getProperty(unitKey);
    if (unitName == null) {
      unitName = prop.getProperty(unitKeyDeprecated);
    }
    if (unitName != null) {
      int index = combo.indexOf(unitName);
      if (index != -1) {
        return index;
      }
    }
    return 0;
  }

  /**
   * The main area of the dialog is just a row with the current selection
   * information and a drop-down of the most recently used workspaces.
   */
  private void createWorkspaceBrowseRow(final Composite parent) {
    Composite panel = new Composite(parent, SWT.NONE);

    GridLayout layout = new GridLayout(3, false);
    layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    panel.setLayout(layout);
    panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    panel.setFont(parent.getFont());

    Label label = new Label(panel, SWT.NONE);
    label.setText("Workspace");

    _text = new Combo(panel, SWT.BORDER | SWT.LEAD | SWT.DROP_DOWN);
    _text.setFocus();
    _text.setLayoutData(new GridData(400, SWT.DEFAULT));
    _text.addModifyListener(new ModifyListener() {

      public void modifyText(final ModifyEvent e) {
        Button okButton = getButton(Window.OK);
        if (okButton != null && !okButton.isDisposed()) {
          boolean nonWhitespaceFound = false;
          String characters = getWorkspaceLocation();
          for (int i = 0; !nonWhitespaceFound && i < characters.length(); i++) {
            if (!Character.isWhitespace(characters.charAt(i))) {
              nonWhitespaceFound = true;
            }
          }
          okButton.setEnabled(nonWhitespaceFound);
          if (nonWhitespaceFound) {
            checkUnitPrefs(characters);
            updateRestoreOnLaunch();
          }
        }
      }
    });
    _text.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        String dirPath = getWorkspaceLocation();
        checkUnitPrefs(dirPath);
        updateRestoreOnLaunch();
      }

    });
    setInitialTextValues(_text);

    Button browseButton = new Button(panel, SWT.PUSH);
    browseButton.setText("Browse...");
    setButtonLayoutData(browseButton);
    GridData data = (GridData) browseButton.getLayoutData();
    data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
    browseButton.setLayoutData(data);
    browseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        DirectoryDialog dialog = new DirectoryDialog(getShell());
        dialog.setText("Browse");
        dialog.setMessage("Select directory");
        dialog.setFilterPath(getInitialBrowsePath());
        String dir = dialog.open();
        if (dir != null) {
          String dirPath = TextProcessor.process(dir);
          _text.setText(dirPath);
          checkUnitPrefs(dirPath);
          updateRestoreOnLaunch();
        }
      }
    });

  }

  /**
   * Return a string containing the path that is closest to the current
   * selection in the text widget. This starts with the current value and
   * works toward the root until there is a directory for which File.exists
   * returns true. Return the current working dir if the text box does not
   * contain a valid path.
   * 
   * @return closest parent that exists or an empty string
   */
  private String getInitialBrowsePath() {
    File dir = new File(getWorkspaceLocation());
    while (dir != null && !dir.exists()) {
      dir = dir.getParentFile();
    }

    return dir != null ? dir.getAbsolutePath() : System.getProperty("user.dir");
  }

  /*
   * see org.eclipse.jface.Window.getInitialLocation() 
   */
  @Override
  protected Point getInitialLocation(final Point initialSize) {
    Composite parent = getShell().getParent();

    if (!_centerOnMonitor || parent == null) {
      return super.getInitialLocation(initialSize);
    }

    Monitor monitor = parent.getMonitor();
    Rectangle monitorBounds = monitor.getClientArea();
    Point centerPoint = Geometry.centerPoint(monitorBounds);

    return new Point(centerPoint.x - initialSize.x / 2, Math.max(monitorBounds.y, Math.min(centerPoint.y
        - initialSize.y * 2 / 3, monitorBounds.y + monitorBounds.height - initialSize.y)));
  }

  private void setInitialTextValues(final Combo text) {
    String[] recentWorkspaces = _launchData.getRecentWorkspaces();
    for (int i = 0; i < recentWorkspaces.length; ++i) {
      if (recentWorkspaces[i] != null) {
        text.add(recentWorkspaces[i]);
      }
    }

    text.setText(TextProcessor.process((text.getItemCount() > 0 ? text.getItem(0) : _launchData.getInitialDefault())));
  }

  /* (non-Javadoc)
     * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
     * 
     * @since 3.2
     */
  @Override
  protected IDialogSettings getDialogBoundsSettings() {
    // If we were explicitly instructed to center on the monitor, then
    // do not provide any settings for retrieving a different location or, worse,
    // saving the centered location.
    if (_centerOnMonitor) {
      return null;
    }

    //    IDialogSettings settings = Activator.getDefault().getDialogSettings();
    //    IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
    //    if (section == null) {
    //      section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
    //    }
    return null;
  }

}
