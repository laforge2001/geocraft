package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.layer.IViewLayer;


/**
 * Defines the action for removing a layer from a viewer.
 */
public class RemoveViewLayer extends Action {

  /** The view layer. */
  private final IViewLayer _viewLayer;

  private final boolean _promptUser;

  public RemoveViewLayer(final IViewLayer viewLayer, final boolean promptUser) {
    super("Remove from view");
    _viewLayer = viewLayer;
    _promptUser = promptUser;
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_FORM_CLOSE));
  }

  @Override
  public void run() {
    if (_viewLayer == null) {
      return;
    }
    boolean confirmed = true;
    if (_promptUser) {
      Shell shell = Display.getCurrent().getActiveShell();
      if (shell != null) {
        confirmed = MessageDialog.openConfirm(shell, "Confirm Removal", "Confirm removal of \'" + _viewLayer.getName()
            + "\'?");
      }
    }
    if (confirmed) {
      _viewLayer.remove();
    }

  }
}
