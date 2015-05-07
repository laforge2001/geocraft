package org.geocraft.ui.io.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.io.DatastoreSelectionDialog;


/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class LoadAction implements IWorkbenchWindowActionDelegate {

  private IWorkbenchWindow _window;

  public LoadAction() {
    // The empty constructor used by OSGI.
  }

  /**
   * The action has been activated. The argument of the method represents the
   * 'real' action sitting in the workbench UI.
   * 
   * @see IWorkbenchWindowActionDelegate#run
   */
  public void run(final IAction action) {

    // this section of code just checks that the data store is accessible. 
    IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
    if (datastoreAccessorService == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error("No datastore accessor service found.");
      return;
    }

    // display the dialog. 
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    final DatastoreSelectionDialog datastoreDialog = DatastoreSelectionDialog.createInputDialog(shell);
    datastoreDialog.create();

    datastoreDialog.getShell().pack();
    Point size = datastoreDialog.getShell().computeSize(500, 400);
    datastoreDialog.getShell().setSize(size);

    // We are already on the SWT thread, but we ensure
    // this way that the setActive() will be called after the open().
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        datastoreDialog.getShell().setActive();
      }
    });
    datastoreDialog.open();
  }

  /**
   * Selection in the workbench has been changed. We can change the state of the
   * 'real' action here if we want, but this can only happen after the delegate
   * has been created.
   * 
   * @see IWorkbenchWindowActionDelegate#selectionChanged
   */
  public void selectionChanged(final IAction action, final ISelection selection) {
    // No action for selected changed.
  }

  /**
   * We can use this method to dispose of any system resources we previously
   * allocated.
   * 
   * @see IWorkbenchWindowActionDelegate#dispose
   */
  public void dispose() {
    // No resources to dispose of.
  }

  /**
   * We will cache window object in order to be able to provide parent shell for
   * the message dialog.
   * 
   * @see IWorkbenchWindowActionDelegate#init
   */
  public void init(final IWorkbenchWindow window) {
    _window = window;
  }
}