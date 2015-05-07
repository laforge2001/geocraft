package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.util.UserAssistMessageBuilder;
import org.geocraft.ui.viewer.layer.IViewLayer;


/**
 * Defines the action for renaming a view layer.
 */
public class RenameViewLayer extends Action {

  /** The view layer. */
  private final IViewLayer _viewLayer;

  public RenameViewLayer(final IViewLayer viewLayer) {
    super("Rename...");
    _viewLayer = viewLayer;
  }

  @Override
  public void run() {
    if (_viewLayer == null) {
      return;
    }
    Shell shell = new Shell(Display.getCurrent());
    RenameDialog dialog = new RenameDialog(shell, "Rename Layer", "Enter a new name for the layer", _viewLayer.getName());
    dialog.create();
    int rtn = dialog.open();
    if (rtn == Window.OK) {
      String name = dialog.getName();
      if (name == null || name.length() == 0) {

        UserAssistMessageBuilder message = new UserAssistMessageBuilder();
        message.setDescription("Could not rename layer.");
        message.addReason("No name specified");
        message.addSolution("Enter a valid layer name (length > 0).");

        MessageDialog.openError(shell, "Rename Layer Error", message.toString());
        return;
      }
      _viewLayer.setName(name);
    }
  }
}
