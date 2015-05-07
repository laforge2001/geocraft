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
 * This class defines the action for saving an ellipse regions model.
 * The user is prompted to select a file on disk using a file
 * selection dialog. The application model is then written.
 */
public class SaveEllipseRegionsModel extends Action {

  /** The string for storing the last-used directory in the preferences. */
  private final String _preferencesString = "ABavoEllipseRegionsModel_DIR";

  /** The preferences store. */
  private final IEclipsePreferences _preferencesStore = PreferencesUtil.getPreferencesStore(ServiceComponent.PLUGIN_ID);

  /** The parent control, used for dialog location. */
  private final Control _parent;

  /** The ellipse regions model in to store on disk. */
  private final EllipseRegionsModel _model;

  public SaveEllipseRegionsModel(final Control parent, final EllipseRegionsModel model) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_SAVE_AS));
    _parent = parent;
    _model = model;
  }

  @Override
  public void run() {
    // Get the last-used directory for ellipse region models from the preferences.
    final String directory = _preferencesStore.get(_preferencesString, Utilities.getWorkingDirectory());

    // Create the file selection dialog.
    Shell shell = new Shell(Display.getCurrent());
    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
    dialog.setFilterPath(directory);
    dialog.setFilterNames(new String[] { "ABAVO Ellipse Model (*." + EllipseRegionsModel.FILE_EXTENSION + ")" });
    dialog.setFilterExtensions(new String[] { "*." + EllipseRegionsModel.FILE_EXTENSION });
    dialog.setText("Save Ellipse Model");
    shell.setLocation(_parent.getLocation());

    // Open the file selection dialog and get the result.
    String result = dialog.open();
    if (result != null && result.length() > 0) {

      File file = new File(result);

      // Check the file extension.
      boolean validFile = file.getAbsolutePath().endsWith("." + EllipseRegionsModel.FILE_EXTENSION);
      // Append a valid file extension if necessary.
      if (!validFile) {
        file = new File(file.getPath() + "." + EllipseRegionsModel.FILE_EXTENSION);
      }
      if (file.exists()) {
        if (!MessageDialog.openConfirm(shell, "File I/O Warning", "The file \'" + file.getAbsolutePath() + "\' already exists.\nDo you wish to overwrite?")) {
          return;
        }
      }

      try {
        // Store the directory location in the preferences.
        _preferencesStore.put(_preferencesString, file.getParent());
        PreferencesUtil.saveInstanceScopePreferences(ServiceComponent.PLUGIN_ID);

        // Write the model to the file.
        _model.writeSession(file);
        String message = "Ellipse regions model saved:\n\'" + file.getAbsolutePath() + "\'";
        MessageDialog.openInformation(shell, "File I/O", message);
      } catch (Exception ex) {
        // Show the user an error message.
        UserAssistMessageBuilder message = new UserAssistMessageBuilder();
        message.setDescription("Unable to write ellipse regions model file.");
        message.addReason(ex.toString());
        MessageDialog.openError(shell, "File I/O Error", message.toString());
      }
    }
    shell.dispose();
  }
}