/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.utilities.loadequations;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.FileProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.shell.ICommandExecutor;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ButtonField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.SpinnerField;
import org.geocraft.ui.form2.field.TextField;


public class LoadEquations extends StandaloneAlgorithm {

  // UI TYPES
  // Input section

  /* Input file */
  private FileProperty _inputFile;

  /* Action to run equations that have been loaded */
  protected final BooleanProperty _runEquations;

  /* Action to rename variables */
  protected final BooleanProperty _renameVariables;

  /** Number of variables to rename */
  private IntegerProperty _numOfVarsToRename;

  /** Old Names */
  private StringProperty _oldName1;

  private StringProperty _oldName2;

  private StringProperty _oldName3;

  private StringProperty _oldName4;

  private StringProperty _oldName5;

  /** New input entities */
  protected final EntityProperty<Entity> _entity1;

  protected final EntityProperty<Entity> _entity2;

  protected final EntityProperty<Entity> _entity3;

  protected final EntityProperty<Entity> _entity4;

  protected final EntityProperty<Entity> _entity5;

  /** The output comments property. */
  public StringProperty _equations;

  public LoadEquations() {
    super();
    _inputFile = addFileProperty("Input File Name");
    _runEquations = addBooleanProperty("Run Equations", false);
    _renameVariables = addBooleanProperty("Rename Variables", false);
    _numOfVarsToRename = addIntegerProperty("Number of Variables to rename", 0);
    _oldName1 = addStringProperty("Old Name #1", "var1");
    _oldName2 = addStringProperty("Old Name #2", "var2");
    _oldName3 = addStringProperty("Old Name #3", "var3");
    _oldName4 = addStringProperty("Old Name #4", "var4");
    _oldName5 = addStringProperty("Old Name #5", "var5");
    _entity1 = addEntityProperty("New Variable #1", Entity.class);
    _entity2 = addEntityProperty("New Variable #2", Entity.class);
    _entity3 = addEntityProperty("New Variable #3", Entity.class);
    _entity4 = addEntityProperty("New Variable #4", Entity.class);
    _entity5 = addEntityProperty("New Variable #5", Entity.class);
    _equations = addStringProperty("Equations", "");
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_numOfVarsToRename.getKey())) {
      setFieldVisible(_oldName1, _numOfVarsToRename.get() > 0);
      setFieldVisible(_oldName2, _numOfVarsToRename.get() > 1);
      setFieldVisible(_oldName3, _numOfVarsToRename.get() > 2);
      setFieldVisible(_oldName4, _numOfVarsToRename.get() > 3);
      setFieldVisible(_oldName5, _numOfVarsToRename.get() > 4);
      setFieldVisible(_entity1, _numOfVarsToRename.get() > 0);
      setFieldVisible(_entity2, _numOfVarsToRename.get() > 1);
      setFieldVisible(_entity3, _numOfVarsToRename.get() > 2);
      setFieldVisible(_entity4, _numOfVarsToRename.get() > 3);
      setFieldVisible(_entity5, _numOfVarsToRename.get() > 4);
    } else if (key.equals(_inputFile.getKey())) {

      // Set the progress monitor to null in order to create a new progress monitor
      IProgressMonitor monitor = null;

      loadEquations(monitor);

    } else if (key.equals(_runEquations.getKey())) {

      runEquations();
    } else if (key.equals(_renameVariables.getKey())) {

      renameVariables();
    }
  }

  @Override
  public void validate(IValidation results) {

    // Make sure the volume to match is non-null
    if (_inputFile.isNull()) {
      results.error(_inputFile, "The Input file has not been specified");
    }

    // Make sure a variable names have been specified
    if (_numOfVarsToRename.get() > 0 && _oldName1.isEmpty()) {
      results.error(_oldName1, "A variable name has not been specified");
    }
    if (_numOfVarsToRename.get() > 1 && _oldName2.isEmpty()) {
      results.error(_oldName2, "A variable name has not been specified");
    }
    if (_numOfVarsToRename.get() > 2 && _oldName3.isEmpty()) {
      results.error(_oldName3, "A variable name has not been specified");
    }
    if (_numOfVarsToRename.get() > 3 && _oldName4.isEmpty()) {
      results.error(_oldName4, "A variable name has not been specified");
    }
    if (_numOfVarsToRename.get() > 4 && _oldName5.isEmpty()) {
      results.error(_oldName5, "A variable name has not been specified");
    }

    // Make sure a variable names have been specified
    if (_numOfVarsToRename.get() > 0 && _entity1.isNull()) {
      results.error(_entity1, "A new variable has not been selected");
    }
    if (_numOfVarsToRename.get() > 1 && _entity2.isNull()) {
      results.error(_entity2, "A new variable has not been selected");
    }
    if (_numOfVarsToRename.get() > 2 && _entity3.isNull()) {
      results.error(_entity3, "A new variable has not been selected");
    }
    if (_numOfVarsToRename.get() > 3 && _entity4.isNull()) {
      results.error(_entity4, "A new variable has not been selected");
    }
    if (_numOfVarsToRename.get() > 4 && _entity5.isNull()) {
      results.error(_entity5, "A new variable has not been selected");
    }
  }

  /* (non-Javadoc)
   * Construct the algorithm's UI consisting of form fields partitioned into sections: Input,
   * Output, and algorithm Parameters.
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.algorithm.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {

    // Set filter to type of file
    String[][] filters = new String[1][2];
    filters[0][0] = "Ascii Files (*.py)";
    filters[0][1] = "*.py";

    // Default the directory to the Preference key otherwise use the working directory
    String directoryName = PreferencesUtil.getPreferencesStore("org.geocraft.ui.io").get("Workspace_DIR",
        Utilities.getWorkingDirectory());

    // Build the input parameters section.
    FormSection inputSection = modelForm.addSection("Input", false);
    inputSection.addFileField(_inputFile, directoryName, filters);

    // Build the actions section.
    FormSection actionsSection = modelForm.addSection("Actions", false);

    BooleanProperty[] properties = { _runEquations, _renameVariables };
    String[] buttonNames = { "Run Equations", "Rename Variables" };
    ButtonField actionFields = actionsSection.addButtonsField(properties, "Actions", buttonNames, 0);
    actionFields.setTooltip("Run a set of equations", 0);
    actionFields.setTooltip("Set variable names to variables currently in the repository", 1);

    // Build the input parameters section.
    FormSection renameSection = modelForm.addSection("Rename variables", false);
    SpinnerField numVarsToRenameField = renameSection.addSpinnerField(_numOfVarsToRename, 0, 5, 0, 1);
    numVarsToRenameField.setTooltip("Number of variables to rename");

    TextField oldName1Field = renameSection.addTextField(_oldName1);
    oldName1Field.setTooltip("Old Variable Name #1");
    oldName1Field.setVisible(false);

    ComboField newVar1Field = renameSection.addEntityComboField(_entity1, Entity.class);
    newVar1Field.setTooltip("New variable #1 in the repository");
    newVar1Field.setVisible(false);

    TextField oldName2Field = renameSection.addTextField(_oldName2);
    oldName2Field.setTooltip("Old Variable Name #2");
    oldName2Field.setVisible(false);

    ComboField newVar2Field = renameSection.addEntityComboField(_entity2, Entity.class);
    newVar2Field.setTooltip("New variable #2 in the repository");
    newVar2Field.setVisible(false);

    TextField oldName3Field = renameSection.addTextField(_oldName3);
    oldName3Field.setTooltip("Old Variable Name #3");
    oldName3Field.setVisible(false);

    ComboField newVar3Field = renameSection.addEntityComboField(_entity3, Entity.class);
    newVar3Field.setTooltip("New variable #3 in the repository");
    newVar3Field.setVisible(false);

    TextField oldName4Field = renameSection.addTextField(_oldName4);
    oldName4Field.setTooltip("Old Variable Name #4");
    oldName4Field.setVisible(false);

    ComboField newVar4Field = renameSection.addEntityComboField(_entity4, Entity.class);
    newVar4Field.setTooltip("New variable #4 in the repository");
    newVar4Field.setVisible(false);

    TextField oldName5Field = renameSection.addTextField(_oldName5);
    oldName5Field.setTooltip("Old Variable Name #5");
    oldName5Field.setVisible(false);

    ComboField newVar5Field = renameSection.addEntityComboField(_entity5, Entity.class);
    newVar5Field.setTooltip("New variable #5 in the repository");
    newVar5Field.setVisible(false);

    FormSection equationsSection = modelForm.addSection("Equations", false);
    equationsSection.addTextBox(_equations, 250);

  }

  /**
   * Runs the domain logic of the algorithm.
   * @param monitor the progress monitor.
   * @param logger the logger to log messages.
   * @param repository the repository in which to add output entities.
   */

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // run the equations
    runEquations();
  }

  /**
   * Creates a buffered reader for the Ascii file by the file path.
   * 
   * @param filePath
   *          the file path of the Ascii file.
   * @return the buffered reader.
   * @throws FileNotFoundException
   */
  private BufferedReader createBufferedReader(final String filePath) throws FileNotFoundException {
    return new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
  }

  /**
   * @set the loading of equations
   */
  public void loadEquations(final IProgressMonitor progressMonitor) {
    IProgressMonitor monitor;
    ProgressMonitorDialog progressDialog;
    if (progressMonitor == null) {
      progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
      progressDialog.open();
      monitor = progressDialog.getProgressMonitor();
    } else {
      progressDialog = null;
      monitor = progressMonitor;
    }
    monitor.beginTask("Load Equations", 100);

    // Generate the input file name
    String inputFileName = _inputFile.get().getAbsolutePath();
    if (!inputFileName.contains(".")) {
      inputFileName = inputFileName + ".py";
    }

    // Create a buffered reader.
    BufferedReader reader = null;
    try {
      reader = createBufferedReader(inputFileName);
    } catch (IOException ex) {
      String text = "Exception:\ncreateBufferedReader(" + inputFileName + ")\n" + ex.getMessage();
      new Status(IStatus.ERROR, getClass().getName(), text, null);
      ServiceProvider.getLoggingService().getLogger(getClass()).error("Error while loading text file", ex);
    }

    // Initialize a new list of equations to be read from the file
    String equations = "";

    int currentLineNum = 1;
    boolean endFlag = false;
    String readLine = "";
    while (!endFlag) {
      // Read the next line
      try {
        readLine = reader.readLine();
      } catch (IOException ex) {
        String text = "Exception:\nreader.readLine()\n" + ex.getMessage();
        IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
        ErrorDialog.openError(null, "Error while loading text file", "Unable to load the input text file", status);
      }

      if (readLine != null) {
        equations = equations + readLine + "\n";
        currentLineNum++;
      } else {
        endFlag = true;
      }
    }
    monitor.worked(50);

    // Close the input file.
    try {
      reader.close();
    } catch (IOException ex) {
      String text = "Exception:\nreader.close()\n" + ex.getMessage();
      IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
      ErrorDialog.openError(null, "Unable to close the text file", "Unable to close the input text file", status);
    }

    // Set the equations
    _equations.set(equations);

    monitor.worked(50);
    monitor.done();
    if (progressDialog != null) {
      progressDialog.close();
    }
  }

  /**
   * @set the renaming of variables
   */
  public void renameVariables() {

    // Determine the repository
    IRepository repository = ServiceProvider.getRepository();

    String equations = _equations.get();
    String newEquations = "";
    boolean equationsChanged = false;

    // Create an array of old variable names and new variables
    int nVars = _numOfVarsToRename.get();
    String[] oldNames = new String[nVars];
    Entity[] newVars = new Entity[nVars];

    for (int i1 = 0; i1 < nVars; i1++) {
      int varNo = i1 + 1;
      if (varNo == 1) {
        oldNames[i1] = _oldName1.get();
        newVars[i1] = _entity1.get();
      } else if (varNo == 2) {
        oldNames[i1] = _oldName2.get();
        newVars[i1] = _entity2.get();
      } else if (varNo == 3) {
        oldNames[i1] = _oldName3.get();
        newVars[i1] = _entity3.get();
      } else if (varNo == 4) {
        oldNames[i1] = _oldName4.get();
        newVars[i1] = _entity4.get();
      } else if (varNo == 5) {
        oldNames[i1] = _oldName5.get();
        newVars[i1] = _entity5.get();
      }
    }

    // Create a new list of equations with the new variable names
    int lineNum = 1;
    String chList = " \",().";

    int ic1 = 0;
    while (ic1 < equations.length()) {
      int ie1 = equations.indexOf("\n", ic1);
      // Point to the end of string if return not found
      if (ie1 < 0) {
        ie1 = equations.length();
      }
      String currentLine = equations.substring(ic1, ie1);
      ic1 = ie1 + 1;
      String newLine = "";
      int lineLen = currentLine.length();

      // determine if any of the variables are on the current line
      int[] pntrs = new int[nVars];
      boolean changeLine = false;
      for (int i1 = 0; i1 < nVars; i1++) {
        int ib = currentLine.indexOf(oldNames[i1]);
        boolean nameFound = false;
        if (ib >= 0) {
          nameFound = true;
          int ic2 = ib - 1;
          if (ic2 >= 0) {
            String cStr = currentLine.substring(ic2, ic2 + 1);
            if (chList.indexOf(cStr) < 0) {
              nameFound = false;
            }
          }
          ic2 = ib + _oldName1.get().length();
          if (nameFound) {
            if (ic2 < lineLen - 1) {
              String cStr = currentLine.substring(ic2, ic2 + 1);
              if (chList.indexOf(cStr) < 0) {
                nameFound = false;
              }
            }
          }

          // If name found, then save pointer
          if (nameFound) {
            pntrs[i1] = ib;
            changeLine = true;
          } else {
            pntrs[i1] = -1;
          }

          // If name not found, then set pointer to -1
        } else {
          pntrs[i1] = -1;
        }
      }

      // if the line needs to be change then put in the new variable names
      if (changeLine) {
        for (int i1 = 0; i1 < nVars; i1++) {
          int ib = pntrs[i1];
          // If name found, then create a new item with the new variable name
          if (ib >= 0) {
            String newName = repository.lookupVariableName(newVars[i1]);

            // Create new line
            if (ib > 0) {
              newLine = currentLine.substring(0, ib) + newName;
            } else {
              newLine = newName;
            }
            int ic2 = ib + oldNames[i1].length();
            if (ic2 < lineLen) {
              newLine = newLine + currentLine.substring(ic2, lineLen);
            }

            // Change pointers if the new name is a different length than the
            // old name
            if (i1 + 1 < nVars) {
              if (newName.length() != oldNames[i1].length()) {
                for (int i2 = i1 + 1; i2 < nVars; i2++) {
                  if (pntrs[i2] > ib) {
                    pntrs[i2] += newName.length() - oldNames[i1].length();
                  }
                }
              }
            }
            // Change the current line for the next time through the loop
            currentLine = newLine;
            lineLen = currentLine.length();
          }
        }
        // The current line changed so equations have changed
        equationsChanged = true;
      }
      // if line has not changed then leave the current line alone
      newEquations = newEquations + currentLine + "\n";
      lineNum++;
    }

    // If equations changed then set the new equations
    if (equationsChanged) {
      _equations.set(newEquations);
    }
  }

  /**
   * @set the running of equations
   */
  public void runEquations() {

    // remove all shell commands
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] extensions = registry
        .getConfigurationElementsFor("org.geocraft.core.shell.commandexecutor");
    for (IConfigurationElement extension : extensions) {
      try {
        ICommandExecutor commandExecutor = (ICommandExecutor) extension.createExecutableExtension("class");
        commandExecutor.removeAllSavedLines();
      } catch (Exception ex) {
        String text = "Exception:\ncommandExecutor.removeAllSaveLines()\n" + ex.getMessage();
        IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
        ErrorDialog.openError(null, "Error while initializing the shell commands",
            "Unable to initialize the shell commands", status);
        return;
      }
    }

    // Determine the equations to run
    List<String> equationsTxt = new ArrayList<String>();
    String equations = _equations.get();
    int ic = 0;
    while (ic < equations.length()) {
      int ie = equations.indexOf("\n", ic);
      // Point to the end of string if return not found
      if (ie < 0) {
        ie = equations.length();
      }
      String line = equations.substring(ic, ie);
      equationsTxt.add(line);
      ic = ie + 1;
    }

    // run the shell commands
    for (IConfigurationElement extension : extensions) {
      try {
        ICommandExecutor commandExecutor = (ICommandExecutor) extension.createExecutableExtension("class");
        commandExecutor.executeShellCommands(equationsTxt);
      } catch (Exception ex) {
        String text = "Exception:\ncommandExecutor.executeShellCommands(equationsTxt)\n" + ex.getMessage();
        IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
        ErrorDialog.openError(null, "Error while running equations", "Unable run your equations", status);
        return;
      }
    }
  }
}
