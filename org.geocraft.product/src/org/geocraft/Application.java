/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.product.ApplicationWorkbenchAdvisor;
import org.geocraft.product.workspace.ChooseWorkspaceData;
import org.geocraft.product.workspace.ChooseWorkspaceDialog;


/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

  /** The Eclipse logger. */

  /**
   * The name of the folder containing metadata information for the workspace.
   */
  public static final String METADATA_FOLDER = ".metadata"; //$NON-NLS-1$

  private static final String VERSION_FILENAME = "version.ini"; //$NON-NLS-1$

  private static final String WORKSPACE_VERSION_KEY = "org.eclipse.core.runtime"; //$NON-NLS-1$

  private static final String WORKSPACE_VERSION_VALUE = "1"; //$NON-NLS-1$

  @SuppressWarnings("unused")
  public Object start(final IApplicationContext context) throws Exception {
    ServiceProvider.getLoggingService().getLogger(getClass()).info("Starting GeoCraft");

    Display display = PlatformUI.createDisplay();
    try {

      // this is currently discouraged... do we have a way around it?
      Shell shell = WorkbenchPlugin.getSplashShell(display);
      if (shell != null) {
        shell.setText(ChooseWorkspaceDialog.getWindowTitle());
        shell.setImages(Window.getDefaultImages());
      }

      if (!checkInstanceLocation(shell)) {
        Platform.endSplash();
        return EXIT_OK;
      }

      int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
      if (returnCode == PlatformUI.RETURN_RESTART) {
        return IApplication.EXIT_RESTART;
      }
    } finally {
      ServiceProvider.getLoggingService().getLogger(getClass()).info("Stopping GeoCraft");
      if (display != null) {
        display.dispose();
      }
      Location instanceLoc = Platform.getInstanceLocation();
      if (instanceLoc != null) {
        instanceLoc.release();
      }
    }
    return IApplication.EXIT_OK;
  }

  public void stop() {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    if (workbench == null) {
      return;
    }
    final Display display = workbench.getDisplay();
    display.syncExec(new Runnable() {

      public void run() {
        if (!display.isDisposed()) {
          workbench.close();
        }
      }
    });
  }

  /**
   * Return true if a valid workspace path has been set and false otherwise.
   * Prompt for and set the path if possible and required.
   * 
   * @return true if a valid instance location has been set and false
   *         otherwise
   */
  private boolean checkInstanceLocation(final Shell shell) {
    // -data @none was specified but an ide requires workspace
    Location instanceLoc = Platform.getInstanceLocation();
    if (instanceLoc == null) {
      MessageDialog.openError(shell, "Workspace Error", "A workspace must be set");
      return false;
    }

    // -data "/valid/path", workspace already set
    if (instanceLoc.isSet()) {
      // make sure the meta data version is compatible (or the user has
      // chosen to overwrite it).
      if (!checkValidWorkspace(shell, instanceLoc.getURL())) {
        return false;
      }

      try {
        if (instanceLoc.lock()) {
          return true;
        }

        // we failed to create the directory.  
        // Two possibilities:
        // 1. directory is already in use
        // 2. directory could not be created
        File workspaceDirectory = new File(instanceLoc.getURL().getFile());
        if (workspaceDirectory.exists()) {
          MessageDialog.openError(shell, "Workspace Error", "Workspace already exists");
        } else {
          MessageDialog.openError(shell, "Workspace Error", "Workspace cannot be set");
        }
      } catch (IOException e) {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(e.getMessage());
        MessageDialog.openError(shell, "Could not obtain lock for workspace location", e.getMessage());
      }
      return false;
    }

    // -data @noDefault or -data not specified, prompt and set
    ChooseWorkspaceData launchData = new ChooseWorkspaceData(instanceLoc.getDefault());

    boolean force = false;
    while (true) {
      URL workspaceUrl = promptForWorkspace(shell, launchData, force);
      if (workspaceUrl == null) {
        return false;
      }

      // if there is an error with the first selection, then force the
      // dialog to open to give the user a chance to correct
      force = true;

      try {
        // the operation will fail if the url is not a valid
        // instance data area, so other checking is unneeded
        if (instanceLoc.set(workspaceUrl, true)) {
          launchData.writePersistedData();
          writeWorkspaceVersion();
          return true;
        }
      } catch (IllegalStateException e) {
        e.printStackTrace();
        MessageDialog.openError(shell, "Workspace error", "Workspace cannot be set");
        return false;
      } catch (IOException e) {
        e.printStackTrace();
        MessageDialog.openError(shell, "Workspace error", "Workspace cannot be set");
        return false;
      }

      // by this point it has been determined that the workspace is
      // already in use -- force the user to choose again
      if (MessageDialog.openQuestion(shell, "Workspace is already in use.",
          "This workspace is already in use. Modifying workspace preferences "
              + "may produce inconsistent behavior when you run two or more concurrent versions "
              + "of GeoCraft.\n\nDo you still want to use this workspace location?")) {
        try {
          if (!instanceLoc.set(workspaceUrl, false)) {
            launchData.writePersistedData();
            writeWorkspaceVersion();
            return true;
          }
        } catch (IllegalStateException e) {
          e.printStackTrace();
          MessageDialog.openError(shell, "Workspace error", "Workspace cannot be set");
          return false;
        } catch (IOException e) {
          e.printStackTrace();
          MessageDialog.openError(shell, "Workspace error", "Workspace cannot be set");
          return false;
        }
      }
    }
  }

  private URL promptForWorkspace(final Shell shell, final ChooseWorkspaceData launchData, final boolean force) {
    URL url = null;
    do {
      // okay to use the shell now - this is the splash shell
      new ChooseWorkspaceDialog(shell, launchData, false, true).prompt(force);
      String instancePath = launchData.getSelection();
      if (instancePath == null) {
        return null;
      }

      // the dialog is not forced on the first iteration, but is on every
      // subsequent one -- if there was an error then the user needs to be
      // allowed to fix it
      //force = true;

      // 70576: don't accept empty input
      if (instancePath.length() <= 0) {
        MessageDialog.openError(shell, "Workspace Error", "Workspace cannot be empty");
        continue;
      }

      // create the workspace if it does not already exist
      File workspace = new File(instancePath);
      if (!workspace.exists()) {
        workspace.mkdir();
      }

      try {
        // Don't use File.toURL() since it adds a leading slash that Platform does not
        // handle properly.  See bug 54081 for more details.  
        String path = workspace.getAbsolutePath().replace(File.separatorChar, '/');
        url = new URL("file", null, path); //$NON-NLS-1$
      } catch (MalformedURLException e) {
        MessageDialog.openError(shell, "Workspace Error", "Invalid workspace specified");
        continue;
      }
    } while (!checkValidWorkspace(shell, url));

    return url;
  }

  /**
   * Write the version of the metadata into a known file overwriting any
   * existing file contents. Writing the version file isn't really crucial,
   * so the function is silent about failure
   */
  private static void writeWorkspaceVersion() {
    Location instanceLoc = Platform.getInstanceLocation();
    if (instanceLoc == null || instanceLoc.isReadOnly()) {
      return;
    }

    File versionFile = getVersionFile(instanceLoc.getURL(), true);
    if (versionFile == null) {
      return;
    }

    OutputStream output = null;
    try {
      String versionLine = WORKSPACE_VERSION_KEY + '=' + WORKSPACE_VERSION_VALUE;

      output = new FileOutputStream(versionFile);
      output.write(versionLine.getBytes("UTF-8")); //$NON-NLS-1$
    } catch (IOException e) {
      // TODO log...
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (IOException e) {
        // do nothing
      }
    }
  }

  /**
   * The version file is stored in the metadata area of the workspace. This
   * method returns an URL to the file or null if the directory or file does
   * not exist (and the create parameter is false).
   * 
   * @param create
   *            If the directory and file does not exist this parameter
   *            controls whether it will be created.
   * @return An url to the file or null if the version file does not exist or
   *         could not be created.
   */
  private static File getVersionFile(final URL workspaceUrl, final boolean create) {
    if (workspaceUrl == null) {
      return null;
    }

    try {
      // make sure the directory exists
      File metaDir = new File(workspaceUrl.getPath(), METADATA_FOLDER);
      if (!metaDir.exists() && (!create || !metaDir.mkdir())) {
        return null;
      }

      // make sure the file exists
      File versionFile = new File(metaDir, VERSION_FILENAME);
      if (!versionFile.exists() && (!create || !versionFile.createNewFile())) {
        return null;
      }

      return versionFile;
    } catch (IOException e) {
      // cannot log because instance area has not been set
      return null;
    }
  }

  /**
   * Return true if the argument directory is ok to use as a workspace and
   * false otherwise. A version check will be performed, and a confirmation
   * box may be displayed on the argument shell if an older version is
   * detected.
   * 
   * @return true if the argument URL is ok to use as a workspace and false
   *         otherwise.
   */
  private boolean checkValidWorkspace(final Shell shell, final URL url) {
    // a null url is not a valid workspace
    if (url == null) {
      return false;
    }

    String version = readWorkspaceVersion(url);

    // if the version could not be read, then there is not any existing
    // workspace data to trample, e.g., perhaps its a new directory that
    // is just starting to be used as a workspace
    if (version == null) {
      return true;
    }

    final int ide_version = Integer.parseInt(WORKSPACE_VERSION_VALUE);
    int workspace_version = Integer.parseInt(version);

    // equality test is required since any version difference (newer
    // or older) may result in data being trampled
    if (workspace_version == ide_version) {
      return true;
    }

    // At this point workspace has been detected to be from a version
    // other than the current ide version -- find out if the user wants
    // to use it anyhow.
    String title = "Version";
    String message = "Different workspace version detected: " + url.getFile();

    MessageBox mbox = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
    mbox.setText(title);
    mbox.setMessage(message);
    return mbox.open() == SWT.OK;
  }

  /**
   * Look at the argument URL for the workspace's version information. Return
   * that version if found and null otherwise.
   */
  private static String readWorkspaceVersion(final URL workspace) {
    File versionFile = getVersionFile(workspace, false);
    if (versionFile == null || !versionFile.exists()) {
      return null;
    }

    try {
      // Although the version file is not spec'ed to be a Java properties
      // file, it happens to follow the same format currently, so using
      // Properties to read it is convenient.
      Properties props = new Properties();
      FileInputStream is = new FileInputStream(versionFile);
      try {
        props.load(is);
      } finally {
        is.close();
      }

      return props.getProperty(WORKSPACE_VERSION_KEY);
    } catch (IOException e) {
      ServiceProvider.getLoggingService().getLogger(Application.class).error("Could not read version file", e);
      return null;
    }
  }
}