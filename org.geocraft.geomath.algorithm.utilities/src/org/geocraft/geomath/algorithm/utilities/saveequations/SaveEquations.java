/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.utilities.saveequations;


import java.io.File;
import java.io.FileOutputStream;
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
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.FileProperty;
import org.geocraft.core.model.property.StringArrayProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.shell.ICommandExecutor;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ButtonField;
import org.geocraft.ui.form2.field.DirectoryField;
import org.geocraft.ui.form2.field.TextField;


public class SaveEquations extends StandaloneAlgorithm {

  // UI TYPES
  // Input section

  /** The current equations */
  public StringArrayProperty _equations;

  /* Action to save equations to a file */
  protected final BooleanProperty _saveEquations;

  /* Action to update equations from the Jython window */
  protected final BooleanProperty _updateEquations;

  /** The output directory name */
  private FileProperty _outputDirectory;

  private String _directoryName;

  /** The output file name */
  private StringProperty _outputFileName;

  private String _fileExtn = "py";

  public SaveEquations() {
    super();
    _equations = addStringArrayProperty("Equations");
    _saveEquations = addBooleanProperty("Save Equations", false);
    _updateEquations = addBooleanProperty("Update Equations", false);

    _outputDirectory = addFileProperty("Output Directory");
    _outputFileName = addStringProperty("Output File Name", "equations1");

    // Default the directory to the Preference key otherwise use the working directory
    _directoryName = PreferencesUtil.getPreferencesStore("org.geocraft.ui.io").get("Workspace_DIR",
        Utilities.getWorkingDirectory());
    File directory = new File(_directoryName);
    _outputDirectory.set(directory);
  }

  @Override
  public void propertyChanged(String key) {

    if (key.equals(_saveEquations.getKey())) {

      saveEquations(null);
    } else if (key.equals(_updateEquations.getKey())) {

      updateEquations();
    }
  }

  @Override
  public void validate(IValidation results) {

    // Make sure the volume to match is non-null
    if (_outputDirectory.isNull()) {
      results.error(_outputDirectory, "The Output directory has not been specified");
    }

    // Make sure a variable names have been specified
    if (_outputFileName.isEmpty()) {
      results.error(_outputFileName, "The output file name has not been specified");
    }

    // Check if an entry already exists in the datastore.
    if (!_outputDirectory.isNull() && !_outputFileName.isEmpty()) {
      String outputFilePath = _outputDirectory.get().getPath() + File.separator + _outputFileName.get() + "."
          + _fileExtn;
      File file = new File(outputFilePath);
      if (file.exists()) {
        results.warning(_outputFileName, "Exists and will be overwritten.");
      }
    }

  }

  /* (non-Javadoc)
   * Construct the algorithm's UI consisting of form fields partitioned into sections: Input,
   * Output, and algorithm Parameters.
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.algorithm.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {

    FormSection equationsSection = modelForm.addSection("Equations", false);
    equationsSection.addDefinedListField(_equations.getKey());

    // Build the actions section.
    FormSection actionsSection = modelForm.addSection("Actions", false);

    BooleanProperty[] properties = { _saveEquations, _updateEquations };
    String[] buttonNames = { "Save Equations", "Update Equations" };
    ButtonField actionFields = actionsSection.addButtonsField(properties, "Actions", buttonNames, 0);
    actionFields.setTooltip("Save equations to a file", 0);
    actionFields.setTooltip("Update equations by reading the current equations that are in the Jython console", 1);

    // Default the directory to the Preference key otherwise use the working directory
    String directoryName = PreferencesUtil.getPreferencesStore("org.geocraft.ui.io").get("Workspace_DIR",
        Utilities.getWorkingDirectory());

    // Build the output parameters section.
    FormSection outputSection = modelForm.addSection("Output", false);
    DirectoryField directoryField = outputSection.addDirectoryField(_outputDirectory, directoryName);
    directoryField.setTooltip("Directory to contain the equations file");

    TextField outputFileNameField = outputSection.addTextField(_outputFileName);
    outputFileNameField.setTooltip("Name of the file to save the equations to");

  }

  /**
   * Runs the domain logic of the algorithm.
   * @param monitor the progress monitor.
   * @param logger the logger to log messages.
   * @param repository the repository in which to add output entities.
   */

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // save the equations
    saveEquations(monitor);
  }

  /**
   * @get the Equations
   */
  public List<String> getEquations() {
    List<String> commands = new ArrayList<String>();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] extensions = registry
        .getConfigurationElementsFor("org.geocraft.core.shell.commandexecutor");
    for (IConfigurationElement extension : extensions) {
      try {
        ICommandExecutor commandExecutor = (ICommandExecutor) extension.createExecutableExtension("class");
        commands = commandExecutor.getShellCommands();
      } catch (Exception e) {
        String text = "Exception:\ncommendExecutor.getShellCommands()\n" + e.getMessage();
        IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
        ErrorDialog.openError(null, "Error while getting shell commands", "Unable to get the shell commands", status);
        return null;
      }
    }

    return commands;
  }

  /**
   * @set the saving of equations
   */
  public void saveEquations(final IProgressMonitor progressMonitor) {
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
    monitor.beginTask("Save Equations", 100);

    // Generate the output file name
    // (add the extension if needed)
    String directory = _outputDirectory.get().getPath();
    String outputFileName = _outputFileName.get();
    if (!outputFileName.contains(".")) {
      outputFileName = outputFileName + "." + _fileExtn;
    }
    String filePath = outputFileName;
    if (!outputFileName.startsWith("/")) {
      filePath = directory + File.separator + outputFileName;
    }

    File file = new File(filePath);
    monitor.worked(50);

    try {
      writeEquations(file);
    } catch (Exception ex) {
      String text = "File I/O Error: " + ex.getMessage();
      IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
      ErrorDialog.openError(null, "Error while writing equations to file", "Unable to write equations to file", status);
    }
    monitor.worked(50);
    monitor.done();
    if (progressDialog != null) {
      progressDialog.close();
    }
  }

  /**
   * @set the updating of equations
   */
  public void updateEquations() {

    // Initialize a new list of equations
    String[] empty = {};
    _equations.set(empty);

    // add equations in Jython console
    List<String> commands = getEquations();
    for (String line : commands) {
      _equations.add(line);
    }
  }

  /**
   * Writes the equations file.
   * 
   * @param file
   *          the equations file to write.
   * @exception Exception
   *              if there is an error writing the equations file.
   */
  protected void writeEquations(final File file) {

    FileOutputStream ostream;
    String text;

    // Check file pointer.
    text = "Exception:\nwriteEquations(file)\nNull File Pointer";
    if (file == null) {
      IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
      ErrorDialog.openError(null, "Error while writing equations to file", "Unable to equations to file", status);
      return;
    }

    try {
      // Create file output stream.
      ostream = new FileOutputStream(file);
      // Write equations
      writeEquations(ostream);
      // Close file output stream.
      ostream.close();

    } catch (Exception ex) {
      text = "Exception:\nwriteEquations(file)\n" + ex.getMessage();
      IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
      ErrorDialog.openError(null, "Error while writing equations to file", "Unable to equations to file", status);
      return;
    }
  }

  /**
   * Writes the equations file.
   * 
   * @param ostream
   *          the equations file output stream to write.
   * @exception Exception
   *              if there is an error writing the equations file output stream.
   */
  protected void writeEquations(final FileOutputStream ostream) throws Exception {

    String equations = "";
    String[] commands = _equations.get();
    for (String line : commands) {
      equations = equations + line + "\n";
    }
    ostream.write(equations.getBytes());
  }

}
