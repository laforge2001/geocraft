package org.geocraft.ui.viewer;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.core.preferences.IGeocraftPreferencePage;


public class DataViewerPreferences extends PreferencePage implements IWorkbenchPreferencePage, IGeocraftPreferencePage {

  public DataViewerPreferences() {
    // TODO Auto-generated constructor stub
  }

  public DataViewerPreferences(String title) {
    super(title);
    // TODO Auto-generated constructor stub
  }

  public DataViewerPreferences(String title, ImageDescriptor image) {
    super(title, image);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected Control createContents(Composite parent) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void init(IWorkbench workbench) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#getPreferenceState()
   */
  @Override
  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    return prefState;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.preferences.IGeocraftPreferencePage#setPreferenceState(java.util.Map)
   */
  @Override
  public void setPreferenceState(Map<String, String> prefs) {
    // TODO Auto-generated method stub

  }

}
