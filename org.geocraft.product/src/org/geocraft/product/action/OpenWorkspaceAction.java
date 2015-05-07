package org.geocraft.product.action;


import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.geocraft.product.workspace.ChooseWorkspaceData;
import org.geocraft.product.workspace.ChooseWorkspaceDialog;


/**
 * Implements the open workspace action. Opens a dialog prompting for a
 * directory and then restarts the IDE on that workspace.
 * 
 * @since 3.0
 */
public class OpenWorkspaceAction extends Action implements ActionFactory.IWorkbenchAction {

  /**
   * Action responsible for opening the "Other..." dialog (ie: the workspace
   * chooser).
   * 
   * @since 3.3
   * 
   */
  class OpenDialogAction extends Action {

    OpenDialogAction() {
      super("&Other...");
      setToolTipText("Open Session");
    }

    @Override
    public void run() {
      OpenWorkspaceAction.this.run();
    }
  }

  /**
   * Action responsible for opening a specific workspace location
   * 
   * @since 3.3
   */
  class WorkspaceMRUAction extends Action {

    protected final ChooseWorkspaceData _data;

    protected final String _location;

    WorkspaceMRUAction(final String location, final ChooseWorkspaceData data) {
      this._location = location; // preserve the location directly -
      // setText mucks with accelerators so we
      // can't necessarily use it safely for
      // manipulating the location later.
      setText(location);
      setToolTipText(location);
      this._data = data;
    }

    @Override
    public void run() {
      _data.workspaceSelected(_location);
      _data.writePersistedData();
      restart(_location);
    }
  }

  private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

  private static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$

  private static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$

  private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

  private static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$

  private static final String CMD_DATA = "-data"; //$NON-NLS-1$

  private static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$

  private static final String NEW_LINE = "\n"; //$NON-NLS-1$

  private IWorkbenchWindow _window;

  /**
   * Set definition for this action and text so that it will be used for File
   * -&gt; Open Workspace in the argument window.
   * 
   * @param window
   *            the window in which this action should appear
   * @deprecated No longer support the "Switch Session" menu item
   */
  @Deprecated
  public OpenWorkspaceAction(final IWorkbenchWindow window) {
    super("Switch &Session", IAction.AS_DROP_DOWN_MENU);

    if (window == null) {
      throw new IllegalArgumentException();
    }

    // TODO help?

    _window = window;
    setToolTipText("Open Session");
    //setActionDefinitionId("org.eclipse.ui.file.openWorkspace"); //$NON-NLS-1$
    setMenuCreator(new IMenuCreator() {

      private MenuManager dropDownMenuMgr;

      /**
       * Creates the menu manager for the drop-down.
       */
      private void createDropDownMenuMgr() {
        if (dropDownMenuMgr == null) {
          dropDownMenuMgr = new MenuManager();
          final ChooseWorkspaceData data = new ChooseWorkspaceData(Platform.getInstanceLocation().getURL());
          data.readPersistedData();
          String current = data.getInitialDefault();
          String[] workspaces = data.getRecentWorkspaces();
          for (int i = 0; i < workspaces.length; i++) {
            if (workspaces[i] != null && !workspaces[i].equals(current)) {
              dropDownMenuMgr.add(new WorkspaceMRUAction(workspaces[i], data));
            }
          }
          if (!dropDownMenuMgr.isEmpty()) {
            dropDownMenuMgr.add(new Separator());
          }
          dropDownMenuMgr.add(new OpenDialogAction());
        }
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
       */
      public Menu getMenu(final Control parent) {
        createDropDownMenuMgr();
        return dropDownMenuMgr.createContextMenu(parent);
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
       */
      public Menu getMenu(final Menu parent) {
        createDropDownMenuMgr();
        Menu menu = new Menu(parent);
        IContributionItem[] items = dropDownMenuMgr.getItems();
        for (IContributionItem item : items) {
          if (item instanceof ActionContributionItem) {
            item = new ActionContributionItem(((ActionContributionItem) item).getAction());
          }
          item.fill(menu, -1);
        }
        return menu;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.action.IMenuCreator#dispose()
       */
      public void dispose() {
        if (dropDownMenuMgr != null) {
          dropDownMenuMgr.dispose();
          dropDownMenuMgr = null;
        }
      }
    });
  }

  @Override
  public void run() {
    String path = promptForWorkspace();
    if (path == null) {
      return;
    }

    restart(path);
  }

  /**
   * Restart the workbench using the specified path as the workspace location.
   * 
   * @param path
   *            the location
   * @since 3.3
   */
  public void restart(final String path) {
    String command_line = buildCommandLine(path);

    System.out.println("Command to restart: " + command_line);
    if (command_line == null) {
      return;
    }

    System.setProperty(PROP_EXIT_CODE, Integer.toString(24));
    System.setProperty(PROP_EXIT_DATA, command_line);
    _window.getWorkbench().restart();
  }

  /**
   * Use the ChooseWorkspaceDialog to get the new workspace from the user.
   * 
   * @return a string naming the new workspace and null if cancel was selected
   */
  private String promptForWorkspace() {
    // get the current workspace as the default
    ChooseWorkspaceData data = new ChooseWorkspaceData(Platform.getInstanceLocation().getURL());
    ChooseWorkspaceDialog dialog = new ChooseWorkspaceDialog(_window.getShell(), data, true, false);
    dialog.prompt(true);

    // return null if the user changed their mind
    String selection = data.getSelection();
    if (selection == null) {
      return null;
    }

    // otherwise store the new selection and return the selection
    data.writePersistedData();
    return selection;
  }

  /**
   * Create and return a string with command line options for eclipse.exe that
   * will launch a new workbench that is the same as the currently running
   * one, but using the argument directory as its workspace.
   * 
   * @param workspace
   *            the directory to use as the new workspace
   * @return a string of command line options or null on error
   */
  private String buildCommandLine(final String workspace) {
    String property = System.getProperty(PROP_VM);
    if (property == null) {
      MessageDialog.openError(_window.getShell(), "Missing system property", NLS.bind(
          "Unable to launch the platform because the {0} property isn't set", PROP_VM));
      return null;
    }

    StringBuffer result = new StringBuffer(512);
    result.append(property);
    result.append(NEW_LINE);

    // append the vmargs and commands. Assume that these already end in \n
    String vmargs = System.getProperty(PROP_VMARGS);
    if (vmargs != null) {
      result.append(vmargs);
    }

    // append the rest of the args, replacing or adding -data as required
    property = System.getProperty(PROP_COMMANDS);
    if (property == null) {
      result.append(CMD_DATA);
      result.append(NEW_LINE);
      result.append(workspace);
      result.append(NEW_LINE);
    } else {
      // find the index of the arg to replace its value
      int cmd_data_pos = property.lastIndexOf(CMD_DATA);
      if (cmd_data_pos != -1) {
        cmd_data_pos += CMD_DATA.length() + 1;
        result.append(property.substring(0, cmd_data_pos));
        result.append(workspace);
        result.append(property.substring(property.indexOf('\n', cmd_data_pos)));
      } else {
        result.append(CMD_DATA);
        result.append(NEW_LINE);
        result.append(workspace);
        result.append(NEW_LINE);
        result.append(property);
      }
    }

    // put the vmargs back at the very end (the eclipse.commands property
    // already contains the -vm arg)
    if (vmargs != null) {
      result.append(CMD_VMARGS);
      result.append(NEW_LINE);
      result.append(vmargs);
    }

    return result.toString();
  }

  public void dispose() {
    _window = null;
  }
}
