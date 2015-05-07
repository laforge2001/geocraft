/*
 * Copyright (C) ConocoPhillips 2006 - 2008 All Rights Reserved.
 */

package org.geocraft.internal.abavo.ellipse;


import java.io.File;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.UserAssistMessageBuilder;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.internal.abavo.ServiceComponent;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


/**
 * This class defines the action for loading an ellipse regions model.
 * The user is prompted to select a model from from disk using a
 * file selection dialog. The selected model is then read and loaded
 * into the application model.
 */
public class LoadEllipseRegionsModel extends Action {

  /** The string for storing the last-used directory in the preferences. */
  private final String _preferencesString = "ABavoEllipseRegionsModel_DIR";

  /** The preferences store. */
  private final IEclipsePreferences _preferencesStore = PreferencesUtil.getPreferencesStore(ServiceComponent.PLUGIN_ID);

  /** The parent control, used for dialog location. */
  private final Control _parent;

  /** The ellipse regions model in which to store the loaded model. */
  private final EllipseRegionsModel _model;

  public LoadEllipseRegionsModel(final Control parent, final EllipseRegionsModel model) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_RELOAD));
    _parent = parent;
    _model = model;
  }

  @Override
  public void run() {
    // Get the last-used directory for ellipse region models from the preferences.
    final String directory = _preferencesStore.get(_preferencesString, Utilities.getWorkingDirectory());

    // Create the file selection dialog.
    Shell shell = new Shell(Display.getCurrent());
    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
    dialog.setFilterPath(directory);
    dialog.setFilterNames(new String[] { "ABAVO Ellipse Model (*." + EllipseRegionsModel.FILE_EXTENSION + ")" });
    dialog.setFilterExtensions(new String[] { "*." + EllipseRegionsModel.FILE_EXTENSION });
    dialog.setText("Load Ellipse Model");
    shell.setLocation(_parent.getLocation());

    // Open the file selection dialog and get the result.
    String result = dialog.open();
    if (result != null && result.length() > 0) {

      File file = new File(result);

      // Check the file extension.
      boolean validFile = file.getAbsolutePath().endsWith("." + EllipseRegionsModel.FILE_EXTENSION);

      if (!validFile) {
        // Show the user an error message.
        UserAssistMessageBuilder message = new UserAssistMessageBuilder();
        message.setDescription("Unable to read ellipse model file.");
        message.addReason("Invalid file type.");
        message.addSolution("Select a valid ellipse model file (*." + EllipseRegionsModel.FILE_EXTENSION + ").");
        MessageDialog.openError(shell, "File I/O Error", message.toString());
      } else {
        try {
          // Store the directory location in the preferences.
          _preferencesStore.put(_preferencesString, file.getParent());
          PreferencesUtil.saveInstanceScopePreferences(ServiceComponent.PLUGIN_ID);

          // Read the model from the file.
          _model.readSession(file);
          String message = "Ellipse regions model loaded:\n\'" + file.getAbsolutePath() + "\'";
          MessageDialog.openInformation(shell, "File I/O", message);
        } catch (Exception ex) {
          // Show the user an error message.
          UserAssistMessageBuilder message = new UserAssistMessageBuilder();
          message.setDescription("Unable to read ellipse model file.");
          message.addReason(ex.toString());
          MessageDialog.openError(shell, "File I/O Error", message.toString());
        }
      }
    }
    shell.dispose();
  }
}
