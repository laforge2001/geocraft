/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.geocraft.core.session.SessionManager;


/**
 * This workbench advisor creates the window advisor, and specifies the
 * perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

  private static final String MAIN_PERSPECTIVE_ID = "GeoMath.perspective";

  @Override
  public void initialize(final IWorkbenchConfigurer configurer) {
    configurer.setSaveAndRestore(false);
  }

  @Override
  public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
    return new ApplicationWorkbenchWindowAdvisor(configurer);
  }

  @Override
  public String getInitialWindowPerspectiveId() {
    return MAIN_PERSPECTIVE_ID;
  }

  @Override
  public boolean preShutdown() {
    boolean canQuit = true;
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] configs = registry.getConfigurationElementsFor("org.geocraft.product.saveables");
    for (IConfigurationElement config : configs) {
      try {
        ISaveable saveable = (ISaveable) config.createExecutableExtension("saver");
        canQuit = canQuit && saveable.save();
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }

    //Ask the user if they want to save this session before exiting
    String[] buttons = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
        IDialogConstants.CANCEL_LABEL };
    MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
        "Save Session?", null, "Do you want to save this session before exiting ?", MessageDialog.QUESTION, buttons, 0) {

      @Override
      protected int getShellStyle() {
        return SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | getDefaultOrientation();
      }
    };

    int choice = dialog.open();
    switch (choice) {
      case 0: // YES
        // Save the session.
        SessionManager.getInstance().saveSessionAs(true);
        break;
      case 1: // NO
        // Don't save the session.
        break;
      case 2: // CANCEL
        // Don't save and don't exit!
        canQuit = false;
        break;
    }

    if (canQuit) {
      processPreShutdown();
    }
    return canQuit;
  }

  /**
   * 
   */
  private void processPreShutdown() {
    IWorkbench workbench = PlatformUI.getWorkbench();
    String currentPerspective = workbench.getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
    if (currentPerspective.equals("Viewer.perspective")) {
      try {
        workbench.showPerspective(MAIN_PERSPECTIVE_ID, workbench.getActiveWorkbenchWindow());
      } catch (WorkbenchException e) {
        e.printStackTrace();
      }
    }

    if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 1) {
      IPerspectiveDescriptor desc = PlatformUI.getWorkbench().getPerspectiveRegistry()
          .findPerspectiveWithId("Viewer.perspective");
      for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
        // close the Viewers perspective
        if (window.getActivePage().getPerspective() == desc) {
          window.close();
        }
      }
    }
  }

}
