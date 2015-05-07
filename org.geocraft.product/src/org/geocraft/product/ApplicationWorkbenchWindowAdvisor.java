/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.GeoCraftVersion;
import org.geocraft.core.session.SessionManager;


public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

  /** Property file containing user preferences, in particular, the path of the last active session. */
  static final String USER_PREFS = ".userPreferences.properties";

  static final String LAST_ACTIVE_SESSION_PROP = "lastActiveSession";

  static final String RESTORE_ON_LAUNCH_PROP = "restoreOnLaunch";

  static boolean initialLaunch = true;

  public ApplicationWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
    super(configurer);
  }

  @Override
  public ActionBarAdvisor createActionBarAdvisor(final IActionBarConfigurer configurer) {
    return new ApplicationActionBarAdvisor(configurer);
  }

  @Override
  public void preWindowOpen() {

    PrefUtil.getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, true);
    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
    configurer.setInitialSize(new Point(1024, 768));
    configurer.setShowPerspectiveBar(true);
    configurer.setShowCoolBar(false); // when we will have something in the toolbar, we will show it again
    configurer.setShowStatusLine(true);
    configurer.setShowProgressIndicator(true);
    configurer.setTitle(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + " - "
        + configurer.getTitle() + " " + GeoCraftVersion.getCurrentVersion());
    PreferencesUtil.getPreferencesStore("org.geocraft.ui.io").put("Workspace_DIR",
        ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());

  }

  @Override
  public void postWindowOpen() {
    //Note: This method is called each time a workbench window is opened, for example,
    //      when a session is restored. Only want to restore an active session when
    //      initially launch Geocraft.
    boolean isRestoreOnLaunch = false;
    String sessionPath = "";
    if (initialLaunch) {
      initialLaunch = false;
      //If the last active session was specified to be restored upon launch,
      //do so; otherwise, come up normally
      String workspaceDir = Platform.getLocation().toString();
      Properties userPrefs = new Properties();
      try {
        FileInputStream fis = new FileInputStream(workspaceDir + File.separatorChar + USER_PREFS);
        userPrefs.load(fis);
        String lasProp = userPrefs.getProperty(LAST_ACTIVE_SESSION_PROP);
        if (lasProp != null) {
          String rolProp = userPrefs.getProperty(RESTORE_ON_LAUNCH_PROP);
          if (rolProp != null && rolProp.equals("true")) {
            isRestoreOnLaunch = true;
            sessionPath = lasProp;
          }
        }
      } catch (FileNotFoundException e) {
        //no .userPreferences.properties file
      } catch (IOException e) {
        //cannot read .userPreferences.properties file
      }
    }

    if (isRestoreOnLaunch) {
      SessionManager.getInstance().restoreSession(sessionPath, true);
      return;
    }

    //Launch normally if workspace has no user preferences or there is
    //a last active session, but it was not specified to be restored
    //on launch.
    IWorkbench workbench = PlatformUI.getWorkbench();
    IPerspectiveRegistry reg = workbench.getPerspectiveRegistry();

    // the currently opened window should be the active one
    IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
    if (workbench.getIntroManager().getIntro() == null) {
      if (activeWindow.getActivePage().getPerspective().getId().equals("Viewer.perspective")) {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowPerspectiveBar(false);
        configurer.setShowMenuBar(false);
      } else if (Platform.getProduct().getId().equals("org.geocraft.abavo.product.product")) {
        activeWindow.getActivePage().setPerspective(reg.findPerspectiveWithId("org.geocraft.abavo.abavo3dPerspective"));
      }
    }
  }

  @Override
  public boolean preWindowShellClose() {
    if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 1
        && !getWindowConfigurer().getWindow().getActivePage().getPerspective().getId().equals("Viewer.perspective")) {
      IPerspectiveDescriptor desc = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(
          "Viewer.perspective");
      for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
        // close the Viewers perspective
        if (window.getActivePage().getPerspective() == desc) {
          window.close();
        }
      }
    }
    return super.preWindowShellClose();
  }
}
