/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciipointset;


import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.io.IDatastoreLocationSelector;


public class AsciiPointSetLocationSelector implements IDatastoreLocationSelector {

  private final String _preferencesString = "LoadModSpecGrid_DIR";

  public void select() {

    IEclipsePreferences _preferencesStore = PreferencesUtil.getPreferencesStore(AsciiPointSetMapperModel.PLUGIN_ID);
    final String directory = _preferencesStore.get(_preferencesString, Utilities.getWorkingDirectory());

    final Shell shell = new Shell(Display.getDefault());
    DirectoryDialog dialog = new DirectoryDialog(shell, SWT.MULTI);
    dialog.setFilterPath(directory);
    dialog.setText("Save Ascii PointSets(s)");
    String rtn = dialog.open();
    if (rtn != null) {
      _preferencesStore.put(_preferencesString, rtn);
      PreferencesUtil.saveInstanceScopePreferences(AsciiPointSetMapperModel.PLUGIN_ID);
    }
  }

}
