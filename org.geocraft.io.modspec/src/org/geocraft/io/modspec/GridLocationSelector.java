package org.geocraft.io.modspec;


import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.io.IDatastoreLocationSelector;
import org.geocraft.internal.io.modspec.ServiceComponent;


public class GridLocationSelector implements IDatastoreLocationSelector {

  private final String _preferencesString = "SaveModSpecGrid_DIR";

  /** The preferences store. */
  private final IEclipsePreferences _preferencesStore = PreferencesUtil.getPreferencesStore(ServiceComponent.PLUGIN_ID);

  public GridLocationSelector() {
    // No action.
  }

  public void select() {
    final String directory = _preferencesStore.get(_preferencesString, Utilities.getWorkingDirectory());

    final Shell shell = new Shell(Display.getDefault());
    DirectoryDialog dialog = new DirectoryDialog(shell, SWT.MULTI);
    dialog.setFilterPath(directory);
    dialog.setText("Save ModSpec Grid(s)");
    String rtn = dialog.open();
    if (rtn != null) {
      _preferencesStore.put(_preferencesString, rtn);
      PreferencesUtil.saveInstanceScopePreferences(ServiceComponent.PLUGIN_ID);
    }
  }

}
