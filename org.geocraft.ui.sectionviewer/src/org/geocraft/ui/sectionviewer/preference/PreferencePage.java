package org.geocraft.ui.sectionviewer.preference;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.core.preferences.IGeocraftPreferencePage;


public class PreferencePage extends org.eclipse.jface.preference.PreferencePage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage {

  public PreferencePage() {
    // TODO Auto-generated constructor stub
  }

  public PreferencePage(final String title) {
    super(title);
    // TODO Auto-generated constructor stub
  }

  public PreferencePage(final String title, final ImageDescriptor image) {
    super(title, image);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected Control createContents(final Composite parent) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void init(final IWorkbench workbench) {
    // TODO Auto-generated method stub

  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    //there are no preferences
    return prefState;
  }

  public void setPreferenceState(final Map<String, String> prefs) {
    //there are no preferences
  }

}
