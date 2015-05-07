/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.io;


import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.io.DatastoreEntrySelector;
import org.geocraft.core.io.IDatastoreEntrySelections;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.internal.ui.io.ServiceComponent;


public abstract class DatastoreDirectorySelector extends DatastoreEntrySelector {

  /** Settings file used to keep the main application settings. */
  public static final String DIRECTORY_SETTINGS = "directories.config";

  private final String _datastoreEntity;

  private final String[] _filterNames;

  private final String[] _filterExtensions;

  private final String _preferencesString;

  /** The preferences store. */
  private final IEclipsePreferences _preferencesStore = PreferencesUtil.getPreferencesStore(ServiceComponent.PLUGIN_ID);

  public DatastoreDirectorySelector(final String datastoreEntity, final String[] filterNames, final String[] filterExtensions, final String preferencesString) {
    _datastoreEntity = datastoreEntity;
    _filterNames = new String[filterNames.length];
    System.arraycopy(filterNames, 0, _filterNames, 0, filterNames.length);
    _filterExtensions = new String[filterExtensions.length];
    System.arraycopy(filterExtensions, 0, _filterExtensions, 0, filterNames.length);
    _preferencesString = preferencesString;
  }

  public void select(final IDatastoreEntrySelections selections) {
    final String directory = _preferencesStore.get(_preferencesString, Utilities.getWorkingDirectory());

    final Shell shell = new Shell(Display.getDefault());
    DirectoryDialog dialog = new DirectoryDialog(shell, SWT.MULTI);
    dialog.setFilterPath(directory);
    dialog.setText("Load " + _datastoreEntity + "(s)");
    String rtn = dialog.open();
    if (rtn != null) {
      _preferencesStore.put(_preferencesString, dialog.getFilterPath());
      PreferencesUtil.saveInstanceScopePreferences(ServiceComponent.PLUGIN_ID);
      Set<File> fileList = new HashSet<File>();
      String fullPath = dialog.getFilterPath();
      fileList.add(new File(fullPath));
      File[] files = fileList.toArray(new File[0]);
      String[] names = new String[files.length];
      MapperModel[] models = createMapperModelsFromSelectedFiles(files);
      for (int i = 0; i < files.length; i++) {
        names[i] = FileUtil.getBaseName(files[i].getName());
      }
      selections.add(names, models);
    }
  }

  protected abstract MapperModel[] createMapperModelsFromSelectedFiles(File[] files);

}
